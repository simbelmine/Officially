package com.android.formalchat.questionary;

import com.android.formalchat.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sve on 3/12/15.
 * Question 3 of 7
 */
public class QuestionFragmentMatchDrinking extends QuestionFragment {

    @Override
    protected String getQuestionTag() {
        return getResources().getString(R.string.tag_match_drinking);
    }

    @Override
    protected String putQuestionText() {
        return getResources().getString(R.string.q_match_drinking);
    }

    @Override
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_match_drinking));
    }

}