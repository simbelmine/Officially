package com.android.formalchat;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

/**
 * Created by Sve on 3/12/15.
 */
public class QuestionaryActivity extends FragmentActivity {
    private QuestionaryPagerAdapter questionaryPagerAdapter;
    private ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionary_pager_layout);

        questionaryPagerAdapter = new QuestionaryPagerAdapter(getSupportFragmentManager(), this, this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);

        viewPager.setAdapter(questionaryPagerAdapter);
    }
}
