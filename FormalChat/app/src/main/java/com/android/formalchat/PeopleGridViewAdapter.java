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
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        RoundedImageView profileImg;
        TextView userName;

        if(convertView == null) {
            //imageView = new ImageView(context);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.ppl_grid_card, parent, false);
            profileImg = (RoundedImageView) view.findViewById(R.id.picture);
            userName = (TextView) view.findViewById(R.id.text);

            ParseUser user = usersList.get(position);
            if(user.containsKey("profileImg") && user.getParseFile("profileImg") != null) {
                Picasso.with(context).load(user.getParseFile("profileImg").getUrl()).into(profileImg);
            }
            userName.setText(user.get("username").toString());
        }
        else {
            view = convertView;
        }


        return view;
    }

}
