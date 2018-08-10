package com.akshaysadarangani.autometa;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlotActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String userName, userID;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Marker m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("uid");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("tasks");
        final List<Reminder> triggerList = new ArrayList<>();

        Query query = myRef.orderByChild("userID").equalTo(userID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(triggerList.size() > 0) {
                    triggerList.clear();
                }
                if(dataSnapshot.exists()) {
                    for(DataSnapshot d : dataSnapshot.getChildren()) {
                        String rid = d.getKey();
                        String type = d.child("type").getValue(String.class);
                        String description = d.child("description").getValue(String.class);
                        String phone = d.child("phone").getValue(String.class);
                        String email = d.child("email").getValue(String.class);
                        int distance = d.child("distance").getValue(Integer.class);
                        String units = d.child("unit").getValue(String.class);

                        LatLng location = new LatLng(
                                d.child("location").child("latitude").getValue(Double.class),
                                d.child("location").child("longitude").getValue(Double.class));
                        String placeName = d.child("placeName").getValue(String.class);
                        boolean completed = d.child("completed").getValue(Boolean.class);
                        Reminder r = new Reminder(rid, userID, userName, type, description, phone, email, distance, units, location, placeName, completed);
                        m = mMap.addMarker(new MarkerOptions().position(location).title(description));
                        int shadeColor = 0x44ff0000;
                        int radius = distance;

                        assert units != null;
                        if(units.equals("km"))
                            radius = distance * 1000;
                        if(completed)
                            shadeColor = 0x4400ff00;
                        Circle circle = mMap.addCircle(new CircleOptions()
                                .center(m.getPosition())
                                .radius(radius)
                                .strokeColor(shadeColor)
                                .fillColor(shadeColor));
                        triggerList.add(r);
                    }
                    final CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(m.getPosition())
                            .zoom(14)                   // Sets the zoom
                            .build();                   //
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLng(m.getPosition()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
}
