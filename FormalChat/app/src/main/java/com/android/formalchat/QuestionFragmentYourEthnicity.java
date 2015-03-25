package com.android.formalchat;

import android.view.View;

/**
 * Created by Sve on 3/12/15.
 * Question 4 of 7
 */
public class QuestionFragmentYourEthnicity extends QuestionFragment {

    @Override
    protected int putLayoutId() {
        return R.layout.question_four;
    }

    @Override
    protected int putAnswersLayout() {
        return R.id.your_ethnicity_lyout;
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
