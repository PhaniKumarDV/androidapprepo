package com.hitsquadtechnologies.sifyconnect.BroadcostReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;


public class WifiConnectionReceiver extends BroadcastReceiver {

    SharedPreference mSharedPreference;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context,   Intent intent) {

        mSharedPreference = new SharedPreference(context);
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.isConnected())
            {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                Log.e("ssid", ssid);

                String bssid = wifiInfo.getBSSID();
                String iPAddress = intToIp(wifiManager.getDhcpInfo().gateway);
                mSharedPreference.saveIPAddress(iPAddress,method(ssid),bssid);
                RouterService.INSTANCE.connectTo(iPAddress);
                Log.e("ipaddress", method(ssid) + "XXXXXXXX");
                //Log.e("INFO", " -- Wifi XXXXXXXXXXXXXXXconnected --- " + " SSID " + ssid );

            }
        }
    }

    public String intToIp(int i) {

        return ((i & 0xFF)+"." +
               ((i >> 8 ) & 0xFF) + "."+
               ((i >> 16 ) & 0xFF) + "."+
               ((i >> 24 ) & 0xFF));
    }

    public String method(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

}
