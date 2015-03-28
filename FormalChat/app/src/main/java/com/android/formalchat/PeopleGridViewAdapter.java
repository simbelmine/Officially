package com.android.formalchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Sve on 3/26/15.
 */
public class PeopleGridViewAdapter extends BaseAdapter {
    private Context context;

    public PeopleGridViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 20;
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
        if(convertView == null) {
            //imageView = new ImageView(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.ppl_grid_card, parent, false);
        }
        else {
            view = convertView;
        }


        return view;
    }
}
