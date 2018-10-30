package com.sheepapps.bookreader.book.bookParser;

import com.sheepapps.bookreader.book.common.CustomCharset;

import java.util.List;

public class BookData {
	
	private final BookLine [] mLines;
	private final int mMaxPosition;
	private final String [] mClasses;
	private String [] mTags;
	private final CustomCharset mCharset;
	
	private int mLineIndex;
	private List<ParagraphData> mParagraphs;
	
	public BookLine getCurrentLine() {
		return mLines[mLineIndex];
	}
	
	public BookLine[] getLines() {
		return mLines;
	}
	
	public int getPosition() {
		return mLineIndex < 0 ? -1 : mLines[mLineIndex].getPosition();
	}
	
	public int setPosition(int position) {
		for (int i = 0; i < mLines.length; i++) {
			if (mLines[i].getPosition() >= position) {
				mLineIndex = i > 0 ? i - 1 : 0;
				int offset = position - mLines[mLineIndex].getPosition();
				if (offset < 0)
					offset = 0;
				return offset;
			}
		}
		
		mLineIndex = mLines.length - 1;
		return mMaxPosition - mLines[mLineIndex].getPosition();
	}
	
	public int getLineIndex() {
		return mLineIndex;
	}
	
	public void setLineIndex(int index) {
		mLineIndex = index;
	}
	
	public BookLine getLine(int lineIndex) {
		return mLines[lineIndex];
	}	
	
	public boolean advance() {
		if (mLineIndex >= mLines.length - 1)
			return false;
		mLineIndex++;
		return true;
	}
	
	public int getMaxPosition() {
		return mMaxPosition;
	}
	
	public String [] getClasses() {
		return mClasses;
	}
	
	public String [] getTags() {
		return mTags;
	}
	
	public void setTags(String [] value) {
		mTags = value;
	}
	
	public CustomCharset getCharset() {
		return mCharset;
	}

	public BookData(BookLine [] lines, String [] tags, String [] classes, int maxPosition, CustomCharset charset) {
		mLines = lines;
		mTags = tags;
		mClasses = classes;
		mLineIndex = -1;
		mMaxPosition = maxPosition;
		mCharset = charset;
	}

	public void setParagraphs(List<ParagraphData> paragraphs) {
		mParagraphs = paragraphs;
	}
	
	public int measureBookPages(float charHeight, float charsPerLine, int pageHeight) {
		int result = 0;
		if (mParagraphs != null) {
			int pageHeightUsed = 0;
			
			for (int i = 0; i < mParagraphs.size(); i++) {
				ParagraphData paragraph = mParagraphs.get(i);
				
				pageHeightUsed += paragraph.getHeight(charHeight, charsPerLine);
				
				if (pageHeightUsed > pageHeight) {
					result += pageHeightUsed / pageHeight;
					pageHeightUsed = pageHeightUsed % pageHeight;
				}
				
				if (pageHeightUsed > 0 && paragraph.isPageBreak()) {
					result++;
					pageHeightUsed = 0;
				}
			}
			if (pageHeightUsed > 0)
				result++;
		}
		return result;
	}
}
