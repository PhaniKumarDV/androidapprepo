package com.keywestnetworks.kwconnect.utils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.keywestnetworks.kwconnect.constants.Encrypt;

import java.util.List;

public class NetworkUtil {

    private static final String TAG = "KW-Network Util";

    private static void addNetwork(WifiManager wifiManager, WifiConfiguration config) {
        int existNetworkId = isNetworkExist(wifiManager, config.SSID);
        boolean networkRemoved = false;

        if (existNetworkId != -1) {
            Log.i(TAG, "Removing old configuration for network " + config.SSID);
            networkRemoved = wifiManager.removeNetwork(existNetworkId);
            wifiManager.saveConfiguration();
        }

        if (existNetworkId != -1 && !networkRemoved) {
            Log.w(TAG, "Disconnect and reconnect to ssid " + config.SSID);
            wifiManager.disconnect();
            wifiManager.enableNetwork(existNetworkId, true);
            wifiManager.reconnect();
        } else {
            Log.w(TAG, "flow as usual " + config.SSID);
            int networkId = wifiManager.addNetwork(config);
            if (networkId >= 0) {
                // Try to disable the current network and start a new one.
                if (wifiManager.enableNetwork(networkId, true)) {
                    Log.i(TAG, "Associating to network " + config.SSID);
                    wifiManager.saveConfiguration();
                } else {
                    Log.w(TAG, "Failed to enable network " + config.SSID);
                }
            } else {
                Log.w(TAG, "Unable to add network " + config.SSID);
            }
        }

        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            Log.w(TAG, "Post Added Network : " + wifiConfiguration.SSID);
        }
    }

    private static int isNetworkExist(WifiManager wifiManager, String ssid) {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (ssid.equals(wifiConfiguration.SSID)) {
                Log.w(TAG, "isNetworkExist : " + wifiConfiguration.SSID);
                return wifiConfiguration.networkId;
            }
        }
        return -1;
    }

    public static void addWPANetwork(WifiManager wifiManager, String ssid, String password,
                                     int enctype, boolean hiddentype) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";

        if (enctype == Encrypt.NONE) {
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedAuthAlgorithms.clear();
        } else {
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.preSharedKey = "\"" + password + "\"";
        }

        wifiConfig.hiddenSSID = hiddentype;
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        addNetwork(wifiManager, wifiConfig);
    }

    public static int forgetNetwork(WifiManager wifiManager, String ssid) {
        String ssidQuote = "\"" + ssid + "\"";
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (ssidQuote.equals(wifiConfiguration.SSID)) {
                Log.w(TAG, "isNetworkExist : " + wifiConfiguration.SSID);
                wifiManager.removeNetwork(wifiConfiguration.networkId);
                wifiManager.saveConfiguration();
                return wifiConfiguration.networkId;
            }
        }
        return -1;
    }
}
