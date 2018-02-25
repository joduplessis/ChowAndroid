package com.chow.chow.chow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chow.chow.chow.utility.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class RedeemFragment extends Fragment implements Button.OnClickListener {

    Button redeemButton ;
    EditText code ;
    String codeHolder ;

    public RedeemFragment() {  }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_redeem, container, false);

        if ( Launch.globalUserLoggedIn == false ) {

            Toast.makeText(getActivity(), "Sorry, you need to log in first.", Toast.LENGTH_SHORT).show();

            FragmentManager fragmentManager = myContext.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LoginFragment lFragment = new LoginFragment();
            fragmentTransaction.replace(R.id.fragment_container, lFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else {

            ((Launch) getActivity()).setActionbarTitle("REDEEM");
            ((Launch) getActivity()).showActionbar();
            ((Launch) getActivity()).navbarItemActivate(2) ;

            redeemButton = (Button) view.findViewById(R.id.buttonCreate);
            code = (EditText) view.findViewById(R.id.editText);

            redeemButton.setOnClickListener(this);

            // fonts

            Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");

            TextView heading = (TextView) view.findViewById(R.id.textView2);
            TextView subheading = (TextView) view.findViewById(R.id.textView);
            TextView overlayDescription = (TextView) view.findViewById(R.id.overlay_description);

            heading.setTypeface(font);
            subheading.setTypeface(font);
            code.setTypeface(font);
            redeemButton.setTypeface(font);
            overlayDescription.setTypeface(jsfont);

        }

        return view ;

    }

    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        switch(v.getId()) {

            case R.id.buttonCreate:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("createButton")
                        .build());

                codeHolder = code.getText().toString() ;
                closeKeyboard(getActivity(), code.getWindowToken());
                new sendRedeemAction().execute() ;

                break;

        }

    }

    public static void closeKeyboard(Context c, IBinder windowToken) {

        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);

    }

    JSONArray dataRedeemJsonArr = null;
    Integer redeem_success ;
    String redeem_gps ;
    String redeem_bucks ;
    String redeem_meal ;
    String redeem_restaurant ;

    private class sendRedeemAction extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.redeem.php?code="+codeHolder);

                dataRedeemJsonArr = json.getJSONArray("redeem");

                JSONObject c = dataRedeemJsonArr.getJSONObject(0);

                redeem_success = c.getInt("success") ;

                if ( redeem_success != -1 ) {

                    redeem_bucks = c.getString("bucks") ; // gets the bucks for this item
                    redeem_gps = c.getString("gps") ; // gets the gps for this place
                    redeem_meal = c.getString("meal") ; // gets the meal
                    redeem_restaurant = c.getString("restaurant") ; // gets the restaurant

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataRedeemJsonArr != null ) {

                if ( redeem_success == -1 ) {

                    Toast.makeText(getActivity(), "Sorry, it doesn't appear to be a voucher...", Toast.LENGTH_SHORT).show();

                } else {

                    // get the user's current location and measure it with REDEEM_GPS above
                    // if the distance is less than X then do the screen swap

                    double la = Float.parseFloat( redeem_gps.split(",")[0] );
                    double lo = Float.parseFloat( redeem_gps.split(",")[1] );
                    double dist = distance(Launch.globalUserLatitude, Launch.globalUserLongitude, la, lo, 'K') ;

                    // log

                    Log.d("JOJOJO", dist + "") ;

                    // get the magic code
                    // if the user uses a code like 007 they do not get hindered by the limits
                    // this is important as it will ensure they can test accurately
                    // VERY IMPORTANT

                    String secretCode = code.getText().toString() ;

                    // if the user is outside ditance
                    // remember the code here > 007

                    if ( dist > 20d && !secretCode.equals("007") ) {

                        Toast.makeText(getActivity(), "Sorry, you need to be within distance!", Toast.LENGTH_SHORT).show();

                    } else {

                        // if the user is within 20km
                        // get the time values

                        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                        long timeout = Long.valueOf( sharedPref.getString("redeem_timeout", "0") ).longValue();
                        long unixTime = System.currentTimeMillis() / 1000L;

                        // if the current time is less than the future time
                        // remember the code here > 007

                        if ( unixTime < timeout && !secretCode.equals("007")) {

                            Toast.makeText(getActivity(), "It's not time to eat again. You can redeem again after 3 hours!", Toast.LENGTH_SHORT).show();

                        } else {

                            gaTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Completions")
                                    .setAction("Code")
                                    .setLabel("Redeem")
                                    .build());

                            // track

                            new TrackUser().execute("type=redeem&user=" + Launch.globalUserID + "&event=" + redeem_meal);

                            // move the screen

                            FragmentManager fragmentManager = myContext.getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            RedeemSuccessFragment rsFragment = new RedeemSuccessFragment();

                            rsFragment.bucks = redeem_bucks;
                            rsFragment.meal = redeem_meal;
                            rsFragment.restaurant = redeem_restaurant;

                            fragmentTransaction.replace(R.id.fragment_container, rsFragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();

                        }

                    }



                }

            } else { Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show(); }

        }

    }

    // calculate distance

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);

    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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

        gaTracker.setScreenName("Redeem");

    }

}
