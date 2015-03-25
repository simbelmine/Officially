package com.android.formalchat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sve on 3/21/15.
 */
public abstract class QuestionFragment extends Fragment {

    private static final String PREFS_NAME = "FormalChatQuestionAnswers";
    protected QuestionaryPagerAdapter questionaryPagerAdapter;
    protected SharedPreferences sharedPreferences;

    protected boolean clicked;
    protected static int question;
    protected ViewPager viewPager;
    protected List<String> answers;
    protected LinearLayout layout;
    protected String answerFromPrefs;
    protected View rootView;
    private TextView skip;
    private Button doneBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(putLayoutId(), container, false);
        viewPager = (ViewPager) container;
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                question = position;
                if(isLastQuestion()) {
                    doneBtn = initButton();
                    layout.addView(doneBtn);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        questionaryPagerAdapter = new QuestionaryPagerAdapter(getFragmentManager(), getActivity().getApplicationContext(), getActivity());
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        clicked = false;

        answers = putAnswersList();
        layout = (LinearLayout) rootView.findViewById(putAnswersLayout());

        init();

        answerFromPrefs = getAnswerFromSharedPrefs();
        return rootView;
    }

    protected abstract int putLayoutId();
    protected abstract int putAnswersLayout();
    protected abstract List<String> putAnswersList();

    protected void init() {
        TextView textView;
        for(int idx = 0; idx < answers.size(); idx++) {
            textView = initTextView(idx);
            putCorrectColor(textView);
            if(layout != null) {
                layout.addView(textView);
            }
        }




        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionaryPagerAdapter.skipQuestionary();
            }
        });
    }

    private Button initButton(){
        Button btn = new Button(getActivity().getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) getResources().getDimension(R.dimen.question_padding_top);
        btn.setText(getResources().getString(R.string.about_me_done));
        btn.setTextColor(getResources().getColor(R.color.black));
        btn.setBackgroundResource(R.drawable.rounded_btns);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionaryPagerAdapter.checkAllAnswersDone(doneBtn);
            }
        });
        btn.setLayoutParams(params);
        return btn;
    }

    private TextView initTextView(final int idx) {
        TextView textView;
        textView = new TextView(getActivity().getApplicationContext());
        final String answer_str = String.valueOf(idx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.answer_height));
        params.bottomMargin = (int) getResources().getDimension(R.dimen.answer_margin_bottom);
        textView.setGravity(Gravity.CENTER);
        textView.setText(answers.get(idx));
        textView.setTextColor(getResources().getColor(R.color.dark_gray));
        textView.setTag(answer_str);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAnswer((TextView) v, idx, answer_str);
            }
        });
        textView.setLayoutParams(params);
        return textView;
    }

    private boolean isLastQuestion() {
       Log.v("formalchat", String.valueOf(question) + " == " + String.valueOf(questionaryPagerAdapter.getCount()-1));
        if(question >= questionaryPagerAdapter.getCount()-1) {
            return true;
        }
        return false;
    }

    private void putCorrectColor(TextView textView) {
        answerFromPrefs = getAnswerFromSharedPrefs();
        if(answerFromPrefs != null) {
            if(answerFromPrefs.equals(textView.getTag().toString())) {
                putCorrectAnswerColor(textView, getResources().getColor(R.color.gray));
            }
            else {
                putCorrectAnswerColor(textView,getResources().getColor(R.color.light_blue));
            }
            clicked = true;
        } else {
            putCorrectAnswerColor(textView, getResources().getColor(R.color.light_blue));
        }
    }

    protected void onClickAnswer(TextView answerTextView, int answer, String answer_str) {
        saveAnswerToParse(answer);
        changeColorOnClick(answerTextView);
        setAnswerToSharedPrefs(answer_str);
        goToNextQuestion();
    }

    protected void goToNextQuestion() {
        if(question != questionaryPagerAdapter.getCount()-1) {
            viewPager.setCurrentItem(question + 1);
        }
    }

    protected void setAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getSharedPreferencesQuestionId(rootView), answer);
        editor.commit();
    }

    protected void changeColorOnClick(TextView answerTextView) {
        if(!clicked) {
            putCorrectAnswerColor(answerTextView, getResources().getColor(R.color.gray));
            clicked = true;
        }
        else {
            putCorrectAnswerColor(answerTextView, getResources().getColor(R.color.gray));
            TextView txtview = (TextView) rootView.findViewWithTag(getAnswerFromSharedPrefs());
            putCorrectAnswerColor(txtview, getResources().getColor(R.color.light_blue));

        }
    }

    protected abstract String getSharedPreferencesQuestionId(View rootView);

    protected void saveAnswerToParse(int answer) {
        String questionTag = getQuestionTag();
        questionaryPagerAdapter.updateQuestionary_(questionTag, answer);
    }

    private String getQuestionTag() {
        return rootView.findViewById(R.id.question).getTag().toString();
    }

    protected String getAnswerFromSharedPrefs() {
        return sharedPreferences.getString(getSharedPreferencesQuestionId(rootView), "");
    }

    protected  void putCorrectAnswerColor(TextView view, int color) {
        if(view != null) {
            view.setBackgroundColor(color);
        }
    }
}
