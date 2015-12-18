package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sve on 12/15/15.
 */
public class BaseActivity extends AppCompatActivity {
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MINUTES = 5; // minutes in which to update last seen
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private ParseUser currentUser;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ParseUser.getCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLastSeen();
    }

    private void updateLastSeen() {
        if(currentUser != null) {
            String lastSeenTimeStamp = (String)currentUser.get("lastSeen");
            Log.v(ApplicationOfficially.TAG, "Last seen = " + lastSeenTimeStamp);

            if(lastSeenTimeStamp != null) {
                long lastSeenInMillis = getLastSeenInMillis(lastSeenTimeStamp);
                if(lastSeenInMillis != 0) {
                    if(isTimeForUpdate(lastSeenInMillis)) {
                        updateParseUserLastSeen();
                    }
                }
            }
            else {
                updateParseUserLastSeen();
            }
        }
    }

    private void updateParseUserLastSeen() {
        if(currentUser != null) {
            currentUser.put("lastSeen", dateFormat.format(new Date(System.currentTimeMillis())));
            currentUser.saveInBackground();
        }
    }

    private boolean isTimeForUpdate(long lastSeenInMillis) {
        long updateIn = MILLIS_IN_SECOND * SECONDS_IN_MINUTE * MINUTES;

        if(Math.abs(lastSeenInMillis - System.currentTimeMillis()) > updateIn) {
            return true;
        }
        return false;
    }

    private long getLastSeenInMillis(String lastSeenTimeStamp) {
        Date resultDate;
        try {
            resultDate = dateFormat.parse(lastSeenTimeStamp);
            return resultDate.getTime();
        }
        catch (ParseException ex) {
            Log.e(ApplicationOfficially.TAG, "Get Last Seen In Millis exception : " + ex.getMessage());
        }

        return 0;
    }

    public boolean isUserOnline(ParseUser remoteUser) {
        long onlineProbability = MILLIS_IN_SECOND * SECONDS_IN_MINUTE * MINUTES;
        String lastSeenString = (String)remoteUser.get("lastSeen");

        if(lastSeenString != null) {
            long remoteUserTimeStamp = getLastSeenInMillis(lastSeenString);

            if (remoteUserTimeStamp != 0) {
                if (Math.abs(System.currentTimeMillis() - remoteUserTimeStamp) <= onlineProbability) {
                    return true;
                }
            }
        }

        return false;
    }
}
