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
import android.widget.Toast;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.View.DiscoveryActivity;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestLTVPacket;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.AuthenticationPacket;
import com.hsq.kw.packet.vo.Configuration;

/* This file receives the Wireless Radio Status Events based on which
   the TCP Connection is maintained
 */
public class WifiConnectionReceiver extends BroadcastReceiver {
    SharedPreference mSharedPreference;
    private Context mContext;
    private int connectionState = 0;
    private DiscoveryActivity activity = null;
    public WifiConnectionReceiver(DiscoveryActivity activity) {
        this.activity = activity;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(final Context context, Intent intent) {
        mSharedPreference = new SharedPreference(context);
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Toast.makeText(context, info.getState().name(), Toast.LENGTH_LONG).show();
            /* If Connected, then establish the TCP Link */
            if (info.isConnected()) {
                if (RouterService.getInstance().getConnectionState() == 0) {
                    mContext = context;
                    locateServer();
                } /*else if (RouterService.getInstance().getConnectionState() == 1){
                    startLoginActivity();
                }*/
            } else {
                    /* If !Connected, then disconnect the TCP Link */
                mSharedPreference.resetIPAddress();
                mSharedPreference.saveIPAddress("", method(""), "");
                RouterService.getInstance().disconnect();


                    //connectionState = 1;
           }
        }
    }
    /* Function which establishes the TCP Connection with the Server */
    private void locateServer() {
        final Context context = mContext;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        Log.e("ssid", ssid);
        String bssid = wifiInfo.getBSSID();
        String iPAddress = intToIp(wifiManager.getDhcpInfo().gateway);
        mSharedPreference.resetIPAddress();
        mSharedPreference.saveIPAddress(iPAddress, method(ssid), bssid);
        RouterService.getInstance().connectTo(iPAddress, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket outerPacket) {

                activity.showToast("Server Found and Connected..");
                //TODO: load login activity

                Log.i(WifiConnectionReceiver.class.getName(), "Server Found and Connected");
                //TODO: below code moves to login activity
                startLoginActivity();


                // from here
                /*AuthenticationPacket authenticationPacket = new AuthenticationPacket("sifyhyd","sifyf@1234");
                final KeywestPacket authRequestPacket = authenticationPacket.getPacket();
                RouterService.getInstance().sendReq(authRequestPacket, new RouterService.Callback<KeywestPacket>() {
                    @Override
                    public void onSuccess(final KeywestPacket innerPacket) {
                        if (innerPacket != null) {
                            KeywestLTVPacket packet = innerPacket.getLTVPacketByType(1);
                            if (packet != null) {
                                byte [] status = packet.getValue();
                                if (status[0] == 1) {
                                    connectionState = 1;
                                    // call configuation Request
                                    activity.showToast("Authentication success. sending config request");
                                    sendConfigurationRequest();
                                } else {
                                    // show toast
                                    RouterService.getInstance().authenticationFailed();
                                    activity.showToast("Authentication failed");
                                }
                            }
                        }
                        *//*Configuration configuration = new Configuration(innerPacket);
                        String ipAddress = configuration.getIpAddress();
                        if (configuration.getIpAddrType() == 2) {
                            ipAddress = configuration.getDhcpAddress();
                        }
                        mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(), configuration.getDeviceMode(), ipAddress);*//*
                    }
                });*/
                // to here
            }
            @Override
            public void onError(String msg, Exception e) {
                activity.showToast("Server Not Found..");
                Log.e(WifiConnectionReceiver.class.getName(), msg, e);
            }
        });
    }


    public void startLoginActivity() {
        if(!RouterService.getInstance().isUserAuthenticated()) {
            this.activity.wifiConnected();
        }

    }

    public void sendConfigurationRequest() {
        KeywestPacket configRequest = new Configuration().getPacket();
        RouterService.getInstance().sendReq(configRequest, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                Configuration configuration = new Configuration(packet);
                mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(), configuration.getDeviceMode(), configuration.getIpAddress());
            }
        });

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