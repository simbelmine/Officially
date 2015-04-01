package com.android.formalchat;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * Created by Sve on 3/31/15.
 */
public class DragedRectangleView extends View {
    private Paint rectPaint;
    private TextPaint textPaint;
    private int xLeft;
    private int yLeft;
    private int xRight;
    private int yRight;

    public DragedRectangleView(Context context) {
        super(context);
        init();
    }

    public DragedRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragedRectangleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    public void init() {
        rectPaint = new Paint();
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5);

        textPaint = new TextPaint();
        textPaint.setColor(Color.MAGENTA);
        textPaint.setTextSize(20);

        xLeft = this.getWidth()/2 -200 ;
        yLeft = this.getHeight()/2 - 200;
        xRight = this.getWidth()/2 + 200;
        yRight = this.getHeight()/2 + 200;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawRect(xLeft, yLeft, xRight, yRight, rectPaint);
    }


}
