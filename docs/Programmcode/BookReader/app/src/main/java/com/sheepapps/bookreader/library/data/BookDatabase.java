package com.sheepapps.bookreader.library.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Book.class}, version = 2)
public abstract class BookDatabase extends RoomDatabase {
    public abstract BookDao bookDao();
}
