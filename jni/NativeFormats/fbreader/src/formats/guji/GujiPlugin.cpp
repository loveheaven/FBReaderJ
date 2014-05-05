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

#include <ZLFile.h>
#include <ZLInputStream.h>

#include "GujiPlugin.h"
#include "GujiBookReader.h"
#include "GujiTextFormat.h"

#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

GujiPlugin::~GujiPlugin() {
}

bool GujiPlugin::providesMetainfo() const {
	return true;
}

const std::string GujiPlugin::supportedFileType() const {
	return "guji";
}

bool GujiPlugin::readMetainfo(Book &book) const {
	const ZLFile &file = book.file();
	shared_ptr<ZLInputStream> stream = file.inputStream();
	if (stream.isNull()) {
		return false;
	}

	GujiTextFormat format(file);
	if (!format.initialized()) {
		GujiTextFormatDetector detector;
		detector.detect(*stream, format);
	}

	readLanguageAndEncoding(book);
	BookModel *model=NULL;
	GujiBookReader(*model, format, book.encoding()).readMetaInfo(*stream, book);
	return true;
}

bool GujiPlugin::readUids(Book &/*book*/) const {
	return true;
}

bool GujiPlugin::readModel(BookModel &model) const {
	Book &book = *model.book();
	const ZLFile &file = book.file();
	shared_ptr<ZLInputStream> stream = file.inputStream();
	if (stream.isNull()) {
		return false;
	}

	GujiTextFormat format(file);
	if (!format.initialized()) {
		GujiTextFormatDetector detector;
		detector.detect(*stream, format);
	}

	readLanguageAndEncoding(book);
	GujiBookReader(model, format, book.encoding()).readDocument(*stream);
	return true;
}

//FormatInfoPage *GujiPlugin::createInfoPage(ZLOptionsDialog &dialog, const ZLFile &file) {
//	return new PlainTextInfoPage(dialog, file, ZLResourceKey("Text"), true);
//}

bool GujiPlugin::readLanguageAndEncoding(Book &book) const {
	shared_ptr<ZLInputStream> stream = book.file().inputStream();
	if (stream.isNull()) {
		return false;
	}
	detectEncodingAndLanguage(book, *stream);
	return !book.encoding().empty();
}
