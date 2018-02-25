package com.chow.chow.chow;



import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager ;
import android.support.v4.app.FragmentTransaction ;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


import com.chow.chow.chow.utility.*;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ProfileEditFragment extends Fragment implements Button.OnClickListener {

    // Code for our image picker select action.
    private static final int IMAGE_PICKER_SELECT = 1;

    // Reference to our image view we will use
    ImageView mSelectedImage;
    View view ;
    Button update ;
    Button profilepic ;
    Button login ;
    Button dateofbirthButton ;
    TextView radiogroupHeading ;
    TextView genderHeading;
    EditText username ;
    EditText email ;
    EditText password ;
    EditText passwordconfirm ;
    EditText textFirstname ;
    EditText textLastname;
    EditText dateofbirth;
    EditText medicalconditions ;
    RadioButton radioButton0;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;
    RadioButton radioButton4;
    RadioButton radioButton5;
    RadioGroup rg ;
    ImageView mask;
    ImageView imageview2;
    String selectedCondition ;
    RadioButton radioButtonMale;
    RadioButton radioButtonFemale;
    RadioGroup radioGroupMF ;

    private FragmentActivity myContext;
    private String image;
    private UiLifecycleHelper uiHelper;

    public ProfileEditFragment() {}

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // general setup
        ((Launch) getActivity()).setActionbarTitle("PROFILE EDIT");
        ((Launch) getActivity()).hideActionbar();
        ((Launch) getActivity()).navbarItemActivate(3) ;

        view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        username = (EditText) view.findViewById(R.id.username);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        passwordconfirm = (EditText) view.findViewById(R.id.passwordconfirm);
        dateofbirthButton = (Button) view.findViewById(R.id.dateofbirthButton);
        update = (Button) view.findViewById(R.id.buttonUpdate);
        profilepic = (Button) view.findViewById(R.id.profilepicbutton) ;
        login = (Button) view.findViewById(R.id.buttonLogin);
        radiogroupHeading = (TextView) view.findViewById(R.id.radiogroupHeading);
        genderHeading = (TextView) view.findViewById(R.id.genderHeading);
        textFirstname = (EditText) view.findViewById(R.id.textFirstname);
        textLastname = (EditText) view.findViewById(R.id.textLastname);
        dateofbirth = (EditText) view.findViewById(R.id.dateofbirth);
        medicalconditions = (EditText) view.findViewById(R.id.medicalconditions);

        // radio group for the diseases
        radioButton0 = (RadioButton) view.findViewById(R.id.radioButton0);
        radioButton1 = (RadioButton) view.findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) view.findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) view.findViewById(R.id.radioButton3);
        radioButton4 = (RadioButton) view.findViewById(R.id.radioButton4);
        radioButton5 = (RadioButton) view.findViewById(R.id.radioButton5);
        rg = (RadioGroup) view.findViewById(R.id.radiogroup);
        rg.clearCheck();

        // radio group for the males and females
        radioButtonMale = (RadioButton) view.findViewById(R.id.radioButtonMale);
        radioButtonFemale = (RadioButton) view.findViewById(R.id.radioButtonFemale);
        radioGroupMF = (RadioGroup) view.findViewById(R.id.radiogroupMF);
        radioGroupMF.clearCheck();

        // Profile pic
        mask = (ImageView) view.findViewById(R.id.mask);
        imageview2 = (ImageView) view.findViewById(R.id.imageView2);

        // fonts
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt_0.otf");
        Typeface fontBold = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Bd_0.otf");

        // Set type faces
        update.setTypeface(font);
        profilepic.setTypeface(font);
        login.setTypeface(font);
        username.setTypeface(font);
        dateofbirthButton.setTypeface(font);
        email.setTypeface(font);
        password.setTypeface(font);
        passwordconfirm.setTypeface(font);
        textFirstname.setTypeface(fontBold);
        textLastname.setTypeface(fontBold);
        dateofbirth.setTypeface(font);
        medicalconditions.setTypeface(font);
        genderHeading.setTypeface(fontBold);
        radiogroupHeading.setTypeface(fontBold);
        radioButton0.setTypeface(font);
        radioButton1.setTypeface(font);
        radioButton2.setTypeface(font);
        radioButton3.setTypeface(font);
        radioButton4.setTypeface(font);
        radioButton5.setTypeface(font);
        radioButtonMale.setTypeface(font);
        radioButtonFemale.setTypeface(font);

        // Click listeners
        radioButton0.setOnClickListener(this);
        radioButton1.setOnClickListener(this);
        radioButton2.setOnClickListener(this);
        radioButtonMale.setOnClickListener(this);
        radioButtonFemale.setOnClickListener(this);
        update.setOnClickListener(this);
        profilepic.setOnClickListener(this);
        login.setOnClickListener(this);
        dateofbirthButton.setOnClickListener(this);

        // set the image with picasso
        if ( Launch.globalUserImage.equals("") ) {
            Launch.globalUserImage = "API_PATH/app/assets/profile_demo.jpg";
        }

        // Use Picasso
        Picasso.with(getActivity())
                .load(Launch.globalUserImage)
                .resize(150,150)
                .centerInside()
                .into(imageview2);

        // do the conditions
        selectedCondition = "" ;

        // we do a try here so that if there is no internet it doesn't crash
        try {
            // set text
            username.setText( Launch.globalUserName ) ;
            password.setText( Launch.globalUserPassword ) ;
            passwordconfirm.setText( Launch.globalUserPassword ) ;
            email.setText( Launch.globalUserEmail ) ;
            textFirstname.setText( Launch.globalFirstName ) ;
            textLastname.setText( Launch.globalLastName ) ;

            // set date of birth
            if ( Launch.globalDOB.equals("null") )
                Launch.globalDOB = "";

            dateofbirth.setText( Launch.globalDOB ) ;

            // set the radio buttons
            if (Launch.globalCondition.equals("None")) {
                radioButton0.setChecked(true);
            } else if (Launch.globalCondition.equals("Diabetes")) {
                radioButton1.setChecked(true);
            } else if (Launch.globalCondition.equals("Heart Disease")) {
                radioButton2.setChecked(true);
            } else if (Launch.globalCondition.equals("High Cholesterol")) {
                radioButton3.setChecked(true);
            } else if (Launch.globalCondition.equals("High Blood Pressure")) {
                radioButton4.setChecked(true);
            } else {
                radioButton5.setChecked(true);
                medicalconditions.setText(Launch.globalCondition);
            }

            // set the gender radio button
            if (Launch.globalGender.equals("Male")) {
                radioButtonMale.setChecked(true);
            } else if (Launch.globalGender.equals("Female")) {
                radioButtonFemale.setChecked(true);
            }

            // hide the password & username fields if it's a FB login
            if (Launch.globalUserLoggedInFB) {
                password.setVisibility(View.GONE);
                passwordconfirm.setVisibility(View.GONE);
                username.setVisibility(View.GONE);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return view ;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("JOJOJO", "Here");

        // Handle the request
        if (requestCode == IMAGE_PICKER_SELECT) {
            if (resultCode == Activity.RESULT_OK) {
                // store the image and get the path
                Uri selectedImageUri = data.getData();
                File sd = Environment.getExternalStorageDirectory();
                File ddata = Environment.getDataDirectory();
                String selectedPath1 = getPath(selectedImageUri);

                // save the path to the local file
                Launch.globalUserImage = "file://" + selectedPath1;

                // Use Picasso
                Picasso.with(getActivity())
                        .load(Launch.globalUserImage)
                        .resize(150,150)
                        .centerInside()
                        .into(imageview2);
            }
        }
    }

    public void onClick(View v) {
        // Animation for the button
        Animation aAnim = new AlphaAnimation(0.5f, 1.0f);
        aAnim.setDuration(250);
        v.startAnimation(aAnim);

        // Button actions
        switch(v.getId()) {
            case R.id.buttonLogin:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("loginButton")
                        .build());
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ProfileFragment laFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.fragment_container, laFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.buttonUpdate:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("updateButton")
                        .build());
                new UploadJSON().execute();
                break;
            case R.id.profilepicbutton:
                gaTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Click")
                        .setLabel("profilePicButton")
                        .build());
                // Start the activity
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, IMAGE_PICKER_SELECT);
                break;
            case R.id.radioButtonMale:
                Launch.globalGender = "Male" ;
                break ;
            case R.id.radioButtonFemale:
                Launch.globalGender = "Female" ;
                break ;
            case R.id.dateofbirthButton:
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                };
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break ;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    Calendar myCalendar = Calendar.getInstance();

    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateofbirth.setText(sdf.format(myCalendar.getTime()));
    }

    JSONArray dataJsonArr = null;
    Integer success ;
    String gender = "";
    String URL = "";

    // set the user details
    private class UploadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // medical conditions
                if ( radioButton0.isChecked() )         selectedCondition = "None" ;
                if ( radioButton1.isChecked() )         selectedCondition = "Diabetes" ;
                if ( radioButton2.isChecked() )         selectedCondition = "Heart Disease" ;
                if ( radioButton3.isChecked() )         selectedCondition = "High Cholesterol" ;
                if ( radioButton4.isChecked() )         selectedCondition = "High Blood Pressure" ;
                if ( radioButton5.isChecked() )         selectedCondition = "" ;
                if ( selectedCondition.equals("") )      selectedCondition = medicalconditions.getText().toString().replaceAll("\\n", " ") ;

                // gender
                if ( radioButtonMale.isChecked() )      gender = "Male" ;
                if ( radioButtonFemale.isChecked() )    gender = "Female" ;

                // carry on
                JsonParser jParser = new JsonParser();
                URL = "API_PATH/app/api/edit.user.php" ;
                URL+= "?id="+Launch.globalUserID ;
                URL+= "&name="+username.getText().toString() ;
                URL+= "&password="+password.getText().toString() ;
                URL+= "&email="+email.getText().toString() ;
                URL+= "&bucks="+Launch.globalUserBucks ;
                URL+= "&firstname="+textFirstname.getText().toString() ;
                URL+= "&lastname="+textLastname.getText().toString() ;
                URL+= "&condition="+selectedCondition ;
                URL+= "&dob="+dateofbirth.getText().toString() ;
                URL+= "&gender="+gender ;
                URL+= "&image="+Launch.globalUserImage ;

                // format the URL nicely
                URL = URL.replace(" ", "%20");
                JSONObject json = jParser.getJSONFromUrl(URL);
                dataJsonArr = json.getJSONArray("user");
                JSONObject c = dataJsonArr.getJSONObject(0);
                success = c.getInt("success") ;
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            if (dataJsonArr!=null) {

                if ( success == -1) {
                    Toast.makeText(getActivity(), "Sorry, there seems to have been an error.", Toast.LENGTH_SHORT).show();
                } else {
                    gaTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Completions")
                            .setAction("Account")
                            .setLabel("accountUpdated")
                            .build());
                    FragmentManager fragmentManager = myContext.getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    ProfileFragment pFragment = new ProfileFragment();
                    fragmentTransaction.replace(R.id.fragment_container, pFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    Toast.makeText(getActivity(), "Account updated.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "Sorry, something seems to have gone wrong. ", Toast.LENGTH_SHORT).show();
            }

        }

    }

    // Build and send an Event for GA

    Tracker gaTracker ;

    @Override
    public void onStart() {
        super.onStart();

        // set up the tracker
        gaTracker = ((CHOWApplication) getActivity().getApplication()).getTracker(CHOWApplication.TrackerName.APP_TRACKER);

        // set the screen name
        gaTracker.setScreenName("Profile Edit");

    }

}
