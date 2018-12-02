package com.hitsquadtechnologies.sifyconnect.Adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
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

import java.util.List;


public class WifiscannerAdapter extends ArrayAdapter<wifiDetailsdata> {

    private DiscoveryActivity mContext;
    private List<wifiDetailsdata> wifiDetailsarrayList;
    private String mConnectedWifiSSID;
    private LayoutInflater inflater;
    private View lastShownWifiDetails;

    public WifiscannerAdapter(DiscoveryActivity context, List<wifiDetailsdata> wifiDetailsList, String ConnectedWifissid) {
        super(context, R.layout.list_item, wifiDetailsList);
        mContext = context;
        this.mConnectedWifiSSID = ConnectedWifissid;
        inflater = LayoutInflater.from(context);
        this.wifiDetailsarrayList = wifiDetailsList;
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
            newViewHolder.mProgressBar =(ProgressBar) convertView.findViewById(R.id.progressbar);
            //viewHolder.txlevel = (ImageView) convertView.findViewById(R.id.Tx_wifi_rssi);
            newViewHolder.txchannels = (TextView) convertView.findViewById(R.id.Tx_channel);
            newViewHolder.txConnectedStatus = (TextView) convertView.findViewById(R.id.connectedText);
            newViewHolder.detailsLayout = convertView.findViewById(R.id.wifi_details);
            newViewHolder.security = convertView.findViewById(R.id.securityValue);
            newViewHolder.channelWidth = convertView.findViewById(R.id.channelWidthValue);
            newViewHolder.bssid = convertView.findViewById(R.id.bssidValue);
            newViewHolder.connectBtn = convertView.findViewById(R.id.connectBtn);
            newViewHolder.cancelBtn = convertView.findViewById(R.id.cancelBtn);
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
            viewHolder = newViewHolder;

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(wifiData.getBSSID().equalsIgnoreCase(this.mConnectedWifiSSID))
        {
            viewHolder.txConnectedStatus.setVisibility(View.VISIBLE);
            viewHolder.txConnectedStatus.setText("Connected");
            viewHolder.connectBtn.setVisibility(View.GONE);

        }else {
            viewHolder.txConnectedStatus.setVisibility(View.GONE);
            viewHolder.connectBtn.setVisibility(View.VISIBLE);
        }

        viewHolder.wifiProvider.setText(wifiData.getSSID());


        if(wifiData.getCapabilities().contains("WPA2"))
        {
            viewHolder.wifisecurity.setImageResource(R.drawable.discovery_secure_wifi);
            viewHolder.security.setText("WPA2");
            viewHolder.passwordInput.setVisibility(View.VISIBLE);

        }else if(!wifiData.getCapabilities().contains("WPA2") && wifiData.getCapabilities().contains("WPA"))
        {
            viewHolder.wifisecurity.setImageResource(R.drawable.discovery_insecure_wifi);
            viewHolder.security.setText("WPA");
            viewHolder.passwordInput.setVisibility(View.GONE);
        }else {
            viewHolder.wifisecurity.setImageResource(R.drawable.discovery_insecure_wifi);
            viewHolder.security.setText("NONE");
            viewHolder.passwordInput.setVisibility(View.GONE);
        }

        viewHolder.txsignalstgth.setText(Integer.toString(wifiData.getRssi()) + "dBm");


        if(wifiData.getRssi()>= -40){

            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder,rssi,"#29a329");

        }else if(wifiData.getRssi()  >= -60 &&
                wifiData.getRssi() < -40){

            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder,rssi,"#f38624");

        }else if(wifiData.getRssi() >= -70 &&
                wifiData.getRssi() < -60){

            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder,rssi,"#e60000");

        }
        else if(wifiData.getRssi() >= -80 &&
                wifiData.getRssi() < -70){

            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder,rssi,"#e60000");
        }
        else if(wifiData.getRssi() >= -90 &&
                wifiData.getRssi() < -80){

            int rssi = wifiData.getRssi();
            setProgressbar(viewHolder,rssi,"#e60000");
        }

        viewHolder.bssid.setText(wifiData.getBSSID());
        viewHolder.channelWidth.setText(getChannelWidthStr(wifiData.getFrequency(), wifiData.getChannelWidth()));
        viewHolder.frequencyLabel.setText(wifiData.getFrequency()+ "MHz");


        //viewHolder.mProgressBar.setProgress(wifiData.getRssi());
        /* Code Handling the display of SignalLevel based on RSSI values */
       /* if( wifiData.getRssi() >= -40 ) {
            viewHolder.txlevel.setImageResource(R.drawable.full_signal);
        } else if ( wifiData.getRssi() >= -60 &&
                wifiData.getRssi() < -40) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_4);
        } else if ( wifiData.getRssi() >= -70 &&
            wifiData.getRssi() < -60) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_3);
        } else if ( wifiData.getRssi() >= -80 &&
            wifiData.getRssi() < -70) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_2);
        } else if ( wifiData.getRssi() >= -90 &&
                wifiData.getRssi() < -80) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_1);
        }  else {
            viewHolder.txlevel.setImageResource(R.drawable.no_signal);
        }*/

        //viewHolder.txchannels.setText(new StringBuilder().append(wifiData.getFrequency()).append("").toString());


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
        return channeWidthNum + "MHz (" + (frequency - channeWidthNum / 2 ) + " - " + (frequency + channeWidthNum / 2 ) +  "MHz)";
    }

    private void setProgressbar(ViewHolder viewHolder,int rssi,String color){

        viewHolder.mProgressBar.setProgress(100 + rssi);
        viewHolder.mProgressBar.getProgressDrawable().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        viewHolder.txsignalstgth.setTextColor(Color.parseColor(color));
        viewHolder.detailsLayout.setBackgroundColor(Color.parseColor(color));

    }

    static class ViewHolder{

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
        public TextView frequencyLabel;
        public Button connectBtn;
        public Button cancelBtn;
        public EditText passwordInput;


    }
}
