package com.hitsquadtechnologies.sifyconnect.View;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.hitsquadtechnologies.sifyconnect.Adapters.SharedLinkSpeedGraphData;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.constants.DirectionType;
import com.hitsquadtechnologies.sifyconnect.utils.Options;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;
import com.hsq.kw.packet.vo.LinkTest;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LinkTestActivity extends BaseActivity {
    private static final int MAX_DATA_POINTS = 10;
    RouterService.Subscription mSubscription;
    SharedPreference mSharedPreference;
    Button mStart,mStop;
    Spinner mDirection,mDuration;
    TextView mMacLabel;
    TextView mSuMac;
    TextView localSnrA1;
    TextView localSnrA2;
    TextView localLinkQuality;
    TextView remoteSnrA1;
    TextView remoteSnrA2;
    TextView remoteLinkQuality;
    int mStrDuration = 30;
    int mStrDirection =1;
    GraphView areaGraph;
    Options directionalOptions;
    Options durationOptions = new Options();
    LineGraphSeries<DataPoint> localSeries;
    LineGraphSeries<DataPoint> remoteSeries;
    CountDownTimer stopTestTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_test);
        this.onCreate("Link Test", R.id.toolbar, true);
        mStop           = (Button)findViewById(R.id.Link_stopbutton);
        mStart          = (Button)findViewById(R.id.Link_Startbutton);
        mDirection      = (Spinner)findViewById(R.id.Link_Direction);
        mDuration       = (Spinner)findViewById(R.id.Link_duration);
        mSuMac          = (TextView)findViewById(R.id.Link_SuMac);
        mSharedPreference = new SharedPreference(LinkTestActivity.this);
        mMacLabel       = findViewById(R.id.mac_label);
        localSnrA1      = findViewById(R.id.localA1);
        localSnrA2      = findViewById(R.id.localA2);
        localLinkQuality = findViewById(R.id.localLinkQuality);
        remoteSnrA1     = findViewById(R.id.remoteA1);
        remoteSnrA2     = findViewById(R.id.remoteA2);
        remoteLinkQuality = findViewById(R.id.remoteLinkQuality);
        durationOptions.add(30, "30");
        durationOptions.add(60, "60");
        durationOptions.add(120, "120");
        durationOptions.add(180, "180");
        if(mSharedPreference.getIsTrue()){
            mStop.setVisibility(View.VISIBLE);
            mStart.setVisibility(View.GONE);
        }
        initSpinner(mDuration, durationOptions);
        mDuration.setSelection(durationOptions.findPositionByKey(mSharedPreference.getDuration()));
        initAreaGraph();
    }

   /* @Override
    protected void onDestroy() {
        super.onDestroy();
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                RouterService.getInstance().disconnect();
                RouterService.getInstance().loginFailed();
                showHome();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeywestPacket wirelessLinkPacket = new KeywestPacket((byte)1, (byte)1, (byte)2);
        directionalOptions = new Options();
        if( mSharedPreference.getRadioMode() == 1 ) {
            mMacLabel.setText("SU MAC");
            directionalOptions.add(DirectionType.DOWN_LINK, "Downlink");
        } else {
            directionalOptions.add(DirectionType.UP_LINK, "Uplink");
            mMacLabel.setText("AP MAC");
        }
        directionalOptions.add(DirectionType.BI_DI_LINK, "Bi-di");
        initSpinner(this.mDirection, directionalOptions);
        mDirection.setSelection(directionalOptions.findPositionByKey(mSharedPreference.getDirection()));
        this.mSubscription = RouterService.getInstance().observe(wirelessLinkPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(new KWWirelessLinkStats(packet));
                    }
                });
            }
            @Override
            public void onError(String msg, Exception e) {
                //super.onError(msg, e);
                mStart.setVisibility(View.VISIBLE);
                mStop.setVisibility(View.GONE);
                Log.e(ConfigurationActivity.class.getName(), "Configuration request failed: " + msg, e);
            }
        });
    }

    private void updateUI(KWWirelessLinkStats wirelessLinkStats) {
        if (mSuMac.getText().length() == 0) {
            mSuMac.setText(wirelessLinkStats.getMacAddress());
        }
        SharedLinkSpeedGraphData.INSTANCE.add(wirelessLinkStats.getTxInput(), wirelessLinkStats.getRxInput());
        renderGraph();
        localSnrA1.setText("" + wirelessLinkStats.getLocalSignalA1());
        localSnrA2.setText("" + wirelessLinkStats.getLocalSignalA2());
        localLinkQuality.setText(""+ wirelessLinkStats.getLocalLinkQualityIndex());
        remoteSnrA1.setText("" + wirelessLinkStats.getRemoteSignalA1());
        remoteSnrA2.setText("" + wirelessLinkStats.getRemoteSignalA2());
        remoteLinkQuality.setText(""+wirelessLinkStats.getRemoteLinkQualityIndex());
    }
    private int max(List<Integer> list) {
        int max = 0;
        for (int i : list) {
            max = max > i ? max : i;
        }
        return max;
    }
    private List<Integer> addData(List<Integer> seriesData, int value) {
        if (seriesData.size() > MAX_DATA_POINTS) {
            seriesData = seriesData.subList(seriesData.size() - MAX_DATA_POINTS, seriesData.size());
        }
        seriesData.add(value);
        return seriesData;
    }
    private String seriesData(List<Integer> seriesData) {
        String str = "";
        for (int i = 0; i < seriesData.size(); i++) {
            str += seriesData.get(i) + ",";
        }
        return  str;
    }
    private DataPoint[] toDataPointArray(List<Integer> seriesData) {
        int len = seriesData.size();
        DataPoint[] dataPoints = new DataPoint[MAX_DATA_POINTS];
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            int v = 0;
            if ( i > (MAX_DATA_POINTS - len)) {
                v = seriesData.get(i - (MAX_DATA_POINTS - len));
            }
            dataPoints[i] = new DataPoint(i, v);
        }
        return dataPoints;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mSubscription != null) {
            mSubscription.cancel();
        }
    }
    public void startTest(View view) {
        String macAddress = mSuMac.getText().toString();
        mStrDirection = getSelectedOption(this.mDirection, directionalOptions);
        mStrDuration  = getSelectedOption(this.mDuration, durationOptions);
        mSharedPreference.saveDuractionValues(mStrDuration);
        mSharedPreference.saveDirectionValues(mStrDirection);
        if(!macAddress.isEmpty()) {
            LinkTest linkTestPkt = new LinkTest(1, macAddress, mStrDirection, mStrDuration);
            KeywestPacket keywestPacket = linkTestPkt.buildPacketFromUI();
            RouterService.getInstance().sendRequest(keywestPacket, new RouterService.Callback<KeywestPacket>() {
                @Override
                public void onSuccess(KeywestPacket packet) {}
            });
            mStop.setVisibility(View.VISIBLE);
            mStart.setVisibility(View.GONE);
            stopTestTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(mStrDuration), 1000) {
                public void onTick(long millisUntilFinished) {}
                public  void onFinish() {
                    stopTestTimer = null;
                    stopTest(null);

                    //mStart.setText("Start");
                }
            }.start();
            mSharedPreference.saveStartOrStop(true);
            //mStart.setText("Stop");
        }else {
            Toast.makeText(LinkTestActivity.this,R.string.mac_address_is_empty_toast,Toast.LENGTH_LONG).show();
        }
    }
    public void stopTest(View view){
        String macAddress = mSuMac.getText().toString();
        LinkTest linkTestPkt = new LinkTest(0, macAddress, mStrDirection, mStrDuration);
        KeywestPacket keywestPacket = linkTestPkt.buildPacketFromUI();
        RouterService.getInstance().sendRequest(keywestPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(KeywestPacket packet) {}
        });
        mSharedPreference.saveStartOrStop(false);
        mStop.setVisibility(View.GONE);
        mStart.setVisibility(View.VISIBLE);
        if (stopTestTimer != null) {
            stopTestTimer.cancel();
        }
    }
    private void initAreaGraph() {
        areaGraph = findViewById(R.id.area_graph);
        localSeries = new LineGraphSeries<>(new DataPoint[] {});
        int localSeriesColor = getResources().getColor(R.color.local_line_graph_color);
        localSeries.setTitle("Local (Mbps)");
        localSeries.setColor(localSeriesColor);
        localSeries.setDrawBackground(true);
        localSeries.setBackgroundColor(Color.argb(64, Color.red(localSeriesColor), Color.green(localSeriesColor), Color.blue(localSeriesColor)));
        localSeries.setDrawDataPoints(false);
        remoteSeries = new LineGraphSeries<>(new DataPoint[] {});
        areaGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                return isValueX ? null : Double.valueOf(value).intValue() + "";
            }
        });
        int remoteSeriesColor = getResources().getColor(R.color.remote_line_graph_color);
        remoteSeries.setColor(remoteSeriesColor);
        remoteSeries.setDrawBackground(true);
        remoteSeries.setBackgroundColor(Color.argb(64, Color.red(remoteSeriesColor), Color.green(remoteSeriesColor), Color.blue(remoteSeriesColor)));
        remoteSeries.setTitle("Remote (Mbps)");
        remoteSeries.setDrawDataPoints(false);
        LegendRenderer legendRenderer = areaGraph.getLegendRenderer();
        legendRenderer.setVisible(true);
        legendRenderer.setFixedPosition(0, -20);
        legendRenderer.setTextSize(32);
        legendRenderer.setTextColor(Color.parseColor("#000000"));
        legendRenderer.setBackgroundColor(Color.argb(0, 0, 0, 0));
        areaGraph.getViewport().setMinY(0);
        areaGraph.getViewport().setMaxY(200);
        areaGraph.getViewport().setMinX(0);
        areaGraph.getViewport().setMaxX(MAX_DATA_POINTS + 1);
        areaGraph.getViewport().setYAxisBoundsManual(true);
        areaGraph.addSeries(localSeries);
        areaGraph.addSeries(remoteSeries);
        renderGraph();
    }
    public void renderGraph() {
        int maxValue = Double.valueOf(SharedLinkSpeedGraphData.INSTANCE.max() * 1.25).intValue();
        areaGraph.getViewport().setMaxY(Math.max(maxValue, 10));
        localSeries.resetData(SharedLinkSpeedGraphData.INSTANCE.getLocalData());
        remoteSeries.resetData(SharedLinkSpeedGraphData.INSTANCE.getRemoteData());
    }
    /* Redirect to the Home Activity */
    public void showHome() {
        this.startActivity(new Intent(this, HomeActivity.class));
        this.finish();
    }
}