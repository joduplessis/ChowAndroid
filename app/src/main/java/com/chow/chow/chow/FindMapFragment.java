package com.chow.chow.chow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chow.chow.chow.adaptors.AdapterMap;
import com.chow.chow.chow.utility.*;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

public class FindMapFragment extends Fragment implements
        Button.OnClickListener, LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    Button searchButton ;
    EditText searchQuery ;
    View view ;

    protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private SupportMapFragment fragment;
    private GoogleMap mMap ;

    public FindMapFragment() {    }

    RestaurantItemClickedFromMap mCallback ;

    public interface RestaurantItemClickedFromMap {
        public void loadRestaurantDetail(String text);
    }

    public void loadRestaurantFromMap(String str) {
        mCallback.loadRestaurantDetail(str);
    }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("MAP");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(1) ;

        // so we know the user has not searched
        Launch.globalSearchedForLatLong = false ;

        // This makes sure that the container activity has implemented the callback interface. If not, it throws an exception
        try {
            mCallback = (RestaurantItemClickedFromMap) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ItemClicked");
        }

        // draw the view
        view = inflater.inflate(R.layout.fragment_find_map, container, false);

        // Get the views
        searchButton = (Button) view.findViewById(R.id.buttonSearch);
        searchQuery = (EditText) view.findViewById(R.id.editText2);

        // Set the click
        searchButton.setOnClickListener(this);

        // fonts
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

        // set fonts
        searchQuery.setTypeface(font);

        // Create the map
        initializeMap();

        // Check to see if its enabled
        LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("CHOW works better with GPS enabled on your device. Enable?").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).show();
        }

        // Build the API request for the Google map request
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // return the view
        return view ;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Move the map
        setMapLocation(location);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Make a location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request an update
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        // Get last location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("JOJOJO", "onConnected");

        // Move the map if the location is not null
        if (mLastLocation != null) {
            Log.d("JOJOJO", "onConnected - mLastLocation");
            setMapLocation(mLastLocation);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("JOJOJO", "onConnectionFailed");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        Log.d("JOJOJO", "onConnectionSuspended");
    }

    public void setMapLocation(Location location) {
        // Store the location
        Launch.globalUserLatitude = location.getLatitude();
        Launch.globalUserLongitude = location.getLongitude();

        // Just do it once
        if (!Launch.globalLocationIsFound) {
            // Move the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Launch.globalUserLatitude, Launch.globalUserLongitude)));

            // Now we set this to
            Launch.globalLocationIsFound = true;
        }
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        try {

            //SupportMapFragment f = (SupportMapFragment) myContext.getSupportFragmentManager().findFragmentById(R.id.map);

            //if (f != null)
               // myContext.getSupportFragmentManager().beginTransaction().remove(f).commit();

        } catch (Exception e) {

            // you can't perform the above after statesave instance
            // it crashes when you press back from the map
            // Why?
            // becayse the part above is a hack to get the map not to crash when you view it twice
            // so THAT throws an error when you press back from the first screen
            // what's the solution?
            // to add the google ap programmatically

        }

    }

    public static void closeKeyboard(Context c, IBinder windowToken) {

        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);

    }

    public void onClick(View v) {

        v.setAlpha(0.5f);

        switch(v.getId()) {

            case R.id.buttonSearch:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("searchButton")
                        .build());

                // close the keyboard

                closeKeyboard(getActivity(), searchQuery.getWindowToken());

                // search for the location

                new findLocationData().execute() ;

                break;
        }

    }

    JSONArray dataJsonArr = null;
    ArrayList<HashMap<String,String>> maps ;
    JSONObject json ;
    Marker lastOpenned = null;

    public static ArrayList<HashMap<String,String>> mapsHolder ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();
                json = jParser.getJSONFromUrl("API_PATH/app/api/get.restaurants.php");
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
                    maps.add(hMap);
                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            try {

                // Carry on if everything is 100%
                mapsHolder = maps;

                // Add markers
                for (int i = 0; i < maps.size(); i++) {
                    HashMap<String, String> gMap = maps.get(i);
                    Double la = Double.parseDouble(gMap.get("gps").split(",")[0]);
                    Double lo = Double.parseDouble(gMap.get("gps").split(",")[1]);
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(la, lo))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.find_map_pin))
                            .title(gMap.get("title")));
                }

                // Do not move the map
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker marker) {
                        gaTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Click")
                                .setLabel("mapPopup")
                                .build());
                        if (lastOpenned != null) {
                            lastOpenned.hideInfoWindow();
                            if (lastOpenned.equals(marker)) {
                                lastOpenned = null;
                                return true;
                            }
                        }
                        marker.showInfoWindow();
                        lastOpenned = marker;
                        return true;
                    }
                });

                // Default map behaviour
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Launch.globalUserLatitude, Launch.globalUserLongitude)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                mMap.setInfoWindowAdapter(new AdapterMap(LayoutInflater.from(getActivity())));

                // When you click on the popup
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        String id = "0";
                        for (int i = 0; i < maps.size(); i++) {
                            // Create a new hash map for each restaurant object
                            HashMap<String, String> gMap = maps.get(i);

                            // Get the LAT/LONG from the marker & the API
                            Double laFromApi = Double.parseDouble(gMap.get("gps").split(",")[0]);
                            Double loFromApi = Double.parseDouble(gMap.get("gps").split(",")[1]);
                            Double laFromMarker = marker.getPosition().latitude;
                            Double loFromMarker = marker.getPosition().longitude;

                            // If the pressed marker equals the API assign the id to use
                            if(laFromApi.equals(laFromMarker) && loFromApi.equals(loFromMarker)) {
                                id = gMap.get("id");
                            }
                        }

                        // Load our restaurant
                        if (id.equals("0")) {
                            Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong.", Toast.LENGTH_SHORT).show();
                        } else {
                            loadRestaurantFromMap(id);
                        }
                    }
                });

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show();
                Log.d("JOJO Post", e.toString());
            }
        }
    }

    @Override
    public void onResume() {
        // Boiler plate
        super.onResume();

        // If our map is null
        if (mMap == null) {
            // Get the map instance
            mMap = fragment.getMap();

            // Download the map data
            new DownloadJSON().execute();
        }
    }

    // Geocoder stuff
    List<Address> addresses = null;

    // Build and send an Event.
    Tracker gaTracker ;

    private class findLocationData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                addresses = geocoder.getFromLocationName(searchQuery.getText().toString(), 1);
            } catch (IOException e) {
                Log.w("JOJOJO", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            try {

                Log.d("JOJOJO", "Search lat & long: "+addresses.get(0).getLatitude() + " / " + addresses.get(0).getLongitude());

                // Set the global variables
                Launch.globalSearchedLatitude = addresses.get(0).getLatitude() ;
                Launch.globalSearchedLongitude = addresses.get(0).getLongitude() ;

                // so we know the user has searched
                Launch.globalSearchedForLatLong = true ;

                // move the fragment
                FragmentManager fragmentManager = myContext.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FindCategoryFragment fllFragment = new FindCategoryFragment();
                fllFragment.query = searchQuery.getText().toString() ;
                fragmentTransaction.replace(R.id.fragment_container, fllFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Sorry, we can't find the location you've searched for.", Toast.LENGTH_SHORT).show();
                searchButton.setAlpha(1f);
            }


        }

    }

    @Override
    public void onStart() {

        super.onStart();
        mGoogleApiClient.connect();

        // set up the tracker
        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name
        gaTracker.setScreenName("Map");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void initializeMap() {
        // Get the fragment manager to use for the map
        FragmentManager fm = getChildFragmentManager();

        // Get our fragment (relative layout)
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        // If it's null, then create it
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

}





        /*


        USING THE NON PLAY SERVICES LOCATION MANAGER

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        @Override
        public void onProviderEnabled(String provider) {
            // Toast.makeText(getActivity(), "onProviderEnabled!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Toast.makeText(getActivity(), "onProviderDisabled!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Toast.makeText(getActivity(), "onStatusChanged: "+provider.toString()+" "+status+" "+extras.toString()+"!", Toast.LENGTH_SHORT).show();
        }

        -----------------------------------------------------------------------------

        The map fragment should ideally be added programmatically, WHICH WE NOW DO
        Extend the MapFragment class and override the onCreateView() method. After this method is done we can get a non-null reference to que GoogleMap object.

        http://stackoverflow.com/questions/13733299/initialize-mapfragment-programmatically-with-maps-api-v2
        http://stackoverflow.com/questions/14565460/error-opening-supportmapfragment-for-second-time

        mMap = ((SupportMapFragment) myContext.getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        The onDestroyView method, combined with the "if ( !currentScreen.equals(item) )" is a hack

        <fragment xmlns:android="http://schemas.android.com/apk/res/android"
            android:id="@+id/map"
            android:layout_width="match_parent"
            android:layout_height="fill_parent"
            android:name="com.google.android.gms.maps.SupportMapFragment" />


            if ( getLocation() == null ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("CHOW requires GPS to be enabled on your device.").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).show();
            }



            if ( !isNetworkAvailable() ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("CHOW requires an active internet connection on your device.").setPositiveButton("Okay", null).show();
            }

        */