package com.android.formalchat;

import android.view.View;
import android.widget.TextView;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionFragmentMatchReligion extends QuestionFragment {
    private TextView skip;
    private TextView answerOne;
    private TextView answerTwo;
    private TextView answerTree;

    @Override
    protected int putLayoutId() {
        return R.layout.question_one;
    }

    @Override
    protected void initButtons(View rootView) {
        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        answerOne = (TextView) rootView.findViewById(R.id.answer_one_one);
        answerTwo = (TextView) rootView.findViewById(R.id.answer_one_two);
        answerTree = (TextView) rootView.findViewById(R.id.answer_one_tree);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionaryPagerAdapter.skipQuestionary();
            }
        });

        answerOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "1";
                onClickAnswer(answerOne, answer);
            }
        });

        answerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "2";
                onClickAnswer(answerTwo, answer);
            }
        });

        answerTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "3";
                onClickAnswer(answerTree, answer);
            }
        });
    }

    protected void putCorrectAnswerColor(String answerFromPrefs, int color) {
        switch(answerFromPrefs) {
            case "1":
                answerOne.setBackgroundColor(getResources().getColor(color));
                break;
            case "2":
                answerTwo.setBackgroundColor(getResources().getColor(color));
                break;
            case "3":
                answerTree.setBackgroundColor(getResources().getColor(color));
                break;
        }
    }

    protected String getSharedPreferencesQuestionId() {
        return "matchReligion";
    }
}
