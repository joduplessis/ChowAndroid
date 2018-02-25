package com.chow.chow.chow;

import com.chow.chow.chow.utility.*;


import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class FindMealDetailFragment extends Fragment implements Button.OnClickListener {

    View view ;
    String id ;
    Button buttonRedeem ;
    ImageView bucksBg ;
    TextView bucksText ;
    TextView bucks ;

    // GA

    Tracker gaTracker ;

    static Boolean open = false ;

    public FindMealDetailFragment() {
        this.id = "0" ;
    }

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    public void setID(String tid) {
        this.id = tid ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("MEAL DETAIL");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(1) ;

        view = inflater.inflate(R.layout.fragment_find_meal_detail, container, false);

        buttonRedeem = (Button) view.findViewById(R.id.buttonRedeem);
        bucksBg = (ImageView) view.findViewById(R.id.imageView4);
        bucksText = (TextView) view.findViewById(R.id.textView);
        bucks = (TextView) view.findViewById(R.id.textView2);

        buttonRedeem.setOnClickListener(this);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");

        buttonRedeem.setTypeface(font);
        buttonRedeem.setVisibility(View.INVISIBLE);
        bucksBg.setVisibility(View.INVISIBLE);
        bucks.setVisibility(View.INVISIBLE);
        bucksText.setVisibility(View.INVISIBLE);

        // get data

        new DownloadJSON().execute();

        return view ;

    }

    @Override
    public void onStart () {

        super.onStart();

        // track

        new TrackUser().execute("type=meal&user="+Launch.globalUserID+"&event="+this.id) ;

        // set up the tracker

        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name

        gaTracker.setScreenName("Meal Detail");

    }

    public void onClick(View v) {

        v.setAlpha(0.5f);

        FragmentManager fragmentManager = myContext.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {

            case R.id.buttonRedeem:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("redeemButton")
                        .build());

                RedeemFragment frFragment = new RedeemFragment();
                fragmentTransaction.replace(R.id.fragment_container, frFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

        }

    }

    JSONArray dataJsonArr = null;
    HashMap< String, String> hMap ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.meal.php?id="+id);

                dataJsonArr = json.getJSONArray("meal");

                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

                    hMap = new HashMap< String, String>();

                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("information", c.getString("information"));
                    hMap.put("description", c.getString("description"));
                    hMap.put("bucks", c.getString("bucks"));

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataJsonArr!=null ) {

                TextView title = (TextView) view.findViewById(R.id.textView6);
                final TextView description = (TextView) view.findViewById(R.id.textView3);
                ImageView image = (ImageView) view.findViewById(R.id.imageView2);
                WebView information = (WebView) view.findViewById(R.id.webView) ;

                bucks.setText(hMap.get("bucks"));
                title.setText(hMap.get("title"));
                description.setText(hMap.get("description"));
                information.loadUrl("API_PATH/app/api/get.mealnutrition.php?id="+id);

                Picasso.with(getActivity()).load(Launch.imageURL + "" + hMap.get("image")).fit().centerCrop().into(image);

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

                            if (FindMealDetailFragment.open) {
                                FindMealDetailFragment.open = false ;
                                description.setText(Html.fromHtml(longstring));
                            } else {
                                FindMealDetailFragment.open = true ;
                                description.setText(Html.fromHtml(shortstring));
                            }
                        }

                    });

                } else {

                    description.setText(hMap.get("description"));

                }

                // fonts

                Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

                title.setTypeface(font);
                bucks.setTypeface(font);
                description.setTypeface(font);

            } else { Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show(); }

        }

    }


}
