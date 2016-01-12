package com.android.formalchat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Sve on 1/8/16.
 */
public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
    public RoundedImageView profileImg;
    public TextView userName;
    public TextView userLocation;
    public TextView userAge;
    public ImageView zodiacSign;
    public ImageView onlineDot;
    public int position;

    public RecyclerViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        profileImg = (RoundedImageView) itemView.findViewById(R.id.picture);
        userName = (TextView) itemView.findViewById(R.id.text);
        userLocation = (TextView) itemView.findViewById(R.id.location);
        userAge = (TextView) itemView.findViewById(R.id.age);
        zodiacSign = (ImageView) itemView.findViewById(R.id.zodiac_sign);
        onlineDot = (ImageView) itemView.findViewById(R.id.online_dot);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), "Clicked Country Position = " + getPosition(), Toast.LENGTH_SHORT).show();
    }
}
