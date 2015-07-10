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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private boolean isGrid;
    private ListView people_ListView;
    private ListView people_ListView_Matches;
    private PeopleListViewAdapter peopleListViewAdapter;

    private List<ParseUser> usersList;
    private List<ParseUser> usersListMatches;

    private Spinner searchSpinner;
    private LinearLayout matchesLayout;

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

      //  doInBackground();

        people_GridView_Matches = (ScrollableGridView) findViewById(R.id.people_gridview_matches);
        people_ListView_Matches = (ListView) findViewById(R.id.people_listview_matches);

        people_GridView = (ScrollableGridView) findViewById(R.id.people_gridview);
        people_ListView = (ListView) findViewById(R.id.people_listview);

        searchSpinner = (Spinner) findViewById(R.id.search_for);
        searchSpinner.setSelection(1);
        matchesLayout = (LinearLayout) findViewById(R.id.matches);

        grid_list_btn = (ImageButton) findViewById(R.id.grid_list_btn);
        initGridListBtn();
        setOnClickListeners();
        setOnSpinnerItemSelected();

//        getGridResults(); // not Needed ,it's called because Spinner position 1 is selected with this
    }

    private void getGridResults() {
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
                    startParseCloudCode(people_GridView_Matches, drinkingCriteria,
                            smokingCriteria, religionCriteria, ethnicityCriteria,
                            yourReligionCriteria, yourEthnicityCriteria);
                    startParseCloudCode(people_GridView, drinkingCriteria,
                            smokingCriteria, religionCriteria, ethnicityCriteria,
                            yourReligionCriteria, yourEthnicityCriteria);
                }
                else {
                    matchesLayout.setVisibility(View.GONE);
                    startParseCloudCode(people_GridView, 0, 0, 0, 0, 0, 0);
                }
            }
        });
    }

    private void startParseCloudCode(final ScrollableGridView gridview, int drinkingCriteria,
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
        if(gridview == people_GridView ) {
            params.put("excludeCriteriaFromAllUsers", true);
        }

       ParseCloud.callFunctionInBackground("clientRequest", params, new FunctionCallback<ArrayList<ArrayList>>() {
           @Override
           public void done(ArrayList<ArrayList> listResults, ParseException e) {
               if(e == null && listResults != null) {
                   ArrayList<ParseUser> users = listResults.get(0);
                   if(gridview == people_GridView) {
                        usersList = new ArrayList<>();
                        for (ParseUser user : users) {
                            usersList.add(user);
                        }
                        initAdapter(peopleGridViewAdapter, gridview, usersList);
                    }
                    else if(gridview == people_GridView_Matches) {
                        usersListMatches = new ArrayList<>();
                        for (ParseUser user : users) {
                            usersListMatches.add(user);
                        }
                        initAdapter(peopleGridViewAdapterMatches, gridview, usersListMatches);
                    }
               }
               else {
                   Log.v("formalchat", "----- NONE");
               }
           }
       });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

//    private void doInBackground() {
//        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
//        parseQuery.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> users, ParseException e) {
//                if (e == null && users.size() > 0) {
//                    usersList = users;
//                    initAdapter();
//                }
//            }
//        });
//    }

    private void initAdapter(PeopleGridViewAdapter adapter, ScrollableGridView gridView, List<ParseUser> list) {
        if(adapter != null) {
            adapter.updateUsers(list);
        }
        else {
            adapter = new PeopleGridViewAdapter(getApplicationContext(), list);
            gridView.setAdapter(adapter);
        }
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
                if (isGrid) {
                    isGrid = false;
                    grid_list_btn.setImageResource(R.drawable.grid);
                    setGridListStatus();
                    setPplListView();
                } else {
                    isGrid = true;
                    grid_list_btn.setImageResource(R.drawable.list);
                    setGridListStatus();
                    setPplGridView();
                }
            }
        });
    }

    private void setOnSpinnerItemSelected() {
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        matchesLayout.setVisibility(View.GONE);
                        startParseCloudCode(people_GridView, 0, 0, 0, 0, 0, 0);
                        break;
                    case 1:
                        getGridResults();
                        break;
                    case 2:
                        startSearch("drinkingCriteria", 2, ">=", false);
                        startSearch("drinkingCriteria", 2, ">=", true);
                        break;
                    case 3:
                        startSearch("drinkingCriteria", 1, "<=", false);
                        startSearch("drinkingCriteria", 1, "<=", true);
                        break;
                    case 4:
                        startSearch("smokingCriteria", 2, ">=", false);
                        startSearch("smokingCriteria", 2, ">=", true);
                        break;
                    case 5:
                        startSearch("smokingCriteria", 1, "<=", false);
                        startSearch("smokingCriteria", 1, "<=", true);
                        break;
                    case 6:
                        startComplexSearch("religionCriteria");
                        break;
                    case 7:
                        startComplexSearch("ethnicityCriteria");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                        Boolean b = (Boolean)listResults.get(1).get(0);
                        if(b) {
                            usersList = new ArrayList<>();
                            for (ParseUser user : users) {
                                usersList.add(user);
                            }
                            initAdapter(peopleGridViewAdapter, people_GridView, usersList);
                        }
                    } else {
                            usersListMatches = new ArrayList<>();
                        for (ParseUser user : users) {
                            usersListMatches.add(user);
                        }
                        initAdapter(peopleGridViewAdapterMatches, people_GridView_Matches, usersListMatches);
                    }
                } else {
                    Log.v("formalchat", "----- NONE");
                }
            }
        });
    }

    private void setPplListView() {
        people_ListView.setAdapter(new PeopleListViewAdapter(getApplicationContext()));
        people_GridView.setVisibility(View.GONE);
        people_GridView_Matches.setVisibility(View.GONE);
        people_ListView.setVisibility(View.VISIBLE);
    }

    private void setPplGridView() {
        //people_GridView.setAdapter(new PeopleGridViewAdapter(getApplicationContext()));
        people_ListView.setVisibility(View.GONE);
        people_GridView.setVisibility(View.VISIBLE );
        people_GridView_Matches.setVisibility(View.VISIBLE);
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
