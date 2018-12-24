package com.hitsquadtechnologies.sifyconnect.View;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hitsquadtechnologies.sifyconnect.Adapters.WifiscannerAdapter;
import com.hitsquadtechnologies.sifyconnect.BroadcostReceivers.WifiConnectionReceiver;
import com.hitsquadtechnologies.sifyconnect.Model.wifiDetailsdata;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.scottyab.showhidepasswordedittext.ShowHidePasswordEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiscoveryActivity extends BaseActivity {

    private static final int SCAN_INTERVAL = 10000;

    WifiManager wifiManager;
    WifiScanReceiver receiverWifi;
    List<ScanResult> wifiList;
    WifiConnectionReceiver connectWifiState;
    ArrayList<wifiDetailsdata> scannedWifisDetailsArrayList;
    ListView mListViwProvider;
    boolean isConnected = false;
    WifiscannerAdapter  adapter;
    SharedPreference mSharedPreference;
    ShowHidePasswordEditText password;
    boolean mScanStopped;
    CountDownTimer timer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        this.onCreate("Discovery", R.id.toolbar, true);
        initialization();
    }

    private void initialization()
    {
        mSharedPreference = new SharedPreference(DiscoveryActivity.this);
        scannedWifisDetailsArrayList = new ArrayList<wifiDetailsdata>();
        mListViwProvider = (ListView) findViewById(R.id.list_view_wifi);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
        {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        if (mSharedPreference.showTour()) {
            this.startActivity(new Intent(this, TourActivity.class));
        }
        timer = new CountDownTimer(SCAN_INTERVAL * 1000, SCAN_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(DiscoveryActivity.this, "Scanning....", Toast.LENGTH_LONG).show();
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
                        connectWifiState = new WifiConnectionReceiver();
                        registerReceiver(connectWifiState, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    }
                }
        );
        listViewOnItemclickListner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(receiverWifi);
            unregisterReceiver(connectWifiState);
            timer.cancel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void listViewOnItemclickListner()
    {
        mListViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
              checkWifiConnected(view,position);
            }
        });
    }

    private void checkWifiConnected(View view,int position)
    {
        isConnected = false;
        TextView ssid = (TextView) view.findViewById(R.id.wifi_name);
        if(ssid.getText().toString().equalsIgnoreCase(mSharedPreference.getSsid())) {
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

    class WifiScanReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onReceive(Context c, Intent intent) {
            if (DiscoveryActivity.this.mScanStopped) {
                return;
            }
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

    /*private void forgetPassword(){
            int networkId = mConnectwifiManager.getConnectionInfo().getNetworkId();
            Log.d("networkId", String.valueOf(networkId));
            mConnectwifiManager.removeNetwork(networkId);
            mConnectwifiManager.saveConfiguration();
    }*/
}