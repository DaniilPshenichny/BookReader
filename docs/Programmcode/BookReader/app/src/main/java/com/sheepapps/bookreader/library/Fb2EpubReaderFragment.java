package com.sheepapps.bookreader.library;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sheepapps.bookreader.R;
import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.book.bookParser.BaseBook;
import com.sheepapps.bookreader.book.bookParser.EpubBook;
import com.sheepapps.bookreader.book.bookParser.Fb2Book;
import com.sheepapps.bookreader.book.bookParser.HtmlBook;
import com.sheepapps.bookreader.book.bookRenderer.BookStyle;
import com.sheepapps.bookreader.book.common.Pair;
import com.sheepapps.bookreader.book.bookRenderer.ReaderView;
import com.sheepapps.bookreader.library.data.Book;
import com.sheepapps.bookreader.library.data.BookDao;
import java.util.ArrayList;
import java.util.List;

public class Fb2EpubReaderFragment extends Fragment {

    private String mFilePath;
    private ReaderView mReaderView;
    private BookDao mBookDao;

    public Fb2EpubReaderFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new Fb2EpubReaderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilePath = BookReaderApp.getInstance().getSharedPreferences().getString("last_book", null);
        mBookDao = BookReaderApp.getInstance().getDatabase().bookDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fb2_epub_reader, container, false);
        mReaderView = root.findViewById(R.id.reader_view);
        initReaderView();
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        List<Book> books = mBookDao.getByName(mFilePath);
        if (books.size() > 0) {
            Book book = books.get(0);
            book.page = mReaderView.getPageNumber();
            book.position = mReaderView.getPosition();
            mBookDao.update(book);
        }
    }

    private void initReaderView() {
        BaseBook book = initBook();
        initReaderViewStyle();
        initReaderViewBook(book);
    }

    private void initReaderViewStyle() {
        BookStyle.BookStyleBuilder builder = new BookStyle.BookStyleBuilder();
        builder.setFontStyle(Typeface.DEFAULT)
                .setColors(getResources().getColor(R.color.colorBackground),
                        getResources().getColor(R.color.colorText), false)
                .setTextSize(20)
                .setLineSpace(1f)
                .setPaddings(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                        getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        BookStyle style = builder.build();
        mReaderView.initFonts(style);
    }

    private BaseBook initBook() {
        BaseBook book = new Fb2Book("null");
        if (mFilePath.endsWith(".epub")) {
            book = new EpubBook(mFilePath);
        } else if (mFilePath.endsWith(".fb2") || mFilePath.endsWith(".fb2.zip")) {
            book = new Fb2Book(mFilePath);
        } else if (mFilePath.endsWith(".xhtml") || mFilePath.endsWith(".html") || mFilePath.endsWith(".htm")) {
            book = new HtmlBook(mFilePath);
        }
        book.init(BookReaderApp.getInstance().getCacheDir().getPath());
        return book;
    }

    private void initReaderViewBook(BaseBook book) {
        List<Pair<String, Float>> chapterPositions = new ArrayList<>(book.getChapters().size());
        try {
            for (Pair<Long, String> chapter : book.getChapters()) {
                long cpos = chapter.first;
                float percent = book.getReader().getPercent(cpos);
                chapterPositions.add(new Pair<>(chapter.second, percent));
            }
        } catch (Exception ex) {
            Log.e("BookReader", "Error parsing chapters " + ex);
        }

        List<Book> books = mBookDao.getByName(mFilePath);
        if (books.size() > 0) {
            int page = books.get(0).page;
            long pos = books.get(0).position;
            mReaderView.init(book.getReader(), pos, page, chapterPositions,
                    book.getStyles(), book.getImages(), book.getNotes());
        } else {
            mReaderView.init(book.getReader(), 0, 0, chapterPositions,
                    book.getStyles(), book.getImages(), book.getNotes());
        }
    }
}
