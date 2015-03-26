package com.android.formalchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Sve on 6/9/14.
 */
public class RoundedImageView extends ImageView {

    public RoundedImageView(Context context) {
        super(context);
    }
    public RoundedImageView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public RoundedImageView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if(drawable == null) {
            return;
        }
        if(getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmap_argb = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        int width = getWidth();

        Bitmap roundBitmap = getCroppedBitmap(bitmap_argb, width);
        canvas.drawBitmap(roundBitmap,0,0, null);
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap scaled_bitmap;

        if(bitmap.getWidth() != radius || bitmap.getHeight() != radius) {
            float smallest = Math.min(bitmap.getWidth(), bitmap.getHeight());
            float factor = smallest/radius;
            scaled_bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / factor), (int) (bitmap.getHeight() / factor), false);
        }
        else {
            scaled_bitmap = bitmap;
        }

        Bitmap output_bitmap = Bitmap.createBitmap(scaled_bitmap.getWidth(), scaled_bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output_bitmap);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,0,scaled_bitmap.getWidth(), getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawARGB(0,0,0,0);
        canvas.drawCircle(scaled_bitmap.getWidth()/2+0.7f, scaled_bitmap.getHeight()/2+0.7f, scaled_bitmap.getWidth()/2+0.1f,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaled_bitmap, rect,rect,paint);

        return output_bitmap;
    }
}
