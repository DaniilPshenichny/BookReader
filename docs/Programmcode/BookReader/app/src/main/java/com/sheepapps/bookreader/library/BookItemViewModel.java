package com.sheepapps.bookreader.library;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;

public class BookItemViewModel extends BaseObservable {

    private ObservableField<String> bookName = new ObservableField<>();
    private ObservableField<String> filePath = new ObservableField<>();
    private OnBookItemClickedListener mListener;
    private int bookId;

    public BookItemViewModel(OnBookItemClickedListener listener) {
        mListener = listener;
    }

    public interface OnBookItemClickedListener {
        void onItemClicked(int category);
    }

    public void onClick() {
        if (mListener != null) {
            mListener.onItemClicked(bookId);
        }
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public void setBookName(String bookName) {
        this.bookName.set(bookName);
    }

    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

    public String getBookName() {
        return bookName.get();
    }

    public String getFilePath() {
        return filePath.get();
    }
}
