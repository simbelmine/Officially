package com.android.formalchat.questionary;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.formalchat.MainActivity;
import com.android.formalchat.R;
import com.android.formalchat.UserQuestionary;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sve on 3/21/15.
 */
public abstract class QuestionFragment extends Fragment {

    private static final String PREFS_NAME = "FormalChatQuestionAnswers";
    private static final String PREFS_NAME_ANSWERS = "FormalChatQuestionAnswers";
    protected SharedPreferences sharedPreferences;

    protected ViewPager viewPager;
    protected List<String> answers;
    protected String questionTxt;
    protected LinearLayout answersHolder;
    protected String currentSelectedAnswer;
    protected View rootView;
    private TextView skip;
    private Button doneBtn;
    private ScrollView scrollView;
    private ImageView scrollDownImg;

    private boolean isLast = false;

    HashMap<Integer, TextView> answersToViews = new HashMap<>();
    private AnswerReadyListener answerReadyListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(putLayoutId(), container, false);
        viewPager = (ViewPager) container;

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);

        questionTxt = putQuestionText();
        answers = putAnswersList();
        answersHolder = (LinearLayout) rootView.findViewById(putAnswersLayout());

        init();

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                initScrollView();
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        return rootView;
    }

    protected int putLayoutId() {
        return R.layout.question_layout;
    }
    protected int putAnswersLayout() {
        return R.id.answers_lyout;
    }


    protected abstract String putQuestionText();
    protected abstract List<String> putAnswersList();

    private void init() {
        TextView questionView = (TextView) rootView.findViewById(R.id.question);
        questionView.setText(questionTxt);

        TextView textView;
        currentSelectedAnswer = getAnswerFromSharedPrefs();

        for(int idx = 0; idx < answers.size(); idx++) {
            textView = initTextView(idx);
            answersToViews.put(idx, textView);
            if(answersHolder != null) {
                answersHolder.addView(textView);
            }
        }

        updateTextViewColorsAccordingToSelectedAnswer();

        skip = (TextView) rootView.findViewById(R.id.skip_txt);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipQuestionary();
            }
        });
    }

    private void initScrollView() {
        scrollView = (ScrollView) rootView.findViewById(R.id.answers_scrollview);
        scrollDownImg = (ImageView) rootView.findViewById(R.id.scroll_down_img);
        if((scrollView.getMeasuredHeight() < scrollView.getChildAt(0).getHeight())) {
            scrollDownImg.setBackgroundResource(R.drawable.down_scrollbar_arrow);
            scrollDownImg.setVisibility(View.VISIBLE);
        }
        else {
            scrollDownImg.setVisibility(View.INVISIBLE);
        }
    }

    private Button initButton(){
        Button btn = new Button(getActivity().getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.answers_done));
//        params.topMargin = (int) getResources().getDimension(R.dimen.question_padding_top);
        params.setMargins(
                (int) getResources().getDimension(R.dimen.answer_margin_lr),
                (int) getResources().getDimension(R.dimen.question_padding_top),
                (int) getResources().getDimension(R.dimen.answer_margin_lr),
                (int) getResources().getDimension(R.dimen.answer_margin_bottom));
        btn.setText(getResources().getString(R.string.done));
        btn.setTextColor(getResources().getColor(R.color.action_bar));
        // btn.setBackgroundResource(R.drawable.rounded_btns);
        btn.setBackgroundColor(getResources().getColor(R.color.white));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllAnswersDone();
            }
        });
        btn.setLayoutParams(params);
        return btn;
    }

    private TextView initTextView(final int idx) {
        TextView textView = createTextViewForAnswer(idx);
        return textView;
    }

    @NonNull
    private TextView createTextViewForAnswer(final int idx) {
        TextView textView;
        textView = new TextView(getActivity().getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.answer_height));
//        params.bottomMargin = (int) getResources().getDimension(R.dimen.answer_margin_bottom);
        params.setMargins(
                (int) getResources().getDimension(R.dimen.answer_margin_lr), 0,
                (int) getResources().getDimension(R.dimen.answer_margin_lr),
                (int) getResources().getDimension(R.dimen.answer_margin_bottom));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTag(String.valueOf(idx));
        textView.setText(answers.get(idx));
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAnswer((TextView)v);
            }
        });
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.answer_selector);

        return textView;
    }

    private void updateTextViewColorsAccordingToSelectedAnswer() {
        for (Integer answer : answersToViews.keySet()) {
            TextView answerTextView = answersToViews.get(answer);
            if (currentSelectedAnswer != null && currentSelectedAnswer.equals(String.valueOf(answer))) {
                answerTextView.setSelected(true);
            } else {
                answerTextView.setSelected(false);
            }
        }
    }

    protected void onClickAnswer(TextView answerTextView) {
        currentSelectedAnswer = (String) answerTextView.getTag();
        saveAnswerToParse(Integer.valueOf(currentSelectedAnswer));
        saveAnswerToSharedPrefs(currentSelectedAnswer);

        updateTextViewColorsAccordingToSelectedAnswer();
        //goToNextQuestion();
    }

    protected void saveAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Log.e("formalchat", getQuestionTag() + " : " + answer);
        editor.putString(getQuestionTag(), answer);
        editor.commit();
    }

    protected void saveAnswerToParse(int answer) {
        String questionTag = getQuestionTag();
       updateQuestionary_(questionTag, answer);
    }

    protected abstract String getQuestionTag();

    protected String getAnswerFromSharedPrefs() {
        return sharedPreferences.getString(getQuestionTag(), "");
    }





    public void skipQuestionary() {
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
        QuestionaryDialog questionaryDialog = new QuestionaryDialog();
        questionaryDialog.show(fragmentTransaction, "dialog");
    }

    public void checkAllAnswersDone() {
        ParseUser user = ParseUser.getCurrentUser();
        String userName = user.getUsername();

        ParseQuery<ParseObject> parseQuery = new ParseQuery<>("UserQuestionary");
        parseQuery.whereEqualTo("loginName", userName);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null) {
                    if(parseObjects.size() > 0) {
                        ParseObject po = parseObjects.get(0);
                        List<String> answersList = getAnswersFromPrefs();
                        int counter = 0;

                        if(answersList!= null) {
                            for (String answer : answersList) {
                                if(po.get(answer) != null) {
                                    counter++;
                                }
                            }

                            // TO DO: getCount() doesn't work! ###############
//                            if(counter == getCount()) {
//                                setDoneQuestionary();
//                                startMainActivity();
//                            }
//                            else {
//                                Toast.makeText(getActivity().getApplicationContext(), "Please answer all Questions", Toast.LENGTH_SHORT).show();
//                            }
                        }
                        else {
                            Log.v("formalchat", "answerList from SharedPrefs is empty");
                        }
                    }
                }
            }
        });
    }

    private List<String> getAnswersFromPrefs() {
        List<String> answers = new ArrayList<>();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME_ANSWERS, 0);
        Map<String, ?> answersMap = sharedPreferences.getAll();
        for(Map.Entry<String, ?> entry : answersMap.entrySet()) {
            answers.add(entry.getKey());
        }
        return answers;
    }

    private void setDoneQuestionary() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("questionary_done", true);
        editor.commit();
    }

    private void startMainActivity() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().getApplicationContext().startActivity(intent);
    }


    public void updateQuestionary_(final String question, final int answer) {
        final ParseUser parseUser = ParseUser.getCurrentUser();
        final String userName = parseUser.getUsername();

        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("UserQuestionary");
        parseQuery.whereEqualTo("loginName", userName);
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null) {
                    saveAnswer((UserQuestionary) parseObject, question, answer);
                }
                else {
                    UserQuestionary questionary = new UserQuestionary();
                    questionary.setLoginName(userName);
                    questionary.saveInBackground();
                    saveAnswer(questionary, question, answer);
                }
            }
        });
    }

    private void saveAnswer(UserQuestionary questionary, String question, int answer) {
        questionary.put(question, answer);
        questionary.saveInBackground();
    }

    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }

    public void setAnswerReadyListener(QuestionaryPagerAdapter answerReadyListener) {
        this.answerReadyListener = answerReadyListener;
    }
}
