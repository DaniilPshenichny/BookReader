package com.sheepapps.bookreader.book.bookParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class FontData implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final String mName;
	private final int mLength;
	private InputStream mStream;
	
	transient private String mFile;
	
	public String getName()
	{
		return mName;
	}

	public FontData(String name, InputStream stream, int length) {
		mStream = stream;
		mLength = length;
		
		if (name.contains("/"))
			mName = name.substring(name.lastIndexOf("/"));
		else
			mName = name;
	}	
	
	public String extractFont(String path) {
		if (mFile != null)
			return mFile;
		
		try {
			mFile = path + "/" + mName;
			File file = new File(mFile);
			if (file.exists() && file.length() == mLength) {
				mStream = null; // clean up
				return mFile;
			}
			
			if (mStream == null)
				return null;
			
			file.createNewFile();			
			
			FileOutputStream stream = new FileOutputStream(mFile);
			
			byte [] buffer = new byte[0x1000];
			int pos = 0;
			while (mStream.available() > 0 && pos < mLength) {
				int read = mStream.read(buffer, 0, buffer.length);
				stream.write(buffer, 0, read);
				pos += read;
			}
			stream.close();
			
			mStream = null; // clean up
			return mFile;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			mFile = null;
		}
		return null;
	}
}
