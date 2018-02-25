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

public class AdapterCategory extends BaseAdapter {

    Context context;
    ImageView check ;
    ArrayList<HashMap<String,String>> data;

    public AdapterCategory(Context context, ArrayList<HashMap<String,String>> data) {

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
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_category, null);

            HashMap<String,String> obj = data.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.textView);
            TextView description = (TextView) convertView.findViewById(R.id.textView5);
            ImageView image = (ImageView) convertView.findViewById(R.id.imageView5);

            check = (ImageView) convertView.findViewById(R.id.check);

            title.setText(obj.get("title"));
            description.setText(obj.get("description"));

            check.setAlpha(0.0f);

            Picasso.with(context).load( Launch.imageURL + "" + obj.get("image") ).fit().centerCrop().into(image);

            // fonts

            Typeface font = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Lt_0.otf");

            title.setTypeface(font);
            description.setTypeface(font);

        }

        return convertView;

    }

}
