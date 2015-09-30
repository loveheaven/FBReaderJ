/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.djvu;

import org.geometerplus.fbreader.book.AbstractBook;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.BuiltinFormatPlugin;
import org.geometerplus.zlibrary.core.encodings.AutoEncodingCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.SystemInfo;
import org.geometerplus.zlibrary.ui.android.image.ZLBitmapImage;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class DjvuPlugin extends BuiltinFormatPlugin {
	public DjvuPlugin(SystemInfo systemInfo) {
		super(systemInfo, "djvu");
	}

	@Override
	public ZLFile realBookFile(ZLFile file) throws BookReadingException {
		
		return file;
	}

	@Override
	public void readMetainfo(AbstractBook book) throws BookReadingException {
		//new FB2MetaInfoReader(book).readMetaInfo();
	}

	@Override
	public void readModel(BookModel model) throws BookReadingException {
		//new FB2Reader(model).readBook();
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		DjvuDocument document = new DjvuContext().openDocument(file.getPath());
		// TODO：270, 360 is the default bookcover size in xxhdpi. This should be get from dimens.xml.
		Bitmap bitmap = document.getPage(0).renderBitmap(270, 360, new RectF(0, 0, 1, 1));
		ZLImage result = new ZLBitmapImage(bitmap);
		document.recycle();
		return result;
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return null;
	}

	@Override
	public AutoEncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(AbstractBook book) {
		book.setEncoding("auto");
	}

	@Override
	public void readUids(AbstractBook book) throws BookReadingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int priority() {
		// TODO Auto-generated method stub
		return 0;
	}
}
