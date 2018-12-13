package com.hitsquadtechnologies.sifyconnect.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Context _context;
    private int PRIVATE_MODE = 0;
    private static String SHARED_PREF_NAME = "Sifyconnect";

    private static final String KEY_SSID = "ssid";
    private static final String KEY_IPADDRESS = "IPAddress";
    private static final String KEY_MACADDRESS = "MacAddress";
    private static final String KEY_RADIOMODE = "RadioMode";
    private static final String KEY_LOCALIPADDRESS = "LocalIPAddress";
    private static final String KEY_DURATION = "Duration";
    private static final String KEY_DIRECTION = "Direction";
    private static final String KEY_ISTRUE = "StartorStop";
    private static final String KEY_WIFIMAC = "WifiMac";
    private static final String KEY_TOUR = "Tour";

    public SharedPreference(Context context)
    {
        this._context = context;
        pref = _context.getSharedPreferences(SHARED_PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void saveIPAddress(String IPAddress,String ssid,String wifimac){

        editor.putString(KEY_SSID, ssid);
        editor.putString(KEY_IPADDRESS, IPAddress);
        editor.putString(KEY_WIFIMAC, wifimac);
        editor.commit();
    }


    public void resetIPAddress(){

        editor.remove(KEY_SSID);
        editor.remove(KEY_IPADDRESS);
        editor.remove(KEY_WIFIMAC);
        editor.commit();
    }

    public void saveLocalDeviceValues(String Mac, int mode,String LocalIpaddress)
    {
        editor.putString(KEY_MACADDRESS, Mac);
        editor.putInt(KEY_RADIOMODE, mode);
        editor.putString(KEY_LOCALIPADDRESS, LocalIpaddress);
        editor.commit();
    }

    public void saveDuractionValues(int duration)
    {
        editor.putInt(KEY_DURATION, duration);
        editor.commit();
    }
    public void saveDirectionValues(int direction)
    {
        editor.putInt(KEY_DIRECTION, direction);
        editor.commit();
    }
    public void saveStartOrStop(boolean isTrue)
    {
        editor.putBoolean(KEY_ISTRUE, isTrue);
        editor.commit();
    }

    public void clear()
    {
        editor.clear();
        editor.commit();
    }

    public String getIPAddress()
    {
        return pref.getString(KEY_IPADDRESS,"");
    }
    public String getMacAddress()
    {
        return pref.getString(KEY_MACADDRESS,"");
    }
    public String getWifiMac()
    {
        return pref.getString(KEY_WIFIMAC,"");
    }


    public int getRadioMode()
    {
        return pref.getInt(KEY_RADIOMODE,0);
    }
    public String getLocalIPAddress()
    {
        return pref.getString(KEY_LOCALIPADDRESS,"");
    }
    public String getSsid()
    {
        return pref.getString(KEY_SSID,null);
    }
    public int getDuration()
    {
        return pref.getInt(KEY_DURATION,0);
    }
    public int getDirection()
    {
        return pref.getInt(KEY_DIRECTION,0);
    }
    public boolean getIsTrue()
    {
        return pref.getBoolean(KEY_ISTRUE,false);
    }

    public boolean showTour() {
        return pref.getBoolean(KEY_TOUR, true);
    }

    public void setTour(boolean flag) {
        editor.putBoolean(KEY_TOUR, flag);
        editor.commit();
    }

}
