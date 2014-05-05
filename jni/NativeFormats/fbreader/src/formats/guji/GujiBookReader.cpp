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
	myIsNeedLineHandler = false;
	if(!myIsAfterBookTitle) {
		myIsAfterBookTitle = true;
	}

	beginContentsParagraph();
	enterTitle();
	pushKind(TITLE);
	beginParagraph();

	addData(str.substr(7, str.length()-9));
	addContentsData(str.substr(7, str.length()-9));
	exitTitle();
	this->internalEndParagraph();
	popKind();
	endContentsParagraph();
}

bool GujiBookReader::sectionHandler(std::string &str) {
	if(str.find("}") != std::string::npos) {
		if(str.find("\\section{") == 0 ) {
			if(myIsAfterBookTitle) {
				myIsAfterBookTitle = false;
			} else {
				internalEndParagraph();
				insertEndOfSectionParagraph();
			}
			beginContentsParagraph();
			enterTitle();
			pushKind(SECTION_TITLE);
			beginParagraph();
			addData(str.substr(9, str.find("}")-9));
			addContentsData(str.substr(9, str.find("}")-9));
			exitTitle();
			this->internalEndParagraph();

			popKind();
			this->beginParagraph();
			myIsNeedLineHandler = false;
			myIsInSectionTitle = false;
		} else {
			addData(str.substr(0, str.find("}")));
			addContentsData(str.substr(0, str.find("}")));
			exitTitle();
			this->internalEndParagraph();

			popKind();
			this->beginParagraph();
			myIsNeedLineHandler = false;
			myIsInSectionTitle = false;
		}
	} else {
		if(str.find("\\section{") == 0 ) {
			if(myIsAfterBookTitle) {
				myIsAfterBookTitle = false;
			} else {
				internalEndParagraph();
				insertEndOfSectionParagraph();
			}
			myIsNeedLineHandler = false;
			myIsInSectionTitle = true;
			beginContentsParagraph();
			enterTitle();
			pushKind(SECTION_TITLE);
			beginParagraph();
			addData(str.substr(9, str.length()-9));
			addContentsData(str.substr(9, str.length()-9));
		} else {
			addData(str);
			addContentsData(str);
			myIsNeedLineHandler = false;
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
		endContentsParagraph();
		pushKind(CONTENTS_TABLE_ENTRY);
		beginParagraph();

			this->internalEndParagraph();
			popKind();
		myIsNeedLineHandler = false;
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
				beginParagraph();
				myIsAfterBookTitle = false;
			}
			if ((myFormat.breakType() & GujiTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT) &&
					myNewLine && (mySpaceCounter > myFormat.ignoredIndent())) {
				internalEndParagraph();
				beginParagraph();
			}

			ptr = end - str.length();
			const char * start = end - str.length();

			while(ptr != end) {
				if(*ptr == '|' && (ptr+1) != end && *(ptr+1) == '{') {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = CODE;
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
				} else if(*ptr == '\\' && (ptr+4) != end 
					&& *(ptr+1) == 's'
					&& *(ptr+2) == 'u'
					&& *(ptr+3) == 'b'				
					&& *(ptr+4) == '{' 
					) {
					if(ptr != start) {
						addData(str.substr(start +str.length() -end, ptr-start));
					}
					myKind = SUB;
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
					myKind = SUP;
					addControl(myKind, true);
					ptr += 4;
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
			}
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
