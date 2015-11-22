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

package org.geometerplus.zlibrary.text.view;

import java.util.Stack;

import org.fbreader.util.Boolean3;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ViewOptions.GujiLayoutStyleEnum;
import org.geometerplus.fbreader.fbreader.options.ViewOptions.GujiCoverStyleEnum;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.view.ZLPaintContext.Size;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.style.*;
import org.geometerplus.zlibrary.ui.android.R;

import com.jni.bitmap_operations.JniBitmapHolder;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

abstract class ZLTextViewBase extends ZLView {
	public static enum ImageFitting {
		none, covers, all
	}

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;
	private ZLTextMetrics myMetrics;

	ZLTextViewBase(FBReaderApp application) {
		super(application);
	}

	private int myMaxSelectionDistance = 0;
	protected final int maxSelectionDistance() {
		if (myMaxSelectionDistance == 0) {
			myMaxSelectionDistance = ZLibrary.Instance().getDisplayDPI() / 20;
		}
		return myMaxSelectionDistance;
	}

	protected void resetMetrics() {
		myMetrics = null;
	}

	protected ZLTextMetrics metrics() {
		// this local variable is used to guarantee null will not
		// be returned from this method enen in multi-thread environment
		ZLTextMetrics m = myMetrics;
		if (m == null) {
			m = new ZLTextMetrics(
				ZLibrary.Instance().getDisplayDPI(),
				// TODO: screen area width
				100,
				// TODO: screen area height
				100,
				getTextStyleCollection().getBaseStyle().getFontSize()
			);
			myMetrics = m;
		}
		return m;
	}

	final int getWordHeight() {
		if (myWordHeight == -1) {
			final ZLTextStyle textStyle = myTextStyle;
			myWordHeight = getContext().getStringHeight() * textStyle.getLineSpacePercent() / 100 + textStyle.getVerticalAlign(metrics());
		}
		return myWordHeight;
	}

	public abstract ZLTextStyleCollection getTextStyleCollection();

	public abstract ImageFitting getImageFitting();

	public abstract int getLeftMargin();
	public abstract int getRightMargin();
	public abstract int getTopMargin();
	public abstract int getBottomMargin();
	public abstract int getSpaceBetweenColumns();
	public abstract int getTextAreaHeight();

	public abstract boolean twoColumnView();

	public abstract ZLFile getWallpaperFile();
	public abstract ZLPaintContext.FillMode getFillMode();
	public abstract ZLColor getBackgroundColor();
	public abstract ZLColor getSelectionBackgroundColor();
	public abstract ZLColor getSelectionForegroundColor();
	public abstract ZLColor getHighlightingBackgroundColor();
	public abstract ZLColor getHighlightingForegroundColor();
	public abstract ZLColor getTextColor(ZLTextHyperlink hyperlink);

	ZLPaintContext.Size getTextAreaSize() {
		return new ZLPaintContext.Size(getTextColumnWidth(), getTextAreaHeight());
	}

	int getFontSize() {
		return (int)myTextStyle.getFontSize(metrics());
	}
	
	protected int getColumnIndex(int x) {
		if (!twoColumnView()) {
			return -1;
		}
		return 2 * x <= (getContextWidth() + getLeftMargin() - getRightMargin() -getGujiLeftBankuangWidth() - getGujiRightBankuangWidth())? 0 : 1;
	}

	public int getTextColumnWidth() {
		return twoColumnView()
			? (getTextAreaWidth() - getSpaceBetweenColumns()) / 2
			: getTextAreaWidth();
	}

	int getTextAreaWidth() {
		return getContextWidth() - getLeftMargin() - getRightMargin()-getGujiLeftBankuangWidth() - getGujiRightBankuangWidth();
	}

	int getRightLine() {
		return getContextWidth() - getRightMargin() - getGujiRightBankuangWidth();
	}
	
	int getGujiWaiBankuangWidth() {
		return isGuji()? Application.ViewOptions.WaiBankuangWidthOption.getValue() : 0;
	}
	int getGujiNeiBankuangWidth() {
		return isGuji()? Application.ViewOptions.NeiBankuangWidthOption.getValue() : 0;
	}
	int getGujiSpaceBetweenBankuang() {
		return isGuji()? Application.ViewOptions.SpaceBetweenBankuangOption.getValue() : 0;
	}
	
	int myGujiLeftBankuangWidth = -1;
	int getGujiLeftBankuangWidth() {
		if(!isGuji()) return 0;
		if(myGujiLeftBankuangWidth != -1) return myGujiLeftBankuangWidth;
		myGujiLeftBankuangWidth = Application.ViewOptions.DoubleTopBankuangOption.getValue()? 
				getGujiWaiBankuangWidth() + getGujiNeiBankuangWidth() + getGujiSpaceBetweenBankuang(): 
					getGujiWaiBankuangWidth();
		return myGujiLeftBankuangWidth;
	}
	
	int myGujiRightBankuangWidth = -1;
	int getGujiRightBankuangWidth() {
		if(!isGuji()) return 0;
		if(myGujiRightBankuangWidth != -1) return myGujiRightBankuangWidth;
		myGujiRightBankuangWidth = Application.ViewOptions.DoubleBottomBankuangOption.getValue()? 
				getGujiWaiBankuangWidth() + getGujiNeiBankuangWidth() + getGujiSpaceBetweenBankuang(): 
					getGujiWaiBankuangWidth();
		return myGujiRightBankuangWidth;
	}
	
	int myGujiTopBankuangWidth = -1;
	int getGujiTopBankuangWidth() {
		if(!isGuji()) return 0;
		if(myGujiTopBankuangWidth != -1) return myGujiTopBankuangWidth;
		myGujiTopBankuangWidth = Application.ViewOptions.DoubleRightBankuangOption.getValue()? 
				getGujiWaiBankuangWidth() + getGujiNeiBankuangWidth() + getGujiSpaceBetweenBankuang(): 
					getGujiWaiBankuangWidth();
		return myGujiTopBankuangWidth;
	}
	
	int myGujiBottomBankuangWidth = -1;
	int getGujiBottomBankuangWidth() {
		if(!isGuji()) return 0;
		if(myGujiBottomBankuangWidth != -1) return myGujiBottomBankuangWidth;
		myGujiBottomBankuangWidth = Application.ViewOptions.DoubleLeftBankuangOption.getValue()? 
				getGujiWaiBankuangWidth() + getGujiNeiBankuangWidth() + getGujiSpaceBetweenBankuang(): 
					getGujiWaiBankuangWidth();
		return myGujiBottomBankuangWidth;
	}
	
	int getGujiHudieHalfLineNum() {
		int gujiLeftBanWidth = getTextAreaHeight()/2;		
		return (gujiLeftBanWidth)/getGujiPossibleLineHeight();
	}
	
	int getGujiBaobeiLineNum() {
		return getTextAreaHeight()/getGujiPossibleLineHeight();
	}
	
	int getGujiBaobeiLineHeight() {
		if(!isGuji()) return 0;
		int gujiBanWidth = getTextAreaHeight();		
		int gujiLineNum = getTextAreaHeight()/getGujiPossibleLineHeight();
		return gujiLineNum == 0?gujiBanWidth:(gujiBanWidth)/gujiLineNum;
	}
	
	int getGujiLineHeight() {
		if(!isGuji()) return 0;
		return getGujiStyle() == GujiLayoutStyleEnum.baobei? 
				getGujiBaobeiLineHeight() : getGujiHudieLineHeight();
	}
	
	int myTextAreaHeight = -1;
	public void clearGujiTextCache() {
		if(!isGuji()) return;
		myGujiPossibleLineHeight = -1;
		myGujiStyle = null;
		myTextAreaHeight = -1;
		myGujiLeftBankuangWidth = -1;
		myGujiRightBankuangWidth = -1;
		myGujiTopBankuangWidth = -1;
		myGujiBottomBankuangWidth = -1;
	}
	
	//TODO
	protected int myGujiPossibleLineHeight = -1;
	int getGujiPossibleLineHeight() {
		if(!isGuji()) return 0;
		if(myGujiPossibleLineHeight != -1) return myGujiPossibleLineHeight;
		int baseFontSize = getTextStyleCollection().getBaseStyle().getFontSize();
		int width=  (int)(baseFontSize * getTextStyleCollection().getBaseStyle().getLineSpacePercent() / 100) + getTextStyleCollection().getBaseStyle().getVerticalAlign(metrics());
		int fontSubSize = getTextStyleCollection().getDescription(FBTextKind.GUJI_ANNOTATION).getFontSize(metrics(), baseFontSize);
		int fontCodeSize = getTextStyleCollection().getDescription(FBTextKind.GUJI_TRANSLATION).getFontSize(metrics(), baseFontSize);
		if( fontSubSize*2> width) width=fontSubSize*2;
		if( fontCodeSize*2> width) width=fontCodeSize*2;
		myGujiPossibleLineHeight = (int)(width);
		return myGujiPossibleLineHeight;
	}
	
	int getGujiBanxinHeight() {
		if(!isGuji()) return 0;
		if(getGujiStyle() == GujiLayoutStyleEnum.jingzhe) {
			return getGujiLeftBankuangWidth();
		} else {
			return getGujiPossibleLineHeight();
		}
	}
	
	int getGujiHudieLineHeight() {
		if(!isGuji()) return 0;
		int gujiLeftBanWidth = getTextAreaHeight()/2;		
		int gujiLineNum = (gujiLeftBanWidth)/getGujiPossibleLineHeight();
		return gujiLineNum == 0?gujiLeftBanWidth:(gujiLeftBanWidth)/gujiLineNum;
	}
	
	boolean isLandscape() {
		Configuration newConfig = ZLibrary.Instance().getResources().getConfiguration();  
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){  
        	return true;
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        	return false;
        }
        return false;
//		String option = ZLibrary.Instance().getOrientationOption().getValue();
//		return option.equals(ZLibrary.SCREEN_ORIENTATION_LANDSCAPE) || 
//				option.equals(ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	}
	
	GujiLayoutStyleEnum myGujiStyle = null;
	GujiLayoutStyleEnum getGujiStyle() {
		if(myGujiStyle != null) return myGujiStyle;
		GujiLayoutStyleEnum ret = Application.ViewOptions.ShowGujiLayoutOption.getValue();
		if(ret == GujiLayoutStyleEnum.baobei && isLandscape()) {
			ret = GujiLayoutStyleEnum.hudie;
		}
		myGujiStyle =  ret;
		return ret;
	}
	
	JniBitmapHolder myGujiCoverbitmap = null;
	GujiCoverStyleEnum myCoverStyle;
	public JniBitmapHolder getGujiCover() {
		GujiCoverStyleEnum value = Application.ViewOptions.ShowGujiCoverOption.getValue();
		if(myCoverStyle == value && myGujiCoverbitmap != null) {
			return myGujiCoverbitmap;
		}
		if(myGujiCoverbitmap != null) {
			myGujiCoverbitmap.freeBitmap();
			myGujiCoverbitmap = null;
		}
		myCoverStyle = value;
		if(value == GujiCoverStyleEnum.heke) {
			myGujiCoverbitmap = getImage(R.drawable.guji_cover_type1, new Size(getContextHeight() -2, getContextWidth()-2));
		} else if(value == GujiCoverStyleEnum.royal) {
			myGujiCoverbitmap = getImage(R.drawable.guji_cover_type2, new Size(getContextHeight()-2, getContextWidth()-2));
		} else if(value == GujiCoverStyleEnum.fangke) {
			myGujiCoverbitmap = getImage(R.drawable.guji_cover_type3, new Size(getContextHeight()-2, getContextWidth()-2));
		}
		return myGujiCoverbitmap;
	}
	
	public JniBitmapHolder getImage(int resId, Size maxSize) {
		try {
			Drawable d = ZLibrary.Instance().getResources().getDrawable(resId);
			Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable)d).getBitmap();
			
			Matrix a = new Matrix();
			if(maxSize != null) {
				a.setScale(((float)maxSize.Width)/bitmap.getWidth(), ((float)maxSize.Height)/bitmap.getHeight());
				a.postRotate(-90);
			} else {
				a.setRotate(-90);
			}
		
			Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), a, true);
			JniBitmapHolder b = new JniBitmapHolder(result);
			bitmap.recycle();
			result.recycle();
			return b;
		} catch(OutOfMemoryError e) {
			
		}
		return null;
	}
	
	private Bitmap myGujiJielan;
	public Bitmap getGujiJielan(Size maxSize) {
		if(myGujiJielan != null && !myGujiJielan.isRecycled()) return myGujiJielan;
		Drawable d = ZLibrary.Instance().getResources().getDrawable(R.drawable.border);
		Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable)d).getBitmap();
		Matrix a = new Matrix();
		if(maxSize != null) {
			a.setScale(((float)maxSize.Width)/bitmap.getWidth(), ((float)maxSize.Height)/bitmap.getHeight());
			a.postRotate(-90);
		} else {
			a.setRotate(-90);
		}
		myGujiJielan = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), a, true);
		bitmap.recycle();
		return myGujiJielan;
	}
	private Bitmap myGujiYinzhang;
	public Bitmap getGujiYinZhang(Size maxSize) {
		if(myGujiYinzhang != null && !myGujiYinzhang.isRecycled()) return myGujiYinzhang;
		Drawable d = ZLibrary.Instance().getResources().getDrawable(R.drawable.guji_yinzhang_yinwen);
		Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable)d).getBitmap();
		Matrix a = new Matrix();
		if(maxSize != null) {
			a.setScale(((float)maxSize.Width)/bitmap.getWidth(), ((float)maxSize.Height)/bitmap.getHeight());
			a.postRotate(-88);
		} else {
			a.setRotate(-88);
		}
		myGujiYinzhang = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), a, true);
		bitmap.recycle();
		return myGujiYinzhang;
	}
	
	final ZLTextStyle getTextStyle() {
		if (myTextStyle == null) {
			resetTextStyle();
		}
		return myTextStyle;
	}

	final void setTextStyle(ZLTextStyle style) {
		if (myTextStyle != style) {
			myTextStyle = style;
			myWordHeight = -1;
		}
		getContext().setFont(style.getFontEntries(), style.getFontSize(metrics()), style.isBold(), style.isItalic(), style.isUnderline(), style.isStrikeThrough());
	}

	final void resetTextStyle() {
		setTextStyle(getTextStyleCollection().getBaseStyle());
	}

	boolean isStyleChangeElement(ZLTextElement element) {
		return
			element == ZLTextElement.StyleClose ||
			element instanceof ZLTextStyleElement ||
			element instanceof ZLTextControlElement;
	}

	void applyStyleChangeElement(ZLTextElement element) {
		if (element == ZLTextElement.StyleClose) {
			applyStyleClose();
		} else if (element instanceof ZLTextStyleElement) {
			applyStyle((ZLTextStyleElement)element);
		} else if (element instanceof ZLTextControlElement) {
			applyControl((ZLTextControlElement)element);
		}
	}
	
	byte getLastOpenControlKind(ZLTextParagraphCursor cursor, int index) {
		if(!isGuji()) return -1;
		int i = index;
		byte ret = -1;
		Stack<Byte> stack = new Stack<Byte>();
		for (; i >= 0; i--) {
			ZLTextElement element = cursor.getElement(i);
			if(element == null) return ret;
			if (element instanceof ZLTextControlElement) {
				ZLTextControlElement control = ((ZLTextControlElement)element);
				if(i == index) {
					return control.Kind;
				}
				if(!control.IsStart) {
					stack.push(control.Kind);
				} else if(control.IsStart) {
					if(!stack.isEmpty()) {
						if(stack.peek().byteValue() == control.Kind) {
							stack.pop();
						} else {
							return control.Kind;
						}
					} else {
						return control.Kind;
					}
				}
			}
		}
		return ret;
	}

	void applyStyleChanges(ZLTextParagraphCursor cursor, int index, int end) {
		for (; index != end; ++index) {
			applyStyleChangeElement(cursor.getElement(index));
		}
	}

	private void applyControl(ZLTextControlElement control) {
		if (control.IsStart) {
			final ZLTextHyperlink hyperlink = control instanceof ZLTextHyperlinkControlElement
				? ((ZLTextHyperlinkControlElement)control).Hyperlink : null;
			final ZLTextNGStyleDescription description =
				getTextStyleCollection().getDescription(control.Kind);
			if (description != null) {
				setTextStyle(new ZLTextNGStyle(myTextStyle, description, hyperlink));
			}
		} else {
			if(control.Kind != FBTextKind.GUJI_SUPERSCRIPT) {
				setTextStyle(myTextStyle.Parent);
			}
		}
	}

	private void applyStyle(ZLTextStyleElement element) {
		setTextStyle(new ZLTextExplicitlyDecoratedStyle(myTextStyle, element.Entry));
	}

	private void applyStyleClose() {
		setTextStyle(myTextStyle.Parent);
	}

	protected final ZLPaintContext.ScalingType getScalingType(ZLTextImageElement imageElement) {
		switch (getImageFitting()) {
			default:
			case none:
				return ZLPaintContext.ScalingType.IntegerCoefficient;
			case covers:
				return imageElement.IsCover
					? ZLPaintContext.ScalingType.FitMaximum
					: ZLPaintContext.ScalingType.IntegerCoefficient;
			case all:
				return ZLPaintContext.ScalingType.FitMaximum;
		}
	}

	public int getSpaceWidth() {
		if(isGuji()) return getContext().getStringHeight();
		return getContext().getSpaceWidth();
	}
	final int getElementWidth(ZLTextElement element, int charIndex) {
		if (element instanceof ZLTextWord) {
			return getWordWidth((ZLTextWord)element, charIndex);
		} else if (element instanceof ZLTextImageElement) {
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;
			final ZLPaintContext.Size size = getContext().imageSize(
				imageElement.ImageData,
				getTextAreaSize(),
				getScalingType(imageElement)
			);
			return size != null ? size.Width : 0;
		} else if (element instanceof ZLTextVideoElement) {
			return Math.min(300, getTextColumnWidth());
		} else if (element instanceof ExtensionElement) {
			return ((ExtensionElement)element).getWidth();
		} else if (element == ZLTextElement.NBSpace || element == ZLTextElement.HSpace) {
			return getSpaceWidth();
		} else if (element == ZLTextElement.Indent) {
			return myTextStyle.getFirstLineIndent(metrics());
		} else if (element instanceof ZLTextFixedHSpaceElement) {
			return getSpaceWidth() * ((ZLTextFixedHSpaceElement)element).Length;
		}
		return 0;
	}

	final int getElementHeight(ZLTextElement element) {
		if (element == ZLTextElement.NBSpace ||
			element instanceof ZLTextWord ||
			element instanceof ZLTextFixedHSpaceElement) {
			return getWordHeight();
		} else if (element instanceof ZLTextImageElement) {
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;
			final ZLPaintContext.Size size = getContext().imageSize(
				imageElement.ImageData,
				getTextAreaSize(),
				getScalingType(imageElement)
			);
			return (size != null ? size.Height : 0) +
				Math.max(getContext().getStringHeight() * (myTextStyle.getLineSpacePercent() - 100) / 100, 3);
		} else if (element instanceof ZLTextVideoElement) {
			return Math.min(Math.min(200, getTextAreaHeight()), getTextColumnWidth() * 2 / 3);
		} else if (element instanceof ExtensionElement) {
			return ((ExtensionElement)element).getHeight();
		}
		return 0;
	}

	final int getElementDescent(ZLTextElement element) {
		return element instanceof ZLTextWord ? getContext().getDescent() : 0;
	}

	final int getWordWidth(ZLTextWord word, int start) {
		return
			start == 0 ?
				word.getWidth(getContext()) :
				getContext().getStringWidth(word.Data, word.Offset + start, word.Length - start);
	}

	final int getWordWidth(ZLTextWord word, int start, int length) {
		return getContext().getStringWidth(word.Data, word.Offset + start, length);
	}

	private char[] myWordPartArray = new char[20];

	final int getWordWidth(ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		if (length == -1) {
			if (start == 0) {
				return word.getWidth(getContext());
			}
			length = word.Length - start;
		}
		if (!addHyphenationSign) {
			return getContext().getStringWidth(word.Data, word.Offset + start, length);
		}
		char[] part = myWordPartArray;
		if (length + 1 > part.length) {
			part = new char[length + 1];
			myWordPartArray = part;
		}
		System.arraycopy(word.Data, word.Offset + start, part, 0, length);
		part[length] = '-';
		return getContext().getStringWidth(part, 0, length + 1);
	}

	int getAreaLength(ZLTextParagraphCursor paragraph, ZLTextElementArea area, int toCharIndex) {
		setTextStyle(area.Style);
		final ZLTextWord word = (ZLTextWord)paragraph.getElement(area.ElementIndex);
		int length = toCharIndex - area.CharIndex;
		boolean selectHyphenationSign = false;
		if (length >= area.Length) {
			selectHyphenationSign = area.AddHyphenationSign;
			length = area.Length;
		}
		if (length > 0) {
			return getWordWidth(word, area.CharIndex, length, selectHyphenationSign);
		}
		return 0;
	}

	final void drawWord(int x, int y, ZLTextWord word, int start, int length, boolean addHyphenationSign, ZLColor color, Boolean3 languageType) {
		final ZLPaintContext context = getContext();
		if (start == 0 && length == -1) {
			drawString(context, x, y, word.Data, word.Offset, word.Length, word.getMark(), color, 0, languageType);
		} else {
			if (length == -1) {
				length = word.Length - start;
			}
			if (!addHyphenationSign) {
				drawString(context, x, y, word.Data, word.Offset + start, length, word.getMark(), color, start, languageType);
			} else {
				char[] part = myWordPartArray;
				if (length + 1 > part.length) {
					part = new char[length + 1];
					myWordPartArray = part;
				}
				System.arraycopy(word.Data, word.Offset + start, part, 0, length);
				part[length] = '-';
				drawString(context, x, y, part, 0, length + 1, word.getMark(), color, start, languageType);
			}
		}
	}

	private final void drawString(ZLPaintContext context, int x, int y, char[] str, int offset, int length, ZLTextWord.Mark mark, ZLColor color, int shift, Boolean3 languageType) {
		if (mark == null) {
			context.setTextColor(color);
			context.drawString(x, y, str, offset, length, isGuji(), languageType);
		} else {
			int pos = 0;
			for (; (mark != null) && (pos < length); mark = mark.getNext()) {
				int markStart = mark.Start - shift;
				int markLen = mark.Length;

				if (markStart < pos) {
					markLen += markStart - pos;
					markStart = pos;
				}

				if (markLen <= 0) {
					continue;
				}

				if (markStart > pos) {
					int endPos = Math.min(markStart, length);
					context.setTextColor(color);
					context.drawString(x, y, str, offset + pos, endPos - pos, isGuji(), languageType);
					x += context.getStringWidth(str, offset + pos, endPos - pos);
				}

				if (markStart < length) {
					context.setFillColor(getHighlightingBackgroundColor());
					int endPos = Math.min(markStart + markLen, length);
					final int endX = x + context.getStringWidth(str, offset + markStart, endPos - markStart);
					if(isGuji()) {
						//y'=x, x'=-y;
						context.fillRectangle(-(y - context.getStringHeight()), x, -( y + context.getDescent()), endX - 1);
					} else {
						context.fillRectangle(x, y - context.getStringHeight(), endX - 1, y + context.getDescent());
					}
					context.setTextColor(getHighlightingForegroundColor());
					context.drawString(x, y, str, offset + markStart, endPos - markStart, isGuji(), languageType);
					x = endX;
				}
				pos = markStart + markLen;
			}

			if (pos < length) {
				context.setTextColor(color);
				context.drawString(x, y, str, offset + pos, length - pos, isGuji(), languageType);
			}
		}
	}
}
