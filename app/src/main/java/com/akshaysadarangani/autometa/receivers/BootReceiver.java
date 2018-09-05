package com.akshaysadarangani.autometa.receivers;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.akshaysadarangani.autometa.Config;
import com.akshaysadarangani.autometa.GeofenceErrorMessages;
import com.akshaysadarangani.autometa.services.GeofenceTransitionsJobIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class BootReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>,OnCompleteListener<Void> {

    private static GoogleApiClient mGoogleApiClient;
    private static List<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;
    private static final String TAG = "BootReceiver";
    Context contextBootReceiver;

    @Override
    public void onReceive(final Context context, Intent intent) {
        contextBootReceiver = context;
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;

        if ((intent.getAction().equals("android.location.MODE_CHANGED") && isLocationServciesAvailable(contextBootReceiver)) || (intent.getAction().equals("android.location.PROVIDERS_CHANGED") && isLocationModeAvailable(contextBootReceiver)) || intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.LOCKED_BOOT_COMPLETED")) {
            // isLocationModeAvailable for API >=19, isLocationServciesAvailable for API <19
            Log.e(TAG, "location_mode_change");
            sharedPrefs = context.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
            editor = sharedPrefs.edit();
            editor.remove("Geofences added");
            editor.apply();
            if (!isGooglePlayServicesAvailable()) {
                Log.i(TAG, "Google Play services unavailable.");
                return;
            }

            mGeofencePendingIntent = null;
            mGeofenceList = new ArrayList<>();

            // Fetch geofences
            SharedPreferences pref = context.getSharedPreferences("GEOFENCES_DB", Context.MODE_PRIVATE);
            Map<String,?> keys = pref.getAll();
            for(Map.Entry<String,?> entry : keys.entrySet()) {
                String[] params = entry.getValue().toString().split("&&");
                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(entry.getKey())

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                Double.parseDouble(params[0]),
                                Double.parseDouble(params[1]),
                                Integer.parseInt(params[2])
                        )
                        .setExpirationDuration(NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build());
                Log.e(TAG, "added Geofence");
            }

            mGoogleApiClient = new GoogleApiClient.Builder(contextBootReceiver)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private boolean isLocationModeAvailable(Context context) {

        return Build.VERSION.SDK_INT >= 19 && getLocationMode(context) != Settings.Secure.LOCATION_MODE_OFF;
    }

    public boolean isLocationServciesAvailable(Context context) {
        if (Build.VERSION.SDK_INT < 19) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        } else return false;
    }

    public int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        SharedPreferences sharedPrefs = contextBootReceiver.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
        String geofencesExist = sharedPrefs.getString("Geofences added", null);

        if (geofencesExist != null && checkPermissions()) {

            GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(contextBootReceiver);
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(this);

            /*LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent(contextBootReceiver)
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.e(TAG, "deprecated geofence added");
                        SharedPreferences sharedPrefs = contextBootReceiver.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString("Geofences added", "1");
                        editor.apply();
                    }
                }
            });*/

        }

    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(contextBootReceiver, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(contextBootReceiver, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(contextBootReceiver, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(contextBootReceiver, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((android.app.Activity) contextBootReceiver,
                        Config.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.i(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }

    }

    @Override
    public void onResult(Status status) {

    }

    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(contextBootReceiver);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (android.app.Activity) contextBootReceiver,
                        Config.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    static GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    static PendingIntent getGeofencePendingIntent(Context context) {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsJobIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.d(TAG, "Added Geofences");
            SharedPreferences sharedPrefs = contextBootReceiver.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("Geofences added", "1");
            editor.apply();
            //Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(contextBootReceiver, task.getException());
            Log.e(TAG, errorMessage);
        }
    }
}