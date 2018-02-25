package com.chow.chow.chow;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.chow.chow.chow.adaptors.AdapterShop;
import com.chow.chow.chow.utility.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DealsFragment extends Fragment {

    View view;
    String buyid ;

    public DealsFragment() { }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("SHOP");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(4) ;

        view = inflater.inflate(R.layout.fragment_deals, container, false);

        if ( Launch.globalUserLoggedIn == false ) {

            Toast.makeText(getActivity(), "Sorry, you need to log in first.", Toast.LENGTH_SHORT).show();

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LoginFragment lFragment = new LoginFragment();
            fragmentTransaction.replace(R.id.fragment_container, lFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else {

            // load the data

            new DownloadJSON().execute();

            // fonts

            Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");

            TextView heading = (TextView) view.findViewById(R.id.textView7);
            TextView overlayDescription = (TextView) view.findViewById(R.id.overlay_description);

            heading.setTypeface(jsfont);
            overlayDescription.setTypeface(jsfont);

        }

        return view ;

    }

    JSONArray dataJsonArr = null;
    ArrayList<HashMap<String,String>> maps ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.shops.php");

                dataJsonArr = json.getJSONArray("shop");

                maps = new ArrayList<HashMap<String,String>>();

                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

                    HashMap< String, String> hMap = new HashMap< String, String>();

                    hMap.put("id", c.getString("id"));
                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("description", c.getString("description"));
                    hMap.put("bucks", c.getString("bucks"));

                    maps.add(hMap);

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataJsonArr != null ) {

                ExpandableHeightGridView gridview = (ExpandableHeightGridView) view.findViewById(R.id.gridView);
                gridview.setAdapter(new AdapterShop(getActivity(), maps));

                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
                        aAnim.setDuration(250);
                        v.startAnimation(aAnim);

                        HashMap<String,String> obj = maps.get(position);
                        buyid = obj.get("id") ;

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        gaTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Click")
                                .setLabel("shopList")
                                .build());

                        DealsDetailFragment flFragment = new DealsDetailFragment();

                        flFragment.setID(buyid) ;

                        fragmentTransaction.replace(R.id.fragment_container, flFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                    }
                });

                // set the height

                gridview.setExpanded(true);

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

        gaTracker.setScreenName("Shop");

    }

}
