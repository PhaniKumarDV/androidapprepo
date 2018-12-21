package com.hitsquadtechnologies.sifyconnect.View;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;

public class AlignmentActivity extends BaseActivity {

    UDPConnection mUDPConnection;
    SharedPreference mSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment);
        this.onCreate("Alignment", R.id.toolbar, true);
        AlignInit();
    }


    private void AlignInit() {
        mSharedPreference = new SharedPreference(AlignmentActivity.this);
        renderSignal(this, (LinearLayout) findViewById(R.id.signalLinesLayout), 3);
        requestToServer();
    }

    public void renderSignal(Activity a, LinearLayout v, int strength) {
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );
        v.removeAllViews();
        for(int i = 0; i < strength; i++) {
            ImageView imageView = new ImageView(a);
            imageView.setImageDrawable(a.getResources().getDrawable(R.drawable.signal_line));
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            v.addView(imageView);
        }
    }

    private void requestToServer() {
        KeywestPacket wirelessLinkPacket = new KeywestPacket((byte)1, (byte)1, (byte)2);
        mUDPConnection   = new UDPConnection(mSharedPreference.getIPAddress(), 9181,wirelessLinkPacket,new ResponseListener());
        mUDPConnection.start();
    }


    class ResponseListener implements TaskCompleted {

        @Override
        public void onTaskComplete(final KeywestPacket result) {

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(KeywestPacket testPacket) {

        KWWirelessLinkStats wirelessLinkStats = new KWWirelessLinkStats(testPacket);
    }
}