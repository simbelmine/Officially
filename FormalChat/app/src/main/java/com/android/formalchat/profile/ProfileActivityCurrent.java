package com.android.formalchat.profile;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.android.formalchat.R;
import com.android.formalchat.UserInfoActivity;
import com.android.formalchat.chat.ChatActivity;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfileActivityCurrent extends ProfileBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        play_profile_video_btn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void setOnClickListeners() {
        super.setOnClickListeners();
        edit_feb_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.feb_button:
                PopupMenu popupMenu = new PopupMenu(ProfileActivityCurrent.this, edit_feb_btn);
                popupMenu.getMenuInflater().inflate(R.menu.profile_edit_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onItemClicked(item);
                    }
                });
                popupMenu.show();
                break;
        }
    }

    private boolean onItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                setUserInfoToExtras();
                onStartActivity(UserInfoActivity.class, null, null);
                return true;
            case R.id.profile_remote:
                onStartActivity(ProfileActivityRemote.class, null, null);
                return true;
            case R.id.chat:
                onStartActivity(ChatActivity.class, null, null);
                return true;
            default:
                return false;
        }
    }


}
