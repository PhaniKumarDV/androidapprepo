package com.hitsquadtechnologies.sifyconnect.View;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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
    WifiManager mConnectwifiManager;
    boolean mScanStopped;



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
        scaningForWifiList();
        listViewOnItemclickListner();
        mConnectwifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void scaningForWifiList()
    {
        requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                new PermissionCallback() {
                    @Override
                    public void onGrant() {
                        Toast.makeText(DiscoveryActivity.this, "Scanning....", Toast.LENGTH_LONG).show();
                        receiverWifi = new WifiScanReceiver();
                        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        wifiManager.startScan();
                        connectWifiState = new WifiConnectionReceiver();
                        registerReceiver(connectWifiState, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(receiverWifi);
            unregisterReceiver(connectWifiState);
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

    public void connectToWifi(String networkSSID,String pass)
    {
        hideKeyboard();
        WifiConfiguration conf = new WifiConfiguration();
                     conf.SSID = "\"" + networkSSID + "\"";
             conf.preSharedKey = "\""+ pass +"\"";
              connet(conf,networkSSID);
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


    private void connet(WifiConfiguration conf,String networkSSID) {

        if (mConnectwifiManager != null) {
            mConnectwifiManager.addNetwork(conf);
        }
        List<WifiConfiguration> list = null;
        if (mConnectwifiManager != null) {
            list = mConnectwifiManager.getConfiguredNetworks();
        }
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                mConnectwifiManager.disconnect();
                mConnectwifiManager.enableNetwork(i.networkId, true);
                mConnectwifiManager.reconnect();
            }
        }
        scaningForWifiList();
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
            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return (Integer.compare(rhs.level, lhs.level));
                }
            });
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

            adapter = new WifiscannerAdapter(DiscoveryActivity.this, scannedWifisDetailsArrayList);
            mListViwProvider.setAdapter(adapter);

        }
    }

    private void forgetPassword(){
            int networkId = mConnectwifiManager.getConnectionInfo().getNetworkId();
            Log.d("networkId", String.valueOf(networkId));
            mConnectwifiManager.removeNetwork(networkId);
            mConnectwifiManager.saveConfiguration();
    }
}