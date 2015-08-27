package org.geometerplus.android.fbreader;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.geometerplus.android.fbreader.BookShelfAdapter.ViewHolder;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class BookShelfCoverCache {
	private HashMap<Long, SoftReference<BitmapDrawable>> imageCache = new HashMap<Long, SoftReference<BitmapDrawable>>();
	
	private static BookShelfCoverCache mInstance = null;
	private Context mContext;
	private AndroidImageSynchronizer mSynchronizer;

	private BookShelfCoverCache(Context context, AndroidImageSynchronizer synchronizer) {
		mContext = context;
		mSynchronizer = synchronizer;
	}

	public static BookShelfCoverCache getInstance(Context context, AndroidImageSynchronizer synchronizer) {
		if (mInstance == null) {
			mInstance = new BookShelfCoverCache(context, synchronizer);
		}
		return mInstance;
	}

	public BitmapDrawable get(Book book, ViewHolder holder) {
		long bookId = book.getId();
		
		if (imageCache.containsKey(bookId)) {
			BitmapDrawable drawable = imageCache.get(bookId).get();
			if (drawable != null && drawable.getBitmap() != null) {
				return drawable;
			} else if(holder.hasCover){
				return createThumbnailDrawable(book, holder);
			}
		} else {
			return createThumbnailDrawable(book, holder);
		}
		return null;
	}
	
	public static Bitmap convertZLImageToBitmap(ZLImage image) {
		final ZLAndroidImageData data =
				((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
		if(data == null) return null;
		
		Bitmap bitmap = data.getBitmap(150, 150);
		return bitmap;
	}
	
	public static Bitmap convertZLImageToBitmap(ZLImage image, int maxWidth, int maxHeight) {
		final ZLAndroidImageData data =
				((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
		if(data == null) return null;
		
		Bitmap bitmap = data.getBitmap(maxWidth, maxHeight);
		return bitmap;
	}
	
	public void putCache(Book book, BitmapDrawable image) {
		imageCache.put(book.getId(), new SoftReference<BitmapDrawable>(image));
	}

	/**
	 * 为图书封面生成缩略图
	 * @param data
	 * @return
	 */
	private  BitmapDrawable createThumbnailDrawable(Book book, ViewHolder holder) {
		try {
			final ZLImage image = CoverUtil.getCover(book, PluginCollection.Instance(Paths.systemInfo(mContext)));
			Bitmap bitmap = null;
			if (image instanceof ZLImageProxy) {
				final ZLImageProxy img = (ZLImageProxy)image;
				if (!img.isSynchronized() && !holder.isGettingCover) {
					holder.isGettingCover = true;
					img.startSynchronization(mSynchronizer,
							holder.new CoverSync(book, img)
					);
					return null;
				}
				return null;
			}
			bitmap = convertZLImageToBitmap(image);
			if(image == null) return null;
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			putCache(book, drawable);
			return drawable;
		} catch (Exception e) {
			return null;
		}
	}
}
