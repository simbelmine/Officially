package com.android.formalchat;

import android.view.View;

/**
 * Created by Sve on 3/12/15.
 * Question 1 of 7
 */
public class QuestionFragmentMatchReligion extends QuestionFragment {

    @Override
    protected int putLayoutId() {
        return R.layout.question_one;
    }

    @Override
    protected int putAnswersLayout() {
        return R.id.match_religion_lyout;
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
