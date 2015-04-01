package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by Sve on 3/31/15.
 */
public class CropImageActivity extends Activity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image);

        imageView = (ImageView) findViewById(R.id.crop_image);
        Intent intent = getIntent();

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
