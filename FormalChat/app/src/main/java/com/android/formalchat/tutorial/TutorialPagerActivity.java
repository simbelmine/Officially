package com.android.formalchat.tutorial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.formalchat.LoginActivity;
import com.android.formalchat.R;

/**
 * Created by Sve on 1/29/15.
 */
public class TutorialPagerActivity extends FragmentActivity {
    public static final String PREFS_NAME = "FormalChatPrefs";
    private TutorialPagerAdapter mTutorialPagerAdapter;
    private ViewPager mViewPager;
    private Button mLoginButton;
    //    private Button btn1;
//    private Button btn2;
//    private Button btn3;
    private ImageView btn1;
    private ImageView btn2;
    private ImageView btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_pager_layout);

        mTutorialPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), this);

        btn1 = (ImageView) findViewById(R.id.btn1);
        btn2 = (ImageView) findViewById(R.id.btn2);
        btn3 = (ImageView) findViewById(R.id.btn3);
        mLoginButton = (Button) findViewById(R.id.login_redirect_btn);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToSharedPrefs();
                launchLoginActivity();
            }
        });
        initButtons();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTutorialPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageSelected(int position) {
                buttonAction(position);
            }
        });
    }

    private void initButtons() {
        btn1.setBackgroundColor(getResources().getColor(R.color.gray));
        btn2.setBackgroundColor(getResources().getColor(R.color.items));
        btn3.setBackgroundColor(getResources().getColor(R.color.items));
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void buttonAction(int position) {
        switch (position) {
            case 0: btn1.setBackgroundColor(getResources().getColor(R.color.gray));
                btn2.setBackgroundColor(getResources().getColor(R.color.items));
                btn3.setBackgroundColor(getResources().getColor(R.color.items));
//                mLoginButton.setVisibility(View.INVISIBLE);
                break;
            case 1: btn1.setBackgroundColor(getResources().getColor(R.color.items));
                btn2.setBackgroundColor(getResources().getColor(R.color.gray));
                btn3.setBackgroundColor(getResources().getColor(R.color.items));
//                mLoginButton.setVisibility(View.INVISIBLE);
                break;
            case 2: btn1.setBackgroundColor(getResources().getColor(R.color.items));
                btn2.setBackgroundColor(getResources().getColor(R.color.items));
                btn3.setBackgroundColor(getResources().getColor(R.color.gray));
//                mLoginButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isSharedPreferencesEmpty()) {
            launchLoginActivity();
        }
    }

    private void saveToSharedPrefs() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("firstAttempt", true);
        editor.commit();
    }

    private boolean isSharedPreferencesEmpty() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if(!settings.getBoolean("firstAttempt", false)) {
            return true;
        }
        return false;
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(TutorialPagerActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
