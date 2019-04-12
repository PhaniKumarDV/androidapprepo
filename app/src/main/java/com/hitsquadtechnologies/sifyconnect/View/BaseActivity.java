package com.hitsquadtechnologies.sifyconnect.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.google.firebase.auth.FirebaseAuth;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.utils.Options;
import java.util.HashMap;
import java.util.List;
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
    private WifiManager mConnectwifiManager;
    protected void onCreate(String title, int toolBarId) {
        this.onCreate(title, toolBarId, false, -1, -1);
    }
    protected void onCreate(String title, int toolBarId, boolean isNavEnabled) {
        this.onCreate(title, toolBarId, isNavEnabled, -1, -1);
    }
    protected void onCreate(String title, int toolBarId, int drawerLayoutId, int drawerNavId) {
        onCreate(title, toolBarId, false, drawerLayoutId, drawerNavId);
    }
    protected void onCreate(String title, int toolBarId, boolean isNavEnabled, int drawerLayoutId, int drawerNavId) {
        final Activity self = this;
        this.mToolbar = findViewById(toolBarId);
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setTitle(title);
        if (isNavEnabled) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    self.startActivity(new Intent(self, HomeActivity.class));
                }
            });
            mToolbar.setNavigationIcon(R.drawable.hamburger);
        }
        if (!isNavEnabled && drawerLayoutId >= 0 && drawerNavId >= 0) {
            this.mDrawerLayout = findViewById(drawerLayoutId);
            NavigationView navigationView = findViewById(drawerNavId);
            navigationView.setNavigationItemSelectedListener(this);
        }
        if (mDrawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.mDrawerLayout, this.mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
        mConnectwifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        } else if (id == R.id.nav_wireless) {
            Intent intent = new Intent(this, StaticsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
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
        if (this.mDrawerLayout != null && this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

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
    protected void initSpinner(Spinner spinner, Options options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    public int getSelectedOption(Spinner spinner, Options options) {
        return options.getKeyByValue(spinner.getSelectedItem().toString());
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void connectToWifi(String networkSSID,String pass) {
        hideKeyboard();
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\""+ pass +"\"";
        if (mConnectwifiManager != null) {
            mConnectwifiManager.addNetwork(conf);
        }
        List<WifiConfiguration> list = null;
        if (mConnectwifiManager != null) {
            list = mConnectwifiManager.getConfiguredNetworks();
        }
        if ( list != null ) {
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    mConnectwifiManager.disconnect();
                    mConnectwifiManager.enableNetwork(i.networkId, true);
                    mConnectwifiManager.reconnect();
                }
            }
        }
    }

    @Deprecated
    protected void forgetNetwork1(String networkSSID) {
        hideKeyboard();
       /* WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\""+ pass +"\"";
        if (mConnectwifiManager != null) {
            mConnectwifiManager.addNetwork(conf);
        }*/
        List<WifiConfiguration> list = null;
        if (mConnectwifiManager != null) {
            list = mConnectwifiManager.getConfiguredNetworks();
        }
        if ( list != null ) {
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    mConnectwifiManager.removeNetwork(i.networkId);
               /* mConnectwifiManager.enableNetwork(i.networkId, true);
                mConnectwifiManager.reconnect();*/
                }
            }
        }
    }
}