package com.android.formalchat;

import android.view.View;

import java.util.Arrays;
import java.util.List;

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
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_your_drinking));
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
