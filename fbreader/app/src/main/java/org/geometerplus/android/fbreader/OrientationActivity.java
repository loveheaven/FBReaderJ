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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.XmlUtil;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.api.MenuNode;
import org.geometerplus.android.util.PackageUtil;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;

public class OrientationActivity extends ListActivity {
	private final ZLResource myResource = ZLResource.resource("menu");

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(myResource.getResource(ActionCode.SET_SCREEN_ORIENTATION).getValue());
		
		final MenuNode.Submenu orientations = new MenuNode.Submenu(ActionCode.SET_SCREEN_ORIENTATION);
		orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM));
		orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SENSOR));
		orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT));
		orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
		
		final OrientationListAdapter adapter = new OrientationListAdapter(orientations);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);
		getListView().setChoiceMode(android.widget.AbsListView.CHOICE_MODE_SINGLE);
	}

	private class OrientationListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final MenuNode.Submenu myOrientations;

		OrientationListAdapter(MenuNode.Submenu orientations) {
			myOrientations = orientations;
		}

		public final int getCount() {
			return myOrientations.Children.isEmpty() ? 1 : myOrientations.Children.size();
		}

		public final MenuNode getItem(int position) {
			return myOrientations.Children.isEmpty() ? null : myOrientations.Children.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.orientation_item, parent, false);
			final TextView titleView = ViewUtil.findTextView(view, R.id.orientation_name);
			final CheckBox checkView = ViewUtil.findCheckBox(view, R.id.orientation_checkbox);
			final MenuNode item = getItem(position);
			if (item != null) {
				final ZLResource resource = myResource.getResource(item.Code);
				titleView.setText(resource.getValue());
				switch (ZLApplication.Instance().isActionChecked(item.Code)) {
				case B3_TRUE:
					checkView.setChecked(true);
					break;
				case B3_FALSE:
					checkView.setChecked(false);
					break;
				case B3_UNDEFINED:
					checkView.setEnabled(false);
					break;
				}
			}
			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			final MenuNode item = getItem(position);
			if (item != null) {
				runOnUiThread(new Runnable() {
					public void run() {
						finish();
						ZLApplication.Instance().runAction(item.Code);
					}
				});
			}
		}
	}
}
