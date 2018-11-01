package com.sheepapps.bookreader.book.bookParser;

import com.sheepapps.bookreader.book.common.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseBook implements Serializable {
		
	protected Map<String, Integer> mStyles = new HashMap<String, Integer>();
	protected Map<String, List<BookLine>> mNotes = new HashMap<String, List<BookLine>>();
	protected List<Pair<Long,String>> mChapters = new ArrayList<Pair<Long,String>>(32);
	protected List<FontData> mFonts = new ArrayList<FontData>(2);
	protected List<ImageData> mImages = new ArrayList<ImageData>();
	protected float mFirstLineIdent = 55.0f;
	protected String mTitle;
	protected String mLanguage;
	protected boolean mInited;
	protected BaseBookReader mReader;
	protected boolean mCheckDir = false;
	
	public float getFirstLine() {
		return mFirstLineIdent;
	}

	public Map<String, Integer> getStyles() {
		return mStyles;
	}
	
	public List<Pair<Long,String>> getChapters() {
		return mChapters;
	}
	
	public List<FontData> getFonts() {
		return mFonts;
	}
	
	public List<ImageData> getImages() {
		return mImages;
	}
	
	public BaseBookReader getReader() {
		return mReader;
	}
	
	public Map<String, List<BookLine>> getNotes() {
		return mNotes;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	
	public boolean init(String cachePath) {
		mStyles = new HashMap<String, Integer>();
		mChapters = new ArrayList<Pair<Long,String>>(32);
		mFonts = new ArrayList<FontData>();
		mImages = new ArrayList<ImageData>();
		mFirstLineIdent = 55.0f;
		mInited = true;
		return true;
	}
	
	public void clean() {
		if (mImages != null)
		{
			for(ImageData image: mImages)
				image.clean();
					
		mImages = null;
		mFonts = null;
		mChapters = null;
		mNotes = null;
		mStyles = null;
		mReader = null;
	}}
	
	protected void checkLanguage(BookData data) {
		if (mLanguage == null) {
			for (int i = 0; i < 5; i++) {
				BookLine line = data.getLine(i);
				String lang = line.getAttribute("xml:lang");
				if (lang == null)
					lang = line.getAttribute("lang");

				if (lang != null) {
					mLanguage = lang;
					break;
				}
			}
		}
	}
}