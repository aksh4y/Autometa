package com.akshaysadarangani.autometa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TriggersActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<Reminder> triggerList;
    private ReminderListAdapter mAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String userID, userName;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triggers);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("uid");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("tasks");

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        recyclerView = findViewById(R.id.recycler_view);
        triggerList = new ArrayList<>();
        mAdapter = new ReminderListAdapter(this, triggerList);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        FloatingActionButton fab2 = findViewById(R.id.fab2);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(TriggersActivity.this, SetActivity.class);
                myIntent.putExtra("userName", userName);
                myIntent.putExtra("uid", userID);
                TriggersActivity.this.startActivity(myIntent);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(TriggersActivity.this, MapActivity.class);
                myIntent.putExtra("userName", userName);
                myIntent.putExtra("uid", userID);
                TriggersActivity.this.startActivity(myIntent);
            }
        });

        // Pull down to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                buildTriggers();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        if (Build.VERSION.SDK_INT >= 21)
            changeStatusBarColor();

        // Create a new thread inside your Actvity.
        Thread thread = new Thread() {

            @Override
            public void run() {

                // Block this thread for 2 seconds.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                // After sleep finished blocking, create a Runnable to run on the UI Thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buildTriggers();
                    }
                });

            }

        };
        thread.start();
    }

    private void buildTriggers() {
        if(mAdapter.getItemCount() > 0) {
            triggerList.clear();
            mAdapter.notifyDataSetChanged();
        }
        fetchTriggers();
    }



    private void fetchTriggers() {
        triggerList.clear();

        Query query = myRef.orderByChild("userID").equalTo(userID);
       /* query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                //progressBar.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.VISIBLE);
                if(ConnectivityReceiver.isConnected()) {
                    Calendar rightNow = Calendar.getInstance();
                    String date = "Last sync: " + rightNow.getTime();
                    tip.setText(date);
                    tip.setTextColor(Color.parseColor("#00c853"));
                }
            }

            public void onCancelled(DatabaseError dbError) {
                //progressBar.setVisibility(View.GONE);
               // recyclerView.setVisibility(View.VISIBLE);
            }
        });*/

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mAdapter.getItemCount() > 0) {
                    triggerList.clear();
                    mAdapter.notifyDataSetChanged();
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
                                d.child("location").child("latitude").getValue(Long.class),
                                d.child("location").child("longitude").getValue(Long.class));
                        String placeName = d.child("placeName").getValue(String.class);
                        boolean completed = d.child("completed").getValue(Boolean.class);
                        Reminder r = new Reminder(rid, userID, userName, type, description, phone, email, distance, units, location, placeName, completed);
                        triggerList.add(r);
                        mAdapter.notifyItemInserted(triggerList.size() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // refreshing recycler view
        mAdapter.notifyDataSetChanged();

        // stop animating Shimmer and hide the layout
        mShimmerViewContainer.stopShimmer();
        mShimmerViewContainer.setVisibility(View.GONE);

    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmer();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmer();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

    }
}
