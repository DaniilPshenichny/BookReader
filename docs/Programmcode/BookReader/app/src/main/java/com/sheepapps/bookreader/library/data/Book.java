package com.sheepapps.bookreader.library.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Book {
    public long id;

    @NonNull
    @PrimaryKey
    public String file;
    public String name;
    public int page;
    public long position;

    @Override
    public boolean equals(Object obj) {
        return ( obj.getClass() == Book.class && this.file.equals(((Book)obj).file));
    }
}
