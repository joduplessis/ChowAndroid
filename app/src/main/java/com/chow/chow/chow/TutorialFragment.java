package com.chow.chow.chow;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class TutorialFragment extends Fragment implements Button.OnClickListener {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private ArrayList<Integer> listOfItems;

    private LinearLayout dotsLayout;
    private int dotsCount;
    private TextView[] dots;

    Button button ;
    TextView helpTextHint ;
    View view ;

    public TutorialFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get the views
        view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        button = (Button) view.findViewById(R.id.button) ;
        helpTextHint = (TextView) view.findViewById(R.id.helpTextHint) ;

        // Setup
        ((Launch) getActivity()).setActionbarTitle("TUTORIAL");
        ((Launch) getActivity()).hideActionbar();
        ((Launch) getActivity()).navbarItemActivate(3) ;

        // Clicks
        button.setOnClickListener(this);

        // Typeface
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface jsfont = Typeface.createFromAsset(getActivity().getAssets(), "JennaSue.ttf");

        // Button clicks
        button.setTypeface(font);
        helpTextHint.setTypeface(jsfont);

        // Init
        initViews();
        setViewPagerItemsWithAdapter();
        setUiPageViewController();

        // Log
        Log.d("JOJOJOJO", Launch.globalUserID+" has been here");

        // Set the last seen user to this user ID
        Launch.globalUserSeenTutorial = String.valueOf(Launch.globalUserID) ;

        // Store the shared preference
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("globalUserSeenTutorial", Launch.globalUserID);
        editor.commit();

        // Return
        return view ;
    }

    public void onClick(View v) {
        // Animations
        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        // Actions
        switch(v.getId()) {
            case R.id.button:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("okayButton")
                        .build());
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FindMapFragment flFragment = new FindMapFragment();
                fragmentTransaction.replace(R.id.fragment_container, flFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ((Launch) getActivity()).navbarItemActivate(1);
                break;
        }

    }

    private void setUiPageViewController() {

        dotsLayout = (LinearLayout)view.findViewById(R.id.viewPagerCountDots);
        dotsCount = myViewPagerAdapter.getCount();
        dots = new TextView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new TextView(getActivity());
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(30);
            dots[i].setTextColor(getResources().getColor(android.R.color.darker_gray));
            dotsLayout.addView(dots[i]);
        }

        dots[0].setTextColor(getResources().getColor(R.color.app_green));

    }

    private void setViewPagerItemsWithAdapter() {
        myViewPagerAdapter = new MyViewPagerAdapter(listOfItems);
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(viewPagerPageChangeListener);
    }

    // page change listener
    OnPageChangeListener viewPagerPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < dotsCount; i++) {
                dots[i].setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
            dots[position].setTextColor(getResources().getColor(R.color.app_green));
            // help text
            switch(position) {
                case 0:
                    helpTextHint.setText("Search and find CHOW accredited restaurants near you");
                    break ;
                case 1:
                    helpTextHint.setText("Select your CHOW meal ");
                    break ;
                case 2:
                    helpTextHint.setText("Add the unique code found on the tillslip - coming soon");
                    break ;
                case 3:
                    helpTextHint.setText("Check the number of CHOW bucks you have earned - coming soon");
                    break ;
                case 4:
                    helpTextHint.setText("Use your CHOW bucks to buy awesome stuff - coming soon");
                    button.setText("I got it, let's start!");
                    break ;
            }

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

    };

    private void initViews() {
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        listOfItems = new ArrayList<Integer>();
        listOfItems.add(1);
        listOfItems.add(2);
        listOfItems.add(3);
        listOfItems.add(4);
        listOfItems.add(5);

    }

    //	adapter
    public class MyViewPagerAdapter extends PagerAdapter{

        private LayoutInflater layoutInflater;
        private ArrayList<Integer> items;

        public MyViewPagerAdapter(ArrayList<Integer> listOfItems) {
            this.items = listOfItems;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {



            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.view_pager_item, container,false);

            ImageView tView = (ImageView)view.findViewById(R.id.TutorialScreen);

            int resID = getResources().getIdentifier("tutorial"+listOfItems.get(position).toString() , "drawable", getActivity().getPackageName());

            tView.setBackgroundResource(resID);

            ((ViewPager) container).addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View)obj);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View)object;
            ((ViewPager) container).removeView(view);
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

        gaTracker.setScreenName("Tutorial");

    }
}
