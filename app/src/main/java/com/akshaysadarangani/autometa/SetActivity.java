package com.akshaysadarangani.autometa;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.Set;

public class SetActivity extends AppCompatActivity {

    final int RESULT_PICK_CONTACT = 1001;
    final int PLACE_PICKER_REQUEST = 1;
    TextView tvContactNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
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
                Intent myIntent = new Intent(SetActivity.this, DoneActivity.class);
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
                        //contactName = cursor.getString(nameIndex);
                        tvContactNumber.setText(contactNumber);
                        cursor.close();
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
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }
}
