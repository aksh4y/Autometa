package com.akshaysadarangani.autometa;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import me.itangqi.waveloadingview.WaveLoadingView;

public class SetActivity extends AppCompatActivity implements DialogActivity.DialogActivityListener {

    final int RESULT_PICK_CONTACT = 1001;
    final int PLACE_PICKER_REQUEST = 1;
    int progress = 1;
    TextView tvContactNumber;
    LatLng location;
    ConstraintLayout mLayout;
    AnimationDrawable animationDrawable;
    WaveLoadingView wlv;
    Spinner task, distance, units;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Snackbar snackbar;
    View parentLayout;
    String userID;
    String contactName;
    String userName;
    String placeName;
    int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("uid");

        wlv = findViewById(R.id.wlv);
        wlv.setProgressValue(progress);
        wlv.startAnimation();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("tasks");

        mLayout = findViewById(R.id.mLayout);
        animationDrawable = (AnimationDrawable) mLayout.getBackground();
        animationDrawable.setEnterFadeDuration(4500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();

        Explode explode = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.explode);

            /*explode = new Explode();
            getWindow().setExitTransition(explode);*/
            getWindow().setEnterTransition(transition);
            getWindow().setAllowEnterTransitionOverlap(false);
        }

        location = null;

        if (Build.VERSION.SDK_INT >= 21)
            changeStatusBarColor();

        task = findViewById(R.id.spinner);
        distance = findViewById(R.id.spinner3);
        units = findViewById(R.id.spinner4);
        //perimeter = findViewById(R.id.spinner5);
        final EditText reminderDesc = findViewById(R.id.reminderDesc);
        Button goButton = findViewById(R.id.go);
        final Button contact = findViewById(R.id.contact);
        Button btn_location = findViewById(R.id.btn_location);
        tvContactNumber = findViewById(R.id.numberView);
        task.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> eventType = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.tasks));
        task.setAdapter(eventType);

        distance.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> digitArray = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.digits));
        distance.setAdapter(digitArray);

        units.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> unitArray = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.units));
        units.setAdapter(unitArray);
        units.setEnabled(false);

        /*perimeter.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> perimeterArray = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.perimeter));
        perimeter.setAdapter(perimeterArray);*/

        // Welcome Snackbar
        parentLayout = findViewById(android.R.id.content);
        snackbar = Snackbar.make(parentLayout, "Welcome back " + userName + "!", Snackbar.LENGTH_LONG);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#ff4081"));
        snackbar.show();


        task.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position != 0)
                    progress += 10;
                if(position == 1) {
                    reminderDesc.setVisibility(View.VISIBLE);
                    contact.setVisibility(View.GONE);
                    tvContactNumber.setVisibility(View.GONE);

                }
                else {
                    reminderDesc.setVisibility(View.GONE);
                    contact.setVisibility(View.VISIBLE);
                    tvContactNumber.setVisibility(View.VISIBLE);
                }
                wlv.setProgressValue(progress);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }

        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent;
                switch (task.getSelectedItemPosition()) {
                    case 2:
                        contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        break;
                    case 3:
                        contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                        break;
                    default: return;
                }
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = null;
                switch(task.getSelectedItemPosition()) {
                    case 1:
                        if(reminderDesc.getText().toString().isEmpty())
                            return;
                        type = "REMINDER";
                        break;
                    case 2:
                        if(tvContactNumber.getText().toString().isEmpty())
                            return;
                        reminderDesc.setText("Send SMS to " + contactName);
                        type = "SMS";
                        break;
                    case 3:
                        if(tvContactNumber.getText().toString().isEmpty())
                            return;
                        reminderDesc.setText("Send email to " + contactName);
                        type = "EMAIL";
                        break;
                    default: return;
                }

                if(distance.getSelectedItemPosition() == 0 || units.getSelectedItemPosition() == 0 || location == null)
                    return;

                // Write to the database
                String rID = myRef.push().getKey();
                if(rID == null) {
                    snackbar = Snackbar.make(parentLayout, "An error has occurred. Try again later.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else {
                    Reminder reminder = new Reminder(rID, userID, userName, type, reminderDesc.getText().toString(), tvContactNumber.getText().toString(), tvContactNumber.getText().toString(), Integer.parseInt(distance.getSelectedItem().toString()), units.getSelectedItem().toString(), location, placeName, false);
                    myRef.child(rID).setValue(reminder);
                }
                finish();
            }
        });

        distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0)
                    units.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        units.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (distance.getSelectedItemPosition()) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        if(position == 1) {
                            units.setSelection(0);
                            snackbar = Snackbar.make(parentLayout, "Radius too small.", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        break;
                    case 8:
                    case 9:
                    case 10:
                        if(position == 2) {
                            units.setSelection(0);
                            snackbar = Snackbar.make(parentLayout, "Radius too big", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                }

                if(position != 0)
                    progress += 10;
                wlv.setProgressValue(progress);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }

        });

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(distance.getSelectedItemPosition() == 0 || units.getSelectedItemPosition() == 0) {
                    snackbar = Snackbar.make(parentLayout, "Select distance and unit first", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }
                if(units.getSelectedItemPosition() == 1)
                    radius = Integer.parseInt(distance.getSelectedItem().toString());
                else
                    radius = Integer.parseInt(distance.getSelectedItem().toString()) * 1000;
                DialogActivity dialogActivity = new DialogActivity();
                dialogActivity.show(getSupportFragmentManager(), "Pick your location");
                /*PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(SetActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    try {
                        String contactNumber = null;
                        String contactEmail = null;
                        contactName = null;
                        // getData() method will have the
                        // Content Uri of the selected contact
                        Uri uri = data.getData();
                        //Query the content uri
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        if(cursor == null)
                            return;
                        cursor.moveToFirst();
                        // column index of the phone number
                        int phoneIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER);
                        // column index of the contact name
                        int nameIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        contactNumber = cursor.getString(phoneIndex);
                        int emailIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Email.ADDRESS);
                        contactEmail = cursor.getString(emailIndex);
                        contactName = cursor.getString(nameIndex);
                        if(task.getSelectedItemPosition() == 2 && contactNumber == null) {
                            snackbar = Snackbar.make(parentLayout, "Could not pick contact number. Check the contact.", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }
                        else
                            tvContactNumber.setText(contactNumber);
                        if(task.getSelectedItemPosition() == 3 && contactEmail == null) {
                            snackbar = Snackbar.make(parentLayout, "Could not pick contact email. Check the contact.", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }
                        else
                            tvContactNumber.setText(contactEmail);
                        cursor.close();
                        snackbar = Snackbar.make(parentLayout, "Contact: " + contactName, Snackbar.LENGTH_LONG);
                        snackbar.show();
                        progress += 25;
                        wlv.setProgressValue(progress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                location = place.getLatLng();
                snackbar = Snackbar.make(parentLayout, toastMsg, Snackbar.LENGTH_LONG);
                snackbar.show();
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                progress += 25;
                wlv.setProgressValue(progress);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

    }

    @Override
    public void setLocation(SudoPlace loc) {
        location = loc.getLatLng();
        placeName= loc.getName();
        String toastMsg = String.format("Place: %s", loc.getName());
        snackbar = Snackbar.make(parentLayout, toastMsg, Snackbar.LENGTH_LONG);
        snackbar.show();
        //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
        progress += 25;
        wlv.setProgressValue(progress);
    }
}
