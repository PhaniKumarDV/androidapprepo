package com.hitsquadtechnologies.sifyconnect.utils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class NetworkUtil {

    private static final String TAG = "KW-Network Util";

    private static void addNetwork(WifiManager wifiManager, WifiConfiguration config) {
        int existNetworkId = isNetworkExist(wifiManager, config.SSID);
        if (existNetworkId != -1) {
            Log.i(TAG, "Removing old configuration for network " + config.SSID);
            wifiManager.removeNetwork(existNetworkId);
            wifiManager.saveConfiguration();
        }
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

    public static void addWPANetwork(WifiManager wifiManager, String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.preSharedKey = "\"" + password + "\"";
        wifiConfig.hiddenSSID = true;
        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        addNetwork(wifiManager, wifiConfig);
    }

    public static int forgetNetwork(WifiManager wifiManager, String ssid) {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (ssid.equals(wifiConfiguration.SSID)) {
                Log.w(TAG, "isNetworkExist : " + wifiConfiguration.SSID);
                wifiManager.removeNetwork(wifiConfiguration.networkId);
                wifiManager.saveConfiguration();
                return wifiConfiguration.networkId;
            }
        }
        return -1;
    }


}
