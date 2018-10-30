package com.sheepapps.bookreader.library.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BookDao {

    @Query("SELECT * FROM book")
    List<Book> getAll();

    @Query("SELECT * FROM book WHERE file = :path")
    List<Book> getByName(String path);

    @Update
    void update(Book book);

    @Insert
    void insert(Book book);

}

