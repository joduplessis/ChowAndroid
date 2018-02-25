package com.chow.chow.chow;


import com.chow.chow.chow.utility.*;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chow.chow.chow.adaptors.AdapterMeal;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import aurelienribon.tweenengine.TweenManager;

public class FindRestaurantDetailFragment extends Fragment {

    String id ;
    View view ;
    ImageButton favicon ;
    Boolean favIconToggle = false ;
    Button overlay_button ;
    ImageButton overlay_icon ;
    TextView overlay_description ;
    RelativeLayout overlay_panel ;
    Tracker gaTracker ;
    ScrollView scrollView ;
    ExpandableHeightGridView gridview;

    private final TweenManager tweenManager = new TweenManager();
    private boolean isAnimationRunning = true;
    static Boolean open = false ;

    public FindRestaurantDetailFragment() {
        this.id = "0" ;
    }

    public void setID(String tid) {
        this.id = tid ;
        Launch.globalCurrentRestaurant = tid ;
    }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    MealItemClicked mCallback ;

    // MealItemClicked is an interface - it lists the method Launch needs to implement
    // so any method we call here like mCallback.loadBlah

    public interface MealItemClicked {
        public void loadMealDetail(String text);
    }

    // load meal gets called - and runs loadMealDetail from mCallback
    // What is mCallback - mCallback is an interface that is declared in Launch
    // so we are running this method in Launch

    public void loadMeal(String str) {
        mCallback.loadMealDetail(str);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("RESTAURANT DETAIL");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(1) ;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (MealItemClicked) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ItemClicked");
        }

        // Get the views
        view = inflater.inflate(R.layout.fragment_find_restaurant_detail, container, false);
        favicon = (ImageButton) view.findViewById(R.id.favicon);
        overlay_button = (Button) view.findViewById(R.id.overlay_button);
        overlay_icon = (ImageButton) view.findViewById(R.id.overlay_icon);
        overlay_description = (TextView) view.findViewById(R.id.overlay_description);
        overlay_panel = (RelativeLayout) view.findViewById(R.id.overlay_panel);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        // Set the fonts
        Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");
        overlay_description.setTypeface(jsfont);

        // If this user has been the last to see it, then hide it
        if (Launch.globalUserSeenOverlay.equals(Launch.globalUserID)) {
            overlay_panel.setVisibility(View.INVISIBLE);
        } else {
            overlay_panel.setVisibility(View.VISIBLE);
        }

        // Add the button event
        overlay_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // hide the panel
                overlay_panel.setVisibility(View.INVISIBLE);

                // Set the last seen user to this user ID
                Launch.globalUserSeenOverlay = String.valueOf(Launch.globalUserID) ;

                // Store the shared preference
                Context context = getActivity();
                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("favicon", "yes");
                editor.putString("globalUserSeenOverlay", Launch.globalUserSeenOverlay);
                editor.commit();

            }
        });

        // favicon

        favicon.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
                aAnim.setDuration(250);
                v.startAnimation(aAnim);

                if (favIconToggle) {
                    favicon.setBackgroundResource(R.drawable.profile_icon_favourites_dark);
                    favIconToggle = false;
                } else {
                    favicon.setBackgroundResource(R.drawable.profile_icon_favourites_light);
                    favIconToggle = true;
                }

                // call to web server
                new toggleFavourite().execute() ;

            }

        });

        // is this favourited
        new isFavourite().execute();

        // get data
        new DownloadJSON().execute();

        return view ;

    }

    @Override
    public void onStart () {

        super.onStart();

        // track
        new TrackUser().execute("type=restaurant&user="+Launch.globalUserID+"&event="+this.id) ;

        // set up the tracker
        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name
        gaTracker.setScreenName("Restaurant Detail");

    }

    JSONArray dataJsonArr = null;
    HashMap< String, String> hMap ;
    ArrayList<HashMap<String,String>> meals ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();
                String URL = "API_PATH/app/api/get.restaurant.php?id=" + id ;
                JSONObject json = jParser.getJSONFromUrl(URL);
                dataJsonArr = json.getJSONArray("restaurant");

                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

                    meals = new ArrayList<HashMap<String,String>>();
                    hMap = new HashMap< String, String>();

                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("address", c.getString("address"));
                    hMap.put("contact", c.getString("contact"));
                    hMap.put("description", c.getString("description"));
                    hMap.put("gps", c.getString("gps"));

                    JSONArray dataJsonMealArr = c.getJSONArray("meals");

                    for (int j = 0; j < dataJsonMealArr.length(); j++) {

                        JSONObject d = dataJsonMealArr.getJSONObject(j);

                        HashMap< String, String> mMap = new HashMap< String, String>();

                        mMap.put("id", d.getString("id"));
                        mMap.put("image", d.getString("image"));
                        mMap.put("title", d.getString("title"));
                        mMap.put("description", d.getString("description"));
                        mMap.put("bucks", d.getString("bucks"));

                        meals.add(mMap);

                    }

                    // On this screen we pad the list with a blank item
                    // This is done because it cuts off sometimes using the ExapandableHeightGridView class
                    // A proper workaround needs to be found
                    HashMap< String, String> mMap = new HashMap< String, String>();
                    mMap.put("id", "");
                    mMap.put("image", "");
                    mMap.put("title", "");
                    mMap.put("description", "");
                    mMap.put("bucks", "");
                    meals.add(mMap);

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataJsonArr!= null ) {

                // get venue location

                double la = Float.parseFloat( hMap.get("gps").split(",")[0] );
                double lo = Float.parseFloat( hMap.get("gps").split(",")[1] );
                double dist ;

                // if the user has searched for the place, then use searched for co-ordiantes
                // to establish the distance, and not his own co-ordniates
                if ( Launch.globalSearchedForLatLong ) {
                    Log.d("JOJOJO", "HAS searched, was ... Lat: " +  Launch.globalUserLatitude + " & Long: " + Launch.globalUserLongitude);
                    dist = distance(Launch.globalSearchedLatitude, Launch.globalSearchedLongitude, la, lo, 'K');
                } else {
                    Log.d("JOJOJO", "HAS NOT searched, was ... Lat: " +  Launch.globalUserLatitude + " & Long: " + Launch.globalUserLongitude);
                    dist = distance(Launch.globalUserLatitude, Launch.globalUserLongitude, la, lo, 'K');
                }

                String distancestring = Integer.toString((int)dist) + "km";

                // populate the view
                ImageView image = (ImageView) view.findViewById(R.id.imageView2);
                TextView title = (TextView) view.findViewById(R.id.textView6);
                TextView address = (TextView) view.findViewById(R.id.textView77);
                Button number = (Button) view.findViewById(R.id.buttonLogin);
                final TextView description = (TextView) view.findViewById(R.id.textView3);
                TextView distance = (TextView) view.findViewById(R.id.textView7);

                title.setText(hMap.get("title"));
                address.setText(hMap.get("address"));
                number.setText("  " + hMap.get("contact"));
                distance.setText(distancestring);
                description.setClickable(true);

                // description
                if ( hMap.get("description").length() > 100 ) {

                    final String jsonString = hMap.get("description") ;
                    final String shortstring = jsonString.substring(0, Math.min(jsonString.length(),100)) + "... <b>Read More</b>" ;
                    final String longstring = jsonString + " ... <b>Read Less</b>" ;

                    description.setText(Html.fromHtml(shortstring)) ;
                    description.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {

                            gaTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("UX")
                                    .setAction("Click")
                                    .setLabel("readMoreToggle")
                                    .build());

                            if (FindRestaurantDetailFragment.open) {
                                FindRestaurantDetailFragment.open = false ;
                                description.setText(Html.fromHtml(longstring));
                            } else {
                                FindRestaurantDetailFragment.open = true ;
                                description.setText(Html.fromHtml(shortstring));
                            }
                        }

                    });

                } else {

                    description.setText(hMap.get("description"));

                }

                Picasso.with(getActivity()).load(Launch.imageURL + "" + hMap.get("image")).fit().centerCrop().into(image) ;
                Button directions = (Button) view.findViewById(R.id.button3);
                TextView which = (TextView) view.findViewById(R.id.textView);

                // fonts
                // This threw an error and seems to be a bug -> http://stackoverflow.com/questions/30216635/attempt-to-invoke-virtual-method-android-content-res-assetmanager-android-conte
                try {
                    Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
                    directions.setTypeface(font);
                    which.setTypeface(font);
                    title.setTypeface(font);
                    address.setTypeface(font);
                    number.setTypeface(font);
                    description.setTypeface(font);
                    distance.setTypeface(font);
                } catch (Exception e) {}

                // clickable number
                final String numberForClick = number.getText().toString() ;
                final String gps = hMap.get("gps");

                number.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        gaTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Click")
                                .setLabel("phoneNumberCall")
                                .build());
                        try {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:"+numberForClick));
                            startActivity(callIntent);
                        } catch (ActivityNotFoundException activityException) {
                            Log.e("Calling a Phone Number", "Call failed", activityException);
                        }
                    }

                });

                // directions button
                directions.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        gaTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Click")
                                .setLabel("mapDirections")
                                .build());
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + gps + "&saddr=Current+Location"));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                        Log.d("JOJOJO", "Opening GPS: "+gps);
                    }
                });

                // this is our meal list
                gridview = (ExpandableHeightGridView) view.findViewById(R.id.gridView);
                gridview.setAdapter(new AdapterMeal(getActivity(), meals));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        // We get this from the meals array using the POSITION parameter
                        HashMap<String, String> obj = meals.get(position);

                        // There is often a blank item added to pad the GridView list
                        // This is done because it cuts off sometimes using the ExapandableHeightGridView class
                        // A proper workaround needs to be found
                        if (obj.get("id")!="") {
                            // GA code
                            gaTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("UX")
                                    .setAction("Click")
                                    .setLabel("mealItem")
                                    .build());

                            // Load the meal screen
                            loadMeal(obj.get("id"));
                        }
                    }
                });

                // Set the height & scroll position
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Scroll to the top
                        scrollView.scrollTo(0,0);

                        // Expand the height
                        gridview.setExpanded(true);

                        // Make the grid higher
                        int gridHeight = gridview.getHeight()+1000;
                        ViewGroup.LayoutParams layoutParams = gridview.getLayoutParams();
                        layoutParams.height = gridHeight;
                        gridview.setLayoutParams(layoutParams);
                    }
                }, 0);

            } else {
                Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show();
            }

        }

    }

    Integer favourite_success ;
    JSONArray dataFavouriteJsonArr = null;

    private class toggleFavourite extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                if (Launch.globalUserLoggedIn) {

                    JsonParser jParser = new JsonParser();

                    JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/edit.favourite.php?user="+Launch.globalUserID+"&restaurant="+Launch.globalCurrentRestaurant);

                    dataFavouriteJsonArr = json.getJSONArray("favourite");

                    JSONObject c = dataFavouriteJsonArr.getJSONObject(0);

                    favourite_success = c.getInt("success") ;

                } else {

                    Toast.makeText(getActivity(), "Sign up to add favourites.", Toast.LENGTH_SHORT).show();

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            // if the user is logged in

            if (Launch.globalUserLoggedIn) {

                // if the JSON returned 100%

                if (dataFavouriteJsonArr != null) {

                    if (favourite_success == -1)
                        Toast.makeText(getActivity(), "Sorry, your details seem to be incorrect.", Toast.LENGTH_SHORT).show();

                    if (favourite_success == 1)
                        Toast.makeText(getActivity(), "Added as a favourite!", Toast.LENGTH_SHORT).show();

                    if (favourite_success == 0)
                        Toast.makeText(getActivity(), "Removed favourite!", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(getActivity(), "Sign up to add favourites.", Toast.LENGTH_SHORT).show();

            }

        }

    }


    Integer is_favourite_success ;
    JSONArray dataIsFavouriteJsonArr = null;

    private class isFavourite extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                if (Launch.globalUserLoggedIn) {

                    JsonParser jParser = new JsonParser();

                    String url = "API_PATH/app/api/get.favourite.php?id="+Launch.globalUserID+"&restaurant="+Launch.globalCurrentRestaurant ;

                    Log.d("JOJOJO", url);

                    JSONObject json = jParser.getJSONFromUrl(url);

                    dataIsFavouriteJsonArr = json.getJSONArray("favourite");

                    JSONObject c = dataIsFavouriteJsonArr.getJSONObject(0);

                    is_favourite_success = c.getInt("id") ;

                    Log.d("JOJOJO", "->"+is_favourite_success.toString());

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            // if the user is logged in

            if (Launch.globalUserLoggedIn) {

                // if the JSON returned 100%

                if (dataIsFavouriteJsonArr != null) {

                    if (is_favourite_success == 1) {

                        Log.d("JOJOJO", " Change image ->"+is_favourite_success.toString());

                        favIconToggle = true ;

                        favicon.setBackgroundResource(R.drawable.profile_icon_favourites_light);

                    }

                }

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

}
