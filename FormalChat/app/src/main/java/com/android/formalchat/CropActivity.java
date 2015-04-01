package com.android.formalchat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Sve on 3/31/15.
 */
public class CropActivity extends Activity implements View.OnDragListener, View.OnTouchListener{
    private View dragedRectangleView;
    private ImageView original;
    private android.widget.RelativeLayout.LayoutParams layoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_layout);

        dragedRectangleView =  findViewById(R.id.dragRect);
        original = (ImageView) findViewById(R.id.image);

        dragedRectangleView.setTag("ImgToCrop");
        dragedRectangleView.setOnTouchListener(this);
        //findViewById(R.id.root).setOnDragListener(this);
        findViewById(R.id.image).setOnDragListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(null, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        if (event.getAction()==DragEvent.ACTION_DROP) {
            View view = (View) event.getLocalState();
            ViewGroup from = (ViewGroup) view.getParent();
            from.removeView(view);

            if(v == original) {
                RelativeLayout to = (RelativeLayout) findViewById(R.id.root);

                view.setX(event.getX() - (view.getWidth()/2));
                view.setY(event.getY() - (view.getHeight() / 2));

                to.addView(view);
                view.setVisibility(View.VISIBLE);





                Bitmap bitmap1 = Bitmap.createScaledBitmap(
                        ((BitmapDrawable) original.getDrawable())
                                .getBitmap(), original.getWidth(), original
                                .getHeight(), false);



                Bitmap bitmap = Bitmap.createBitmap(bitmap1,
                        (int) view.getX(), (int) view.getY(), view.getWidth(),
                        view.getHeight());

                //view.setBackground(new BitmapDrawable(getResources(), bitmap));
                Intent intent = new Intent(this, CropImageActivity.class);
                intent.putExtra("BitmapImage", bitmap);
                startActivity(intent);
            }

        }
        return true;
    }
}
