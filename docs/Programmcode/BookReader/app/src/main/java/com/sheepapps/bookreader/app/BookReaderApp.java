package com.sheepapps.bookreader.app;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;

import com.sheepapps.bookreader.library.data.BookDatabase;

public class BookReaderApp extends Application {

    private static BookReaderApp app;
    private final String prefsName = "sharedPrefs";
    private SharedPreferences sharedPreferences;
    private BookDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        sharedPreferences = getSharedPreferences(prefsName, MODE_PRIVATE);
        db = Room.databaseBuilder(this, BookDatabase.class, "bookdatabase")
                .allowMainThreadQueries()
                .build();
    }

    public static BookReaderApp getInstance() {
        return app;
    }
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
    public BookDatabase getDatabase() {
        return db;
    }
}

