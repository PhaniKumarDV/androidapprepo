package com.hitsquadtechnologies.sifyconnect.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitsquadtechnologies.sifyconnect.Model.wifiDetailsdata;
import com.hitsquadtechnologies.sifyconnect.R;

import java.util.List;


public class WifiscannerAdapter extends ArrayAdapter<wifiDetailsdata> {

    private Context mContext;
    private List<wifiDetailsdata> wifiDetailsarrayList;
    private String mConnectedWifiSSID;
    private LayoutInflater inflater;

    public WifiscannerAdapter(Context context, List<wifiDetailsdata> wifiDetailsList, String ConnectedWifissid) {
        super(context, R.layout.list_item, wifiDetailsList);
        mContext = context;
        this.mConnectedWifiSSID = ConnectedWifissid;
        inflater = LayoutInflater.from(context);
        this.wifiDetailsarrayList = wifiDetailsList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

         ViewHolder viewHolder;
            if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.wifisecurity = (ImageView) convertView.findViewById(R.id.wifi_security);
            viewHolder.wifiProvider = (TextView) convertView.findViewById(R.id.wifi_name);
            viewHolder.txsignalstgth = (TextView) convertView.findViewById(R.id.signal_strength);
            viewHolder.mProgressBar =(ProgressBar) convertView.findViewById(R.id.progressbar);
            //viewHolder.txlevel = (ImageView) convertView.findViewById(R.id.Tx_wifi_rssi);
            viewHolder.txchannels = (TextView) convertView.findViewById(R.id.Tx_channel);
            viewHolder.txConnectedStatus = (TextView) convertView.findViewById(R.id.connectedText);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(wifiDetailsarrayList.get(position).getBSSID().equalsIgnoreCase(this.mConnectedWifiSSID))
        {
            viewHolder.txConnectedStatus.setVisibility(View.VISIBLE);
            viewHolder.txConnectedStatus.setText("Connected");

        }else {
            viewHolder.txConnectedStatus.setVisibility(View.GONE);
        }

        viewHolder.wifiProvider.setText(wifiDetailsarrayList.get(position).getSSID());


        if(wifiDetailsarrayList.get(position).getCapabilities().contains("WPA2"))
        {
            viewHolder.wifisecurity.setImageResource(R.drawable.wl);

        }else if(!wifiDetailsarrayList.get(position).getCapabilities().contains("WPA2") && wifiDetailsarrayList.get(position).getCapabilities().contains("WPA"))
        {
            viewHolder.wifisecurity.setImageResource(R.drawable.wl);
        }else {
            viewHolder.wifisecurity.setImageResource(R.drawable.w);
        }

        viewHolder.txsignalstgth.setText(Integer.toString(wifiDetailsarrayList.get(position).getRssi()));


        if(wifiDetailsarrayList.get(position).getRssi()>= -40){

            int rssi = wifiDetailsarrayList.get(position).getRssi();
            setProgressbar(viewHolder,rssi,"#29a329");

        }else if(wifiDetailsarrayList.get(position).getRssi()  >= -60 &&
                wifiDetailsarrayList.get(position).getRssi() < -40){

            int rssi = wifiDetailsarrayList.get(position).getRssi();
            setProgressbar(viewHolder,rssi,"#f38624");

        }else if(wifiDetailsarrayList.get(position).getRssi() >= -70 &&
                wifiDetailsarrayList.get(position).getRssi() < -60){

            int rssi = wifiDetailsarrayList.get(position).getRssi();
            setProgressbar(viewHolder,rssi,"#e60000");

        }
        else if(wifiDetailsarrayList.get(position).getRssi() >= -80 &&
                wifiDetailsarrayList.get(position).getRssi() < -70){

            int rssi = wifiDetailsarrayList.get(position).getRssi();
            setProgressbar(viewHolder,rssi,"#e60000");
        }
        else if(wifiDetailsarrayList.get(position).getRssi() >= -90 &&
                wifiDetailsarrayList.get(position).getRssi() < -80){

            int rssi = wifiDetailsarrayList.get(position).getRssi();
            setProgressbar(viewHolder,rssi,"#e60000");
        }


        //viewHolder.mProgressBar.setProgress(wifiDetailsarrayList.get(position).getRssi());
        /* Code Handling the display of SignalLevel based on RSSI values */
       /* if( wifiDetailsarrayList.get(position).getRssi() >= -40 ) {
            viewHolder.txlevel.setImageResource(R.drawable.full_signal);
        } else if ( wifiDetailsarrayList.get(position).getRssi() >= -60 &&
                wifiDetailsarrayList.get(position).getRssi() < -40) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_4);
        } else if ( wifiDetailsarrayList.get(position).getRssi() >= -70 &&
            wifiDetailsarrayList.get(position).getRssi() < -60) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_3);
        } else if ( wifiDetailsarrayList.get(position).getRssi() >= -80 &&
            wifiDetailsarrayList.get(position).getRssi() < -70) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_2);
        } else if ( wifiDetailsarrayList.get(position).getRssi() >= -90 &&
                wifiDetailsarrayList.get(position).getRssi() < -80) {
            viewHolder.txlevel.setImageResource(R.drawable.signal_1);
        }  else {
            viewHolder.txlevel.setImageResource(R.drawable.no_signal);
        }*/

        viewHolder.txchannels.setText(new StringBuilder().append(wifiDetailsarrayList.get(position).getFrequency()).append("").toString());


        return convertView;
    }

    private void setProgressbar(ViewHolder viewHolder,int rssi,String color){

        viewHolder.mProgressBar.setProgress(rssi);
        viewHolder.mProgressBar.getProgressDrawable().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        viewHolder.txsignalstgth.setTextColor(Color.parseColor(color));

    }

    static class ViewHolder{

        public TextView wifiProvider;
        public TextView txsignalstgth;
        public ImageView txlevel;
        public TextView txchannels;
        public ImageView wifisecurity;
        public TextView txConnectedStatus;
        public ProgressBar mProgressBar;


    }
}
