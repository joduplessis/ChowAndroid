package com.chow.chow.chow;

import android.app.Activity;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class LostPasswordFragment extends Fragment implements Button.OnClickListener {

    Button login;
    Button password ;
    EditText emailText ;

    View view ;

    public LostPasswordFragment() {    }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        ((Launch) getActivity()).setActionbarTitle("LOST PASSWORD");
        ((Launch) getActivity()).hideActionbar();
        ((Launch) getActivity()).navbarItemActivate(3);

        view = inflater.inflate(R.layout.fragment_lost_password, container, false);

        emailText = (EditText) view.findViewById(R.id.editText) ;

        login = (Button) view.findViewById(R.id.buttonLogin);
        password = (Button) view.findViewById(R.id.buttonPassword);

        login.setOnClickListener(this);
        password.setOnClickListener(this);

        /* ----------- fonts ----------- */

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

        TextView heading = (TextView) view.findViewById(R.id.textView2);
        Button goback = (Button) view.findViewById(R.id.buttonLogin);

        heading.setTypeface(font);
        goback.setTypeface(font);

        emailText.setTypeface(font);

        password.setTypeface(font);
        login.setTypeface(font);

        return view ;
    }

    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        switch(v.getId()) {

            case R.id.buttonLogin:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("loginButton")
                        .build());

                FragmentManager fragmentManager = myContext.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                LoginFragment lFragment = new LoginFragment();

                fragmentTransaction.replace(R.id.fragment_container, lFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

            case R.id.buttonPassword:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("passwordButton")
                        .build());

                new DownloadJSON().execute();

                break;

        }

    }

    JSONArray dataJsonArr = null;
    Integer success ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.password.php?name="+emailText.getText().toString());

                dataJsonArr = json.getJSONArray("lostpassword");

                JSONObject c = dataJsonArr.getJSONObject(0);

                success = c.getInt("success") ;

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataJsonArr!=null ) {

                if ( success == -1 ) {
                    Toast.makeText(getActivity(), "Sorry, your details seem to be incorrect.", Toast.LENGTH_SHORT).show();
                } else {                    gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Completions")
                        .setAction("Account")
                        .setLabel("passwordSent")
                        .build());
                    Toast.makeText(getActivity(), "Your password has been emailed to you.", Toast.LENGTH_SHORT).show();
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

        gaTracker.setScreenName("Lost Password");

    }

}
