package com.android.formalchat.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.android.formalchat.ApplicationOfficially;
import com.android.formalchat.R;
import com.android.formalchat.chat.ChatActivity;

/**
 * Created by Sve on 5/29/15.
 */
public class ProfileActivityRemote extends ProfileBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edit_feb_btn.setVisibility(View.GONE);
        chat_feb_button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setOnClickListeners() {
        super.setOnClickListeners();
        play_profile_video_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(((ApplicationOfficially)getApplication()).isNetworkAvailable()) {
            super.onClick(v);
            switch (v.getId()) {
                case R.id.play_profile_video_btn:
                    startVideo();
                    break;
                case R.id.small_prof_pic:
                    startVideo();
                    break;
                case R.id.feb_chat_button:
                    Intent intent = new Intent(ProfileActivityRemote.this, ChatActivity.class);
                    intent.putExtra("username_remote", userName);
                    startActivity(intent);
                    break;
            }
        }
        else {
            ((ApplicationOfficially)getApplication()).getSnackbar(this, R.string.no_network, R.color.alert_red).show();
        }
    }
}
