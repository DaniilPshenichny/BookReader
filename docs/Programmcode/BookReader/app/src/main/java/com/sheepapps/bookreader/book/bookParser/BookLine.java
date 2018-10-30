package com.sheepapps.bookreader.book.bookParser;

import com.sheepapps.bookreader.book.common.ByteCharSequence;

public final class BookLine {

	public static final int PART = 1;
	public static final int RTL = 2;
	public static final int PARENT_EMPTY = 4;
	public static final int EMPTY = 8;
	
	private int mPosition;
	
	private long mTagMask;
	private long mClassMask;
	private byte mFlags;
	
	private CharSequence mAttributes;
	private CharSequence mText;
	
	public long getTagMask() {
		return mTagMask;
	}
	
	public void setTagMask(long value) {
		mTagMask = value;
	}
	
	public long getClassMask() {
		return mClassMask;
	}

	public CharSequence getText() {
		return mText;
	}

	public boolean isRtl() {
		return (mFlags & RTL) != 0;
	}

	public void setText(CharSequence value) {
		mText = value;
	}
	
	public void setAttributes(CharSequence value) {
		mAttributes = value;
	}
	
	public int getPosition() {
		return mPosition;
	}
	
	public boolean isPart() {
		return (mFlags & PART) != 0;
	}
	
	public boolean isParentEmpty() {
		return (mFlags & PARENT_EMPTY) != 0;
	}
	
	public boolean isEmpty() {
		return (mFlags & EMPTY) != 0;
	}
	
	public String getAttribute(String name) {
		if (mAttributes == null)
			return null;

		if (mAttributes.getClass() != String.class)
			mAttributes = mAttributes.toString();
		
		String attributes = (String) mAttributes;

		int nattribute = attributes.indexOf(name, 0);
		if (nattribute == -1)
			return null;

		int nquote = attributes.indexOf("\"", nattribute + 1);
		if (nquote == -1)
			return null;

		int equote = attributes.indexOf("\"", nquote + 1);
		if (equote == -1)
			return null;

		return attributes.subSequence(nquote + 1, equote).toString();
	}	
	
	public BookLine(long tagMask, CharSequence attributes, long classMask, CharSequence text, boolean rtl, int position, boolean parentEmpty) {
		mTagMask = tagMask;
		mAttributes = attributes != null && attributes.length() == 0 ? null : attributes;
		mText = text != null && text.length() == 0 ? null : text;
		mPosition = position;
		mClassMask = classMask;
		mFlags = 0;
		if (parentEmpty)
			mFlags |= PARENT_EMPTY;
		
		if (rtl)			
			mFlags |= RTL;
		
		if (text == null || text.length() == 0)			
			mFlags |= EMPTY;
	}
	
	public BookLine(BookLine parent, CharSequence text, int position) {
		mTagMask = parent.mTagMask;
		mClassMask = parent.mClassMask;
		mAttributes = parent.mAttributes;
		mText = text != null && text.length() == 0 ? null : text;
		mPosition = position;
		mFlags = PART;
		
		if (parent.isRtl())			
			mFlags |= RTL;
		
		if (text == null || text.length() == 0)			
			mFlags |= EMPTY;
	}

	public void optimize() {
		if (mAttributes != null) {
			if (mAttributes instanceof ByteCharSequence)
				mAttributes = ((ByteCharSequence) mAttributes).optimize();
		}
		if (mText != null) {
			if (mText instanceof ByteCharSequence)
				mText = ((ByteCharSequence) mText).optimize();
		}
	}
}