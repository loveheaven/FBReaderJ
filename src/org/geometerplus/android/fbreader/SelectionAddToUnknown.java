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

package org.geometerplus.android.fbreader;

import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Word;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class SelectionAddToUnknown extends FBAndroidAction {
	SelectionAddToUnknown(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		final String text = fbview.getSelectedSnippet().getText().toLowerCase().replaceAll("[,.?;\"]", "");

		BaseActivity.getCollection().bindToService(BaseActivity, new Runnable() {
			
			@Override
			public void run() {
				BaseActivity.getCollection().saveToUnknownWords(new Word(Reader.Model.Book, fbview.getSelectionStartPosition(), text, 0));
			}
		});
		
		fbview.clearSelection();

		UIMessageUtil.showMessageText(
			BaseActivity,
			ZLResource.resource("selection").getResource("wordAdded").getValue().replace("%s", text)
		);
	}
}
