package com.android.formalchat;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseUser;

/**
 * Created by Sve on 3/26/15.
 */
public class DrawerActivity extends FragmentActivity {
    public static final int NONE = 101;
    private DrawerLayout drawerLayout;
    private ListView leftDrawerList;
    private String[] listElements;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence title;
    private CharSequence drawerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);

        title = drawerTitle = getTitle();
        listElements = getResources().getStringArray(R.array.menu_list);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawerList = (ListView) findViewById(R.id.left_drawer);
        initDrawableToggle();
        initActionBar();

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        drawerLayout.setDrawerListener(drawerToggle);
        leftDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, listElements));
        setListOnClickItemListener();
    }

    private void initActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        // Hide Action Bar icon and text
        getActionBar().setDisplayShowHomeEnabled(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(leftDrawerList);
        //menu.findItem(R.id.search_button).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void initDrawableToggle() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //updateTitle(title);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //updateTitle(drawerTitle);
            }
        };
    }

    private void updateTitle(CharSequence title) {
        getActionBar().setTitle(title);
        invalidateOptionsMenu();
    }

    private void setListOnClickItemListener() {
        leftDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
    }

    private void selectItem(int position) {
        switch (position) {
            case 0:
                onItemSelected(position);
                launchActivity(MainActivity.class, position);
                break;
            case 1:
                onItemSelected(position);
                launchActivity(ProfileActivity.class, position);
                break;
            case 2:
                logOut();
                break;
        }
    }

    private void launchActivity(Class classToCall, int title_position) {
        Intent intent = new Intent(DrawerActivity.this, classToCall);
        intent.putExtra("title_position", title_position);
        startActivity(intent);
    }

    private void onItemSelected(int position) {
        leftDrawerList.setItemChecked(position, true);
        setTitle(listElements[position]);
        drawerLayout.closeDrawer(leftDrawerList);
    }

    private void logOut() {
        ParseUser.logOut();
        launchActivity(LoginActivity.class, NONE);
    }
}
