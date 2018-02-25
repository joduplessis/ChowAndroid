package com.chow.chow.chow.adaptors;

import android.graphics.Typeface;
import android.util.Log;
import android.widget.BaseAdapter ;
import android.content.Context ;
import android.view.* ;
import android.widget.ImageView;
import android.widget.TextView;

import com.chow.chow.chow.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterMeal extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String,String>> data;

    public AdapterMeal(Context context, ArrayList<HashMap<String,String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_meal, null);

            HashMap<String,String> obj = data.get(position);

            // There is often a blank item added to pad the GridView list
            // This is done because it cuts off sometimes using the ExapandableHeightGridView class
            // A proper workaround needs to be found
            if (obj.get("id")=="") {
                convertView.setVisibility(View.INVISIBLE);
            }

            TextView title = (TextView) convertView.findViewById(R.id.textView);
            TextView description = (TextView) convertView.findViewById(R.id.textView9);
            TextView bucks = (TextView) convertView.findViewById(R.id.textView10);
            TextView bucksText = (TextView) convertView.findViewById(R.id.textView11);
            ImageView image = (ImageView) convertView.findViewById(R.id.imageView9);
            ImageView button = (ImageView) convertView.findViewById(R.id.button);

            // Temp
            bucks.setVisibility(View.INVISIBLE);
            bucksText.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);




            Log.d("JOJOJO", ">> "+ obj.get("title"));


            // Reworking the text
            String shortenedDescription = obj.get("description");
            String shortenedTitle = obj.get("title");

            if (obj.get("title").length() > 78) {
                shortenedTitle = shortenedTitle.substring(0, Math.min(shortenedTitle.length(),70)) + "...";
            }

            if (shortenedDescription.split(" ").length > 9) {
                shortenedDescription = shortenedDescription.substring(0, Math.min(shortenedDescription.length(),35)) + "...";
            } else {
                shortenedDescription = shortenedDescription.substring(0, Math.min(shortenedDescription.length(),65)) + "...";
            }

            title.setText(shortenedTitle);
            description.setText(shortenedDescription);
            bucks.setText(obj.get("bucks"));

            Picasso.with(context).load(Launch.imageURL + "" + obj.get("image")).fit().centerCrop().into(image);

            /* ----------- fonts ----------- */

            Typeface font = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface fontBold = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Bd_0.otf");

            title.setTypeface(fontBold);
            description.setTypeface(font);
            bucks.setTypeface(font);

        }

        return convertView;

    }

}
