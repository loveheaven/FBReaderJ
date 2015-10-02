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
//				if(isTitleEnd) {
//					isTitleEnd = false;
//					if(!text.endsWith("}\n")) {
//						text+="}\n";
//					}
//				}
//				if(isChapterEnd) {
//					isChapterEnd = false;
//					if(!text.endsWith("}\n")) {
//						text+="}\n";
//					}
//				}
				if(it.getControlIsStart()) {
					switch(it.getControlKind()) {
					case FBTextKind.GUJI_TRANSLATION:
						text+="|{";
						break;
					case FBTextKind.GUJI_ANNOTATION:
						text+="\\anno{";
						break;
					case FBTextKind.GUJI_COMMENT:
						text+="\\com{";
						break;
					case FBTextKind.GUJI_SUBSCRIPT:
						text+="\\sub{";
						break;
					case FBTextKind.GUJI_SUBTITLE:
						text+="\\subt{";
						break;
					case FBTextKind.GUJI_CR:
						text+="\\cr{";
						break;
					case FBTextKind.GUJI_PARAGRAPHSTART:
						text+="\\ps{";
						break;
					case FBTextKind.GUJI_AUTHOR:
						text+="\\author{";
						break;
					case FBTextKind.GUJI_TITLEANNOTATION:
						text+="\\tanno{";
						break;
					case FBTextKind.GUJI_SECTIONTITLE1:
						text+="\\sec1{";
						break;
					case FBTextKind.GUJI_SECTIONTITLE2:
						text+="\\sec2{";
						break;
					case FBTextKind.GUJI_SECTIONTITLE3:
						text+="\\sec3{";
						break;
					case FBTextKind.GUJI_SECTIONTITLE4:
						text+="\\sec4{";
						break;
					case FBTextKind.GUJI_SUPERSCRIPT:
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
					case FBTextKind.H1:
						text+="\\h1{";
						break;
					case FBTextKind.GUJI_SECTIONTITLE:
						text+="\\section{";
						isChapterEnd = true;
						break;
					case FBTextKind.TITLE:
						text+="\\title{";
						isTitleEnd = true;
						break;
					}
				} else {
					switch(it.getControlKind()) {
					case FBTextKind.GUJI_TRANSLATION:
						text+="}";
						break;
					case FBTextKind.GUJI_ANNOTATION:
						text+="}";
						break;
					case FBTextKind.GUJI_COMMENT:
						text+="}";
						break;
					case FBTextKind.GUJI_SUBSCRIPT:
						text+="}";
						break;
					case FBTextKind.GUJI_SUBTITLE:
						text+="}";
						break;
					case FBTextKind.GUJI_CR:
						text+="}";
						break;
					case FBTextKind.GUJI_PARAGRAPHSTART:
						text+="}";
						break;
					case FBTextKind.GUJI_AUTHOR:
						text+="}";
						break;
					case FBTextKind.GUJI_TITLEANNOTATION:
						text+="}";
						break;
					case FBTextKind.GUJI_SECTIONTITLE:
						text+="}\n";
						break;
					case FBTextKind.GUJI_SECTIONTITLE1:
						text+="}";
						break;
					case FBTextKind.GUJI_SECTIONTITLE2:
						text+="}";
						break;
					case FBTextKind.GUJI_SECTIONTITLE3:
						text+="}";
						break;
					case FBTextKind.GUJI_SECTIONTITLE4:
						text+="}";
						break;
					case FBTextKind.GUJI_SUPERSCRIPT:
						text+="}";
						break;
					case FBTextKind.H3:
						text+="}";
						break;
					case FBTextKind.H2:
						text+="}";
						break;
					case FBTextKind.H1:
						text+="}";
						break;
					case FBTextKind.H4:
						text+="}";
						break;
					case FBTextKind.TITLE:
						text+="}\n";
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
