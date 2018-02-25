package com.chow.chow.chow.utility;

import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class TrackUser extends AsyncTask<String, Void, Void> {

    @Override
    protected void onPreExecute(){

    }

    @Override
    protected Void doInBackground(String... params) {

        try {

            OkHttpClient client = new OkHttpClient();

            String url = "API_PATH/app/api/track.php?"+params[0] ;

            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();


        } catch (Exception e) { e.printStackTrace(); }

        return null;

    }

    protected void onProgressUpdate(Void args){

    }

    @Override
    protected void onPostExecute(Void args) {

    }

}