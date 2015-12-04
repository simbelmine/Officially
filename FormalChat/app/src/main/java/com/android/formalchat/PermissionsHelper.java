package com.android.formalchat;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 12/2/15.
 */
public class PermissionsHelper {
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;
    private static final String[] PERMISSIONS =
            {
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
    private Activity activity;
    public boolean isAllPermissionsGranted = false;

    public PermissionsHelper(Activity activity) {
        this.activity = activity;
    }

    public boolean isBiggerOrEqualToAPI23() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M){
            return true;
        } else{
            return false;
        }
    }



    public void checkForPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();

        for(String permission : PERMISSIONS) {
            if(!isPermissionAdded(permissionsList, permission)) {
                permissionsNeeded.add(permission);
            }
        }

        if(permissionsList.size() > 0) {
            if(permissionsNeeded.size() > 0) {
                showMessageOKCancel("To have better experience please allow us the following permissions, or go to App Setings.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        }
                        ,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                goToAppPermissionSettings();
                            }
                        }
                );
            } else {
                activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
        else {
            isAllPermissionsGranted = true;
        }
    }

    public void goToAppPermissionSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(myAppSettings);
    }

    public boolean isPermissionAdded(List<String> permissionsList, String permission) {
        if(activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if(!activity.shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }

    public void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener settingsListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Settings", settingsListener)
                .create()
                .show();
    }
}
