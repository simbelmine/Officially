package com.android.formalchat;

import android.view.View;

/**
 * Created by Sve on 3/12/15.
 * Question 7 of 7
 */
public class QuestionFragmentYourDrinking extends QuestionFragment {

    @Override
    protected int putLayoutId() {
        return R.layout.question_seven;
    }

    @Override
    protected int putAnswersLayout() {
        return R.id.your_drinking_lyout;
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
