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

package org.geometerplus.zlibrary.ui.android.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jopencc.util.ChineseConvertor;

import org.fbreader.util.Boolean3;
import org.geometerplus.fbreader.fbreader.options.ViewOptions.GujiPunctuationEnum;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.util.SystemInfo;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import com.jni.bitmap_operations.JniBitmapHolder;

import org.geometerplus.zlibrary.ui.android.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public final class ZLAndroidPaintContext extends ZLPaintContext {
	public static ZLBooleanOption AntiAliasOption = new ZLBooleanOption("Fonts", "AntiAlias", true);
	public static ZLBooleanOption DeviceKerningOption = new ZLBooleanOption("Fonts", "DeviceKerning", false);
	public static ZLBooleanOption DitheringOption = new ZLBooleanOption("Fonts", "Dithering", false);
	public static ZLBooleanOption SubpixelOption = new ZLBooleanOption("Fonts", "Subpixel", false);
    private final LinearGradient mLinearGradient = new LinearGradient(0, 0, 50, 50,
            new int[] { 0xFF403F35, 0xC455504A, 0xFF403F35 },
            new float[]{0, 0.5f, 1.0f}, Shader.TileMode.MIRROR);

	private final Canvas myCanvas;
	private final Paint myTextPaint = new Paint();
	private final Paint myLinePaint = new Paint();
	private final Paint myFillPaint = new Paint();
	private final Paint myOutlinePaint = new Paint();
    private Random random;

	public static final class Geometry {
		final Size ScreenSize;
		final Size AreaSize;
		final int LeftMargin;
		final int TopMargin;

		public Geometry(int screenWidth, int screenHeight, int width, int height, int leftMargin, int topMargin) {
			ScreenSize = new Size(screenWidth, screenHeight);
			AreaSize = new Size(width, height);
			LeftMargin = leftMargin;
			TopMargin = topMargin;
		}
	}

	private final Geometry myGeometry;
	private final int myScrollbarWidth;

	private ZLColor myBackgroundColor = new ZLColor(0, 0, 0);

	public ZLAndroidPaintContext(SystemInfo systemInfo, Canvas canvas, Geometry geometry, int scrollbarWidth, boolean isGuji) {
		super(systemInfo);

		mIsGuji = isGuji;
		myCanvas = canvas;
		myGeometry = geometry;
		myScrollbarWidth = scrollbarWidth;

		myTextPaint.setLinearText(false);
		myTextPaint.setAntiAlias(AntiAliasOption.getValue());
		if (DeviceKerningOption.getValue()) {
			myTextPaint.setFlags(myTextPaint.getFlags() | Paint.DEV_KERN_TEXT_FLAG);
		} else {
			myTextPaint.setFlags(myTextPaint.getFlags() & ~Paint.DEV_KERN_TEXT_FLAG);
		}
		myTextPaint.setDither(DitheringOption.getValue());
		myTextPaint.setSubpixelText(SubpixelOption.getValue());
		//myTextPaint.setMaskFilter(new EmbossMaskFilter(new float[] {1, 1, 1}, .4f, 6f, 3.5f));
		//myTextPaint.setPathEffect(new DiscretePathEffect(6.0F, 2.0F));

//		Drawable d = ZLibrary.Instance().getResources().getDrawable(R.drawable.border);
//		Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable)d).getBitmap();
//		BitmapShader mBitmapShader = new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT); 
		myLinePaint.setStyle(Paint.Style.STROKE);
//		myLinePaint.setShader(mBitmapShader);
        random = new Random(1L);

        //myTextPaint.setShader(linearGradient);
        //myLinePaint.setShader(linearGradient);
		myFillPaint.setAntiAlias(AntiAliasOption.getValue());

		myOutlinePaint.setAntiAlias(true);
		myOutlinePaint.setDither(true);
		myOutlinePaint.setStrokeWidth(4);
		myOutlinePaint.setStyle(Paint.Style.STROKE);
		myOutlinePaint.setPathEffect(new CornerPathEffect(5));
		myOutlinePaint.setMaskFilter(new EmbossMaskFilter(new float[] {1, 1, 1}, .4f, 6f, 3.5f));
	}

	private static ZLFile ourWallpaperFile;
	private static JniBitmapHolder ourWallpaper;
	private static ZLFile ourWallpaperFileOdd;
	private static JniBitmapHolder ourWallpaperOdd;
	private static ZLFile ourWallpaperFileEven;
	private static JniBitmapHolder ourWallpaperEven;
	private static FillMode ourFillMode;
	
	@Override
	public void clear(ZLFile wallpaperFile, FillMode mode) {
		if (!wallpaperFile.equals(ourWallpaperFile) || mode != ourFillMode) {
			ourWallpaperFile = wallpaperFile;
			ourFillMode = mode;
			if(ourWallpaper != null) {
				ourWallpaper.freeBitmap();
			}
			ourWallpaper = null;
			try {
				final Bitmap fileBitmap =
					BitmapFactory.decodeStream(wallpaperFile.getInputStream());
				switch (mode) {
					default:
						ourWallpaper = new JniBitmapHolder(fileBitmap);
						fileBitmap.recycle();
						break;
					case tileMirror:
					{
						final int w = fileBitmap.getWidth();
						final int h = fileBitmap.getHeight();
						final Bitmap wallpaper = Bitmap.createBitmap(2 * w, 2 * h, fileBitmap.getConfig());
						final Canvas wallpaperCanvas = new Canvas(wallpaper);
						final Paint wallpaperPaint = new Paint();

						Matrix m = new Matrix();
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						m.preScale(-1, 1);
						m.postTranslate(2 * w, 0);
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						m.preScale(1, -1);
						m.postTranslate(0, 2 * h);
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						m.preScale(-1, 1);
						m.postTranslate(- 2 * w, 0);
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						ourWallpaper = new JniBitmapHolder(wallpaper);
						wallpaper.recycle();
						fileBitmap.recycle();
						break;
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (ourWallpaper != null) {
			myBackgroundColor = ZLAndroidColorUtil.getAverageColor(ourWallpaper);
			final int w = ourWallpaper.getWidth();
			final int h = ourWallpaper.getHeight();
			final Geometry g = myGeometry;
			Bitmap fileBitmap = ourWallpaper.getBitmap();
			switch (mode) {
				case fullscreen:
				{
					final Matrix m = new Matrix();
					m.preScale(1f * g.ScreenSize.Width / w, 1f * g.ScreenSize.Height / h);
					m.postTranslate(-g.LeftMargin, -g.TopMargin);
					myCanvas.drawBitmap(fileBitmap, m, myFillPaint);
					break;
				}
				case stretch:
				{
					final Matrix m = new Matrix();
					final float sw = 1f * g.ScreenSize.Width / w;
					final float sh = 1f * g.ScreenSize.Height / h;
					final float scale;
					float dx = g.LeftMargin;
					float dy = g.TopMargin;
					if (sw < sh) {
						scale = sh;
						dx += (scale * w - g.ScreenSize.Width) / 2;
					} else {
						scale = sw;
						dy += (scale * h - g.ScreenSize.Height) / 2;
					}
					m.preScale(scale, scale);
					m.postTranslate(-dx, -dy);
					myCanvas.drawBitmap(fileBitmap, m, myFillPaint);
					break;
				}
				case tileVertically:
				{
					final Matrix m = new Matrix();
					final int dx = g.LeftMargin;
					final int dy = g.TopMargin % h;
					m.preScale(1f * g.ScreenSize.Width / w, 1);
					m.postTranslate(-dx, -dy);
					for (int ch = g.AreaSize.Height + dy; ch > 0; ch -= h) {
						myCanvas.drawBitmap(fileBitmap, m, myFillPaint);
						m.postTranslate(0, h);
					}
					break;
				}
				case tileHorizontally:
				{
					final Matrix m = new Matrix();
					final int dx = g.LeftMargin % w;
					final int dy = g.TopMargin;
					m.preScale(1, 1f * g.ScreenSize.Height / h);
					m.postTranslate(-dx, -dy);
					for (int cw = g.AreaSize.Width + dx; cw > 0; cw -= w) {
						myCanvas.drawBitmap(fileBitmap, m, myFillPaint);
						m.postTranslate(w, 0);
					}
					break;
				}
				case tile:
				case tileMirror:
				{
					final int dx = g.LeftMargin % w;
					final int dy = g.TopMargin % h;
					final int fullw = g.AreaSize.Width + dx;
					final int fullh = g.AreaSize.Height + dy;
					for (int cw = 0; cw < fullw; cw += w) {
						for (int ch = 0; ch < fullh; ch += h) {
							try {
								myCanvas.drawBitmap(fileBitmap, cw - dx, ch - dy,
										myFillPaint);
							} catch (NullPointerException ex) {
	
							}
						}
					}
					break;
				}
			}
			if(fileBitmap != null) {
				fileBitmap.recycle();
			}
		} else {
			clear(new ZLColor(128, 128, 128));
		}
	}
	
	@Override
	public void clear(ZLFile wallpaperFileOdd, ZLFile wallpaperFileEven, FillMode mode) {
		boolean change = !wallpaperFileOdd.equals(ourWallpaperFileOdd) || !wallpaperFileEven.equals(ourWallpaperFileEven);
		if (change || mode != ourFillMode) {
			ourWallpaperFileOdd = wallpaperFileOdd;
			if(ourWallpaperOdd!=null) {
				ourWallpaperOdd.freeBitmap();
			}
			ourWallpaperOdd = null;
			ourWallpaperFileEven = wallpaperFileEven;
			if(ourWallpaperEven!=null) {
				ourWallpaperEven.freeBitmap();
			}
			ourWallpaperEven = null;
			ourFillMode = mode;
			
			try {
				final Bitmap fileBitmapOdd =
					BitmapFactory.decodeStream(wallpaperFileOdd.getInputStream());
				final Bitmap fileBitmapEven =
						BitmapFactory.decodeStream(wallpaperFileEven.getInputStream());
				switch (mode) {
					case fullscreen:
					default:
						ourWallpaperOdd = new JniBitmapHolder(fileBitmapOdd);
						ourWallpaperEven = new JniBitmapHolder(fileBitmapEven);
						break;
				}
				fileBitmapOdd.recycle();
				fileBitmapEven.recycle();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (ourWallpaperOdd != null && ourWallpaperEven != null) {
			myBackgroundColor = ZLAndroidColorUtil.getAverageColor(ourWallpaperOdd);
			final int w = ourWallpaperOdd.getWidth();
			final int h = ourWallpaperOdd.getHeight();
			final Geometry g = myGeometry;
			
			final int dx = g.LeftMargin % w;
			final int dy = g.TopMargin % h;
			final int fullw = g.AreaSize.Width + dx;
			final int fullh = g.AreaSize.Height + dy;
			
			Bitmap fileBitmapOdd = ourWallpaperOdd.getBitmap();
			myCanvas.drawBitmap(fileBitmapOdd, fullw/2 - w, 0, myFillPaint);
			fileBitmapOdd.recycle();
			Bitmap fileBitmapEven = ourWallpaperEven.getBitmap();
			myCanvas.drawBitmap(fileBitmapEven, fullw/2 , 0, myFillPaint);
			fileBitmapEven.recycle();
					
		} else {
			clear(new ZLColor(128, 128, 128));
		}
	}
	
	@Override
	public void clear(ZLColor color) {
		myBackgroundColor = color;
		myFillPaint.setColor(ZLAndroidColorUtil.rgb(color));
		myCanvas.drawRect(0, 0, myGeometry.AreaSize.Width, myGeometry.AreaSize.Height, myFillPaint);
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myBackgroundColor;
	}

    @Override
    public void drawShadow(int left, int top, int right, int bottom, boolean isLeftToRight) {
        final GradientDrawable.Orientation orientation = !isLeftToRight
                ? GradientDrawable.Orientation.RIGHT_LEFT
                : GradientDrawable.Orientation.LEFT_RIGHT;
        final int[] colors = new int[] { 0xA0202020, 0x00d8b389 };//x00FFFFFF
        final GradientDrawable gradient = new GradientDrawable(orientation, colors);
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        //gradient.setDither(true);
        gradient.setBounds(left, top, right, bottom);
        gradient.draw(myCanvas);
    }

	public void drawPolygonalLine(int[] xs, int[] ys) {

        HashMap<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
        List<Integer> retX = new ArrayList<Integer>();
        List<Integer> retY = new ArrayList<Integer>();
        List<Integer> l = new ArrayList<Integer>();
        l.add(ys[0]);
        map.put(xs[0], l);
        for (int i = 1; i< xs.length; i++) {
            if (xs[i] != xs[i-1]) {
                l = map.get(new Integer(xs[i-1]));
                int[] a = new int[l.size()];
                for(int j = 0; j< l.size(); j++) {
                    a[j] = l.get(j);
                }
                //Arrays.sort(a);
                retX.add(xs[i-1]);
                retY.add(a[0]);
                if(a.length > 1) {
                    retX.add(xs[i - 1]);
                    retY.add(a[l.size() - 1]);
                }
                l = new ArrayList<Integer>();
                l.add(ys[i]);
                map.put(xs[i], l);
            } else {
                map.get(new Integer(xs[i])).add(new Integer(ys[i]));
            }
        }
        l = map.get(new Integer(xs[xs.length-1]));
        int[] a = new int[l.size()];
        for(int j = 0; j< l.size(); j++) {
            a[j] = l.get(j);
        }
        //Arrays.sort(a);
        retX.add(xs[xs.length-1]);
        retY.add(a[0]);
        if(a.length > 1) {
            retX.add(xs[xs.length - 1]);
            retY.add(a[l.size() - 1]);
        }

        xs = new int[retX.size()];
        int j = 0;
        for(Integer x: retX) {
            xs[j] = x;
            j++;
        }
        ys = new int[retY.size()];
        j=0;
        for(Integer x:retY) {
            ys[j] = x;
            j++;
        }

        if(xs.length <= 6) {
            drawLine(xs[0],ys[0], xs[1], ys[1]);//425,122,425,1122
            drawLine(xs[1],ys[1], xs[2], ys[2]);//425,1122,1720,1122
            drawLine(xs[2],ys[2], xs[3], ys[3]);//1720,1122,1720,108
            drawLine(xs[3],ys[3], xs[4], ys[4]);//1720,108, 425, 108
            if(xs.length == 5) {
                drawLine(xs[4], ys[4], xs[0], ys[0]);//1720,108, 425, 108
            } else {
                drawLine(xs[4], ys[4], xs[5], ys[5]);//1720,108, 425, 108
            }
            return;
        }

		final Path path = new Path();
		final int last = xs.length - 1;
		path.moveTo(xs[last], ys[last]);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		myCanvas.drawPath(path, myLinePaint);
	}

	public void drawOutline(int[] xs, int[] ys) {
		final int last = xs.length - 1;
		int xStart = (xs[0] + xs[last]) / 2;
		int yStart = (ys[0] + ys[last]) / 2;
		int xEnd = xStart;
		int yEnd = yStart;
		if (xs[0] != xs[last]) {
			if (xs[0] > xs[last]) {
				xStart -= 5;
				xEnd += 5;
			} else {
				xStart += 5;
				xEnd -= 5;
			}
		} else {
			if (ys[0] > ys[last]) {
				yStart -= 5;
				yEnd += 5;
			} else {
				yStart += 5;
				yEnd -= 5;
			}
		}

		final Path path = new Path();
		path.moveTo(xStart, yStart);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		path.lineTo(xEnd, yEnd);
		myCanvas.drawPath(path, myOutlinePaint);
	}

	@Override
	protected void setFontInternal(List<FontEntry> entries, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrought) {
		Typeface typeface = null;
		for (FontEntry e : entries) {
			typeface = AndroidFontUtil.typeface(getSystemInfo(), e, bold, italic);
			if (typeface != null) {
				break;
			}
		}
		myTextPaint.setTypeface(typeface);
		myTextPaint.setTextSize(size);
		myTextPaint.setUnderlineText(underline);
		myTextPaint.setStrikeThruText(strikeThrought);
	}

	@Override
	public void setTextColor(ZLColor color) {
		if (color != null) {
			myTextPaint.setColor(ZLAndroidColorUtil.rgb(color));
		}
	}
	
	@Override
	public void setTextSize(float textSize) {
		if (textSize > 0) {
			myTextPaint.setTextSize(textSize);
			myStringHeight = -1;
		}
	}

	@Override
	public void setLineColor(ZLColor color) {
		if (color != null) {
			myLinePaint.setColor(ZLAndroidColorUtil.rgb(color));
			myOutlinePaint.setColor(ZLAndroidColorUtil.rgb(color));
		}
	}

	@Override
	public void setLineWidth(int width) {
		myLinePaint.setStrokeWidth(width);
	}
	
	@Override
	public void setLineEffect(PathEffect effect, MaskFilter filter) {
		myLinePaint.setPathEffect(effect);
		myLinePaint.setMaskFilter(filter);
	}

	@Override
	public void setFillColor(ZLColor color, int alpha) {
		if (color != null) {
			myFillPaint.setColor(ZLAndroidColorUtil.rgba(color, alpha));
		}
	}

    @Override
    public void setFillShader(boolean gradient) {
        if(gradient) {
            myFillPaint.setShader(mLinearGradient);
        } else {
            myFillPaint.setShader(null);
        }
    }

	protected int getWidth() {
		return myGeometry.AreaSize.Width - myScrollbarWidth;
	}

	protected int getHeight() {
		return myGeometry.AreaSize.Height;
	}
	
	public static boolean isDigit(int codePoint) {
        // Optimized case for ASCII
        if ('0' <= codePoint && codePoint <= '9') {
            return true;
        } else if(codePoint == '（' || codePoint == '）') {
        	return true;
        } else if(codePoint == '《' || codePoint == '》') {
        	return true;
        }  else if(codePoint == '【' || codePoint == '】') {
        	return true;
        }
        return false;
    }
	
	private static final byte KANA_COMBINE_VOICED[] = new byte[] { 
		0, //ウ
		0, 0, 0, 0, 1, 0, 1, //キ
		0, 1, 0, 1, 0, 1, 0, //ゴ
		1, 0, 1, 0, 1, 0, 1, //セ
		0, 1, 0, 1, 0, 1, 0, //ヂ
		0, 1, 0, 1, 0, 1, 0, //ド
		0, 0, 0, 0, 0, 1, 0, //バ
		0, 1, 0, 0, 1, 0, 0, //プ
		1, 0, 0, 1, 0, 0, 0, //マ
		0, 0, 0, 0, 0, 0, 0, //ュ
		0, 0, 0, 0, 0, 0, 0, //レ
		0, 0, 8, 8, 8, 8, 0, //ン
		0, 0, 0, 0, 0, 0, 0, //ヺ
		0, 0, 1 };//ヽ
	
	private static final byte KANA_COMBINE_HALF_VOICED[] = new byte[] { 
		0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 2, 0,
		0, 2, 0, 0, 2, 0, 0, 
		2, 0, 0, 2, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0 };
	
	public static boolean isCombiningChar(char ch)
	{
		if (ch < 0x0300) return false;
		if ((ch >= 0x0300) && (ch <= 0x0345)) return true;
		if ((ch >= 0x0360) && (ch <= 0x0361)) return true;
		if ((ch >= 0x0483) && (ch <= 0x0486)) return true;
		if ((ch >= 0x0591) && (ch <= 0x05a1)) return true;
		if ((ch >= 0x05a3) && (ch <= 0x05b9)) return true;
		if ((ch >= 0x05bb) && (ch <= 0x05bd)) return true;
		if (ch == 0x05bf) return true;
		if ((ch >= 0x05c1) && (ch <= 0x05c2)) return true;
		if (ch == 0x05c4) return true;
		if ((ch >= 0x064b) && (ch <= 0x0652)) return true;
		if (ch == 0x0670) return true;
		if ((ch >= 0x06d6) && (ch <= 0x06dc)) return true;
		if ((ch >= 0x06dd) && (ch <= 0x06df)) return true;
		if ((ch >= 0x06e0) && (ch <= 0x06e4)) return true;
		if ((ch >= 0x06e7) && (ch <= 0x06e8)) return true;
		if ((ch >= 0x06ea) && (ch <= 0x06ed)) return true;
		if ((ch >= 0x0901) && (ch <= 0x0903)) return true;
		if (ch == 0x093c) return true;
		if ((ch >= 0x093e) && (ch <= 0x094c)) return true;
		if (ch == 0x094d) return true;
		if ((ch >= 0x0951) && (ch <= 0x0954)) return true;
		if ((ch >= 0x0962) && (ch <= 0x0963)) return true;
		if ((ch >= 0x0981) && (ch <= 0x0983)) return true;
		if (ch == 0x09bc) return true;
		if (ch == 0x09be) return true;
		if (ch == 0x09bf) return true;
		if ((ch >= 0x09c0) && (ch <= 0x09c4)) return true;
		if ((ch >= 0x09c7) && (ch <= 0x09c8)) return true;
		if ((ch >= 0x09cb) && (ch <= 0x09cd)) return true;
		if (ch == 0x09d7) return true;
		if ((ch >= 0x09e2) && (ch <= 0x09e3)) return true;
		if (ch == 0x0a02) return true;
		if (ch == 0x0a3c) return true;
		if (ch == 0x0a3e) return true;
		if (ch == 0x0a3f) return true;
		if ((ch >= 0x0a40) && (ch <= 0x0a42)) return true;
		if ((ch >= 0x0a47) && (ch <= 0x0a48)) return true;
		if ((ch >= 0x0a4b) && (ch <= 0x0a4d)) return true;
		if ((ch >= 0x0a70) && (ch <= 0x0a71)) return true;
		if ((ch >= 0x0a81) && (ch <= 0x0a83)) return true;
		if (ch == 0x0abc) return true;
		if ((ch >= 0x0abe) && (ch <= 0x0ac5)) return true;
		if ((ch >= 0x0ac7) && (ch <= 0x0ac9)) return true;
		if ((ch >= 0x0acb) && (ch <= 0x0acd)) return true;
		if ((ch >= 0x0b01) && (ch <= 0x0b03)) return true;
		if (ch == 0x0b3c) return true;
		if ((ch >= 0x0b3e) && (ch <= 0x0b43)) return true;
		if ((ch >= 0x0b47) && (ch <= 0x0b48)) return true;
		if ((ch >= 0x0b4b) && (ch <= 0x0b4d)) return true;
		if ((ch >= 0x0b56) && (ch <= 0x0b57)) return true;
		if ((ch >= 0x0b82) && (ch <= 0x0b83)) return true;
		if ((ch >= 0x0bbe) && (ch <= 0x0bc2)) return true;
		if ((ch >= 0x0bc6) && (ch <= 0x0bc8)) return true;
		if ((ch >= 0x0bca) && (ch <= 0x0bcd)) return true;
		if (ch == 0x0bd7) return true;
		if ((ch >= 0x0c01) && (ch <= 0x0c03)) return true;
		if ((ch >= 0x0c3e) && (ch <= 0x0c44)) return true;
		if ((ch >= 0x0c46) && (ch <= 0x0c48)) return true;
		if ((ch >= 0x0c4a) && (ch <= 0x0c4d)) return true;
		if ((ch >= 0x0c55) && (ch <= 0x0c56)) return true;
		if ((ch >= 0x0c82) && (ch <= 0x0c83)) return true;
		if ((ch >= 0x0cbe) && (ch <= 0x0cc4)) return true;
		if ((ch >= 0x0cc6) && (ch <= 0x0cc8)) return true;
		if ((ch >= 0x0cca) && (ch <= 0x0ccd)) return true;
		if ((ch >= 0x0cd5) && (ch <= 0x0cd6)) return true;
		if ((ch >= 0x0d02) && (ch <= 0x0d03)) return true;
		if ((ch >= 0x0d3e) && (ch <= 0x0d43)) return true;
		if ((ch >= 0x0d46) && (ch <= 0x0d48)) return true;
		if ((ch >= 0x0d4a) && (ch <= 0x0d4d)) return true;
		if (ch == 0x0d57) return true;
		if (ch == 0x0e31) return true;
		if ((ch >= 0x0e34) && (ch <= 0x0e3a)) return true;
		if ((ch >= 0x0e47) && (ch <= 0x0e4e)) return true;
		if (ch == 0x0eb1) return true;
		if ((ch >= 0x0eb4) && (ch <= 0x0eb9)) return true;
		if ((ch >= 0x0ebb) && (ch <= 0x0ebc)) return true;
		if ((ch >= 0x0ec8) && (ch <= 0x0ecd)) return true;
		if ((ch >= 0x0f18) && (ch <= 0x0f19)) return true;
		if (ch == 0x0f35) return true;
		if (ch == 0x0f37) return true;
		if (ch == 0x0f39) return true;
		if (ch == 0x0f3e) return true;
		if (ch == 0x0f3f) return true;
		if ((ch >= 0x0f71) && (ch <= 0x0f84)) return true;
		if ((ch >= 0x0f86) && (ch <= 0x0f8b)) return true;
		if ((ch >= 0x0f90) && (ch <= 0x0f95)) return true;
		if (ch == 0x0f97) return true;
		if ((ch >= 0x0f99) && (ch <= 0x0fad)) return true;
		if ((ch >= 0x0fb1) && (ch <= 0x0fb7)) return true;
		if (ch == 0x0fb9) return true;
		if ((ch >= 0x20d0) && (ch <= 0x20dc)) return true;
		if (ch == 0x20e1) return true;
		if ((ch >= 0x302a) && (ch <= 0x302f)) return true;
		if (ch == 0x3099) return true;
		if (ch == 0x309a) return true;

		return false;
	}
	
	public static String combineChartoString(String str) {
		if(TextUtils.isEmpty(str)) {
			return str;
		}
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if( i < str.length() - 1) {
				if(isCombiningChar(str.charAt(i+1))) {
					if (ch >= 0x30A6 && ch <= 0x30FD) {
						ch += (str.charAt(i+1) == 0x3099) ? KANA_COMBINE_VOICED[ch - 0x30A6] : KANA_COMBINE_HALF_VOICED[ch - 0x30A6];
						i+=1;
					} else if((ch >= 0x3046 && ch <= 0x309D)) {
						ch += (str.charAt(i+1) == 0x3099) ? KANA_COMBINE_VOICED[ch - 0x3046] : KANA_COMBINE_HALF_VOICED[ch - 0x3046];
						i+=1;
					}
				}
			}
			ret.append(ch);
		}
		return ret.toString();
	}
	
	public int measureText(char[] string, int offset, int length) {
		int start = offset;
		float stringWidth = 0;
		for(int i = offset;i < offset + length; i++) {
			if(Character.isHighSurrogate(string[i])) {
				if(i != start) {
					stringWidth += mIsGuji? (i-start)*getStringHeight():myTextPaint.measureText(new String(string, start, i-start));
				}
				Typeface previosType = myTextPaint.getTypeface();
				myTextPaint.setTypeface(AndroidFontUtil.systemTypeface("TW-Kai-Ext-B", previosType.isBold(), previosType.isItalic()));
				stringWidth += myTextPaint.measureText(new String(string, i, 2));
				myTextPaint.setTypeface(previosType);
				i++;
				start = i+1;
			} else if(mIsGuji && myIsShowGujiPunctuation!=GujiPunctuationEnum.show && isCharPunctuation(string[i]) ) {
				if(i != start) {
					stringWidth += (i-start)*getStringHeight();//myTextPaint.measureText(new String(string, start, i-start));
				}
				start = i + 1;
			} else if(mIsGuji && isDigit(string[i]) ) {
				if(i != start) {
					stringWidth += (i-start)*getStringHeight();//myTextPaint.measureText(new String(string, start, i-start));
				}
				stringWidth += getStringHeight();
				start = i + 1;
			}
		}
		if(start < offset+length) {
			stringWidth += mIsGuji?(offset+length - start)*getStringHeight() : 
				myTextPaint.measureText(new String(string, start, offset+length - start));
		}
		return (int)(stringWidth + 0.5f);
	}
	
	@Override
	public int getStringWidth(char[] string, int offset, int length) {
		boolean containsSoftHyphen = false;
		for (int i = offset; i < offset + length; ++i) {
			if (string[i] == (char)0xAD) {
				containsSoftHyphen = true;
				break;
			}
		}
		if (!containsSoftHyphen) {
			
			return measureText(string, offset, length);
			//return (int)(myTextPaint.measureText(new String(string, offset, length)) + 0.5f);
		} else {
			final char[] corrected = new char[length];
			int len = 0;
			for (int o = offset; o < offset + length; ++o) {
				final char chr = string[o];
				if (chr != (char)0xAD) {
					corrected[len++] = chr;
				}
			}
			return measureText(corrected, 0, len);
			//return (int)(myTextPaint.measureText(corrected, 0, len) + 0.5f);
		}
	}

	@Override
	protected int getSpaceWidthInternal() {
		return (int)(myTextPaint.measureText(" ", 0, 1) + 0.5f);
	}

	@Override
	protected int getCharHeightInternal(char chr) {
		final Rect r = new Rect();
		final char[] txt = new char[] { chr };
		myTextPaint.getTextBounds(txt, 0, 1, r);
		return r.bottom - r.top;
	}

	@Override
	protected int getStringHeightInternal() {
		return (int)(myTextPaint.getTextSize() + 0.5f);
	}

	@Override
	protected int getDescentInternal() {
		return (int)(myTextPaint.descent() + 0.5f);
	}
	
	static LinkedHashSet<Character> PUNCTUATION_CHARS = new LinkedHashSet<Character>();
	static char[] PUNCTUATION_CHARS_ARRAY = new char[] {',', '.', ';', ':', '!', '?', '，', '。', '；', '？', '：', '！', '「', '」', '、', '『', '』', '\'', '"',
			'“', '”', '‘', '’', '(', ')', '[', ']', '（', '）', '【', '】','《','》','─','…'};
	public static boolean isCharPunctuation(char ch) {
		if(PUNCTUATION_CHARS.size() == 0) {
			for(int i = 0; i < PUNCTUATION_CHARS_ARRAY.length; i++) {
				PUNCTUATION_CHARS.add(PUNCTUATION_CHARS_ARRAY[i]);
			}
		}

		if(PUNCTUATION_CHARS.contains(ch)) return true;
		return false;
	}
	
	LinkedHashSet<Character> HORIZONTAL_CHARS = new LinkedHashSet<Character>();
	char[] HORIZONTAL_CHARS_ARRAY = new char[]{'(', ')', '[', ']', '（', '）', '【', '】','《','》','…','─'};
	private boolean isCharShouldHorizontal(char ch) {
		if(HORIZONTAL_CHARS.size() == 0) {
			for(int i = 0; i < HORIZONTAL_CHARS_ARRAY.length; i++) {
				HORIZONTAL_CHARS.add(HORIZONTAL_CHARS_ARRAY[i]);
			}
		}
		if(HORIZONTAL_CHARS.contains(ch)) return true;
		return false;
	}
	
	public Canvas getCanvas() {
		return myCanvas;
	}
	
	public static boolean isPUA(char ch) {
        return ('\uE000' <= ch && '\uF8FF' >= ch);
    }
	
	public void drawText(int x, int y, char[] string, int offset, int length) {
		int stringWidth = 0;
//		myFillPaint.setColor(Color.BLUE);
//		myCanvas.drawRect(new Rect(-415, 200, -515, 400), myFillPaint);
//		myTextPaint.setColor(Color.YELLOW);
//        myTextPaint.setTextSize(108);
//        String s = ""+myTextPaint.getFontMetrics().leading+";"
//                +myTextPaint.getFontMetrics().top+";"
//                +myTextPaint.getFontMetrics().bottom+";"
//                +myTextPaint.getFontMetrics().ascent+";"
//                +myTextPaint.getFontMetrics().descent+";"+this.myTextPaint.getTextSize();
//        myTextPaint.setTextSize(48);
//        myCanvas.drawText(s,-1000, 50,myTextPaint);
//		myTextPaint.setTextSize(108);
//		myCanvas.drawText("Apwppppp蕭", -800, 200,myTextPaint);
//        myCanvas.drawLine(0, 200, -800, 200, myTextPaint);
//        myLinePaint.setColor(Color.BLACK);
//		myCanvas.drawLine(0, 200-myTextPaint.getTextSize(), -800, 200-myTextPaint.getTextSize(), myLinePaint);
//        myLinePaint.setColor(Color.BLUE);
//		myCanvas.drawLine(0, 200+myTextPaint.descent(), -800, 200+myTextPaint.descent(), myLinePaint);
//        myLinePaint.setColor(Color.RED);
//		myCanvas.drawLine(0, 200+myTextPaint.ascent(), -800, 200+myTextPaint.ascent(), myLinePaint);
//        myLinePaint.setColor(Color.GREEN);
//		myCanvas.drawLine(0, 200+myTextPaint.getTextSize() + myTextPaint.ascent(), -800,
//                200+myTextPaint.getTextSize() + myTextPaint.ascent(), myLinePaint);
//        myLinePaint.setColor(Color.RED);
//        myCanvas.drawLine(0, 308+myTextPaint.ascent(), -800, 308+myTextPaint.ascent(), myLinePaint);
//
//        myCanvas.drawText("Apw我䥢一", -800, 308,myTextPaint);
		//y'=x, x'=-y;
		float diff = -myTextPaint.getTextSize() - myTextPaint.ascent();//-myTextPaint.getFontMetricsInt().descent;
        if(diff > 0) {
            diff = 0;
        }
		for(int i = offset; i < offset + length; i++) {
//			if(isPUA(string[i])) {
//				stringWidth += drawUTF16Text(""+string[i], x + stringWidth, y, "EUDC");
//			} else
			if(Character.isHighSurrogate(string[i]) && string.length > 1) {
				stringWidth += drawUTF16Text(""+string[i]+string[i+1], (int)(x + stringWidth+diff), y, "TW-Kai-Ext-B");
				i++;
			} else if(myIsShowGujiPunctuation!=GujiPunctuationEnum.show && isCharPunctuation(string[i])) {
			} else if(isCharShouldHorizontal(string[i])) {
				myCanvas.rotate(90);
				myCanvas.drawText(string, i, 1, x+stringWidth, y, myTextPaint);
				stringWidth += getStringWidth(string, i, 1);
				myCanvas.rotate(-90);	
			} else if(isDigit(string[i])) {
				myCanvas.drawText(string, i, 1, -y+ 3, x + stringWidth + this.getStringHeight()+diff, myTextPaint);
				stringWidth += getStringHeight();
			} else {
				// set x', y'
//				myCanvas.drawLine(0, x + stringWidth + this.getStringHeight(), 1000, x + stringWidth + this.getStringHeight(), this.myLinePaint);
//				myCanvas.drawLine(0, x + stringWidth + this.getStringHeight()-myTextP, 1000, x + stringWidth + this.getStringHeight(), this.myLinePaint);
//				myCanvas.drawLine(0, x + stringWidth + this.getStringHeight(), 1000, x + stringWidth + this.getStringHeight(), this.myLinePaint);
//				myCanvas.drawLine(0, x + stringWidth + this.getStringHeight(), 1000, x + stringWidth + this.getStringHeight(), this.myLinePaint);
//				myCanvas.drawLine(0, x + stringWidth + this.getStringHeight(), 1000, x + stringWidth + this.getStringHeight(), this.myLinePaint);
				myCanvas.drawText(string, i, 1, -y, x + stringWidth + this.getStringHeight()+diff, myTextPaint);
				stringWidth += getStringHeight();//getStringWidth(string, i, 1);
			}
		}
	}

	@Override
	public void drawString(int x, int y, char[] string, int offset, int length, boolean isGujiString, Boolean3 languageType) {
		if(languageType == Boolean3.TRUE) {
			String zhString = new String(string, offset, length);
			string = ChineseConvertor.convertToZht(zhString).toCharArray();
			offset = 0;
		} else if(languageType == Boolean3.FALSE) {
			String zhString = new String(string, offset, length);
			string = ChineseConvertor.convertToZhs(zhString).toCharArray();
			offset = 0;
		}
		
		boolean containsSoftHyphen = false;
		for (int i = offset; i < offset + length; ++i) {
			if (string[i] == (char)0xAD) {
				containsSoftHyphen = true;
				break;
			}
		}
		if (!containsSoftHyphen) {
			if(isGujiString) {
				drawText(x, y, string, offset, length);
			} else {
//                myTextPaint.setColor(Color.YELLOW);
//                myTextPaint.setTextSize(108);
//                myCanvas.drawText("我愛伱", 415, 200,myTextPaint);
//                myCanvas.drawLine(0, 200, 800, 200, myTextPaint);
//                myLinePaint.setColor(Color.BLACK);
//                myCanvas.drawLine(0, 200-myTextPaint.getTextSize(), 800, 200-myTextPaint.getTextSize(), myLinePaint);
//                myLinePaint.setColor(Color.BLUE);
//                myCanvas.drawLine(0, 200+myTextPaint.descent(), 800, 200+myTextPaint.descent(), myLinePaint);
//                myLinePaint.setColor(Color.RED);
//                myCanvas.drawLine(0, 200+myTextPaint.ascent(), 800, 200+myTextPaint.ascent(), myLinePaint);
//                myLinePaint.setColor(Color.GREEN);
//                myCanvas.drawLine(0, 200+myTextPaint.getTextSize() + myTextPaint.ascent(), 800, 200+myTextPaint.getTextSize() + myTextPaint.ascent(), myLinePaint);
				myCanvas.drawText(string, offset, length, x, y, myTextPaint);
			}
			
		} else {
			final char[] corrected = new char[length];
			int len = 0;
			for (int o = offset; o < offset + length; ++o) {
				final char chr = string[o];
				if (chr != (char)0xAD) {
					corrected[len++] = chr;
				}
			}
			if(isGujiString) {
				drawText(x, y, corrected, 0, len);
			} else {
				myCanvas.drawText(corrected, 0, len, x, y, myTextPaint);
			}
		}
	}

	@Override
	public Size imageSize(ZLImageData imageData, Size maxSize, ScalingType scaling) {
		final Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(maxSize, scaling);
		return (bitmap != null && !bitmap.isRecycled())
			? new Size(bitmap.getWidth(), bitmap.getHeight()) : null;
	}
	
	@Override
	public void drawImage(int x, int y, Bitmap bitmap, Size maxSize, ScalingType scaling, ColorAdjustingMode adjustingMode) {
		if (bitmap != null) {
			switch (adjustingMode) {
				case LIGHTEN_TO_BACKGROUND:
					myFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
					break;
				case DARKEN_TO_BACKGROUND:
					myFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
					break;
				case NONE:
					break;
			}
			myCanvas.drawBitmap(bitmap, x, y, myFillPaint);
			myFillPaint.setXfermode(null);
		}
	}

	@Override
	public void drawImage(int x, int y, ZLImageData imageData, Size maxSize, ScalingType scaling, ColorAdjustingMode adjustingMode) {
		final Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(maxSize, scaling);
		if (bitmap != null && !bitmap.isRecycled()) {
			switch (adjustingMode) {
				case LIGHTEN_TO_BACKGROUND:
					myFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
					break;
				case DARKEN_TO_BACKGROUND:
					myFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
					break;
				case NONE:
					break;
			}
			myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myFillPaint);
			myFillPaint.setXfermode(null);
		}
	}

    public void drawLine(int startX, int startY, int endX, int endY, Paint paint, Canvas canvas) {
        int start  = 0;
        int end = 0;
        int startCross = 0;
        if (startY == endY) {
            start = Math.min(startX, endX);
            end = Math.max(startX, endX);
            startCross = startY;
        } else if( startX == endX) {
            start = Math.min(startY, endY);
            end = Math.max(startY,endY);
            startCross = startX;
        }

        int lineWidth = (int)paint.getStrokeWidth();
        int lbwidth = lineWidth;
        int nbwidth = lbwidth;
        int lightWidth = 1;
        int heavyWidth = 1;
        boolean direction = random.nextBoolean();
        int pos = 0;
        int section=0;
        int currentPos = start;
        int currentWidth = 0;

        List<Point> alpha0 = new ArrayList<Point>();
        List<Point> alpha100 = new ArrayList<Point>();
        List<Point> alpha220 = new ArrayList<Point>();
        List<Point> alpha255 = new ArrayList<Point>();
        List<Point> list = null;
        Point p = new Point();
        for(int i = start; i< end; i++) {
            if ((i - currentPos)==section) {
                lbwidth = nbwidth;
                nbwidth =   lineWidth > 2 ? lineWidth / 3 + random.nextInt(2*lineWidth/3 + 1) : random.nextInt(lineWidth + 1);
                section = 200;//random.randint(1, 200)
                currentPos = i;
//                lightWidth = random.nextInt(2);
//                heavyWidth = random.nextInt(2);
                direction = random.nextBoolean();
            }
            if (i % 2 == 0) {
                pos = random.nextInt(lineWidth <= 2 ? 3 : 4);
            }
            if(nbwidth == 0) {
                pos = 0;
            }
            currentWidth = lbwidth + ((i - currentPos)) * (nbwidth - lbwidth) / section;
            lightWidth = 0;
            heavyWidth = 0;
            for(int width = -pos; width< lineWidth; width++) {
                int y = startCross + (lineWidth - currentWidth) / 2 - lightWidth + width;
                int alpha = 0;
                if (width < lightWidth) {
                    alpha = 100;//direction ? 100 : 220;
                    list = alpha100;
                } else if( width>=lightWidth && width < lightWidth + currentWidth) {
                    alpha = 255;
                    list = alpha255;
                } else if( width<lightWidth + currentWidth + heavyWidth) {
                    alpha = direction? 220 : 100;
                    list = direction? alpha220 : alpha100;
                } else {
                    list = alpha0;
                }

                //paint.setAlpha(alpha);
                if (startY == endY) {
                    p = new Point(i, y);
                    //canvas.drawPoint(i, y, paint);
                } else {
                    //canvas.drawPoint(y, i, paint);
                    p = new Point(y, i);
                }
                list.add(p);
            }
        }

        paint.setAlpha(0);
        list = alpha0;
        Point[] po = new Point[list.size()];
        list.toArray(po);
        float[] points = new float[po.length * 2];
        for(int j = 0; j < po.length; j++) {
            points[2*j] = po[j].x;
            points[2*j+1] = po[j].y;
        }
        canvas.drawPoints(points, paint);

        paint.setAlpha(100);
        list = alpha100;
        po = new Point[list.size()];
        list.toArray(po);
        points = new float[po.length * 2];
        for(int j = 0; j < po.length; j++) {
            points[2*j] = po[j].x;
            points[2*j+1] = po[j].y;
        }
        canvas.drawPoints(points, paint);

        paint.setAlpha(220);
        list = alpha220;
        po = new Point[list.size()];
        list.toArray(po);
        points = new float[po.length * 2];
        for(int j = 0; j < po.length; j++) {
            points[2*j] = po[j].x;
            points[2*j+1] = po[j].y;
        }
        canvas.drawPoints(points, paint);

        paint.setAlpha(255);
        list = alpha255;
        po = new Point[list.size()];
        list.toArray(po);
        points = new float[po.length * 2];
        for(int j = 0; j < po.length; j++) {
            points[2*j] = po[j].x;
            points[2*j+1] = po[j].y;
        }
        canvas.drawPoints(points, paint);

    }

	@Override
	public void drawLine(int x0, int y0, int x1, int y1) {
		final Canvas canvas = myCanvas;
		final Paint paint = myLinePaint;
		paint.setAntiAlias(false);
		//canvas.drawLine(x0, y0, x1, y1, paint);
		if(y0 == y1 && mIsGuji) {
            drawLine(x0, y0, x1, y1, paint, canvas);
		} else if(x0 == x1 && mIsGuji) {
            drawLine(x0, y0, x1, y1, paint, canvas);
        } else {
			canvas.drawLine(x0, y0, x1, y1, paint);
		}
        //paint.setAlpha(255);
		canvas.drawPoint(x0, y0, paint);
		canvas.drawPoint(x1, y1, paint);
		paint.setAntiAlias(true);
	}
	
	@Override
	public void drawRectangle(int x0, int y0, int x1, int y1) {
		final Canvas canvas = myCanvas;
		final Paint paint = myLinePaint;
		if (x1 < x0) {
			int swap = x1;
			x1 = x0;
			x0 = swap;
		}
		if (y1 < y0) {
			int swap = y1;
			y1 = y0;
			y0 = swap;
		}
		paint.setAntiAlias(false);
		canvas.drawRect(x0, y0, x1, y1, paint);
		paint.setAntiAlias(true);
	}

	@Override
	public void fillRectangle(int x0, int y0, int x1, int y1) {
		if (x1 < x0) {
			int swap = x1;
			x1 = x0;
			x0 = swap;
		}
		if (y1 < y0) {
			int swap = y1;
			y1 = y0;
			y0 = swap;
		}
		myCanvas.drawRect(x0, y0, x1 + 1, y1 + 1, myFillPaint);
	}
	
	@Override
	public void fillRoundRectangle(int x0, int y0, int x1, int y1, float rx, float ry) {
		if (x1 < x0) {
			int swap = x1;
			x1 = x0;
			x0 = swap;
		}
		if (y1 < y0) {
			int swap = y1;
			y1 = y0;
			y0 = swap;
		}
		myCanvas.drawRoundRect(new RectF(x0, y0, x1 + 1, y1 + 1), rx, ry, myFillPaint);
	}

	@Override
	public void fillCircle(int x, int y, int radius) {
		myCanvas.drawCircle(x, y, radius, myFillPaint);
	}

    @Override
    public void fillPolygon(int[] xs, int[] ys) {
        final Path path = new Path();
        final int last = xs.length - 1;
        path.moveTo(xs[last], ys[last]);
        for (int i = 0; i <= last; ++i) {
            path.lineTo(xs[i], ys[i]);
        }

        myCanvas.drawPath(path, myFillPaint);
    }
	
	public float drawUTF16Text(String newGlyph, int x, int y, String fontName) {
		Typeface previosType = myTextPaint.getTypeface();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(!myTextPaint.hasGlyph(newGlyph)) {
				myTextPaint.setTypeface(AndroidFontUtil.systemTypeface(fontName, previosType.isBold(), previosType.isItalic()));
			}
		}
		myCanvas.drawText(newGlyph, -y, x + this.getStringHeight(), myTextPaint);
		float width = myTextPaint.measureText(newGlyph);
		myTextPaint.setTypeface(previosType);
		return width;
	}
}
