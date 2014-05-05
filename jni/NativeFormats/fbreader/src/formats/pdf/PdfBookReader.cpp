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

#include <cstdlib>
#include <iostream>

#include <ZLFile.h>
#include <ZLStringUtil.h>
#include <ZLInputStream.h>


#include "../../library/Book.h"
#include "../../bookmodel/BookModel.h"

extern "C"
{
#include "fitz.h"
#include "mupdf.h"

}
#include "PdfBridge.h"
#include "PdfBookReader.h"

PdfBookReader::PdfBookReader(BookModel &model) : myModelReader(model) {
	m_indent = 0;
	m_startX = -1;
	m_lastLineX = -1;
	m_lastLen = -1;
	m_averageHeight = 0;
	m_totalLen = 0;
}

PdfBookReader::~PdfBookReader() {
}

bool PdfBookReader::readBook() {
	const shared_ptr<Book> myBook = myModelReader.model().book();
	renderdocument_t *doc = PdfDocument_open((char*)(myBook->file().path().c_str()), NULL);
	myModelReader.setMainTextModel();
	int count = pdf_getpagecount(doc->xref);
	myModelReader.beginParagraph();
	for(int i = 0; i< count; i++) {
		m_lastLineY = -1;
		showpage(doc->xref, pdf_getpageobject(doc->xref, i), i+1);
	}
	myModelReader.endParagraph();
	PdfDocument_free(doc);
	return true;
}
void PdfBookReader::pdf_gettextline(pdf_textline *line)
{
	char buf[10];
	int c, n, k, i;

	std::string str = "";
	if(m_lastLen == -1) {
		m_lastLen = line->len;
	}

	if(line->len > 0) {
		if(m_lastLineX == -1) {
			m_lastLineX = line->text[0].x;
			m_lastLineY = line->text[0].y;
		}

	}

	if(line->len == 0) {
		myModelReader.endParagraph();
		myModelReader.beginParagraph();
		str.append(2,' ');
	} else {
		if(m_startX == -1) {
			m_startX = line->text[0].x;
		}
		if(m_startX > line->text[0].x) {
			m_startX = line->text[0].x;
		}
		if( (line->text[0].x -m_lastLineX) > 2) {
			myModelReader.endParagraph();
			myModelReader.beginParagraph();
		} else {
			if(m_lastLineX == line->text[0].x) {
				if((line->text[0].y - m_lastLineY)> 1.7*m_averageHeight) {
					myModelReader.endParagraph();
					myModelReader.beginParagraph();
				}
			} else {
				if(
						((line->len*2/3)>m_lastLen)
				   ) {
					myModelReader.endParagraph();
					myModelReader.beginParagraph();
				}
			}
		}

		if(m_averageHeight == 0) {
			m_averageHeight = line->text[0].y - m_lastLineY;
		}
		m_averageHeight = ((m_totalLen * m_averageHeight) + ((line->text[0].y - m_lastLineY) *line->len))/(m_totalLen+line->len);
		DEBUG("pdf%d,%d,%d",line->text[0].x, line->text[0].y,m_averageHeight);
		m_totalLen +=line->len;
		m_lastLineX = line->text[0].x;
		m_lastLineY = line->text[0].y;
	}
	m_lastLen = line->len;


	for (i = 0; i < line->len; i++)
	{
		c = line->text[i].c;
		if (c < 128)
		{
			str.append(1,(char)c);
		}
		else
		{
			n = runetochar(buf, &c);
			str.append(buf, n);
		}
	}
	str.append(1,'\n');

	myModelReader.addData(str);
	myModelReader.addContentsData(str);

	if (line->next)
		pdf_gettextline(line->next);
}

void PdfBookReader::showpage(pdf_xref *xref, fz_obj *pageobj, int pagenum)
{
	fz_error error;
	pdf_page *page;
	char namebuf[256];
	char buf[128];
	fz_pixmap *pix;
	fz_matrix ctm;
	fz_irect bbox;
	int fd;
	int x, y;
	int w, h;
	int b, bh;
	float zoom = 1.0;

	error = pdf_loadpage(&page, xref, pageobj);
	if (error)
		return;

//	if (showtree)
//	{
//		fz_debugobj(pageobj);
//		printf("\n");
//
//		printf("page\n");
//		printf("  mediabox [ %g %g %g %g ]\n",
//			page->mediabox.min.x, page->mediabox.min.y,
//			page->mediabox.max.x, page->mediabox.max.y);
//		printf("  rotate %d\n", page->rotate);
//
//		printf("  resources\n");
//		fz_debugobj(page->resources);
//		printf("\n");
//
//		printf("tree\n");
//		fz_debugtree(page->tree);
//		printf("endtree\n");
//	}

	ctm = fz_concat(fz_translate(0, -page->mediabox.y1),
					fz_scale(zoom, -zoom));

	if (1)
	{
		pdf_textline *line;

		error = pdf_loadtextfromtree(&line, page->tree, ctm);
		if (error)
			return;


		pdf_gettextline(line);
		pdf_droptextline(line);
		pdf_droppage(page);

		return;
	}

//	bbox = fz_roundrect(page->mediabox);
//	bbox.min.x = bbox.min.x * zoom;
//	bbox.min.y = bbox.min.y * zoom;
//	bbox.max.x = bbox.max.x * zoom;
//	bbox.max.y = bbox.max.y * zoom;
//	w = bbox.max.x - bbox.min.x;
//	h = bbox.max.y - bbox.min.y;
//	bh = h / nbands;
//
//	fd = open(namebuf, O_BINARY|O_WRONLY|O_CREAT|O_TRUNC, 0666);
//	if (fd < 0)
//		fz_abort(fz_throw("open %s failed: %s", namebuf, strerror(errno)));
//	sprintf(buf, "P6\n%d %d\n255\n", w, bh * nbands);
//	write(fd, buf, strlen(buf));
//
//	error = fz_newpixmap(&pix, bbox.min.x, bbox.min.y, w, bh, 4);
//	if (error)
//		fz_abort(error);
//
//	for (b = 0; b < nbands; b++)
//	{
//		if (verbose)
//			printf("render band %d of %d\n", b + 1, nbands);
//
//		memset(pix->samples, 0xff, pix->w * pix->h * 4);
//
//		error = fz_rendertreeover(gc, pix, page->tree, ctm);
//		if (error)
//			fz_abort(error);
//
//		for (y = 0; y < pix->h; y++)
//		{
//			unsigned char *src = pix->samples + y * pix->w * 4;
//			unsigned char *dst = src;
//
//			for (x = 0; x < pix->w; x++)
//			{
//				dst[x * 3 + 0] = src[x * 4 + 1];
//				dst[x * 3 + 1] = src[x * 4 + 2];
//				dst[x * 3 + 2] = src[x * 4 + 3];
//			}
//
//			write(fd, dst, pix->w * 3);
//		}
//
//		pix->y += bh;
//	}
//
//	fz_droppixmap(pix);
//
//	close(fd);
//
//	pdf_droppage(page);
}
