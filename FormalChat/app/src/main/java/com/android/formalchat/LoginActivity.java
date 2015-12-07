package com.android.formalchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.formalchat.chat.MessagingUser;
import com.android.formalchat.questionary.QuestionaryActivity;
import com.android.formalchat.tutorial.TutorialPagerActivity;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%-+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                    "(" +
                    "." +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                    ")+"
    );

    // UI references.
    public static final String PREFS_NAME = "FormalChatPrefs";
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private TextView mForgotPassword;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button mSignUpButton;
    private RelativeLayout alertLayout;
    private TextView alertMsg;
    private ParseUser parseUser;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Boolean exit;
    private Toolbar toolbar;

    private String TAG = "formalchat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        initToolbar();
        initActionBar();

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        editor = sharedPreferences.edit();
        exit = false;

        init();
        populateAutoComplete();
        setOnClickListeners();
        setOnEditoInfoListeners();

        startCorrectActivity();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        // Hide Action Bar icon and text
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_gray)));
    }

    private void startCorrectActivity() {
        ParseUser pUser = ParseUser.getCurrentUser();

        Log.v(ApplicationOfficially.TAG, "Login Activity: startCorrectActivity: user = " + pUser);

////////////
//            // *** If e-Mail autorisation is needed => Uncomment!
///////////
////                if(isEmailAutorized(pUser)) {
////                    launchMainActivity();
////                }

        if (pUser != null) {
            showProgress(true);
            if(!isMainQuestionsDone(pUser)) {
                startActivityByClassName(MainQuestionsActivity.class);
            }
            else if(!isQuestionaryDone(pUser)) {
                startActivityByClassName(QuestionaryActivity.class);
            }
            else {
                startActivityByClassName(MainActivity.class);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private boolean isMainQuestionsDone(ParseUser pUser) {
        if(pUser.containsKey("doneMainQuestions") && pUser.getBoolean("doneMainQuestions")) {
            return true;
        }
        return false;
    }

    private boolean isQuestionaryDone(ParseUser pUser) {
        if(pUser.containsKey("doneQuestionary") && pUser.getBoolean("doneQuestionary")) {
            return true;
        }
        return false;
    }

    private void init() {
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        mForgotPassword = (TextView) findViewById(R.id.forgot_pass);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignUpButton = (Button) findViewById(R.id.email_log_in);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        alertLayout = (RelativeLayout) findViewById(R.id.alert_layout);
        alertMsg = (TextView) findViewById(R.id.alert_msg);
    }
    private void setOnClickListeners() {
        mEmailView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAlertMsg();
            }
        });
        mPasswordView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAlertMsg();
            }
        });
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAlertMsg();
                attemptLogin();
            }
        });
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAlertMsg();
                signUp();
            }
        });
        mForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "We will send you an email... DUMMY!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setOnEditoInfoListeners() {
        mEmailView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mPasswordView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                }
                return false;
            }
        });
    }

    private void signUp() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String userName = email;
        View focusView = getFocusView(email, password);

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            if(((ApplicationOfficially)getApplication()).isNetworkAvailable()) {
                startFirstTimeUser(userName, email, password);
            }
            else {
//                showAlertMsg(R.string.no_network, R.color.alert_red);
                ((ApplicationOfficially)getApplication()).getSnackbar(this, R.string.no_network, R.color.alert_red).show();
            }
        }
    }

    private void startFirstTimeUser(final String userName, final String email, final String password) {
        Log.v(ApplicationOfficially.TAG, "Login Activity: SignUp - startFirstTimeUser: user name = " + userName);

        ParseQuery query = ParseUser.getQuery();
        query.whereContains("username", userName);
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null || parseObject != null) {
//                    showAlertMsg(R.string.already_exists, R.color.alert_red);
                    ((ApplicationOfficially)getApplication()).getSnackbar(LoginActivity.this, R.string.already_exists, R.color.alert_red).show();
                }
                else {
                    Log.e("formalchat", e.getMessage());

                    if(isEmailValid(email) && isPasswordValid(password)) {
                        saveDataToParse(userName, email, password);
                    }
                    else {

                    }
                }
            }
        });
    }

    private void startActivityByClassName(Class<?> activityToCall) {
        Intent intent = new Intent(this, activityToCall);
        startActivity(intent);
    }

    private boolean isValidData(String userName, String password) {
        if (userName.equals("") && password.equals("")) {
            showAlertMsg(R.string.please_complete_the_sign_up, R.color.alert_red);
//            Toast.makeText(getApplicationContext(),
//                    getResources().getString(R.string.please_complete_the_sign_up),
//                    Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void saveDataToParse(String userName, String email, String password) {
        MessagingUser parseUser = new MessagingUser();
        Log.v(ApplicationOfficially.TAG, "Login Activity: saveDataToParse: user = " + parseUser);
        parseUser.setUsername(userName);
        parseUser.setPassword(password);
        parseUser.setEmail(email);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    Log.v(TAG, "Hooray! We saved it!!!");
//                    Toast.makeText(getApplicationContext(),
//                            getString(R.string.sign_up_success),
//                            Toast.LENGTH_LONG).show();
                    ((ApplicationOfficially)getApplication()).getSnackbar(LoginActivity.this, R.string.sign_up_success, R.color.alert_green_80).show();

                    setLogedInSharedPrefs();
                    startActivityByClassName(MainQuestionsActivity.class);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.v(TAG, "Nope :(    Error is: " + e);
//                    showAlertMsg(R.string.something_wrong, R.color.alert_red);
                    ((ApplicationOfficially)getApplication()).getSnackbar(LoginActivity.this, R.string.no_network, R.color.alert_red).show();
                }
            }
        });
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        final String userName = email;

        View focusView = null;
        focusView = getFocusView(email, password);


        ///////////////////////////////////////////////////
        //////// **************************************////
        ///////////////////////////////////////////////////

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            if(((ApplicationOfficially)getApplication()).isNetworkAvailable()) {
                loginInBackground(userName, password);
            }
            else {
//                showAlertMsg(R.string.no_network, R.color.alert_red);
                ((ApplicationOfficially)getApplication()).getSnackbar(this, R.string.no_network, R.color.alert_red).show();
            }

//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
        }
    }

    private void loginInBackground(final String userName, String password) {
        Log.v(ApplicationOfficially.TAG, "Login Activity: loginInBackground: user = " + parseUser + "  name = " + userName);

        parseUser.logInInBackground(userName, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    Log.v(TAG, "emailVerified" + (parseUser.getBoolean("emailVerified")));
                    if (isEmailAutorized(parseUser) || BuildConfig.DEBUG) {
                        logInWithCorrectActivity();
                    } else {
//                        showAlertMsg(R.string.confirm_email, R.color.alert_green);
                        ((ApplicationOfficially)getApplication()).getSnackbar(LoginActivity.this, R.string.confirm_email, R.color.alert_green_80).show();
                    }
                } else {
//                    showAlertMsg(R.string.no_such_user, R.color.alert_red);
                    ((ApplicationOfficially)getApplication()).getSnackbar(LoginActivity.this, R.string.no_such_user, R.color.alert_red).show();
                }
            }
        });
    }

    private void showAlertMsg(int textId, int colorId) {
        alertLayout.setVisibility(View.VISIBLE);
        alertLayout.setBackgroundColor(getResources().getColor(colorId));
        alertMsg.setText(getString(textId));
    }

    private void hideAlertMsg() {
        alertLayout.setVisibility(View.INVISIBLE);
    }

    private void logInWithCorrectActivity() {
        startCorrectActivity();
        setLogedInSharedPrefs();
    }

    private void setLogedInSharedPrefs() {
        editor.putBoolean("loggedIn", true);
        editor.commit();
    }

    private View getFocusView(String email, String password) {
        View focusView = null;
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_missing_password));
            focusView = mPasswordView;
        }
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
        }
        return focusView;
    }

    private boolean isEmailAutorized(ParseUser pu) {
        if(pu.getBoolean("emailVerified")) {
            Log.v(TAG, "emailVerified" + (pu.getBoolean("emailVerified")));
            return true;
        }
        return false;
    }

//    private void launchMainActivity() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//    }


    private void launchTutorialActivity() {
        Intent intent = new Intent(this, TutorialPagerActivity.class);
        startActivity(intent);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
//        return email.contains("@");
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
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



