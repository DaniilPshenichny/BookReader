package com.sheepapps.bookreader.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.sheepapps.bookreader.R;
import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.data.CurrentBook;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<Integer> mItemId;
    private final String LAST_BOOK = "last_book";

    @NonNull
    public MutableLiveData<Integer> getItemId() {
        if (mItemId == null) {
            mItemId = new MutableLiveData<>();
        }
        return mItemId;
    }

    public void setItemId(int id) {
        mItemId.setValue(id);
    }

    public void saveLastBook() {
        BookReaderApp.getInstance().getSharedPreferences().edit()
                .putString(LAST_BOOK, CurrentBook.currentBook.getValue()).apply();
    }

    public String getLastBook() {
        return BookReaderApp.getInstance().getSharedPreferences().getString(LAST_BOOK, null);
    }
}
