package com.chow.chow.chow.adaptors;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chow.chow.chow.FindMapFragment;
import com.chow.chow.chow.Launch;
import com.chow.chow.chow.R;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AdapterMap implements InfoWindowAdapter {

    LayoutInflater inflater = null;
    Button button ;
    View view ;
    Context context ;
    Boolean not_first_time_showing_info_window = false;

    public AdapterMap(LayoutInflater inflater) {
        this.inflater = inflater;
        context = inflater.getContext() ;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        view = inflater.inflate(R.layout.map_popup, null);
        HashMap<String,String> obj ;
        String thumbnail = "";
        TextView title = (TextView) view.findViewById(R.id.textView6);
        TextView subtitle = (TextView) view.findViewById(R.id.textView7);
        ImageView image = (ImageView) view.findViewById(R.id.imageView7);
        Typeface font = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface fontBold = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Bd_0.otf");
        title.setTypeface(fontBold);
        subtitle.setTypeface(font);
        for(int i=0; i < FindMapFragment.mapsHolder.size(); i++) {
            obj = FindMapFragment.mapsHolder.get(i);
            if ( obj.get("title").toString().equals( marker.getTitle().toString() ) ) {
                String upToNCharacters = "";
                Log.d("JOJOJO", "Map adaptor spanning "+title.getLineCount()+" lines") ;
                if (obj.get("title").split(" ").length > 2 || obj.get("title").split(" ")[0].length() > 10) {
                    upToNCharacters = obj.get("description").substring(0, Math.min(obj.get("description").length(),50)) + "...";
                } else {
                    upToNCharacters = obj.get("description").substring(0, Math.min(obj.get("description").length(),75)) + "...";
                }
                title.setText( obj.get("title") );
                subtitle.setText( upToNCharacters );
                thumbnail = Launch.imageURL + "" + obj.get("image") ;
            }
        }

        Log.d("JOJOJO", "AdaptorMap: "+not_first_time_showing_info_window.toString()) ;
        if (not_first_time_showing_info_window) {
            not_first_time_showing_info_window = false;
            Picasso.with(context).load(thumbnail).resize(60,50).centerCrop().into(image);
        } else {
            not_first_time_showing_info_window = true;
            Picasso.with(context).load(thumbnail).resize(60,50).centerCrop().into(image, new InfoWindowRefresher(marker));
        }
        return view;

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null ;
    }

    // To refresh the google infowindow
    private class InfoWindowRefresher implements Callback {
        private Marker markerToRefresh;
        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }
        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }
        @Override
        public void onError() {}
    }



}
