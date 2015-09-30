package org.geometerplus.zlibrary.ui.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.GridView;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.drawable.SpotlightDrawable;
import org.geometerplus.zlibrary.ui.android.drawable.TransitionDrawable;

public class ShelvesView extends GridView {

	private Bitmap mShelfBackground;

	private Bitmap mShelfLeftLayer;
	private Bitmap mShelfRightLayer;
	private Bitmap mShelfDock;
	private int mShelfWidth;
	private int mShelfHeight;

	private Bitmap mWebLeft;
	private Bitmap mWebRight;
	private int mWebRightWidth;

	public ShelvesView(Context context) {
		super(context);
	}

	public ShelvesView(Context context, AttributeSet attrs) {
		super(context, attrs);
		load(context, attrs, 0);
		init(context);
	}

	public ShelvesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		load(context, attrs, defStyle);
		init(context);
	}

	private void load(Context context, AttributeSet attrs, int defStyle) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ShelvesView, defStyle, 0);

		final Resources resources = getResources();

		final int background = a.getResourceId(
				R.styleable.ShelvesView_shelfBackground, 0);
		final Bitmap shelfBackground = BitmapFactory.decodeResource(resources,
				background);
		mShelfLeftLayer = BitmapFactory.decodeResource(resources,
				a.getResourceId(R.styleable.ShelvesView_shelfLeftLayer, 0));
		mShelfRightLayer = BitmapFactory.decodeResource(resources,
				a.getResourceId(R.styleable.ShelvesView_shelfRightLayer, 0));
		mShelfDock = BitmapFactory.decodeResource(resources,
				a.getResourceId(R.styleable.ShelvesView_shelfDock, 0));
		if (shelfBackground != null) {
			mShelfWidth = shelfBackground.getWidth();
			mShelfHeight = shelfBackground.getHeight();
			//mShelfHeight = getResources().getDimensionPixelSize(R.dimen.BookHeight);
			mShelfBackground = shelfBackground;
		}
		
		mWebLeft = BitmapFactory.decodeResource(resources, R.drawable.web_left);

        final Bitmap webRight = BitmapFactory.decodeResource(resources, R.drawable.web_right);
        mWebRightWidth = webRight.getWidth();
        mWebRight = webRight;
        
		a.recycle();
	}

	private void init(Context context) {
		StateListDrawable drawable = new StateListDrawable();

        SpotlightDrawable start = new SpotlightDrawable(context, this);
        start.disableOffset();
        SpotlightDrawable end = new SpotlightDrawable(context, this, R.drawable.spotlight_blue);
        end.disableOffset();
        TransitionDrawable transition = new TransitionDrawable(start, end);
        drawable.addState(new int[] { android.R.attr.state_pressed },
                transition);

        final SpotlightDrawable normal = new SpotlightDrawable(context, this);
        drawable.addState(new int[] { }, normal);

        normal.setParent(drawable);
        transition.setParent(drawable);

        setSelector(drawable);
        setDrawSelectorOnTop(false);
        
        Resources r=getResources();

        mDefaultBookItemWidth =r.getDimensionPixelSize(R.dimen.BookShelf_ColumnWidth);

	}
	protected int mDefaultBookItemWidth;
	
//	@Override
//	protected void layoutChildren() {
//		super.layoutChildren();
//		final int count = getChildCount();
//		int leftWidth = mShelfLeftLayer.getWidth();
//		int rightWidth = mShelfRightLayer.getWidth();
//		final int top = count > 0 ? getChildAt(0).getTop() : 0;
//		final int width = getWidth();
//		int column = (width-leftWidth-rightWidth)/mDefaultBookItemWidth;
//		int columnSpace = ((width-leftWidth-rightWidth) - (column*mDefaultBookItemWidth))/(column+1);
//		
//		android.util.Log.v("BOOKSHELF", top + ";" + column + ";" + count);
//		
//		for(int i = 0; i< count; i++) {
//			int row = i/column;
//			int col = i%column;
//			View child = this.getChildAt(i);
//			child.layout(leftWidth+columnSpace + col* (mDefaultBookItemWidth+columnSpace), 
//					top+row*mShelfHeight, 
//					leftWidth+columnSpace + col* (mDefaultBookItemWidth+columnSpace)+mDefaultBookItemWidth, 
//					top+row*mShelfHeight+mShelfHeight);
//		}
//	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		final int count = getChildCount();
		
		final int top = count > 0 ? getChildAt(0).getTop() : 0;
		final int shelfWidth = mShelfWidth;
		final int shelfHeight = mShelfHeight;
		final int width = getWidth();
		final int height = getHeight();
		final Bitmap background = mShelfBackground;
		//android.util.Log.v("BOOKSHELF", "dispatchDraw:"+ top  + ";" + count);

		int leftWidth = (mShelfLeftLayer == null)? 0 : mShelfLeftLayer.getWidth();
		int rightWidth = (mShelfRightLayer == null)? 0 :mShelfRightLayer.getWidth();
		
		int height1 = (height / shelfHeight + 1) * shelfHeight + top;
		for (int y = top; y <= height1; y += shelfHeight) {
			if(mShelfLeftLayer  != null) {
				canvas.drawBitmap(mShelfLeftLayer, 0, y, null);
			}
			for (int x = leftWidth; x < width-rightWidth; x += shelfWidth) {
				// 仿照IReader书架效果
				canvas.drawBitmap(background, x, y, null);
			}
			if(mShelfRightLayer != null) {
				canvas.drawBitmap(mShelfRightLayer, width - rightWidth, y,
					null);
			}
			if(mShelfDock != null) {
				for (int x = 0; x < width-rightWidth; x += shelfWidth) {
					canvas.drawBitmap(mShelfDock, 0, y + shelfHeight - mShelfDock.getHeight(), null);
				}
			}
			
			
		}
		if (count == 0) {
            canvas.drawBitmap(mWebLeft, 0.0f, top + 1, null);
            canvas.drawBitmap(mWebRight, width - mWebRightWidth, (height / shelfHeight-1) * shelfHeight + top - 1, null);
        }
		
		/*
		 * for (int x = 0; x < width; x += shelfWidth) { for (int y = top; y <=
		 * height1; y += shelfHeight) { //仿照IReader书架效果 //
		 * canvas.drawBitmap(mShelfLeftLayer,x,y, null); //
		 * canvas.drawBitmap(background, x+leftWidth, y, null); //
		 * canvas.drawBitmap(mShelfRightLayer,width-rightWidth,y, null);
		 * canvas.drawBitmap(background, x,y, null); } }
		 */
		
		super.dispatchDraw(canvas);
		
	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);

		final Drawable current = getSelector().getCurrent();
		if (current instanceof TransitionDrawable) {
			if (pressed) {
				((TransitionDrawable) current)
						.startTransition(ViewConfiguration
								.getLongPressTimeout());
			} else {
				((TransitionDrawable) current).resetTransition();
			}
		}
	}
}
