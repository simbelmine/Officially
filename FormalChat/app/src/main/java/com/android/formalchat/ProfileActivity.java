package com.android.formalchat;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfileActivity extends ProfileBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        help_video_layout.setVisibility(View.GONE);
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
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, edit_feb_btn);
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
            default:
                return false;
        }
    }


}
