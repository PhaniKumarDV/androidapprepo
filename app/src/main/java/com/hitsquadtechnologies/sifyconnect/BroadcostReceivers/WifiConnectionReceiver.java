package com.hitsquadtechnologies.sifyconnect.BroadcostReceivers;

import android.app.ProgressDialog;
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
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;


public class WifiConnectionReceiver extends BroadcastReceiver {

    SharedPreference mSharedPreference;

    boolean locateServer = false;

    private Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(final Context context, Intent intent) {

        mSharedPreference = new SharedPreference(context);
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.isConnected()) {
                mContext = context;
                locateServer();
            }
        }
    }

    private void locateServer() {
        if (RouterService.INSTANCE.isConnecting()) {
            locateServer = true;
            return;
        }
        final Context context = mContext;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        Log.e("ssid", ssid);
        String bssid = wifiInfo.getBSSID();
        String iPAddress = intToIp(wifiManager.getDhcpInfo().gateway);
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage("Locating server");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        mSharedPreference.resetIPAddress();
        mSharedPreference.saveIPAddress(iPAddress, method(ssid), bssid);
        RouterService.INSTANCE.connectTo(iPAddress, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                Toast.makeText(context, "Server found", Toast.LENGTH_LONG).show();
                KeywestPacket configRequest = new Configuration().getPacket();
                RouterService.INSTANCE.sendRequest(configRequest, new RouterService.Callback<KeywestPacket>() {
                    @Override
                    public void onSuccess(final KeywestPacket packet) {
                        Configuration configuration = new Configuration(packet);
                        mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(), configuration.getDeviceMode(), configuration.getIpAddress());
                    }
                });
                progress.hide();
                if (locateServer) {
                    locateServer();
                    locateServer = false;
                }
            }

            @Override
            public void onError(String msg, Exception e) {
                Toast.makeText(context, "Server not found", Toast.LENGTH_LONG).show();
                progress.hide();
                Log.e(WifiConnectionReceiver.class.getName(), msg, e);
                if (locateServer) {
                    locateServer();
                    locateServer = false;
                }
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
