package com.sheepapps.bookreader.book.bookRenderer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.book.bookParser.BaseBookReader;
import com.sheepapps.bookreader.book.bookParser.BookLine;
import com.sheepapps.bookreader.book.bookParser.ImageData;
import com.sheepapps.bookreader.book.common.Dips;
import com.sheepapps.bookreader.book.common.FixedCharSequence;
import com.sheepapps.bookreader.book.common.Pair;
import com.sheepapps.bookreader.book.common.SortedList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReaderView extends View {

    private static final String[] s_superscriptNumbers = { " ¹", " ²", " ³", " \u2074" };

    private BaseBookReader mReader;
    private List<TextPage> mPages;
    private int mCurrentPage;
    private long mBookStart;

    private int mAveragePageSize;
    private int mAveragePages;
    private int mAverageTotal;
    private SortedList<Integer> mAveragePageSizes;

    private List<Pair<String, Float>> mChapters;
    private List<ImageData> mImages;
    private Map<String, List<BookLine>> mNotes;
    private Map<String, Integer> mStyles;

    private FontStyle mTextPaint;
    private FontStyle mSuperPaint;
    private FontStyle mBoldPaint;
    private FontStyle mBoldItalicPaint;
    private FontStyle mItalicPaint;
    private FontStyle mHeader1Paint;
    private FontStyle mHeader2Paint;
    private FontStyle mHeader3Paint;
    private FontStyle mHeader4Paint;
    private FontStyle mSubtitlePaint;

    private TextPaint mPagePaint = new TextPaint();
    private TextPaint mHeadPaint = new TextPaint();

    private int mPageNumber;
    private int mActualWidth;
    private int mActualHeight;

    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mMaxCachedPages;

    private boolean mInverse = false;
    private int mBackColor;
    private int mTextColor;

    private  float mDpiCompensation;

    private CharSequence mFooterTextLeft;
    private int mFooterTextOffset;

    public List<Pair<String, Float>> getChapters() {
        return mChapters;
    }

    public ReaderView(Context context) {
        super(context);
        initView();
    }

    public ReaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(21)
    public ReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setFocusable(false);
        setOnTouchListener((v, event) -> {
                int width = Dips.screenWidth();
                event.getFlags();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() > width / 2) {
                        nextPage(true, true);
                    } else {
                        prevPage();
                    }
                }
                return true;
            });
        mDpiCompensation = 1.0f;
        mTextPaint = new FontStyle(mHeadPaint, 1, 30, 1);

        this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mActualWidth = getWidth();
        mActualHeight = getHeight();

        mMaxCachedPages = 45;
        mPaddingLeft = 5;
        mPaddingRight = 5;
        mPaddingTop = 5;
        mPaddingBottom = 5;

        mPageNumber = 0;
        mAveragePageSizes = new SortedList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mDpiCompensation != 1f) {
            if (w > h)
                w = (int) (w / mDpiCompensation); // landscape
            else
                h = (int) (h / mDpiCompensation); // portrait
        }

        if ((mActualHeight != h || mActualWidth != w) && mCurrentPage != -1) {
            mActualWidth = w;
            mActualHeight = h;
            reset();
        } else {
            mActualWidth = w;
            mActualHeight = h;
            if (mCurrentPage == -1)
                nextPage(true, true);
        }
    }

    public void init(BaseBookReader reader, long position, int page, List<Pair<String, Float>> chapters, Map<String, Integer> styles, List<ImageData> images, Map<String, List<BookLine>> notes) {
        if (reader == null) {
            clear();
            return;
        }
        mChapters = chapters;
        mImages = images;
        mStyles = styles;
        mReader = reader;
        mNotes = notes;
        mBookStart = mReader.getPosition();
        mReader.reset(position);
        mFooterTextLeft = null;

        int offset = mReader.getOffset();
        if (offset < 10) {
            mReader.setOffset(0);
        }
        resetPages(false);
        mPageNumber = page;
        nextPage(true, true);
    }

    public void clear() {
        mChapters = null;
        mImages = null;
        mStyles = null;
        mReader = null;
        mNotes = null;
        mBookStart = 0;
        resetPages(false);
        mPageNumber = 0;
    }

    private void resetPages(boolean clearAverage) {
        if (mPages != null)
            for (int i = 0; i < mPages.size(); i++) {
                TextPage page = mPages.get(i);
                if (page != null)
                    page.clean();
            }

        mPages = new LinkedList<>();
        mCurrentPage = -1;

        if (clearAverage) {
            mAveragePages = 0;
            mAveragePageSize = 0;
            mAverageTotal = 0;
            mAveragePageSizes.clear();
        }
    }

    public void setNightMode(boolean nightModeIsOn) {
        changeSettings(nightModeIsOn);
    }

    public void changeSettings(boolean inverse) {
        mInverse = inverse;

        if (mTextPaint != null) {
            int textColor = mInverse ? mBackColor : mTextColor;

            mTextPaint.getPaint().setColor(textColor);
            mSuperPaint.getPaint().setColor(textColor);
            mBoldPaint.getPaint().setColor(textColor);
            mBoldItalicPaint.getPaint().setColor(textColor);
            mItalicPaint.getPaint().setColor(textColor);
            mHeader1Paint.getPaint().setColor(textColor);
            mHeader2Paint.getPaint().setColor(textColor);
            mHeader3Paint.getPaint().setColor(textColor);
            mHeader4Paint.getPaint().setColor(textColor);
            mSubtitlePaint.getPaint().setColor(textColor);
        }
    }

    public void reset() {
        if (mReader != null) {
            if (mPages != null && mPages.size() > 0 && mCurrentPage >= 0) {
                TextPage currentPage = mPages.get(mCurrentPage);
                mReader.reset(currentPage.getStartPosition());
                mFooterTextLeft = null;
                mPageNumber = currentPage.getPageNumber() - 1;
            }
        }
        resetPages(true);
    }

    public void initFonts(BookStyle style) {
        initFonts(style.getTextSize(),
                style.getDefaultFont(),
                style.getBold(),
                style.getItalic(),
                style.getBoldItalic(),
                style.getNightMode(),
                style.getTextColor(),
                style.getBackColor(),
                style.getLineSpace(),
                style.getPaddings()[0],
                style.getPaddings()[1],
                style.getPaddings()[2],
                style.getPaddings()[3]);
    }

    public void initFonts(int size, Typeface normalFont, Typeface boldFont, Typeface italicFont, Typeface boldItalicFont,
                          boolean inverse, int textColor, int backColor, float lineSpace,
                          int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {

        size = Dips.spToPx((int) (size * 0.75));
        float firstLine = 55 * 2;
        reset();

        mInverse = inverse;
        mBackColor = backColor;
        mTextColor = textColor;
        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
        mPaddingTop = paddingTop;
        mPaddingBottom = paddingBottom;

        TextPaint textPaint = new TextPaint();
        textPaint.setDither(false);
        textPaint.setLinearText(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(size);
        textPaint.setTypeface(normalFont);
        textPaint.setColor(mInverse ? mBackColor : mTextColor);

        textPaint.setStyle(Style.FILL);
        TextPaint superPaint = new TextPaint(textPaint);
        superPaint.setTextSize(size * 3 / 4);

        TextPaint italicPaint = new TextPaint(textPaint);
        italicPaint.setTypeface(italicFont);

        TextPaint boldPaint = new TextPaint(textPaint);
        boldPaint.setTypeface(boldFont);
        if (boldFont == normalFont || !boldFont.isBold())
            boldPaint.setFakeBoldText(true);

        TextPaint boldItalicPaint = new TextPaint(textPaint);
        boldItalicPaint.setTypeface(boldItalicFont);
        if (boldItalicFont == italicFont || !boldItalicFont.isBold())
            boldItalicPaint.setFakeBoldText(true);

        TextPaint header1Paint = new TextPaint(boldPaint);
        header1Paint.setTextSize(size + 8);

        TextPaint header2Paint = new TextPaint(boldPaint);
        header2Paint.setTextSize(size + 6);

        TextPaint header3Paint = new TextPaint(boldPaint);
        header3Paint.setTextSize(size + 4);

        TextPaint header4Paint = new TextPaint(textPaint);
        header4Paint.setTextSize(size + 2);

        TextPaint subtitlePaint = new TextPaint(italicPaint);
        subtitlePaint.setTextSize(size + 1);

        mPagePaint = new TextPaint();
        mPagePaint.setTextSize(Dips.spToPx(9));
        mPagePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        mPagePaint.setLinearText(true);
        mPagePaint.setAntiAlias(true);
        mPagePaint.setTextAlign(Align.RIGHT);
        mPagePaint.setColor(mTextColor);

        mTextPaint = new FontStyle(textPaint, 0.0f, lineSpace, firstLine);
        mSuperPaint = new FontStyle(superPaint, 0.0f, lineSpace, firstLine);
        mBoldPaint = new FontStyle(boldPaint, 0.0f, lineSpace, firstLine);
        mBoldItalicPaint = new FontStyle(boldItalicPaint, 0.0f, lineSpace, firstLine);
        mItalicPaint = new FontStyle(italicPaint, 0.0f, lineSpace + 0.05f, firstLine);
        mHeader1Paint = new FontStyle(header1Paint, 12.0f, lineSpace + 0.05f, 65.0f);
        mHeader2Paint = new FontStyle(header2Paint, 8.0f, lineSpace + 0.05f, 60.0f);
        mHeader3Paint = new FontStyle(header3Paint, 4.0f, lineSpace + 0.05f, 55.0f);
        mHeader4Paint = new FontStyle(header4Paint, 2.0f, lineSpace + 0.05f, 50.0f);
        mSubtitlePaint = new FontStyle(subtitlePaint, 0.0f, lineSpace + 0.05f, firstLine);
    }

    private void applyOrientation(Canvas canvas) {
        if (mDpiCompensation != 1.0f) {
            if (mActualWidth > mActualHeight) {
                canvas.scale(mDpiCompensation, 1.0f);
            } else {
                canvas.scale(1.0f, mDpiCompensation);
            }
        }
    }

    public void doInvalidate() {
        if (mTextPaint == null) {
            return;
        }
        if (mPages == null || mPages.size() == 0 || mCurrentPage == -1) {
            nextPage(false, true);
        }
        if (mPages != null && mPages.size() > 0) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mReader == null) {
            TextPaint tempPaint = new TextPaint();
            tempPaint.setAntiAlias(true);
            tempPaint.setTextSize(getWidth() < 300 ? 26 : 32);
            tempPaint.setTypeface(Typeface.SERIF);
            tempPaint.setTextAlign(Align.CENTER);
            tempPaint.setTextAlign(Align.RIGHT);
            tempPaint.setTextSize(getWidth() < 300 ? 22 : 26);
            return;
        }

        doDraw(canvas);
    }

    protected void doDraw(Canvas canvas) {
        if (mCurrentPage == -1)
            nextPage(true, true);

        if (mPages != null && mPages.size() > 0 && mCurrentPage != -1) {
            canvas.drawColor(mInverse ? mTextColor : mBackColor, PorterDuff.Mode.SRC);
            TextPage page = mPages.get(mCurrentPage);
            long start = SystemClock.elapsedRealtime();

            canvas.save();
            applyOrientation(canvas);
            Rect clip = canvas.getClipBounds();

            int top = mPaddingTop;
            int bottom = mActualHeight - mPaddingBottom;

            if (clip.bottom > top && clip.top < bottom) {
                page.draw(canvas, mPaddingLeft, top, mPagePaint);
            }

            canvas.restore();

            long elapsed = (SystemClock.elapsedRealtime() - start);
            if (elapsed > 400) {
                Log.d("TextReader", "Page draw took " + elapsed);
            }
        }
    }

    private TextPage preparePage(int width, int height, int virtualHeight, long stopAt, int pageNumber) {
        TextPage page = new TextPage(width, virtualHeight == 0 ? height : virtualHeight, 3, pageNumber);

        int lastFlags = 0;
        int lastStyleFlags = 0;
        int notesCount = 0;
        boolean footerBreak = false;

        while (!mReader.isFinished()) {
            int flags;
            int offset;
            long position;
            CharSequence nchars;

            if (!footerBreak && mFooterTextLeft != null) {
                flags = BaseBookReader.FOOTER | BaseBookReader.LINE_BREAK | BaseBookReader.JUSTIFY;
                nchars = mFooterTextLeft;
                offset = mFooterTextOffset;
                position = 0;
            } else {
                flags = mReader.getFlags();

                if (!page.isEmpty() && (flags & BaseBookReader.NEW_PAGE) != 0) {
                    break;
                }

                offset = mReader.getOffset();
                position = mReader.getPosition();

                if (stopAt > 0 && position + offset >= stopAt) {
                    page.finish(TextPage.PAGE_FULL, height, mPaddingTop);
                    return page;
                }

                nchars = mReader.getText();
                flags = mReader.getFlags();
            }

            if ((flags & BaseBookReader.LINK) != 0) {
                String href = mReader.getLinkHRef();
                if (href != null) {
                    if (href.startsWith("#") && mNotes.containsKey(href.substring(1))) {
                        Log.d("TextReader", "Time to insert note " + href);

                        String noteSymbol;

                        if (notesCount >= s_superscriptNumbers.length) {
                            noteSymbol = " *";
                            for (int h = 0; h < notesCount - s_superscriptNumbers.length; h++)
                                noteSymbol += "*";
                        } else {
                            noteSymbol = s_superscriptNumbers[notesCount];
                        }
                        notesCount++;

                        StringBuilder noteText = new StringBuilder();
                        noteText.append(noteSymbol);
                        for (BookLine line : mNotes.get(href.substring(1))) {
                            if (line.getText() != null) {
                                noteText.append(" ");
                                noteText.append(line.getText().toString());
                            }
                        }
                        nchars = noteText.toString();

                        page.addNonBreakText(noteSymbol);
                        flags = BaseBookReader.FOOTER | BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY;
                        lastFlags |= BaseBookReader.WORD_BREAK;
                        offset = 0;

                    } else {
                        String linkTitle = mReader.getLinkTitle();

                        if (linkTitle != null && linkTitle.length() > 2 && href != null && href.contains("#") && nchars.length() < 10) {
                            String noteSymbol;

                            if (notesCount >= s_superscriptNumbers.length) {
                                noteSymbol = " *";
                                for (int h = 0; h < notesCount - s_superscriptNumbers.length; h++)
                                    noteSymbol += "*";
                            } else {
                                noteSymbol = s_superscriptNumbers[notesCount];
                            }
                            notesCount++;

                            StringBuilder noteText = new StringBuilder(noteSymbol.length() + linkTitle.length() + 1);
                            noteText.append(noteSymbol);
                            noteText.append(" ");
                            noteText.append(linkTitle);

                            nchars = noteText.toString();

                            page.addNonBreakText(noteSymbol);

                            flags = BaseBookReader.FOOTER | BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY;
                            lastFlags |= BaseBookReader.WORD_BREAK;
                            offset = 0;
                        }
                    }
                } else {
                    if ((flags & BaseBookReader.SUPER) != 0) {
                        mReader.advance();
                        continue;
                    }
                    flags = lastFlags;
                }
            }

            if ((flags & BaseBookReader.IMAGE) != 0) {
                String imageSrc = mReader.getImageSrc();
                Log.d("TextReader", "Processing image " + imageSrc);

                boolean added = false;

                if (mImages != null) {
                    for (int i = 0; i < mImages.size(); i++) {
                        if (mImages.get(i).getName().equals(imageSrc)) {
                            if (!page.addImage(mImages.get(i), nchars, mTextPaint, position + offset)) {
                                page.finish(TextPage.PAGE_FULL, height, mPaddingTop);
                                return page;
                            }
                            added = true;
                            break;
                        }
                    }
                }

                if (!added) {
                    Log.d("TextReader", "Failed to add image " + imageSrc);
                } else {
                    Log.d("TextReader", "Added image " + imageSrc);
                    mReader.advance();
                    continue;
                }
            }

            if (nchars == null || nchars.length() == 0)
                break;

            String[] classes = mReader.getClassNames();

            if (classes != null) {
                for (int l = 0; l < classes.length; l++) {
                    Integer istyle = mStyles.get(classes[l]);
                    if (istyle != null)
                        flags |= istyle;
                }
            }

            FontStyle style = mTextPaint;

            if ((flags & BaseBookReader.HEADER_4) != 0) {
                style = mHeader4Paint;
            } else if ((flags & BaseBookReader.FOOTER) != 0) {
                style = mSuperPaint;
            } else if ((flags & BaseBookReader.HEADER_3) != 0) {
                style = mHeader3Paint;
            } else if ((flags & BaseBookReader.HEADER_2) != 0) {
                style = mHeader2Paint;
            } else if ((flags & BaseBookReader.HEADER_1) != 0) {
                style = mHeader1Paint;
            } else if ((flags & BaseBookReader.SUBTITLE) != 0) {
                style = mSubtitlePaint;
            } else if ((flags & BaseBookReader.BOLD) != 0) {
                if ((flags & BaseBookReader.ITALIC) != 0) {
                    style = mBoldItalicPaint;
                } else {
                    style = mBoldPaint;
                }
            } else if ((flags & BaseBookReader.ITALIC) != 0)
                style = mItalicPaint;

            if (lastStyleFlags != 0 && (lastStyleFlags & BaseBookReader.STYLE) != (flags & BaseBookReader.STYLE))
                flags |= BaseBookReader.NEW_LINE;

            if ((flags & BaseBookReader.NEW_LINE) != 0 && offset > 0)
                flags &= ~BaseBookReader.NEW_LINE;

            FixedCharSequence text = FixedCharSequence.toFixedCharSequence(nchars);
            int lastChar = text.length() - 1;

            boolean lineStart = (flags & BaseBookReader.NEW_LINE) != 0;
            boolean hasTab = false;
            int wordLength = 0;
            int wordStart = offset;

            for (int i = offset; i <= lastChar; i++) {
                char ch = text.charAt(i);
                boolean separator = false;
                boolean dash = false;

                switch (ch) {
                case ' ':
                    if (i > 0 && lineStart && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–')) {
                        text.setChar(i, '\u00a0'); // set nbsp
                        break;
                    }

                    separator = true;
                    break;
                case '\t':
                    if (lineStart) {
                        if (!mTextPaint.hasTab())
                            text.setChar(i, ' ');
                        hasTab = true;
                        break;
                    }
                    separator = true;
                    break;
                case '\n':
                case '\r':
                case '\0':
                    text.setChar(i, ' ');
                    if (lineStart) {
                        break;
                    }
                    separator = true;
                    break;
                case '-':
                case '\'':
                    if (mReader.isDirty()) {
                        if ((i > 0 && text.charAt(i - 1) == ' ') || (i != lastChar && text.charAt(i + 1) == ' ')) {
                            text.setChar(i, '—');
                        } else {
                            if ((i != lastChar && text.charAt(i + 1) == '-')) {
                                text.setChar(i, ' ');
                            } else {
                                dash = true;
                                separator = true;
                            }
                        }
                    } else {
                        if (!((i > 0 && text.charAt(i - 1) == ' ') || (i != lastChar && text.charAt(i + 1) == ' '))) {
                            dash = true;
                            separator = true;
                        }
                    }
                    break;
                case '\u00a0': // &nbsp;
                    if (lineStart) {
                        break;
                    }

                    if (i > 0 && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–')) {
                        break;
                    }
                    separator = true;
                    break;
                case '—':
                case '–':
                    break;
                default:
                    lineStart = false;
                    if (i != lastChar)
                        continue;
                    break;
                }

                if (separator || i == lastChar) {
                    lineStart = false;
                    wordLength = i - wordStart;
                    if (dash || (!separator && i == lastChar)) {
                        wordLength++;
                    }
                    if (wordLength > 0) {
                        FixedCharSequence word;

                        word = (FixedCharSequence) text.subSequence(wordStart, wordStart + wordLength);

                        if (hasTab && mTextPaint.getFirstLine() != 0) {
                            String str = word.toString();
                            if (str.contains(mTextPaint.hasTab() ? "\t  " : "   ")) {
                                mTextPaint.setFirstLine(0);
                                mItalicPaint.setFirstLine(0);
                                mBoldPaint.setFirstLine(0);
                                mBoldItalicPaint.setFirstLine(0);
                                mSubtitlePaint.setFirstLine(0);
                            }
                            word = new FixedCharSequence(str.replace("\t", "      "));
                        }

                        if ((flags & BaseBookReader.LINE_BREAK) != 0 || (lastFlags & BaseBookReader.LINE_BREAK) != 0) {
                            if ((flags & BaseBookReader.FOOTER) != 0 && mFooterTextLeft != null) {
                                flags &= ~(BaseBookReader.LINE_BREAK | BaseBookReader.WORD_BREAK);
                            } else {
                                flags &= ~(BaseBookReader.LINE_BREAK | BaseBookReader.WORD_BREAK);
                                flags |= BaseBookReader.NEW_LINE;
                            }
                        } else if (dash) {
                            flags |= BaseBookReader.WORD_BREAK;
                        }

                        if (separator && !dash)
                            flags &= ~BaseBookReader.WORD_BREAK;

                        if ((flags & BaseBookReader.FOOTER) == 0)
                            lastStyleFlags = flags;

                        int addResult = page.addWord(word, flags, style, position + wordStart);

                        if (addResult >= wordLength) {
                            flags &= ~(BaseBookReader.NEW_PAGE | BaseBookReader.NEW_LINE | BaseBookReader.WORD_BREAK);

                            if ((flags & BaseBookReader.FOOTER) != 0)
                                footerBreak = false;

                        } else if (addResult <= 0) {
                            if ((flags & BaseBookReader.FOOTER) != 0) {
                                mFooterTextLeft = text;
                                mFooterTextOffset = wordStart;
                                footerBreak = true;
                                if (wordStart > 0) {
                                    page.addNonBreakFooterText(" >");
                                }
                            } else {
                                mReader.setOffset(wordStart);
                            }

                            page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.PAGE_BREAK : TextPage.PAGE_BREAK, height, mPaddingTop);
                            return page;
                        } else {
                            if ((flags & BaseBookReader.FOOTER) != 0) {
                                mFooterTextLeft = text;
                                mFooterTextOffset = wordStart + addResult;
                                footerBreak = true;
                            } else
                                mReader.setOffset(wordStart + addResult);

                            page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.PAGE_BREAK : TextPage.PAGE_BREAK, height, mPaddingTop);
                            return page;
                        }
                    }
                    wordStart = i + 1;
                    hasTab = false;

                    if ((flags & BaseBookReader.FOOTER) == 0) {
                        if (stopAt > 0 && position + wordStart >= stopAt) {
                            if (position + wordStart != stopAt) {
                                page.trimLast();
                                wordStart = (int) (stopAt - position);
                            }

                            if (lastChar + 1 - wordStart > 0) {
                                mReader.setOffset(wordStart);
                                page.finish(TextPage.PAGE_BREAK, height, mPaddingTop);
                            } else
                                page.finish(TextPage.PAGE_FULL, height, mPaddingTop);
                            return page;
                        }
                    }
                }
            }

            if (!footerBreak && (flags & BaseBookReader.FOOTER) != 0)
                mFooterTextLeft = null;

            if ((flags & BaseBookReader.FOOTER) == 0) {
                lastFlags = flags & ~(BaseBookReader.NEW_PAGE | BaseBookReader.NEW_LINE | BaseBookReader.WORD_BREAK);
            }

            if (mFooterTextLeft == null)
                mReader.advance();
        }

        page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.NEW_PAGE : TextPage.NEW_PAGE, height, mPaddingTop);
        return page;
    }

    public void nextPage(boolean update, boolean cacheMore) {
        if (mPages == null)
            return;

        if (getWidth() == 0)
            return;

        if (mCurrentPage >= mPages.size() - 4)
            cachePage(mPages.size() != 0 && mReader.getPosition() == 0, cacheMore);

        if (mCurrentPage >= mPages.size() - 1)
            return;

        mCurrentPage++;
        setNightMode(true);

        if (update)
            doInvalidate();
    }

    private boolean cachePage(boolean delay, boolean cacheMore) {
        if (mReader == null || mReader.isFinished())
            return false;

        int height = mActualHeight;
        int width = mActualWidth;

        if (mPages.size() > 0) {
            TextPage prevPage = mPages.get(mPages.size() - 1);
            if (prevPage.getEndPosition() != mReader.getPosition()) {
                mReader.reset(prevPage.getEndPosition() + prevPage.getEndOffset());
                mFooterTextLeft = null;
            }
        }

        TextPage page = preparePage(width - mPaddingLeft - mPaddingRight, height - mPaddingTop - mPaddingBottom, 0, -1, ++mPageNumber);

        float lineChars = page.getAverageLineChars(5);

        long endPosition = mReader.getPosition();

        page.setEndPosition(endPosition);
        page.setEndOffset(mReader.getOffset());

        long startPos = mReader.getGlobalPosition(page.getStartPosition());
        long endPos = mReader.getGlobalPosition(page.getEndPosition());

        if (lineChars != 0) {
            int pageSize = (int) (endPos - startPos);

            int lineHeight = (int) (mTextPaint.getHeight() + 2 * mTextPaint.getHeightMod());
            int pageHeight = height - mPaddingTop - mPaddingBottom;
            int pageLines = pageHeight / lineHeight + 1;
            int maxSize = (int) (pageLines * lineChars * 1.2f);

            if (pageSize <= 0) {
                pageSize = maxSize;
            } else if (pageSize > maxSize) {
                pageSize = maxSize;
            }

            mAveragePageSizes.put(pageSize);

            int averageSize = mAveragePageSizes.getMedian();

            mAveragePageSize += pageSize;
            mAveragePages++;

            if (mAveragePages > 0 && ((mAveragePages - 1) % (mMaxCachedPages) == 0) && averageSize != 0) {
                int value = (int) (mReader.getMaxSize() / averageSize) + mChapters.size();

                float percent = mReader.getPercent(page.getStartPosition());

                int percentValue = Math.round(100 * page.getPageNumber() / percent);
                if (mAverageTotal == 0) {
                    if (percent > 10) {
                        mAverageTotal = percentValue;
                    } else {
                        mAverageTotal = value;
                    }
                } else {
                    if (percent > 10)
                        if (value > percentValue * 1.2 || value < percentValue / 0.8)
                            value = percentValue;

                    mAverageTotal = (mAverageTotal * 3 + value) / 4;
                }
            }
        }

        mPages.add(page);

        if (mPages.size() > mMaxCachedPages) {
            TextPage bpage = mPages.remove(0);
            if (bpage != null)
                bpage.clean();
            mCurrentPage--;
        }

        if (!mReader.isFinished() && mPages.size() - mCurrentPage < mMaxCachedPages && cacheMore) {
            if (delay)
                postDelayed(new CachePageRunnable(), 0);
            else
                cachePage(false, true);
        }
        return true;
    }

    public void prevPage() {
        if (getWidth() == 0)
            return;

        if (mCurrentPage <= 0) {
            if (mPages == null || mPages.size() == 0)
                return;

            int height = mActualHeight;
            int width = mActualWidth;

            TextPage page = mPages.get(0);
            long pagePos = page.getStartPosition();
            if (pagePos <= mBookStart)
                return;

            int pageLines = (int) ((height - mPaddingTop - mPaddingBottom) / (mTextPaint.getHeight() + mTextPaint.getHeightMod()) + 1);
            int lineChars = (int) ((width - mPaddingLeft - mPaddingRight) / mTextPaint.getDashWidth());
            int pageSize = pageLines * lineChars;
            int count = 0;
            do {
                pageSize = mReader.seekBackwards(pagePos, pageSize, pageLines, lineChars);

                int pageNumber = page.getPageNumber() - 1;
                if (pageNumber < 1)
                    pageNumber = 1;

                long startPos = mReader.getPosition();
                page = preparePage(width - mPaddingLeft - mPaddingRight, height - mPaddingTop - mPaddingBottom, 0x10000, pagePos, pageNumber);
                pagePos = startPos;
            } while (page.getStartPosition() == -1 && count++ < 5);

            long endPosition = mReader.getPosition();

            page.setEndPosition(endPosition);
            page.setEndOffset(mReader.getOffset());
            mPages.add(0, page);

            if (mPages.size() > 20)
                mPages.remove(mPages.size() - 1);

            mCurrentPage = 1;
        }

        setNightMode(true);
        mCurrentPage--;
        doInvalidate();
    }

    public void gotoPosition(long position) {
        if (mReader == null)
            return;
        mReader.reset(position);
        mFooterTextLeft = null;
        resetPages(false);
        if (mAveragePages == 0)
            mPageNumber = 0;
        else {
            mPageNumber = (int) (mReader.getGlobalPosition(mReader.getPosition()) * mAveragePages / mAveragePageSize) - 1;
            if (mPageNumber < 0)
                mPageNumber = 0;
        }
        nextPage(true, true);
    }

    public void gotoPage(int page, float percent) {
        if (page == getPageNumber())
            return;

        if (mReader == null)
            return;

        mReader.reset((long) (mReader.getMaxSize() * percent));
        resetPages(false);
        mPageNumber = page;
        nextPage(true, false);
    }

    public long getPosition() {
        if (mPages == null || mPages.size() == 0)
            return -1;

        if (mCurrentPage < 0 || mCurrentPage >= mPages.size())
            return -1;

        return mPages.get(mCurrentPage).getStartPosition();
    }

    public int getPageNumber() {
        if (mPages == null || mPages.size() == 0)
            return 0;

        if (mCurrentPage < 0 || mCurrentPage >= mPages.size())
            return 0;

        int result = mPages.get(mCurrentPage).getPageNumber() - 1;
        if (result < 1)
            return 1;

        return result;
    }

    public int getTotalPages() {
        if (mPages == null || mPages.size() == 0)
            return 0;

        if (mCurrentPage < 0 || mCurrentPage >= mPages.size())
            return 0;

        TextPage page = mPages.get(mCurrentPage);

        float percent = mReader.getPercent(page.getEndPosition());

        int height = mActualHeight;
        int width = mActualWidth;

        int pageNumber = page.getPageNumber();

        int total = mAverageTotal;

        if (total <= 0 && mReader != null) {
            int pageLines = (int) (height / (mTextPaint.getHeight() + mTextPaint.getHeightMod()) + 1);
            int lineChars = (int) (width / mTextPaint.getDashWidth());
            total = (int) mReader.getMaxSize() / (pageLines * lineChars) + mChapters.size();
        }

        if (pageNumber > total || percent >= 100)
            total = pageNumber;
        else if (pageNumber == total && percent < 100)
            total = pageNumber + 1;

        return total;
    }

    private class CachePageRunnable implements Runnable {
        @Override
        public void run() {
            if (!mReader.isFinished() && mPages.size() - mCurrentPage < mMaxCachedPages)
                cachePage(true, true);
        }
    }
}
