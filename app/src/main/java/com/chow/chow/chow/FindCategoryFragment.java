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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chow.chow.chow.adaptors.AdapterCategory;
import com.chow.chow.chow.utility.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FindCategoryFragment extends Fragment implements Button.OnClickListener {

    View view;
    TextView textHeading ;
    Button selectAll ;
    Button buttonFilter ;
    ExpandableHeightGridView gridview ;
    Boolean allSelected = false ;

    public String query = "" ;

    public FindCategoryFragment() { }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("CATEGORIES");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(1) ;

        view = inflater.inflate(R.layout.fragment_find_category, container, false);

        textHeading = (TextView) view.findViewById(R.id.textHeading);
        selectAll = (Button) view.findViewById(R.id.selectAll);
        buttonFilter = (Button) view.findViewById(R.id.buttonFilter);

        buttonFilter.setOnClickListener(this);
        selectAll.setOnClickListener(this);

        new DownloadJSON().execute();

        // fonts

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");

        selectAll.setTypeface(font);
        textHeading.setTypeface(jsfont);

        return view ;

    }

    public void onClick(View v) {

        v.setAlpha(0.5f);

        FragmentManager fragmentManager = myContext.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {

            case R.id.buttonFilter:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("filterButton")
                        .build());

                FindListFragment flFragment = new FindListFragment();

                flFragment.query = query ;

                fragmentTransaction.replace(R.id.fragment_container, flFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

            case R.id.selectAll:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("selectAll")
                        .build());

                if ( allSelected ) {

                    for (int i=0; i<gridview.getChildCount(); i++) {
                        View child = (View) gridview.getChildAt(i);
                        ImageView image = (ImageView)child.findViewById(R.id.check);
                        image.setAlpha(0.0f);
                    }

                    allSelected = false ;
                    selectAll.setText("SELECT ALL");

                    Launch.globalCategories = new ArrayList<String>();
                    Toast.makeText(getActivity(), "No categories selected.", Toast.LENGTH_SHORT).show();

                } else {

                    for (int i=0; i<gridview.getChildCount(); i++) {
                        View child = (View) gridview.getChildAt(i);
                        ImageView image = (ImageView)child.findViewById(R.id.check);
                        image.setAlpha(1.0f);
                    }

                    allSelected = true ;
                    selectAll.setText("SELECT NONE");

                    Launch.globalCategories = carray ;
                    Toast.makeText(getActivity(), "All categories selected.", Toast.LENGTH_SHORT).show();

                }

                break;

        }

    }

    JSONArray dataJsonArr = null;
    ArrayList<HashMap<String,String>> maps ;
    ArrayList<String> carray ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.category.php");

                dataJsonArr = json.getJSONArray("category");
                maps = new ArrayList<HashMap<String,String>>();
                carray = new ArrayList<String>();

                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

                    HashMap< String, String> hMap = new HashMap< String, String>();

                    hMap.put("id", c.getString("id"));
                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("description", c.getString("description"));

                    maps.add(hMap);
                    carray.add(c.getString("id")) ;

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if (dataJsonArr!=null) {

                gridview = (ExpandableHeightGridView) view.findViewById(R.id.gridView);
                gridview.setAdapter(new AdapterCategory(getActivity(), maps));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                        gaTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Click")
                                .setLabel("categoryButton")
                                .build());

                        // toggle the opacity of the checkbox

                        HashMap<String,String> currentMap = maps.get(position) ;

                        ImageView image = (ImageView)v.findViewById(R.id.check);
                        Float itemAlpha = image.getAlpha() ;

                        if ( itemAlpha == 1.0f) {

                            image.setAlpha(0.0f);
                            Launch.globalCategories.remove( currentMap.get("id") ) ;

                        } else {

                            image.setAlpha(1.0f);
                            Launch.globalCategories.add( currentMap.get("id") ) ;

                        }


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

        gaTracker.setScreenName("Category");

    }
}
