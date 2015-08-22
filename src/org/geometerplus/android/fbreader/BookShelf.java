package org.geometerplus.android.fbreader;

import java.io.File;
import java.util.List;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.NetworkLibraryActivity;
import org.geometerplus.android.fbreader.network.NetworkLibraryPrimaryActivity;
import org.geometerplus.android.fbreader.preferences.PreferenceActivity;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.view.ShelvesView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author fan
 * 
 */
public class BookShelf extends Activity implements OnClickListener {

	private static final String TAG = "BookShelf";
	
	public static final String BOOK_PATH_KEY = "BookPath";
	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";

	private static final int DIALOG_CONFIRM = 0;
	private static final int DIALOG_OPTION = 1;

	private static final int OPTION_OPEN = 0;
	private static final int OPTION_DETAIL = 1;
	private static final int OPTION_DELETE = 2;
	// add by wenyd on 2011-12-21
	private TextView titleTextView;
	private Button rightButton;
	private ImageButton goNetStoreButton;
	private ShelvesView mShelevesView;
	private BookShelfAdapter mCursorAdapter;
	
	private long mBookId = -1;

	private BookOptionListener mOptionListener;
	final BookCollectionShadow mCollection = new BookCollectionShadow();
	public final AndroidImageSynchronizer ImageSynchronizer = new AndroidImageSynchronizer(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		setContentView(R.layout.shelf_activity);
		init();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	}

	private void init() {
		rightButton = (Button) findViewById(R.id.rightButton);
		titleTextView = (TextView) findViewById(R.id.title);
		goNetStoreButton = (ImageButton) findViewById(R.id.goNetStore);
		rightButton.setVisibility(View.GONE);
		titleTextView.setText(R.string.reader);
		goNetStoreButton.setOnClickListener(this);
		findViewById(R.id.goLocal).setOnClickListener(this);
		
		mCollection.bindToService(this, null);
		mShelevesView = (ShelvesView) findViewById(R.id.grid_shelves);
		mShelevesView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long id) {
				mBookId = id;
				mOptionListener = new BookOptionListener();
				showDialog(DIALOG_OPTION);
				return true;
			}

		});
		
		//mCursorAdapter.registerDataSetObserver(mObserver);		
		mShelevesView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, final long id) {
				
				
				mCollection.bindToService(BookShelf.this, new Runnable() {
					@Override
					public void run() {
						Book book = mCollection.getBookById(id);
						
						if (book != null) {
							String bookPath = book.getPath();
							boolean isExist = isExistOnSdcard(bookPath);
							if(isExist) {
								FBReader.openBookActivity(BookShelf.this, book, null);
							} else {
								Toast.makeText(BookShelf.this, R.string.no_exist_on_sdcard, Toast.LENGTH_SHORT).show();
								mCollection.removeBook(book, false);
								mCursorAdapter.removeItem(book);
								mCursorAdapter.notifyDataSetChanged();
							}
						}
					}
				});
				
			}

		});

		
	}

	private DataSetObserver mObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			super.onChanged();
		}

	};

	public boolean isExistOnSdcard(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	protected void onDestroy() {
		ImageSynchronizer.clear();
		mCollection.unbind();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onPause() {
		super.onPause();
		//mCursorAdapter.unregisterDataSetObserver(mObserver);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onResume() {
		super.onResume();
		mCollection.bindToService(this, new Runnable() {
			
			@Override
			public void run() {
				List<Book> books = mCollection.recentlyOpenedBooks();
				mCursorAdapter = new BookShelfAdapter(BookShelf.this, books, ImageSynchronizer);
				mShelevesView.setAdapter(mCursorAdapter);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(
				0,
				R.drawable.cover_ebk,
				0,
				ZLResource.resource("menu")
						.getResource(ActionCode.SHOW_PREFERENCES).getValue())
				.setIcon(R.drawable.popup_settings);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.drawable.cover_ebk:
			intent = new Intent(getApplicationContext(), PreferenceActivity.class);
			OrientationUtil.startActivityForResult(this, intent, FBReader.REQUEST_PREFERENCES);
			break;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CONFIRM:
			return new AlertDialog.Builder(BookShelf.this)
					.setTitle(R.string.quit)
					.setMessage(R.string.confirm_quit)
					.setPositiveButton(R.string.dialog_ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							})
					.setNegativeButton(R.string.dialog_cancel,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
		case DIALOG_OPTION:
			return new AlertDialog.Builder(BookShelf.this).setItems(
					R.array.dialog_option, mOptionListener).create();
		}
		return null;
	}

	private class DeleteBookDialog extends AlertDialog {
		private long id = -1;

		protected DeleteBookDialog(Context context, long bookId) {
			super(context);
			id = bookId;
		}

	}

	@Override
	public void onBackPressed() {
		showDialog(DIALOG_CONFIRM);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goNetStore:
			// Read
			startActivity(new Intent(getApplicationContext(),
					NetworkLibraryPrimaryActivity.class));
			break;
		case R.id.goLocal:
			startActivity(new Intent(getApplicationContext(), LibraryActivity.class));
		default:
			break;
		}
	}

	private class CancelListener implements DialogInterface.OnCancelListener {

		@Override
		public void onCancel(DialogInterface dialog) {
			dialog.dismiss();
		}

	}

	private void refreshBookList() {
		
	}
	
	private class BookOptionListener implements DialogInterface.OnClickListener {

		private long bookId = -1;
		private Book book = null;

		public BookOptionListener() {
			//bookId = id;
		}
		public void setBookId(long id) {
			bookId = id;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String path = null;
			switch (which) {
			case OPTION_OPEN:
				mCollection.bindToService(BookShelf.this, new Runnable() {
					@Override
					public void run() {
						Book book = mCollection.getRecentBook(0);
						if (book != null) {
							FBReader.openBookActivity(BookShelf.this, book, null);
						}
					}
				});
				
				break;
			case OPTION_DETAIL:
				mCollection.bindToService(BookShelf.this, new Runnable() {
					@Override
					public void run() {
						Book book = mCollection.getRecentBook(0);
						if (book != null) {
							final Intent intent =
									new Intent(BookShelf.this, BookInfoActivity.class)
										.putExtra(BookInfoActivity.FROM_READING_MODE_KEY, true);
								FBReaderIntents.putBookExtra(intent, book);
								OrientationUtil.startActivity(BookShelf.this, intent);
						}
					}
				});
				break;
			case OPTION_DELETE:
				mCollection.bindToService(BookShelf.this, new Runnable() {
					@Override
					public void run() {
						Book book = mCollection.getBookById(mBookId);
						
						if (book != null) {
							mCollection.removeBook(book, false);
							mCursorAdapter.removeItem(book);
							mCursorAdapter.notifyDataSetChanged();						}
					}
				});
				break;
			}
		}

	}

	/*@Override
	protected ZLApplication createApplication() {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "READER");
		}
		return new FBReaderApp();
	}

	@Override
	protected ZLFile fileFromIntent(Intent intent) {
		String filePath = intent.getStringExtra(BOOK_PATH_KEY);
		if (filePath == null) {
			final Uri data = intent.getData();
			if (data != null) {
				filePath = data.getPath();
			}
		}
		return filePath != null ? ZLFile.createFileByPath(filePath) : null;
	}

	@Override
	protected Runnable getPostponedInitAction() {
		return new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						//new TipRunner().start();
						DictionaryUtil.init(BookShelf.this);
					}
				});
			}
		};
	}*/

}
