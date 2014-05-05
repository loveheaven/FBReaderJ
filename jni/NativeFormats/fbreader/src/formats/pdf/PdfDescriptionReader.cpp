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

#include <ZLInputStream.h>
#include <ZLFile.h>
#include <ZLLanguageDetector.h>

extern "C"
{
#include "fitz.h"
#include "mupdf.h"

}
#include "PdfBridge.h"
#include "PdfDescriptionReader.h"
#include "../../library/Book.h"




PdfDescriptionReader::PdfDescriptionReader(Book &book) : myBook(book) {
	myBook.removeAllAuthors();
	myBook.setTitle(std::string());
	myBook.setLanguage(std::string());
	myBook.removeAllTags();
}


bool PdfDescriptionReader::readMetaInfo(const ZLFile& file) {
	renderdocument_t *doc = PdfDocument_open((char*)(file.path().c_str()), NULL);
	PdfDocument_getInfo(doc,myBook);
	detectEncodingAndLanguage(doc);
	PdfDocument_free(doc);
	return true;
}

void PdfDescriptionReader::detectEncodingAndLanguage(renderdocument_t *doc) {
	std::string language = myBook.language();
	std::string encoding = myBook.encoding();

	if (!encoding.empty()) {
		return;
	}

	if (encoding.empty()) {
		encoding = "utf-8";
	}
		std::string str = "";
		int count = pdf_getpagecount(doc->xref);
		if(count > 0) {
			int start = count > 5?5:0;
			int end = count > 10?10:count;

			for(int i = start; i< end; i++) {
				detectPage(doc->xref, pdf_getpageobject(doc->xref, i), str);
			}
		}

		shared_ptr<ZLLanguageDetector::LanguageInfo> info = ZLLanguageDetector().findInfo(str.c_str(), strlen(str.c_str()));
		if (!info.isNull()) {
			if (!info->Language.empty()) {
				language = info->Language;
			}
			encoding = info->Encoding;
			if ((encoding == "us-ascii") || (encoding == "iso-8859-1")) {
				encoding = "windows-1252";
			}
		}
	myBook.setEncoding(encoding);
	myBook.setLanguage(language);
}

void PdfDescriptionReader::pdf_gettextline(pdf_textline *line, std::string& str)
{
	char buf[10];
	int c, n, k, i;
	for (i = 0; i < line->len; i++)
	{
		c = line->text[i].c;
		if (c < 128)
		{
			//putchar(c);
			str.append(1,(char)c);
		}
		else
		{
			n = runetochar(buf, &c);
			str.append(buf, n);
//			for (k = 0; k < n; k++)
//				putchar(buf[k]);
		}
	}
	str.append(1,'\n');
	if (line->next)
		pdf_gettextline(line->next, str);
}

void PdfDescriptionReader::detectPage(pdf_xref *xref, fz_obj *pageobj, std::string& str)
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

		pdf_gettextline(line, str);
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
