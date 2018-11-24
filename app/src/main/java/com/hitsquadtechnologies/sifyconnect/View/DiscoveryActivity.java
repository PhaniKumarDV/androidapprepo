package com.hitsquadtechnologies.sifyconnect.View;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.concurrent.CountDownLatch;

public class DiscoveryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    WifiManager wifiManager;
    WifiScanReceiver receiverWifi;
    List<ScanResult> wifiList;
    WifiConnectionReceiver connectWifiState;
    ArrayList<wifiDetailsdata> scannedWifisDetailsArrayList;
    ListView mListViwProvider;
    String txpassword;
    TextView mSignalstength;
    boolean isConnected = false;
    WifiscannerAdapter  adapter;
    SharedPreference mSharedPreference;
    ShowHidePasswordEditText password;
    WifiManager mConnectwifiManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discovery");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        initialization();
        navigationview();

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
        scaningForWifiList();
        listViewOnItemclickListner();
    }

    private void navigationview()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void scaningForWifiList()
    {
        Toast.makeText(this, "Scanning....", Toast.LENGTH_LONG).show();
        receiverWifi = new WifiScanReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        connectWifiState = new WifiConnectionReceiver();
        registerReceiver(connectWifiState, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
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
        if(ssid.getText().toString().equalsIgnoreCase(mSharedPreference.getSsid()))
         {
           isConnected = true;
         }
         dialogView(position);
    }

    private void connectToWifi(int position,String pass)
    {
        String networkSSID     = scannedWifisDetailsArrayList.get(position).getSSID();
        WifiConfiguration conf = new WifiConfiguration();
                     conf.SSID = "\"" + networkSSID + "\"";
             conf.preSharedKey = "\""+ pass +"\"";
              connet(conf,networkSSID);
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

    class WifiScanReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onReceive(Context c, Intent intent) {
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

            adapter = new WifiscannerAdapter(DiscoveryActivity.this, scannedWifisDetailsArrayList,mSharedPreference.getWifiMac());
            mListViwProvider.setAdapter(adapter);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_discovery) {

            Intent intent = new Intent(DiscoveryActivity.this,DiscoveryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_configuration) {

            Intent intent = new Intent(DiscoveryActivity.this,ConfigurationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_summary) {

            Intent intent = new Intent(DiscoveryActivity.this,SummaryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Alignment) {

            Intent intent = new Intent(DiscoveryActivity.this,AlignmentActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_linktest) {

            Intent intent = new Intent(DiscoveryActivity.this,LinkTestActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_wireless)
        {
            Intent intent = new Intent(DiscoveryActivity.this,StaticsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout)
        {
            logout();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DiscoveryActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void dialogView(final int position){

        final Dialog dialog = new Dialog(DiscoveryActivity.this);
        dialog.setContentView(R.layout.dialog_view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView txSsid = (TextView) dialog.findViewById(R.id.dialog_wifi_ssid);
        TextView textbssia = (TextView) dialog.findViewById(R.id.dialog_wifi_bssid);
        TextView textcapabilites = (TextView) dialog.findViewById(R.id.dialog_wifi_capabilities);
        TextView textFrequency = (TextView) dialog.findViewById(R.id.dialog_wifi_frequecy);
        TextView txchannelwidth = (TextView) dialog.findViewById(R.id.dialog_wifi_channelwidth);
        Button closeButton = (Button) dialog.findViewById(R.id.wifi_cancle_button);
        Button forgotButton = (Button) dialog.findViewById(R.id.wifi_forgot_button);
        Button connectbutton = (Button) dialog.findViewById(R.id.wifi_connect_button);
        password = (ShowHidePasswordEditText) dialog.findViewById(R.id.simplePassword);

        txSsid.setText(scannedWifisDetailsArrayList.get(position).getSSID());
        textbssia.setText(String.format("%s", scannedWifisDetailsArrayList.get(position).getBSSID()));
        textcapabilites.setText(String.format("%s", scannedWifisDetailsArrayList.get(position).getCapabilities()));
        textFrequency.setText(new StringBuilder().append(scannedWifisDetailsArrayList.get(position).getFrequency()).append(" MHz").toString());
        mConnectwifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(isConnected)
        {
            forgotButton.setEnabled(true);
            forgotButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            connectbutton.setEnabled(false);
            password.setVisibility(View.GONE);

        } else {
            connectbutton.setEnabled(true);
            forgotButton.setEnabled(false);
            connectbutton.setTextColor(getResources().getColor(R.color.colorPrimary));
            password.setVisibility(View.VISIBLE);
        }

        if(scannedWifisDetailsArrayList.get(position).getChannelWidth() == 0) {
            txchannelwidth.setText("20 MHz");
        } else if(scannedWifisDetailsArrayList.get(position).getChannelWidth() == 1) {
            txchannelwidth.setText("40 MHz");
        } else if(scannedWifisDetailsArrayList.get(position).getChannelWidth() == 2) {
            txchannelwidth.setText("  MHz");
        }

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });
        connectbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                txpassword = password.getText().toString();
                String ssid = ((TextView) view).getText().toString();
                connectToWifi(position,txpassword);
               // String mSsid = mSharedPreference.getSsid();
                String bssid = mSharedPreference.getWifiMac();
                if(bssid != null){
                    adapter = new WifiscannerAdapter(DiscoveryActivity.this, scannedWifisDetailsArrayList,bssid);
                    mListViwProvider.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });

        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                forgetPassword();

                adapter = new WifiscannerAdapter(DiscoveryActivity.this, scannedWifisDetailsArrayList,mSharedPreference.getWifiMac());
                mListViwProvider.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void forgetPassword(){
            int networkId = mConnectwifiManager.getConnectionInfo().getNetworkId();
            Log.d("networkId", String.valueOf(networkId));
            mConnectwifiManager.removeNetwork(networkId);
            mConnectwifiManager.saveConfiguration();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}