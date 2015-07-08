package com.android.formalchat.questionary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionaryPagerAdapter extends FragmentStatePagerAdapter implements AnswerReadyListener {
    private static final String PREFS_NAME = "FormalChatPrefs";

    private Context context;
    private Activity activity;

    public QuestionaryPagerAdapter(FragmentManager fragmentManager, Context ctx, Activity currActivity) {
        super(fragmentManager);
        context = ctx;
        activity = currActivity;
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Fragment getItem(int position) {
        QuestionFragment q = null;
        switch (position) {
            case 0:
                q = new QuestionFragmentMatchSmoking();
                break;
            case 1:
                q = new QuestionFragmentMatchDrinking();
                break;
            case 2:
                q = new QuestionFragmentMatchReligion();
                break;
            case 3:
                q = new QuestionFragmentMatchEthnicity();
                break;
            case 4:
                q = new QuestionFragmentYourSmoking();
                break;
            case 5:
                q = new QuestionFragmentYourDrinking();
                break;
            case 6:
                q = new QuestionFragmentYourReligion();
                break;
            case 7:
                q = new QuestionFragmentYourEthnicity();
                q.setIsLast(true);
        }
        q.setAnswerReadyListener(this);
        return q;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Question " + (position + 1) + " of " + getCount();
    }

    @Override
    public void onAnswerReady(QuestionFragment questionFragment) {

        int position = getItemPosition(questionFragment);
        if (position == getCount() - 1) {
            //todo done?
        } else {
            //todo move to next page?
        }
    }
}
