package com.android.formalchat;

import android.view.View;

import java.util.Arrays;
import java.util.List;

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
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_your_religion));
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return null;
    }
}
