package com.akshaysadarangani.autometa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.q42.android.scrollingimageview.ScrollingImageView;

public class DoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        ScrollingImageView scrollingBackground = findViewById(R.id.scrolling_background);
        scrollingBackground.stop();
        scrollingBackground.start();
    }
}
