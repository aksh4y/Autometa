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
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;

import me.itangqi.waveloadingview.WaveLoadingView;

public class SetActivity extends AppCompatActivity {

    final int RESULT_PICK_CONTACT = 1001;
    final int PLACE_PICKER_REQUEST = 1;
    int progress = 1;
    TextView tvContactNumber;

    ConstraintLayout mLayout;
    AnimationDrawable animationDrawable;
    WaveLoadingView wlv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        wlv = findViewById(R.id.wlv);

        wlv.setProgressValue(progress);
        wlv.startAnimation();

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

        if (Build.VERSION.SDK_INT >= 21)
            changeStatusBarColor();

        Spinner spinner = findViewById(R.id.spinner);
        Spinner digits = findViewById(R.id.spinner3);
        Spinner units = findViewById(R.id.spinner4);
        final EditText reminderDesc = findViewById(R.id.reminderDesc);
        Button goButton = findViewById(R.id.go);
        final Button contact = findViewById(R.id.contact);
        Button btn_location = findViewById(R.id.btn_location);
        tvContactNumber = findViewById(R.id.numberView);
        spinner.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> eventType = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.tasks));
        spinner.setAdapter(eventType);

        digits.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> digitArray = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.digits));
        digits.setAdapter(digitArray);

        units.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> unitArray = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.units));
        units.setAdapter(unitArray);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SetActivity.this, TriggersActivity.class);
                SetActivity.this.startActivity(myIntent);
            }
        });

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(SetActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
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
                        //String contactName = null;
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
                        //contactName = cursor.getString(nameIndex);
                        tvContactNumber.setText(contactNumber);
                        cursor.close();
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
                //place.getLatLng().latitude
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
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
}
