package com.chow.chow.chow;



import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import com.chow.chow.chow.utility.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class LoginFragment extends Fragment implements Button.OnClickListener {

    Button login;
    Button button5;
    Button signup ;
    Button password ;
    LoginButton facebook ;
    View view ;
    EditText usernameText ;
    EditText passwordText ;

    private UiLifecycleHelper uiHelper;
    private final List<String> permissions;
    private FragmentActivity myContext;

    public LoginFragment() {
        permissions = Arrays.asList("user_status");
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set some stuff up
        ((Launch) getActivity()).setActionbarTitle("LOGIN");
        ((Launch) getActivity()).hideActionbar();
        ((Launch) getActivity()).navbarItemActivate(3);

        // Get our views
        view = inflater.inflate(R.layout.fragment_login, container, false);
        usernameText = (EditText) view.findViewById(R.id.editText) ;
        passwordText = (EditText) view.findViewById(R.id.editText2) ;
        login = (Button) view.findViewById(R.id.buttonLogin);
        password = (Button) view.findViewById(R.id.buttonPassword);
        signup = (Button) view.findViewById(R.id.buttonSignup);
        button5 = (Button) view.findViewById(R.id.button5);
        facebook = (LoginButton) view.findViewById(R.id.authButton);

        // Set the click listeners
        login.setOnClickListener(this);
        password.setOnClickListener(this);
        signup.setOnClickListener(this);
        button5.setOnClickListener(this);

        // fonts
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

        // Set the fonts
        login.setTypeface(font);
        signup.setTypeface(font);
        password.setTypeface(font);
        usernameText.setTypeface(font);
        passwordText.setTypeface(font);
        button5.setTypeface(font);

        // facebook
        facebook.setFragment(this);
        facebook.setReadPermissions(permissions);

        return view ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();

        // Log
        Log.d("JOJOJO", "Session: "+session.toString());

        if (session != null && (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                // edit global and stored variables
                                Context context = getActivity();
                                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();

                                // Change global variables
                                Launch.globalUserLoggedIn = true ;
                                Launch.globalUserLoggedInFB = true ;
                                Launch.globalUserPassword = "Facebook" ;
                                Launch.globalUserID = user.getId() ;
                                Launch.globalUserName = user.getName();
                                Launch.globalFirstName = user.getFirstName();
                                Launch.globalLastName = user.getLastName();
                                Launch.globalDOB = user.getBirthday() ;
                                Launch.globalGender = (String) user.getProperty("gender");
                                Launch.globalUserImage = "https://graph.facebook.com/" + Launch.globalUserID + "/picture?type=large" ;

                                // set the preferences
                                editor.putString("loggedin", "yes");
                                editor.putString("loggedinfb", "yes");
                                editor.putString("id", user.getId());
                                editor.commit();

                                // notify the user
                                Toast.makeText(getActivity(), "Logged in with Facebook", Toast.LENGTH_SHORT).show();

                                // check if this account exists or not - creates it if not
                                new CheckAccountJSON().execute();
                            }
                        }
                        if (response.getError() != null) { }
                    }
                });
        request.executeAsync();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            // if they are logged in
            // set the variables
            makeMeRequest(session);
        } else if (state.isClosed()) {
            // if the user is logged out of facebook
            // set the global variables to logged out
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();

            // edit global and stored variables
            Context context = getActivity();
            SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("loggedin", "no");
            editor.commit();

            // global
            Launch.globalUserLoggedIn = false ;
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    // button actions
    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);
        FragmentManager fragmentManager = myContext.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {
            case R.id.buttonLogin:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("loginButton")
                        .build());
                new DownloadJSON().execute();
                break;
            case R.id.buttonPassword:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("passwordButton")
                        .build());
                LostPasswordFragment pFragment = new LostPasswordFragment();
                fragmentTransaction.replace(R.id.fragment_container, pFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.buttonSignup:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("signupButton")
                        .build());
                CreateAccountFragment caFragment = new CreateAccountFragment();
                fragmentTransaction.replace(R.id.fragment_container, caFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.button5:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("mapButton")
                        .build());
                FindMapFragment fmFragment = new FindMapFragment();
                fragmentTransaction.replace(R.id.fragment_container, fmFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ((Launch) getActivity()).navbarItemActivate(1);
                break;
        }

    }

    // send the data to the server to login
    JSONArray dataJsonArr = null;
    String userID ;

    // send data to the server and get the user id if they are found
    // if they are found: set global variables
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                String URL = "API_PATH/app/api/get.account.login.php?username="+usernameText.getText().toString()+"&password="+passwordText.getText().toString() ;

                JSONObject json = jParser.getJSONFromUrl(URL);

                dataJsonArr = json.getJSONArray("user");

                // because there will only be one returned
                // Or at least, just grab the first one
                JSONObject c = dataJsonArr.getJSONObject(0);
                userID = c.getString("id") ;

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {
            if ( dataJsonArr!=null ) {
                if ( userID.equals("-1") ) {
                    Toast.makeText(getActivity(), "Sorry, your details seem to be incorrect.", Toast.LENGTH_SHORT).show();
                } else {
                    // Get the preferences
                    Context context = getActivity();
                    SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("id", userID.toString());
                    editor.putString("loggedin", "yes");
                    editor.commit();

                    // edit the global variables - the user has successfully logged in without FB
                    Launch.globalUserID = userID.toString() ;
                    Launch.globalUserLoggedIn = true ;

                    // user association
                    gaTracker.set("&uid", Launch.globalUserID);
                    gaTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Completions")
                            .setAction("Account")
                            .setLabel("signIn")
                            .build());

                    // load the profile fragment
                    FragmentManager fragmentManager = myContext.getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    ProfileFragment pFragment = new ProfileFragment();
                    fragmentTransaction.replace(R.id.fragment_container, pFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

            } else { Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong with your connection.", Toast.LENGTH_SHORT).show(); }

        }

    }

    // if the user has a FB account, but not yet a normal account
    // then we create the normal account so they can interact with it
    private class CheckAccountJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                JsonParser jParser = new JsonParser();

                String URL = "API_PATH/app/api/add.fb.user.php" ;
                URL += "?id="+Launch.globalUserID ;
                URL += "&username="+Launch.globalUserName ;
                URL += "&firstname="+Launch.globalFirstName ;
                URL += "&password="+Launch.globalUserPassword ;
                URL += "&lastname="+Launch.globalLastName ;
                URL += "&dob="+Launch.globalDOB ;
                URL += "&gender="+Launch.globalGender ;
                URL += "&image="+Launch.globalUserImage ;

                // format the URL nicely
                URL = URL.replace(" ", "%20");

                // make the call
                JSONObject json = jParser.getJSONFromUrl(URL);

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {
            // If this user has been the last to see it, then hide it
            if (Launch.globalUserSeenTutorial.equals(Launch.globalUserID)) {
                // Make the map active
                ((Launch) getActivity()).navbarItemActivate(1);

                // Load the tutorial
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FindMapFragment fmFragment = new FindMapFragment();
                fragmentTransaction.replace(R.id.fragment_container, fmFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } else {
                // Make the profile button active
                ((Launch) getActivity()).navbarItemActivate(3);

                // Load the tutorial
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                TutorialFragment fmFragment = new TutorialFragment();
                fragmentTransaction.replace(R.id.fragment_container, fmFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }


    // Build and send an Event.
    Tracker gaTracker ;

    @Override
    public void onStart() {
        super.onStart();

        // set up the tracker
        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name
        gaTracker.setScreenName("Login");

    }

}
