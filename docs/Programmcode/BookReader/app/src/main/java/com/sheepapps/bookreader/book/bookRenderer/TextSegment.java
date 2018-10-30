package com.sheepapps.bookreader.book.bookRenderer;

import android.graphics.Canvas;

import com.sheepapps.bookreader.book.bookParser.BaseBookReader;
import com.sheepapps.bookreader.book.common.FixedCharSequence;
import com.sheepapps.bookreader.book.common.FixedStringBuilder;

import java.util.LinkedList;
import java.util.List;

public class TextSegment implements TextPage.IPageSegment {
	private int mFlags;
	private int mLetters;
	private int mWidth;
	private int mLineWidth;
	private List<FixedCharSequence> mWords;
	private FixedCharSequence mText;
	
	private final FontStyle mPaint;
	private final long mPosition;

	public int getFlags() {
		return mFlags;
	}
	
	public long getPosition() {
		return mPosition;
	}

	public int getWidth() {
		return mWidth;
	}
	
	public void setLineWidth(int value) {
		mLineWidth = value;
	}
	
	public void setFlags(int value) {
		mFlags = value;
	}
	
	public int getChars() {
		return mText == null ? mLetters + mWords.size() - 1 : mText.length();
	}

	public FixedCharSequence getText() {
		return mText;
	}

	public void appendText(FixedCharSequence value, int width) {
		int length = value.length();	
		
		mWords.add(value);
		mLetters += length;
		mWidth += width;
	}

	public void appendNonBreakText(CharSequence value, int width) {
		CharSequence old = mWords.get(mWords.size() - 1);
		FixedStringBuilder builder = new FixedStringBuilder(old.length() + value.length());
		builder.append(old);
		builder.append(value);
		
		mWords.set(mWords.size() - 1, builder.toCharSequence());
		mLetters += value.length();
		mWidth += width;
	}
	
	public void appendSpace(int width) {
		if (width != 0) {
			mLetters++;
			mWidth += width;
		}
	}
	
	public FontStyle getPaint() {
		return mPaint;
	}

	public TextSegment(FixedCharSequence text, int flags, FontStyle paint, int width, long position) {
		mWords = new LinkedList<>();
		mFlags = flags;
		mPaint = paint;
		mPosition = position;
		appendText(text, width);
	}
	
	private void justify(int targetWidth) {
		if (mLineWidth == -1 || mWords.size() == 0) {
			finish();
			return;
		}
		
		char spaceChar = mPaint.getSpaceChar();
		
		if (mLineWidth == 0)
			mLineWidth = mWidth;
		
		if (mWords.size() == 1) {
			if ((mFlags & BaseBookReader.NEW_LINE) == 0) {
				int spacesNeedeed = (targetWidth - mLineWidth) / mPaint.getSpaceWidthInt();
				FixedStringBuilder result = new FixedStringBuilder(mLetters + spacesNeedeed);
				for(int j = 0; j < spacesNeedeed; j++)
					result.append(spaceChar);
				
				result.append(mWords.get(0));
				mText = result.toCharSequence();
				mWords = null;
			} else				
				finish();
			
			return;
		}
		
		int spaces = (mWords.size() - 1);
		int spaceNeeded = (targetWidth - mLineWidth)  / mPaint.getSpaceWidthInt();
		
		if (spaceNeeded <= 0) {
			spaceNeeded = 0;
		}
		
		int toAdd = (spaceNeeded / spaces);
		if (spaceNeeded % spaces != 0)
			toAdd++;
		
		if (toAdd < 0)
			toAdd = 0;

		FixedStringBuilder result = new FixedStringBuilder(mLetters + spaces + spaceNeeded + 1);

		boolean first = true;
		for (FixedCharSequence word : mWords) {
			result.append(' ');
			if (!first) {
				for (int j = 0; j < toAdd; j++) {
					result.append(spaceChar);
					spaceNeeded--;
					if (spaceNeeded <= 0) {
						toAdd = 0;
					}
				}
			} else {
				first = false;
			}
			result.append(word);
		}

		mText = result.toCharSequence(1);
		mWords = null;
		mWidth >>= 4;
	}
	
	private void finish() {
		if (mWords.size() == 1) {
            mText = mWords.get(0);
        } else {
			FixedStringBuilder result = new FixedStringBuilder(mLetters + 1);

			for (FixedCharSequence word : mWords) {
				result.append(' ');
				result.append(word);
			}
			mText = result.toCharSequence(1);
		}
		mWords = null;
		mWidth >>= 4;
	}
	
	public void draw(Canvas canvas, float shiftX, TextPage.PageCaret caret, int pageWidth, int pageHeight, boolean onlyOne) {
		float posx = caret.getPosX();
		float posy = caret.getPosY();
		
		if ((mFlags & BaseBookReader.NEW_LINE) != 0) {
			posx = shiftX;
			posy += mPaint.getHeight();

            if (caret.getLastHeightMod() != 0)
                posy += caret.getLastHeightMod();

            posy += mPaint.getHeightMod();
	
			caret.setLastHeightMod(mPaint.getHeightMod());
			
			if ((mFlags & BaseBookReader.FIRST_LINE) != 0)
				posx += mPaint.getFirstLine();
			
			caret.setPosY(posy);
		}			
		
		if (mWords != null)
			finish(pageWidth, pageHeight, onlyOne);		
		
		if ((mFlags & BaseBookReader.RTL) != 0) {
			mPaint.drawText(canvas, mText, (pageWidth >> 4) - posx - mWidth + shiftX * 2, posy);
		}
		else {
			mPaint.drawText(canvas, mText, posx, posy);
		}
		
		posx += mWidth;
		
		caret.setPosX(posx);
	}

	public boolean calculate(TextPage.PageCaret caret, int maxHeight) {
		if (caret.getPosY() > maxHeight)
			return false;
		
		if ((mFlags & BaseBookReader.NEW_LINE) != 0) {
			float posy = caret.getPosY();
			posy += mPaint.getHeight();

            if (caret.getLastHeightMod() != 0)
                posy += caret.getLastHeightMod();

            posy += mPaint.getHeightMod();
	
			caret.setLastHeightMod(mPaint.getHeightMod());
			caret.setPosY(posy);
			return true;
		}		
		return true;
	}
	
	private void finish(int pageWidth, int pageHeight, boolean onlyOne) {
		if ((mFlags & BaseBookReader.RTL) != 0) {
			finish();
			mWidth = (int) mPaint.measureText(mText);
			return;
		}
		
		if ((mFlags & (BaseBookReader.JUSTIFY | BaseBookReader.LINE_BREAK)) == BaseBookReader.JUSTIFY) {
			justify(pageWidth);
		} else {
            finish();
        }
	}
	
	public void clean() {
		mText = null;
		mWords = null;
	}
}