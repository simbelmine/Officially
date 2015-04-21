package com.android.formalchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Sve on 3/27/15.
 */
public class DrawerListAdapter extends BaseAdapter {
    public static final String PREFS_NAME = "FormalChatPrefs";
    private static final String NONE_ICON = "none";
    Context context;
    String[] drawerListMenu;
    String[] drawerListIcons;
    Switch switchBtn;

    public DrawerListAdapter(Context context) {
        this.context = context;
        drawerListMenu = context.getResources().getStringArray(R.array.menu_list);
        drawerListIcons = context.getResources().getStringArray(R.array.menu_icon_list);
    }

    @Override
    public int getCount() {
        return drawerListMenu.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            view = inflater.inflate(R.layout.drawer_list_item, parent, false);
        }
        else {
            view = convertView;
        }

        setListItemText(view, position);
        setListIcons(view, position);


        if("Logged In".equals(drawerListMenu[position])) {
            initSwitch(view);
            switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!isChecked) {
                        ((DrawerActivity)context).logOut();
                    }
                }
            });
        }

        return view;
    }

    private void setListIcons(View view, int position) {
        ImageView icon = (ImageView) view.findViewById(R.id.drawer_list_icon);
        Drawable drawable;
        if(NONE_ICON.equals(drawerListIcons[position])) {
            drawable = new ColorDrawable(Color.TRANSPARENT);
        }
        else {
            String uri = "drawable/" + drawerListIcons[position];
            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
            drawable = context.getResources().getDrawable(imageResource);
        }
        icon.setImageDrawable(drawable);
    }

    private void setListItemText(View view, int position) {
        TextView textView = (TextView) view.findViewById(R.id.drawertxt);
        textView.setText(drawerListMenu[position]);
    }

    private void initSwitch(View view) {
        switchBtn = (Switch) view.findViewById(R.id.online_switch);
        switchBtn.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        if (sharedPreferences.getBoolean("loggedIn", false)) {
            switchBtn.setChecked(true);
        }
        else {
            switchBtn.setChecked(false);
        }
    }
}
