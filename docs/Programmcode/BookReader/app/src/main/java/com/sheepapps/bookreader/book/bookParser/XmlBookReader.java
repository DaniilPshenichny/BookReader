package com.sheepapps.bookreader.book.bookParser;

import com.sheepapps.bookreader.book.common.CustomCharset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XmlBookReader extends BaseBookReader {
    private final Map<String, Integer> mTextTags;
    private final BookData m_bookData;
    private final String[] mChapterTags;

    private int[] mTagFlags;
    private long mBodyIndex;
    private long mChapterTagIndex;

    private String mTitle;
    private String mLinkTitle;
    private String mLinkHRef;
    private String mImageSrc;

    private long mClassMask;
    private int mOffset;

    public String getTitle() {
        return mTitle;
    }

    public String getLinkTitle() {
        return mLinkTitle;
    }

    public String getLinkHRef() {
        return mLinkHRef;
    }

    public String getImageSrc() {
        return mImageSrc;
    }

    public long getPosition() {
        return m_bookData.getPosition();
    }

    public int getOffset() {
        return mOffset;
    }

    public CustomCharset getCharset() {
        return m_bookData.getCharset();
    }

    public void setOffset(int value) {
        mOffset = value;
    }

    public BookData getBookData() {
        return m_bookData;
    }

    public XmlBookReader(BookData data, String title, Map<String, Integer> textTags, String[] chapterTags, boolean dirty) {
        super(dirty);
        mTitle = title;
        m_bookData = data;
        mChapterTags = chapterTags;

        mTextTags = textTags;
        reinitTags();
        init();
    }

    public void reinitTags() {
        String[] tags = m_bookData.getTags();
        mTagFlags = new int[tags.length];

        mBodyIndex = 0;
        mChapterTagIndex = 0;

        for (int i = 0; i < tags.length; i++) {
            Integer value = mTextTags.get(tags[i]);
            mTagFlags[i] = value == null ? 0 : value;

            if (tags[i].equals("body")) {
                mBodyIndex |= 1l << i;
            }

            for (int j = 0; j < mChapterTags.length; j++) {
                if (tags[i].equals(mChapterTags[j])) {
                    mChapterTagIndex |= 1l << i;
                }
            }
        }

        mMaxSize = m_bookData.getMaxPosition();
    }

    public void init() {
        if (mInited)
            return;

        try {
            mInited = true;
            reset(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            mFinished = true;
        }
    }

    private boolean processLine(BookLine line) {
        long mask = line.getTagMask();

        if (mask == 0 || mask == mChapterTagIndex || (mask & mBodyIndex) == 0) {
            if (mask == mChapterTagIndex && !line.isPart()) {
                mText = "\u00a0"; // &nbsp;
                mFlags = NEW_PAGE;
                return true;
            }
            return false;
        }

        int textFlag = retrieveFlags(mask);

        if (textFlag != 0) {
            if (line.getClassMask() != mClassMask) {
                mClassMask = line.getClassMask();
                updateClass(mClassMask);
            }

            mText = line.getText();
            mFlags = textFlag;

            if ((textFlag & LINK) != 0) {
                mLinkTitle = line.getAttribute("title");
                mLinkHRef = line.getAttribute("href");
                if (mLinkHRef == null) {
                    mLinkHRef = line.getAttribute("l:href");
                }

                if ((mText == null || mText.length() == 0)) {
                    mText = "*";
                }
            }

            if ((textFlag & IMAGE) != 0) {
                mImageSrc = line.getAttribute("src");
                if ((mImageSrc == null || mImageSrc.length() == 0))
                    mImageSrc = line.getAttribute("l:href");
                if ((mImageSrc == null || mImageSrc.length() == 0)) {
                    mImageSrc = line.getAttribute("xlink:href");
                }

                if (mImageSrc != null && mImageSrc.startsWith("#")) {
                    mImageSrc = mImageSrc.substring(1);
                }

                if ((mText == null || mText.length() == 0)) {
                    mText = line.getAttribute("alt");
                }
                if ((mText == null || mText.length() == 0)) {
                    mText = " ";
                }

                return true;
            }

            if (line.isPart()) {
                mFlags &= ~(NEW_LINE | NEW_PAGE);
            } else if (!line.isParentEmpty()) {
                if ((mFlags & NO_NEW_LINE) != 0)
                    mFlags &= ~NEW_LINE;

                if ((mFlags & NO_NEW_PAGE) != 0)
                    mFlags &= ~NEW_PAGE;
            }

            if (line.isRtl()) {
                mFlags |= RTL;
            }


            if ((mText == null || mText.length() == 0) && (mFlags & (NEW_LINE | NEW_PAGE)) != 0) {
                //Log.d("TextReader", "Replacing empty text with nbsp for tag " + tag);
                mText = "\u00a0"; // &nbsp;
                return true;
            }

            if (mText != null && mText.length() > 0) {
                return true;
            }
        }
        return false;
    }

    public void advance() {
        init();
        super.advance();
        mOffset = 0;

        while (m_bookData.advance()) {
            BookLine line = m_bookData.getCurrentLine();
            if (processLine(line))
                return;
        }

        mFlags = NEW_PAGE;
        mText = null;
        mFinished = true;
    }

    private void updateClass(long mask) {
        String[] classes = m_bookData.getClasses();
        List<String> nclasses = new ArrayList<String>(classes.length);
        for (int i = 0; i < classes.length; i++) {
            if ((mask & (1l << i)) != 0) {
                nclasses.add(classes[i]);
            }
        }
        mClassNames = nclasses.toArray(new String[nclasses.size()]);
    }

    public int retrieveFlags(long mask) {
        int result = 0;
        for (int i = 0; i < mTagFlags.length; i++)
            if (mTagFlags[i] != 0 && (mask & (1l << i)) != 0) {
                result |= mTagFlags[i];
            }
        return result;
    }

    public void reset(long position) {
        if (!mInited) {
            return;
        }
        if (m_bookData.getPosition() + mOffset == position) {
            return;

            mFlags = NEW_PAGE;
            mText = null;
            mFinished = false;
            mClassMask = 0;

            mOffset = m_bookData.setPosition((int) position);

            do {
                BookLine line = m_bookData.getCurrentLine();
                if (processLine(line))
                    break;
            } while (m_bookData.advance());

            //Log.d("TextReader", "Reset for " + position + " goes to pos " + m_bookData.getPosition() + ", offset " + mOffset);
        }

        public void gotoPercent ( float percent){
            reset((long) (percent * getMaxSize()));
        }

        public int seekBackwards ( long nposition, int value, int pageLines, int lineChars){
            reset(nposition);
            int currentLine = m_bookData.getLineIndex();
            int nbytes = mOffset;

            while (currentLine-- > 0) {
                BookLine line = m_bookData.getLine(currentLine);

                if ((line.getTagMask() & mBodyIndex) == 0)
                    continue;

                int textFlag = retrieveFlags(line.getTagMask());

                if (textFlag != 0) {
                    if (line.isPart())
                        textFlag &= ~(NEW_LINE | NEW_PAGE);

                    if ((textFlag & NO_NEW_PAGE) != 0)
                        textFlag &= ~NEW_PAGE;

                    CharSequence text = line.getText();

                    if (text != null) {
                        nbytes += text.length();
                    }

                    if ((textFlag & IMAGE) != 0) {
                        nbytes += lineChars * 2; // don't know image height
                    }

                    if (nbytes > 0 && (textFlag & NEW_PAGE) != 0) {
                        break;
                    }

                    if (nbytes >= value) {
                        break;
                    }
                }
            }

            if (currentLine < 0)
                currentLine = 0;

            m_bookData.setLineIndex(currentLine);
            mOffset = 0;
            mFlags = NEW_PAGE;
            mText = null;
            mFinished = false;
            mClassMask = 0;

            do {
                BookLine line = m_bookData.getCurrentLine();
                if (processLine(line))
                    break;
            } while (m_bookData.advance());


            return nbytes;
        }

        @Override
        public Object getData () {
            return getBookData();
        }

}