package com.android.formalchat.questionary;

import com.android.formalchat.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sve on 3/12/15.
 * Question 7 of 7
 */
public class QuestionFragmentYourDrinking extends QuestionFragment {

    @Override
    protected String getQuestionTag() {
        return getResources().getString(R.string.tag_your_drinking);
    }

    @Override
    protected String putQuestionText() {
        return getResources().getString(R.string.q_your_drinking);
    }

    @Override
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_your_drinking));
    }
}
