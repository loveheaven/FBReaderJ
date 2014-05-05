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

package org.geometerplus.android.fbreader;

import jopencc.util.ChineseConvertor;
import jopencc.util.Dict;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

public class FBReaderApplication extends ZLAndroidApplication {
	@Override
	public void onCreate() {
		super.onCreate();
		//bindService(new Intent(this, LibraryService.class), null, LibraryService.BIND_AUTO_CREATE);
		ChineseConvertor.DICT_TO_ZHS = new Dict(Dict.ZHT_TO_ZHS, this.getApplicationContext());
        ChineseConvertor.DICT_TO_ZHT = new Dict(Dict.ZHS_TO_ZHT, this.getApplicationContext());
	}
}
