package com.android.formalchat.profile;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.formalchat.R;
import com.android.formalchat.UserImages;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfileAddImageDialog extends DialogFragment {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int ACTIVITY_SELECT_IMAGE = 321;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final String ACTION="PICTURE_UPLOAD_COMPLETE";

    private static final String PREFS_NAME = "FormalChatPrefs";
    private SharedPreferences sharedPreferences;
    public String imageName = "IMG_"+ getTimeStamp() + ".jpg";
    private ImageView mTakePhotoImg;
    private TextView mTakePhotoImgTxt;
    private ImageView mAttachPhotoImg;
    private TextView mAttachPhotoImgTxt;
    private Uri fileUri;
    private String imgCameraPath;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int id = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_img_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);

        mTakePhotoImg = (ImageView) view.findViewById(R.id.take_photo_img);
        mTakePhotoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTakePhoto();
            }
        });
        mTakePhotoImgTxt = (TextView) view.findViewById(R.id.take_photo_txt);
        mTakePhotoImgTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTakePhoto();
            }
        });

        mAttachPhotoImg = (ImageView) view.findViewById(R.id.attach_photo_img);
        mAttachPhotoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAttachPhoto();
            }
        });
        mAttachPhotoImgTxt = (TextView) view.findViewById(R.id.attach_photo_txt);
        mAttachPhotoImgTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAttachPhoto();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Drawable drawable;

        if(resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (requestCode == ACTIVITY_SELECT_IMAGE) {
                    processTakenImage(data, 800);
//                    drawable = getSelectedImage(data, requestCode);
//                    saveToParse(drawable);
//                    //saveThumbnail(data);
//                    saveToLocalStorage(drawable);
                }
            }
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                drawable = getSelectedImage(data, requestCode);
                showUploadNotification(
                        getActivity(),
                        R.string.picture_upload_notif_title,
                        R.string.picture_upload_notif_text,
                        R.drawable.upload_icon,
                        true);
                saveToParse(getActivity(), drawable);
                //saveToLocalStorage(getCameraImageThumbnail(drawable, 300));
//                saveToLocalStorage(drawable);
            }

            getDialog().dismiss();
        }
    }

//    private void saveThumbnail(Intent data) {
//        Drawable drawable = getThumbImage(data);
//        saveToLocalStorage(drawable);
//    }

    private void onClickTakePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void onClickAttachPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
    }

    private void saveToLocalStorage(Drawable drawable) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        boolean success = true;
        if(!folder.exists()) {
            success = folder.mkdir();
        }

        if(success && drawable != null) {
            File picture = new File(folder, imageName);
            FileOutputStream out;
            try {
                out = new FileOutputStream(picture);
                Bitmap bitmapImage = ((BitmapDrawable) drawable).getBitmap();
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            }
            catch(IOException ex) {
                Log.e("formalchat", "Exception: " + ex.toString());
            }
        }
        else {
            Log.e("formalchat", "The Folder failed to create!");
        }
    }

    private void saveToParse(final Activity activity, final Drawable drawableL) {
        ParseUser parseUser = ParseUser.getCurrentUser();
        final String userName = parseUser.getUsername();
        final ParseFile imgFile = drawableToParseFile(drawableL);
        final UserImages userImages = new UserImages();

        if(isNetworkAvailable(activity)) {
            userImages.setUserName(userName);
            userImages.setPhotoFile(imgFile);
            userImages.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    saveToLocalStorage(drawableL);
                    if(e == null) {
                        onDoneSaveTransaction();
                    }
                    else {
                        Log.e("formalchat", "Error saving: " + e.getMessage());
                        showUploadNotification(
                                activity,
                                R.string.picture_upload_notif_title,
                                R.string.picture_upload_notif_text_warning,
                                R.drawable.upload_icon_wrong,
                                false);
                    }
                }
            });
        }
        else {
            // -----------------------------//
            // This is NOT allowed;
            // Remove Toast form "ADD" image button
            // -----------------------------//
//            userImages.saveEventually(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e == null) {
//                        Log.v("formalchat", "saveEventually: It was saved Successfully");
////                        saveToLocalStorage(drawableL);
//                        userImages.setUserName(userName);
//                        userImages.setPhotoFile(imgFile);
//                        userImages.saveInBackground();
//
//                    } else {
//                        Log.v("formalchat", "saveEventually: " + e.getMessage());
//                    }
//                }
//            });
        }
    }

    private boolean isNetworkAvailable(Activity activity) {
        if(activity != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }

    private void onDoneSaveTransaction() {
        Log.e("formalchat", "Photo was saved Successfully !");
        //hide notification for uploading - or just show error on the same notification
        hideUploadNotification();
        sendBroadcastMessage();
    }

    private void sendBroadcastMessage() {
        Intent sender = new Intent(ACTION);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(sender);
    }

    private void showUploadNotification(Activity activity, int titleId, int textId, int drawableId, boolean ongoing) {
        notificationManager = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(activity);
        notificationBuilder.setContentTitle("Picture Upload")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.upload_icon)
                .setOngoing(true);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int progress;
                        // Do the operation 20 times
                        for (progress = 0; progress <= 100; progress+= 5) {
                            // Set the progress indicator to (max value, current completition percentage, determinate state)
                            notificationBuilder.setProgress(100, progress, true);
                            notificationManager.notify(id, notificationBuilder.build());
                        }
                    }
                }
        ).start();
    }

    private void hideUploadNotification() {
        notificationBuilder.setContentText("Upload complete")
                .setOngoing(false)
                .setProgress(0, 0, false);
        notificationManager.notify(id, notificationBuilder.build());
    }

    private ParseFile drawableToParseFile(Drawable drawable) {
        byte[] bitmapdata = drawableToByteArray(drawable);
        return new ParseFile(imageName, bitmapdata);
    }

    private byte[] drawableToByteArray(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);

        return stream.toByteArray();
    }

    private Drawable getSelectedImage(Intent data, int requestCode) {
        if(requestCode == ACTIVITY_SELECT_IMAGE) {
            return getGalleryImage(data);
        }
        else if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            return getCapturedImage();
        }
        return null;
    }

    private Drawable getCapturedImage() {
        Drawable drawable;
        if(fileUri != null) {
            imgCameraPath = fileUri.getPath();
            Bitmap lessResolutionImage = getLessResolutionImg(imgCameraPath, 800);
            drawable = new BitmapDrawable(getResources(), lessResolutionImage);
            return drawable;
        }
        return null;
    }

//    private Drawable getThumbImage(Intent data) {
//        if(data != null) {
//            return getDrawableFromIntent(data, 300);
//        }
//        return null;
//    }

//    private Drawable getCameraImageThumbnail(Drawable drawable, int maxImgSize) {
//        Bitmap bitmapFromDrawable = ((BitmapDrawable)drawable).getBitmap();
//        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmapFromDrawable, maxImgSize, maxImgSize);
//
//        return new BitmapDrawable(getResources(), thumbBitmap);
//    }

    private Drawable getGalleryImage(Intent data) {
        if(data != null) {
            return getDrawableFromIntent(data, 800);
        }
        return null;
    }

    private Drawable getDrawableFromIntent(Intent data, int maxImgSize) {
        Drawable drawable;

        Uri selectedImage = data.getData();
        if(selectedImage != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = this.getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap yourSelectedImage = getLessResolutionImg(filePath, maxImgSize);
            Bitmap compressedImage = compressBitmapImg(yourSelectedImage);
            drawable = new BitmapDrawable(getResources(), compressedImage);
            return drawable;
        }

        return null;
    }

    private void processTakenImage(Intent data, int maxImgSize) {
        Uri selectedImage = data.getData();
        if(selectedImage != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = this.getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();


            showUploadNotification(
                    getActivity(),
                    R.string.picture_upload_notif_title,
                    R.string.picture_upload_notif_text,
                    R.drawable.upload_icon,
                    true);
            new MyAsyncTask(getActivity()).execute(filePath, maxImgSize);
        }
    }

    private class MyAsyncTask extends AsyncTask<Object, Void, Drawable> {
        Activity activity;

        public MyAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Drawable doInBackground(Object... params) {
            Drawable drawable;
            String filePath = (String)params[0];
            int maxImgSize = (int)params[1];

            Bitmap yourSelectedImage = getLessResolutionImg(filePath, maxImgSize);
            Bitmap compressedImage = compressBitmapImg(yourSelectedImage);
            drawable = new BitmapDrawable(activity.getApplicationContext().getResources(), compressedImage);

            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if(drawable != null) {
                Log.v("formalchat", "CONTEXT = " + activity);

                //show notification for uploading
                saveToParse(activity, drawable);
            }
        }
    }

    private Bitmap compressBitmapImg(Bitmap bitmapImg) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
        byte[] imageArray = outputStream.toByteArray();

        return BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
    }

    private Bitmap getLessResolutionImg(String filePath, int maxTargetLength) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        //int maxTargetLength = 400;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxTargetLength, maxTargetLength);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    private int getScreenWidth() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {
        File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());

        // Create the storage directory if it does not exist
        if (! root.exists()){
            root.mkdirs();
        }

        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(root, imageName);
        }
        else {
            return null;
        }

        return mediaFile;
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
