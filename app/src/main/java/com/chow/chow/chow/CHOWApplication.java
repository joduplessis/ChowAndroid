package com.chow.chow.chow;

import android.app.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import java.util.HashMap;

public class CHOWApplication extends Application {

    private static final String PROPERTY_ID = "UA-57785943-2";

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
        ECOMMERCE_TRACKER,
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public CHOWApplication() {
        super();
    }

    synchronized Tracker getTracker(TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.ecommerce_tracker);

            mTrackers.put(trackerId, t);

        }

        return mTrackers.get(trackerId);

    }

}