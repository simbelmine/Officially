package com.android.formalchat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sve on 3/27/15.
 */
public class PeopleListViewAdapter extends BaseAdapter {
    private Context context;
    private List<ParseUser> usersList;

    public PeopleListViewAdapter(Context context, List<ParseUser> usersList) {
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
        if(convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ppl_list_item, parent, false);
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
