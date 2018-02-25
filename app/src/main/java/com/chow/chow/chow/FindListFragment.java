package com.chow.chow.chow;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chow.chow.chow.adaptors.AdapterRestaurant;
import com.chow.chow.chow.utility.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FindListFragment extends Fragment implements Button.OnClickListener {

    Button buttonBack ;
    ExpandableHeightGridView gridview ;
    View view ;
    SeekBar seekbar ;
    TextView textHeading ;

    public String query = "" ;
    public String categories = "" ;
    public FindListFragment() {}

    RestaurantItemClicked mCallback ;

    public interface RestaurantItemClicked {
        public void loadRestaurantDetail(String text);
    }

    public void loadRestaurant(String str) {
        mCallback.loadRestaurantDetail(str);
    }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {

        myContext=(FragmentActivity) activity;
        super.onAttach(activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("LIST");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(1) ;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception

        try {
            mCallback = (RestaurantItemClicked) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ItemClicked");
        }

        view = inflater.inflate(R.layout.fragment_find_list, container, false);

        // ui

        buttonBack = (Button) view.findViewById(R.id.buttonBack);
        seekbar = (SeekBar) view.findViewById(R.id.seekBar) ;
        textHeading = (TextView) view.findViewById(R.id.textHeading);

        buttonBack.setOnClickListener(this);

        // seekbar

        seekbar.setProgress(20);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            ArrayList<HashMap<String,String>> mapsDuplicate ;
            int km = 0;

            public void editGridViewContents() {

                try {

                    if (dataJsonArr != null || maps.size() != 0) {

                        // temp holder variables called OBJ

                        HashMap<String, String> obj;

                        // create empty arraylist for the new values

                        mapsDuplicate = new ArrayList<HashMap<String, String>>();

                        // and itirate over the master map array

                        for (int i = 0; i < FindListFragment.maps.size(); i++) {

                            // get the hashmap and add it to OBJ

                            obj = FindListFragment.maps.get(i);

                            // get the INTEGER of the distance, for comparison reasons

                            int venueDistance = Integer.parseInt(obj.get("distance")) ;

                            // compare and add the new hashmap

                            if (venueDistance <= km)
                                mapsDuplicate.add(obj);

                        }

                        // Log.d("JOJOJO", "Old list: " + FindListFragment.mapsHolder.size()) ;
                        // Log.d("JOJOJO", "New list: " + mapsDuplicate.size()) ;

                        // assign the edited mapsholder the new pas arraylist

                        FindListFragment.mapsHolder = mapsDuplicate ;

                        // reassign the list adaptor to the new list

                        gridview.setAdapter(new AdapterRestaurant(getActivity(), FindListFragment.mapsHolder));

                    }

                } catch (Exception e) { e.printStackTrace(); }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                editGridViewContents();

                Toast toast = Toast.makeText(getActivity(), "Now searching within " + km + "km", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

                float v = (float)progress / 2;

                km = (int)(Math.round(v));

                // editGridViewContents();

            }

        });

        // fonts

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");

        TextView startkm = (TextView) view.findViewById(R.id.textView);
        TextView midkm = (TextView) view.findViewById(R.id.textView6);
        TextView endkm = (TextView) view.findViewById(R.id.textView2);

        buttonBack.setTypeface(font);

        startkm.setTypeface(font);
        midkm.setTypeface(font);
        endkm.setTypeface(font);

        textHeading.setTypeface(jsfont);

        // download the JSON

        new DownloadJSON().execute();

        // set the tiny test

        String result = query;
        try {
            result = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        midkm.setText("Narrow search from '"+ result +"'");

        return view ;

    }

    public void onClick(View v) {

        v.setAlpha(0.5f);

        FragmentManager fragmentManager = myContext.getSupportFragmentManager() ;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {

            case R.id.buttonBack:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("backButton")
                        .build());

                FindCategoryFragment fcFragment = new FindCategoryFragment();
                fragmentTransaction.replace(R.id.fragment_container, fcFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

        }

    }

    // the maps arraylist here is the MASTER list

    JSONArray dataJsonArr = null;

    // master array of all the items

    static ArrayList<HashMap<String,String>> maps ;

    // the one that changes the whole time when we edit criteria

    static ArrayList<HashMap<String,String>> mapsHolder ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                // turns the categories into a string like 1,2,3,4 ... etc.

                categories = getStringRepresentation( Launch.globalCategories );

                // format the URL nicely

                query = query.replace(" ", "%20");

                // DO NOT SEND THE QUERY NOW - WE'RE USING GOECODER
                // WE NEED TO GET ALLLLLL OF THE ITEMS
                // String url = "API_PATH/app/api/get.restaurants.php?query="+query+"&categories="+categories ;
                String url = "API_PATH/app/api/get.restaurants.php?query=&categories="+categories ;

                JsonParser jParser = new JsonParser();
                JSONObject json = jParser.getJSONFromUrl(url);

                dataJsonArr = json.getJSONArray("restaurants");

                maps = new ArrayList<HashMap<String,String>>();

                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

                    HashMap< String, String> hMap = new HashMap< String, String>();

                    hMap.put("id", c.getString("id"));
                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("description", c.getString("description"));
                    hMap.put("gps", c.getString("gps"));

                    double la = Float.parseFloat( c.getString("gps").split(",")[0] );
                    double lo = Float.parseFloat( c.getString("gps").split(",")[1] );
                    double dist = distance(Launch.globalSearchedLatitude, Launch.globalSearchedLongitude, la, lo, 'K') ;

                    hMap.put("distance", Integer.toString((int)dist));

                    if (dist<=50d) {
                        maps.add(hMap);
                    }

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            // track the category

            new TrackUser().execute("type=categories&user="+Launch.globalUserID+"&event="+categories) ;

            // carry on

            try {

                if (dataJsonArr != null) {

                    // we do this exercise to remove duplicates out of the array
                    // add the array to a hashset
                    // then add the hashset back to the array

                    // rework this part of the app if we need to

                    ArrayList all = maps ;
                    HashSet hashset_all = new HashSet();
                    hashset_all.addAll(all);
                    all.clear();
                    all.addAll(hashset_all);

                    // assign the new array to other variables
                    // we assign the mapsHolder variables to the maps list so that
                    // is is accessible from everywhere

                    maps = all ;
                    mapsHolder = all;

                    // go back to processing the hasmap

                    gridview = (ExpandableHeightGridView) view.findViewById(R.id.gridView);
                    gridview.setAdapter(new AdapterRestaurant(getActivity(), maps));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            // we use FindListFragment.mapsHolder INSTEAD OF maps here as it's the edited one

                            gaTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("UX")
                                    .setAction("Click")
                                    .setLabel("restaurantList")
                                    .build());

                            HashMap<String, String> obj = FindListFragment.mapsHolder.get(position);
                            loadRestaurant(obj.get("id"));

                        }

                    });

                    // set the height

                    gridview.setExpanded(true);

                    // set the initial distance
                    // itirate through the hashmap and remove the ones over 10km
                    // we now duplicate the FindListFragment.mapsHolder vairable

                    ArrayList<HashMap<String, String>> mapsDuplicate = new ArrayList<HashMap<String, String>>();

                    HashMap<String, String> obj;

                    for (int i = 0; i < FindListFragment.maps.size(); i++) {

                        // get the hashmap and add it to OBJ

                        obj = FindListFragment.maps.get(i);

                        // get the INTEGER of the distance, for comparison reasons

                        int venueDistance = Integer.parseInt(obj.get("distance")) ;

                        // compare and add the new hashmap

                        if (venueDistance <= 10)
                            mapsDuplicate.add(obj);

                    }

                    // assgined the duplicate back to the static variable
                    // so that the "int position" in our click event finds the right position
                    // because it uses the FindListFragment.mapsHolder variable

                    FindListFragment.mapsHolder = mapsDuplicate ;

                    // set the adaptor

                    gridview.setAdapter(new AdapterRestaurant(getActivity(), FindListFragment.mapsHolder));

                } else { Toast.makeText(getActivity(), "Sorry, no results were returned.", Toast.LENGTH_SHORT).show(); }

            } catch (Exception e) { e.printStackTrace(); Toast.makeText(getActivity(), "Sorry, there has been an error, please try again later.", Toast.LENGTH_SHORT).show(); }

        }

    }

    // array split

    public String getStringRepresentation(ArrayList<String> list) {

        String listString = "";

        if (list.size()>0) {

            for (String s : list) {
                listString += s + ",";
            }

        }

        return listString ;

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

        gaTracker.setScreenName("List");

    }


}