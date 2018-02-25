package com.chow.chow.chow.utility;

import org.json.JSONObject;

import android.text.Html;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class JsonParser {

    static JSONObject jObj = null;

    OkHttpClient client = new OkHttpClient();

    public JSONObject getJSONFromUrl(String url) {

        try {

            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            String returnText = response.body().string();
            String name = new String(returnText.getBytes("ISO-8859-1"), "UTF-8");
            String decodedName = Html.fromHtml(name).toString();

            jObj = new JSONObject( decodedName );

        } catch (Exception e) {

            Log.e("JSON_OKHTTP ", " :: " + e.toString());

        }

        return jObj;

    }
}
