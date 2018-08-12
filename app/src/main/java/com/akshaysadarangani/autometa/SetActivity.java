package com.akshaysadarangani.autometa;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.akshaysadarangani.autometa.receivers.GeofenceBroadcastReceiver;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import me.itangqi.waveloadingview.WaveLoadingView;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;


public class SetActivity extends AppCompatActivity implements DialogActivity.DialogActivityListener, OnCompleteListener<Void> {

    final int RESULT_PICK_CONTACT = 1001;
    final int PLACE_PICKER_REQUEST = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    int progress = 1;
    TextView tvContactNumber;
    LatLng location;
    ConstraintLayout mLayout;
    AnimationDrawable animationDrawable;
    WaveLoadingView wlv;
    Spinner task, distance;// units;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Snackbar snackbar;
    View parentLayout;
    String userID;
    String contactName;
    String userName;
    String placeName;
    int radius;
    GeoFire geoFire;
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_set);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.END);
            getWindow().setEnterTransition(slide);
            slide = new Slide(Gravity.START);
            getWindow().setExitTransition(slide);
            getWindow().setAllowEnterTransitionOverlap(false);
        }

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("uid");

        wlv = findViewById(R.id.wlv);
        wlv.setProgressValue(progress);
        wlv.startAnimation();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("tasks");
        geoFire = new GeoFire(myRef);

        mLayout = findViewById(R.id.mLayout);
        animationDrawable = (AnimationDrawable) mLayout.getBackground();
        animationDrawable.setEnterFadeDuration(4500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        location = null;

        if (Build.VERSION.SDK_INT >= 21)
            changeStatusBarColor();

        task = findViewById(R.id.spinner);
        distance = findViewById(R.id.spinner3);
        //units = findViewById(R.id.spinner4);
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

        /*units.setPopupBackgroundResource(R.color.bg_screen1);
        final ArrayAdapter<String> unitArray = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.units));
        units.setAdapter(unitArray);
        units.setEnabled(false);*/

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

        final String[] deets = {null, null};

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
                        deets[0] = tvContactNumber.getText().toString();
                        break;
                    case 3:
                        if(tvContactNumber.getText().toString().isEmpty())
                            return;
                        reminderDesc.setText("Send email to " + contactName);
                        type = "EMAIL";
                        deets[0] = tvContactNumber.getText().toString();
                        break;
                    default: return;
                }

                if(distance.getSelectedItemPosition() == 0 || location == null)
                    return;

                // Write to the database
                String rID = myRef.push().getKey();
                if(rID == null) {
                    snackbar = Snackbar.make(parentLayout, "An error has occurred. Try again later.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else {
                    String dis[] = distance.getSelectedItem().toString().split(" ");
                    int d = Integer.parseInt(dis[0]);
                    String unit = dis[1];
                    if(unit.equals("meters"))
                        unit = "m";
                    else
                        unit = "km";


                    Reminder reminder = new Reminder(rID, userID, userName, type, reminderDesc.getText().toString(), tvContactNumber.getText().toString(), tvContactNumber.getText().toString(), d, unit, location, placeName, false);
                    myRef.child(rID).setValue(reminder);
                    /*geoFire = new GeoFire(myRef.child(rID));
                    geoFire.setLocation(rID, new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                System.err.println("There was an error saving the location to GeoFire: " + error);
                            } else {
                                System.out.println("Location saved on server successfully!");
                            }
                        }
                    });*/
                    // add geofence
                    String desc;
                    if(type.equals("REMINDER"))
                        desc = reminderDesc.getText().toString();
                    else
                        desc = contactName;
                    String key = rID + "&&" + type + "&&" + desc + "&&" + placeName + "&&" + deets[0];
                    populateGeofenceList(key, location, radius);
                    addGeofencesButtonHandler(v);
                }
                finish();
            }
        });

        distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0)
                   // units.setEnabled(true);
                    progress += 10;
                wlv.setProgressValue(progress);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

       /* units.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position != 0)

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }

        });*/

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(distance.getSelectedItemPosition() == 0) {
                    snackbar = Snackbar.make(parentLayout, "Select distance first", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }
                if(distance.getSelectedItemPosition() > 0 && distance.getSelectedItemPosition() < 8)
                    radius = Integer.parseInt(distance.getSelectedItem().toString().split(" ")[0]) * 1000;
                else
                    radius = Integer.parseInt(distance.getSelectedItem().toString().split(" ")[0]);
                DialogActivity dialogActivity = new DialogActivity();
                dialogActivity.show(getSupportFragmentManager(), "Pick your location");
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

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            performPendingGeofenceTask();
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler(View view) {
        Log.e(TAG, "addGeofencesBtnHandler");
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }
        addGeofences();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        Log.e(TAG, "addGeofences");
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler(View view) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        removeGeofences();
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        Log.e(TAG, "removeGeofences");
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            Log.d(TAG, getString(messageId));
            //Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */

    private void populateGeofenceList(String key, LatLng position, int radius) {
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(key)

                // Set the circular region of this geofence.
                .setCircularRegion(
                        position.latitude,
                        position.longitude,
                        radius
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(NEVER_EXPIRE)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER) //| Geofence.GEOFENCE_TRANSITION_EXIT)
                // Create the geofence.
                .build());
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Config.GEOFENCES_ADDED_KEY, false);
    }

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Config.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences();
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(SetActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(SetActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
            } else {
                // Permission denied.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                mPendingGeofenceTask = PendingGeofenceTask.NONE;
            }
        }
    }
}
