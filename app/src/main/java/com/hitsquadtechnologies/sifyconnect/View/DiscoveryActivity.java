package com.hitsquadtechnologies.sifyconnect.View;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hitsquadtechnologies.sifyconnect.Adapters.WifiscannerAdapter;
import com.hitsquadtechnologies.sifyconnect.BroadcostReceivers.WifiConnectionReceiver;
import com.hitsquadtechnologies.sifyconnect.Model.wifiDetailsdata;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.utils.NetworkUtil;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/*
   This file provides the functionality to scan the list of wireless channels
   supported by the wireless radio & provides the ability to connect to the wireless
   networks
 */
public class DiscoveryActivity extends BaseActivity {
    private static final int SCAN_INTERVAL = 10000;
    WifiManager wifiManager;
    WifiScanReceiver receiverWifi;
    WifiConnectionReceiver connectWifiState;
    WifiscannerAdapter adapter;
    SharedPreference mSharedPreference;
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
        initialization();
    }

    private void initialization() {
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.discovery_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_network:
                displayAddNetworkDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayAddNetworkDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.view_add_network_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText ssdEditText = (EditText) dialogView.findViewById(R.id.ssdEditText);
        final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.passwordEditText);

        dialogBuilder.setTitle("Add Network");
        final AlertDialog alertDialog = dialogBuilder.create();

        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        Button addBtn = dialogView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidAddNetworkDetails(ssdEditText.getText().toString(),
                        passwordEditText.getText().toString())) {
                    NetworkUtil.addWPANetwork(wifiManager, ssdEditText.getText().toString(),
                            passwordEditText.getText().toString());
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

    private boolean isValidAddNetworkDetails(String ssid, String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password))
            return false;
        else {
            if (password.length() < 8)
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
/*
    public void forget() {
        forgetNetwork(mSharedPreference.getSsid());
    }*/

    public void wifiConnected() {
        this.startActivity(new Intent(this, LoginActivity.class));
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
                mWifiDetailsdata.setBSSID(wifiList.get(i).BSSID);
                mWifiDetailsdata.setSSID(wifiList.get(i).SSID);
                mWifiDetailsdata.setCapabilities(wifiList.get(i).capabilities);
                mWifiDetailsdata.setRssi(wifiList.get(i).level);
                mWifiDetailsdata.setFrequency(wifiList.get(i).frequency);
                scannedWifisDetailsArrayList.add(mWifiDetailsdata);
            }
            final String connectedWifiSsid = new SharedPreference(c).getWifiMac();
            Collections.sort(scannedWifisDetailsArrayList, new Comparator<wifiDetailsdata>() {
                @Override
                public int compare(wifiDetailsdata o1, wifiDetailsdata o2) {
                    if (o1 == null) {
                        return 1;
                    } else if (o2 == null) {
                        return -1;
                    } else if (o1.getBSSID().equalsIgnoreCase(connectedWifiSsid) || o2.getSSID().trim().length() == 0) {
                        return -1;
                    } else if (o2.getBSSID().equalsIgnoreCase(connectedWifiSsid) || o1.getSSID().trim().length() == 0) {
                        return 1;
                    } else {
                        return o1.getSSID().toLowerCase().compareTo(o2.getSSID().toLowerCase());
                    }
                }
            });
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
}