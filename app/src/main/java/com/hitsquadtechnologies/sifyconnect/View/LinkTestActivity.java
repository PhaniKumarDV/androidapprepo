package com.hitsquadtechnologies.sifyconnect.View;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPSetRequest;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.LinkTest;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LinkTestActivity extends BaseActivity {

    SharedPreference mSharedPreference;
    UDPConnection mUDPConnection;
    Button mStart,mStop;
    Spinner mDirection,mDuration;
    TextView mSuMac;
    int mStrDuration = 30;
    int mStrDirection =1;
    LinkTest setPackrt;
    GraphView areaGraph;
    LineGraphSeries<DataPoint> localSeries;
    LineGraphSeries<DataPoint> remoteSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);
        this.onCreate("Link Test", R.id.toolbar, R.id.tools_drawer_layout, R.id.nav_view);
        initi();
    }

    private void initi()
    {
        mStop           = (Button)findViewById(R.id.Link_stopbutton);
        mStart          = (Button)findViewById(R.id.Link_Startbutton);
        mDirection      =(Spinner)findViewById(R.id.Link_Direction);
        mDuration       =(Spinner)findViewById(R.id.Link_duration);
        mSuMac          = (TextView)findViewById(R.id.Link_SuMac);
        mSharedPreference = new SharedPreference(LinkTestActivity.this);

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

           startLinkTestRequest();
           sendGetRequestToServer();
           //sendGetRequest();
        areaGraph           = findViewById(R.id.area_graph);
        initAreaGraph();
        renderAreaGraph();
    }



    private void startLinkTestRequest()
    {
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String macaddress = mSuMac.getText().toString();
                if(!macaddress.isEmpty())
                {
                    mStop.setVisibility(View.VISIBLE);
                    sendSetRequestToServer();
                    timer();
                    mSharedPreference.saveStartOrStop(true);
                    sendGetRequest();
                }else {
                    Toast.makeText(LinkTestActivity.this,R.string.toast,Toast.LENGTH_LONG).show();
                }
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStop.setVisibility(View.GONE);
                mStart.setVisibility(View.VISIBLE);
                mSharedPreference.saveStartOrStop(false);
                stopRequest();
            }
        });
    }


    private void stopRequest(){

        String macaddress = mSuMac.getText().toString();
        if(macaddress != null){
            setPackrt = new LinkTest();
            setPackrt.setMacAddr(macaddress);
        }
        setPackrt.setStartStop(0);
        setPackrt.setDirection(1);
        setPackrt.setDuration(mStrDuration);

        LinkTest setPackrt = new LinkTest(0,macaddress,1,mStrDuration);
        KeywestPacket setLinkTestres = setPackrt.buildPacketFromUI();
        UDPSetRequest setRequest = new UDPSetRequest(mSharedPreference.getIPAddress(),9181,setLinkTestres);
        setRequest.start();
    }
    private void sendSetRequestToServer()
    {
        String macaddress = mSuMac.getText().toString();
        if(macaddress != null){
            setPackrt = new LinkTest();
            setPackrt.setMacAddr(macaddress);
        }else {
            Toast.makeText(LinkTestActivity.this,"Mac address is empty",Toast.LENGTH_LONG).show();
        }
        setPackrt.setStartStop(1);
        setPackrt.setDirection(mStrDirection);
        setPackrt.setDuration(mStrDuration);
        LinkTest setPackrt = new LinkTest(1,macaddress,mStrDirection,mStrDuration);

        KeywestPacket setLinkTestres = setPackrt.buildPacketFromUI();
        UDPSetRequest setRequest = new UDPSetRequest(mSharedPreference.getIPAddress(),9181,setLinkTestres);
        setRequest.start();
    }


    private void sendGetRequest(){

        new CountDownTimer(1000, 1000)
        {
            public void onTick(long millisUntilFinished)
            { }
            public  void onFinish()
            {sendGetRequestToServer();
            }
        }.start();
    }
    private void timer(){
        new CountDownTimer(TimeUnit.SECONDS.toMillis(mStrDuration), 1000)
        {
            public void onTick(long millisUntilFinished)
            {}
            public  void onFinish()
            {
                mSharedPreference.saveStartOrStop(false);
                mStop.setVisibility(View.GONE);
                mStart.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void sendGetRequestToServer()
    {
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
                    /*updateUI(result);*/
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initAreaGraph() {
        areaGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        localSeries = new LineGraphSeries<>(new DataPoint[] {});
        int localSeriesColor = getResources().getColor(R.color.su_line_graph_color);
        localSeries.setTitle("Local");
        localSeries.setColor(localSeriesColor);
        localSeries.setDrawBackground(true);
        localSeries.setBackgroundColor(Color.argb(64, Color.red(localSeriesColor), Color.green(localSeriesColor), Color.blue(localSeriesColor)));
        localSeries.setDrawDataPoints(false);
        remoteSeries = new LineGraphSeries<>(new DataPoint[] {});
        areaGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                return null;
            }
        });
        int remoteSeriesColor = getResources().getColor(R.color.bsu_line_graph_color);
        remoteSeries.setColor(remoteSeriesColor);
        remoteSeries.setDrawBackground(true);
        remoteSeries.setBackgroundColor(Color.argb(64, Color.red(remoteSeriesColor), Color.green(remoteSeriesColor), Color.blue(remoteSeriesColor)));
        remoteSeries.setTitle("Remote");
        remoteSeries.setDrawDataPoints(false);
        LegendRenderer legendRenderer = areaGraph.getLegendRenderer();
        legendRenderer.setVisible(true);
        legendRenderer.setFixedPosition(0, 0);
        legendRenderer.setTextSize(32);
        legendRenderer.setTextColor(Color.parseColor("#000000"));
        legendRenderer.setBackgroundColor(Color.argb(0, 0, 0, 0));
        areaGraph.getViewport().setMaxY(10);
        areaGraph.getViewport().setYAxisBoundsManual(true);
        areaGraph.addSeries(localSeries);
        areaGraph.addSeries(remoteSeries);
    }

    private void renderAreaGraph() {
        localSeries.resetData(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 3),
                new DataPoint(5, 6),
                new DataPoint(6, 7),
                new DataPoint(7, 5),
                new DataPoint(8, 2),
                new DataPoint(9, 3),
                new DataPoint(10, 8)
        });
        remoteSeries.resetData(new DataPoint[] {
                new DataPoint(0, 5),
                new DataPoint(1, 3),
                new DataPoint(2, 4),
                new DataPoint(3, 9),
                new DataPoint(4, 7),
                new DataPoint(5, 6),
                new DataPoint(6, 5),
                new DataPoint(7, 8),
                new DataPoint(8, 3),
                new DataPoint(9, 10),
                new DataPoint(10, 8)
        });
    }

}
