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
    protected String getQuestionTag() {
        return getResources().getString(R.string.tag_your_religion);
    }

    @Override
    protected String putQuestionText() {
        return getResources().getString(R.string.q_your_religion);
    }

    @Override
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_your_religion));
    }
}
