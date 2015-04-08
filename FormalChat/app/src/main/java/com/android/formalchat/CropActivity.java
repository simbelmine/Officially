package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Sve on 3/31/15.
 */
public class CropActivity extends Activity {
    private ImageView windowRectangleView;
    private ImageView original;
    private android.widget.RelativeLayout.LayoutParams layoutParams;
    private ZoomInOutImgView imgView;
    private Button doneEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_layout);

//        dragedRectangleView =  findViewById(R.id.dragRect);
        //original = (ImageView) findViewById(R.id.image);

        final Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        final FrameLayout rl = (FrameLayout) findViewById(R.id.root_crop);

        final LinearLayout.LayoutParams params_original = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        ViewTreeObserver viewTreeObserver = rl.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeOnGlobalLayoutListener(rl, this);
                int posX = rl.getMeasuredWidth();
                int posY = rl.getMeasuredHeight();

                imgView = new ZoomInOutImgView(getApplicationContext(), drawable, posX, posY);
                imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                imgView.setLayoutParams(params_original);
                imgView.setOnDragListener(new View.OnDragListener() {
                    @Override
                    public boolean onDrag(View v, DragEvent event) {
                        return onDrag(v, event);
                    }
                });
                rl.addView(imgView);

                windowRectangleView = (ImageView) findViewById(R.id.dragRect);
                windowRectangleView.bringToFront();

                doneEditing = (Button) findViewById(R.id.done_editing);
                doneEditing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                        Log.v("formalchat", "posX = " + imgView.getPosX() + "   posY = " + imgView.getPosY() +
                        "   width = " + imgView.getWidth() + "   height = " + imgView.getHeight());

                        Log.v("formalchat", "posX = " + (int) windowRectangleView.getX() + "   posY = " + (int) windowRectangleView.getY() +
                                "   width = " + windowRectangleView.getWidth() + "   height = " + windowRectangleView.getHeight());
                    }
                });
            }
        });




//        dragedRectangleView.setOnTouchListener(this);
        //findViewById(R.id.root).setOnDragListener(this);
//        findViewById(R.id.image).setOnDragListener(this);
    }


//    public boolean onTouch(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
//            v.startDrag(null, shadowBuilder, v, 0);
//            v.setVisibility(View.INVISIBLE);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean onDrag(View v, DragEvent event) {
//
//        if (event.getAction()==DragEvent.ACTION_DROP) {
//            View view = (View) event.getLocalState();
//            ViewGroup from = (ViewGroup) view.getParent();
//            from.removeView(view);
//
//            if(v == original) {
//                FrameLayout to = (FrameLayout) findViewById(R.id.root_crop);
//
//                view.setX(event.getX() - (view.getWidth()/2));
//                view.setY(event.getY() - (view.getHeight() / 2));
//
//                to.addView(view);
//                view.setVisibility(View.VISIBLE);
//
//
//                Bitmap bitmap1 = Bitmap.createScaledBitmap(
//                        ((BitmapDrawable)
//                                original.getDrawable()).getBitmap(),
//                                original.getWidth(),
//                        original.getHeight(), false);
//
//
//
//                Bitmap bitmap = Bitmap.createBitmap(bitmap1,
//                        (int) view.getX(), (int) view.getY(), view.getWidth(),
//                        view.getHeight());
//
//                //view.setBackground(new BitmapDrawable(getResources(), bitmap));
//                Intent intent = new Intent(this, CropImageActivity.class);
//                intent.putExtra("BitmapImage", bitmap);
//                startActivity(intent);
//            }
//
//        }
//        return true;
//    }

    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}
