package com.android.formalchat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Sve on 4/13/15.
 */
public class VideoCompressService extends IntentService {

    private String ffmpeg_workFolder = null;
    private String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;
    private String destinationFolder;
    private String destinationVideoPath;
    private String videoName;
    private String out_videoName;

    private String destinationVideoPathOut;

    public VideoCompressService() {
        super("VideoCompressService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        videoName = intent.getStringExtra("videoName");
        out_videoName = "out_" + videoName;

        destinationFolder = intent.getStringExtra("destinationFolder");
        destinationVideoPath = destinationFolder + videoName;
        destinationVideoPathOut = destinationFolder + out_videoName;

        Log.i("formalchat", getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(getApplicationContext()));
//        workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
//        Log.i("formalchat", "workFolder (license and logs location) path: " + workFolder);
//        vkLogPath = workFolder + "vk.log";
//        Log.i("formalchat", "vk log (native log) path: " + vkLogPath);

//        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(activity, workFolder);
//        GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(activity, demoVideoFolder);

        //workFolder = destinationFolder;
        //vkLogPath = workFolder + "vk.log";
        ffmpeg_workFolder = getApplicationContext().getFilesDir() + "/";
        vkLogPath = ffmpeg_workFolder + "vk.log";
        Log.v("formalchat", "### vkLogPath = " + vkLogPath);


        if (GeneralUtils.checkIfFileExistAndNotEmpty(destinationVideoPath)) {
            new TranscdingBackground().execute();
        }
        else {
            Toast.makeText(getApplicationContext(), destinationFolder + " not found", Toast.LENGTH_LONG).show();
        }

        int rc = GeneralUtils.isLicenseValid(getApplicationContext(), getApplicationContext().getFilesDir() + "/");
        Log.i("formalchat", "License check RC: " + rc);
    }

    public class TranscdingBackground extends AsyncTask<String, Integer, Integer>
    {
        public TranscdingBackground (){}


        @Override
        protected void onPreExecute() {
            Log.v("formalchat", "FFmpeg4Android Transcoding in progress...");
        }

        protected Integer doInBackground(String... paths) {
            Log.i("formalchat", "doInBackground started...");

            // delete previous log
            //GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
//            GeneralUtils.deleteFileUtil(destinationFolder + "/vk.log");

            PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Activity.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
            Log.d("formalchat", "Acquire wake lock");
            wakeLock.acquire();


            String commandStr = "ffmpeg -y -i "+ destinationVideoPath +
                    " -strict experimental -s 320x240 -r 25 -vcodec mpeg4 -b 1000k -ab 64k -ac 2 -ar 22050 " +
                    destinationVideoPathOut;
            Log.e("formalchat", commandStr);

            ///////////// Set Command using code (overriding the UI EditText) /////
            //String commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
            //String[] complexCommand = {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental","-s", "160x120","-r","25", "-vcodec", "mpeg4", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", "/sdcard/videokit/out.mp4"};
            ///////////////////////////////////////////////////////////////////////


            LoadJNI vk = new LoadJNI();
            try {

                //vk.run(complexCommand, workFolder, getApplicationContext());
                vk.run(GeneralUtils.utilConvertToComplex(commandStr), ffmpeg_workFolder, getApplicationContext());

                // running without command validation
                //vk.run(complexCommand, workFolder, getApplicationContext(), false);

                // copying vk.log (internal native log) to the videokit folder
                GeneralUtils.copyFileToFolder(vkLogPath, destinationFolder);

            } catch (CommandValidationException e) {
                Log.e("formalchat", "vk run exeption.", e);
                commandValidationFailedFlag = true;

            } catch (Throwable e) {
                Log.e("formalchat", "vk run exeption.", e);
            }
            finally {
                if (wakeLock.isHeld())
                    wakeLock.release();
                else{
                    Log.i("formalchat", "Wake lock is already released, doing nothing");
                }
            }
            Log.i("formalchat", "doInBackground finished");
            return Integer.valueOf(0);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onCancelled() {
            Log.i("formalchat", "onCancelled");
            super.onCancelled();
        }


        @Override
        protected void onPostExecute(Integer result) {
            Log.i("formalchat", "onPostExecute");
            super.onPostExecute(result);

            // finished Toast
            String rc = null;
            if (commandValidationFailedFlag) {
                rc = "Command validation failed.";
            }
            else {
                rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
            }
            final String status = rc;

            Log.v("formalchat", "### status = " + status);


            if (status.equals("Transcoding Status: Failed")) {
                Log.v("formalchat", "Check: " + vkLogPath + " for more information.");
            }
            else if(status.equals("Transcoding Status: Finished OK")) {
                //Toast.makeText(VideoCompressService.this, status, Toast.LENGTH_LONG).show();
//                Toast.makeText(VideoCompressService.this, "Your Video will appear shortly on your wall.", Toast.LENGTH_LONG).show();

                startUploadService(destinationFolder, out_videoName);

//                File videoFile = getVideoFile(destinationFolder);
//                saveVideoToParse(videoFile, out_videoName);
            }
        }

        private void startUploadService(String destinationFolder, String out_videoName) {
            Intent intent = new Intent(VideoCompressService.this, VideoUploadService.class);
            intent.putExtra("destinationFolder", destinationFolder);
            intent.putExtra("out_videoName", out_videoName);
            startService(intent);
        }
    }

}
