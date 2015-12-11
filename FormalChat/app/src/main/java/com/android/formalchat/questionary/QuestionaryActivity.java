package com.android.formalchat.questionary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.android.formalchat.ApplicationOfficially;
import com.android.formalchat.MainActivity;
import com.android.formalchat.R;
import com.parse.ParseUser;

import java.lang.reflect.Field;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionaryActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private static final String PREFS_NAME_ANSWERS = "FormalChatQuestionAnswers";
    private QuestionaryPagerAdapter questionaryPagerAdapter;
    private ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;
    private Menu menu;
    private BroadcastReceiver onNotice;
    private boolean isDoneBtnVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionary_pager_layout);
        initToolbar();
        initActionBar();

        isDoneBtnVisible = false;

        questionaryPagerAdapter = new QuestionaryPagerAdapter(getSupportFragmentManager(), this, this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);

        initBroadcastReceiver();
        viewPager.setAdapter(questionaryPagerAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void initActionBar() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_gray)));
    }

    private void initBroadcastReceiver() {
        onNotice = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("isDoneBtnVisible") && intent.getBooleanExtra("isDoneBtnVisible", false)){
                    isDoneBtnVisible = true;
                }
                else {
                    isDoneBtnVisible = false;
                }
                setDoneBtnVisibility(menu);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(QuestionFragment.ACTION_SHOW_HIDE);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.questionary_menu, menu);
        setDoneBtnVisibility(menu);
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        return true;
    }

    private void setDoneBtnVisibility(Menu menu) {
        MenuItem item = menu.findItem(R.id.done_questionary);
        if(isDoneBtnVisible) {
            item.setVisible(true);
        }
        else {
            item.setVisible(false);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_questionary:
                checkAllAnswersDone();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkAllAnswersDone() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME_ANSWERS, 0);
        if(sharedPreferences.getAll().size() == viewPager.getAdapter().getCount()) {
            setDoneQuestionary();
            startMainActivity();
        }
        else {
            Toast.makeText(getApplicationContext(), "Please answer all Questions", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDoneQuestionary() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME_ANSWERS, 0);
        sharedPreferences.edit().clear().commit();
        ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.put("doneQuestionary", true);
        parseUser.saveInBackground();
    }

    private void startMainActivity() {
        if(((ApplicationOfficially)getApplication()).isNetworkAvailable()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            ((ApplicationOfficially)getApplication()).getSnackbar(this, R.string.no_network, R.color.alert_red).show();
        }
    }
}
