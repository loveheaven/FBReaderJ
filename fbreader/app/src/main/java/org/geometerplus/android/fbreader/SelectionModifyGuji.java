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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

public class SelectionModifyGuji extends FBAndroidAction {
	FBReader myApplication;
	SelectionModifyGuji(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
		myApplication = baseApplication;
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		final int indexOfParagraph = fbview.getSelectionStartPosition().getParagraphIndex();
		ZLTextParagraph p = Reader.Model.getTextModel().getParagraph(indexOfParagraph);
		ZLTextParagraph.EntryIterator it = p.iterator();
		String text = "";//fbview.getSelectedText();
		boolean isChapterEnd = false;
		boolean isTitleEnd = false;
		while (it.next()) {
			//it.next();
			if (it.getType() == ZLTextParagraph.Entry.TEXT) {
				char[] textData = it.getTextData();
				int textOffset = it.getTextOffset();
				int textLength = it.getTextLength();
				char[] textDataDst = new char[textLength];
				
				System.arraycopy(textData, textOffset, textDataDst, 0, textLength);
				
				text += new String(textDataDst);
			} else if (it.getType() == ZLTextParagraph.Entry.CONTROL) {
				if(isTitleEnd) {
					isTitleEnd = false;
					text +="}\n";
				}
				if(isChapterEnd) {
					isChapterEnd = false;
					text +="}\n";
				}
				if(it.getControlIsStart()) {
					switch(it.getControlKind()) {
					case FBTextKind.CODE:
						text+="|{";
						break;
					case FBTextKind.SUB:
						text+="\\sub{";
						break;
					case FBTextKind.SUP:
						text+="\\sup{";
						break;
					case FBTextKind.H3:
						text+="\\h3{";
						break;
					case FBTextKind.H2:
						text+="\\h2{";
						break;
					case FBTextKind.H4:
						text+="\\h4{";
						break;
					case FBTextKind.SECTION_TITLE:
						text+="\\chapter{";
						isChapterEnd = true;
						break;
					case FBTextKind.TITLE:
						text+="\\title{";
						isTitleEnd = true;
						break;
					}
				} else {
					switch(it.getControlKind()) {
					case FBTextKind.CODE:
						text+="}";
						break;
					case FBTextKind.SUB:
						text+="}";
						break;
					case FBTextKind.SUP:
						text+="}";
						break;
					case FBTextKind.H3:
						text+="}";
						break;
					case FBTextKind.H2:
						text+="}";
						break;
					case FBTextKind.H4:
						text+="}";
						break;
					}
					
				}
			}
		}
		
		final EditText edit = new EditText(myApplication);
		edit.setText(text);
		new AlertDialog.Builder(myApplication).setView(edit)
		.setPositiveButton(ZLResource.resource("menu").getResource("saveguji").getValue(), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newText = edit.getText().toString();
				
				Reader.Model.getTextModel().saveGuji(Reader.Model.Book, 
						indexOfParagraph, 
						newText);
				
				fbview.clearSelection();
				Reader.reloadBook();
				UIMessageUtil.showMessageText(
						BaseActivity,
						ZLResource.resource("selection").getResource("gujiModified").getValue().replace("%s", newText)
					);
			}
		}).show();

		fbview.clearSelection();

		
	}
}
