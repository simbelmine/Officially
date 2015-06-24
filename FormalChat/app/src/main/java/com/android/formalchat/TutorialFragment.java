package com.android.formalchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Sve on 6/23/15.
 */
public class TutorialFragment extends Fragment {
    private int fragmentBackgroundId;
    private int fragmentTextId;
    private FrameLayout fragmentLayout;
    private TextView fragmentTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getArgumentsFromBundle();
        View rootView = inflater.inflate(R.layout.tutorial_fragment, container, false);
        fragmentLayout = (FrameLayout) rootView.findViewById(R.id.layout_tutorial);
        fragmentTextView = (TextView) rootView.findViewById(R.id.text_tutorial);

        Log.v("formalchat", String.valueOf(fragmentBackgroundId));
        fragmentLayout.setBackgroundResource(fragmentBackgroundId);
        fragmentTextView.setText(getString(fragmentTextId));

        return rootView;
    }

    private void getArgumentsFromBundle() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            if(bundle.containsKey("backgroundId")) {
                fragmentBackgroundId = bundle.getInt("backgroundId", 0);
            }
            if(bundle.containsKey("textId")) {
                fragmentTextId = bundle.getInt("textId", 0);
            }
        }
    }
}
