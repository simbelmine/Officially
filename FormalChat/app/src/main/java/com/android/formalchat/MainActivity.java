package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

/**
 * Created by Sve on 1/28/15.
 */
public class MainActivity extends Activity {
    public static final String PREFS_NAME = "FormalChatPrefs";
    private TextView userName;
    private ParseUser currentUser;
    private Button logOutButton;
    private Button profileButton;
    private Boolean exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exit = false;

        userName = (TextView) findViewById(R.id.user_name);
        currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            userName.setText(currentUser.getUsername());
        } else {
            // show the signup or login screen
            launchLoginActivity();
        }

        logOutButton = (Button) findViewById(R.id.log_out);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                launchLoginActivity();
            }
        });

        profileButton = (Button) findViewById(R.id.to_tutorial);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchProfileActivity();
            }
        });
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void launchProfileActivity() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if(isLoggedIn()){
            if(exit) {
                finish();
            }
            else {
                //The Handler here handles accidental back presses,
                // it simply shows a Toast, and if there is another back press within 3 seconds,
                // it closes the application.
                Toast.makeText(this, getString(R.string.back_to_exit), Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000 );
            }
        }
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        if(sharedPreferences.getBoolean("loggedIn", false)) {
            return true;
        }
        return false;
    }
}
