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
 * Created by Sve on 3/12/15.
 */
public class QuestionFragmentOne extends Fragment {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private QuestionaryPagerAdapter questionaryPagerAdapter;
    private SharedPreferences sharedPreferences;
    private String answer;
    private TextView skip;
    private TextView answerOne;
    private TextView answerTwo;
    private TextView answerTree;
    private boolean clicked;
    private int question;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.question_one, container, false);
        question = 0;
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

        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        answerOne = (TextView) rootView.findViewById(R.id.answer_one_one);
        answerTwo = (TextView) rootView.findViewById(R.id.answer_one_two);
        answerTree = (TextView) rootView.findViewById(R.id.answer_one_tree);

        final String answerFromPrefs = getAnswerFromSharedPrefs();
        if(answerFromPrefs != "") {
            putCorrectAnswerColor(answerFromPrefs, R.color.gray);
            clicked = true;
        }

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionaryPagerAdapter.skipQuestionary();
            }
        });

        answerOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "1";
                onClickAnswer(answerOne, answer);
            }
        });

        answerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "2";
                onClickAnswer(answerTwo, answer);
            }
        });

        answerTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "3";
                onClickAnswer(answerTree, answer);
            }
        });

        return rootView;
    }

    private void onClickAnswer(TextView answerTextView, String answer) {
        saveAnswerToParse(answer);
        changeColorOnClick(answerTextView);
        setAnswerToSharedPrefs(answer);
        goToNextQuestion();
    }

    private void goToNextQuestion() {
        viewPager.setCurrentItem(question+1);
    }

    private void changeColorOnClick(TextView answerTextView) {
        if(!clicked) {
            answerTextView.setBackgroundResource(R.color.gray);
            clicked = true;
        }
        else {
            putCorrectAnswerColor(getAnswerFromSharedPrefs(), R.color.light_blue);
            answerTextView.setBackgroundResource(R.color.gray);
        }
    }

    private void saveAnswerToParse(String answer) {
        questionaryPagerAdapter.updateQuestionary(question, answer);
    }

    private void setAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("questionOne", answer);
        editor.commit();
    }

    private void putCorrectAnswerColor(String answerFromPrefs, int color) {
        switch(answerFromPrefs) {
            case "1":
                answerOne.setBackgroundColor(getResources().getColor(color));
                break;
            case "2":
                answerTwo.setBackgroundColor(getResources().getColor(color));
                break;
            case "3":
                answerTree.setBackgroundColor(getResources().getColor(color));
                break;
        }
    }

    private String getAnswerFromSharedPrefs() {
        return sharedPreferences.getString("questionOne", "");
    }
}
