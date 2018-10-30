package com.sheepapps.bookreader.book.bookParser;

public class ParagraphData {
	private int mCharacters;
	private final int mImageHeight;
	private float mModifier;
	
	private boolean mPageBreak;
	
	public boolean isImage()
	{
		return mImageHeight != 0;
	}
	
	public float getModifier()
	{
		return mModifier;
	}
	
	public boolean isPageBreak()
	{
		return mPageBreak;
	}
	
	public int getCharacters()
	{
		return mCharacters;
	}
	
	public ParagraphData(int characters, float modifier, boolean pageBreak) {
		mImageHeight = 0;
		mCharacters = characters;
		mModifier = modifier;
		mPageBreak = pageBreak;
	}
	
	public ParagraphData(int imageHeight) {
		mImageHeight = imageHeight;
		mCharacters = 0;
		mModifier = 0.0f;
	}

	public boolean addParagraph(ParagraphData another) {
		if (mImageHeight != 0)
			return false;
		
		if (mModifier != another.getModifier())
			return false;
		
		if (another.isPageBreak())
			mPageBreak = true;
					
		mCharacters += another.getCharacters();
		
		return true;
	}
	
	public int getHeight(float characterHeight, float charsPerLine) {
		if (mImageHeight != 0)
			return mImageHeight;
		
		return (int)((mCharacters / charsPerLine + 1) * (characterHeight * mModifier));
	}
}
