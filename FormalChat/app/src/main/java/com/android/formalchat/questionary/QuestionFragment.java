package com.android.formalchat.questionary;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.formalchat.R;
import com.android.formalchat.UserQuestionary;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sve on 3/21/15.
 */
public abstract class QuestionFragment extends Fragment {

    private static final String PREFS_NAME = "FormalChatQuestionAnswers";
    private static final String PREFS_NAME_ANSWERS = "FormalChatQuestionAnswers";
    protected static final String ACTION_SHOW_HIDE = "ShowHideDoneBtn";
    private static final int PAGER_SCROLLER_DURATION = 2;

    protected SharedPreferences sharedPreferences;
    protected SharedPreferences sharedPreferencesAnswers;
    protected ViewPager viewPager;
    protected List<String> answers;
    protected String questionTxt;
    protected LinearLayout answersHolder;
    protected String currentSelectedAnswer;
    protected View rootView;
    private TextView skip;
    private ScrollView scrollView;
    private ImageView scrollDownImg;
    private int questionPosition;
    private boolean isLast = false;
    private FixedViewPagerScroller mScroller;

    HashMap<Integer, TextView> answersToViews = new HashMap<>();
    private AnswerReadyListener answerReadyListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(putLayoutId(), container, false);
        viewPager = (ViewPager) container;

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sharedPreferencesAnswers = getActivity().getSharedPreferences(PREFS_NAME_ANSWERS, 0);

        questionTxt = putQuestionText();
        answers = putAnswersList();
        answersHolder = (LinearLayout) rootView.findViewById(putAnswersLayout());

        init();

        initViewPagerScroller();
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                Log.v("formalchat", "position = " + position);
                setQuestionPosition(position);
                initScrollView();
                sendBroadcastToActionDoneBtnOnPosition(position);
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


    public int getQuestionPosition() {
        int questionPos = sharedPreferencesAnswers.getInt("questionPosition", 0);
        clearQuestionPosition();
        return questionPos;
    }

    public void setQuestionPosition(int questionPosition) {
        sharedPreferencesAnswers.edit().putInt("questionPosition", questionPosition).commit();
    }

    public void clearQuestionPosition() {
        if(sharedPreferencesAnswers.contains("questionPosition")) {
            sharedPreferencesAnswers.edit().remove("questionPosition").commit();
        }
    }

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

    private TextView initTextView(final int idx) {
        TextView textView = createTextViewForAnswer(idx);
        return textView;
    }

    private void initViewPagerScroller() {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);

            mScroller = new FixedViewPagerScroller(viewPager.getContext(), new AccelerateInterpolator());
            field.set(viewPager, mScroller);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
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
        goToNextQuestion();
    }

    protected void saveAnswerToSharedPrefs(String answer) {
        SharedPreferences.Editor editor = sharedPreferencesAnswers.edit();
        editor.putString(getQuestionTag(), answer);
        editor.commit();
    }

    protected void saveAnswerToParse(int answer) {
        String questionTag = getQuestionTag();
        updateQuestionary_(questionTag, answer);
    }

    protected void goToNextQuestion() {
//        mScroller.setCurrentDuration(PAGER_SCROLLER_DURATION);
        int questionPosition = getQuestionPosition();
        int pagerLastPosition = viewPager.getAdapter().getCount()-1;

        if(!isLast) {
            if (questionPosition != pagerLastPosition) {
                viewPager.setCurrentItem(questionPosition + 1);
            }
        }
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

    public void setAnswerReadyListener(QuestionaryPagerAdapter answerReadyListener) {
        this.answerReadyListener = answerReadyListener;
    }

    private void sendBroadcastToActionDoneBtnOnPosition(int position) {
        try {
            Intent showDoneBtnIntent = new Intent(ACTION_SHOW_HIDE);
            if (position == viewPager.getAdapter().getCount() - 1) {
                showDoneBtnIntent.putExtra("isDoneBtnVisible", true);
            } else {
                showDoneBtnIntent.putExtra("isDoneBtnVisible", false);
            }
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(showDoneBtnIntent);
        }
        catch (Exception ex) {
            Log.v("formalchat", "formalchat: " + ex.getMessage());
        }
    }

    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }
}
