package com.keywestnetworks.kwconnect.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;
import com.keywestnetworks.kwconnect.R;
import com.keywestnetworks.kwconnect.ServerPrograms.RouterService;
import com.keywestnetworks.kwconnect.utils.Options;
import com.keywestnetworks.kwconnect.utils.SharedPreference;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static int nextPermissionRequestId = 1000;

    public static abstract class PermissionCallback {
        public abstract void onGrant();

        public void onReject() {
        }
    }

    Map<String, DisplayMapClass> displayMap = new HashMap<String, DisplayMapClass>();
    public Map<Integer, PermissionCallback> requestBag = new HashMap<>();
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    protected ProgressDialog progress;
    protected SharedPreference mSharedPreference;
    protected Menu menu = null;

    protected void onCreate(String title, int toolBarId) {
        this.onCreate(title, toolBarId, false, -1, -1);
    }

    protected void onCreate(String title, int toolBarId, boolean isNavEnabled) {
        this.onCreate(title, toolBarId, isNavEnabled, -1, -1);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayMap.put("deviceMode", new DisplayMapClass("Radio Mode", Options.DEV_MODE));
        displayMap.put("countryCode", new DisplayMapClass("Country", Options.COUNTRY_CODE_OPTIONS));
        displayMap.put("ssid", new DisplayMapClass("SSID", null));
        displayMap.put("operationalMode", new DisplayMapClass("Operational Mode", Options.OPERATIONAL_MODE));
        displayMap.put("channelBW", new DisplayMapClass("Bandwidth", Options.BANDWIDTH));
        displayMap.put("channel", new DisplayMapClass("Channel", null));
        displayMap.put("encryptType", new DisplayMapClass("Encryption", Options.ENCRYPT));
        displayMap.put("encryptKey", new DisplayMapClass("Encryption Key", null));
        displayMap.put("ddrsStatus", new DisplayMapClass("DDRS Status", Options.ENABLE_DISABLE));
        displayMap.put("spacialStream", new DisplayMapClass("Spatial Stream", Options.SPATIAL_STREAM));
        displayMap.put("modulationIndex", new DisplayMapClass("Modulation Index", Options.MCSINDEX));
        displayMap.put("minModulationIndex", new DisplayMapClass("Min Modulation Index", Options.MCSINDEX));
        displayMap.put("maxModulationIndex", new DisplayMapClass("Max Modulation Index", Options.MCSINDEX));
        displayMap.put("atpcStatus", new DisplayMapClass("ATPC Status", Options.ENABLE_DISABLE));
        displayMap.put("tranmitPower", new DisplayMapClass("TX Power", null));
        displayMap.put("ipAddrType", new DisplayMapClass("IP Address Type", Options.IP_ADDRESS_TYPE));
        displayMap.put("ipAddress", new DisplayMapClass("IP Address", null));
        displayMap.put("netMask", new DisplayMapClass("Subnet Mask", null));
        displayMap.put("gatewayIp", new DisplayMapClass("Gateway IP", null));
        displayMap.put("custName", new DisplayMapClass("Customer Name", null));
        displayMap.put("linkId", new DisplayMapClass("Link ID", null));
        displayMap.put("vlanStatus", new DisplayMapClass("VLAN Status", Options.ENABLE_DISABLE));
        displayMap.put("vlanMode", new DisplayMapClass("VLAN Mode", Options.VLAN_MODE));
        displayMap.put("vlanMgmtId", new DisplayMapClass("VLAN Managment ID", null));
        displayMap.put("vlanAccessId", new DisplayMapClass("VLAN Access ID", null));
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
            mToolbar.setNavigationIcon(R.drawable.ic_dehaze_white_36dp);
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
    }

    public void requestPermission(String permissionCode, @NonNull PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permissionCode) == PackageManager.PERMISSION_GRANTED) {
                callback.onGrant();
            } else {
                int requestId = nextPermissionRequestId++;
                requestPermissions(new String[]{permissionCode}, requestId);
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
                if (permissionCodes.length - 1 == index) {
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
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
        /* Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        this.menu = menu;
        enableDisableIcon(RouterService.getInstance().isEnableSave());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                RouterService.getInstance().disconnect();
                RouterService.getInstance().loginFailed();
                showHome();
                break;
            case R.id.action_apply:
                displaysavedDialog();
                break;
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

    protected abstract void updateUI(Configuration mConfiguration);

    public void displaysavedDialog() {
        boolean modified = false;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.view_add_saved_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Configuration Changes");
        Button applyBtn = dialogView.findViewById(R.id.cfgapplyBtn);
        Button revertBtn = dialogView.findViewById(R.id.cfgrevertBtn);

        final Configuration oldConfiguration = RouterService.getInstance().getOldConfiguration();
        final Configuration newConfiguration = RouterService.getInstance().getNewConfiguration();
        dialogBuilder.setMessage("\nThere are no changes to apply!\n");

        if (newConfiguration != null) {
            StringBuilder builder = new StringBuilder();
            Map<String, Object> map = oldConfiguration.modifiedMap(newConfiguration);
            if (map.size() > 0) {
                Object v = map.get("channel");
                if (v instanceof Integer && ((Integer) v).intValue() == 0) {
                    map.put("channel", "Auto");
                }
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    DisplayMapClass displayMapClass = displayMap.get(entry.getKey());
                    if (displayMapClass != null) {
                        if (displayMapClass.option != null) {
                            builder.append(displayMapClass.displayName + " = " + displayMapClass.option.getValueByKey((int) entry.getValue()) + "\n");
                        } else {
                            builder.append(displayMapClass.displayName + " = " + entry.getValue() + "\n");
                        }
                    } else {
                        builder.append(entry.getKey() + " = " + entry.getValue() + "\n");
                    }

                }
                dialogBuilder.setMessage(builder.toString());
                modified = true;
            }
        }
        final AlertDialog alertDialog = dialogBuilder.create();
        revertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                updateUI(oldConfiguration);
                RouterService.getInstance().setNewConfiguration(null);
                RouterService.getInstance().setEnableSave(false);
                enableDisableIcon(false);
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                /* Showing the Progress Bar */
                showProgress("Applying Configuration...", 20 * 1000, new Runnable() {
                    @Override
                    public void run() {
                        new CountDownTimer(10000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                Log.i(ConfigurationActivity.class.getName(), "onFinish setConfiguration.....");
                                progress.dismiss();
                                showDiscovery();
                                RouterService.getInstance().setNewConfiguration(null);
                                RouterService.getInstance().setEnableSave(false);
                            }
                        }.start();
                    }
                });
                KeywestPacket setpacket = newConfiguration.buildPacketFromUI();
                Log.i(ConfigurationActivity.class.getName(), "applying configuration setConfiguration.....");
                RouterService.getInstance().sendRequest(setpacket, new RouterService.Callback<KeywestPacket>() {
                    @Override
                    public void onSuccess(KeywestPacket packet) {
                        Log.i(ConfigurationActivity.class.getName(), "on success applying configuration.....");
                    }

                    @Override
                    public void onError(String msg, Exception e) {
                        Log.i(ConfigurationActivity.class.getName(), "on error applying configuration.....");
                    }
                });
            }
        });

        if (!modified) {
            applyBtn.setVisibility(View.GONE);
            revertBtn.setVisibility(View.GONE);
        }

        if (mSharedPreference.getIsTrue()) {
            applyBtn.setVisibility(View.GONE);
            revertBtn.setVisibility(View.GONE);
        }
        alertDialog.show();
    }

    protected void enableDisableIcon(boolean enableDisable) {
        if (menu != null) {
            if (!enableDisable) {
                menu.findItem(R.id.action_apply).setIcon(R.drawable.ic_save_white_36dp);
            } else {
                menu.findItem(R.id.action_apply).setIcon(R.drawable.ic_save_grey_600_36dp);
            }
        }
    }

    public class DisplayMapClass {
        private String displayName;
        private Options option;

        public DisplayMapClass(String displayName, Options option) {
            this.option = option;
            this.displayName = displayName;
        }
    }

    protected void showProgress(String message, int timeout, final Runnable finishCallback) {
        final String ssid = mSharedPreference.getSsid();
        progress = new ProgressDialog(this);
        progress.setMessage(message);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        if (timeout > 0) {
            new CountDownTimer(timeout, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Log.i(ConfigurationActivity.class.getName(), "closing show progress.....");
                    finishCallback.run();
                }
            }.start();
        }
    }

    public void showDiscovery() {
        this.startActivity(new Intent(this, DiscoveryActivity.class));
        this.finish();
    }

    /* Redirect to the Home Activity */
    public void showHome() {
        this.startActivity(new Intent(this, HomeActivity.class));
        this.finish();
    }
}