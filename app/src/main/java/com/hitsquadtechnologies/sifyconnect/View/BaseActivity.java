package com.hitsquadtechnologies.sifyconnect.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.hitsquadtechnologies.sifyconnect.R;

import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static int nextPermissionRequestId = 1000;

    public static abstract class PermissionCallback {
        public abstract void onGrant();
        public void onReject(){}
    }

    public Map<Integer, PermissionCallback> requestBag = new HashMap<>();

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    protected void onCreate(String title, int toolBarId) {
        this.mToolbar = findViewById(toolBarId);
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setTitle(title);
    }

    protected void onCreate(int drawerLayoutId, int drawerNavId) {
        this.mDrawerLayout = findViewById(drawerLayoutId);
        NavigationView navigationView = findViewById(drawerNavId);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void onCreate(String title, int toolBarId, int drawerLayoutId, int drawerNavId) {
        this.onCreate(title, toolBarId);
        this.onCreate(drawerLayoutId, drawerNavId);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.mDrawerLayout, this.mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void requestPermission(String permissionCode, @NonNull PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permissionCode) == PackageManager.PERMISSION_GRANTED) {
                callback.onGrant();
            } else {
                int requestId = nextPermissionRequestId++;
                requestPermissions(new String[]{ permissionCode }, requestId);
                this.requestBag.put(requestId, callback);
            }
        } else {
            callback.onGrant();
        }
    }

    public void requestPermissions(final String[] permissionCodes, final PermissionCallback callback, final int index) {
        requestPermission(permissionCodes[index], new PermissionCallback() {
            @Override
            public void onGrant() {
                if (permissionCodes.length-1 == index) {
                    callback.onGrant();
                } else {
                    requestPermissions(permissionCodes, callback, index + 1);
                }
            }

            @Override
            public void onReject() {
                callback.onReject();
            }
        });
    }

    public void requestPermissions(final String[] permissionCodes, @NonNull PermissionCallback callback) {
        requestPermissions(permissionCodes, callback, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionCallback callback = this.requestBag.get(requestCode);
        if (callback != null) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callback.onGrant();
                } else {
                    callback.onReject();
                }
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (this.mDrawerLayout == null) {
            return false;
        }
        int id = item.getItemId();
        if (id == R.id.nav_discovery) {

            Intent intent = new Intent(this, DiscoveryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_configuration) {

            Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_summary) {

            Intent intent = new Intent(this, SummaryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Alignment) {

            Intent intent = new Intent(this, AlignmentActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_linktest) {

            Intent intent = new Intent(this, LinkTestActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_wireless)
        {
            Intent intent = new Intent(this, StaticsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout)
        {
            logout();
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
