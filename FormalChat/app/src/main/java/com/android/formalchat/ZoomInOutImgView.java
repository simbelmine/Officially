package com.android.formalchat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Created by Sve on 4/1/15.
 */
public class ZoomInOutImgView extends ImageView {
    private static final int INVALID_POINTER_ID = -1;

    private Drawable image;
    private int squareDimension;
    private float posX;
    private float posY;
    private float lastTouchX;
    private float lastTouchY;
    private int activePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.f;

    public ZoomInOutImgView(Context context, Drawable image, int pX, int pY) {
        super(context);
        this.image = image;
        squareDimension = (int)(getResources().getDimension(R.dimen.croppingSquare)/getResources().getDisplayMetrics().density);
        int pxs = (int) (squareDimension * getResources().getDisplayMetrics().density);
//         image.setBounds(0, 0, pxs, pxs);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        posX = pX / 2 - pxs / 2;
        posY = pY / 2 - pxs / 2;

        calculateImageBounds(pxs);
    }

    private void calculateImageBounds(int pixels) {
        int rootLayoutHeight = (int)(getResources().getDimension(R.dimen.root_crop_height)/getResources().getDisplayMetrics().density);
        int rootLayoutWidth = (int)(getResources().getDimension(R.dimen.root_crop_width)/getResources().getDisplayMetrics().density);
        int imgHeight = image.getIntrinsicHeight(); // 533
        int imgWidth = image.getIntrinsicWidth();   // 395
        double minimisationNumber;

        Log.v("formalchat", "square dimens: " + squareDimension);
        Log.v("formalchat", "imgHeight: " + imgHeight);
        Log.v("formalchat", "imgWidth: " + imgWidth);


        if(imgHeight > squareDimension && imgWidth > squareDimension) {
            if(imgHeight > rootLayoutHeight) {
                squareDimension = rootLayoutHeight+100;
            }
            double minimisationNumberHeight = (double)imgWidth/(double)squareDimension;
            double minimizedHeight = imgHeight/minimisationNumberHeight;
            int minimizedHeight_rounded = (int) Math.ceil(minimizedHeight);

            if(imgWidth > rootLayoutWidth) {
                squareDimension = rootLayoutWidth+100;
            }
            double minimisationNumberWidth = (double)imgHeight/(double)squareDimension;
            double minimizedWidth = imgWidth/minimisationNumberWidth;
            int minimizedWidth_rounded = (int) Math.ceil(minimizedWidth);

            image.setBounds(0 - minimizedWidth_rounded/2, 0 - minimizedHeight_rounded/2, pixels + minimizedWidth_rounded/2, pixels + minimizedHeight_rounded/2);
        }
        else if(imgHeight > imgWidth && imgHeight > squareDimension) {
            minimisationNumber = (double)imgWidth/(double)squareDimension;
            double minimizedHeight = imgHeight/minimisationNumber;
            int minimizedHeight_rounded = (int) Math.ceil(minimizedHeight);

            image.setBounds(0, 0 - minimizedHeight_rounded/2, pixels, pixels + minimizedHeight_rounded/2);
        }
        else if(imgWidth > imgHeight && imgWidth > squareDimension) {
            minimisationNumber = (double)imgHeight/(double)squareDimension;
            double minimizedWidth = imgWidth/minimisationNumber;
            int minimizedWidth_rounded = (int) Math.ceil(minimizedWidth);

            image.setBounds(0 - minimizedWidth_rounded/2, 0, pixels + minimizedWidth_rounded/2, pixels);
        }
        else {
            image.setBounds(0, 0, pixels, pixels);
        }


//        image.setBounds(0, 0 - scaledHeightByMinimisationNumber_ToPixels, (int) posX, (int) posX + scaledHeightByMinimisationNumber_ToPixels);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                float x = event.getX();
                float y = event.getY();
                lastTouchX = x;
                lastTouchY = y;
                activePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = event.findPointerIndex(activePointerId);
                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);

                //Only move is scaleGestureDetector isn't processing a gesture
                if (!scaleGestureDetector.isInProgress()) {
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;
                    posX += dx;
                    posY += dy;

                    invalidate();
                }

                lastTouchX = x;
                lastTouchY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
                activePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.

                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(posX, posY);
        canvas.scale(scaleFactor, scaleFactor);
        image.draw(canvas);
        setcache();
        canvas.restore();
    }

    private void setcache() {
        this.setDrawingCacheEnabled(true);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));

            invalidate();
            return true;
        }
    }
}
