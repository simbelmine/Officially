package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sve on 4/2/15.
 */
public class VideoRecordActivity extends Activity implements View.OnClickListener{
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final String VIDEO_EXTENSION = ".mp4";
    private static final String VIDEO_OUT_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/";
    private static final String VIDEO_OUT_NAME = "out_VID_intro.mp4";
    private static final String VIDEO_OUT_FILE_PATH = VIDEO_OUT_FOLDER + VIDEO_OUT_NAME;
    private Button startRecordingBtn;


    String workFolder = null;
    String initialVideoFolder = null;
    String initialVideoPath = null;
    String initialVideoPathOut = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        setContentView(R.layout.video_record_layout);

        init();
        initVideoUtils();
        setOnclickListeners();
    }

    private void initActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(false);
        // Hide Action Bar icon and text
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_gray)));
    }

    private void init() {
        startRecordingBtn = (Button) findViewById(R.id.start_record_video_btn);
    }

    private void initVideoUtils() {
        initialVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
        initialVideoPath = initialVideoFolder + "in" + VIDEO_EXTENSION;
        initialVideoPathOut = initialVideoFolder + "out" + VIDEO_EXTENSION;

        Log.i(Prefs.TAG, "VideoRecordActivity: " + getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(getApplicationContext()));
        workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
        Log.i(Prefs.TAG, "VideoRecordActivity: workFolder (license and logs location) path: " + workFolder);
        vkLogPath = workFolder + "vk.log";
        Log.i(Prefs.TAG, "VideoRecordActivity: vk log (native log) path: " + vkLogPath);

        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(this, workFolder);
        GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(this, initialVideoFolder);
        int rc = GeneralUtils.isLicenseValid(getApplicationContext(), initialVideoFolder);
        Log.i(Prefs.TAG, "VideoRecordActivity: License check RC: " + rc);
    }

    private void setOnclickListeners() {
        startRecordingBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record_video_btn:
                dispatchTakeVideoIntent();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String destinationFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/video_in/";
        String videoName;

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            videoName = getVideoName(videoUri);
            compressVideo(destinationFolder, videoName);
            showCompleteMessage();
            finish();
        }
    }

    private void showCompleteMessage() {
        Toast.makeText(this, "Your Video will appear shortly in your Gallery.", Toast.LENGTH_LONG).show();
    }

    private String getVideoName(Uri videoUri) {
        String videoUrl = videoUri.getPath();
        return videoUrl.substring(videoUrl.lastIndexOf("/")+1);
    }

    private void compressVideo(String startFolder, String videoName) {
        //     GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(this, startFolder);

//        Intent intent = new Intent(this, VideoCompressService.class);
//        intent.putExtra("destinationFolder", startFolder);
//        intent.putExtra("videoName", videoName);
//
//        startService(intent);

        Log.i(Prefs.TAG, "VideoRecordActivity: run clicked.");
        if (GeneralUtils.checkIfFileExistAndNotEmpty(initialVideoPath)) {
            new TranscdingBackground(this).execute();
        }
        else {
            Toast.makeText(getApplicationContext(), initialVideoPath + " not found", Toast.LENGTH_LONG).show();
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            // Performing this check is important because if you call startActivityForResult()
            // using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            Uri videoUri = Uri.fromFile(getMediaFileUri());
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private File getMediaFileUri() {
        File root = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/video_in/");
        // Create the storage directory if it does not exist
        if (! root.exists()){
            root.mkdirs();
        }
        // Create a media file name
        String videoName = "VID_intro";
        //File mediaFile = new File(root, videoName + VIDEO_EXTENSION);
        File mediaFile = new File(initialVideoPath);
        return mediaFile;
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }


    public class TranscdingBackground extends AsyncTask<String, Integer, Integer>
    {
        Activity _act;

        public TranscdingBackground (Activity act) {
            _act = act;
        }

        protected Integer doInBackground(String... paths) {
            Log.i(Prefs.TAG, "VideoRecordActivity: doInBackground started...");

            // delete previous log
            GeneralUtils.deleteFileUtil(workFolder + "vk.log");

            PowerManager powerManager = (PowerManager)_act.getSystemService(Activity.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
            Log.d(Prefs.TAG, "VideoRecordActivity: Acquire wake lock");
            wakeLock.acquire();

            //String commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -vf transpose=1 -s 160x120 -r 30 -aspect 4:3 -ab 48000 -ac 2 -ar 22050 -b 2097k /sdcard/videokit/out.mp4";
            String commandStr = "ffmpeg -y -i " + initialVideoPath + " -strict experimental -s 320x240 -r 25 -vcodec mpeg4 -b 1000k -ab 64k -ac 2 -ar 22050 " + initialVideoPathOut;


            ///////////// Set Command using code (overriding the UI EditText) /////
            //String commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
            //String[] complexCommand = {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental","-s", "160x120","-r","25", "-vcodec", "mpeg4", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", "/sdcard/videokit/out.mp4"};
            ///////////////////////////////////////////////////////////////////////


            LoadJNI vk = new LoadJNI();
            try {

                //vk.run(complexCommand, workFolder, getApplicationContext());
                vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());

                // running without command validation
                //vk.run(complexCommand, workFolder, getApplicationContext(), false);

                // copying vk.log (internal native log) to the videokit folder
                GeneralUtils.copyFileToFolder(vkLogPath, initialVideoFolder);

            } catch (CommandValidationException e) {
                Log.e(Prefs.TAG, "VideoRecordActivity: vk run exeption.", e);
                commandValidationFailedFlag = true;

            } catch (Throwable e) {
                Log.e(Prefs.TAG, "VideoRecordActivity: vk run exeption.", e);
            }
            finally {
                if (wakeLock.isHeld())
                    wakeLock.release();
                else{
                    Log.i(Prefs.TAG, "VideoRecordActivity: Wake lock is already released, doing nothing");
                }
            }
            Log.i(Prefs.TAG, "VideoRecordActivity: doInBackground finished");
            return Integer.valueOf(0);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onCancelled() {
            Log.i(Prefs.TAG, "VideoRecordActivity: onCancelled");
            //progressDialog.dismiss();
            super.onCancelled();
        }


        @Override
        protected void onPostExecute(Integer result) {
            Log.i(Prefs.TAG, "VideoRecordActivity: onPostExecute");
            super.onPostExecute(result);

            // finished Toast
            String rc = null;
            if (commandValidationFailedFlag) {
                rc = "Command Vaidation Failed";
            }
            else {
                rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
            }
            final String status = rc;


            Log.v("formalchat", "VideoRecordActivity: STATUS = " + status);
            if(status.equals("Transcoding Status: Finished OK")) {
                moveOutVideoToProjectDir();
                deleteOldFile();

                createVideoThumbnail();
                startUploadService();
            }
        }
    }

    private void moveOutVideoToProjectDir() {
        if(new File(initialVideoPath).exists()) {
            try {
                File sourceFile = new File(initialVideoPathOut);
                File destinationFile = new File(VIDEO_OUT_FILE_PATH);
                FileUtils.copyFile(sourceFile, destinationFile);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteOldFile() {
        if(new File(initialVideoPath).exists()) {
            new File(initialVideoFolder + "out.mp4").delete();
        }
    }

    private void createVideoThumbnail() {
        if(new File(VIDEO_OUT_FILE_PATH).exists()) {
            File videoThumbnailFile = new File(VIDEO_OUT_FOLDER, "thumbnail_video.jpg");
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(VIDEO_OUT_FILE_PATH,
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            Bitmap playIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.play_g_white);
            Bitmap overlayedBitmap = overlayThumbnailWithPlayIcon(thumb, playIcon);

            saveThumbnailToLocal(videoThumbnailFile, overlayedBitmap);
        }
    }

    private Bitmap overlayThumbnailWithPlayIcon(Bitmap thumb, Bitmap playImage) {
        Bitmap overlayBmp = Bitmap.createBitmap(thumb.getWidth(), thumb.getHeight(), thumb.getConfig());
        float columnWidth = getResources().getDimension(R.dimen.grid_column_width_gallery);
        Bitmap play = Bitmap.createScaledBitmap(playImage, (int)columnWidth/2, (int)columnWidth/2, true);

        Canvas canvas = new Canvas(overlayBmp);
        canvas.drawBitmap(thumb, new Matrix(), null);
        canvas.drawBitmap(play, (int) columnWidth / 2 - (play.getHeight() / 3), (int) columnWidth / 2, null);

        overlayBmp = compressBitmapImg(overlayBmp);

        return overlayBmp;
    }

    private Bitmap compressBitmapImg(Bitmap bitmapImg) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] imageArray = outputStream.toByteArray();

        return BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
    }

    private void saveThumbnailToLocal(File videoThumbnailFile, Bitmap thumb) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(videoThumbnailFile);
            thumb.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void startUploadService() {
        Intent intent = new Intent(VideoRecordActivity.this, VideoUploadService.class);
        intent.putExtra("destinationFolder", VIDEO_OUT_FOLDER);
        intent.putExtra("out_videoName", VIDEO_OUT_NAME);
        startService(intent);
    }
}
