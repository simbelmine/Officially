package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfilePagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<String> images = new ArrayList<>();

    public ProfilePagerAdapter(Context ctx, ArrayList<String> paths) {
        context = ctx;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images.addAll(paths);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void updateImages(List<String> paths) {
        this.images.clear();
        this.images.addAll(paths);
        this.notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {

        Log.v("formalchat", "instantiate item: " + position);

        View itemView = layoutInflater.inflate(R.layout.viewpager_item, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
        final ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

        Picasso.with(context).load(images.get(position)).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FullImageActivity.class);
                i.putExtra("url", images.get(position));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
