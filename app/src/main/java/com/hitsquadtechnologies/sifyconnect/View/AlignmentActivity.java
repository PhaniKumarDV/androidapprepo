package com.hitsquadtechnologies.sifyconnect.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.utils.CustomTypefaceSpan;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.KWWirelessLinkStats;

public class AlignmentActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    UDPConnection mUDPConnection;
    ProgressBar mProgressBar01,mProgressBar02,mProgressBar03,mProgressBar04,mProgressBar05;
    ProgressBar mProgressBar11,mProgressBar12,mProgressBar13,mProgressBar14,mProgressBar15;
    ProgressBar mProgressBar21,mProgressBar22,mProgressBar23,mProgressBar24,mProgressBar25;
    ProgressBar mProgressBar31,mProgressBar32,mProgressBar33,mProgressBar34,mProgressBar35;
    TextView mLocalSNR1,mLocalSNR2,mLocal;
    TextView mRemoteSNR1,mRemoteSNR2,mRemote;
    SharedPreference mSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagonise);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Alignment");
        AlignInit();
        navigationView();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.diagonise_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/calibri.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void AlignInit() {
        mProgressBar01 = (ProgressBar)findViewById(R.id.progressBar01);
        mProgressBar02 = (ProgressBar)findViewById(R.id.progressBar02);
        mProgressBar03 = (ProgressBar)findViewById(R.id.progressBar03);
        mProgressBar04 = (ProgressBar)findViewById(R.id.progressBar04);
        mProgressBar05 = (ProgressBar)findViewById(R.id.progressBar05);
        mProgressBar11 = (ProgressBar)findViewById(R.id.progressBar11);
        mProgressBar12 = (ProgressBar)findViewById(R.id.progressBar12);
        mProgressBar13 = (ProgressBar)findViewById(R.id.progressBar13);
        mProgressBar14 = (ProgressBar)findViewById(R.id.progressBar14);
        mProgressBar15 = (ProgressBar)findViewById(R.id.progressBar15);
        mProgressBar21 = (ProgressBar)findViewById(R.id.progressBar21);
        mProgressBar22 = (ProgressBar)findViewById(R.id.progressBar22);
        mProgressBar23 = (ProgressBar)findViewById(R.id.progressBar23);
        mProgressBar24 = (ProgressBar)findViewById(R.id.progressBar24);
        mProgressBar25 = (ProgressBar)findViewById(R.id.progressBar25);
        mProgressBar31 = (ProgressBar)findViewById(R.id.progressBar31);
        mProgressBar32 = (ProgressBar)findViewById(R.id.progressBar32);
        mProgressBar33 = (ProgressBar)findViewById(R.id.progressBar33);
        mProgressBar34 = (ProgressBar)findViewById(R.id.progressBar34);
        mProgressBar35 = (ProgressBar)findViewById(R.id.progressBar35);
        mLocalSNR1     = (TextView)findViewById(R.id.LocalSNR1);
        mLocalSNR2     = (TextView)findViewById(R.id.LocalSNR2);
        mRemoteSNR1    = (TextView)findViewById(R.id.RemoteSNR1);
        mRemoteSNR2    = (TextView)findViewById(R.id.RemoteSNR2);
        mLocal         = (TextView)findViewById(R.id.localsnr);
        mRemote        = (TextView)findViewById(R.id.remotesnr);

        mSharedPreference = new SharedPreference(AlignmentActivity.this);
        requestToServer();
    }

    private void navigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_discovery) {
            Intent intent = new Intent(AlignmentActivity.this,DiscoveryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_configuration) {
            Intent intent = new Intent(AlignmentActivity.this,ConfigurationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_summary) {
            Intent intent = new Intent(AlignmentActivity.this,SummaryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Alignment) {
            Intent intent = new Intent(AlignmentActivity.this,AlignmentActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_linktest) {
            Intent intent = new Intent(AlignmentActivity.this,LinkTestActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_wireless)
        {
            Intent intent = new Intent(AlignmentActivity.this,StaticsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout)
        {
            logout();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.diagonise_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AlignmentActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(KeywestPacket testPacket) {

        KWWirelessLinkStats wirelessLinkStats = new KWWirelessLinkStats(testPacket);
        mLocalSNR1.setText(Integer.toString(wirelessLinkStats.getLocalSNRA1()));
        mLocalSNR2.setText(Integer.toString(wirelessLinkStats.getLocalSNRA2()));
        mRemoteSNR1.setText(Integer.toString(wirelessLinkStats.getRemoteSNRA1()));
        mRemoteSNR2.setText(Integer.toString(wirelessLinkStats.getRemoteSNRA2()));

        if( mSharedPreference.getRadioMode() ==1 ) {
            mLocal.setText("BSU");
            mRemote.setText("SU");
        } else {
            mLocal.setText("SU");
            mRemote.setText("BSU");
        }

        setSNRToProgressbars(wirelessLinkStats.getLocalSNRA1(),mProgressBar01,mProgressBar02,mProgressBar03,mProgressBar04,mProgressBar05);
        setSNRToProgressbars(wirelessLinkStats.getRemoteSNRA1(),mProgressBar11,mProgressBar12,mProgressBar13,mProgressBar14,mProgressBar15);
        setSNRToProgressbars(wirelessLinkStats.getLocalSNRA2(),mProgressBar21,mProgressBar22,mProgressBar23,mProgressBar24,mProgressBar25);
        setSNRToProgressbars(wirelessLinkStats.getRemoteSNRA2(),mProgressBar31,mProgressBar32,mProgressBar33,mProgressBar34,mProgressBar35); //wirelessLinkStats.getLocalSNRA1()

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.diagonise_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}