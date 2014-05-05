/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#ifndef __PdfBOOKREADER_H__
#define __PdfBOOKREADER_H__

#include <map>

#include "../../bookmodel/BookReader.h"

class PdfBookReader {

public:
	PdfBookReader(BookModel &model);
	~PdfBookReader();
	bool readBook();

private:
	void showpage(pdf_xref *xref, fz_obj *pageobj, int pagenum);
	void pdf_gettextline(pdf_textline *line);
private:
	BookReader myModelReader;
	int m_startX;
	int m_lastLineX;
	int m_lastLineY;
	int m_indent;
	int m_lastLen;
	int m_averageHeight;
	int m_totalLen;
};

#endif /* __PdfBOOKREADER_H__ */
