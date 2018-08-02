package com.akshaysadarangani.autometa;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.q42.android.scrollingimageview.ScrollingImageView;

public class SplashActivity extends AppCompatActivity {

    ImageView img;
    Animation anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.explode);
            getWindow().setEnterTransition(transition);
            getWindow().setAllowEnterTransitionOverlap(false);
        }

        img = findViewById(R.id.imageView);
        anim = AnimationUtils.loadAnimation(this, R.anim.fromtop);

        final ScrollingImageView scrollingBackground = findViewById(R.id.scrolling_background);
        scrollingBackground.stop();
        scrollingBackground.start();

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        // Block this thread for 2 seconds.
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) { }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                PrefManager pref = new PrefManager(SplashActivity.this);
                                Intent intent;
                                if (pref.isFirstTimeLaunch())
                                    intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                                else
                                    intent = new Intent(SplashActivity.this, LoginActivity.class);

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                if (!pref.isFirstTimeLaunch() && Build.VERSION.SDK_INT >= 21) {
                                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, new Pair<View, String>(img, "logo_transition"));
                                    startActivity(intent, options.toBundle());
                                }
                                else
                                    startActivity(intent);
                            }
                        });

                    }

                };
                thread.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        img.setAnimation(anim);
    }
}
