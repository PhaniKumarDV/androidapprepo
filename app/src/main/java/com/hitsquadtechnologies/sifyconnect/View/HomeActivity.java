package com.hitsquadtechnologies.sifyconnect.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.hitsquadtechnologies.sifyconnect.R;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.onCreate(getString(R.string.client_name), R.id.toolbar);
    }

    public void goToSummary(View v) {
        this.startActivity(new Intent(this, SummaryActivity.class));
    }

    public void goToDiscovery(View v) {
        this.startActivity(new Intent(this, DiscoveryActivity.class));
    }

    public void goToConfiguration(View v) {
        this.startActivity(new Intent(this, ConfigurationActivity.class));
    }

    public void goToAlignment(View v) {
        this.startActivity(new Intent(this, AlignmentActivity.class));
    }

    public void goToLinkTest(View v) {
        this.startActivity(new Intent(this, LinkTestActivity.class));
    }

}
