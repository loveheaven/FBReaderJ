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

package org.geometerplus.zlibrary.ui.android.view.animation;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewEnums;
import org.geometerplus.zlibrary.text.view.ZLTextView;

abstract class SimpleAnimationProvider extends AnimationProvider {
	private float mySpeedFactor;

	SimpleAnimationProvider(BitmapManager bitmapManager) {
		super(bitmapManager);
	}

	@Override
	public ZLViewEnums.PageIndex getPageToScrollTo(int x, int y) {
		if (myDirection == null) {
			return ZLViewEnums.PageIndex.current;
		}

		ZLViewEnums.PageIndex index = ZLViewEnums.PageIndex.current;
		switch (myDirection) {
			case rightToLeft:
				index = myStartX < x ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
				break;
			case leftToRight:
				index = myStartX < x ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
				break;
			case up:
				index = myStartY < y ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
				break;
			case down:
				index = myStartY < y ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
				break;
		}
		final ZLView view = ZLApplication.Instance().getCurrentView();
		boolean isGuji = view.isGuji();
		if(isGuji) {
			if(index == ZLViewEnums.PageIndex.next) {
				return ZLViewEnums.PageIndex.previous;
			}
			if(index == ZLViewEnums.PageIndex.previous) return ZLViewEnums.PageIndex.next;
		}
		return index;
	}

	@Override
	protected void setupAnimatedScrollingStart(Integer x, Integer y) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		boolean isGuji = view.isGuji();
		if(isGuji) {
			mySpeed = -1 * mySpeed;
		}
		if (x == null || y == null) {
			if (myDirection.IsHorizontal) {
				x = mySpeed < 0 ? myWidth : 0;
				y = 0;
			} else {
				x = 0;
				y = mySpeed < 0 ? myHeight : 0;
			}
		}
		myEndX = myStartX = x;
		myEndY = myStartY = y;
	}

	@Override
	protected void startAnimatedScrollingInternal(int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		boolean isGuji = view.isGuji();
		if(isGuji) {
			mySpeed = -1 * mySpeed;
		}
		mySpeedFactor = (float)Math.pow(1.5, 0.25 * speed*0.6);
		doStep();
	}

	@Override
	public final void doStep() {
		if (!getMode().Auto) {
			return;
		}
		
		switch (myDirection) {
			case leftToRight:
				myEndX -= (int)mySpeed;
				break;
			case rightToLeft:
				myEndX += (int)mySpeed;
				break;
			case up:
				myEndY += (int)mySpeed;
				break;
			case down:
				myEndY -= (int)mySpeed;
				break;
		}
		final int bound;
		if (getMode() == Mode.AnimatedScrollingForward) {
			bound = myDirection.IsHorizontal ? myWidth : myHeight;
		} else {
			bound = 0;
		}
		if (mySpeed > 0) {
			if (getScrollingShift() >= bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX + bound;
				} else {
					myEndY = myStartY + bound;
				}
				terminate();
				return;
			}
		} else {
			if (getScrollingShift() <= -bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX - bound;
				} else {
					myEndY = myStartY - bound;
				}
				terminate();
				return;
			}
		}
		mySpeed *= mySpeedFactor;
	}
}
