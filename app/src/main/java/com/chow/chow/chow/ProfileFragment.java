package com.chow.chow.chow;

import android.app.Activity;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chow.chow.chow.adaptors.AdapterRestaurant;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.chow.chow.chow.utility.*;

public class ProfileFragment extends Fragment implements Button.OnClickListener {

    String id ;
    View view ;
    ExpandableHeightGridView gridview ;
    Button edit ;
    Button spend ;
    ImageView image ;
    TextView bucks ;
    TextView favnum ;

    public ProfileFragment() {
        id = Launch.globalUserID ;
    }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    RestaurantItemClicked mCallback ;

    // interface for Launch activity
    public interface RestaurantItemClicked {
        public void loadRestaurantDetail(String text);
    }

    // our method that fires a call to loadrestaurant on Launch
    public void loadRestaurant(String str) {
        mCallback.loadRestaurantDetail(str);
    }

    // constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("PROFILE");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(3) ;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (RestaurantItemClicked) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ItemClicked");
        }

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        // if they're not logged in
        if ( Launch.globalUserLoggedIn == false ) {
            FragmentManager fragmentManager = myContext.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LoginFragment lFragment = new LoginFragment();
            fragmentTransaction.replace(R.id.fragment_container, lFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            // Loading
            Toast.makeText(getActivity(), "Fetching profile ...", Toast.LENGTH_LONG).show();

            // Views
            favnum = (TextView) view.findViewById(R.id.textView666);
            bucks = (TextView) view.findViewById(R.id.textView);
            edit = (Button) view.findViewById(R.id.buttonEdit);
            spend = (Button) view.findViewById(R.id.buttonSpend);
            image = (ImageView) view.findViewById(R.id.imageView2);

            // Clicks
            edit.setOnClickListener(this);
            spend.setOnClickListener(this);

            // fonts
            Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface boldfont = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Hv_0.otf");
            TextView heading = (TextView) view.findViewById(R.id.textView);
            TextView subheading = (TextView) view.findViewById(R.id.textView2);
            TextView fav = (TextView) view.findViewById(R.id.textView7);
            heading.setTypeface(boldfont);
            subheading.setTypeface(font);
            fav.setTypeface(font);
            favnum.setTypeface(font);
            bucks.setTypeface(boldfont);
            spend.setTypeface(font);

            // download favourites
            new DownloadFavouritesJSON().execute();

            // download profile data
            new DownloadJSON().execute();
        }

        return view ;

    }

    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {

            case R.id.buttonEdit:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("editButton")
                        .build());

                ProfileEditFragment peFragment = new ProfileEditFragment();
                fragmentTransaction.replace(R.id.fragment_container, peFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

            case R.id.buttonSpend:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("spendButton")
                        .build());

                DealsFragment dFragment = new DealsFragment();
                fragmentTransaction.replace(R.id.fragment_container, dFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

        }

    }

    // this thread calls the general profile data

    JSONArray dataJsonArr = null;
    String chowBucks ;
    String favCount ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.account.php?id="+Launch.globalUserID);

                Log.e("JOJOJO", Launch.globalUserID);

                dataJsonArr = json.getJSONArray("user");

                JSONObject c = dataJsonArr.getJSONObject(0);

                chowBucks = c.getString("bucks") ;
                favCount = c.getString("favourites") ;

                Launch.globalUserName = c.getString("name") ;
                Launch.globalUserEmail = c.getString("email") ;
                Launch.globalUserPassword = c.getString("password") ;
                Launch.globalUserBucks = c.getString("bucks") ;
                Launch.globalFirstName = c.getString("firstname") ;
                Launch.globalLastName = c.getString("lastname") ;
                Launch.globalCondition = c.getString("condition") ;
                Launch.globalDOB = c.getString("dob") ;
                Launch.globalGender = c.getString("gender") ;
                Launch.globalUserImage = c.getString("image") ;

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {
            if (dataJsonArr!=null) {
                // set the image, favourite count & bucks
                bucks.setText(chowBucks);
                favnum.setText(favCount);

                // if there is no image set
                Log.d("JOJOJO","> "+Launch.globalUserImage);

                if ( Launch.globalUserImage.equals("") ) {
                    Launch.globalUserImage = "API_PATH/app/assets/profile_demo.jpg";
                }
                Picasso.with(getActivity())
                        .load(Launch.globalUserImage)
                        .resize(150,150)
                        .centerInside()
                        .into(image);

            } else {
                bucks.setText("0");
                favnum.setText("0");
                Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show();
            }

        }

    }

    // this thread call the favourite data
    JSONArray dataFavJsonArr = null;
    ArrayList<HashMap<String,String>> maps ;

    private class DownloadFavouritesJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.favourites.php?id="+Launch.globalUserID);

                dataFavJsonArr = json.getJSONArray("restaurants");

                maps = new ArrayList<HashMap<String,String>>();

                for (int i = 0; i < dataFavJsonArr.length(); i++) {

                    JSONObject c = dataFavJsonArr.getJSONObject(i);

                    HashMap< String, String> hMap = new HashMap< String, String>();

                    hMap.put("id", c.getString("id"));
                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("description", c.getString("description"));
                    hMap.put("gps", c.getString("gps"));

                    double la = Float.parseFloat( c.getString("gps").split(",")[0] );
                    double lo = Float.parseFloat( c.getString("gps").split(",")[1] );
                    double dist = distance(Launch.globalUserLatitude, Launch.globalUserLongitude, la, lo, 'K') ;

                    hMap.put("distance", Integer.toString((int)dist));

                    maps.add(hMap);

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataFavJsonArr!=null ) {

                gridview = (ExpandableHeightGridView) view.findViewById(R.id.gridView);
                gridview.setAdapter(new AdapterRestaurant(getActivity(), maps));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        gaTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Click")
                                .setLabel("favouriteList")
                                .build());

                        HashMap<String, String> obj = maps.get(position);
                        loadRestaurant(obj.get("id"));

                    }

                });

                // set the height

                gridview.setExpanded(true);

            } else {

                Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show();

            }

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

    Tracker gaTracker ;

    @Override
    public void onStart() {

        super.onStart();

        // set up the tracker

        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name

        gaTracker.setScreenName("Profile");

    }

}
