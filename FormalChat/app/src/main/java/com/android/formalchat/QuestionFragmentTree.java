package com.android.formalchat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionFragmentTree extends Fragment {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private QuestionaryPagerAdapter questionaryPagerAdapter;
    private SharedPreferences sharedPreferences;
    private String answer;
    private TextView skip;
    private TextView answerOne;
    private TextView answerTwo;
    private TextView answerTree;
    private TextView answerFour;
    private TextView answerFive;
    private boolean clicked;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.question_tree, container, false);
        questionaryPagerAdapter = new QuestionaryPagerAdapter(getFragmentManager(), getActivity().getApplicationContext(), getActivity());
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        clicked = false;

        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        answerOne = (TextView) rootView.findViewById(R.id.answer_tree_one);
        answerTwo = (TextView) rootView.findViewById(R.id.answer_tree_two);
        answerTree = (TextView) rootView.findViewById(R.id.answer_tree_tree);
        answerFour = (TextView) rootView.findViewById(R.id.answer_tree_four);
        answerFive = (TextView) rootView.findViewById(R.id.answer_tree_five);

        String answerFromPrefs = getAnswerFromSharedPrefs();
        if(answerFromPrefs != "") {
            putCorrectAnswer(answerFromPrefs);
        }

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionaryPagerAdapter.skipQuestionary();
            }
        });

        answerOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "1";
                    questionaryPagerAdapter.updateQuestionary(3, answer);
                    answerOne.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    clicked = true;
                }
            }
        });

        answerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "2";
                    questionaryPagerAdapter.updateQuestionary(3, answer);
                    answerTwo.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    clicked = true;
                }
            }
        });

        answerTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "3";
                    questionaryPagerAdapter.updateQuestionary(3, answer);
                    answerTree.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    clicked = true;
                }
            }
        });

        answerFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "4";
                    questionaryPagerAdapter.updateQuestionary(3, answer);
                    answerFour.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    clicked = true;
                }
            }
        });

        answerFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "5";
                    questionaryPagerAdapter.updateQuestionary(3, answer);
                    answerFive.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    clicked = true;
                }
            }
        });

        return rootView;
    }

    private void setAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("questionTree", answer);
        editor.commit();
    }

    private void putCorrectAnswer(String answerFromPrefs) {
        switch(answerFromPrefs) {
            case "1":
                answerOne.setBackgroundColor(getResources().getColor(R.color.gray));
                break;
            case "2":
                answerTwo.setBackgroundColor(getResources().getColor(R.color.gray));
                break;
            case "3":
                answerTree.setBackgroundColor(getResources().getColor(R.color.gray));
                break;
            case "4":
                answerFour.setBackgroundColor(getResources().getColor(R.color.gray));
                break;
            case "5":
                answerFive.setBackgroundColor(getResources().getColor(R.color.gray));
                break;
        }
    }

    private String getAnswerFromSharedPrefs() {
        return sharedPreferences.getString("questionTree", "");
    }
}
