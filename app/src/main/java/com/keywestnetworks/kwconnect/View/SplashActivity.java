package com.keywestnetworks.kwconnect.View;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.keywestnetworks.kwconnect.R;
import com.keywestnetworks.kwconnect.utils.SharedPreference;

public class SplashActivity extends AppCompatActivity {
    /* Duration of wait */
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                SharedPreference sharedPreference = new SharedPreference(SplashActivity.this);
                if (sharedPreference.showTour()) {
                    SplashActivity.this.startActivity(new Intent(SplashActivity.this, TourActivity.class));
                } else {
                    SplashActivity.this.startActivity(new Intent(SplashActivity.this, DiscoveryActivity.class));
                }
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}