package com.sheepapps.bookreader.book.bookParser;

import android.content.res.XmlResourceParser;

import com.sheepapps.bookreader.book.common.ByteCharSequence;
import com.sheepapps.bookreader.book.common.CustomCharset;
import com.sheepapps.bookreader.book.common.XmlReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public final class XmlBookParser {
	private static final byte [] sClassBytes = { 'c','l','a','s','s' };
	private static final byte [] sDirBytes = { 'd','i','r' };
	
	private final List<BookLine> mLines = new LinkedList<BookLine>();
	private final StringIntCollection mClasses = new StringIntCollection();
	private final StringIntCollection mTags = new StringIntCollection();
	private CustomCharset mCharset;
	private int mPosition;
	
	public boolean isEmpty() {
		return mLines.size() == 0;
	}

	public int parse(XmlReader reader) {
		mCharset = reader.getCharset();
		
		Stack<BookLine> hierarchy = new Stack<BookLine>();
		int lineCount = 0;

		int eventType;
		try {
			eventType = reader.getEventType();

			while (eventType != XmlResourceParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlResourceParser.START_TAG: {
						ByteCharSequence tag = (ByteCharSequence)reader.getName();
						
						int position = reader.getPosition();
						CharSequence attributes = null;
						long tagMask = 0;
						long classMask = 0;
						boolean rtl = false; 
						BookLine parent = hierarchy.size() == 0 ? null : hierarchy.peek();
						
						if (tag.length() > 0 && tag.charAt(0) != '?' && tag.charAt(0) != '!') {
							ByteCharSequence nclass = (ByteCharSequence)reader.getAttribute(sClassBytes);
							ByteCharSequence dir = (ByteCharSequence)reader.getAttribute(sDirBytes);
							
							if (dir != null && dir.toString().equals("rtl"))
								rtl = true;
							
							attributes = reader.getAttributes();
							
							if (attributes != null && attributes.length() > 0) {
								int tcnt = 0;
								if (nclass != null)
									tcnt++;
								if (dir != null)
									tcnt++;								
									
								byte [] attrBytes = ((ByteCharSequence)attributes).getBytes();
								int offset = ((ByteCharSequence)attributes).getOffset();
								
								int cnt = 0;
								
								for(int i=0;i<attributes.length();i++)
									if (attrBytes[i + offset] == '=')
										cnt++;
								
								if (cnt == tcnt)
									attributes = null;
							}

							
							if (parent != null)
								classMask = parent.getClassMask(); 
							
							if (nclass != null && nclass.length() > 0) {
								byte [] classBytes = ((ByteCharSequence)nclass).getBytes();
								int offset = ((ByteCharSequence)nclass).getOffset();
								int i = nclass.length() - 1;
								int end = nclass.length();
								
								do {
									if (i == 0 || classBytes[i+offset] == ' ') {
										ByteCharSequence str = (ByteCharSequence)nclass.subSequence(i == 0 ? 0 : i + 1, end);
										
										/// !!!! HACK
										if (str.length() == 3 && str.toString().equals("rtl"))
										{
											rtl = true;}
										/// !!! HACK
										
										int pos = mClasses.get(str);
										
										if (pos == -1) {
											pos = mClasses.size();
											mClasses.put(str, pos);
										}
										
										classMask|= 1L<<pos;
										end = i;
									}
								}
								while(--i >= 0);
							}
							
							{
								int pos = mClasses.get(tag);
								
								if (pos == -1) {
									pos = mClasses.size();
									mClasses.put(tag, pos);
								}
								classMask|= 1L<<pos;
							}
							
							if (parent != null)
								tagMask = parent.getTagMask();
							
							{
								int pos = mTags.get(tag);
								
								if (pos == -1) {
									pos = mTags.size();
									mTags.put(tag, pos);
								}
								
								tagMask|= 1l<<pos;
							}
						}
						
						if (!rtl && parent != null && parent.isRtl())
							rtl = true;
						
						CharSequence text = reader.nextText();
						BookLine line = new BookLine(tagMask, attributes, classMask, text, rtl, mPosition + position, parent != null && parent.isEmpty());
						mLines.add(line);
						lineCount++;
						hierarchy.push(line);
						break;
					}
					case XmlResourceParser.END_TAG: {
						if (hierarchy.size() > 0)
							hierarchy.pop();
						break;
					}
					case XmlResourceParser.TEXT: {
						int position = reader.getPosition();
						CharSequence text = reader.nextText();
						if (hierarchy.size() > 0 && text != null && text.length() > 0) {
							boolean delimiters = true;
							byte [] textBytes = ((ByteCharSequence)text).getBytes();
							int offset = ((ByteCharSequence)text).getOffset();
							
							for (int i = 0; i < text.length(); i++) {
								switch(textBytes[i + offset]) {
									case ' ':
									case '\r':
									case '\n':
									case '\0':
									case '\t':
										break;
									default:
										delimiters = false;
										break;
								}
								if (!delimiters)
									break;
							}
							
							if (!delimiters) {
								BookLine line = new BookLine(hierarchy.peek(), text, mPosition + position);
								mLines.add(line);
								lineCount ++;
							}
							//Log.d("XmlBookParser", "Got part text " + text + ", tag " + line.getTagMask());
						}
							//Log.d("XmlBookParser", "Got part text " + text + ", hierarchy empty");
						break;
					}
				}
				eventType = reader.next();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
		
		if (lineCount == 0)
			return -1;
		
		int pos = mPosition;
		
		mPosition += reader.getMaxPosition();
		
		return pos;
	}
		
	public BookData bake() {
		String [] sclasses = mClasses.toArray(new String[mClasses.size()]);
		String [] stags = mTags.toArray(new String[mTags.size()]);
		BookLine [] slines = mLines.toArray(new BookLine[mLines.size()]);
				
		mLines.clear();
		mClasses.clear();
		mTags.clear();
		
		return new BookData(slines, stags, sclasses, mPosition, mCharset);
	}
	
	private class StringIntCollection {
		public class Chunk {
			public final ByteCharSequence String;
			public final int Value;
			
			public Chunk(ByteCharSequence str, int value) {
				String = str;
				Value = value;
			}
		}

		private Chunk[][] m_array;
		private int m_count;
		
		public int size()
		{
			return m_count;
		}
		
		public StringIntCollection() {
			m_array = new Chunk[26][];
			
			for (int i = 0; i < m_array.length; i++)
				m_array[i] = new Chunk[8];
		}
		
		public int get(ByteCharSequence key) {
			int firstChar = key.byteAt(0) - 97;
			
			if (firstChar < 0 || firstChar >= m_array.length - 1)
				firstChar = m_array.length - 1;
			
			Chunk[] array = m_array[firstChar];
			
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null)
					break;
				
				if (array[i].String.equals(key))
					return array[i].Value;
			}
			return -1;
		}
		
		public void put(ByteCharSequence key, int value) {
			int firstChar = key.byteAt(0) - 97;
			
			if (firstChar < 0 || firstChar >= m_array.length - 1)
				firstChar = m_array.length - 1;
			
			Chunk[] array = m_array[firstChar];
			m_count++;
			
			for(int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					array[i]= new Chunk(key, value);
					break;
				}
			}
		}
		
		public String[] toArray(String [] array) {
			for(int i = 0; i < m_array.length; i++) {
				for (int j = 0; j < m_array[i].length;j++) {
                    if (m_array[i][j] != null) {
                        array[m_array[i][j].Value] = m_array[i][j].String.toString();
                    }
                }
			}
			return array;
		}
		
		public void clear() {
			
		}
	}
}
