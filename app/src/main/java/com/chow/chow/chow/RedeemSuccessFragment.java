package com.chow.chow.chow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chow.chow.chow.utility.*;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class RedeemSuccessFragment extends Fragment implements Button.OnClickListener {

    Button redeemButton ;
    ImageButton shareButtonFacebook ;
    ImageButton shareButtonTwitter ;

    public String bucks ;
    public String calories ;
    public String meal ;
    public String restaurant ;

    private UiLifecycleHelper uiHelper;

    public RedeemSuccessFragment() {}

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("REDEEM SUCCESS");
        ((Launch) getActivity()).showActionbar();
        ((Launch) getActivity()).navbarItemActivate(2) ;

        View view = inflater.inflate(R.layout.fragment_redeem_success, container, false);

        uiHelper = new UiLifecycleHelper(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);

        redeemButton = (Button) view.findViewById(R.id.buttonSpend);
        shareButtonFacebook = (ImageButton) view.findViewById(R.id.buttonShareFacebook);
        shareButtonTwitter = (ImageButton) view.findViewById(R.id.buttonShareTwitter);

        redeemButton.setOnClickListener(this);
        shareButtonFacebook.setOnClickListener(this);
        shareButtonTwitter.setOnClickListener(this);

        // fonts

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface boldfont = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Hv_0.otf");

        TextView heading = (TextView) view.findViewById(R.id.textView2);
        TextView subheading = (TextView) view.findViewById(R.id.textView3);
        TextView bucks = (TextView) view.findViewById(R.id.textView);
        TextView subbucks = (TextView) view.findViewById(R.id.textViewSub);
        TextView description = (TextView) view.findViewById(R.id.textView6);
        TextView tooltip = (TextView) view.findViewById(R.id.textView6);

        bucks.setText( this.bucks );

        heading.setTypeface(font);
        subheading.setTypeface(font);
        bucks.setTypeface(boldfont);
        subbucks.setTypeface(font);
        description.setTypeface(font);
        tooltip.setTypeface(font);

        redeemButton.setTypeface(font);

        new sendRedeemAction().execute() ;

        return view ;
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {


        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            // TODO Auto-generated method stub

        }
    };

    public void onClick(View v) {

        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        FragmentManager fragmentManager = myContext.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {

            case R.id.buttonSpend:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("spendButton")
                        .build());

                ((Launch) getActivity()).navbarItemActivate(4);
                DealsFragment fragment = new DealsFragment();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

            case R.id.buttonShareFacebook:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("facebookShareButton")
                        .build());

                FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
                        .setLink("API_PATH")
                        .setDescription("I just earned " + bucks + " CHOW bucks for eating at " + restaurant + "! #chowapp")
                        .setName("CHOW")
                        .setCaption("I just earned " + bucks + " CHOW bucks for eating at " + restaurant + "! #chowapp")
                        .build();
                uiHelper.trackPendingDialogCall(shareDialog.present());

                break;

            case R.id.buttonShareTwitter:

                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("otherShareButton")
                        .build());

                // social media sharing

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "CHOW");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I just earned " + bucks + " CHOW bucks for eating at " + restaurant + "! #chowapp");

                startActivity(Intent.createChooser(sharingIntent, "Share via "));

                break;

        }

    }

    JSONArray dataRedeemJsonArr = null;
    Integer redeem_success ;

    private class sendRedeemAction extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                JsonParser jParser = new JsonParser();

                JSONObject json = jParser.getJSONFromUrl("API_PATH/app/api/edit.user.bucks.php?id="+Launch.globalUserID+"&bucks="+bucks);

                dataRedeemJsonArr = json.getJSONArray("redeem");

                JSONObject c = dataRedeemJsonArr.getJSONObject(0);

                redeem_success = c.getInt("success") ;

                // set the timestamp of the redemption

                Context context = getActivity();
                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                // current time + 3 hours

                long unixTime = System.currentTimeMillis() / 1000L;
                long threeHours = 60 * 60 * 3 + unixTime ;

                // add thee shared preference

                editor.putString("redeem_timeout", threeHours+"");
                editor.commit();

                // log

                Log.d("JOJOJO", threeHours + "") ;

            } catch (Exception e) { e.printStackTrace(); }

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            if ( dataRedeemJsonArr != null ) {

                if ( redeem_success == -1 ) {

                    Toast.makeText(getActivity(), "Sorry, something has gone wrong.", Toast.LENGTH_SHORT).show();

                } else {

                    gaTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Completions")
                            .setAction("Code")
                            .setLabel("Redeemed")
                            .build());

                    Toast.makeText(getActivity(), "Successfully redeemed a voucher for " + meal, Toast.LENGTH_SHORT).show();

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

        gaTracker.setScreenName("Redeem Success");

    }

}
