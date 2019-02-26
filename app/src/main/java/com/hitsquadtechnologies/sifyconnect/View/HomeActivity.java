package com.hitsquadtechnologies.sifyconnect.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;

public class HomeActivity extends BaseActivity {
    private ImageView summaryIcon;
    private ImageView discoveryIcon;
    private ImageView configurationIcon;
    private ImageView linkTestIcon;
    private ImageView alignmentIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.onCreate(getString(R.string.client_name), R.id.toolbar);
        this.summaryIcon = findViewById(R.id.summaryIcon);
        this.discoveryIcon = findViewById(R.id.discoveryIcon);
        this.configurationIcon = findViewById(R.id.configurationIcon);
        this.alignmentIcon = findViewById(R.id.alignmentIcon);
        this.linkTestIcon = findViewById(R.id.linkTestIcon);
        if (RouterService.INSTANCE.isServerFound()) {
            this.summaryIcon.setImageResource(R.drawable.summary);
            this.configurationIcon.setImageResource(R.drawable.configuration);
            this.alignmentIcon.setImageResource(R.drawable.alignment);
            this.linkTestIcon.setImageResource(R.drawable.linktest);
        } else {
            this.summaryIcon.setImageResource(R.drawable.summary_disabled);
            this.configurationIcon.setImageResource(R.drawable.configuration_disabled);
            this.alignmentIcon.setImageResource(R.drawable.alignment_disabled);
            this.linkTestIcon.setImageResource(R.drawable.linktest_disabled);
        }
    }
    public void goToSummary(View v) {
        this.goTo(SummaryActivity.class);
    }
    public void goToDiscovery(View v) {
        this.startActivity(new Intent(this, DiscoveryActivity.class));
    }
    public void goToConfiguration(View v) {
        this.goTo(ConfigurationActivity.class);
    }
    public void goToAlignment(View v) {
        this.goTo(AlignmentActivity.class);
    }
    public void goToLinkTest(View v) {
        this.goTo(LinkTestActivity.class);
    }
    private void goTo(Class<? extends Activity> activityClass) {
        if (RouterService.INSTANCE.isServerFound()) {
            this.startActivity(new Intent(this, activityClass));
        }
    }
}