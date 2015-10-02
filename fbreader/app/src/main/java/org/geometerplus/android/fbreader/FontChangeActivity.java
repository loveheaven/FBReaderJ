/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.ArrayList;

import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class FontChangeActivity extends ListActivity {
	private final ZLResource myResource = ZLResource.resource("Preferences");
	private String myIntialValue = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(myResource.getResource("text").getResource("font").getValue());
		reload();
	}
	
	public void reload() {
		final ArrayList<String> fonts = new ArrayList<String>();
		AndroidFontUtil.fillFamiliesList(fonts);
		
		FBReaderApp application = (FBReaderApp)(FBReaderApp.Instance());
		ZLStringOption option = application.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;
		final String optionValue = option.getValue();
		final String initialValue = optionValue.length() > 0 ?
			AndroidFontUtil.realFontFamilyName(optionValue) : null;
		for (String fontName : fonts) {
			if (initialValue.equals(fontName)) {
				myIntialValue = fontName;
				break;
			}
		}
		if(myIntialValue == null) {
			for (String fontName : fonts) {
				if (initialValue.equals(AndroidFontUtil.realFontFamilyName(fontName))) {
					myIntialValue = fontName;
					break;
				}
			}
		}
		
		final FontListAdapter adapter = new FontListAdapter(fonts);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);
		getListView().setChoiceMode(android.widget.AbsListView.CHOICE_MODE_SINGLE);
	}

	private class FontListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final ArrayList<String> myFonts;

		FontListAdapter(ArrayList<String> orientations) {
			myFonts = orientations;
		}

		public final int getCount() {
			return myFonts.size();
		}

		public final String getItem(int position) {
			return myFonts.isEmpty() ? null : myFonts.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.orientation_item, parent, false);
			final TextView titleView = ViewUtil.findTextView(view, R.id.orientation_name);
			final RadioButton checkView = ViewUtil.findRadioButton(view, R.id.orientation_checkbox);
			final String item = getItem(position);
			if (item != null) {
				titleView.setText(item);
				if(item.equals(myIntialValue)) {
					checkView.setChecked(true);
				} else {
					checkView.setChecked(false);
				}
			}
			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			final String item = getItem(position);
			if (item != null) {
				runOnUiThread(new Runnable() {
					public void run() {
						finish();
						FBReaderApp application = (FBReaderApp)(FBReaderApp.Instance());
						ZLStringOption option = application.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;
						option.setValue(item);
						//ZLApplication.Instance().runAction(item.Code);
					}
				});
			}
		}
	}
}
