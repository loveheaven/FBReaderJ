/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

#include <cctype>
#include <android/log.h>
#include "GujiBookReader.h"
#include "../../library/Book.h"
#include "../../bookmodel/BookModel.h"

GujiBookReader::GujiBookReader(BookModel &model, const GujiTextFormat &format, const std::string &encoding) :
	GujiReader(encoding), BookReader(model), myFormat(format), myModel(model),myLastString(""),
	myIsNeedLineHandler(true), myIsInSectionTitle(false), myIsAfterBookTitle(false) {
}

void GujiBookReader::internalEndParagraph() {
	if (!myLastLineIsEmpty) {
		//myLineFeedCounter = 0;
		myLineFeedCounter = -1; /* Fixed by Hatred: zero value was break LINE INDENT formater -
		                           second line print with indent like new paragraf */
	}
	myLastLineIsEmpty = true;
	endParagraph();
}
int replace_all(std::string& str, const std::string& pattern, const std::string& newpat)
{
	int count = 0;
	const size_t nsize = newpat.size();
	const size_t psize = pattern.size();

	for(size_t pos = str.find(pattern, 0);pos != std::string::npos;pos = str.find(pattern,pos + nsize))
	{
		str.replace(pos, psize, newpat);
		count++;
	}

	return count;
}

bool GujiBookReader::titleHandler(std::string &str) {
	const shared_ptr<Book> myBook = myModel.book();
	myBook->setTitle(str.substr(7, str.length()-9));
	myIsNeedLineHandler = true;
	if(!myIsAfterBookTitle) {
		myIsAfterBookTitle = true;
	}

	beginParagraph();//表示正文的一个段落开始
	beginContentsParagraph(model().bookTextModel()->paragraphsNumber() - 1);//表示一个目录的开始。用于实现目录的嵌套
	enterTitle();
	addControl(TITLE, true);


	addData(str.substr(7, str.length()-9));
	addContentsData(str.substr(7, str.length()-9));
	exitTitle();
	//popKind();
	endContentsParagraph();
	addControl(TITLE, false);
	//this->internalEndParagraph();
}

bool GujiBookReader::sectionHandler(std::string &str) {
	if(str.find("}") != std::string::npos) {
		if(str.find("\\section{") == 0 ) {
			if(myIsAfterBookTitle) {
				myIsAfterBookTitle = false;
			} else {
				//internalEndParagraph();
				//insertEndOfSectionParagraph();
				//__android_log_print(ANDROID_LOG_INFO, "love", "!!!!!666sectionHandlerSSS!!%d",model().bookTextModel()->paragraphsNumber());
				//beginParagraph();
			}

			beginContentsParagraph(model().bookTextModel()->paragraphsNumber() - 1);
			enterTitle();
			addControl(GUJI_SECTIONTITLE, true);

			addData(str.substr(9, str.find("}")-9));
			addContentsData(str.substr(9, str.find("}")-9));
			exitTitle();


			addControl(GUJI_SECTIONTITLE, false);
			this->internalEndParagraph();
			this->beginParagraph();
			myIsNeedLineHandler = false;
			myIsInSectionTitle = false;
		} else {
			addData(str.substr(0, str.find("}")));
			addContentsData(str.substr(0, str.find("}")));
			exitTitle();


			//popKind();
			addControl(GUJI_SECTIONTITLE, false);
			this->internalEndParagraph();
			this->beginParagraph();
			myIsNeedLineHandler = false;
			myIsInSectionTitle = false;
		}
	} else {
		if(str.find("\\section{") == 0 ) {
			if(myIsAfterBookTitle) {
				myIsAfterBookTitle = false;
			} else {
				//internalEndParagraph();
				//insertEndOfSectionParagraph();
				//beginParagraph();
			}
			myIsNeedLineHandler = false;
			myIsInSectionTitle = true;
			beginContentsParagraph(model().bookTextModel()->paragraphsNumber() - 1);
			enterTitle();
			addControl(GUJI_SECTIONTITLE, true);

			addData(str.substr(9, str.length()-9));
			addContentsData(str.substr(9, str.length()-9));
		} else {
			addData(str);
			addContentsData(str);
			myIsNeedLineHandler = false;
		}
	}
}

void GujiBookReader::insertEndOfSectionParagraph() {
	if (model().bookTextModel() != 0) {
		std::size_t size = model().bookTextModel()->paragraphsNumber();
		if (size > 0) {
			endParagraph();
			((ZLTextPlainModel&)(*model().bookTextModel())).createParagraph(ZLTextParagraph::END_OF_SECTION_PARAGRAPH);
		}
	}
}

bool GujiBookReader::characterDataHandler(std::string &str) {
	const char *ptr = str.data();

	if(myLastString.length() > 0) {
		str= myLastString + str;
		myLastString.erase();
	}

	if(str.find("\\title{") == 0) {
		titleHandler(str);
	} else if(str.find("\\section{") == 0 || myIsInSectionTitle) {
		sectionHandler(str);
	} else if(str.find("\\endSection") == 0) {
		endContentsParagraph();//表示一个目录的结束。用于实现目录的嵌套
		model().bookTextModel()->popParagraph();
		insertEndOfSectionParagraph();
		//pushKind(CONTENTS_TABLE_ENTRY);
		//beginParagraph();

			//this->internalEndParagraph();
			//popKind();
		myIsNeedLineHandler = true;
	} else if(str.find("\\") == 0 && str.length() < 11 && (str.rfind("\n") != (str.length() - 1))) {
		myLastString.erase();
		myLastString = str;
		myIsNeedLineHandler = false;
	} else {
		const char *end = ptr + str.length();
		myIsNeedLineHandler = true;
		for (; ptr != end; ++ptr) {
			if (isspace((unsigned char)*ptr)) {
				if (*ptr != '\t') {
					++mySpaceCounter;
				} else {
					mySpaceCounter += myFormat.ignoredIndent() + 1; // TODO: implement single option in GujiTextFormat
				}
			} else {
				myLastLineIsEmpty = false;
				break;
			}
		}
		if (ptr != end) {//is the first line of an paragraph because it has some text
			if(myIsAfterBookTitle) {
				//beginParagraph();
				myIsAfterBookTitle = false;
			}
//			if ((myFormat.breakType() & GujiTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT) &&
//					myNewLine && (mySpaceCounter > myFormat.ignoredIndent())) {
//				internalEndParagraph();
//				beginParagraph();
//			}

			ptr = end - str.length();
			const char * start = end - str.length();

			while(ptr != end) {
				if(*ptr == '|' && (ptr+1) != end && *(ptr+1) == '{') {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_TRANSLATION;
					addControl(myKind, true);
					ptr += 1;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+3) != end 
					&& *(ptr+1) == 'h'
					&& *(ptr+2) == '1'
					&& *(ptr+3) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = H1;
					addControl(myKind, true);
					ptr += 3;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+3) != end 
					&& *(ptr+1) == 'h'
					&& *(ptr+2) == '2'
					&& *(ptr+3) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = H2;
					addControl(myKind, true);
					ptr += 3;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+3) != end 
					&& *(ptr+1) == 'h'
					&& *(ptr+2) == '3'
					&& *(ptr+3) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = H3;
					addControl(myKind, true);
					ptr += 3;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+3) != end 
					&& *(ptr+1) == 'h'
					&& *(ptr+2) == '4'
					&& *(ptr+3) == '{'					
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = H4;
					addControl(myKind, true);
					ptr += 3;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+3) != end
					&& *(ptr+1) == 'c'
					&& *(ptr+2) == 'r'
					&& *(ptr+3) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_CR;
					addControl(myKind, true);
					ptr += 3;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+3) != end
					&& *(ptr+1) == 'p'
					&& *(ptr+2) == 's'
					&& *(ptr+3) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_PARAGRAPHSTART;
					addControl(myKind, true);
					ptr += 3;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+5) != end
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'e'
					&& *(ptr+3) == 'c'
					&& *(ptr+4) == '1'
					&& *(ptr+5) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SECTIONTITLE1;
					addControl(myKind, true);
					ptr += 5;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+5) != end
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'e'
					&& *(ptr+3) == 'c'
					&& *(ptr+4) == '2'
					&& *(ptr+5) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SECTIONTITLE2;
					addControl(myKind, true);
					ptr += 5;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+5) != end
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'e'
					&& *(ptr+3) == 'c'
					&& *(ptr+4) == '3'
					&& *(ptr+5) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SECTIONTITLE3;
					addControl(myKind, true);
					ptr += 5;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+5) != end
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'e'
					&& *(ptr+3) == 'c'
					&& *(ptr+4) == '4'
					&& *(ptr+5) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SECTIONTITLE4;
					addControl(myKind, true);
					ptr += 5;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+5) != end
					&& *(ptr+1) == 'a'
					&& *(ptr+2) == 'n'
					&& *(ptr+3) == 'n'
					&& *(ptr+4) == 'o'
					&& *(ptr+5) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_ANNOTATION;
					addControl(myKind, true);
					ptr += 5;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+6) != end
					&& *(ptr+1) == 't'
					&& *(ptr+2) == 'a'
					&& *(ptr+3) == 'n'
					&& *(ptr+4) == 'n'
					&& *(ptr+5) == 'o'
					&& *(ptr+6) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_TITLEANNOTATION;
					addControl(myKind, true);
					ptr += 6;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+5) != end
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'u'
					&& *(ptr+3) == 'b'
					&& *(ptr+4) == 't'
					&& *(ptr+5) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SUBTITLE;
					addControl(myKind, true);
					ptr += 5;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+4) != end
					&& *(ptr+1) == 'c'
					&& *(ptr+2) == 'o'
					&& *(ptr+3) == 'm'
					&& *(ptr+4) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_COMMENT;
					addControl(myKind, true);
					ptr += 4;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+4) != end 
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'u'
					&& *(ptr+3) == 'b'				
					&& *(ptr+4) == '{' 
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SUBSCRIPT;
					addControl(myKind, true);
					ptr += 4;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+4) != end 
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'u'
					&& *(ptr+3) == 'p'				
					&& *(ptr+4) == '{' 
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_SUPERSCRIPT;
					addControl(myKind, true);
					ptr += 4;
					start =ptr+1;
				} else if(*ptr == '\\' && (ptr+7) != end
					&& *(ptr+1) == 'a'
					&& *(ptr+2) == 'u'
					&& *(ptr+3) == 't'
					&& *(ptr+4) == 'h'
					&& *(ptr+5) == 'o'
					&& *(ptr+6) == 'r'
					&& *(ptr+7) == '{'
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = GUJI_AUTHOR;
					addControl(myKind, true);
					ptr += 7;
					start =ptr+1;
				} else if(*ptr == '}') {
					if(ptr != start) {
						//__android_log_print(ANDROID_LOG_INFO, "love", "!!!!!,,,!!!!!!%s",str.substr(start +str.length() -end, ptr-start).c_str());
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					start = ptr +1;
					addControl(myKind, false);
				} else {					
					
				}
				ptr += 1;
			} //end while
			if(ptr != start) {
				if((*start == '|' || *start == '\\') && (str.rfind("\n") != (str.length() - 1))) {
					myLastString.erase();
					myLastString = str.substr(start +str.length() -end, ptr-start);
					myIsNeedLineHandler = false;
				} else {
					//__android_log_print(ANDROID_LOG_INFO, "love", "!!!!!666!!!!!!%s",str.substr(start +str.length() -end, ptr-start).c_str());
					addData(str.substr(start +str.length() -end, ptr-start));
				}
			}
			//addData(str);
//			if (myInsideContentsParagraph) {
//				addContentsData(str);
//			}
			myNewLine = false;
		}
	}
	return true;
}

bool GujiBookReader::newLineHandler() {
	if(!myIsNeedLineHandler) {
		return true;
	}
	if (!myLastLineIsEmpty) {
		myLineFeedCounter = -1;
	}
	myLastLineIsEmpty = true;
	++myLineFeedCounter;
	myNewLine = true;
	mySpaceCounter = 0;
	bool paragraphBreak =
		(myFormat.breakType() & GujiTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE) ||
		((myFormat.breakType() & GujiTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE) && (myLineFeedCounter > 0));

	if (true) {
		internalEndParagraph();
		beginParagraph();
	}
	return true;
}

void GujiBookReader::startDocumentHandler() {
	setMainTextModel();
	myLineFeedCounter = 0;
	myInsideContentsParagraph = false;
	myLastLineIsEmpty = true;
	myNewLine = true;
	mySpaceCounter = 0;
}

void GujiBookReader::endDocumentHandler() {
	internalEndParagraph();
}
