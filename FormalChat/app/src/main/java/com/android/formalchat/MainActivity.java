package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private ScrollableGridView people_GridView;
    private ScrollableGridView people_GridView_Matches;
    private PeopleGridViewAdapter peopleGridViewAdapter;
    private PeopleGridViewAdapter peopleGridViewAdapterMatches;
    private ImageButton grid_list_btn;
    private boolean isListButtonVisible;
    private ScrollableListView people_ListView;
    private ScrollableListView people_ListView_Matches;
    private PeopleListViewAdapter peopleListViewAdapter;
    private PeopleListViewAdapter peopleListViewAdapterMatches;

    private List<ParseUser> usersList;
    private List<ParseUser> usersListMatches;

    private Spinner searchSpinner;
    private LinearLayout matchesLayout;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.main_activity, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
        exit = false;
        isListButtonVisible = true; // Start always with grid view

        setTitle();
        initSharedPreferences();

        init();

        initGridListBtn();
        setOnClickListeners();
        setOnSpinnerItemSelected();
        setOnRefreshListener();

//        getGridListResults(); // not Needed ,it's called because Spinner position 1 is selected with this
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpinnerPosition();
    }

    private void init() {
        currentUser = ParseUser.getCurrentUser();
        startLoginIfNoUser();
        initSwipeContainer();
        people_GridView_Matches = (ScrollableGridView) findViewById(R.id.people_gridview_matches);
        people_GridView_Matches.setExpanded(true);
        people_GridView = (ScrollableGridView) findViewById(R.id.people_gridview);
        people_GridView.setExpanded(true);

        people_ListView_Matches = (ScrollableListView) findViewById(R.id.people_listview_matches);
        people_ListView = (ScrollableListView) findViewById(R.id.people_listview);

        searchSpinner = (Spinner) findViewById(R.id.search_for);

        matchesLayout = (LinearLayout) findViewById(R.id.matches);
        grid_list_btn = (ImageButton) findViewById(R.id.grid_list_btn);
    }

    private void initSwipeContainer() {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        setSwipeAppearance();
    }

    private void setSwipeAppearance() {
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark
        );
    }

    private void setSpinnerPosition() {
        if(sharedPreferences.contains("spinner_position")) {
            int position = sharedPreferences.getInt("spinner_position", 1);
            searchSpinner.setSelection(position);
            performFilterByPosition(position);
        }
        else {
            searchSpinner.setSelection(1);
        }
    }

    private void startLoginIfNoUser() {
        if (currentUser == null) {
            launchLoginActivity();
        }
    }

    private void getGridListResults() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserQuestionary");
        parseQuery.whereEqualTo("loginName", ParseUser.getCurrentUser().getUsername());
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    matchesLayout.setVisibility(View.VISIBLE);
                    int drinkingCriteria = parseObject.getInt("matchDrinking");
                    int smokingCriteria = parseObject.getInt("matchSmoking");
                    int religionCriteria = parseObject.getInt("matchReligion");
                    int ethnicityCriteria = parseObject.getInt("matchEthnicity");
                    int yourReligionCriteria = parseObject.getInt("yourReligion");
                    int yourEthnicityCriteria = parseObject.getInt("yourEthnicity");

                    if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                        getResultsFromParseCloud(people_GridView_Matches, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria);
                        getResultsFromParseCloud(people_GridView, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria);
                    } else {
                        getResultsFromParseCloud(people_ListView_Matches, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria);
                        getResultsFromParseCloud(people_ListView, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria);
                    }
                } else {
                    matchesLayout.setVisibility(View.GONE);
                    if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                        getResultsFromParseCloud(people_GridView, 0, 0, 0, 0, 0, 0);
                    } else {
                        getResultsFromParseCloud(people_GridView, 0, 0, 0, 0, 0, 0);
                    }
                }

                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void getResultsFromParseCloud(final View view, int drinkingCriteria,
                                          int smokingCriteria, int religionCriteria, int ethnicityCriteria,
                                          int yourReligionCriteria, int yourEthnicityCriteria) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("userName", ParseUser.getCurrentUser().getUsername());
        params.put("drinkingCriteria", drinkingCriteria);
        params.put("smokingCriteria", smokingCriteria);
        params.put("religionCriteria", religionCriteria);
        params.put("ethnicityCriteria", ethnicityCriteria);
        params.put("yourReligionCriteria", yourReligionCriteria);
        params.put("yourEthnicityCriteria", yourEthnicityCriteria);
        if(view == people_GridView || view == people_ListView) {
            params.put("excludeCriteriaFromAllUsers", true);
        }

        ParseCloud.callFunctionInBackground("clientRequest", params, new FunctionCallback<ArrayList<ArrayList>>() {
            @Override
            public void done(ArrayList<ArrayList> listResults, ParseException e) {
                if (e == null && listResults != null) {
                    ArrayList<ParseUser> users = listResults.get(0);

                    if (view == people_GridView || view == people_ListView) {
                        usersList = new ArrayList<>();
                        for (ParseUser user : users) {
                            usersList.add(user);
                        }
                        initAdapter(view, usersList);
                    } else if (view == people_GridView_Matches || view == people_ListView_Matches) {
                        usersListMatches = new ArrayList<>();
                        for (ParseUser user : users) {
                            usersListMatches.add(user);
                        }
                        initAdapter(view, usersListMatches);
                    }

                    swipeContainer.setRefreshing(false);
                } else {
                    Log.v("formalchat", "----- NONE");
                }
            }
        });
    }


    private void initAdapter(View view, List<ParseUser> list){
        if(view == people_GridView)  {
//            if(peopleGridViewAdapter != null) {
//                peopleGridViewAdapter.updateUsers(list);
//            }
//            else {
            peopleGridViewAdapter = new PeopleGridViewAdapter(getApplicationContext(), list);
            ((ScrollableGridView)view).setAdapter(peopleGridViewAdapter);
//            }
        }
        else if(view == people_GridView_Matches) {
//            if(peopleGridViewAdapterMatches != null) {
//                peopleGridViewAdapterMatches.updateUsers(list);
//            }
//            else {
            peopleGridViewAdapterMatches = new PeopleGridViewAdapter(getApplicationContext(), list);
            ((ScrollableGridView)view).setAdapter(peopleGridViewAdapterMatches);
//            }
        }
        else if(view == people_ListView) {
//            if(peopleListViewAdapter != null) {
//                peopleListViewAdapter.updateUsers(list);
//            }
//            else {
            peopleListViewAdapter = new PeopleListViewAdapter(getApplicationContext(), list);
            ((ScrollableListView)view).setAdapter(peopleListViewAdapter);
//            }
        }
        else if(view == people_ListView_Matches) {
//            if(peopleListViewAdapterMatches != null) {
//                peopleListViewAdapterMatches.updateUsers(list);
//            }
//            else {
            peopleListViewAdapterMatches = new PeopleListViewAdapter(getApplicationContext(), list);
            ((ScrollableListView)view).setAdapter(peopleListViewAdapterMatches);
//            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
    }

    private void initGridListBtn() {
        if(!sharedPreferences.contains("isListButtonVisible")) {
            grid_list_btn.setImageResource(R.drawable.list);
            setPplGridView();
        }
        else {
            if (sharedPreferences.getBoolean("isListButtonVisible", false)) {
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
                if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                    isListButtonVisible = false;
                    grid_list_btn.setImageResource(R.drawable.grid);
                    setGridListStatus();
                    setPplListView();
                    setSpinnerPosition();
                } else {
                    isListButtonVisible = true;
                    grid_list_btn.setImageResource(R.drawable.list);
                    setGridListStatus();
                    setPplGridView();
                    setSpinnerPosition();
                }
            }
        });
    }

    private void setOnSpinnerItemSelected() {
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performFilterByPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setOnRefreshListener() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setSpinnerPosition();
            }
        });
    }

    private void performFilterByPosition(int position) {
        switch (position) {
            case 0:
                matchesLayout.setVisibility(View.GONE);
                if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                    getResultsFromParseCloud(people_GridView, 0, 0, 0, 0, 0, 0);
                } else {
                    getResultsFromParseCloud(people_ListView, 0, 0, 0, 0, 0, 0);
                }
                saveSpinnerPosition(position);
                break;
            case 1:
                getGridListResults();
                saveSpinnerPosition(position);
                break;
            case 2:
                startSearch("drinkingCriteria", 2, ">=", false);
                startSearch("drinkingCriteria", 2, ">=", true);
                saveSpinnerPosition(position);
                break;
            case 3:
                startSearch("drinkingCriteria", 1, "<=", false);
                startSearch("drinkingCriteria", 1, "<=", true);
                saveSpinnerPosition(position);
                break;
            case 4:
                startSearch("smokingCriteria", 2, ">=", false);
                startSearch("smokingCriteria", 2, ">=", true);
                saveSpinnerPosition(position);
                break;
            case 5:
                startSearch("smokingCriteria", 1, "<=", false);
                startSearch("smokingCriteria", 1, "<=", true);
                saveSpinnerPosition(position);
                break;
            case 6:
                startComplexSearch("religionCriteria");
                saveSpinnerPosition(position);
                break;
            case 7:
                startComplexSearch("ethnicityCriteria");
                saveSpinnerPosition(position);
                break;
        }
    }

    private void saveSpinnerPosition(int position) {
        sharedPreferences.edit().putInt("spinner_position", position).commit();
    }

    private void startComplexSearch(String criteria) {
        switch (criteria) {
            case "religionCriteria" :
                prepareSearch("religionCriteria");
                break;
            case "ethnicityCriteria":
                prepareSearch("ethnicityCriteria");
                break;
        }
    }

    private void prepareSearch(final String criteriaName) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserQuestionary");
        parseQuery.whereEqualTo("loginName", ParseUser.getCurrentUser().getUsername());
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    if (criteriaName.equals("religionCriteria")) {
                        int yourReligionCriteria = parseObject.getInt("yourReligion");
                        startSearch(criteriaName, yourReligionCriteria, "==", false);
                        startSearch(criteriaName, yourReligionCriteria, "!=", true);
                    } else if (criteriaName.equals("ethnicityCriteria")) {
                        int yourEthnicityCriteria = parseObject.getInt("yourEthnicity");
                        startSearch(criteriaName, yourEthnicityCriteria, "==", false);
                        startSearch(criteriaName, yourEthnicityCriteria, "!=", true);
                    }
                }
            }
        });
    }

    private void startSearch(String criteriaName, int criteriaValue, String criteriaSign, boolean excludeCriteriaFromAllUsers) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", ParseUser.getCurrentUser().getUsername());
        params.put(criteriaName, criteriaValue);
        params.put("criteriaSign", criteriaSign);
        params.put("excludeCriteriaFromAllUsers", excludeCriteriaFromAllUsers);

        callParseCloudFunction(params);
    }

    private void callParseCloudFunction(HashMap<String, Object> params) {
        ParseCloud.callFunctionInBackground("clientRequest", params, new FunctionCallback<ArrayList<ArrayList>>() {
            @Override
            public void done(ArrayList<ArrayList> listResults, ParseException e) {
                if (e == null) {
                    ArrayList<ParseUser> users = listResults.get(0);
                    matchesLayout.setVisibility(View.VISIBLE);

                    if(listResults.size() >= 2) {
                        Boolean excludeCriteria = (Boolean)listResults.get(1).get(0);
                        if(excludeCriteria) {
                            usersList = new ArrayList<>();
                            for (ParseUser user : users) {
                                usersList.add(user);
                            }
                            if(sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                                initAdapter(people_GridView, usersList);
                            }
                            else {
                                initAdapter(people_ListView, usersList);
                            }
                        }
                    } else {
                        usersListMatches = new ArrayList<>();
                        for (ParseUser user : users) {
                            usersListMatches.add(user);
                        }
                        if(sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                            initAdapter(people_GridView_Matches, usersListMatches);
                        }
                        else {
                            initAdapter(people_ListView_Matches, usersListMatches);
                        }
                    }

                    swipeContainer.setRefreshing(false);
                } else {
                    Log.v("formalchat", "----- NONE");
                }
            }
        });
    }

    private void setPplListView() {
        //people_ListView.setAdapter(new PeopleListViewAdapter(getApplicationContext()));
        people_GridView.setVisibility(View.GONE);
        people_GridView_Matches.setVisibility(View.GONE);
        people_ListView.setVisibility(View.VISIBLE);
        people_ListView_Matches.setVisibility(View.VISIBLE);
        //getGridListResults();
        setSpinnerPosition();
    }

    private void setPplGridView() {
        //people_GridView.setAdapter(new PeopleGridViewAdapter(getApplicationContext()));
        people_ListView.setVisibility(View.GONE);
        people_ListView_Matches.setVisibility(View.GONE);
        people_GridView.setVisibility(View.VISIBLE );
        people_GridView_Matches.setVisibility(View.VISIBLE);
        // getGridListResults();
        setSpinnerPosition();
    }

    private void setGridListStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isListButtonVisible", isListButtonVisible);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().remove("spinner_position").commit();
    }
}
