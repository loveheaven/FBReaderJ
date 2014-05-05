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

#ifndef __GUJIBOOKREADER_H__
#define __GUJIBOOKREADER_H__

#include <stack>

#include "GujiReader.h"
#include "GujiTextFormat.h"
#include "../../bookmodel/BookReader.h"

class BookModel;

class GujiBookReader : public GujiReader, public BookReader {

public:
	GujiBookReader(BookModel &model, const GujiTextFormat &format, const std::string &encoding);
	~GujiBookReader();

protected:
	void startDocumentHandler();
	void endDocumentHandler();

	bool characterDataHandler(std::string &str);
	bool sectionHandler(std::string &str);
	bool titleHandler(std::string &str);
	bool newLineHandler();

private:
	void internalEndParagraph();

private:
	const GujiTextFormat &myFormat;
	BookModel &myModel;

	std::string myLastString;
	int myLineFeedCounter;
	bool myInsideContentsParagraph;
	bool myLastLineIsEmpty;
	bool myNewLine;
	int mySpaceCounter;
	bool myIsNeedLineHandler;
	bool myIsInSectionTitle;
	bool myIsAfterBookTitle;
	FBTextKind myKind;
};

inline GujiBookReader::~GujiBookReader() {}

#endif /* __GUJIBOOKREADER_H__ */
