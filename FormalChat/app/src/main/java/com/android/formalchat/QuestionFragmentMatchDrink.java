package com.android.formalchat;

import android.view.View;
import android.widget.TextView;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionFragmentMatchDrink extends QuestionFragment {
    private String answer;
    private TextView skip;
    private TextView answerOne;
    private TextView answerTwo;
    private TextView answerThree;
    private TextView answerFour;
    private TextView answerFive;

    @Override
    protected int putLayoutId() {
        return R.layout.question_tree;
    }

    @Override
    protected void initButtons(View rootView) {
        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        answerOne = (TextView) rootView.findViewById(R.id.answer_tree_one);
        answerTwo = (TextView) rootView.findViewById(R.id.answer_tree_two);
        answerThree = (TextView) rootView.findViewById(R.id.answer_tree_tree);
        answerFour = (TextView) rootView.findViewById(R.id.answer_tree_four);
        answerFive = (TextView) rootView.findViewById(R.id.answer_tree_five);

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

        answerThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "3";
                onClickAnswer(answerThree, answer);
            }
        });

        answerFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "4";
                onClickAnswer(answerFour, answer);
            }
        });

        answerFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "5";
                onClickAnswer(answerFive, answer);
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
                answerThree.setBackgroundColor(getResources().getColor(color));
                break;
            case "4":
                answerFour.setBackgroundColor(getResources().getColor(color));
                break;
            case "5":
                answerFive.setBackgroundColor(getResources().getColor(color));
                break;
        }
    }

    @Override
    protected String getSharedPreferencesQuestionId() {
        return "matchDrink";
    }
}
