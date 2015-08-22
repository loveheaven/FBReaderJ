/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

final class NavigationPopup extends ZLApplication.PopupPanel {
	final static String ID = "NavigationPopup";

	private volatile NavigationWindow myWindow;
	private volatile FBReader myActivity;
	private volatile RelativeLayout myRoot;
	private ZLTextWordCursor myStartPosition;
	private final FBReaderApp myFBReader;
	private volatile boolean myIsInProgress;

	NavigationPopup(FBReaderApp fbReader) {
		super(fbReader);
		myFBReader = fbReader;
	}

	public void setPanelInfo(FBReader activity, RelativeLayout root) {
		myActivity = activity;
		myRoot = root;
	}

	public void runNavigation() {
		if (myWindow == null || myWindow.getVisibility() == View.GONE) {
			myIsInProgress = false;
			if (myStartPosition == null) {
				myStartPosition = new ZLTextWordCursor(myFBReader.getTextView().getStartCursor());
			}
			Application.showPopup(ID);
		}
	}

	@Override
	protected void show_() {
		if (myActivity != null) {
			createPanel(myActivity, myRoot);
		}
		if (myWindow != null) {
			myWindow.show();
			setupNavigation();
		}
	}

	@Override
	protected void hide_() {
		final ZLTextWordCursor position = myStartPosition;
			if (myStartPosition != null &&
				!myStartPosition.equals(myFBReader.getTextView().getStartCursor())) {
				myFBReader.addInvisibleBookmark(myStartPosition);
				myFBReader.storePosition();
			}
		myStartPosition = null;
		myFBReader.getViewWidget().reset();
		myFBReader.getViewWidget().repaint();
		if (myWindow != null) {
			myWindow.hide();
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void update() {
		if (!myIsInProgress && myWindow != null) {
			setupNavigation();
		}
	}

	private void createPanel(FBReader activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getContext()) {
			return;
		}

		activity.getLayoutInflater().inflate(R.layout.navigation_panel, root);
		myWindow = (NavigationWindow)root.findViewById(R.id.navigation_panel);

		TextView changeFontSize = (TextView)myWindow.findViewById(R.id.navigation_fontsize);
		changeFontSize.setText(ZLResource.resource("Preferences").getResource("text").getResource("font").getValue());
		final ImageButton zoomin = (ImageButton)myWindow.findViewById(R.id.navigation_zoomin);
		zoomin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.INCREASE_FONT);
			}
		});
		final ImageButton zoomout = (ImageButton)myWindow.findViewById(R.id.navigation_zoomout);
		zoomout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.DECREASE_FONT);
			}
		});
		
		ImageButton toc = (ImageButton)myWindow.findViewById(R.id.navigation_toc);
		boolean visible = myFBReader.isActionVisible(ActionCode.SHOW_TOC) && myFBReader.isActionEnabled(ActionCode.SHOW_TOC);
		toc.setVisibility(visible?View.VISIBLE:View.GONE);
		//toc.setText(ZLResource.resource("menu").getResource(ActionCode.SHOW_TOC).getValue());
		toc.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SHOW_TOC);
			}
		});
		ImageButton bookmark = (ImageButton)myWindow.findViewById(R.id.navigation_bookmark);
		//bookmark.setText(ZLResource.resource("menu").getResource(ActionCode.SHOW_BOOKMARKS).getValue());
		bookmark.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SHOW_BOOKMARKS);
			}
		});
		ImageButton settings = (ImageButton)myWindow.findViewById(R.id.navigation_settings);
		//settings.setText(ZLResource.resource("menu").getResource(ActionCode.SHOW_PREFERENCES).getValue());
		settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SHOW_PREFERENCES);
			}
		});
		ImageButton search = (ImageButton)myWindow.findViewById(R.id.navigation_search);
		//search.setText(ZLResource.resource("menu").getResource(ActionCode.SEARCH).getValue());
		search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SEARCH);
			}
		});
		ImageButton share = (ImageButton)myWindow.findViewById(R.id.navigation_share);
		//share.setText(ZLResource.resource("menu").getResource(ActionCode.SHARE_BOOK).getValue());
		share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SHARE_BOOK);
			}
		});
		ImageButton info = (ImageButton)myWindow.findViewById(R.id.navigation_info);
		//info.setText(ZLResource.resource("menu").getResource(ActionCode.SHOW_BOOK_INFO).getValue());
		info.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SHOW_BOOK_INFO);
			}
		});
		
		ImageButton orientation = (ImageButton)myWindow.findViewById(R.id.navigation_orientation);
		//orientation.setText(ZLResource.resource("menu").getResource(ActionCode.SET_SCREEN_ORIENTATION).getValue());
		orientation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SET_SCREEN_ORIENTATION);
			}
		});
		ImageButton install = (ImageButton)myWindow.findViewById(R.id.navigation_install);
		//install.setText(ZLResource.resource("menu").getResource(ActionCode.INSTALL_PLUGINS).getValue());
		install.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.INSTALL_PLUGINS);
			}
		});
	
		ImageButton more = (ImageButton)myWindow.findViewById(R.id.navigation_more);
		//more.setText(ZLResource.resource("menu").getResource(ActionCode.SHOW_MORE).getValue());
		more.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myActivity.openOptionsMenu();
			}
		});
		final Button night = (Button)myWindow.findViewById(R.id.navigation_night);
		night.setText(ZLResource.resource("menu").getResource(ActionCode.SWITCH_TO_NIGHT_PROFILE).getValue());
		visible = myFBReader.isActionVisible(ActionCode.SWITCH_TO_NIGHT_PROFILE) && myFBReader.isActionEnabled(ActionCode.SWITCH_TO_NIGHT_PROFILE);
		night.setVisibility(visible?View.VISIBLE:View.GONE);
		
		final Button day = (Button)myWindow.findViewById(R.id.navigation_day);
		day.setText(ZLResource.resource("menu").getResource(ActionCode.SWITCH_TO_DAY_PROFILE).getValue());
		visible = myFBReader.isActionVisible(ActionCode.SWITCH_TO_DAY_PROFILE) && myFBReader.isActionEnabled(ActionCode.SWITCH_TO_DAY_PROFILE);
		day.setVisibility(visible?View.VISIBLE:View.GONE);
		night.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SWITCH_TO_NIGHT_PROFILE);
				night.setVisibility(View.GONE);
				day.setVisibility(View.VISIBLE);
				
			}
		});
		day.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myFBReader.runAction(ActionCode.SWITCH_TO_DAY_PROFILE);
				day.setVisibility(View.GONE);
				night.setVisibility(View.VISIBLE);
			}
		});
		
		
		final SeekBar slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		final TextView text = (TextView)myWindow.findViewById(R.id.navigation_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLTextView view = myFBReader.getTextView();
				if (page == 1) {
					view.gotoHome();
				} else {
					view.gotoPage(page);
				}
				myFBReader.getViewWidget().reset();
				myFBReader.getViewWidget().repaint();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				myIsInProgress = true;
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myIsInProgress = false;
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int page = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1;
					gotoPage(page);
					text.setText(makeProgressText(page, pagesNumber));
				}
			}
		});

	}

	private void setupNavigation() {
		final SeekBar slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		final TextView text = (TextView)myWindow.findViewById(R.id.navigation_text);

		final ZLTextView textView = myFBReader.getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();

		if (slider.getMax() != pagePosition.Total - 1 || slider.getProgress() != pagePosition.Current - 1) {
			slider.setMax(pagePosition.Total - 1);
			slider.setProgress(pagePosition.Current - 1);
			text.setText(makeProgressText(pagePosition.Current, pagePosition.Total));
		}
	}

	private String makeProgressText(int page, int pagesNumber) {
		final StringBuilder builder = new StringBuilder();
		builder.append(page);
		builder.append("/");
		builder.append(pagesNumber);
		final TOCTree tocElement = myFBReader.getCurrentTOCElement();
		if (tocElement != null) {
			builder.append("  ");
			builder.append(tocElement.getText());
		}
		return builder.toString();
	}

	final void removeWindow(Activity activity) {
		if (myWindow != null && activity == myWindow.getContext()) {
			final ViewGroup root = (ViewGroup)myWindow.getParent();
			myWindow.hide();
			root.removeView(myWindow);
			myWindow = null;
		}
	}
}
