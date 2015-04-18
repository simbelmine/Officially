package com.android.formalchat;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

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

    public String imageName = "IMG_"+ getTimeStamp() + ".jpg";
    private ImageButton mTakePhotoImg;
    private ImageButton mAttachPhotoImg;
    private Uri fileUri;
    private ProfileActivity callingActivity;
    private String imgCameraPath;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int id = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_img_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        mTakePhotoImg = (ImageButton) view.findViewById(R.id.take_photo_img);
        mTakePhotoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        mAttachPhotoImg = (ImageButton) view.findViewById(R.id.attach_photo_img);
        mAttachPhotoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Drawable drawable;

        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (requestCode == ACTIVITY_SELECT_IMAGE) {
                    drawable = getSelectedImage(data, requestCode);
                    saveToParse(getActivity(), drawable);
                }
            }
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                drawable = getSelectedImage(data, requestCode);
                saveToParse(getActivity(), drawable);
            }
            getDialog().dismiss();
        }
    }

    private void saveToLocalStorage(Drawable drawable) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        boolean success = true;
        if(!folder.exists()) {
            success = folder.mkdir();
        }

        if(success) {
            File picture = new File(folder, imageName);
            FileOutputStream out;
            try {
                out = new FileOutputStream(picture);
                Bitmap bitmapImage = ((BitmapDrawable) drawable).getBitmap();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
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

        if(isNetworkAvailable()) {
            userImages.setUserName(userName);
            userImages.setPhotoFile(imgFile);
            //show notification for uploading
            showUploadNotification();
            userImages.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    saveToLocalStorage(drawableL);
                    onDoneSaveTransaction(activity, e);
                }
            });
        }
        else {
            // -----------------------------//
            // This is NOT allowed;
            // Remove Toast form "ADD" image button
            // -----------------------------//
            userImages.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.v("formalchat", "saveEventually: It was saved Successfully");
                        saveToLocalStorage(drawableL);
                        userImages.setUserName(userName);
                        userImages.setPhotoFile(imgFile);
                        userImages.saveInBackground();

                    } else {
                        Log.v("formalchat", "saveEventually: " + e.getMessage());
                    }
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void onDoneSaveTransaction(Activity activity, ParseException e) {
        if (e == null) {
            Log.e("formalchat", "Photo was saved Successfully !");
            //hide notification for uploading - or just show error on the same notification
            hideUploadNotification();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, activity.getIntent());
        }
        else {
            Log.e("formalchat", "Error saving: " + e.getMessage());
        }
    }

    private void showUploadNotification() {
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(getActivity());
        notificationBuilder.setContentTitle("Picture Upload")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_launcher)
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
            Bitmap lessResolutionImage = getLessResolutionImg(imgCameraPath);
            drawable = new BitmapDrawable(getResources(), lessResolutionImage);
            return drawable;
        }
        return null;
    }

    private Drawable getGalleryImage(Intent data) {
        Drawable drawable;
        if(data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap yourSelectedImage = getLessResolutionImg(filePath);
            Bitmap compressedImage = compressBitmapImg(yourSelectedImage);
            drawable = new BitmapDrawable(getResources(), compressedImage);
            return drawable;
        }
        return null;
    }

    private Bitmap compressBitmapImg(Bitmap bitmapImg) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        byte[] imageArray = outputStream.toByteArray();

        return BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
    }

    private Bitmap getLessResolutionImg(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int maxTargetLength = 800;

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
