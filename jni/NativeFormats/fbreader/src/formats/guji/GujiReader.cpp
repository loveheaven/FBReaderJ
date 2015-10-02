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
#include <ZLInputStream.h>

#include "GujiReader.h"
#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

class GujiReaderCore {

public:
	GujiReaderCore(GujiReader &reader);
	virtual ~GujiReaderCore(){};
	virtual void readDocument(ZLInputStream &stream);
	virtual void readMetaInfo(ZLInputStream &stream, Book& book);
	virtual std::string findTag(ZLInputStream &stream, Book& book, std::string tag);

protected:
	GujiReader &myReader;
};

class GujiReaderCoreUtf16 : public GujiReaderCore {

public:
	GujiReaderCoreUtf16(GujiReader &reader);
	virtual ~GujiReaderCoreUtf16(){};
	void readDocument(ZLInputStream &stream);
	virtual void readMetaInfo(ZLInputStream &stream, Book& book);
	virtual std::string findTag(ZLInputStream &stream, Book& book, std::string tag);

protected:
	virtual char getAscii(const char *ptr) = 0;
	virtual void setAscii(char *ptr, char ascii) = 0;
};

class GujiReaderCoreUtf16LE : public GujiReaderCoreUtf16 {

public:
	GujiReaderCoreUtf16LE(GujiReader &reader);

protected:
	char getAscii(const char *ptr);
	void setAscii(char *ptr, char ascii);
};

class GujiReaderCoreUtf16BE : public GujiReaderCoreUtf16 {

public:
	GujiReaderCoreUtf16BE(GujiReader &reader);

protected:
	char getAscii(const char *ptr);
	void setAscii(char *ptr, char ascii);
};

GujiReader::GujiReader(const std::string &encoding) : EncodedTextReader(encoding) {
	if (ZLEncodingConverter::UTF16 == encoding) {
		myCore = new GujiReaderCoreUtf16LE(*this);
	} else if (ZLEncodingConverter::UTF16BE == encoding) {
		myCore = new GujiReaderCoreUtf16BE(*this);
	} else {
		myCore = new GujiReaderCore(*this);
	}
}

GujiReader::~GujiReader() {
}

void GujiReader::readMetaInfo(ZLInputStream &stream, Book& book) {
	if (!stream.open()) {
		return;
	}

	myCore->readMetaInfo(stream, book);

	stream.close();
}

void GujiReader::readDocument(ZLInputStream &stream) {
	if (!stream.open()) {
		return;
	}
	startDocumentHandler();
	myCore->readDocument(stream);
	endDocumentHandler();
	stream.close();
}

GujiReaderCore::GujiReaderCore(GujiReader &reader) : myReader(reader) {
}

GujiReaderCoreUtf16::GujiReaderCoreUtf16(GujiReader &reader) : GujiReaderCore(reader) {
}

std::string GujiReaderCore::findTag(ZLInputStream &stream, Book& book, std::string tag) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	bool isExit =false;
	std::string ret;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr != end; ++ptr) {
			if (*ptr == '\n' || *ptr == '\r') {
				bool skipNewLine = false;
				if (*ptr == '\r' && (ptr + 1) != end && *(ptr + 1) == '\n') {
					skipNewLine = true;
					*ptr = '\n';
				}
				if (start != ptr) {
					str.erase();
					myReader.myConverter->convert(str, start, ptr + 1);
					if(str.find(tag) == 0) {
						ret = str.substr(tag.length(), str.length()-tag.length() -2);//book.setTitle();
						isExit = true;
						break;
					}
				}
				if (skipNewLine) {
					++ptr;
				}
				start = ptr + 1;
			} else if (isspace((unsigned char)*ptr)) {
				if (*ptr != '\t') {
					*ptr = ' ';
				}
			} else {
			}
		}
		if(isExit) {
			break;
		}
		if (start != end) {
			str.erase();
			myReader.myConverter->convert(str, start, end);
			if(str.find(tag) == 0) {
				ret = str.substr(tag.length(), str.length()-tag.length() -2);//book.setTitle(str.substr(7, str.length()-9));
				break;
			}
		}
	} while (length == BUFSIZE);
	delete[] buffer;
	return ret;
}
template <typename T>
std::vector<std::string> splitString(const std::string& str, T separator)
{
    std::vector<std::string> strvec;
    std::string::size_type pos1, pos2;
    pos2 = str.find(separator);
    pos1 = 0;

    while (std::string::npos != pos2)
    {
        strvec.push_back(str.substr(pos1, pos2 - pos1));
        pos1 = pos2 + 1;
        pos2 = str.find(separator, pos1);
    }
    strvec.push_back(str.substr(pos1));
    return strvec;
}

void GujiReaderCore::readMetaInfo(ZLInputStream &stream, Book& book) {
	std::string title = findTag(stream, book, "\\title{");
	book.setTitle(title);
	stream.seek(0,true);
	std::string author = findTag(stream, book, "\\author{");
	if(!author.empty()) {
		std::vector<std::string> authors = splitString(author, ";");
		for (std::vector<std::string>::iterator iter=authors.begin();iter!=authors.end();iter++) {
			book.addAuthor(*iter);
		}
	}
}

std::string GujiReaderCoreUtf16::findTag(ZLInputStream &stream, Book& book, std::string tag) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	bool isExit =false;
	std::string ret;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr < end; ptr += 2) {
			const char chr = getAscii(ptr);
			if (chr == '\n' || chr == '\r') {
				bool skipNewLine = false;
				if (chr == '\r' && ptr + 2 != end && getAscii(ptr + 2) == '\n') {
					skipNewLine = true;
					setAscii(ptr, '\n');
				}
				if (start != ptr) {
					str.erase();
					myReader.myConverter->convert(str, start, ptr + 2);
					if(str.find(tag) == 0) {
						ret = str.substr(tag.length(), str.length()-tag.length() -2);
						isExit = true;
						break;
					}
				}
				if (skipNewLine) {
					ptr += 2;
				}
				start = ptr + 2;
			} else if (chr != 0 && isspace(chr)) {
				if (chr != '\t') {
					setAscii(ptr, ' ');
				}
			}
		}
		if(isExit) {
			break;
		}
		if (start != end) {
			str.erase();
			myReader.myConverter->convert(str, start, end);
			if(str.find(tag) == 0) {
				ret = str.substr(tag.length(), str.length()-tag.length()-2);
				break;
			}
		}
	} while (length == BUFSIZE);
	delete[] buffer;
	return ret;

}
void GujiReaderCoreUtf16::readMetaInfo(ZLInputStream &stream, Book& book) {
	std::string title = findTag(stream, book, "\\title{");
	book.setTitle(title);
	stream.seek(0, true);
	std::string author = findTag(stream, book, "\\author{");
	if(!author.empty()) {
		book.addAuthor(author);
	}
}

void GujiReaderCore::readDocument(ZLInputStream &stream) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	size_t stored = 0;
	do {
		length = stream.read(buffer+stored, BUFSIZE-stored)+stored;
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr != end; ++ptr) {
			if (*ptr == '\n' || *ptr == '\r') {
				bool skipNewLine = false;
				if (*ptr == '\r' && (ptr + 1) != end && *(ptr + 1) == '\n') {
					skipNewLine = true;
					*ptr = '\n';
				} else if (*ptr == '\r'){
					skipNewLine = false;
					*ptr = '\n';
				}
				if (start != ptr) {
					str.erase();

					myReader.myConverter->convert(str, start, ptr + 1);
					//__android_log_print(ANDROID_LOG_INFO, "love", "!!!!!!!!!!!!!!%s",str.c_str());
					myReader.characterDataHandler(str);

				}
				if (skipNewLine) {
					++ptr;
				}
				start = ptr + 1;
				myReader.newLineHandler();
			} else if (isspace((unsigned char)*ptr)) {
				if (*ptr != '\t') {
					*ptr = ' ';
				}
			}
		}

		stored = 0;
		if (start != end) {
			str.erase();

			unsigned char t = *(end-1-stored);
			while((end -1 -stored) >= start && ((t&0xF0) < 0xC0)) {
				stored++;
				t = *(end-1-stored);
			}
			//__android_log_print(ANDROID_LOG_INFO, "love", "!!!!!!!!!!!!!!%d",t);
			if((end-1-stored) < start) {
				stored = 0;
			} else if(((t&0xF0) >= 0xC0) && ((t&0xF0) <= 0xF0)) {
				stored+=1;
			}
			if(length < BUFSIZE) {
				stored = 0;
			}
			myReader.myConverter->convert(str, start, end-stored);
			//__android_log_print(ANDROID_LOG_INFO, "love", "!!!!!---!!!!!!%s",str.c_str());
			myReader.characterDataHandler(str);
			if(stored > 0) {
				memcpy(buffer, end - stored, stored);
			}
		}
	} while (length == BUFSIZE);
	delete[] buffer;
}

void GujiReaderCoreUtf16::readDocument(ZLInputStream &stream) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr < end; ptr += 2) {
			const char chr = getAscii(ptr);
			if (chr == '\n' || chr == '\r') {
				bool skipNewLine = false;
				if (chr == '\r' && ptr + 2 != end && getAscii(ptr + 2) == '\n') {
					skipNewLine = true;
					setAscii(ptr, '\n');
				}
				if (start != ptr) {
					str.erase();
					myReader.myConverter->convert(str, start, ptr + 2);
					myReader.characterDataHandler(str);
				}
				if (skipNewLine) {
					ptr += 2;
				}
				start = ptr + 2;
				myReader.newLineHandler();
			} else if (chr != 0 && isspace(chr)) {
				if (chr != '\t') {
					setAscii(ptr, ' ');
				}
			}
		}
		if (start != end) {
			str.erase();
			myReader.myConverter->convert(str, start, end);
			myReader.characterDataHandler(str);
		}
	} while (length == BUFSIZE);
	delete[] buffer;
}

GujiReaderCoreUtf16LE::GujiReaderCoreUtf16LE(GujiReader &reader) : GujiReaderCoreUtf16(reader) {
}

char GujiReaderCoreUtf16LE::getAscii(const char *ptr) {
	return *(ptr + 1) == '\0' ? *ptr : '\0';
}

void GujiReaderCoreUtf16LE::setAscii(char *ptr, char ascii) {
	*ptr = ascii;
}

GujiReaderCoreUtf16BE::GujiReaderCoreUtf16BE(GujiReader &reader) : GujiReaderCoreUtf16(reader) {
}

char GujiReaderCoreUtf16BE::getAscii(const char *ptr) {
	return *ptr == '\0' ? *(ptr + 1) : '\0';
}

void GujiReaderCoreUtf16BE::setAscii(char *ptr, char ascii) {
	*(ptr + 1) = ascii;
}
