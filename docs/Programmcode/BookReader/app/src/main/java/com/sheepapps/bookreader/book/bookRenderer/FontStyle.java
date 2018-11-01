package com.sheepapps.bookreader.book.bookRenderer;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;

import com.sheepapps.bookreader.book.common.FixedCharSequence;

public class FontStyle {
	
	private static float [] sWidth = new float[32];
	
	private final TextPaint mPaint;
	
	private final float mHeightMod;
	private final float mLineSpace;
	private final float mHeight;
	private final float mSpaceWidth;
	private final float mDashWidth;
	private float mFirstLine;
	
	private final int mDashWidthInt;
	private final int mSpaceWidthInt;
	private final int mWordSpaceWidthInt;
	private final int mHeightInt;
	private final int mHeightModInt;
	private int mFirstLineInt;
	private final boolean mHasTab;
	
	private final char mSpaceChar;
	
	public TextPaint getPaint() {
		return mPaint;
	}
	
	public float getHeightMod() {
		return mHeightMod;
	}

	public float getHeight() {
		return mHeight;
	}	

	public int getHeightModInt() {
		return mHeightModInt;
	}
	
	public int getHeightInt() {
		return mHeightInt;
	}
	
	public float getSpaceWidth() {
		return mSpaceWidth;
	}
	
	public float getDashWidth() {
		return mDashWidth;
	}
	
	public int getDashWidthInt() {
		return mDashWidthInt;
	}
	
	public int getSpaceWidthInt() {
		return mSpaceWidthInt;
	}
	
	public float getFirstLine() {
		return mFirstLine;
	}
	
	public int getFirstLineInt() {
		return mFirstLineInt;
	}
	
	public int getWordSpaceWidthInt() {
		return mWordSpaceWidthInt;
	}
	
	public char getSpaceChar() {
		return mSpaceChar;
	}
	
	public boolean hasTab() {
		return mHasTab;
	}
	
	public void setFirstLine(float value) {
		mFirstLine = value;
		mFirstLineInt = (int)(value * 16);
	}
	
	public FontStyle(TextPaint paint, float extraSpace, float lineSpace, float firstLine) {
		mPaint = paint;
		mLineSpace = lineSpace;
		mFirstLine = firstLine;
		
		mHeight = paint.getTextSize();
		
		mHeightMod = (mLineSpace - 0.75f) * paint.getTextSize() * 0.5f;

		float wordSpaceWidth = measureChar(' ');
		
		Rect spaceBounds = new Rect();
		mPaint.getTextBounds(new char[]{' '}, 0, 1, spaceBounds); // thin space
		
		float spaceWidth = spaceBounds.width();
		if (spaceBounds.height() > paint.getTextSize() / 4 || spaceWidth == 0) {
			mSpaceWidth = wordSpaceWidth;
			mSpaceChar = ' ';
		} else {
			mSpaceWidth = spaceWidth;
			mSpaceChar = ' ';
		}
		
		mDashWidth = measureChar('-');
		
		Rect tabBounds = new Rect();
		mPaint.getTextBounds(new char[]{'\t'}, 0, 1, tabBounds);
		
		mHasTab = tabBounds.height() < paint.getTextSize() / 4;
		
		mHeightModInt = (int)(mHeightMod * 16);
		mSpaceWidthInt = (int)(mSpaceWidth * 16);
		mWordSpaceWidthInt = (int)(wordSpaceWidth * 16);
		mDashWidthInt = (int)(mDashWidth * 16);
		mHeightInt = (int)(mHeight * 16);
		mFirstLineInt = (int)(firstLine * 16);
	}


	
	public void drawText(Canvas canvas, FixedCharSequence text, float posx, float posy) {
		char [] chars = text.getChars();
		int offset = text.getOffset();
		int length = text.length();
		canvas.drawText(chars, offset, length, posx, posy, mPaint);
	}
	
	private float measureChar(char ch) {
		return mPaint.measureText(new char[]{ch},0,1);
	}
	
	public float[] getTextWidths(CharSequence text) {
		if (text.length() > sWidth.length)
			sWidth = new float[text.length()];
		mPaint.getTextWidths(text, 0, text.length(), sWidth);
		return sWidth;
	}

	/*public float measureText(CharSequence text) {
		return mPaint.measureText(text, 0, text.length());
	}*/
	
	public float measureText(FixedCharSequence text) {
		return mPaint.measureText(text.getChars(), text.getOffset(), text.length());
	}

	public int measureTextInt(CharSequence text) {
		return (int)(mPaint.measureText(text, 0, text.length()) * 16);
	}

	public int measureTextInt(FixedCharSequence text) {
		return (int)(mPaint.measureText(text.getChars(), text.getOffset(), text.length()) * 16);
	}
	

}
