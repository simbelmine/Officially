package com.android.formalchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionFragmentSeven extends Fragment {
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
    private Button doneBtn;
    private boolean clicked;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.question_seven, container, false);
        questionaryPagerAdapter = new QuestionaryPagerAdapter(getFragmentManager(), getActivity().getApplicationContext(), getActivity());
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        clicked = false;

        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        doneBtn = (Button) rootView.findViewById(R.id.done_btn);
        answerOne = (TextView) rootView.findViewById(R.id.answer_seven_one);
        answerTwo = (TextView) rootView.findViewById(R.id.answer_seven_two);
        answerTree = (TextView) rootView.findViewById(R.id.answer_seven_tree);
        answerFour = (TextView) rootView.findViewById(R.id.answer_seven_four);
        answerFive = (TextView) rootView.findViewById(R.id.answer_seven_five);

        doneBtn.setVisibility(View.INVISIBLE);

        String answerFromPrefs = getAnswerFromSharedPrefs();
        if(answerFromPrefs != "") {
            putCorrectAnswer(answerFromPrefs);
            questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
        }

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionaryPagerAdapter.skipQuestionary();
            }
        });


//        doneBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startMainActivity();
//            }
//        });

        answerOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "1";
                    questionaryPagerAdapter.updateQuestionary(7, answer);
                    answerOne.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
                    clicked = true;
                }
            }
        });

        answerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "2";
                    questionaryPagerAdapter.updateQuestionary(7, answer);
                    answerTwo.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
                    clicked = true;
                }
            }
        });

        answerTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "3";
                    questionaryPagerAdapter.updateQuestionary(7, answer);
                    answerTree.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs("3");
                    questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
                    clicked = true;
                }
            }
        });

        answerFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "4";
                    questionaryPagerAdapter.updateQuestionary(7, answer);
                    answerFour.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
                    clicked = true;
                }
            }
        });

        answerFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    answer = "5";
                    questionaryPagerAdapter.updateQuestionary(7, answer);
                    answerFive.setBackgroundResource(R.color.gray);
                    setAnswerToSharedPrefs(answer);
                    questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
                    clicked = true;
                }
            }
        });

        return rootView;
    }

    private void setAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("questionSeven", answer);
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
        return sharedPreferences.getString("questionSeven", "");
    }

    private void startMainActivity() {
        Intent i = new Intent(getActivity(), MainActivity.class);
        startActivity(i);
    }
}
