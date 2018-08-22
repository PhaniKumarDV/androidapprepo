package com.hitsquadtechnologies.sifyconnect.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.View.StaticsActivity;
import com.hitsquadtechnologies.sifyconnect.View.SummaryActivity;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;
import com.hsq.kw.packet.vo.KeywestWirelessStats;

import java.util.ArrayList;

public class WirelessFragment extends Fragment {

    UDPConnection mUDPConnection;
    SharedPreference mSharedPreference;
    TextView mTxDatapackets,mRxDatapackets;
    TextView mTxManagementspackets,mRxManagementspackets;
    TextView mMpduErrors,mPhyErrors;

    @Override
    public void onAttach(Context context ) {
        super.onAttach(context);

        mSharedPreference   = new SharedPreference(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        requestToServer();

        View view = inflater.inflate(R.layout.wireless, container, false);

        mTxDatapackets = (TextView)view.findViewById(R.id.TxDatapackets);
        mRxDatapackets = (TextView)view.findViewById(R.id.RxDatapackets);
        mTxManagementspackets = (TextView)view.findViewById(R.id.TxManagementspackets);
        mRxManagementspackets = (TextView)view.findViewById(R.id.RxManagementspackets);
        mMpduErrors = (TextView)view.findViewById(R.id.MpduErrors);
        mPhyErrors = (TextView)view.findViewById(R.id.PhyErrors);

        return view;

    }


    private void requestToServer()
    {
        KeywestPacket wirelessStatsPacket = new KeywestPacket((byte)1, (byte)1, (byte)5);
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
        private void updateUI(KeywestPacket packet) {

            KeywestWirelessStats keywestWirelessStats = new KeywestWirelessStats(packet);

            requestToServer();
            mTxDatapackets.setText(keywestWirelessStats.getTxDataPkts());
            mRxDatapackets.setText(keywestWirelessStats.getRxDataPkts());
            mTxManagementspackets.setText(keywestWirelessStats.getTxMgmtDataPkts());
            mRxManagementspackets.setText(keywestWirelessStats.getRxMgmtDataPkts());
            mMpduErrors.setText(keywestWirelessStats.getMpduErrors());
            mPhyErrors.setText(keywestWirelessStats.getPhyErrors());

        }

    }

 }
