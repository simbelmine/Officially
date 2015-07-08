package com.android.formalchat.questionary;

import com.android.formalchat.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sve on 7/8/15.
 */
public class QuestionFragmentMatchEthnicity extends QuestionFragment {

    @Override
    protected String getQuestionTag() {
        return getResources().getString(R.string.tag_match_ethnicity);
    }

    @Override
    protected String putQuestionText() {
        return getResources().getString(R.string.q_match_ethnicity);
    }

    @Override
    protected List<String> putAnswersList() {
        return Arrays.asList(getResources().getStringArray(R.array.a_match_ethnicity));
    }
}
