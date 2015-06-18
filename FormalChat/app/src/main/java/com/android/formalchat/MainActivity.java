package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

/**
 * Created by Sve on 1/28/15.
 */
public class MainActivity extends DrawerActivity {
    public static final String PREFS_NAME = "FormalChatPrefs";
    public static final int NONE = 101;
    private SharedPreferences sharedPreferences;
    private ParseUser currentUser;
    private Boolean exit;
    private DrawerLayout drawerLayout;
    private GridView people_GridView;
    private ImageButton grid_list_btn;
    private boolean isGrid;
    private ListView people_ListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.main_activity, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
        exit = false;
        isGrid = false;

        setTitle();
        initSharedPreferences();

        currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // show the signup or login screen
            launchLoginActivity();
        }

        people_GridView = (GridView) findViewById(R.id.people_gridview);
        people_ListView = (ListView) findViewById(R.id.people_listview);

        grid_list_btn = (ImageButton) findViewById(R.id.grid_list_btn);
        initGridListBtn();
        setOnClickListeners();
    }

    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
    }

    private void initGridListBtn() {
        if(!sharedPreferences.contains("isGrid")) {
            grid_list_btn.setImageResource(R.drawable.list);
            setPplGridView();
        }
        else {
            if (sharedPreferences.getBoolean("isGrid", false)) {
                grid_list_btn.setImageResource(R.drawable.list);
                setPplGridView();
            } else {
                grid_list_btn.setImageResource(R.drawable.grid);
                setPplListView();
            }
        }
    }

    private void setOnClickListeners() {
        grid_list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGrid) {
                    isGrid = false;
                    grid_list_btn.setImageResource(R.drawable.grid);
                    setGridListStatus();
                    setPplListView();
                }
                else {
                    isGrid = true;
                    grid_list_btn.setImageResource(R.drawable.list);
                    setGridListStatus();
                    setPplGridView();
                }
            }
        });
    }

    private void setPplListView() {
        people_ListView.setAdapter(new PeopleListViewAdapter(getApplicationContext()));
        people_GridView.setVisibility(View.GONE);
        people_ListView.setVisibility(View.VISIBLE);
    }

    private void setPplGridView() {
        people_GridView.setAdapter(new PeopleGridViewAdapter(getApplicationContext()));
        people_ListView.setVisibility(View.GONE);
        people_GridView.setVisibility(View.VISIBLE );
    }

    private void setGridListStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isGrid", isGrid);
        editor.commit();
    }

    private void setTitle() {
        int title_position = getIntent().getIntExtra("title_position", NONE);
        if(title_position != NONE) {
            setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
        else {
            setTitle(getResources().getStringArray(R.array.menu_list)[0]);
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
        if(sharedPreferences.getBoolean("loggedIn", false)) {
            return true;
        }
        return false;
    }
}
