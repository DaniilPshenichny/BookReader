package com.sheepapps.bookreader.book.bookParser;

import java.util.HashMap;

public class Fb2BookReader extends XmlBookReader {
	private final static HashMap<String, Integer> sFb2Tags = new HashMap<String, Integer>();

	static {
		sFb2Tags.put("p", JUSTIFY | NEW_LINE | NO_NEW_PAGE);
		sFb2Tags.put("v",  JUSTIFY | NEW_LINE);
		sFb2Tags.put("title", HEADER_1 | NEW_LINE);
		sFb2Tags.put("subtitle", HEADER_2 | NEW_LINE);
		sFb2Tags.put("epigraph", SUBTITLE);
		sFb2Tags.put("cite", SUBTITLE);
		sFb2Tags.put("emphasis", ITALIC | NO_NEW_LINE | NO_NEW_PAGE);
		sFb2Tags.put("strong", BOLD | NO_NEW_LINE | NO_NEW_PAGE);
		sFb2Tags.put("a", LINK | NORMAL | NO_NEW_LINE | NO_NEW_PAGE);
		sFb2Tags.put("empty-line", NEW_LINE);
		sFb2Tags.put("text-author", BOLD | NEW_LINE);
		sFb2Tags.put("image", IMAGE);
	}
	
	public Fb2BookReader(BookData data, String title) {
		super(data, title, sFb2Tags, new String[]{"fictionbook", "body", "section"}, true);
	}	
}
