package com.sheepapps.bookreader.blank;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sheepapps.bookreader.R;

public class BlankBookFragment extends Fragment {

    public BlankBookFragment() {
        // Required empty public constructor
    }

    public static BlankBookFragment newInstance() {
        return new BlankBookFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current, container, false);
    }

}
