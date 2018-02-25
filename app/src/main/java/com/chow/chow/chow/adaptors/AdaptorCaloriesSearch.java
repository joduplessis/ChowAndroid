package com.chow.chow.chow.adaptors;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chow.chow.chow.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joduplessis on 14/10/03.
 */

public class AdaptorCaloriesSearch extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String,String>> data;

    public AdaptorCaloriesSearch(Context context, ArrayList<HashMap<String,String>> data) {

        this.context = context;
        this.data = data;

    }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_calories_search, null);

            HashMap<String,String> obj = data.get(position);

            String content = obj.get("html") ;
            String[] parts = content.split("-");
            String part1 = parts[0]; // 004
            String part2 = parts[1]; // 034556

            TextView title = (TextView) convertView.findViewById(R.id.meal);
            TextView description = (TextView) convertView.findViewById(R.id.calories);

            title.setText(part1);
            description.setText(part2);
            /* ----------- fonts ----------- */

            Typeface font = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface boldfont = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Hv_0.otf");

            title.setTypeface(boldfont);
            description.setTypeface(font);

        }

        return convertView;

    }

}

