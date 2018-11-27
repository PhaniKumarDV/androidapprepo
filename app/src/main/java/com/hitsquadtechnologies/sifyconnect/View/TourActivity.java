package com.hitsquadtechnologies.sifyconnect.View;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

public class TourActivity extends AppCompatActivity {

    CarouselView carouselView;

    int[] sampleImages = {R.drawable.intro_screen1,
            R.drawable.intro_screen2,
            R.drawable.intro_screen3,
            R.drawable.intro_screen4};

    private Button endTourBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
        carouselView = findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);
        endTourBtn = findViewById(R.id.endTourBtn);
        new SharedPreference(this).setTour(false);
        carouselView.setViewListener(new ViewListener() {
            @Override
            public View setViewForPosition(int position) {
                View carousalScreenView = getLayoutInflater().inflate(R.layout.activity_tour_view, null);
                ImageView image = carousalScreenView.findViewById(R.id.carousalImageView);
                image.setImageResource(sampleImages[position]);
                return carousalScreenView;
            }
        });
        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int i = 0;
            private boolean reachedEnd = false;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == sampleImages.length - 1 && reachedEnd) {
                    TourActivity.this.endTour(null);
                }
                reachedEnd = position == sampleImages.length - 1;
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void endTour(View view) {
        this.startActivity(new Intent(TourActivity.this, DiscoveryActivity.class));
        this.finish();
    }
}
