package com.sheepapps.bookreader.library;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.sheepapps.bookreader.R;
import com.sheepapps.bookreader.databinding.FragmentLibraryBinding;
import com.sheepapps.bookreader.library.adapters.BookListAdapter;
import java.io.File;

/*
    Класс BookListAdapter используется для отображения фрагмента библитотеки. Данный класс
    наследуется от стандартного android-sdk класса Fragment.
*/

public class LibraryFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_STORAGE = 228;

    FilePickerDialog mDialog;
    DialogProperties mProperties;
    LibraryViewModel mLibraryViewModel;

    public LibraryFragment() {
        // Required empty public constructor
    }

    public static LibraryFragment newInstance() {
        return new LibraryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProperties = new DialogProperties();
        mProperties.selection_mode = DialogConfigs.SINGLE_MODE;
        mProperties.selection_type = DialogConfigs.FILE_SELECT;
        mProperties.root = new File(DialogConfigs.DEFAULT_DIR);
        mProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        mProperties.offset = new File(DialogConfigs.DEFAULT_DIR);
        mProperties.extensions = new String[]{"fb2", "html", "xhtml", "htm", "pdf", "epub"};
        mLibraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        mLibraryViewModel.getClickData().observe(this, it -> {
            if (it != null && it) {
                if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)) {
                    openFilePickerDialog();
                } else {
                    requestStoragePermission(getActivity());
                }
            }
        });
    }

    private void openFilePickerDialog() {
        mDialog = new FilePickerDialog(getContext(), mProperties);
        mDialog.setTitle(getContext().getString(R.string.choose_file));
        mDialog.setDialogSelectionListener(files -> mLibraryViewModel.setCurrentBook(files[0]));
        mDialog.show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentLibraryBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_library, container, false);
        binding.setViewModel(mLibraryViewModel);
        binding.booksRecyclerView.setAdapter(new BookListAdapter());
        binding.booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestStoragePermission(Activity activity) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePickerDialog();
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
