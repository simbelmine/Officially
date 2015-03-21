package com.android.formalchat;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

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

        switch (position) {
            case 0:
                return new TutorialFragmentFirst();
            case 1:
                return new TutorialFragmentSecond();
            case 2:
                return new TutorialFragmentThird();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
