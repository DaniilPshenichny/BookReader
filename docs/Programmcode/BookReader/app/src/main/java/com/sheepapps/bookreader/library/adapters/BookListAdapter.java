package com.sheepapps.bookreader.library.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.sheepapps.bookreader.R;
import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.data.CurrentBook;
import com.sheepapps.bookreader.library.BookItemViewModel;
import com.sheepapps.bookreader.databinding.BookItemRawBinding;
import com.sheepapps.bookreader.library.data.Book;
import com.sheepapps.bookreader.databinding.BookItemRawBinding;
import java.util.List;

/*
    Класс BookListAdapter используется в качестве реализации Андроид-паттерна Adapter.
    В данном случае BookListAdapter адаптирует представление списка книг из вкладки
    Библиотека с данными из БД.
*/

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder>
        implements BookItemViewModel.OnBookItemClickedListener {

    private List<Book> data = BookReaderApp.getInstance().getDatabase().bookDao().getAll();

    class ViewHolder extends RecyclerView.ViewHolder {

        private BookItemRawBinding mBinding;
        private BookItemViewModel mViewModel;

        private ViewHolder(BookItemRawBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mViewModel = new BookItemViewModel(BookListAdapter.this);
            mBinding.setViewModel(mViewModel);
        }

        private void bind(int position) {
            mViewModel.setBookId(position);
            mViewModel.setBookName(data.get(position).name);
            mViewModel.setFilePath(data.get(position).file);
        }
    }

    @NonNull
    @Override
    public BookListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        BookItemRawBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.book_item_raw, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.bind(position);
    }

    @Override
    public void onItemClicked(int category) {
        CurrentBook.currentBook.setValue(data.get(category).file);
    }
}
