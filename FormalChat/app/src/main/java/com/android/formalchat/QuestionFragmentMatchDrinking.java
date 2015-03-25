package com.android.formalchat;

import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sve on 3/12/15.
 * Question 3 of 7
 */
public class QuestionFragmentMatchDrinking extends QuestionFragment {

    @Override
    protected int putLayoutId() {
        return R.layout.question_tree;
    }

    @Override
    protected int putAnswersLayout() {
        return R.id.match_drinking_lyout;
    }

    @Override
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_match_drinking));
    }

    @Override
    protected String getSharedPreferencesQuestionId(View rootView) {
        return rootView.findViewById(R.id.question).getTag().toString();
    }
}
