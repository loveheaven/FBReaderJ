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

package org.geometerplus.fbreader.fbreader.options;

import org.fbreader.util.Boolean3;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.fbreader.fbreader.FBView;

public class ViewOptions implements ZLOption.ZLOptionCallback{
	public static enum GujiLayoutStyleEnum {
		jingzhe, hudie, baobei
	}
	
	public static enum GujiBanxinStyleEnum {
		daheikou, xiaoheikou, baikou, booktitle
	}
	
	public static enum GujiCoverStyleEnum {
		heke, royal, fangke, mock
	}
	
	public static enum GujiPunctuationEnum {
		show, hide, judou
	}

	public final ZLEnumOption<GujiCoverStyleEnum> ShowGujiCoverOption;
	public final ZLEnumOption<GujiLayoutStyleEnum> ShowGujiLayoutOption;
	public final ZLEnumOption<GujiBanxinStyleEnum> ShowGujiBanxinOption;
	public final ZLBooleanOption ShowGujiJielanOption;
	public final ZLBooleanOption DoubleLeftBankuangOption;
	public final ZLBooleanOption DoubleRightBankuangOption;
	public final ZLBooleanOption DoubleTopBankuangOption;
	public final ZLBooleanOption DoubleBottomBankuangOption;
	public final ZLIntegerRangeOption WaiBankuangWidthOption;
	public final ZLIntegerRangeOption NeiBankuangWidthOption;
	public final ZLIntegerRangeOption SpaceBetweenBankuangOption;
	public final ZLBooleanOption ShowGujiAnnotationOption;
	public final ZLBooleanOption ShowGujiTranslationOption;
	public final ZLBooleanOption ShowGujiSuperscriptOption;
	public final ZLEnumOption<GujiPunctuationEnum> ShowGujiPunctuationOption;
	public final ZLColorOption GujiBanxinColorOption;
	public final ZLBoolean3Option ShowTranditionalOption;
	public final ZLBooleanOption TwoColumnView;
	public final ZLIntegerRangeOption LeftMargin;
	public final ZLIntegerRangeOption RightMargin;
	public final ZLIntegerRangeOption TopMargin;
	public final ZLIntegerRangeOption BottomMargin;
	public final ZLIntegerRangeOption GujiLeftMargin;
	public final ZLIntegerRangeOption GujiRightMargin;
	public final ZLIntegerRangeOption GujiTopMargin;
	public final ZLIntegerRangeOption GujiBottomMargin;
	public final ZLBooleanOption HeaderHidden;
	public final ZLIntegerRangeOption HeaderHeight;
	public final ZLIntegerRangeOption SpaceBetweenColumns;
	public final ZLIntegerRangeOption ScrollbarType;
	public final ZLIntegerRangeOption FooterHeight;
	public final ZLStringOption ColorProfileName;

	private ColorProfile myColorProfile;
	private ZLTextStyleCollection myTextStyleCollection;
	private FooterOptions myFooterOptions;

	public ViewOptions() {
		final ZLibrary zlibrary = ZLibrary.Instance();

		final int dpi = zlibrary.getDisplayDPI();
		final int x = zlibrary.getWidthInPixels();
		final int y = zlibrary.getHeightInPixels();
		final int horMargin = Math.min(dpi / 5, Math.min(x, y) / 30);

		ShowGujiCoverOption =
				new ZLEnumOption<GujiCoverStyleEnum>("Options", "ShowGujiCover", GujiCoverStyleEnum.royal);
		ShowGujiLayoutOption =
					new ZLEnumOption<GujiLayoutStyleEnum>("Options", "ShowGujiLayout", GujiLayoutStyleEnum.baobei);
		ShowGujiBanxinOption =
				new ZLEnumOption<GujiBanxinStyleEnum>("Options", "ShowGujiBanxin", GujiBanxinStyleEnum.booktitle);
		ShowGujiLayoutOption.setCallback(this);
		ShowGujiJielanOption = new ZLBooleanOption("Options", "ShowGujiJielan", true);
		DoubleLeftBankuangOption =
				new ZLBooleanOption("Options", "DoubleLeftBankuang", true);
		DoubleLeftBankuangOption.setCallback(this);
		DoubleRightBankuangOption =
				new ZLBooleanOption("Options", "DoubleRightBankuang", true);
		DoubleRightBankuangOption.setCallback(this);
		DoubleTopBankuangOption =
				new ZLBooleanOption("Options", "DoubleTopBankuang", true);
		DoubleTopBankuangOption.setCallback(this);
		DoubleBottomBankuangOption =
				new ZLBooleanOption("Options", "DoubleBottomBankuang", true);
		DoubleBottomBankuangOption.setCallback(this);
		WaiBankuangWidthOption =
				new ZLIntegerRangeOption("Options", "WaiBankuangWidth", 0, 100, horMargin/3+1);
		WaiBankuangWidthOption.setCallback(this);
		NeiBankuangWidthOption =
				new ZLIntegerRangeOption("Options", "NeiBankuang", 0, 100, 2);
		NeiBankuangWidthOption.setCallback(this);
		SpaceBetweenBankuangOption =
				new ZLIntegerRangeOption("Options", "SpaceBetweenBankuang", 0, 100, 2);
		SpaceBetweenBankuangOption.setCallback(this);
		ShowGujiTranslationOption =
				new ZLBooleanOption("Options", "ShowGujiTranslation", true);
		ShowGujiAnnotationOption =
				new ZLBooleanOption("Options", "ShowGujiAnnotation", true);
		ShowGujiPunctuationOption =
				new ZLEnumOption<GujiPunctuationEnum>("Options", "ShowGujiPunctuation", GujiPunctuationEnum.judou);
		ShowGujiSuperscriptOption =
				new ZLBooleanOption("Options", "ShowGujiSuperscript", true);
		GujiBanxinColorOption = new ZLColorOption("Colors", "GujiBanxinColor", ZLColor.GUJI_TRANSLATE);
		ShowTranditionalOption = new ZLBoolean3Option("Options", "ShowTranditional", Boolean3.UNDEFINED);
		
		TwoColumnView =
			new ZLBooleanOption("Options", "TwoColumnView", x * x + y * y >= 42 * dpi * dpi);
		LeftMargin =
			new ZLIntegerRangeOption("Options", "LeftMargin", 0, x/4, horMargin);
		RightMargin =
			new ZLIntegerRangeOption("Options", "RightMargin", 0, x/4, horMargin);
		GujiLeftMargin =
				new ZLIntegerRangeOption("Options", "GujiLeftMargin", 0, x/4, horMargin*3);
		GujiRightMargin =
				new ZLIntegerRangeOption("Options", "GujiRightMargin", 0, x/4, horMargin*3);
		TopMargin =
			new ZLIntegerRangeOption("Options", "TopMargin", 0, y/4, horMargin);
		GujiTopMargin =
				new ZLIntegerRangeOption("Options", "GujiTopMargin", 0, y/4, 400*y/1920);
		GujiTopMargin.setCallback(this);
		HeaderHidden = 
			new ZLBooleanOption("Options", "HeaderHidden", false);
		HeaderHeight =
			new ZLIntegerRangeOption("Options", "HeaderHeight", 0, 100, horMargin);
		BottomMargin =
			new ZLIntegerRangeOption("Options", "BottomMargin", 0, y/4, horMargin);
		GujiBottomMargin =
				new ZLIntegerRangeOption("Options", "GujiBottomMargin", 0, y/4, 200*y/1920);
		GujiBottomMargin.setCallback(this);
		SpaceBetweenColumns =
			new ZLIntegerRangeOption("Options", "SpaceBetweenColumns", 0, 300, 3 * horMargin);
		ScrollbarType =
			new ZLIntegerRangeOption("Options", "ScrollbarType", 0, 4, FBView.SCROLLBAR_SHOW_AS_FOOTER);
		FooterHeight =
			new ZLIntegerRangeOption("Options", "FooterHeight", 8, dpi / 8, dpi / 20);
		ColorProfileName =
			new ZLStringOption("Options", "ColorProfile", ColorProfile.DAY);
		ColorProfileName.setSpecialName("colorProfile");
	}

	public ColorProfile getColorProfile() {
		final String name = ColorProfileName.getValue();
		if (myColorProfile == null || !name.equals(myColorProfile.Name)) {
			myColorProfile = ColorProfile.get(name);
		}
		return myColorProfile;
	}

	public ZLTextStyleCollection getTextStyleCollection() {
		if (myTextStyleCollection == null) {
			myTextStyleCollection = new ZLTextStyleCollection("Base");
		}
		return myTextStyleCollection;
	}

	public FooterOptions getFooterOptions() {
		if (myFooterOptions == null) {
			myFooterOptions = new FooterOptions();
		}
		return myFooterOptions;
	}
	@Override
	public void setValueCallback() {
		ZLView view = ZLApplication.Instance().getCurrentView();
		if(view != null && view instanceof ZLTextView) {
			ZLTextView textview = (ZLTextView) view;
			if(view.isGuji()) {
				textview.clearCaches();
			}
		}
	}
}
