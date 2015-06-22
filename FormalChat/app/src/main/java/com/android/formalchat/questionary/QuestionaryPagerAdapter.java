package com.android.formalchat.questionary;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.widget.Toast;

import com.android.formalchat.MainActivity;
import com.android.formalchat.UserQuestionary;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return 7;
    }

    @Override
    public Fragment getItem(int position) {
        QuestionFragment q = null;
        switch (position) {
            case 0:
                q = new QuestionFragmentMatchReligion();
                break;
            case 1:
                q = new QuestionFragmentMatchSmoking();
                break;
            case 2:
                q = new QuestionFragmentMatchDrinking();
                break;
            case 3:
                q = new QuestionFragmentYourEthnicity();
                break;
            case 4:
                q = new QuestionFragmentYourReligion();
                break;
            case 5:
                q = new QuestionFragmentYourSmoking();
                break;
            case 6:
                q = new QuestionFragmentYourDrinking();
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

// TO DO : is last question and then show Done into Action Bar
}
