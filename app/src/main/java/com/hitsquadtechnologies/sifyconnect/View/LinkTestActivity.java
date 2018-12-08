package com.hitsquadtechnologies.sifyconnect.View;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;
import com.hsq.kw.packet.vo.LinkTest;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
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
    LineGraphSeries<DataPoint> localSeries;
    LineGraphSeries<DataPoint> remoteSeries;
    List<Integer> localSeriesData = new ArrayList<>();
    List<Integer> remoteSeriesData = new ArrayList<>();

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

        mDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null)
                {
                    mSharedPreference.saveDuractionValues(position);
                    mStrDuration = Integer.parseInt((item.toString()));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null)
                {
                    mSharedPreference.saveDirectionValues(position);
                    mStrDirection = position+1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mDuration.setSelection(mSharedPreference.getDuration());
        mDirection.setSelection(mSharedPreference.getDirection());

        if(mSharedPreference.getIsTrue()){
            mStop.setVisibility(View.VISIBLE);
            mStart.setVisibility(View.GONE);
        }
        initAreaGraph();
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeywestPacket wirelessLinkPacket = new KeywestPacket((byte)1, (byte)1, (byte)2);
        if( mSharedPreference.getRadioMode() == 1 ) {
            mMacLabel.setText("SU MAC");
        } else {
            mMacLabel.setText("BSU MAC");
        }
        this.mSubscription = RouterService.INSTANCE.observe(wirelessLinkPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(KeywestPacket response) {
                KWWirelessLinkStats wirelessLinkStats = new KWWirelessLinkStats(response);
                if (mSuMac.getText().length() == 0) {
                    mSuMac.setText(wirelessLinkStats.getMacAddress());
                }
                localSeriesData = addData(localSeriesData, wirelessLinkStats.getTxInput());
                remoteSeriesData = addData(remoteSeriesData, wirelessLinkStats.getRxInput());
                int maxValue = Double.valueOf(Math.max(max(localSeriesData), max(remoteSeriesData)) * 1.25).intValue();
                areaGraph.getViewport().setMaxY(Math.max(maxValue, 10));
                localSeries.resetData(toDataPointArray(localSeriesData));
                remoteSeries.resetData(toDataPointArray(remoteSeriesData));
                localSnrA1.setText("" + wirelessLinkStats.getLocalSNRA1());
                localSnrA2.setText("" + wirelessLinkStats.getLocalSNRA2());
                localLinkQuality.setText("5");
                remoteSnrA1.setText("" + wirelessLinkStats.getRemoteSNRA1());
                remoteSnrA2.setText("" + wirelessLinkStats.getRemoteSNRA2());
                remoteLinkQuality.setText("8");
            }
        });
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
        if(!macAddress.isEmpty()) {
            LinkTest linkTestPkt = new LinkTest(1, macAddress, mStrDirection, mStrDuration);
            KeywestPacket keywestPacket = linkTestPkt.buildPacketFromUI();
            RouterService.INSTANCE.sendRequest(keywestPacket, new RouterService.Callback<KeywestPacket>() {
                @Override
                public void onSuccess(KeywestPacket packet) {}
            });
            mStop.setVisibility(View.VISIBLE);
            new CountDownTimer(TimeUnit.SECONDS.toMillis(mStrDuration), 1000) {
                public void onTick(long millisUntilFinished) {}
                public  void onFinish() {
                    stopTest(null);
                }
            }.start();
            mSharedPreference.saveStartOrStop(true);
        }else {
            Toast.makeText(LinkTestActivity.this,R.string.mac_address_is_empty_toast,Toast.LENGTH_LONG).show();
        }
    }

    public void stopTest(View view){
        String macAddress = mSuMac.getText().toString();
        LinkTest linkTestPkt = new LinkTest(0, macAddress, mStrDirection, mStrDuration);
        KeywestPacket keywestPacket = linkTestPkt.buildPacketFromUI();
        RouterService.INSTANCE.sendRequest(keywestPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(KeywestPacket packet) {}
        });
        mSharedPreference.saveStartOrStop(false);
        mStop.setVisibility(View.GONE);
        mStart.setVisibility(View.VISIBLE);
    }

    private void initAreaGraph() {
        areaGraph = findViewById(R.id.area_graph);
        localSeries = new LineGraphSeries<>(new DataPoint[] {});
        int localSeriesColor = getResources().getColor(R.color.local_line_graph_color);
        localSeries.setTitle("Local");
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
        remoteSeries.setTitle("Remote");
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
    }
}
