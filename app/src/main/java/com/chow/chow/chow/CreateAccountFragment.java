package com.chow.chow.chow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chow.chow.chow.utility.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class CreateAccountFragment extends Fragment implements Button.OnClickListener {

    Button create ;
    Button login ;

    View view ;

    EditText username ;
    EditText email ;
    EditText password ;
    EditText passwordconfirm ;
    TextView heading;
    TextView footnote;
    Button goback;

    public CreateAccountFragment() {}

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set some default stuff
        ((Launch) getActivity()).setActionbarTitle("CREATE PROFILE");
        ((Launch) getActivity()).hideActionbar();
        ((Launch) getActivity()).navbarItemActivate(3) ;

        // Font
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

        // Get views
        view = inflater.inflate(R.layout.fragment_create_account, container, false);
        username = (EditText) view.findViewById(R.id.username);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        passwordconfirm = (EditText) view.findViewById(R.id.passwordconfirm);
        login = (Button) view.findViewById(R.id.buttonLogin);
        create = (Button) view.findViewById(R.id.buttonCreate);
        heading = (TextView) view.findViewById(R.id.textView2);
        footnote = (TextView) view.findViewById(R.id.textView);
        goback = (Button) view.findViewById(R.id.buttonLogin);

        // Set the fonts
        heading.setTypeface(font);
        footnote.setTypeface(font);
        goback.setTypeface(font);
        login.setTypeface(font);
        create.setTypeface(font);
        username.setTypeface(font);
        email.setTypeface(font);
        password.setTypeface(font);
        passwordconfirm.setTypeface(font);

        // Event listeners
        create.setOnClickListener(this);
        login.setOnClickListener(this);

        // Return the view
        return view ;

    }

    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        switch(v.getId()) {
            case R.id.buttonLogin:
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                LoginFragment laFragment = new LoginFragment();
                fragmentTransaction.replace(R.id.fragment_container, laFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("loginButton")
                        .build());
                break;
            case R.id.buttonCreate:
                if ( password.getText().toString().equals( passwordconfirm.getText().toString() ) ) {
                    new Register().execute();
                } else {
                    Toast.makeText(getActivity(), "Sorry, your passwords don't match", Toast.LENGTH_SHORT).show();
                }
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("createButton")
                        .build());
                break;

        }

    }

    JSONArray dataJsonArr = null;
    Integer success ;
    Long successid ;

    private class Register extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                String URL = "API_PATH/app/api/add.user.php" ;

                URL += "?name="+username.getText().toString();
                URL += "&password="+password.getText().toString();
                URL += "&email="+email.getText().toString();
                URL += "&bucks=0" ;

                JSONObject json = jParser.getJSONFromUrl(URL);

                dataJsonArr = json.getJSONArray("user");

                JSONObject c = dataJsonArr.getJSONObject(0);

                success = c.getInt("success") ;
                successid = c.getLong("id") ;

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if (dataJsonArr!=null) {

                if ( success == -1 ) {

                    Toast.makeText(getActivity(), "Sorry, there seems to have been an error.", Toast.LENGTH_SHORT).show();

                } else {
                    // Tracker
                    gaTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Completions")
                            .setAction("Account")
                            .setLabel("signUp")
                            .build());

                    // edit the global variables - the user has successfully logged in (without FB)
                    Launch.globalUserID = successid.toString() ;
                    Launch.globalUserLoggedIn = true ;

                    // edit the preferences
                    Context context = getActivity();
                    SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("id", successid.toString());
                    editor.putString("loggedin", "yes");
                    editor.commit();

                    // load the tutorial
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    TutorialFragment lbFragment = new TutorialFragment();
                    fragmentTransaction.replace(R.id.fragment_container, lbFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                }

            } else { Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show(); }

        }

    }

    // Build and send an Event.

    // GA

    Tracker gaTracker ;

    @Override
    public void onStart() {

        super.onStart();

        // set up the tracker

        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name

        gaTracker.setScreenName("Create Account");


    }

}
