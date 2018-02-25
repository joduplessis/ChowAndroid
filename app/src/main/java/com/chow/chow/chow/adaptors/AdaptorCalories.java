package com.chow.chow.chow.adaptors;

import android.graphics.Typeface;
import android.widget.BaseAdapter ;
import android.content.Context ;
import android.view.* ;
import android.widget.TextView;

import com.chow.chow.chow.*;
import com.chow.chow.chow.db.Food;

import java.util.List;

/**
 * Created by joduplessis on 14/10/03.
 */
public class AdaptorCalories extends BaseAdapter {

    Context context;
    List<Food> data;
    public String dist ;

    public AdaptorCalories(Context context, List<Food> data) {

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

            convertView = inflater.inflate(R.layout.list_calories, null);

            Food obj = data.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.meal);
            TextView description = (TextView) convertView.findViewById(R.id.calories);

            title.setText(obj.getMeal()+", "+obj.getCalories());
            description.setText(obj.getTime());

            /* ----------- fonts ----------- */

            Typeface font = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
            Typeface boldfont = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Hv_0.otf");

            title.setTypeface(boldfont);
            description.setTypeface(font);

        }

        return convertView;

    }

}

