package com.sheepapps.bookreader.pdf;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.barteksc.pdfviewer.PDFView;
import com.sheepapps.bookreader.R;

import java.io.File;

public class PdfFragment extends Fragment {

    private String mFilePath;
    private PDFView pdfViewer;
    private static final String FILE_PATH = "FILE_PATH";
    PdfFragmentViewModel viewModel;

    public PdfFragment() {
        // Required empty public constructor
    }

    public static PdfFragment newInstance(String filePath) {
        PdfFragment fragment = new PdfFragment();
        Bundle args = new Bundle();
        args.putString(FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFilePath = getArguments().getString(FILE_PATH);
        }
        viewModel = ViewModelProviders.of(this).get(PdfFragmentViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pdf, container, false);
        pdfViewer = root.findViewById(R.id.pdfViewer);
        setUpPdfViewer();
        return root;
    }

    private void setUpPdfViewer() {
        pdfViewer.fromFile(new File(mFilePath)).defaultPage(viewModel.getCurrentPage()).load();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!pdfViewer.isRecycled()) {
            viewModel.saveCurrentPage(pdfViewer.getCurrentPage());
        }
    }
}
