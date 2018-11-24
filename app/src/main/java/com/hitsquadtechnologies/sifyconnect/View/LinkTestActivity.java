package com.hitsquadtechnologies.sifyconnect.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPSetRequest;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;
import com.hsq.kw.packet.vo.LinkTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class LinkTestActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreference mSharedPreference;
    private XYPlot plot;
    ArrayList<Integer> series1Numbers1 = new ArrayList<>();
    ArrayList<Integer> series1Numbers2 = new ArrayList<>();
    XYSeries series1;
    XYSeries series2;
    UDPConnection mUDPConnection;
    Button mStart,mStop;
    Spinner mDirection,mDuration;
    TextView mSuMac,mLocalMode,mRemoteMode;
    ProgressBar mProgressBar01,mProgressBar02,mProgressBar03,mProgressBar04,mProgressBar05;
    ProgressBar mProgressBar11,mProgressBar12,mProgressBar13,mProgressBar14,mProgressBar15;
    ProgressBar mProgressBar21,mProgressBar22,mProgressBar23,mProgressBar24,mProgressBar25;
    ProgressBar mProgressBar31,mProgressBar32,mProgressBar33,mProgressBar34,mProgressBar35;
    TextView mLocalSNR1,mLocalSNR2;
    TextView mRemoteSNR1,mRemoteSNR2;
    TextView mLocalDataRate,mRemoteDataRate;
    String strMac;
    int mStrDuration = 30;
    int mStrDirection =1;
    LinkTest setPackrt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Link Test");

        initi();
        plot = (XYPlot) findViewById(R.id.plot);
        addvaluesToarray();
        navigationView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.tools_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu m = navigationView.getMenu();

    }

    private void addvaluesToarray()
    {
      for(int i =0; i<10;i++)
       {
         int  n =0;
         series1Numbers1.add(n);
         int  m = 0;
         series1Numbers2.add(m);
       }
        addvaluesToGraph();
    }

    private void initi()
    {
        mProgressBar01  = (ProgressBar)findViewById(R.id.progressBar01);
        mProgressBar02  = (ProgressBar)findViewById(R.id.progressBar02);
        mProgressBar03  = (ProgressBar)findViewById(R.id.progressBar03);
        mProgressBar04  = (ProgressBar)findViewById(R.id.progressBar04);
        mProgressBar05  = (ProgressBar)findViewById(R.id.progressBar05);
        mProgressBar11  = (ProgressBar)findViewById(R.id.progressBar11);
        mProgressBar12  = (ProgressBar)findViewById(R.id.progressBar12);
        mProgressBar13  = (ProgressBar)findViewById(R.id.progressBar13);
        mProgressBar14  = (ProgressBar)findViewById(R.id.progressBar14);
        mProgressBar15  = (ProgressBar)findViewById(R.id.progressBar15);
        mProgressBar21  = (ProgressBar)findViewById(R.id.progressBar21);
        mProgressBar22  = (ProgressBar)findViewById(R.id.progressBar22);
        mProgressBar23  = (ProgressBar)findViewById(R.id.progressBar23);
        mProgressBar24  = (ProgressBar)findViewById(R.id.progressBar24);
        mProgressBar25  = (ProgressBar)findViewById(R.id.progressBar25);
        mProgressBar31  = (ProgressBar)findViewById(R.id.progressBar31);
        mProgressBar32  = (ProgressBar)findViewById(R.id.progressBar32);
        mProgressBar33  = (ProgressBar)findViewById(R.id.progressBar33);
        mProgressBar34  = (ProgressBar)findViewById(R.id.progressBar34);
        mProgressBar35  = (ProgressBar)findViewById(R.id.progressBar35);
        mLocalSNR1      = (TextView)findViewById(R.id.LocalSNR1);
        mLocalSNR2      = (TextView)findViewById(R.id.LocalSNR2);
        mRemoteSNR1     = (TextView)findViewById(R.id.RemoteSNR1);
        mRemoteSNR2     = (TextView)findViewById(R.id.RemoteSNR2);
        mLocalDataRate  = (TextView)findViewById(R.id.Local_datarate);
        mRemoteDataRate = (TextView)findViewById(R.id.Remote_datarate);
        mStop           = (Button)findViewById(R.id.Link_stopbutton);
        mStart          = (Button)findViewById(R.id.Link_Startbutton);
        mDirection      =(Spinner)findViewById(R.id.Link_Direction);
        mDuration       =(Spinner)findViewById(R.id.Link_duration);
        mSuMac          = (TextView)findViewById(R.id.Link_SuMac);
        mLocalMode      = (TextView)findViewById(R.id.mLinktest_Localmode);
        mRemoteMode     = (TextView)findViewById(R.id.mLinktest_Remotemode);
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

    private void navigationView()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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



    private void addvaluesToGraph()
    {
        series1 = new SimpleXYSeries(series1Numbers1, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        series2 = new SimpleXYSeries(series1Numbers2, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.parseColor("#422551"),Color.parseColor("#422551"), Shader.TileMode.MIRROR));

        Paint lineFill2 = new Paint();
        lineFill2.setAlpha(200);
        lineFill2.setShader(new LinearGradient(0, 0, 0, 250, Color.parseColor("#8BF412"),Color.parseColor("#8BF412"), Shader.TileMode.MIRROR));

        LineAndPointFormatter series1Format = new LineAndPointFormatter(this, R.xml.line_point_formatter);
        series1Format.getLinePaint().setColor(Color.parseColor("#422551"));
        //series1Format.setFillPaint(lineFill);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(this, R.xml.line_point_formatter);
        series2Format.getLinePaint().setColor(Color.parseColor("#b01d5b"));
        //series2Format.setFillPaint(lineFill2);

        int upperBoundery = 15;
        int x = Collections.max(series1Numbers1);
        int y = Collections.max(series1Numbers2);
        if(x>y){
            upperBoundery = upperBoundery+x;
        }else {
            upperBoundery = upperBoundery+y;
        }

        plot.setRangeBoundaries(0, upperBoundery, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 9, BoundaryMode.FIXED);
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);

        series1Format.getPointLabelFormatter().getTextPaint().setColor(Color.RED);
        series2Format.getPointLabelFormatter().getTextPaint().setColor(Color.RED);
        plot.redraw();

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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_discovery)
        {
            Intent intent = new Intent(LinkTestActivity.this,DiscoveryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_configuration)
        {
            Intent intent = new Intent(LinkTestActivity.this,ConfigurationActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_summary)
        {
            Intent intent = new Intent(LinkTestActivity.this,SummaryActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_Alignment)
        {
            Intent intent = new Intent(LinkTestActivity.this,AlignmentActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_linktest)
        {
            Intent intent = new Intent(LinkTestActivity.this,LinkTestActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_wireless)
        {
            Intent intent = new Intent(LinkTestActivity.this,StaticsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout)
        {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.tools_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout()
    {
        Intent intent = new Intent(LinkTestActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(KeywestPacket testPacket)
    {
        KWWirelessLinkStats wirelessLinkStats = new KWWirelessLinkStats(testPacket);

        if( mSharedPreference.getRadioMode() == 1 ) {
            mLocalMode.setText("BSU");
            mRemoteMode.setText("SU");
        } else {
            mLocalMode.setText("SU");
            mRemoteMode.setText("BSU");
        }
        sendGetRequest();
        strMac = wirelessLinkStats.getMacAddress();
        mSuMac.setText(strMac);
        mLocalSNR1.setText(Integer.toString(wirelessLinkStats.getLocalSNRA1()));
        mLocalSNR2.setText(Integer.toString(wirelessLinkStats.getLocalSNRA2()));
        mRemoteSNR1.setText(Integer.toString(wirelessLinkStats.getRemoteSNRA1()));
        mRemoteSNR2.setText(Integer.toString(wirelessLinkStats.getRemoteSNRA2()));
        mLocalDataRate.setText(Integer.toString(wirelessLinkStats.getLocalRate())+ " Mbps");
        mRemoteDataRate.setText(Integer.toString(wirelessLinkStats.getRemoteRate())+ " Mbps");

        setSNRToProgressbars(wirelessLinkStats.getLocalSNRA1(),mProgressBar01,mProgressBar02,mProgressBar03,mProgressBar04,mProgressBar05);
        setSNRToProgressbars(wirelessLinkStats.getRemoteSNRA1(),mProgressBar11,mProgressBar12,mProgressBar13,mProgressBar14,mProgressBar15);
        setSNRToProgressbars(wirelessLinkStats.getLocalSNRA2(),mProgressBar21,mProgressBar22,mProgressBar23,mProgressBar24,mProgressBar25);
        setSNRToProgressbars(wirelessLinkStats.getRemoteSNRA2(),mProgressBar31,mProgressBar32,mProgressBar33,mProgressBar34,mProgressBar35);

        if(series1Numbers1.size()>0)
        {
            series1Numbers1.remove(0);
            series1Numbers2.remove(0);
            plot.clear();
        }
        series1Numbers1.add(wirelessLinkStats.getTxInput());
        series1Numbers2.add(wirelessLinkStats.getRxInput());
        addvaluesToGraph();


    }

    private void setSNRToProgressbars(int progress, ProgressBar p1, ProgressBar p2, ProgressBar p3, ProgressBar p4, ProgressBar p5) {

        if(progress < 25){
            p1.setProgressDrawable( getResources().getDrawable(R.drawable.lowsignal_progressbar));
            p2.setProgressDrawable( getResources().getDrawable(R.drawable.lowsignal_progressbar));
        }else if(progress > 25 && progress <= 35){
            p1.setProgressDrawable( getResources().getDrawable(R.drawable.averagesignal_progressbar));
            p2.setProgressDrawable( getResources().getDrawable(R.drawable.averagesignal_progressbar));
        }else {
            p1.setProgressDrawable( getResources().getDrawable(R.drawable.myprogressbar));
            p2.setProgressDrawable( getResources().getDrawable(R.drawable.myprogressbar));
            p3.setProgressDrawable( getResources().getDrawable(R.drawable.myprogressbar));
            p4.setProgressDrawable( getResources().getDrawable(R.drawable.myprogressbar));
            p5.setProgressDrawable( getResources().getDrawable(R.drawable.myprogressbar));
        }
        int rem =  progress % 20;
        progress = progress / 20;

        if( progress == 0 ){ p1.setProgress(rem); }
        if( progress == 1 ){ p2.setProgress(rem); }
        if( progress == 2 ){ p3.setProgress(rem); }
        if( progress == 3 ){ p4.setProgress(rem); }
        if( progress == 4 ){ p5.setProgress(rem); }

        for ( int i = 0; i < progress; i++ ) {
            if( i == 0 ){ p1.setProgress(20); }
            if( i == 1 ){ p2.setProgress(20); }
            if( i == 2 ){ p3.setProgress(20); }
            if( i == 3 ){ p4.setProgress(20); }
            if( i == 4 ){ p5.setProgress(20); }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.tools_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
