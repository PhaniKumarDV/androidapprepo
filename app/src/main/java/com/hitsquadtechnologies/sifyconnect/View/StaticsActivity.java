package com.hitsquadtechnologies.sifyconnect.View;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.hitsquadtechnologies.sifyconnect.Adapters.PagerAdapter;
import com.hitsquadtechnologies.sifyconnect.R;

public class StaticsActivity extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener{

     ViewPager viewPager;
     TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statics);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Statistics");

        iniView();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(StaticsActivity.this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.tools_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    private void iniView()
    {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Wireless"));
        tabLayout.addTab(tabLayout.newTab().setText("Ethernet"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_discovery)
        {
            Intent intent = new Intent(StaticsActivity.this,DiscoveryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_configuration)
        {
            Intent intent = new Intent(StaticsActivity.this,ConfigurationActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_summary)
        {
            Intent intent = new Intent(StaticsActivity.this,SummaryActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_Alignment)
        {
            Intent intent = new Intent(StaticsActivity.this,AlignmentActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_linktest)
        {
            Intent intent = new Intent(StaticsActivity.this,LinkTestActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_wireless)
        {
            Intent intent = new Intent(StaticsActivity.this,StaticsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout)
        {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.tools_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout()
    {
        Intent intent = new Intent(StaticsActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
