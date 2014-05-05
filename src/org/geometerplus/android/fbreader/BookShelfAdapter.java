package org.geometerplus.android.fbreader;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.ui.android.R;

public class BookShelfAdapter extends BaseAdapter {

	private static final String TAG = "BookCursorAdapter";

	public final class ViewHolder{  
        public ImageView bookCover;  
        public TextView bookTitle;  
        public class CoverSync implements Runnable {
        	ZLImageProxy mImage;
        	Book mBook;
        	public CoverSync(Book book, ZLImageProxy img) {
        		mImage = img;
        		mBook = book;
        	}
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!mImage.isSynchronized()) return;
				Bitmap bitmap = BookShelfCoverCache.convertZLImageToBitmap(mImage);
				mCache.putCache(mBook, bitmap);
				bookCover.setImageBitmap(bitmap);
			}
        	
        }
    }     

	private LayoutInflater mInflater;  
	private Context mContext;
	private List<Book> mBooks;

	private BookShelfCoverCache mCache = null;

	public BookShelfAdapter(Context context, List<Book> books, AndroidImageSynchronizer synchronizer) {
		super();
		mContext = context;
		mInflater = LayoutInflater.from(context);  
		mBooks = books;
		mCache = BookShelfCoverCache.getInstance(mContext, synchronizer);
		
	}

	@Override
	public int getCount() {
		if(mBooks == null) return 0;
		return mBooks.size();
	}

	@Override
	public Object getItem(int position) {
		if(mBooks == null) return null;
		return mBooks.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(mBooks == null) return -1;
		return mBooks.get(position).getId();
	}
	
	public void removeItem(Book book) {
		if(mBooks != null) {
			mBooks.remove(book);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.shelf_book, null);
			holder.bookCover = (ImageView) convertView.findViewById(R.id.book_cover);
			holder.bookTitle = (TextView) convertView.findViewById(R.id.book_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		Book book = mBooks.get(position);
		Bitmap drawable = mCache.get(book, holder);
		if (drawable != null) {
			holder.bookCover.setImageBitmap(drawable);
		} else {
			String path = book.getExtention().toLowerCase();

			if(path.endsWith("txt")) {
				holder.bookCover.setBackgroundResource(R.drawable.cover_txt);
			} else if(path.endsWith("epub")) {
				holder.bookCover.setBackgroundResource(R.drawable.cover_epub);
			} else {
				holder.bookCover.setBackgroundResource(R.drawable.cover_epub);
			}
		}
		
		String title = book.getTitle();
		holder.bookTitle.setText(title);

		return convertView;
	}

}
