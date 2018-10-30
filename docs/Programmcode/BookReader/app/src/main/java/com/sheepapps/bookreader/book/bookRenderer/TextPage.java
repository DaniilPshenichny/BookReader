package com.sheepapps.bookreader.book.bookRenderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.sheepapps.bookreader.book.bookParser.BaseBookReader;
import com.sheepapps.bookreader.book.bookParser.ImageData;
import com.sheepapps.bookreader.book.common.FixedCharSequence;

import java.util.LinkedList;
import java.util.List;

public class TextPage {
	public static final int PAGE_BREAK = 8;
	public static final int PAGE_FULL = 1;
	public static final int NEW_PAGE = 2;
	public static final int FOOTER_PAGE_BREAK = 4;
	
	private int mLastWidth;
	private int mLastFooterWidth;
	private int mHeightLeft;
	
	private long mStartPosition;
	private long mEndPosition;
	private int mEndOffset;
	
	private List<IPageSegment> mSegments;
	private List<IPageSegment> mFooterSegments;
	private TextSegment mLastTextSegment;
	private TextSegment mLastFooterTextSegment;
	
	private final int mWidth;
	private final int mFooterSpace;
	private final int mNumber;

	private int mHeight;
	private Bitmap mBitmap;
	private boolean mHaveImages;
	private int mPageFinishType;

	public boolean isEmpty() {
		return mSegments.size() == 0;
	}	
	
	public boolean haveImages() {
		return mHaveImages;
	}
	
	public long getStartPosition() {
		return mStartPosition;
	}
	
	public long getEndPosition() {
		return mEndPosition;
	}
	
	public int getEndOffset() {
		return mEndOffset;
	}
	
	public int getPageNumber() {
		return mNumber;
	}
	
	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setBitmap(Bitmap value) {
		mBitmap = value;
	}
	
	public void setEndPosition(long endPosition) {
		mEndPosition = endPosition;
	}
	
	public void setEndOffset(int endOffset) {
		mEndOffset = endOffset;
	}
	
	public int getPageFinishType() {
		return mPageFinishType;
	}
	
	public TextPage(int width, int height, int footerSpace, int number) {
		mWidth = width << 4;
		mHeight = height << 4;
		mHeightLeft = height << 4;
		mFooterSpace = footerSpace << 4;
		mSegments = new LinkedList<>();
		mFooterSegments = new LinkedList<>();
		mNumber = number;
	}

	private TextSegment addTextSegment(FixedCharSequence text, int flags, FontStyle paint, int width, int height, long position) {
		TextSegment segment = null;
		
		if ((flags & BaseBookReader.FOOTER) != 0) {
			if (mFooterSegments.size() == 0)
				mHeightLeft -= mFooterSpace;
			
			mLastFooterTextSegment = segment = new TextSegment(text, flags, paint, width, position);
			mFooterSegments.add(segment);
		} else {
			mLastTextSegment = segment = new TextSegment(text, flags, paint, width, position);
			mSegments.add(segment);
		}

		if ((flags & BaseBookReader.NEW_LINE) != 0) {
			if ((flags & BaseBookReader.FOOTER) != 0)
				mLastFooterWidth = width;
			else
				mLastWidth = width;
			
			mHeightLeft -= height;
		}
		return segment;
	}
	
	public void addNonBreakText(CharSequence word) {
		if (mLastTextSegment != null) {
			int width = mLastTextSegment.getPaint().measureTextInt(word);
			mLastWidth += width;
			mLastTextSegment.appendNonBreakText(word, width);
		}
	}
	
	public void addNonBreakFooterText(CharSequence word) {
		if (mLastFooterTextSegment != null) {
			int width = mLastFooterTextSegment.getPaint().measureTextInt(word);
			mLastFooterWidth += width;
			mLastFooterTextSegment.appendNonBreakText(word, width);
		}
	}
	
	public int addWord(FixedCharSequence word, int flags, FontStyle paint, long position) {
		boolean isFooter = (flags & BaseBookReader.FOOTER) != 0;
		TextSegment lastSegment = isFooter ? mLastFooterTextSegment : mLastTextSegment;
		int lastWidth = isFooter ? mLastFooterWidth : mLastWidth;
		
		int height = paint.getHeightInt() + paint.getHeightModInt() * 2;
		
		if (isFooter && mFooterSegments.size() == 0) {
			height += mFooterSpace;
		}
		
		int width;

		width = paint.measureTextInt(word);
		
		if ((flags & BaseBookReader.NEW_LINE) != 0) {
			if (lastSegment != null)			
				lastSegment.setFlags(lastSegment.getFlags() | BaseBookReader.LINE_BREAK);

			if (mHeightLeft < height) {
				height -= paint.getHeightModInt();
				
				if (mHeightLeft < height)
					return 0;
			}

			flags |= BaseBookReader.FIRST_LINE;
			
			addTextSegment(word, flags, paint, width, height, position);
			if (isFooter) {
                mLastFooterWidth += paint.getFirstLineInt();
            } else {
                mLastWidth += paint.getFirstLineInt();
            }
			
			return word.length();
		}

		boolean wordBreak = lastSegment != null && (lastSegment.getFlags() & BaseBookReader.WORD_BREAK) != 0;
		int spaceWidth = lastSegment == null || wordBreak ? 0 : lastSegment.getPaint().getWordSpaceWidthInt();
		
		int usedLength = 0;
		FixedCharSequence restWord = null;
		int restWidth = 0;
		if (lastSegment != null) {
			int leftWidth = mWidth - spaceWidth - lastWidth;
			boolean validWidth = width <= leftWidth;			
			boolean shouldHypen = !validWidth && !wordBreak;
			
			if (shouldHypen && mHeightLeft < height) // last line?
				shouldHypen = leftWidth > mWidth / 8;  // too much space left
			
			if (shouldHypen) {
				Object [] hypened = new Object[4];
				if (HypenateManager.canHypenate(word, (mWidth - spaceWidth - lastWidth)/16.0f, paint.getTextWidths(word), paint.getDashWidth(), hypened)) {
					restWord = (FixedCharSequence)hypened[1];
					word = (FixedCharSequence)hypened[0];
					int nwidth = paint.measureTextInt(word);
					restWidth = width - nwidth + paint.getDashWidthInt();
					width = nwidth;
					
					usedLength = word.length() - 1;					
					validWidth = true;
					position += usedLength;
				}
			}
				
			if (validWidth) {
				lastSegment.appendSpace(spaceWidth);
				if (isFooter)
					lastWidth = mLastFooterWidth = mLastFooterWidth + width + spaceWidth;
				else
					lastWidth = mLastWidth = mLastWidth + width + spaceWidth;
				
				if ((flags & BaseBookReader.WORD_BREAK) != 0)
					lastSegment.setFlags(lastSegment.getFlags() | BaseBookReader.WORD_BREAK);
				else
					lastSegment.setFlags(lastSegment.getFlags() & ~BaseBookReader.WORD_BREAK);
				
				if (lastSegment.getPaint() == paint) {
					if (wordBreak)
						lastSegment.appendNonBreakText(word, width);
					else
						lastSegment.appendText(word, width);
				} else {
					lastSegment.setLineWidth(-1); // do not justify
					lastSegment = addTextSegment(word, flags &~ BaseBookReader.NEW_LINE, paint, width, height, position);
					lastWidth = isFooter ? mLastFooterWidth : mLastWidth;
				}
				
				if (usedLength == 0) {
                    return word.length();
                } else {
					width = restWidth;
					word = restWord;
				}
			}
		}

		if (lastSegment != null)
			lastSegment.setLineWidth(lastWidth);

		if (mHeightLeft < height) {
			height -= paint.getHeightModInt();
			if (mHeightLeft < height)
				return usedLength;
		}

		addTextSegment(word, flags | BaseBookReader.NEW_LINE, paint, width, height, position);
				
		return usedLength + word.length();
	}
	
	public void addLink(String title, CharSequence text, FontStyle paint, long position) {
		addWord(FixedCharSequence.toFixedCharSequence(text), 0, paint, position);
	}

	public boolean addImage(ImageData image, CharSequence text, FontStyle paint, long position) {
		int width = image.getWidth() << 4;		
		int height = image.getHeight() << 4;
		
		if (width > mWidth) {
			height = height * mWidth / width;
			width = mWidth;
		}
		
		if (height > mHeight) {
			width = width * mHeight / height;
			height = mHeight;
		}
			
		if (mHeightLeft >= height * 4/5 || (mHeightLeft >= mHeight / 2)) {
			if (height > mHeightLeft) {
				width = (int)(width * mHeightLeft / height);
				height = mHeightLeft;
			}
				
			mHeightLeft -= height;
			mSegments.add(new ImageSegment(image, width, height, position));
			mHaveImages = true;
			
			if (mHeightLeft < paint.getHeightInt() * 2)
				mHeightLeft = 0;
			
			return true;
		}
		
		mHeightLeft = 0;
		return false;		
	}
	
	public void clean() {
		if (mBitmap != null)
			mBitmap.recycle();

		for(IPageSegment segment: mSegments)
			segment.clean();
			
		mSegments.clear();
		
		for(IPageSegment segment: mFooterSegments)
			segment.clean();
		
		mFooterSegments.clear();
	}

	public void trimLast() {
		if (mLastTextSegment != null) {
			mHeightLeft += mLastTextSegment.getPaint().getHeightInt() + mLastTextSegment.getPaint().getHeightModInt();
			mSegments.remove(mLastTextSegment);
		}

		mLastTextSegment = null;
		mLastWidth = 0;
		for(int i = mSegments.size() - 1; i>=0; i--) {
			IPageSegment segment = mSegments.get(i);
			if (segment.getClass() == TextSegment.class) {
				mLastTextSegment = (TextSegment)segment;
				break;
			} 
		}
	}

	public void finish(int pageFinishType, int height, int shiftY) {
		mStartPosition = -1;
		
		if ((pageFinishType & PAGE_BREAK) == 0 && mLastTextSegment != null)
			mLastTextSegment.setFlags(mLastTextSegment.getFlags() | BaseBookReader.LINE_BREAK);
		
		if (mLastTextSegment != null && mLastWidth > 0)
			mLastTextSegment.setLineWidth(mLastWidth);
		
		if ((pageFinishType & FOOTER_PAGE_BREAK) == 0 && mLastFooterTextSegment != null)
			mLastFooterTextSegment.setFlags(mLastFooterTextSegment.getFlags() | BaseBookReader.LINE_BREAK);
		
		if (mLastFooterTextSegment != null && mLastFooterWidth > 0)
			mLastFooterTextSegment.setLineWidth(mLastFooterWidth);
		
		mPageFinishType = pageFinishType;
		
		if (mHeight / 16 != height) {
			int origHeight = height;
			if (pageFinishType == NEW_PAGE) {
                height = height * 3 / 4;
            }

            int bigHeight = height << 4;
			
			if (mHeight - mHeightLeft > bigHeight) {

				PageCaret caret = new PageCaret(0, shiftY);
				
				for(IPageSegment segment: mFooterSegments) {
					segment.calculate(caret, height);
				}
				
				int first = mSegments.size() - 1;
				
				for (int i = mSegments.size() - 1; i>=0; i--) {
					if (mSegments.get(i).calculate(caret, height)) {
						first = i;
					} else {
                        break;
                    }
				}
				
				mHeightLeft = (int)((height - caret.getPosY() + shiftY) * 16);
				mHeight = origHeight << 4;
				
				if (first < 0) {
					mSegments.clear();
					return;
				} else {
                    mSegments = mSegments.subList(first, mSegments.size());
                }
			}
			
		}
		if (mSegments.size() > 0) {
			mStartPosition = mSegments.get(0).getPosition();
		}
	}

	public void draw(Canvas canvas, int shiftX, int shiftY, Paint footerLine) {
		boolean one = mSegments.size() == 1 && mFooterSegments.size() == 0;
		float posY = shiftY;
		
		if (!one && mHeightLeft < mHeight / 16)
			posY += mHeightLeft / 48;
		
		PageCaret caret = new PageCaret(shiftX, posY);

		for (IPageSegment segment : mSegments)
			segment.draw(canvas, shiftX, caret, mWidth, mHeight, one);

		if (mFooterSegments.size() > 0) {
			caret.reset();
			for (IPageSegment segment : mFooterSegments)
				segment.calculate(caret, 0x10000);

			posY = (mHeight >> 4) - caret.getPosY() + shiftY - caret.getLastHeightMod();
			
			canvas.drawLine(shiftX, posY, shiftX + (mWidth >> 4), posY, footerLine);
			
			caret.setPosY(posY);
			caret.setLastHeightMod(0);
			caret.setFirstLine(true);
			
			for (IPageSegment segment : mFooterSegments)
				segment.draw(canvas, shiftX, caret, mWidth, mHeight, false);
		}
	}
	
	public class PageCaret {
		private float m_posX;
		private float m_posY;
		private float m_lastHeightMod;
		private boolean m_firstLine;
		
		public float getPosX() {
			return m_posX;
		}
		
		public float getPosY() {
			return m_posY;
		}
		
		public boolean isFirstLine() {
			return m_firstLine;
		}
		
		public float getLastHeightMod() {
			return m_lastHeightMod;
		}
		
		public void setPosX(float value) {
			m_posX = value;
		}
		
		public void setFirstLine(boolean value) {
			m_firstLine = value;
		}
		
		public void setPosY(float value) {
			m_posY = value;
		}
		
		public void setLastHeightMod(float value) {
			m_firstLine = false;
			m_lastHeightMod = value;
		}
		
		public PageCaret(float posX, float posY) {
			m_firstLine = true;
			m_posX = posX;
			m_posY = posY;
		}
		
		public void reset() {
			m_posY = 0;
			m_posX = 0;
			m_lastHeightMod = 0;
			m_firstLine = true;
		}
	}
	
	public interface IPageSegment {
		boolean calculate(PageCaret caret, int maxHeight);
		
		void draw(Canvas canvas, float shiftX, PageCaret caret, int pageWidth, int pageHeight, boolean onlyOne);

		long getPosition();

		void clean();
	}

	public float getAverageLineChars(int minLines) {
		int lines = 0;
		int chars = 0;
		for (IPageSegment segment : mSegments) {
			if (segment instanceof TextSegment) {
				TextSegment textSegment = (TextSegment)segment; 
				if (textSegment.getChars() > 0 && (textSegment.getFlags() & (BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY | BaseBookReader.LINE_BREAK)) == (BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY)) {
					lines++;
					chars += textSegment.getChars();
				}
			}
		}
		if (lines >= minLines && chars > 0)
			return (float)chars / (float)lines;
		return 0;
	}
	
	public int getAverageLines(int minLines) {
		int lines = 0;
		for (IPageSegment segment : mSegments) {
			if (segment instanceof TextSegment) {
				TextSegment textSegment = (TextSegment)segment; 
				if ((textSegment.getFlags() & (BaseBookReader.NEW_LINE)) != 0) {
					lines++;
				}
			}
		}
		if (lines >= minLines)
			return lines;
		return 0;
	}
}