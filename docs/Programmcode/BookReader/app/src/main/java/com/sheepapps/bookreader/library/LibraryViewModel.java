package com.sheepapps.bookreader.library;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.book.bookParser.BaseBook;
import com.sheepapps.bookreader.book.bookParser.EpubBook;
import com.sheepapps.bookreader.book.bookParser.Fb2Book;
import com.sheepapps.bookreader.book.bookParser.HtmlBook;
import com.sheepapps.bookreader.data.CurrentBook;
import com.sheepapps.bookreader.library.data.Book;

public class LibraryViewModel extends ViewModel {

    private MutableLiveData<Boolean> mFileClickerDialogOpened = new MutableLiveData<>();

    public void onClickFab() {
        mFileClickerDialogOpened.postValue(true);
    }

    public MutableLiveData<Boolean> getClickData() {
        return mFileClickerDialogOpened;
    }

    public void setCurrentBook(String bookFile) {
        CurrentBook.currentBook.setValue(bookFile);
        Book book = new Book();
        book.file = bookFile;
        book.name = bookFile.substring(bookFile.lastIndexOf("/") + 1);
        if (!BookReaderApp.getInstance().getDatabase().bookDao().getAll().contains(book)) {
            BookReaderApp.getInstance().getDatabase().bookDao().insert(book);
        }
    }

}
