package com.android.formalchat.questionary;

import android.content.Context;
import android.util.Log;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by Sve on 6/22/15.
 */
public class FixedViewPagerScroller extends Scroller {
    private int currentDuration = 600;

    public FixedViewPagerScroller(Context context) {
        super(context);
    }
    public FixedViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public int getCurrentDuration() {
        return currentDuration;
    }

    public void setCurrentDuration(int time) {
        currentDuration = time;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, currentDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, currentDuration);
    }
}
