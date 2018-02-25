package com.chow.chow.chow;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chow.chow.chow.utility.JsonParser;
import com.chow.chow.chow.utility.TrackUser;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DealsDetailFragment extends Fragment implements Button.OnClickListener {

    View view ;
    String id ;

    Button buttonBuy ;

    static Boolean open = false ;

    public DealsDetailFragment() { this.id = "0" ; }

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

        ((Launch) getActivity()).setActionbarTitle("SHOP DETAIL");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(4) ;

        view = inflater.inflate(R.layout.fragment_deals_detail, container, false);


        buttonBuy = (Button) view.findViewById(R.id.buttonBuy);

        buttonBuy.setOnClickListener(this);

        // get data

        new DownloadJSON().execute();

        // track

        new TrackUser().execute("type=shop&user="+Launch.globalUserID+"&event="+this.id) ;

        return view ;

    }

    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        switch(v.getId()) {

            case R.id.buttonBuy:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("purchase")
                        .build());
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case DialogInterface.BUTTON_POSITIVE:
                                gaTracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("UX")
                                        .setAction("Click")
                                        .setLabel("purchaseConfirm")
                                        .build());
                                Toast.makeText(getActivity(), "Making purchase!", Toast.LENGTH_SHORT).show();
                                new sendBuyAction().execute();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // DO NOTHING
                                break;

                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

                break;

        }

    }

    // getting the info from the web

    JSONArray dataJsonArr = null;
    HashMap< String, String> hMap ;
    ArrayList<HashMap<String,String>> maps ;

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/get.shop.php?id="+id);

                dataJsonArr = json.getJSONArray("shop");

                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

                    hMap = new HashMap< String, String>();

                    hMap.put("id", c.getString("id"));
                    hMap.put("image", c.getString("image"));
                    hMap.put("title", c.getString("title"));
                    hMap.put("description", c.getString("description"));
                    hMap.put("detail", c.getString("detail"));
                    hMap.put("bucks", c.getString("bucks"));

                }

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataJsonArr!=null ) {

                TextView bucks = (TextView) view.findViewById(R.id.textView2);
                TextView title = (TextView) view.findViewById(R.id.textView6);
                TextView description = (TextView) view.findViewById(R.id.textView3);
                final TextView info = (TextView) view.findViewById(R.id.textView5);
                ImageView image = (ImageView) view.findViewById(R.id.imageView2);

                bucks.setText(hMap.get("bucks"));
                title.setText(hMap.get("title"));
                description.setText(hMap.get("description"));

                Picasso.with(getActivity()).load(Launch.imageURL + "" + hMap.get("image")).fit().centerCrop().into(image);

                info.setClickable(true);

                // description

                if ( hMap.get("detail").length() > 100 ) {

                    final String jsonString = hMap.get("detail") ;
                    final String shortstring = jsonString.substring(0, Math.min(jsonString.length(),100)) + "... <b>Read More</b>" ;
                    final String longstring = jsonString + " ... <b>Read Less</b>" ;

                    info.setText(Html.fromHtml(shortstring)) ;
                    info.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {

                            if (DealsDetailFragment.open) {
                                DealsDetailFragment.open = false ;
                                info.setText(Html.fromHtml(longstring));
                            } else {
                                DealsDetailFragment.open = true ;
                                info.setText(Html.fromHtml(shortstring));
                            }
                        }

                    });

                } else {

                    info.setText(hMap.get("detail"));

                }

                // fonts

                Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

                title.setTypeface(font);
                bucks.setTypeface(font);
                description.setTypeface(font);
                info.setTypeface(font);

                buttonBuy.setTypeface(font);

            } else { Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show(); }

        }

    }

    // making the purchase

    String buyid ;
    Integer buyid_success ;
    JSONArray dataShopJsonArr = null;

    private class sendBuyAction extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/request.shop.php?user="+Launch.globalUserID+"&shop="+id);

                Log.d("JOJOJO", "VoucherID : "+id);

                dataShopJsonArr = json.getJSONArray("shop");

                JSONObject c = dataShopJsonArr.getJSONObject(0);

                buyid_success = c.getInt("success") ;

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataShopJsonArr != null ) {

                if ( buyid_success == -1 ) {
                    Toast.makeText(getActivity(), "Sorry, your details seem to be incorrect, or you don't have enough bucks.", Toast.LENGTH_SHORT).show();
                } else {

                    // notify the user

                    gaTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Completions")
                            .setAction("Shop")
                            .setLabel("purchase")
                            .build());

                    Toast.makeText(getActivity(), "Congratulations, your voucher is on it's way!", Toast.LENGTH_SHORT).show();

                    // track the deal

                    new TrackUser().execute("type=shop&user="+Launch.globalUserID+"&event="+id) ;

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

        gaTracker.setScreenName("Shop Detail");

    }

}
