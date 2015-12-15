package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Sve on 12/15/15.
 */
public class BaseActivity extends AppCompatActivity {

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public Snackbar getSnackbar(Activity activity, int messageId, int colorId) {
//        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.snackbar_location);

        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),  //                coordinatorLayout,
                getResources().getString(messageId),
                Snackbar.LENGTH_LONG
        );
        ViewGroup snackBarView = (ViewGroup) snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), colorId));
        TextView tv = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
//        tv.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), colorId));
        tv.setTextSize(16);
        return snackbar;
    }
}
