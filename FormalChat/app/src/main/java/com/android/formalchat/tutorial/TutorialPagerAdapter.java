package com.android.formalchat.tutorial;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.android.formalchat.R;

/**
 * Created by Sve on 1/29/15.
 */
public class TutorialPagerAdapter extends FragmentStatePagerAdapter {
    protected Context context;

    public TutorialPagerAdapter(FragmentManager fragmentManager, Context ctx) {
        super(fragmentManager);
        context = ctx;
    }

    @Override
    public Fragment getItem(int position) {
        //Fragment fragment = new DemoFragment();
//        Bundle args = new Bundle();
//        args.putInt("page_position", position + 1);
        //fragment.setArguments(args);
        Log.v("formalchat", "........... Position = " + position);

        Fragment fragmentToStart;
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                fragmentToStart = new TutorialFragment();
                args.putInt("backgroundId", R.drawable.love1);
                args.putInt("textId", R.string.done);
                fragmentToStart.setArguments(args);
                return fragmentToStart;
            case 1:
                fragmentToStart = new TutorialFragment();
                args.putInt("backgroundId", R.drawable.love2);
                args.putInt("textId", R.string.done);
                fragmentToStart.setArguments(args);
                return fragmentToStart;
            case 2:
                fragmentToStart = new TutorialFragment();
                args.putInt("backgroundId", R.drawable.love3);
                args.putInt("textId", R.string.done);
                fragmentToStart.setArguments(args);
                return fragmentToStart;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
