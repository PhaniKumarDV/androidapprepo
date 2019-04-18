package com.hitsquadtechnologies.sifyconnect.Adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitsquadtechnologies.sifyconnect.Model.wifiDetailsdata;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.View.DiscoveryActivity;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;

import java.util.List;

public class WifiscannerAdapter extends ArrayAdapter<wifiDetailsdata> {
    private DiscoveryActivity mContext;
    private List<wifiDetailsdata> wifiDetailsarrayList;
    private LayoutInflater inflater;
    private View lastShownWifiDetails;
    private SharedPreference mPreferences;

    public WifiscannerAdapter(DiscoveryActivity context, List<wifiDetailsdata> wifiDetailsList) {
        super(context, R.layout.list_item, wifiDetailsList);
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.wifiDetailsarrayList = wifiDetailsList;
        this.mPreferences = new SharedPreference(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        wifiDetailsdata wifiData = wifiDetailsarrayList.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            final ViewHolder newViewHolder = new ViewHolder();
            newViewHolder.wifisecurity = (ImageView) convertView.findViewById(R.id.wifi_security);
            newViewHolder.wifiProvider = (TextView) convertView.findViewById(R.id.wifi_name);
            newViewHolder.txsignalstgth = (TextView) convertView.findViewById(R.id.signal_strength);
            newViewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
            newViewHolder.txchannels = (TextView) convertView.findViewById(R.id.Tx_channel);
            newViewHolder.txConnectedStatus = (TextView) convertView.findViewById(R.id.connectedText);
            newViewHolder.detailsLayout = convertView.findViewById(R.id.wifi_details);
            newViewHolder.security = convertView.findViewById(R.id.securityValue);
            newViewHolder.channelWidth = convertView.findViewById(R.id.channelWidthValue);
            newViewHolder.bssid = convertView.findViewById(R.id.bssidValue);
            newViewHolder.ssid = convertView.findViewById(R.id.ssidValue);
            newViewHolder.connectBtn = convertView.findViewById(R.id.connectBtn);
            newViewHolder.cancelBtn = convertView.findViewById(R.id.cancelBtn);
            newViewHolder.forgetBtn = convertView.findViewById(R.id.forgetBtn);
            newViewHolder.passwordInput = convertView.findViewById(R.id.wifiPassword);
            newViewHolder.frequencyLabel = convertView.findViewById(R.id.frequency);
            convertView.setTag(newViewHolder);
            convertView.findViewById(R.id.wifi_list_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newViewHolder.detailsLayout.getVisibility() == View.GONE) {
                        if (lastShownWifiDetails != null) {
                            lastShownWifiDetails.setVisibility(View.GONE);
                        }
                        newViewHolder.detailsLayout.setVisibility(View.VISIBLE);
                        lastShownWifiDetails = newViewHolder.detailsLayout;
                    } else {
                        newViewHolder.detailsLayout.setVisibility(View.GONE);
                    }
                    WifiscannerAdapter.this.mContext.stopScan();
                }
            });
            newViewHolder.passwordInput.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        v.clearFocus();
                        WifiscannerAdapter.this.mContext.startScan();
                        WifiscannerAdapter.this.mContext.connectToWifi(newViewHolder.wifiProvider.getText().toString(), newViewHolder.passwordInput.getText().toString());
                    }
                    return false;
                }
            });
            newViewHolder.connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WifiscannerAdapter.this.mContext.startScan();
                    WifiscannerAdapter.this.mContext.connectToWifi(newViewHolder.wifiProvider.getText().toString(), newViewHolder.passwordInput.getText().toString());
                }
            });
            newViewHolder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WifiscannerAdapter.this.mContext.startScan();
                    newViewHolder.detailsLayout.setVisibility(View.GONE);
                }
            });
            newViewHolder.forgetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WifiscannerAdapter.this.mContext.forgetNetworkConfirmation(newViewHolder.wifiProvider.getText().toString());
                    newViewHolder.detailsLayout.setVisibility(View.GONE);
                }
            });
            viewHolder = newViewHolder;
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.wifiProvider.setText(wifiData.getSSID());
        if (wifiData.getCapabilities().contains("WPA2")) {
            viewHolder.wifisecurity.setImageResource(R.drawable.discovery_secure_wifi);
            viewHolder.security.setText("WPA2");
            viewHolder.passwordInput.setVisibility(View.VISIBLE);
        } else if (!wifiData.getCapabilities().contains("WPA2") && wifiData.getCapabilities().contains("WPA")) {
            viewHolder.wifisecurity.setImageResource(R.drawable.discovery_insecure_wifi);
            viewHolder.security.setText("WPA");
            viewHolder.passwordInput.setVisibility(View.GONE);
        } else {
            viewHolder.wifisecurity.setImageResource(R.drawable.discovery_insecure_wifi);
            viewHolder.security.setText("NONE");
            viewHolder.passwordInput.setVisibility(View.GONE);
        }
        if (wifiData.getBSSID().equalsIgnoreCase(this.mPreferences.getWifiMac())) {
            viewHolder.txConnectedStatus.setVisibility(View.VISIBLE);
            viewHolder.txConnectedStatus.setText("Connected");
            viewHolder.connectBtn.setVisibility(View.GONE);
            viewHolder.passwordInput.setVisibility(View.GONE);
        } else {
            viewHolder.txConnectedStatus.setVisibility(View.GONE);
            viewHolder.connectBtn.setVisibility(View.VISIBLE);
        }
        viewHolder.txsignalstgth.setText(Integer.toString(wifiData.getRssi()) + "dBm");
        if (wifiData.getRssi() >= -40) {
            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder, rssi, "#29a329");
        } else if (wifiData.getRssi() >= -60 &&
                wifiData.getRssi() < -40) {
            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder, rssi, "#f38624");
        } else {
            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder, rssi, "#e60000");
        }
        viewHolder.bssid.setText(wifiData.getBSSID());
        viewHolder.ssid.setText(wifiData.getSSID());
        viewHolder.channelWidth.setText(getChannelWidthStr(wifiData.getFrequency(), wifiData.getChannelWidth()));
        viewHolder.frequencyLabel.setText(wifiData.getFrequency() + "MHz");
        return convertView;
    }

    private String getChannelWidthStr(int frequency, int channelWidth) {
        int channeWidthNum = 0;
        if (channelWidth == ScanResult.CHANNEL_WIDTH_20MHZ) {
            channeWidthNum = 20;
        } else if (channelWidth == ScanResult.CHANNEL_WIDTH_40MHZ) {
            channeWidthNum = 40;
        } else if (channelWidth == ScanResult.CHANNEL_WIDTH_80MHZ) {
            channeWidthNum = 80;
        } else if (channelWidth == ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ) {
            channeWidthNum = 160;
        }
        return channeWidthNum + "MHz (" + (frequency - channeWidthNum / 2) + " - " + (frequency + channeWidthNum / 2) + "MHz)";
    }

    private void setProgressbar(ViewHolder viewHolder, int rssi, String color) {
        viewHolder.mProgressBar.setProgress(100 + rssi);
        viewHolder.mProgressBar.getProgressDrawable().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        viewHolder.txsignalstgth.setTextColor(Color.parseColor(color));
        viewHolder.detailsLayout.setBackgroundColor(Color.parseColor(color));
    }

    static class ViewHolder {
        public TextView wifiProvider;
        public TextView txsignalstgth;
        public ImageView txlevel;
        public TextView txchannels;
        public ImageView wifisecurity;
        public TextView txConnectedStatus;
        public ProgressBar mProgressBar;
        public View detailsLayout;
        public TextView security;
        public TextView channelWidth;
        public TextView bssid;
        public TextView ssid;
        public TextView frequencyLabel;
        public Button connectBtn;
        public Button cancelBtn;
        public Button forgetBtn;
        public EditText passwordInput;
    }
}