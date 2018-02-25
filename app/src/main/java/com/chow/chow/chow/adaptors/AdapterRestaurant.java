package com.chow.chow.chow.adaptors;

import android.graphics.Typeface;
import android.widget.BaseAdapter ;
import android.content.Context ;
import android.view.* ;
import android.widget.ImageView;
import android.widget.TextView;

import com.chow.chow.chow.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterRestaurant extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String,String>> data;
    public String dist ;

    public AdapterRestaurant(Context context, ArrayList<HashMap<String,String>> data) {

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

            convertView = inflater.inflate(R.layout.list_restaurant, null);

            HashMap<String,String> obj = data.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.textView);
            TextView description = (TextView) convertView.findViewById(R.id.textView2);
            TextView distance = (TextView) convertView.findViewById(R.id.textView3);
            ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);

            String upToNCharacters = obj.get("description").substring(0, Math.min(obj.get("description").length(),65)) + "...";

            title.setText(obj.get("title"));
            description.setText(upToNCharacters);
            distance.setText(obj.get("distance"));

            Picasso.with(context).load(Launch.imageURL + "" + obj.get("image")).fit().centerCrop().into(thumbnail);

            /* ----------- fonts ----------- */

            Typeface font = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface fontBold = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Bd_0.otf");

            title.setTypeface(fontBold);
            description.setTypeface(font);
            distance.setTypeface(font);

        }

        return convertView;

    }

}


