package com.sheepapps.bookreader.pdf;

import android.arch.lifecycle.ViewModel;

import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.data.CurrentBook;

public class PdfFragmentViewModel extends ViewModel {

    private final String LAST_BOOK = "last_book";

    void saveCurrentPage(int currentPage) {
        BookReaderApp.getInstance().getSharedPreferences().edit()
                .putInt(BookReaderApp.getInstance().getSharedPreferences()
                        .getString(LAST_BOOK, null), currentPage).apply();
    }

    int getCurrentPage() {
        return BookReaderApp.getInstance().getSharedPreferences().getInt(BookReaderApp.getInstance()
                .getSharedPreferences().getString(LAST_BOOK, null), 0);
    }

}
