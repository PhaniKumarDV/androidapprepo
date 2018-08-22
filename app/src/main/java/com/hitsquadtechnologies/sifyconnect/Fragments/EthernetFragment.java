package com.hitsquadtechnologies.sifyconnect.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.View.StaticsActivity;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KeywestEthernetStats;
import com.hsq.kw.packet.vo.KeywestWirelessStats;

public class EthernetFragment extends Fragment {

    UDPConnection mUDPConnection;
    SharedPreference mSharedPreference;
    TextView mTxTotalpackets,mRXTotalPackets;
    TextView mTxErrors,mRxErrors;
    TextView mTXTotalBytes,mRxTotalbytes;

    @Override
    public void onAttach(Context context ) {
        super.onAttach(context);

        mSharedPreference   = new SharedPreference(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        requestToServer();

        View view = inflater.inflate(R.layout.ethernet, container, false);

        mTxTotalpackets = (TextView)view.findViewById(R.id.Tx_TotalPackets);
        mRXTotalPackets = (TextView)view.findViewById(R.id.Rx_TotalPackets);
        mTXTotalBytes = (TextView)view.findViewById(R.id.Tx_TotalBytes);
        mRxTotalbytes = (TextView)view.findViewById(R.id.Rx_TotalBytes);
        mRxErrors = (TextView)view.findViewById(R.id.RxErrors);
        mTxErrors= (TextView)view.findViewById(R.id.TxErrors);

        return view;
    }

    private void requestToServer()
    {

        KeywestPacket wirelessStatsPacket = new KeywestPacket((byte)1, (byte)1, (byte)3);
        mUDPConnection = new UDPConnection(mSharedPreference.getIPAddress(), 9181,wirelessStatsPacket,new ResponseListener());
        mUDPConnection.start();
    }

    class ResponseListener implements TaskCompleted {

        @Override
        public void onTaskComplete(final KeywestPacket result) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI(result);
                }
            });

        }

        @Override
        public void endServer(String result) {

        }

        @Override
        public void responce(KeywestPacket responce) {

        }

    }

    public void updateUI(KeywestPacket packet){

        requestToServer();
        KeywestEthernetStats keywestEthernetStats = new KeywestEthernetStats(packet);
        mTxTotalpackets.setText(keywestEthernetStats.getTxPkts());
        mRXTotalPackets.setText(keywestEthernetStats.getRxErrors());
        mTXTotalBytes.setText(keywestEthernetStats.getTxBytes());
        mRxTotalbytes.setText(keywestEthernetStats.getRxBytes());
        mRxErrors.setText(keywestEthernetStats.getRxErrors());
        mTxErrors.setText(keywestEthernetStats.getTxErrors());

    }

}
