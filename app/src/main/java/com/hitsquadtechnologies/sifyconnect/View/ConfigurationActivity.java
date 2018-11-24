package com.hitsquadtechnologies.sifyconnect.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.utils.CustomTypefaceSpan;
import com.hitsquadtechnologies.sifyconnect.Interfaces.TaskCompleted;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPConnection;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.UDPSetRequest;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    Configuration  mConfiguration;
    UDPConnection  mUdpClientThread;
    EditText       mSSID,mChannelNumber,mLinkId,mCustName;
    TextView       mGateway,mIPAddress,mDeviceMode,mNetMask;
    Button         mSetRequest;
    Spinner        mChannelBandwidth,mMode,mIPAddressType,mCountryCode;
    SharedPreference mSharedPreference;
    ProgressDialog progress;
    public static final int COUNTRYCODE_UL = 5016;
    public static final int COUNTRYCODE_L  = 5017;
    public static final int COUNTRYCODE_R  = 5011;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Configuration");
        ConfigInit();

        DrawerLayout drawer      = (DrawerLayout) findViewById(R.id.configuration_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView();
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/calibri.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void ConfigInit() {
        mSharedPreference = new SharedPreference(ConfigurationActivity.this);
        mCountryCode      =(Spinner)findViewById(R.id.config_countrycode);
        mSSID             = (EditText)findViewById(R.id.config_ssid);
        mChannelBandwidth = (Spinner)findViewById(R.id.config_CBW);
        mMode             = (Spinner)findViewById(R.id.config_cmode);
        mChannelNumber    = (EditText)findViewById(R.id.config_channelNumber);
        mIPAddressType    = (Spinner)findViewById(R.id.config_ipaddressType);
        mDeviceMode       = (TextView)findViewById(R.id.config_Devicemode);
        mIPAddress        = (TextView)findViewById(R.id.config_ipaddress);
        mSetRequest       = (Button)findViewById(R.id.config_setRequestButton);
        mGateway          = (TextView)findViewById(R.id.config_gateway);
        mLinkId           = (EditText)findViewById(R.id.config_LinkId);
        mCustName         = (EditText)findViewById(R.id.config_custName);
        mNetMask          = (TextView)findViewById(R.id.config_netmask);

        requestToServer();
    }

    private void showProgress(String message){

        progress = new ProgressDialog(this);
        progress.setMessage(message);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
    }

    private void navigationView()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

        private void requestToServer()
        {
            mConfiguration    = new Configuration();
            mUdpClientThread = new UDPConnection(mSharedPreference.getIPAddress(), 9181,mConfiguration.getPacket(),new ResponseListener());
            mUdpClientThread.start();
       }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.action_settings ) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if ( id == R.id.nav_discovery ) {
            Intent intent = new Intent(ConfigurationActivity.this,DiscoveryActivity.class);
            startActivity(intent);
        } else if ( id == R.id.nav_configuration ) {
            Intent intent = new Intent(ConfigurationActivity.this,ConfigurationActivity.class);
            startActivity(intent);
        } else if ( id == R.id.nav_summary ) {
            Intent intent = new Intent(ConfigurationActivity.this,SummaryActivity.class);
            startActivity(intent);
        } else if ( id == R.id.nav_Alignment ) {
            Intent intent = new Intent(ConfigurationActivity.this,AlignmentActivity.class);
            startActivity(intent);
        } else if ( id == R.id.nav_linktest ) {
            Intent intent = new Intent(ConfigurationActivity.this,LinkTestActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_wireless)
        {
            Intent intent = new Intent(ConfigurationActivity.this,StaticsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout)
        {
            logout();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configuration_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logout() {
        Intent intent = new Intent(ConfigurationActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configuration_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    class ResponseListener implements TaskCompleted {

        @Override
        public void onTaskComplete(final KeywestPacket result) {

            runOnUiThread(new Runnable() {
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

        private void updateUI(KeywestPacket packet) {

            Configuration configuration = new Configuration(packet);

            mSSID.setText(configuration.getSsid());
            mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(),configuration.getDeviceMode(),configuration.getIpAddress());

            int bw = configuration.getChannelBW();
            mChannelBandwidth.setSelection(bw-1);
            mConfiguration.setChannelBW(bw);

            int Mode = configuration.getMode();
            setBWvalues(Mode);
            mMode.setSelection(Mode-1);
            mConfiguration.setMode(Mode);

            switch(configuration.getCountryCode()) {

                case COUNTRYCODE_UL :
                    mCountryCode.setSelection(ZERO);
                    mConfiguration.setCountryCode(COUNTRYCODE_UL);
                    break;

                case COUNTRYCODE_L :
                    mCountryCode.setSelection(ONE);
                    mConfiguration.setCountryCode(COUNTRYCODE_L);
                    break;

                case COUNTRYCODE_R :
                    mCountryCode.setSelection(TWO);
                    mConfiguration.setCountryCode(COUNTRYCODE_R);
                    break;

                default :
                    break;
            }

            if( configuration.getIpAddrType() == ONE ) {
                mIPAddressType.setSelection( 0 );
                mConfiguration.setIpAddrType( configuration.getIpAddrType() );
                mIPAddress.setText( configuration.getIpAddress() );
                mGateway.setText( configuration.getGatewayIp() );
            }
            if( configuration.getIpAddrType() == TWO ) {
                mIPAddressType.setSelection( 1 );
                mIPAddress.setClickable( false );
                mIPAddress.setEnabled( false );
                mConfiguration.setIpAddrType( configuration.getIpAddrType() );
                mIPAddress.setText( configuration.getIpAddress() );
                mGateway.setText( configuration.getGatewayIp() );
            }

            if(configuration.getDeviceMode() == ONE)
            {
                mDeviceMode.setText("BSU");
            }else {
                mDeviceMode.setText("SU");
            }

            mNetMask.setText(configuration.getNetMask());
            mCustName.setText(configuration.getCustName());
            mLinkId.setText(Integer.toString(configuration.getLinkId()));

            if( configuration.getChannel() == ZERO ){
                mChannelNumber.setText("Auto");
            } else {
                mChannelNumber.setText(Integer.toString(configuration.getChannel()));
            }

            setRequest();
            progress.dismiss();
        }


        /*
         * SET REQUEST TO SERVER
         *
         */
        private void setRequest()  {

            showProgress("Please wait fetching config...");

            mChannelBandwidth.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    Object item = adapterView.getItemAtPosition( position );
                    if ( item != null ) {
                        mConfiguration.setChannelBW( position + 3 );
                        Log.w("position", String.valueOf(mConfiguration.getChannel()));
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            mMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    Object item = adapterView.getItemAtPosition(position);
                    if ( item != null ) {
                        mConfiguration.setMode( position + 1 );
                        setBWvalues( position );
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            mIPAddressType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    Object item = adapterView.getItemAtPosition(position);
                    if ( item != null ) {
                        mConfiguration.setIpAddrType(position+1);
                        if(item.toString().equalsIgnoreCase("Dynamic")) {
                            mIPAddress.setClickable(false);
                            mIPAddress.setEnabled(false);
                            mGateway.setEnabled(false);
                        }
                        if(item.toString().equalsIgnoreCase("Static")) {
                            mIPAddress.setEnabled(true);
                            mIPAddress.setClickable(true);
                            mGateway.setEnabled(true);
                        }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            mCountryCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    Object item = adapterView.getItemAtPosition(position);
                    if ( item != null) {
                        if( position == ZERO ){ mConfiguration.setCountryCode(COUNTRYCODE_UL); }
                        if( position == ONE ){ mConfiguration.setCountryCode(COUNTRYCODE_L); }
                        if( position == TWO ){ mConfiguration.setCountryCode(COUNTRYCODE_R); }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}});

            if(mDeviceMode.getText().toString().equalsIgnoreCase("BSU")){
                mConfiguration.setDeviceMode(ONE);
            }else {
                mConfiguration.setDeviceMode(TWO);
            }

            mSetRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showProgress("Loading...");

                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) { }
                        public  void onFinish() {
                            progress.dismiss();
                        }
                    }.start();

                    String ssid = mSSID.getText().toString();
                    if( ssid != null ){
                        mConfiguration.setSsid(mSSID.getText().toString());
                    } else {
                        mConfiguration.setSsid("");
                    }
                    String ipaddress = mIPAddress.getText().toString();
                    if( ipaddress != null ){
                        mConfiguration.setIpAddress(mIPAddress.getText().toString());
                    } else {
                        mConfiguration.setIpAddress("");
                    }
                    if(mChannelNumber.getText().toString().equalsIgnoreCase("Auto")) {
                        mConfiguration.setChannel(ZERO);
                    } else {
                        mConfiguration.setChannel(Integer.parseInt(mChannelNumber.getText().toString()));
                    }
                    String gateway = mGateway.getText().toString();
                    if( gateway != null ) {
                        mConfiguration.setGatewayIp(mGateway.getText().toString());
                    } else {
                        mConfiguration.setGatewayIp("");
                    }

                    mConfiguration.setNetMask(mNetMask.getText().toString());
                    mConfiguration.setCustName(mCustName.getText().toString());
                    mConfiguration.setLinkId(stringToInt(mLinkId.getText().toString()));

                    KeywestPacket setpacket =  mConfiguration.buildPacketFromUI();
                    UDPSetRequest mUDPSetRequest   = new UDPSetRequest(mSharedPreference.getIPAddress(), 9181,setpacket);
                    mUDPSetRequest.start();
                }
            });
        }
    }


    private void setBWvalues(int strMode) {
         List<String> bandwidth_arrays = new ArrayList<String>();
         bandwidth_arrays.clear();

         if( strMode == ZERO ) {
            bandwidth_arrays.add("20MHz");
         }
         if( strMode == ONE ) {
             bandwidth_arrays.add("20MHz");
             bandwidth_arrays.add("40MHz");
             bandwidth_arrays.add("40MHz-");
         }
         if( strMode == TWO) {
             bandwidth_arrays.add("20MHz");
             bandwidth_arrays.add("40MHz");
             bandwidth_arrays.add("40MHz-");
             bandwidth_arrays.add("80MHz");
         }
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_spinner_item, bandwidth_arrays);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         mChannelBandwidth.setAdapter(adapter);
    }

    private int stringToInt(String value){

        int conValue = 0;
        try {
            conValue=  Integer.parseInt(value);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        return conValue;
    }
}
