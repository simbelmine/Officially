package com.android.formalchat.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.android.formalchat.R;

/**
 * Created by Sve on 5/29/15.
 */
public class ProfileActivityRemote extends ProfileBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edit_feb_btn.setVisibility(View.GONE);
    }

    @Override
    protected void setOnClickListeners() {
        super.setOnClickListeners();
        got_it_img.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.got_it_img:
                ((FrameLayout) help_video_layout.getParent()).removeView(help_video_layout);
                help_video_layout.setVisibility(View.GONE);
                startVideo();
                break;
            case R.id.small_prof_pic:
                startVideo();
                break;
        }
    }
}
