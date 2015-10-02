package org.geometerplus.zlibrary.ui.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class VerticalTextView extends TextView {

    /**
     * 绘制单个字符的画笔
     */
    private Paint charPaint;

    private char[] indexs;

    private int textCount;

    private String textString;
    private boolean isVertical;

    public VerticalTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        charPaint = new Paint();

        textString = getText().toString();
        indexs = getKeyChar(textString);
        textCount = textString.toCharArray().length;
    }
    
    public void setText(CharSequence text, boolean vertical) {
        setText(text);
        isVertical = vertical;
        textString = getText().toString();
        indexs = getKeyChar(textString);
        textCount = indexs.length;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	if(isVertical) {
    		if (textCount == 0 || TextUtils.isEmpty(textString))
	            return;
	        float childHeight = this.getTextSize();//getHeight() / textCount;
	        charPaint.setTypeface(AndroidFontUtil.systemTypeface("方正书宋_GBK", true, false));
	        charPaint.setColor(getResources().getColor(android.R.color.black));
	        if(textCount <=5) {
	        	charPaint.setTextSize(this.getTextSize());
	        } else {
	        	charPaint.setTextSize(this.getTextSize()/2);
	        	childHeight = this.getTextSize()/2;
	        }
	        charPaint.setTextAlign(Paint.Align.LEFT);
	
	        FontMetrics fm = charPaint.getFontMetrics();
	        for (int i = 0; i < textCount; i++) {
	            canvas.drawText(
	                    String.valueOf(indexs[i]),
	                    this.getPaddingLeft() - 4,
	                    (float) (((i + 0.5) * childHeight) - (fm.ascent + fm.descent) / 2) + this.getPaddingTop(),
	                    charPaint);
	        }
    	} else {
    		super.onDraw(canvas);
    	}
    }

    protected char[] getKeyChar(String str) {
    	str = ZLAndroidPaintContext.combineChartoString(str);
        char[] keys = new char[str.length()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = str.charAt(i);
        }
        return keys;
    }
}