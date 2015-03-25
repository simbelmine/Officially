package com.android.formalchat;

import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sve on 3/12/15.
 * Question 6 of 7
 */
public class QuestionFragmentYourSmoking extends QuestionFragment {

    @Override
    protected int putLayoutId() {
        return R.layout.question_six;
    }

    @Override
    protected int putAnswersLayout() {
        return R.id.your_smoking_lyout;
    }

    @Override
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_your_smoking));
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
