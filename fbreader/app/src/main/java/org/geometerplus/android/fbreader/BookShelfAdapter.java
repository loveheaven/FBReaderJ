package org.geometerplus.android.fbreader;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import org.geometerplus.zlibrary.ui.android.drawable.CrossFadeDrawable;
import org.geometerplus.zlibrary.ui.android.drawable.FastBitmapDrawable;
import org.geometerplus.zlibrary.ui.android.view.VerticalTextView;

public class BookShelfAdapter extends BaseAdapter {

	private static final String TAG = "BookShelfAdapter";
	private static final int COVER_TRANSITION_DURATION = 175;  

	public final class ViewHolder{  
        public ImageView bookCover;  
        public VerticalTextView bookTitle;  
        public TextView bookType;  
        boolean isGettingCover = false;
        boolean hasCover = false;
        public class CoverSync implements Runnable {
        	ZLImageProxy mImage;
        	Book mBook;
        	public CoverSync(Book book, ZLImageProxy img) {
        		mImage = img;
        		mBook = book;
        	}
			@Override
			public void run() {
				if(!mImage.isSynchronized()) return;
				
				Bitmap bitmap = BookShelfCoverCache.convertZLImageToBitmap(mImage,mCoverWidth,mCoverHeight);
				if(bitmap == null) return;
				hasCover = true;
				isGettingCover = false;
				BitmapDrawable drawable = new BitmapDrawable(bitmap);
				mCache.putCache(mBook, drawable);
				bookTitle.setVisibility(View.GONE);
				bookType.setVisibility(View.GONE);
				
//				final CrossFadeDrawable transition = new CrossFadeDrawable(mDefaultCoverBitmap, bitmap);
//				transition.setCallback(bookCover);
//		        transition.setCrossFadeEnabled(true);
//		        transition.setBounds(0, 0, bookCover.getWidth(), bookCover.getHeight());
//              bookCover.setBackgroundDrawable(transition);
//              transition.startTransition(COVER_TRANSITION_DURATION);
				bookCover.setBackgroundDrawable(drawable);
			}
        	
        }
    }     

	private LayoutInflater mInflater;  
	private Context mContext;
	private List<Book> mBooks;
//	private Bitmap mDefaultCoverBitmap;

	private BookShelfCoverCache mCache = null;
	public int mCoverWidth;
	public int mCoverHeight;
	

	public BookShelfAdapter(Context context, List<Book> books, AndroidImageSynchronizer synchronizer) {
		super();
		mContext = context;
		mInflater = LayoutInflater.from(context);  
		mBooks = books;
		mCache = BookShelfCoverCache.getInstance(mContext, synchronizer);
		mCoverWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.BookShelf_CoverWidth);
		mCoverHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.BookShelf_CoverHeight);
//		mDefaultCoverBitmap = BitmapFactory.decodeResource(context.getResources(),
//                R.drawable.cover_epub);
		
	}
	
	public void refresh(List<Book> books) {
		mBooks = books;
		this.notifyDataSetChanged();
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
			convertView = mInflater.inflate(R.layout.shelf_book, parent, false);
			holder.bookCover = (ImageView) convertView.findViewById(R.id.book_cover);
			holder.bookTitle = (VerticalTextView) convertView.findViewById(R.id.book_title);
			holder.bookType = (TextView) convertView.findViewById(R.id.book_type);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		Book book = mBooks.get(position);
		BitmapDrawable drawable = mCache.get(book, holder);
		if (drawable != null) {
			holder.bookCover.setBackgroundDrawable(drawable);
			holder.bookTitle.setVisibility(View.GONE);
			holder.bookType.setVisibility(View.GONE);
		} else {
			String path = book.getExtention().toUpperCase();
			String title = book.getTitle();
			if(path.endsWith("GUJI")) {
				holder.bookCover.setBackgroundResource(R.drawable.cover_guji);
				holder.bookType.setVisibility(View.GONE);
				holder.bookTitle.setText(title, true);
			} else {
				holder.bookCover.setBackgroundResource(R.drawable.cover_default);
				holder.bookType.setText("ㄧ"+path+"ㄧ");
				holder.bookType.setVisibility(View.VISIBLE);
				holder.bookTitle.setText(title, false);
			}
			holder.bookTitle.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}

}
