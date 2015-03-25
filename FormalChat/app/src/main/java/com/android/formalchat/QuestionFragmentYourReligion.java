package com.android.formalchat;

import android.view.View;

/**
 * Created by Sve on 3/12/15.
 * Question 5 of 7
 */
public class QuestionFragmentYourReligion extends QuestionFragment {

    @Override
    protected int putLayoutId() {
        return R.layout.question_five;
    }

    @Override
    protected int putAnswersLayout() {
        return R.id.your_religion_lyout;
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return null;
    }
}
