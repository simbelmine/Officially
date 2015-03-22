package com.android.formalchat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionFragmentYourDrinking extends QuestionFragment {
    private TextView skip;
    private TextView answerOne;
    private TextView answerTwo;
    private TextView answerTree;
    private TextView answerFour;
    private TextView answerFive;
    private Button doneBtn;

    @Override
    protected int putLayoutId() {
        return R.layout.question_seven;
    }

    @Override
    protected void initButtons(View rootView) {
        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        doneBtn = (Button) rootView.findViewById(R.id.done_btn);
        answerOne = (TextView) rootView.findViewById(R.id.answer_seven_one);
        answerTwo = (TextView) rootView.findViewById(R.id.answer_seven_two);
        answerTree = (TextView) rootView.findViewById(R.id.answer_seven_tree);
        answerFour = (TextView) rootView.findViewById(R.id.answer_seven_four);
        answerFive = (TextView) rootView.findViewById(R.id.answer_seven_five);

        doneBtn.setVisibility(View.INVISIBLE);

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
                questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
            }
        });

        answerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "2";
                onClickAnswer(answerTwo, answer);
                questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
            }
        });

        answerTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "3";
                onClickAnswer(answerTree, answer);
                questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
            }
        });

        answerFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "4";
                onClickAnswer(answerFour, answer);
                questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
            }
        });

        answerFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = "5";
                onClickAnswer(answerFive, answer);
                questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
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
            case "4":
                answerFour.setBackgroundColor(getResources().getColor(color));
                break;
            case "5":
                answerFive.setBackgroundColor(getResources().getColor(color));
                break;
        }
    }

    protected String getSharedPreferencesQuestionId() {
        return "yourDrinking";
    }
}
