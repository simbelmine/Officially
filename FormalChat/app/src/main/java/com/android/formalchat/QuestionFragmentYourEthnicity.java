package com.android.formalchat;

import android.view.View;

import java.util.Arrays;
import java.util.List;

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
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_your_ethnicity));
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
