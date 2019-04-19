package com.hitsquadtechnologies.sifyconnect.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    private ImageView loginIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.onCreate("Home", R.id.toolbar, false);
        this.summaryIcon = findViewById(R.id.summaryIcon);
        this.discoveryIcon = findViewById(R.id.discoveryIcon);
        this.configurationIcon = findViewById(R.id.configurationIcon);
        this.alignmentIcon = findViewById(R.id.alignmentIcon);
        this.linkTestIcon = findViewById(R.id.linkTestIcon);
        this.loginIcon = findViewById(R.id.loginIcon);

        this.loginIcon.setImageResource(R.drawable.login_disabled);
        if ( RouterService.getInstance().isServerFound() ) {
            this.loginIcon.setImageResource(R.drawable.login);
        }
        if ( RouterService.getInstance().isUserAuthenticated() ) {
            this.summaryIcon.setImageResource(R.drawable.summary);
            this.configurationIcon.setImageResource(R.drawable.configuration);
            this.alignmentIcon.setImageResource(R.drawable.alignment);
            this.linkTestIcon.setImageResource(R.drawable.linktest);
            this.loginIcon.setImageResource(R.drawable.login_disabled);
        } else {
            this.summaryIcon.setImageResource(R.drawable.summary_disabled);
            this.configurationIcon.setImageResource(R.drawable.configuration_disabled);
            this.alignmentIcon.setImageResource(R.drawable.alignment_disabled);
            this.linkTestIcon.setImageResource(R.drawable.linktest_disabled);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                RouterService.getInstance().disconnect();
                RouterService.getInstance().loginFailed();
                this.loginIcon.setImageResource(R.drawable.login_disabled);
                this.summaryIcon.setImageResource(R.drawable.summary_disabled);
                this.configurationIcon.setImageResource(R.drawable.configuration_disabled);
                this.alignmentIcon.setImageResource(R.drawable.alignment_disabled);
                this.linkTestIcon.setImageResource(R.drawable.linktest_disabled);
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void goToLogin(View v) {
        if ( RouterService.getInstance().isServerFound() ) {
            this.startActivity(new Intent(this, LoginActivity.class));
        }
    }
    private void goTo(Class<? extends Activity> activityClass) {
        if (RouterService.getInstance().isServerFound() && RouterService.getInstance().isUserAuthenticated()) {
            this.startActivity(new Intent(this, activityClass));
        }
    }
}