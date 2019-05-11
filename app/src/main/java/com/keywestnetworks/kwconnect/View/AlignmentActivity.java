package com.keywestnetworks.kwconnect.View;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hsq.kw.packet.vo.Configuration;
import com.keywestnetworks.kwconnect.Adapters.AntennaAdapter;
import com.keywestnetworks.kwconnect.Model.AntennaSignal;
import com.keywestnetworks.kwconnect.R;
import com.keywestnetworks.kwconnect.ServerPrograms.RouterService;
import com.keywestnetworks.kwconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;

import java.util.LinkedList;
import java.util.List;

/* This class implements the Alignment Activity which helps
   the installer to show the current and best SNR values */

public class AlignmentActivity extends BaseActivity {
    RouterService.Subscription mSubscription;
    TextView registerLabel;
    AntennaSignal localA1;
    AntennaSignal localA2;
    AntennaSignal remoteA1;
    AntennaSignal remoteA2;
    String localRadio;
    String remoteRadio;
    ListView antennaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment);
        this.onCreate("Alignment", R.id.toolbar, true);
        registerLabel = findViewById(R.id.register_label);
        antennaList = findViewById(R.id.antenna_list);
        mSharedPreference = new SharedPreference(this);
        if (mSharedPreference.getRadioMode() == 1) {
            localRadio = "AP";
            remoteRadio = "SU";
        } else {
            localRadio = "SU";
            remoteRadio = "AP";
        }
        alignmentActivityInit();

    }

    private void alignmentActivityInit() {
        mSharedPreference = new SharedPreference(AlignmentActivity.this);
    }

    /* not required for this release */
    public void renderSignal(Activity a, LinearLayout v, int strength) {
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );
        v.removeAllViews();
        for (int i = 0; i < strength; i++) {
            ImageView imageView = new ImageView(a);
            imageView.setImageDrawable(a.getResources().getDrawable(R.drawable.signal_line));
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            v.addView(imageView);
        }
    }

    private void requestToServer() {
        KeywestPacket wirelessLinkPacket = new KeywestPacket((byte) 1, (byte) 1, (byte) 2);
        mSubscription = RouterService.getInstance().observe(wirelessLinkPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(new KWWirelessLinkStats(packet));
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSubscription != null) {
            mSubscription.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestToServer();
    }

    @Override
    protected void updateUI(Configuration mConfiguration) {

    }

    @SuppressLint("SetTextI18n")
    private void updateUI(KWWirelessLinkStats wirelessLinkStats) {
        if (wirelessLinkStats.getNoOfLinks() > 0) {
            registerLabel.setText("Registered");
        } else {
            registerLabel.setText("Unregistered");
        }
        if (localA1 == null) {
            localA1 = new AntennaSignal(localRadio, "A1", R.drawable.summary_bg1, R.drawable.summary_bg6);
        }
        if (localA2 == null) {
            localA2 = new AntennaSignal(localRadio, "A2", R.drawable.summary_bg1, R.drawable.summary_bg6);
        }
        if (remoteA1 == null) {
            remoteA1 = new AntennaSignal(remoteRadio, "A1", R.drawable.summary_bg3, R.drawable.summary_bg5);
        }
        if (remoteA2 == null) {
            remoteA2 = new AntennaSignal(remoteRadio, "A2", R.drawable.summary_bg3, R.drawable.summary_bg5);
        }
        localA1.setCurrent(wirelessLinkStats.getLocalSignalA1());
        localA2.setCurrent(wirelessLinkStats.getLocalSignalA2());
        remoteA1.setCurrent(wirelessLinkStats.getRemoteSignalA1());
        remoteA2.setCurrent(wirelessLinkStats.getRemoteSignalA2());
        List<AntennaSignal> list = new LinkedList<>();
        list.add(localA1);
        list.add(localA2);
        list.add(remoteA1);
        list.add(remoteA2);
        AntennaAdapter adapter = new AntennaAdapter(this, list);
        antennaList.setAdapter(adapter);
    }
}