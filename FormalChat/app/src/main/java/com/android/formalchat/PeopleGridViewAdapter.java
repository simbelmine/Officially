package com.android.formalchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 3/26/15.
 */
public class PeopleGridViewAdapter extends BaseAdapter {
    private Context context;
    private List<ParseUser> usersList;

    public PeopleGridViewAdapter(Context context, List<ParseUser> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    public void updateUsers(List<ParseUser> users) {
        this.usersList = users;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ppl_grid_card, parent, false);
            viewHolder.profileImg = (RoundedImageView) convertView.findViewById(R.id.picture);
            viewHolder.userName = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        ParseUser user = usersList.get(position);
        if(user.containsKey("profileImg") && user.getParseFile("profileImg") != null) {
            Picasso.with(context).load(user.getParseFile("profileImg").getUrl()).into(viewHolder.profileImg);
        }
        viewHolder.userName.setText(user.get("username").toString());

        return convertView;
    }

    public static class ViewHolder {
        RoundedImageView profileImg;
        TextView userName;
    }
}
