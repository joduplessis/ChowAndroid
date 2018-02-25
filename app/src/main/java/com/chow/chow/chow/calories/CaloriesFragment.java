package com.chow.chow.chow.calories;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton ;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;

import com.chow.chow.chow.Launch;
import com.chow.chow.chow.R;
import com.chow.chow.chow.adaptors.AdaptorCalories;
import com.chow.chow.chow.db.DatabaseHandler;
import com.chow.chow.chow.db.Food;

import java.util.List;

public class CaloriesFragment extends Fragment implements Button.OnClickListener {

    ImageButton mButton;
    View view ;
    Button searchButton ;
    EditText searchQuery ;

    public CaloriesFragment() {}

    private FragmentActivity myContext;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    public void addMeal(String m, String c) {

        String meal = m ;
        String calories = c ;

        DatabaseHandler db = new DatabaseHandler(getActivity());

        db.insertRow(meal, calories);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((Launch) getActivity()).setActionbarTitle("CALORIES");
        ((Launch) getActivity()).showActionbar();

        // db stuff

        DatabaseHandler db = new DatabaseHandler(getActivity());
        List<Food> food = db.getAllFood();

        view = inflater.inflate(R.layout.fragment_calories, container, false);

        ListView listview = (ListView) view.findViewById(R.id.listView);
        listview.setAdapter(new AdaptorCalories(getActivity(), food));
        Integer getallcalories = 0 ;

        for(int i=0; i < food.size(); i++) {

            Food obj = food.get(i);

            String cs = obj.getCalories().replaceAll("!", "") ;

            if ( cs.equals("") ) cs = "0" ;

            Integer csi = Integer.parseInt(cs) ;



            getallcalories +=  csi ;

        }

        // ui stuff

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface boldfont = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Hv_0.otf");

        TextView caloriesfield = (TextView) view.findViewById(R.id.textView);
        TextView todayfield = (TextView) view.findViewById(R.id.textView6);
        TextView descriptionfield = (TextView) view.findViewById(R.id.textView2);
        Button button = (Button) view.findViewById(R.id.button);
        searchButton = (Button) view.findViewById(R.id.buttonSearch);
        searchQuery = (EditText) view.findViewById(R.id.editText2);

        searchButton.setOnClickListener(this);

        caloriesfield.setTypeface(font);
        descriptionfield.setTypeface(font);
        todayfield.setTypeface(font);
        button.setTypeface(font);
        searchQuery.setTypeface(font);

        // sets all the calories

        caloriesfield.setText( getallcalories.toString() );

        // button for the calories

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
                aAnim.setDuration(250);
                v.startAnimation(aAnim);

                // get description

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final EditText meal = new EditText(getActivity());
                meal.setHint("Add meal description");
                alert.setView(meal);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // get calories

                        AlertDialog.Builder calorieAlert = new AlertDialog.Builder(getActivity());
                        final EditText calories = new EditText(getActivity());
                        calorieAlert.setView(calories);
                        calories.setHint("Add meal calories");
                        calorieAlert.setView(calories);

                        calorieAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                addMeal(meal.getText().toString(), calories.getText().toString()) ;

                            }
                        });

                        calorieAlert.show();

                    }
                });

                alert.show();

            }
        });

        return view;

    }

    public void onClick(View v) {

        v.setAlpha(0.5f);

        FragmentManager fragmentManager = myContext.getSupportFragmentManager() ;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(v.getId()) {

            case R.id.buttonSearch:

                CaloriesSearchFragment csFragment = new CaloriesSearchFragment();

                csFragment.query = searchQuery.getText().toString() ;

                // fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_left);
                fragmentTransaction.replace(R.id.fragment_container, csFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;
        }

    }

}
