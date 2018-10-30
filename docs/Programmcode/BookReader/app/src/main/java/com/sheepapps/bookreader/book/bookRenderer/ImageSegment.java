package com.sheepapps.bookreader.book.bookRenderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.sheepapps.bookreader.book.bookParser.ImageData;

public class ImageSegment implements TextPage.IPageSegment {
	private static final Paint sBitmapPaint;
	private static final Paint sBackPaint;
	
	static {
		sBitmapPaint = new Paint();
		sBitmapPaint.setFilterBitmap(true);
		sBitmapPaint.setColor(0xFFFFFFF);
		sBitmapPaint.setDither(true);
		sBitmapPaint.setAlpha(255);
		
		sBackPaint = new Paint();
		sBackPaint.setColor(0xFFFFFFFF);
	}
	
	private int mWidth;
	private int mHeight;
	private final ImageData mImage;
	private final long mPosition;
	
	private int mWidthOffset;
	private int mHeightOffset;
	private boolean mFinished;
	
	public long getPosition()
	{
		return mPosition;
	}
	
	public ImageSegment(ImageData image, int width, int height, long position) {
		mImage = image;
		mWidth = width;
		mHeight = height;
		mFinished = false;
		mPosition = position;
	}	
	
	public void draw(Canvas canvas, float shiftX, TextPage.PageCaret caret, int pageWidth, int pageHeight, boolean onlyOne) {
		if (!mFinished)
			finish(pageWidth, pageHeight, onlyOne);
		
		float posx = shiftX + mWidthOffset;
		float posy = caret.getPosY() + mHeightOffset + caret.getLastHeightMod();
		
		int width = (mWidth >> 4);
		int height = (mHeight >> 4);
		
		Bitmap bitmap = mImage.extractImage(width, height);
		
		if (bitmap != null && !bitmap.isRecycled()) {
			canvas.drawRect(new RectF(posx, posy, posx + width, posy + height), sBackPaint);
			canvas.drawBitmap(bitmap, null, new RectF(posx, posy, posx + width, posy + height), sBitmapPaint);
		} else {
			canvas.drawRect(new RectF(posx, posy, posx + width, posy + height), sBitmapPaint);
		}
		caret.setLastHeightMod(0);
			
		posx += width;
		posy += height;
		
		caret.setPosX(posx);
		caret.setPosY(posy);
	}

	private void finish(int pageWidth, int pageHeight, boolean onlyOne) {
		if (onlyOne) {
			mHeight = mHeight * pageWidth / mWidth;
			mWidth = pageWidth;
			
			if (mHeight > pageHeight) {
				mWidth = mWidth * pageHeight / mHeight;
				mHeight = pageHeight;
			}
		}
		
		mWidthOffset = ((pageWidth - mWidth) / 32);
		mHeightOffset = onlyOne ? ((pageHeight - mHeight) / 32) : 1;
		mFinished = true;
	}
	
	public boolean calculate(TextPage.PageCaret caret, int maxHeight) {
		float posy = caret.getPosY() + mHeightOffset + caret.getLastHeightMod() + (mHeight >> 4);
		
		if (posy > maxHeight)
			return false;
		
		caret.setLastHeightMod(0);		
		caret.setPosY(posy);
		return true;
	}
	
	public void clean() {
		mImage.clean();
	}
}