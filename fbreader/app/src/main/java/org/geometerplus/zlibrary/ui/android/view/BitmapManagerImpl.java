/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.ui.android.view;

import android.graphics.*;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewEnums.PageIndex;
import org.geometerplus.zlibrary.ui.android.view.animation.BitmapManager;

final class BitmapManagerImpl implements BitmapManager {
	private final int SIZE = 2;
	private final Bitmap[] myBitmaps = new Bitmap[SIZE];
	private final ZLView.PageIndex[] myIndexes = new ZLView.PageIndex[SIZE];

	private int myWidth;
	private int myHeight;

	private final ZLAndroidWidget myWidget;

	BitmapManagerImpl(ZLAndroidWidget widget) {
		myWidget = widget;
	}

	void setSize(int w, int h) {
		if (myWidth != w || myHeight != h) {
			myWidth = w;
			myHeight = h;
			for (int i = 0; i < SIZE; ++i) {
				myBitmaps[i] = null;
				myIndexes[i] = null;
			}
			System.gc();
			System.gc();
			System.gc();
		}
	}

	public Bitmap getBitmap(ZLView.PageIndex index) {
		for (int i = 0; i < SIZE; ++i) {
			if (index == myIndexes[i] && myBitmaps[i] != null && !myBitmaps[i].isRecycled()) {
				return myBitmaps[i];
			}
		}
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		
		boolean isDjvu = fbReader.Model == null?false:fbReader.BookTextView.isDjvu();
		final int iIndex = getInternalIndex(index);
		myIndexes[iIndex] = index;
		if(myBitmaps[iIndex] != null && (isDjvu)) {
			myBitmaps[iIndex].recycle();
			myBitmaps[iIndex] = null;
		}
		if(myBitmaps[iIndex] != null && !myBitmaps[iIndex].isMutable()) {
			myBitmaps[iIndex].recycle();
			myBitmaps[iIndex] = null;
		}
		if (myBitmaps[iIndex] == null) {
			try {
				if(isDjvu) {
					if(fbReader.DJVUDocument == null) return null;
					int pageIndex = fbReader.DJVUDocument.currentPageIndex;
					if(index == ZLView.PageIndex.next) pageIndex++;
					else if(index == ZLView.PageIndex.previous) pageIndex--;
					if(pageIndex < 0) pageIndex = 0;
					else if(pageIndex > fbReader.DJVUDocument.getPageCount()-1) pageIndex = fbReader.DJVUDocument.getPageCount() -1;
					
					Bitmap bitmap = fbReader.DJVUDocument.getPage(pageIndex).renderBitmap(myWidth, myHeight, new RectF(0, 0, 1, 1));
					if(bitmap == null) {
						bitmap = fbReader.DJVUDocument.getPage(pageIndex).renderBitmap(myWidth, myHeight, new RectF(0, 0, 1, 1));
					}
					myBitmaps[iIndex] = bitmap;
				} else {
					myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.ARGB_8888);
				}
			} catch (OutOfMemoryError e) {
				System.gc();
				System.gc();
				try {
					myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
				} catch (OutOfMemoryError ex) {
					System.gc();
					System.gc();
				}
			}
		}
		if(!isDjvu) {
			myWidget.drawOnBitmap(myBitmaps[iIndex], index);
		}
		return myBitmaps[iIndex];
	}

	public void drawBitmap(Canvas canvas, int x, int y, ZLView.PageIndex index, Paint paint) {
		Bitmap bitmap = getBitmap(index);
		if(bitmap != null) {
			canvas.drawBitmap(bitmap, x, y, paint);
		}
	}

	private int getInternalIndex(ZLView.PageIndex index) {
		for (int i = 0; i < SIZE; ++i) {
			if (myIndexes[i] == null) {
				return i;
			}
		}
		for (int i = 0; i < SIZE; ++i) {
			if (myIndexes[i] != ZLView.PageIndex.current) {
				return i;
			}
		}
		return 0;
		//throw new RuntimeException("That's impossible");
	}

	void reset() {
		for (int i = 0; i < SIZE; ++i) {
			myIndexes[i] = null;
		}
	}

	void shift(boolean forward) {
		for (int i = 0; i < SIZE; ++i) {
			if (myIndexes[i] == null) {
				continue;
			}
			myIndexes[i] = forward ? myIndexes[i].getPrevious() : myIndexes[i].getNext();
		}
	}
}
