package com.android.formalchat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Sve on 3/21/15.
 */
public abstract class QuestionFragment extends Fragment {

    private static final String PREFS_NAME = "FormalChatQuestionAnswers";
    protected QuestionaryPagerAdapter questionaryPagerAdapter;
    protected SharedPreferences sharedPreferences;

    protected boolean clicked;
    protected static int question;
    protected ViewPager viewPager;
    protected String answer;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(putLayoutId(), container, false);
        //question = 0;
        viewPager = (ViewPager) container;
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                question = position;
                Log.v("formalchat", "####### question pos = " + question);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        questionaryPagerAdapter = new QuestionaryPagerAdapter(getFragmentManager(), getActivity().getApplicationContext(), getActivity());
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        clicked = false;

        initButtons(rootView);

        final String answerFromPrefs = getAnswerFromSharedPrefs();
        if(answerFromPrefs != "") {
            putCorrectAnswerColor(answerFromPrefs, R.color.gray);
            clicked = true;
        }

        return rootView;
    }

    protected abstract int putLayoutId();

    protected abstract void initButtons(View rootView);

    protected void onClickAnswer(TextView answerTextView, String answer) {
        saveAnswerToParse(answer);
        changeColorOnClick(answerTextView);
        setAnswerToSharedPrefs(answer);
        goToNextQuestion();
    }

    protected void goToNextQuestion() {
        Log.v("formalchat","Question position ===== " + question);
        if(question != questionaryPagerAdapter.getCount()-1) {
            viewPager.setCurrentItem(question + 1);
        }
    }

    protected void setAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getSharedPreferencesQuestionId(), answer);
        editor.commit();
    }

    protected void changeColorOnClick(TextView answerTextView) {
        if(!clicked) {
            answerTextView.setBackgroundResource(R.color.gray);
            clicked = true;
        }
        else {
            putCorrectAnswerColor(getAnswerFromSharedPrefs(), R.color.light_blue);
            answerTextView.setBackgroundResource(R.color.gray);
        }
    }

    protected abstract String getSharedPreferencesQuestionId();

    protected void saveAnswerToParse(String answer) {
        questionaryPagerAdapter.updateQuestionary(question+1, answer);
    }

    protected String getAnswerFromSharedPrefs() {
        return sharedPreferences.getString(getSharedPreferencesQuestionId(), "");
    }

    protected abstract void putCorrectAnswerColor(String answerFromPrefs, int color);

}
