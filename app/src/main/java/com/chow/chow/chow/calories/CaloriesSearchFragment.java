package com.chow.chow.chow.calories;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.chow.chow.chow.R;
import com.chow.chow.chow.adaptors.AdaptorCaloriesSearch;
import com.chow.chow.chow.db.DatabaseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

public class CaloriesSearchFragment extends Fragment {

    View view ;
    String query = "air";
    ListView listview ;

    public CaloriesSearchFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_calories_search, container, false);

        listview = (ListView) view.findViewById(R.id.listView);
        //listview.setAdapter(new AdaptorCalories(getActivity(), food));

        new ParseURL().execute(new String[]{"https://www.fatsecret.com/calories-nutrition/search?q="+query+""});

        return view;
    }

    public void addMeal(String m, String c) {

        String meal = m ;
        String calories = c ;

        DatabaseHandler db = new DatabaseHandler(getActivity());

        db.insertRow(meal, calories);

    }


    ArrayList<HashMap<String,String>> maps ;

    private class ParseURL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            StringBuffer buffer = new StringBuffer();

            try {

                Document doc  = Jsoup.connect(strings[0]).get();

                Elements topicList = doc.select("table.generic.searchResult tr td");

                maps = new ArrayList<HashMap<String,String>>();

                for (Element topic : topicList) {

                    HashMap<String,String> hMap = new HashMap<String,String>();

                    String data = topic.text();

                    hMap.put("html", data);

                    maps.add(hMap);

                    Log.d("JOJOJO",data);

                }



            } catch(Throwable t) { t.printStackTrace(); }

            return buffer.toString();
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);



            if ( maps != null ) {

                listview.setAdapter(new AdaptorCaloriesSearch(getActivity(), maps));
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                        HashMap<String,String> obj = maps.get(position);

                        String content = obj.get("html") ;
                        String[] parts = content.split("-");
                        String part1 = parts[1];
                        String[] parts1 = part1.split("kcal");
                        String part2 = parts1[0];
                        String[] parts2 = part2.split(" ");
                        String part3 = parts2[2];

                        addMeal(parts[0], part3) ;

                        Toast.makeText(getActivity(), "Successfully added!", Toast.LENGTH_SHORT).show();

                    }

                });

            } else { Toast.makeText(getActivity(), "Sorry, there has been an error.", Toast.LENGTH_SHORT).show(); }

        }

    }

}
