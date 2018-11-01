package com.sheepapps.bookreader.book.bookRenderer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

public class BookStyle {

    private Typeface mDefaultFont;
    private Typeface mBold;
    private Typeface mItalic;
    private Typeface mBoldItalic;
    private int mTextSize;
    private int mTextColor;
    private int mBackColor;
    private boolean mNightMode;
    private int[] mPaddings;
    private float mLineSpace;

    private BookStyle(BookStyleBuilder builder) {
        mDefaultFont = builder.mDefaultFont;
        mBold = builder.mBold;
        mItalic = builder.mItalic;
        mBoldItalic = builder.mBoldItalic;
        mTextSize = builder.mTextSize;
        mTextColor = builder.mTextColor;
        mBackColor = builder.mBackColor;
        mNightMode = builder.mNightMode;
        mPaddings = builder.mPaddings;
        mLineSpace = builder.mLineSpace;
    }

    public boolean getNightMode() {
        return mNightMode;
    }

    public Typeface getDefaultFont() {
        return mDefaultFont;
    }

    public Typeface getBold() {
        return mBold;
    }

    public Typeface getItalic() {
        return mItalic;
    }

    public Typeface getBoldItalic() {
        return mBoldItalic;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getBackColor() {
        return mBackColor;
    }

    public int[] getPaddings() {
        return mPaddings;
    }

    public float getLineSpace() {
        return mLineSpace;
    }

    public static class BookStyleBuilder {
        private Typeface mDefaultFont = Typeface.DEFAULT;
        private Typeface mBold = Typeface.DEFAULT_BOLD;
        private Typeface mItalic = Typeface.DEFAULT_BOLD;
        private Typeface mBoldItalic = Typeface.DEFAULT_BOLD;
        private int mTextSize = 20;
        private int mTextColor = Color.BLACK;
        private int mBackColor = Color.WHITE;
        private boolean mNightMode = false;
        private int[] mPaddings = new int[4];
        private float mLineSpace = 1f;

        public BookStyleBuilder setFontStyle(@NonNull Typeface typeface) {
            mDefaultFont = typeface;
            mBold = Typeface.create(typeface, Typeface.BOLD);
            mItalic = Typeface.create(typeface, Typeface.ITALIC);
            mBoldItalic = Typeface.create(typeface, Typeface.BOLD_ITALIC);
            return this;
        }

        public BookStyleBuilder setTextSize(int size) {
            mTextSize = size;
            return this;
        }

        public BookStyleBuilder setColors(int textColor, int backgroundColor, boolean nightMode) {
            mTextColor = textColor;
            mBackColor = backgroundColor;
            mNightMode = nightMode;
            return this;
        }

        public BookStyleBuilder setPaddings(int left, int top, int right, int bottom) {
            mPaddings[0] = left;
            mPaddings[1] = top;
            mPaddings[2] = right;
            mPaddings[3] = bottom;
            return this;
        }

        public BookStyleBuilder setPaddings(int horizontal, int vertical) {
            mPaddings[0] = horizontal;
            mPaddings[2] = horizontal;
            mPaddings[1] = vertical;
            mPaddings[3] = vertical;
            return this;
        }

        public BookStyleBuilder setLineSpace(float lineSpace) {
            mLineSpace = lineSpace;
            return this;
        }

        public BookStyle build() {
            return new BookStyle(this);
        }
    }
}
