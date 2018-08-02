package com.akshaysadarangani.autometa;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class TriggersActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<Reminder> triggerList;
    private ReminderListAdapter mAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triggers);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        recyclerView = findViewById(R.id.recycler_view);
        triggerList = new ArrayList<>();
        mAdapter = new ReminderListAdapter(this, triggerList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        if (Build.VERSION.SDK_INT >= 21)
            changeStatusBarColor();

        // fetchTriggers();

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
                        fetchTriggers();
                    }
                });

            }

        };
        thread.start();
    }



    private void fetchTriggers() {
        triggerList.clear();
        Reminder reminder1 = new Reminder();
        reminder1.setUserName("Akshay");
        reminder1.setDescription("Pick up bread");
        reminder1.setDistance(15);
        reminder1.setUnit("mins");

        Reminder reminder2 = new Reminder();
        reminder2.setUserName("Akshay");
        reminder2.setDescription("Send text to mom");
        reminder2.setDistance(200);
        reminder2.setUnit("ft");
        reminder2.setPhone("9830338048");

        triggerList.add(reminder1);
        triggerList.add(reminder2);

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
