package com.sheepapps.bookreader.book.bookParser;

import java.util.HashMap;

public class HtmlBookReader extends XmlBookReader {
	private final static HashMap<String, Integer> sXhtmlTags = new HashMap<String, Integer>();

	static {
		sXhtmlTags.put("p", JUSTIFY | NEW_LINE);
		sXhtmlTags.put("dd", JUSTIFY | NEW_LINE); // for books from samizdat
		sXhtmlTags.put("span", NEW_LINE);
		sXhtmlTags.put("h1", HEADER_1 | NEW_LINE);
		sXhtmlTags.put("h2", HEADER_2 | NEW_LINE);
		sXhtmlTags.put("h3", HEADER_3 | NEW_LINE);
		sXhtmlTags.put("h4", HEADER_4 | NEW_LINE);
		sXhtmlTags.put("i", ITALIC | NO_NEW_LINE | NO_NEW_PAGE);
		sXhtmlTags.put("b", BOLD | NO_NEW_LINE | NO_NEW_PAGE);
		sXhtmlTags.put("strong", BOLD | NO_NEW_LINE | NO_NEW_PAGE);
		sXhtmlTags.put("div", DIV | NEW_LINE);
		sXhtmlTags.put("br", NEW_LINE);
		sXhtmlTags.put("a", LINK | NO_NEW_LINE | NO_NEW_PAGE);
		sXhtmlTags.put("sup", SUPER | NO_NEW_LINE | NO_NEW_PAGE);
		sXhtmlTags.put("img", IMAGE);
		sXhtmlTags.put("image", IMAGE);
	}

	public HtmlBookReader(BookData data, String title, boolean dirty) {
		super(data, title, sXhtmlTags, new String[]{ "html" }, dirty);
	}
}
