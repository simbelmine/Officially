package com.android.formalchat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
    private static final String PREFS_NAME = "FormalChatPrefs";
    private ImageView windowRectangleView;
    private ZoomInOutImgView imgView;
    private Button doneEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_layout);

        String url = getIntent().getStringExtra("url");
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

                windowRectangleView = (ImageView) findViewById(R.id.dragRect);
                windowRectangleView.bringToFront();

                doneEditing = (Button) findViewById(R.id.done_editing);
                doneEditing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Bitmap b1 = imgView.getDrawingCache();
                        Bitmap newBitmap = Bitmap.createBitmap(b1,
                                (int) windowRectangleView.getX(),
                                (int) windowRectangleView.getY(),
                                windowRectangleView.getWidth(),
                                windowRectangleView.getHeight());

                        windowRectangleView.setImageBitmap(newBitmap);
                        imgView.destroyDrawingCache();

                        saveProfileImg(newBitmap);


                        String profileImgPath = getProfileImgPath();

                        Intent intent = new Intent(CropActivity.this, FullImageActivity.class);
                        //intent.putExtra("profileImgPath", profileImgPath);
                        byte[] profileImg = bitmapToByteArray(newBitmap);
                        intent.putExtra("profileImg", profileImg);
                        setResult(RESULT_OK, intent);

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


    private String getProfileImgPath() {
        File filename = new File(Environment.getExternalStorageDirectory() + "/.formal_chat/profile_pic.jpg");
        if(filename.exists()) {
            return filename.getPath();
        }
        return null;
    }

    private void saveProfileImg(Bitmap bitmapToReturn) {
        File filename = new File(Environment.getExternalStorageDirectory() + "/.formal_chat/profile_pic.jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmapToReturn.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Drawable getDrawableFromUrl(String url) {
        String imgName = getShortName(url);
        File dir = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        File imgFile = new File(dir, imgName);

        if(!isImgExists(imgFile)){
            downloadImg(url, imgName, imgFile);
        }

        return Drawable.createFromPath(imgFile.getAbsolutePath());
    }

    private void downloadImg(String img_url, String imgName, File imgFile) {

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
