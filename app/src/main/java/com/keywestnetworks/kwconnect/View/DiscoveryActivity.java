package com.keywestnetworks.kwconnect.View;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hsq.kw.packet.vo.Configuration;
import com.keywestnetworks.kwconnect.Adapters.WifiscannerAdapter;
import com.keywestnetworks.kwconnect.BroadcostReceivers.WifiConnectionReceiver;
import com.keywestnetworks.kwconnect.Model.wifiDetailsdata;
import com.keywestnetworks.kwconnect.R;
import com.keywestnetworks.kwconnect.ServerPrograms.RouterService;
import com.keywestnetworks.kwconnect.constants.Encrypt;
import com.keywestnetworks.kwconnect.constants.Hidden;
import com.keywestnetworks.kwconnect.utils.NetworkUtil;
import com.keywestnetworks.kwconnect.utils.Options;
import com.keywestnetworks.kwconnect.utils.SharedPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/* This file provides the functionality to scan the list of
   wireless channels supported by the wireless radio & provides
   the ability to connect to the wireless networks */

public class DiscoveryActivity extends BaseActivity {
    private static final int SCAN_INTERVAL = 5000;
    WifiManager wifiManager;
    WifiScanReceiver receiverWifi;
    WifiConnectionReceiver connectWifiState;
    WifiscannerAdapter adapter;
    ArrayList<wifiDetailsdata> scannedWifisDetailsArrayList;
    ListView mListViwProvider;
    List<ScanResult> wifiList;
    boolean mScanStopped;
    boolean isConnected = false;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        this.onCreate("Discovery", R.id.toolbar, true);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.disc_bottom_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        discoveryActivityInit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.disc_home:
                    showHome();
                    return true;
            }
            return false;
        }
    };

    private void discoveryActivityInit() {
        mSharedPreference = new SharedPreference(DiscoveryActivity.this);
        scannedWifisDetailsArrayList = new ArrayList<wifiDetailsdata>();
        mListViwProvider = (ListView) findViewById(R.id.list_view_wifi);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        /* If WifiMgr is disabled, then enable it automatically */
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        if (mSharedPreference.showTour()) {
            this.startActivity(new Intent(this, TourActivity.class));
        }
        /* Create & Start Scan Timer to do regular scanning till connection is established */
        timer = new CountDownTimer(SCAN_INTERVAL * 1000, SCAN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                wifiManager.startScan();
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
        requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                new PermissionCallback() {
                    @Override
                    public void onGrant() {
                        receiverWifi = new WifiScanReceiver();
                        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        connectWifiState = new WifiConnectionReceiver(DiscoveryActivity.this);
                        registerReceiver(connectWifiState, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    }
                }
        );
        listViewOnItemclickListner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.discovery_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_network:
                displayAddNetworkDialog();
                break;
            case R.id.action_logout:
                RouterService.getInstance().disconnect();
                RouterService.getInstance().loginFailed();
                showHome();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateUI(Configuration mConfiguration) {

    }

    public void displayAddNetworkDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.view_add_network_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText ssdEditText = (EditText) dialogView.findViewById(R.id.ssdEditText);
        final Spinner hiddenType = dialogView.findViewById(R.id.config_hidden);
        final Spinner encryptType = dialogView.findViewById(R.id.config_enctype);
        final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.passwordEditText);
        final View passwordview = dialogView.findViewById(R.id.enckey_view);

        dialogBuilder.setTitle("Add Network");
        final AlertDialog alertDialog = dialogBuilder.create();

        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button loaddefBtn = dialogView.findViewById(R.id.load_default);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        loaddefBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ssdEditText.setText("SMAC3_Wi-Fi");
                encryptType.setSelection(1);
                passwordEditText.setText("sify@1234");
            }
        });

        /* Set Encryption Options */
        encryptType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                int encrypttype = getSelectedOption(encryptType, Options.ENCRYPT);
                if (encrypttype == Encrypt.NONE) {
                    passwordview.setVisibility(View.GONE);
                } else {
                    passwordview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button addBtn = dialogView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hidden;
                int encrypttype = getSelectedOption(encryptType, Options.ENCRYPT);
                int hiddenval = getSelectedOption(hiddenType, Options.HIDDEN);
                if (hiddenval == Hidden.YES)
                    hidden = true;
                else
                    hidden = false;

                if (isValidAddNetworkDetails(ssdEditText.getText().toString(),
                        passwordEditText.getText().toString(), encrypttype)) {
                    NetworkUtil.addWPANetwork(wifiManager, ssdEditText.getText().toString(),
                            passwordEditText.getText().toString(), encrypttype, hidden);
                    startScan();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(getBaseContext(), "SSID & Password should not be empty and" +
                            " password length must be atleast 8 charcters.", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
    }

    private boolean isValidAddNetworkDetails(String ssid, String password, int encrypttype) {
        if (TextUtils.isEmpty(ssid))
            return false;
        else {
            if (password.length() < 8 && encrypttype != Encrypt.NONE)
                return false;
            else
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiverWifi);
            unregisterReceiver(connectWifiState);
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listViewOnItemclickListner() {
        mListViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                checkWifiConnected(view, position);
            }
        });
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        /*Find the currently focused view, so we can grab the correct window token from it.*/
        View view = this.getCurrentFocus();
        /*If no view currently has focus, create a new one, just so we can grab a window token from it*/
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void connectNetwork(final String ssid, final String password, final String security) {
        hideKeyboard();
        boolean hidden = false;
        int sectype = "none".equalsIgnoreCase(security) ? Encrypt.NONE : Encrypt.WPA2_PSK;
        if (isValidAddNetworkDetails(ssid, password, sectype)) {
            NetworkUtil.addWPANetwork(wifiManager, ssid, password, sectype, hidden);
            startScan();
        } else {
            Toast.makeText(getBaseContext(), "SSID & Password should not be empty and" +
                    " password length must be atleast 8 charcters.", Toast.LENGTH_LONG).show();
        }
    }

    public void forgetNetworkConfirmation(final String ssid) {
        new SweetAlertDialog(DiscoveryActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("You want to forget  this network.")
                .setConfirmText("Forget")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        NetworkUtil.forgetNetwork(wifiManager, ssid);
                        sDialog
                                .setTitleText("Done!")
                                .setContentText("Your network has been removed.")
                                .setConfirmText("OK")
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .show();
    }

    private void checkWifiConnected(View view, int position) {
        isConnected = false;
        TextView ssid = (TextView) view.findViewById(R.id.wifi_name);
        if (ssid.getText().toString().equalsIgnoreCase(mSharedPreference.getSsid())) {
            isConnected = true;
        }
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public void stopScan() {
        mScanStopped = true;
    }

    public void startScan() {
        mScanStopped = false;
    }

    /* This class is called as callback on the change of ScanResults by WifiManager */
    class WifiScanReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onReceive(Context c, Intent intent) {
            /* If ScanStopped, then return */
            if (DiscoveryActivity.this.mScanStopped) {
                return;
            }
            /* Get ScanResults, clear the old displayed list and add new entries */
            wifiList = wifiManager.getScanResults();
            scannedWifisDetailsArrayList.clear();
            for (int i = 0; i < wifiList.size(); i++) {
                wifiDetailsdata mWifiDetailsdata = new wifiDetailsdata();
                if (wifiList.get(i).SSID.equals("")) {
                    continue;
                }
                mWifiDetailsdata.setBSSID(wifiList.get(i).BSSID);
                mWifiDetailsdata.setSSID(wifiList.get(i).SSID);
                mWifiDetailsdata.setCapabilities(wifiList.get(i).capabilities);
                mWifiDetailsdata.setRssi(wifiList.get(i).level);
                mWifiDetailsdata.setFrequency(wifiList.get(i).frequency);
                scannedWifisDetailsArrayList.add(mWifiDetailsdata);
            }
            final String connectedWifiSsid = new SharedPreference(c).getWifiMac();
            Comparator<wifiDetailsdata> comparator = new Comparator<wifiDetailsdata>() {
                @Override
                public int compare(wifiDetailsdata o1, wifiDetailsdata o2) {
                    if (o1 == null) {
                        return 1;
                    } else if (o2 == null) {
                        return -1;
                    } else {
                        Integer rss1 = o1.getRssi();
                        Integer rss2 = o2.getRssi();
                        return rss2.compareTo(rss1);
                    }
                }
            };
            Collections.sort(scannedWifisDetailsArrayList, comparator);
            adapter = new WifiscannerAdapter(DiscoveryActivity.this, scannedWifisDetailsArrayList);
            mListViwProvider.setAdapter(adapter);
            mListViwProvider.refreshDrawableState();
        }
    }

    public void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DiscoveryActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /* Redirect to the Home Activity */
    public void showHome() {
        this.startActivity(new Intent(this, HomeActivity.class));
        this.finish();
    }
}