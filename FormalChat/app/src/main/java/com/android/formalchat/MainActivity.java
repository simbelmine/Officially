package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
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
    private static final int DEFAULT_SPINNER_POSITION = 1;
    private static final int DISPLAY_LIMIT = 4; // 20
    private SharedPreferences sharedPreferences;
    private ParseUser currentUser;
    private Boolean exit;
    private DrawerLayout drawerLayout;
    private GridView people_GridView;
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
    private TextView noSearchResultText;
    public int pageCount;
    private boolean isMatches;
    private boolean isSpinnerFirstCall;


    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    private RecyclerView recyclerMainView;
    private GridLayoutManager mainLayout;
    private RecyclerViewAdapter rcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.main_activity, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
        exit = false;
        isListButtonVisible = true; // Start always with grid view
        isMatches = true; // Start always with matches if any
        isSpinnerFirstCall = true;


        setTitle();
        initSharedPreferences();

        init();
        initGridListBtn();
        initValues();

        setUpParsePushNotifications();
        setOnClickListeners();
        setOnRefreshListener();

        mainLayout = new GridLayoutManager(MainActivity.this, 3);
        mainLayout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return rcAdapter.isHeader(position) ? mainLayout.getSpanCount() : 1;
            }
        });

        recyclerMainView.setHasFixedSize(true);
        recyclerMainView.setLayoutManager(mainLayout);

        if(isNetworkAvailable()) {
            setOnSpinnerItemSelectedListener();
            setOnScrollListener();
            setSpinnerPosition();
//            people_GridView.setOnScrollListener(onScrollListener());
        }
        else {
            getSnackbar(this, R.string.no_network, R.color.alert_red).show();
        }


//        getGridListResults(); // not Needed ,it's called because Spinner position 1 is selected with this
    }


    private void setUpParsePushNotifications() {
        // To track statistics around application
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        // inform the Parse Cloud that it is ready for notifications
        ParseInstallation.getCurrentInstallation().saveInBackground();

        // subscribe to channel
        ParsePush.subscribeInBackground(currentUser.getObjectId());
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void init() {
        currentUser = ParseUser.getCurrentUser();
        startLoginIfNoUser();
        initSwipeContainer();
        people_GridView_Matches = (ScrollableGridView) findViewById(R.id.people_gridview_matches);
        people_GridView_Matches.setExpanded(true);
        people_GridView = (GridView) findViewById(R.id.people_gridview);
//        people_GridView.setExpanded(true);

        people_ListView_Matches = (ScrollableListView) findViewById(R.id.people_listview_matches);
        people_ListView = (ScrollableListView) findViewById(R.id.people_listview);

        searchSpinner = (Spinner) findViewById(R.id.search_for);

        matchesLayout = (LinearLayout) findViewById(R.id.matches);
        grid_list_btn = (ImageButton) findViewById(R.id.grid_list_btn);

        recyclerMainView = (RecyclerView)findViewById(R.id.recycler_view);
    }

    private void setSpinnerPosition() {
        int position = sharedPreferences.getInt("spinner_position", DEFAULT_SPINNER_POSITION);
        searchSpinner.setSelection(position);

        performFilterByPosition(position);
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
//                    matchesLayout.setVisibility(View.VISIBLE);
                    int drinkingCriteria = parseObject.getInt("matchDrinking");
                    int smokingCriteria = parseObject.getInt("matchSmoking");
                    int religionCriteria = parseObject.getInt("matchReligion");
                    int ethnicityCriteria = parseObject.getInt("matchEthnicity");
                    int yourReligionCriteria = parseObject.getInt("yourReligion");
                    int yourEthnicityCriteria = parseObject.getInt("yourEthnicity");

                    if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
//                        getResultsFromParseCloud(people_GridView_Matches, drinkingCriteria,
//                                smokingCriteria, religionCriteria, ethnicityCriteria,
//                                yourReligionCriteria, yourEthnicityCriteria);

//
//                        getResultsFromParseCloud(people_GridView, drinkingCriteria,
//                                smokingCriteria, religionCriteria, ethnicityCriteria,
//                                yourReligionCriteria, yourEthnicityCriteria);


                        getResultsFromParseCloud(recyclerMainView, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria, true);
                        getResultsFromParseCloud(recyclerMainView, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria, false);

                    }
                    else {
                        getResultsFromParseCloud(people_ListView_Matches, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria, false);
                        getResultsFromParseCloud(people_ListView, drinkingCriteria,
                                smokingCriteria, religionCriteria, ethnicityCriteria,
                                yourReligionCriteria, yourEthnicityCriteria, false);
                    }
                } else {
//                    matchesLayout.setVisibility(View.GONE);
                    if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                        getResultsFromParseCloud(people_GridView, 0, 0, 0, 0, 0, 0, false);
                    } else {
                        getResultsFromParseCloud(people_GridView, 0, 0, 0, 0, 0, 0, false);
                    }
                }

                getSwipeContainer().setRefreshing(false);
            }
        });
    }

    private void getResultsFromParseCloud(final View view, int drinkingCriteria,
                                          int smokingCriteria, int religionCriteria, int ethnicityCriteria,
                                          int yourReligionCriteria, int yourEthnicityCriteria, final boolean isMatches) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("userName", ParseUser.getCurrentUser().getUsername());
        params.put("drinkingCriteria", drinkingCriteria);
        params.put("smokingCriteria", smokingCriteria);
        params.put("religionCriteria", religionCriteria);
        params.put("ethnicityCriteria", ethnicityCriteria);
        params.put("yourReligionCriteria", yourReligionCriteria);
        params.put("yourEthnicityCriteria", yourEthnicityCriteria);
//        if(view == people_GridView || view == people_ListView) {
        if(!isMatches) {
            params.put("excludeCriteriaFromAllUsers", true);
        }
//        params.put("rowsToSkip", (getPageCount() * DISPLAY_LIMIT));
//        params.put("rowsLimit", DISPLAY_LIMIT);
//        Log.e(ApplicationOfficially.TAG, "Rows to Skip .... = " + pageCount + "*" + DISPLAY_LIMIT + " = " + pageCount*DISPLAY_LIMIT);

        ParseCloud.callFunctionInBackground("clientRequest", params, new FunctionCallback<ArrayList<ArrayList>>() {
            @Override
            public void done(ArrayList<ArrayList> listResults, ParseException e) {
                if (e == null && listResults != null) {
                    ArrayList<ParseUser> users = listResults.get(0);

                    Log.v(ApplicationOfficially.TAG, "performFilterByPosition : Show me ALL :  IN clientRequest");

//                    if(view == recyclerMainView) {

                        Log.v(ApplicationOfficially.TAG, "isMatches ======= " + isMatches);

                        usersList = new ArrayList<>();
                        usersList.add(null);
                        for (ParseUser user : users) {
                            usersList.add(user);
                        }

                        initAdapter(view, usersList, isMatches);
//                    }


//
//
//
////                    if (view == people_GridView || view == people_ListView) {
//                    if(isMatches && users.size() == 0) {
//                        setPageCount(0);
//                        isMatches = false;
//                        getGridListResults();
//                    }
//
//                    if(!isMatches) {
//                        usersList = new ArrayList<>();
//                        for (ParseUser user : users) {
//                            usersList.add(user);
//                        }
//                        initAdapter(view, usersList);
//                    }
////                    else if (view == people_GridView_Matches || view == people_ListView_Matches) {
//                    else {
//                        usersListMatches = new ArrayList<>();
//                        for (ParseUser user : users) {
//                            usersListMatches.add(user);
//                        }
//                        initAdapter(view, usersListMatches);
//                    }
//
//                    getSwipeContainer().setRefreshing(false);
//                } else {
//
                }
            }
        });
    }


    private void initAdapter(View view, List<ParseUser> list, boolean isMatches){
        // *** Grig View *** //
////        if(view == people_GridView)  {
//        if(!isMatches) {
//            setNoSearchResultTxtVisibility(list.size(), R.id.no_result_all);
//            peopleGridViewAdapter = new PeopleGridViewAdapter(MainActivity.this, getApplicationContext(), list, isMatches);
//            ((ScrollableGridView) view).setAdapter(peopleGridViewAdapter);
//        }
////        else if(view == people_GridView_Matches) {
//        else if(isMatches){
//            setNoSearchResultTxtVisibility(list.size(), R.id.no_result_matches);
//            peopleGridViewAdapterMatches = new PeopleGridViewAdapter(MainActivity.this, getApplicationContext(), list, isMatches);
//            ((GridView)view).setAdapter(peopleGridViewAdapterMatches);
//        }
//
//        // *** List View *** //
//        else if(view == people_ListView) {
//            setNoSearchResultTxtVisibility(list.size(), R.id.no_result_all);
//            peopleListViewAdapter = new PeopleListViewAdapter(getApplicationContext(), list);
//            ((ScrollableListView)view).setAdapter(peopleListViewAdapter);
//        }
//        else if(view == people_ListView_Matches) {
//            setNoSearchResultTxtVisibility(list.size(), R.id.no_result_matches);
//            peopleListViewAdapterMatches = new PeopleListViewAdapter(getApplicationContext(), list);
//            ((ScrollableListView)view).setAdapter(peopleListViewAdapterMatches);
//        }

        if(rcAdapter == null) {
            rcAdapter = new RecyclerViewAdapter(MainActivity.this, getApplicationContext(), list, isMatches);
            recyclerMainView.setAdapter(rcAdapter);
        }
        else {
            rcAdapter.updateUsersList(MainActivity.this, getApplicationContext(), list, isMatches);
        }

//        if(peopleGridViewAdapter == null) {
//            peopleGridViewAdapter = new PeopleGridViewAdapter(MainActivity.this, getApplicationContext(), list, isMatches);
//            ((GridView) view).setAdapter(peopleGridViewAdapter);
//        }
//        else {
//            peopleGridViewAdapter.updateUsersList(list);
//        }
    }

    private void setNoSearchResultTxtVisibility(int listSize, int textViewId) {
        if(listSize > 0) {
            hideNoSearchResultText(textViewId);
        }
        else {
            showNoSearchResultText(textViewId);
        }
    }

    private void showNoSearchResultText(int textViewId) {
        noSearchResultText = (TextView)findViewById(textViewId);
        noSearchResultText.setVisibility(View.VISIBLE);
    }

    private void hideNoSearchResultText(int textViewId) {
        noSearchResultText = (TextView)findViewById(textViewId);
        noSearchResultText.setVisibility(View.GONE);
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

    private void initValues() {
        isMatches = true;
        peopleGridViewAdapter = null;
        rcAdapter = null;
        setPageCount(0);
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

    private void setOnSpinnerItemSelectedListener() {
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerFirstCall) {
                    initValues();
                    performFilterByPosition(position);
                } else {
                    isSpinnerFirstCall = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setOnRefreshListener() {
        getSwipeContainer().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    initValues();
                    setSpinnerPosition();
                } else {
                    getSwipeContainer().setRefreshing(false);
                    getSnackbar(MainActivity.this, R.string.no_network, R.color.alert_red).show();
                }
            }
        });
    }

    private void setOnScrollListener() {
        people_GridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                int threshold = 1;
                int count = people_GridView.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (people_GridView.getLastVisiblePosition() >= count - threshold && getPageCount() < 2) {
                        Log.e(ApplicationOfficially.TAG, "Loading data...");
                        int pageCount = getPageCount();
                        pageCount++;
                        setPageCount(pageCount);
                        setSpinnerPosition();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                boolean enable;

                if (people_GridView != null && people_GridView.getChildCount() > 0) {
                    // check if the first item ofthe grid is visible
                    boolean firstItemVisible = people_GridView.getFirstVisiblePosition() == 0;
                    //check if the top of the first item is visible
                    boolean firstItemTopVisible = people_GridView.getChildAt(0).getTop() == 0;

                    enable = firstItemVisible && firstItemTopVisible;
                    getSwipeContainer().setEnabled(enable);
                }
            }
        });
    }

    private void performFilterByPosition(int position) {
//        Log.v(ApplicationOfficially.TAG, "performFilterByPosition: pageCount = " + getPageCount());

        switch (position) {
            case 0:
                // Show me All
                matchesLayout.setVisibility(View.GONE);
                if (sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                    getResultsFromParseCloud(people_GridView, 0, 0, 0, 0, 0, 0, false);
                } else {
                    getResultsFromParseCloud(people_ListView, 0, 0, 0, 0, 0, 0, false);
                }
                saveSpinnerPosition(position);
                break;
            case 1:
                // Perfect Match
                getGridListResults();
                saveSpinnerPosition(position);
                break;
            case 2:
                // Match is Drinking
                startSearch("drinkingCriteria", 2, ">=", false, true);
                startSearch("drinkingCriteria", 2, ">=", true, false);
                saveSpinnerPosition(position);
                break;
            case 3:
                // Match is Not Drinking
                startSearch("drinkingCriteria", 1, "<=", false, true);
//                startSearch("drinkingCriteria", 1, "<=", true);
                saveSpinnerPosition(position);
                break;
            case 4:
                // Match is Smoking
                startSearch("smokingCriteria", 2, ">=", false, true);
//                startSearch("smokingCriteria", 2, ">=", true);
                saveSpinnerPosition(position);
                break;
            case 5:
                // Match is Not Smoking
                startSearch("smokingCriteria", 1, "<=", false, true);
//                startSearch("smokingCriteria", 1, "<=", true);
                saveSpinnerPosition(position);
                break;
            case 6:
                // Match with my Religion
                startComplexSearch("religionCriteria");
                saveSpinnerPosition(position);
                break;
            case 7:
                // Match with my Ethnicity
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
                        startSearch(criteriaName, yourReligionCriteria, "==", false, false);
                        startSearch(criteriaName, yourReligionCriteria, "!=", false, true);
                    } else if (criteriaName.equals("ethnicityCriteria")) {
                        int yourEthnicityCriteria = parseObject.getInt("yourEthnicity");
                        startSearch(criteriaName, yourEthnicityCriteria, "==", false, false);
                        startSearch(criteriaName, yourEthnicityCriteria, "!=", false, true);
                    }
                }
            }
        });
    }

    private void startSearch(String criteriaName, int criteriaValue, String criteriaSign, boolean excludeCriteriaFromAllUsers, final boolean isMatches_) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", ParseUser.getCurrentUser().getUsername());
        params.put(criteriaName, criteriaValue);
        params.put("criteriaSign", criteriaSign);
        params.put("excludeCriteriaFromAllUsers", excludeCriteriaFromAllUsers);
//        params.put("rowsToSkip", (getPageCount() * DISPLAY_LIMIT));
//        params.put("rowsLimit", DISPLAY_LIMIT);
        Log.e(ApplicationOfficially.TAG, "Rows to Skip .... = " + getPageCount() + "*" + DISPLAY_LIMIT + " = " + pageCount * DISPLAY_LIMIT);

        callParseCloudFunction(params, isMatches);
    }

    private void callParseCloudFunction(HashMap<String, Object> params, final boolean isMatches_) {
        ParseCloud.callFunctionInBackground("clientRequest", params, new FunctionCallback<ArrayList<ArrayList>>() {
            @Override
            public void done(ArrayList<ArrayList> listResults, ParseException e) {
                if (e == null) {
                    ArrayList<ParseUser> users = listResults.get(0);
//                    matchesLayout.setVisibility(View.VISIBLE);

                    Log.v(ApplicationOfficially.TAG, "clientRequest RESULT === " + users.size() + "   isMatches === " + isMatches);

//                    if(users.size() == 0) {
//                        isMatches = false;
//                        setPageCount(0);
//                        setSpinnerPosition();
//                    }

                    if(listResults.size() >= 2) {
                        Log.v(ApplicationOfficially.TAG, "LIST RESULT IS >= 2");

                        Boolean excludeCriteria = (Boolean)listResults.get(1).get(0);
                        if(excludeCriteria || !isMatches) {
                            usersList = new ArrayList<>();
                            usersList.add(null);
                            for (ParseUser user : users) {
                                usersList.add(user);
                            }
                            if(sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
                                initAdapter(people_GridView, usersList, isMatches);
                            }
                            else {
                                initAdapter(people_ListView, usersList, isMatches);
                            }
                        }
                    } else {
                        usersListMatches = new ArrayList<>();
                        usersListMatches.add(null);
                        for (ParseUser user : users) {
                            usersListMatches.add(user);
                        }
                        if(sharedPreferences.contains("isListButtonVisible") && sharedPreferences.getBoolean("isListButtonVisible", false)) {
//                            initAdapter(people_GridView_Matches, usersListMatches);
                            //initAdapter(people_GridView, usersListMatches);

                            initAdapter(recyclerMainView, usersListMatches, isMatches);
                        }

                    }

                    getSwipeContainer().setRefreshing(false);
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
//        setSpinnerPosition();
    }

    private void setPplGridView() {
        //people_GridView.setAdapter(new PeopleGridViewAdapter(getApplicationContext()));
        people_ListView.setVisibility(View.GONE);
        people_ListView_Matches.setVisibility(View.GONE);
        people_GridView.setVisibility(View.VISIBLE );
        people_GridView_Matches.setVisibility(View.VISIBLE);
        // getGridListResults();
//        setSpinnerPosition();
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

//    private AbsListView.OnScrollListener onScrollListener () {
//        return new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView listView, int scrollState) {
//                int treshold = 1;
//                int count = people_GridView.getCount();
//
//                if(scrollState == SCROLL_STATE_IDLE) {
//                    if(people_GridView.getLastVisiblePosition() >= count - treshold && pageCount < 2) {
////                        Log.i(ApplicationOfficially.TAG, "loading more data....");
//                        Toast.makeText(getApplicationContext(), "Loading more data...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView listView, int i, int i1, int i2) {
//
//            }
//        };
//    }
}
