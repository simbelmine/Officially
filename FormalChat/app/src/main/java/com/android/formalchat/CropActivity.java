package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by Sve on 3/31/15.
 */
public class CropActivity extends Activity {
    private File dir;
    private ImageView cropMeasureView;
    private ZoomInOutImgView imgView;
    private Button doneEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_layout);

        dir = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        final String url = getIntent().getStringExtra("url");
        final Drawable drawable = getDrawableFromUrl(url);

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

                rl.addView(imgView);

                cropMeasureView = (ImageView) findViewById(R.id.mesureRect);
                cropMeasureView.bringToFront();

                doneEditing = (Button) findViewById(R.id.done_editing);
                doneEditing.setAllCaps(false);
                doneEditing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap b1 = imgView.getDrawingCache();
                        Bitmap newBitmap = Bitmap.createBitmap(b1,
                                (int) cropMeasureView.getX(),
                                (int) cropMeasureView.getY(),
                                cropMeasureView.getWidth(),
                                cropMeasureView.getHeight());

                        cropMeasureView.setImageBitmap(newBitmap);
                        imgView.destroyDrawingCache();

                        Intent intent = new Intent(CropActivity.this, FullImageActivity.class);
                        byte[] profileImg = bitmapToByteArray(newBitmap);
                        intent.putExtra("profileImg", profileImg);
                        setResult(RESULT_OK, intent);

                        deleteSavedImg(url);

                        finish();
                    }
                });
            }
        });
    }

    private byte[] bitmapToByteArray(Bitmap newBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        return outputStream.toByteArray();
    }

    private Drawable getDrawableFromUrl(String url) {
        String imgName = getShortName(url);
        File imgFile = new File(dir, imgName);

        if(!isImgExists(imgFile)){
            downloadImg(url, imgFile);
        }

        return Drawable.createFromPath(imgFile.getAbsolutePath());
    }

    private void downloadImg(String img_url, File imgFile) {
        try {
            URL url = new URL(img_url);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(imgFile);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        }
        catch (IOException ex) {
            Log.e("formalchat", ex.getMessage());
        }
    }

    private void deleteSavedImg(String url) {
        File[] files_list = dir.listFiles();
        for(int f = 0; f < files_list.length; f++) {
            if(getShortName(url).equals(files_list[f].getName())) {
                files_list[f].delete();
            }
        }
    }

    private boolean isImgExists(File imgFile) {
        if(imgFile.exists())
            return true;
        return false;
    }

    private String getShortName(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}
