package com.android.formalchat;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionaryPagerAdapter extends FragmentStatePagerAdapter {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private Context context;
    private Activity activity;

    public QuestionaryPagerAdapter(FragmentManager fragmentManager, Context ctx, Activity currActivity) {
        super(fragmentManager);
        context = ctx;
        activity = currActivity;
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new QuestionFragmentOne();
            case 1:
                return new QuestionFragmentTwo();
            case 2:
                return new QuestionFragmentTree();
            case 3:
                return new QuestionFragmentFour();
            case 4:
                return new QuestionFragmentFive();
            case 5:
                return new QuestionFragmentSix();
            case 6:
                return new QuestionFragmentSeven();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Question One";
            case 1:
                return "Question Two";
            case 2:
                return "Question Three";
            case 3:
                return "Question Four";
            case 4:
                return "Question Five";
            case 5:
                return "Question Six";
            case 6:
                return "Question Seven";
        }

        return null;
    }

    public void updateQuestionary(final int answer, final String value) {
        final ParseUser parseUser = ParseUser.getCurrentUser();
        final String userName = parseUser.getUsername();

        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("UserQuestionary");
        parseQuery.whereEqualTo("loginName", userName);
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null) {
                    saveAnswers(answer, (UserQuestionary) parseObject, value);
                }
                else {
                    Log.v("formalchat", e.getMessage());
                    UserQuestionary questionary = new UserQuestionary();
                    questionary.setLoginName(userName);
                    questionary.saveInBackground();
                    saveAnswers(answer, questionary, value);
                }
            }
        });
    }

    private void saveAnswers(int answer, UserQuestionary questionary, String value) {
        switch (answer) {
            case 1:
                questionary.setQuestionOne(value);
                questionary.saveInBackground();
                break;
            case 2:
                questionary.setQuestionTwo(value);
                questionary.saveInBackground();
                break;
            case 3:
                questionary.setQuestionTree(value);
                questionary.saveInBackground();
                break;
            case 4:
                questionary.setQuestionFour(value);
                questionary.saveInBackground();
                break;
            case 5:
                questionary.setQuestionFive(value);
                questionary.saveInBackground();
                break;
            case 6:
                questionary.setQuestionSix(value);
                questionary.saveInBackground();
                break;
            case 7:
                questionary.setQuestionSeven(value);
                questionary.saveInBackground();
                break;
        }
    }

    public void checkAllAnswersDone(final Button doneBtn) {
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
                        if (po.getString("questionOne") != null && po.getString("questionTwo") != null &&
                                po.getString("questionTree") != null && po.getString("questionFour") != null &&
                                po.getString("questionFive") != null && po.getString("questionSix") != null &&
                                po.getString("questionSeven") != null) {
                            doneBtn.setVisibility(View.VISIBLE);
                            doneBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    setDoneQuestionary();
                                    startMainActivity();
                                }
                            });
                        } else {
                            Toast.makeText(context, "Please answer all Questions", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void setDoneQuestionary() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("questionary_done", true);
        editor.commit();
    }

    public void skipQuestionary() {
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        QuestionaryDialog questionaryDialog = new QuestionaryDialog();
        questionaryDialog.show(fragmentTransaction, "dialog");
    }

    private void startMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
