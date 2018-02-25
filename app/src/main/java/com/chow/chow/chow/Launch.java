package com.chow.chow.chow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment ;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar ;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.analytics.GoogleAnalytics ;
import com.facebook.Session;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

public class Launch extends FragmentActivity implements FindListFragment.RestaurantItemClicked,
                                                        ProfileFragment.RestaurantItemClicked,
                                                        FindRestaurantDetailFragment.MealItemClicked,
                                                        FindMapFragment.RestaurantItemClickedFromMap {

    // User details store
    public static String globalCurrentRestaurant = "" ;
    public static String globalUserSeenTutorial = "0" ;
    public static String globalUserSeenOverlay = "0" ;
    public static String globalUserName = "";
    public static String globalUserEmail = "";
    public static String globalUserPassword = "";
    public static String globalUserBucks = "";
    public static String globalUserImage = "";
    public static String globalFirstName = "";
    public static String globalLastName = "";
    public static String globalCondition = "";
    public static String globalDOB = "";
    public static String globalGender = "";
    public static String globalUserID = "0";
    public static Boolean globalUserLoggedIn = false;
    public static Boolean globalUserLoggedInFB = false;
    public static double globalUserLongitude = 31.0365951d;
    public static double globalUserLatitude = -29.7772063d;
    public static double globalSearchedLongitude = 31.0365951d;
    public static double globalSearchedLatitude = -29.7772063d;
    public static boolean globalSearchedForLatLong = false;
    public static boolean globalLocationIsFound = false ;
    public static ArrayList<String> globalCategories = new ArrayList<String>();
    // GA
    Tracker gaTracker ;

    // location of online images
    public static String imageURL = "API_PATH/app/assets/" ;

    // general
    LinearLayout linearLayout ;
    ImageButton buttonFind ;
    ImageButton buttonShop ;
    ImageButton buttonProfile ;
    ImageButton buttonRedeem ;
    Button logouticon ;
    Typeface font ;

    // this is called from profile, map & list fragments
    @Override
    public void loadRestaurantDetail(String text) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FindRestaurantDetailFragment fragment = new FindRestaurantDetailFragment();
        fragment.setID(text);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    // called from restaurant fragment
    @Override
    public void loadMealDetail(String text) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FindMealDetailFragment fragment = new FindMealDetailFragment();
        fragment.setID(text);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the content view to our launcher fragment
        setContentView(R.layout.activity_launch);

        // set up shared preferences if the user is already logged in
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (sharedPref.getString("loggedin", "").equals( "yes" )) {
            Launch.globalUserID = sharedPref.getString("id", "") ;
            Launch.globalUserLoggedIn = true;
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
        }

        // if they're logged in with FB
        if (sharedPref.getString("loggedinfb", "").equals( "yes" )) {
            Launch.globalUserLoggedInFB = true;
        }

        // If they've seen the overlay, store their ID
        if (!sharedPref.getString("globalUserSeenOverlay", "").equals("0")) {
            Launch.globalUserSeenOverlay = sharedPref.getString("globalUserSeenOverlay", "");
        }

        // If they've seen the tutorial, store their ID
        if (!sharedPref.getString("globalUserSeenTutorial", "").equals("0")) {
            Launch.globalUserSeenTutorial = sharedPref.getString("globalUserSeenTutorial", "");
        }

        // Logs
        Log.d("JOJOJOJO", "Overlay store: "+Launch.globalUserSeenOverlay);
        Log.d("JOJOJOJO", "Tutorial store: "+Launch.globalUserSeenTutorial);

        // set the dont
        font = Typeface.createFromAsset(getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

        // actionbar at the bottom
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        // Action bar properties
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.actionbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(getResources().getDrawable(R.drawable.blank));

        // Buttons
        buttonFind = (ImageButton) findViewById(R.id.naviconfind);
        buttonShop = (ImageButton) findViewById(R.id.naviconshop);
        buttonProfile = (ImageButton) findViewById(R.id.naviconprofile);
        buttonRedeem = (ImageButton) findViewById(R.id.naviconredeem);

        // Click events
        buttonFind.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
            aAnim.setDuration(250);
            v.startAnimation(aAnim);
            navbarItemClicked(1) ;
        } });
        buttonShop.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
            aAnim.setDuration(250);
            v.startAnimation(aAnim);
            navbarItemClicked(2) ;
        } });
        buttonProfile.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
            aAnim.setDuration(250);
            v.startAnimation(aAnim);
            navbarItemClicked(4) ;
        } });
        buttonRedeem.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
            aAnim.setDuration(250);
            v.startAnimation(aAnim);
            navbarItemClicked(3) ;
        } });

        // Favicon / logout buttons at the top
        logouticon = (Button) findViewById(R.id.logouticon);
        logouticon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
                aAnim.setDuration(250);
                v.startAnimation(aAnim);
                logout();
            }
        });

        // Set of the default actionbar stuff
        setTheme(R.style.AppTheme);
        setActionbarTitle("CHOW");
        navbarItemActivate(1) ;

        // if the fragment container exists
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                FindMapFragment firstFragment = new FindMapFragment() ;
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment, "MAP_FRAGMENT").commit();
            }
        }

        // Add code to print out the key hash for private use
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.chow.chow.chow",PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {} catch (NoSuchAlgorithmException e) {}

        // get screenwidth and set button heights
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        Double width = new Double (size.x / 4 / 1.3) ;
        int integerWidth = width.intValue() ;

        // Set the widths
        buttonFind.getLayoutParams().height = integerWidth;
        buttonShop.getLayoutParams().height = integerWidth;
        buttonProfile.getLayoutParams().height = integerWidth ;
        buttonRedeem.getLayoutParams().height = integerWidth ;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        // Stock
        super.onResume();

        // reset backgrounds
        buttonFind.setBackgroundResource(R.drawable.nav_icon_find);
        buttonShop.setBackgroundResource(R.drawable.nav_icon_deals);
        buttonProfile.setBackgroundResource(R.drawable.nav_icon_profile);
        buttonRedeem.setBackgroundResource(R.drawable.nav_icon_redeem);

        // Fragment changing
        /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment flFragment = new FindMapFragment();
        fragmentTransaction.replace(R.id.fragment_container, flFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        */

        // Recreate the height of the navbar
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        Double width = new Double (size.x / 4 / 1.3) ;
        int integerWidth = width.intValue() ;

        // Set the actual height
        buttonFind.getLayoutParams().height = integerWidth;
        buttonShop.getLayoutParams().height = integerWidth;
        buttonProfile.getLayoutParams().height = integerWidth ;
        buttonRedeem.getLayoutParams().height = integerWidth ;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.launch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (id) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    // public method for changing the actionbar title & fav icon
    public void setActionbarTitle(String str) {

        TextView actionBarTitle = (TextView) findViewById(R.id.actionbarTitle);
        actionBarTitle.setTypeface(font);
        actionBarTitle.setText(str);

        if ( this.globalUserLoggedIn )
            logouticon.setAlpha(1.0f);
        else
            logouticon.setAlpha(0.0f);

    }

    public void navbarItemActivate(Integer item) {
        // reset backgrounds
        buttonFind.setBackgroundResource(R.drawable.nav_icon_find);
        buttonShop.setBackgroundResource(R.drawable.nav_icon_deals);
        buttonProfile.setBackgroundResource(R.drawable.nav_icon_profile);
        buttonRedeem.setBackgroundResource(R.drawable.nav_icon_redeem);

        // Make these active
        switch (item) {
            case 1:
                buttonFind.setBackgroundResource(R.drawable.nav_icon_find_over);
                break;

            case 2:
                buttonRedeem.setBackgroundResource(R.drawable.nav_icon_redeem_over);
                break;

            case 3:
                buttonProfile.setBackgroundResource(R.drawable.nav_icon_profile_over);
                break;

            case 4:
                buttonShop.setBackgroundResource(R.drawable.nav_icon_deals_over);
                break;
        }

    }

    public void navbarItemClicked(Integer item) {
        // regsiter the click
        gaTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Navigation")
                .setAction("Click")
                .setLabel(""+item)
                .build());

        // reset backgrounds
        buttonFind.setBackgroundResource(R.drawable.nav_icon_find);
        buttonShop.setBackgroundResource(R.drawable.nav_icon_deals);
        buttonProfile.setBackgroundResource(R.drawable.nav_icon_profile);
        buttonRedeem.setBackgroundResource(R.drawable.nav_icon_redeem);

        // Fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (item) {
            case 1:
                buttonFind.setBackgroundResource(R.drawable.nav_icon_find_over);
                FindMapFragment flFragment = new FindMapFragment();
                fragmentTransaction.replace(R.id.fragment_container, flFragment, "MAP_FRAGMENT");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case 2:
                buttonShop.setBackgroundResource(R.drawable.nav_icon_deals_over);
                DealsFragment dFragment = new DealsFragment();
                fragmentTransaction.replace(R.id.fragment_container, dFragment, "DEALS_FRAGMENT");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;


            case 3:
                buttonRedeem.setBackgroundResource(R.drawable.nav_icon_redeem_over);
                RedeemFragment rFragment = new RedeemFragment();
                fragmentTransaction.replace(R.id.fragment_container, rFragment, "REDEEM_FRAGMENT");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case 4:
                buttonProfile.setBackgroundResource(R.drawable.nav_icon_profile_over);
                ProfileFragment pFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.fragment_container, pFragment, "PROFILE_FRAGMENT");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
        }
    }

    public void logout() {

        // clear FB session
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
        } else {
            Session session2 = Session.openActiveSession(this, false, null);
            if(session2 != null)
                session2.closeAndClearTokenInformation();
        }
        Session.setActiveSession(null);

        // edit global variables
        Launch.globalUserID = "";
        Launch.globalUserImage = "";
        Launch.globalUserLoggedIn = false;
        Launch.globalUserLoggedInFB = false ;
        Launch.globalCategories = new ArrayList<String>();
        Launch.globalUserName = "";
        Launch.globalUserEmail = "";
        Launch.globalUserPassword = "";
        Launch.globalUserBucks = "";
        Launch.globalFirstName = "";
        Launch.globalLastName = "";
        Launch.globalCondition = "";
        Launch.globalDOB = "";
        Launch.globalGender = "";
        Launch.globalCurrentRestaurant = "";
        Launch.globalSearchedForLatLong = false;
        Launch.globalLocationIsFound = false ;

        /*
        Launch.globalUserLongitude = 31.0365951d;
        Launch.globalUserLatitude = -29.7772063d;
        Launch.globalSearchedLongitude = 31.0365951d;
        Launch.globalSearchedLatitude = -29.7772063d;
        */

        // user association
        gaTracker.set("&uid", "");

        // edit stored variables
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("loggedin", "no");
        editor.putString("loggedinfb", "no");
        editor.putString("id", "");
        editor.putString("redeem_timeout", "0");
        editor.commit();

        // globalUserSeenTutorial & globalUserSeenOverlay is not reset
        // Because we need the state to always be there

        // load the login fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment lFragment = new LoginFragment();
        fragmentTransaction.replace(R.id.fragment_container, lFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public void hideActionbar() {
        linearLayout.setVisibility(LinearLayout.GONE);
    }

    public void showActionbar() {
        linearLayout.setVisibility(LinearLayout.VISIBLE);
    }

    @Override
    protected void onStart() {
        // Normal
        super.onStart();

        // set up the tracker
        gaTracker = ((CHOWApplication) this.getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name
        gaTracker.setScreenName("Launch");

        // register the screen
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

}
