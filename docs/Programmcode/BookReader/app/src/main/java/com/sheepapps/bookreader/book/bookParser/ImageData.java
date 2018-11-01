package com.sheepapps.bookreader.book.bookParser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;

public class ImageData implements Serializable {
	private static final long serialVersionUID = 2L;

	private final String mName;
	private byte[] mData;
	private final int mOffset;
	private final int mLength;
	private InputStream mStream;
	
	transient private int mWidth;
	transient private int mHeight;
	transient private SoftReference<Bitmap> mBitmap;
	transient private boolean mInited;
	
	private String mCacheFile;

	public String getName() {
		return mName;
	}

	public byte[] getData() {
		return mData;
	}
	
	public int getWidth() {
		if (!mInited)
			init(null);
		return mWidth;
	}

	public int getHeight() {
		if (!mInited)
			init(null);
		return mHeight;
	}
	
	public ImageData(String name, InputStream stream, int length) {
		mStream = stream;
		mOffset = 0;
		mLength = length;

		mName = name;
	}

	public ImageData(String name, byte [] data, int offset, int length) {
		mData = data;
		mOffset = offset;
		mLength = length;
		mName = name;
	}
	
	public void init(String cachePath) {
		if (mInited)
			return;
		
		if (cachePath != null) {
			cacheFile(cachePath);
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		if (mCacheFile != null)
			BitmapFactory.decodeFile(mCacheFile, options);
		else
			BitmapFactory.decodeByteArray(mData, mOffset, mLength, options);
		
		mWidth = options.outWidth;
		mHeight = options.outHeight;
		mInited = true;
	}

	private void cacheFile(String cachePath) {
		try {
			int ppos = mName.lastIndexOf("/");
			if (ppos == -1)
				mCacheFile = cachePath + "/" + mName;
			else
				mCacheFile = cachePath + "/" + mName.substring(ppos + 1);
			
			mCacheFile += mLength;
			
			File file = new File(mCacheFile);
			if (file.exists() && file.length() == mLength) {
				Log.d("TextReader", "Using existing image file: " + mCacheFile);
				mData = null; // clean up
				mStream = null;
				return;
			}
						
			if (mData == null) {
				if (mStream == null)
					return;
				
				mData = new byte[mLength];
				int pos = 0;
				
				while (mStream.available() > 0 && pos < mLength)
					pos += mStream.read(mData, pos, mLength - pos);
				
				mStream = null;
			}
			
			file.createNewFile();
			
			FileOutputStream stream = new FileOutputStream(mCacheFile);
			stream.write(mData, mOffset, mLength);
			stream.close();
			
			Log.d("TextReader", "Cached image to file " + mCacheFile);
			
			mData = null;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			mCacheFile = null;
		}
	}
	
	public Bitmap extractImage(int maxWidth, int maxHeight) {
		Bitmap result = mBitmap != null ? mBitmap.get() : null;
		
		if (result != null && !result.isRecycled()){
			return result;}

		try {
			init(null);
			BitmapFactory.Options options = new BitmapFactory.Options();
			
			int width = mWidth;
			int height = mHeight;
			options.inSampleSize = 1;
			options.inPreferredConfig = /*BaseActivity.isNook || BaseActivity.isNookTouch || BaseActivity.isEmulator ? Bitmap.Config.ALPHA_8 : */Bitmap.Config.RGB_565;
			
			while (width / 2 >= maxWidth && height / 2 >= maxHeight) {
				options.inSampleSize *= 2;
				width /= 2;
				height /= 2;
			}
			if (options.inSampleSize != 1){
				Log.d("TextReader", "Image size is " + mWidth + ", " + mHeight +
						", while needed size is " + maxWidth + ", " + maxHeight +
                        ", using sample size " + options.inSampleSize);}
			
			if (mCacheFile != null && mData == null) {
				try {
					result = BitmapFactory.decodeFile(mCacheFile, options);
				}
				catch (OutOfMemoryError ex) {
					System.gc();
					result = BitmapFactory.decodeFile(mCacheFile, options);
				}
			} else {
				if (mData == null && mStream != null) {
					mData = new byte[mLength];
					int pos = 0;
					
					while (mStream.available() > 0 && pos < mLength)
						pos += mStream.read(mData, pos, mLength - pos);
					
					mStream = null;
				}
				
				if (mData != null)
					result = BitmapFactory.decodeByteArray(mData, mOffset, mLength, options);
			}
			if (result != null && !result.isRecycled()) {
				mBitmap = new SoftReference<Bitmap>(result);
			} else {
                mBitmap = null;
            }
			return result;
		}
		catch (OutOfMemoryError ex) {
			ex.printStackTrace();
			mBitmap = null;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			mBitmap = null;
		}
		return null;
	}
	
	public void clean() {
		Bitmap result = mBitmap != null ? mBitmap.get() : null;
		
		if (result != null && !result.isRecycled()){
			result.recycle();}
		
		mData = null;
	}

}
