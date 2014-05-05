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

#ifndef __PDFBRIDGE_H__
#define __PDFBRIDGE_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>

#include <android/log.h>

#include <errno.h>


#define DEBUG(args...) \
	__android_log_print(ANDROID_LOG_DEBUG, "PdfDroid", args)

//#define DEBUG(args...) {}
#define ERROR(args...) \
	__android_log_print(ANDROID_LOG_ERROR, "PdfDroid", args)

#define INFO(args...) \
	__android_log_print(ANDROID_LOG_INFO, "PdfDroid", args)

class Book;

typedef struct renderdocument_s renderdocument_t;
struct renderdocument_s
{
	pdf_xref *xref;
	fz_renderer *rast;
};

typedef struct renderpage_s renderpage_t;
struct renderpage_s
{
	pdf_page *page;
};

renderdocument_t * PdfDocument_open(char * filename, char * password);
void PdfDocument_free(renderdocument_t *doc);
void PdfDocument_getInfo(renderdocument_t *doc, Book& pdfBook);
renderpage_t * PdfPage_open(renderdocument_t* doc, int pageno);
void PdfPage_free(renderpage_t* page);




#ifdef __cplusplus
}
#endif

#endif /* __PDFBRIDGE_H__ */
