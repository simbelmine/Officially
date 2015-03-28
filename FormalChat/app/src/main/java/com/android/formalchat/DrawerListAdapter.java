package com.android.formalchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Sve on 3/27/15.
 */
public class DrawerListAdapter extends BaseAdapter {
    public static final String PREFS_NAME = "FormalChatPrefs";
    Context context;
    String[] drawerListMenu;
    Switch switchBtn;

    public DrawerListAdapter(Context context) {
        this.context = context;
        drawerListMenu = context.getResources().getStringArray(R.array.menu_list);
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

        TextView textView = (TextView) view.findViewById(R.id.drawertxt);
        textView.setText(drawerListMenu[position]);

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
