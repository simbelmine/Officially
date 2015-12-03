package com.android.formalchat.tutorial;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.formalchat.ApplicationOfficially;
import com.android.formalchat.LoginActivity;
import com.android.formalchat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 1/29/15.
 */
public class TutorialPagerActivity extends FragmentActivity {
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;
    private static final int REQUEST_APP_SETTINGS = 1;
    private static final String[] PERMISSIONS = {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String PREFS_NAME = "FormalChatPrefs";
    private TutorialPagerAdapter mTutorialPagerAdapter;
    private ViewPager mViewPager;
    private Button mLoginButton;
    //    private Button btn1;
//    private Button btn2;
//    private Button btn3;
    private ImageView btn1;
    private ImageView btn2;
    private ImageView btn3;

    private boolean isAllPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(isBiggerOrEqualToAPI23()) {
            checkForPermissions();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_pager_layout);

        mTutorialPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), this);

        btn1 = (ImageView) findViewById(R.id.btn1);
        btn2 = (ImageView) findViewById(R.id.btn2);
        btn3 = (ImageView) findViewById(R.id.btn3);
        mLoginButton = (Button) findViewById(R.id.login_redirect_btn);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAllPermissionsGranted) {
                    saveToSharedPrefs();
                    launchLoginActivity();
                }
                else {
                    if(isBiggerOrEqualToAPI23()) {
                        checkForPermissions();
                    }
                }
            }
        });
        initButtons();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTutorialPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageSelected(int position) {
                buttonAction(position);
            }
        });
    }

    private boolean isBiggerOrEqualToAPI23() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M){
            return true;
        } else{
            return false;
        }
    }

    private void checkForPermissions() {
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
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
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
            }
            else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
        else {
            isAllPermissionsGranted = true;
        }
    }

    private void goToAppPermissionSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myAppSettings);
    }

    private boolean isPermissionAdded(List<String> permissionsList, String permission) {
        if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if(!shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener settingsListener) {
        new AlertDialog.Builder(TutorialPagerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Settings", settingsListener)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int permission_denied = -1;
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                for(int result : grantResults) {
                    if(result == permission_denied) {
                        return;
                    }
                }
                isAllPermissionsGranted = true;
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initButtons() {
        btn1.setBackgroundColor(getResources().getColor(R.color.gray));
        btn2.setBackgroundColor(getResources().getColor(R.color.items));
        btn3.setBackgroundColor(getResources().getColor(R.color.items));
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void buttonAction(int position) {
        switch (position) {
            case 0: btn1.setBackgroundColor(getResources().getColor(R.color.gray));
                btn2.setBackgroundColor(getResources().getColor(R.color.items));
                btn3.setBackgroundColor(getResources().getColor(R.color.items));
//                mLoginButton.setVisibility(View.INVISIBLE);
                break;
            case 1: btn1.setBackgroundColor(getResources().getColor(R.color.items));
                btn2.setBackgroundColor(getResources().getColor(R.color.gray));
                btn3.setBackgroundColor(getResources().getColor(R.color.items));
//                mLoginButton.setVisibility(View.INVISIBLE);
                break;
            case 2: btn1.setBackgroundColor(getResources().getColor(R.color.items));
                btn2.setBackgroundColor(getResources().getColor(R.color.items));
                btn3.setBackgroundColor(getResources().getColor(R.color.gray));
//                mLoginButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isSharedPreferencesEmpty()) {
            launchLoginActivity();
        }
    }

    private void saveToSharedPrefs() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("firstAttempt", true);
        editor.commit();
    }

    private boolean isSharedPreferencesEmpty() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if(!settings.getBoolean("firstAttempt", false)) {
            return true;
        }
        return false;
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(TutorialPagerActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
