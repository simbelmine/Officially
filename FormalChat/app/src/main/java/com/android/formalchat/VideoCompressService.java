package com.android.formalchat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

/**
 * Created by Sve on 4/13/15.
 */
public class VideoCompressService extends IntentService {
    Context context;
    String workFolder = null;
    String demoVideoFolder = null;
    String demoVideoPath = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;

    public VideoCompressService() {
        super("VideoCompressService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/";
        demoVideoPath = demoVideoFolder + "in.mp4";

        Log.i(Prefs.TAG, getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(getApplicationContext()));
        workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
        Log.i(Prefs.TAG, "workFolder (license and logs location) path: " + workFolder);
        vkLogPath = workFolder + "vk.log";
        Log.i(Prefs.TAG, "vk log (native log) path: " + vkLogPath);

//        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(activity, workFolder);
//        GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(activity, demoVideoFolder);

        if (GeneralUtils.checkIfFileExistAndNotEmpty(demoVideoPath)) {
            new TranscdingBackground().execute();
        }
        else {
            Toast.makeText(getApplicationContext(), demoVideoPath + " not found", Toast.LENGTH_LONG).show();
        }

        int rc = GeneralUtils.isLicenseValid(getApplicationContext(), workFolder);
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
            GeneralUtils.deleteFileUtil(workFolder + "/vk.log");

            PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Activity.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
            Log.d("formalchat", "Acquire wake lock");
            wakeLock.acquire();


            String commandStr = "ffmpeg -y -i "+demoVideoFolder+"in.mp4 -strict experimental -s 160x120 -r 25 -vcodec mpeg4 -b 150k -ab 48000 -ac 2 -ar 22050 "+demoVideoFolder+"out.mp4";
            Log.e("formalchat", commandStr);

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
                GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);

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
                rc = "Command Vaidation Failed";
            }
            else {
                rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
            }
            final String status = rc;


            Toast.makeText(VideoCompressService.this, status, Toast.LENGTH_LONG).show();
            if (status.equals("Transcoding Status: Failed")) {
                Log.v("formalchat", "Check: " + vkLogPath + " for more information.");
            }

        }

    }

}
