package com.keywestnetworks.kwconnect.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.keywestnetworks.kwconnect.R;
import com.keywestnetworks.kwconnect.ServerPrograms.RouterService;
import com.keywestnetworks.kwconnect.constants.Bandwidth;
import com.keywestnetworks.kwconnect.constants.DeviceMode;
import com.keywestnetworks.kwconnect.constants.EnableDisable;
import com.keywestnetworks.kwconnect.constants.IPAddressType;
import com.keywestnetworks.kwconnect.constants.OperationalMode;
import com.keywestnetworks.kwconnect.constants.SpatialStream;
import com.keywestnetworks.kwconnect.constants.VlanMode;
import com.keywestnetworks.kwconnect.utils.Options;
import com.keywestnetworks.kwconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;

/*
   This class implements the configuration options for 5GHz Radio
 */
public class ConfigurationActivity extends BaseActivity {
    Configuration mConfiguration;
    EditText mSSID, mChannelNumber, mLinkId, mCustName, mTxPower, mDistance,
            mVlanMgmtID, mVlanAccID, mVlanTrunkID, mSvlanID;
    TextView mGateway, mIPAddress, mNetMask;
    Button mSetRequest;
    private RouterService.Subscription subscription;
    Spinner mDeviceMode, mChannelBandwidth, mMode, mIPAddressType, mCountryCode, mDdrsStatus,
            mSpatialStream, mMcsIndex, mMinMcsIndex, mMaxMcsIndex, matpcStatus,
            mVlanStatus, mVlanMode, mVlanTrunkOpt, mSvlanethertype;
    SharedPreference mSharedPreference;
    ProgressDialog progress;
    View mMinMcsIndexRow, mMaxMcsIndexRow, mMcsIndexRow, mTxPowerRow, mOpmodeval, mBwidthval,
            mChanval, mLinkidval, mDistval, mVlanmodeval, mMgmtidval, mAccidval, mTrunkoptval,
            mTrunkvlanidval, mSvlanidval, mSvlanethtypeval;
    private Options bandwidthOptions;
    private Options mcsOptions;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        this.onCreate("Configuration", R.id.toolbar, true);
        mSharedPreference = new SharedPreference(ConfigurationActivity.this);
        mCountryCode = (Spinner) findViewById(R.id.config_countrycode);
        mSSID = (EditText) findViewById(R.id.config_ssid);
        mChannelBandwidth = (Spinner) findViewById(R.id.config_CBW);
        mBwidthval = findViewById(R.id.bwidth_val);
        mMode = (Spinner) findViewById(R.id.config_cmode);
        mOpmodeval = findViewById(R.id.opmode_val);
        mChannelNumber = (EditText) findViewById(R.id.config_channelNumber);
        mChanval = findViewById(R.id.chan_val);
        mIPAddressType = (Spinner) findViewById(R.id.config_ipaddressType);
        mDeviceMode = (Spinner) findViewById(R.id.config_Devicemode);
        mIPAddress = (TextView) findViewById(R.id.config_ipaddress);
        mSetRequest = (Button) findViewById(R.id.config_setRequestButton);
        mGateway = (TextView) findViewById(R.id.config_gateway);
        mLinkId = (EditText) findViewById(R.id.config_LinkId);
        mLinkidval = findViewById(R.id.linkid_val);
        mCustName = (EditText) findViewById(R.id.config_custName);
        mNetMask = (TextView) findViewById(R.id.config_netmask);
        mDdrsStatus = findViewById(R.id.config_DDRS_status);
        mSpatialStream = findViewById(R.id.config_spatial_stream);
        mMcsIndex = findViewById(R.id.config_mcs_index);
        mMcsIndexRow = findViewById(R.id.mcs_index_row);
        mMaxMcsIndex = findViewById(R.id.config_max_mcs_index);
        mMaxMcsIndexRow = findViewById(R.id.max_mcs_index_row);
        mMinMcsIndex = findViewById(R.id.config_min_mcs_index);
        mMinMcsIndexRow = findViewById(R.id.min_mcs_index_row);
        matpcStatus = findViewById(R.id.config_atpc_status);
        mTxPower = findViewById(R.id.config_tx_power);
        mTxPowerRow = findViewById(R.id.tx_power_row);
        mDistance = findViewById(R.id.config_dist);
        mDistval = findViewById(R.id.dist_val);
        mVlanStatus = findViewById(R.id.config_vlan_status);
        mVlanMode = findViewById(R.id.config_vlan_mode);
        mVlanmodeval = findViewById(R.id.vlanmode_val);
        mVlanMgmtID = findViewById(R.id.config_mgmt_vlanid);
        mMgmtidval = findViewById(R.id.mgmtid_val);
        mVlanAccID = findViewById(R.id.config_acc_vlanid);
        mAccidval = findViewById(R.id.accid_val);
        /*mVlanTrunkOpt = findViewById(R.id.config_trunk_opt);
        mTrunkoptval = findViewById(R.id.trunkopt_val);
        mVlanTrunkID = findViewById(R.id.config_trunk_vlanid);
        mTrunkvlanidval = findViewById(R.id.trunkvlanid_val);
        mSvlanID = findViewById(R.id.config_svlanid);
        mSvlanidval = findViewById(R.id.svlanid_val);
        mSvlanethertype = findViewById(R.id.config_svlan_ethertype);
        mSvlanethtypeval = findViewById(R.id.svlanethtype_val);*/
        configurationActivityInit();
        loadConfiguration();
    }

    /* Initialize the cofiguration parameters */
    private void configurationActivityInit() {
        mDeviceMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setDevmodeOptions();
                setVLANOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mDdrsStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setMCSOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mSpatialStream.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setMCSOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setBandwidthOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        matpcStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                onAptcChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mIPAddressType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                onIpAddressTypeChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mVlanStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setVLANOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mVlanMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setVLANOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        /*mVlanTrunkOpt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setVLANOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mSvlanethertype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setVLANOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });*/

        initSpinner(mDeviceMode, Options.DEV_MODE);
        initSpinner(mCountryCode, Options.COUNTRY_CODE_OPTIONS);
        initSpinner(mMode, Options.OPERATIONAL_MODE);
        initSpinner(mDdrsStatus, Options.ENABLE_DISABLE);
        initSpinner(mSpatialStream, Options.SPATIAL_STREAM);
        initSpinner(matpcStatus, Options.ENABLE_DISABLE);
        initSpinner(mVlanStatus, Options.ENABLE_DISABLE);
        initSpinner(mVlanMode, Options.VLAN_MODE);
        /*initSpinner(mVlanTrunkOpt, Options.TRUNK_OPT);
        initSpinner(mSvlanethertype, Options.SVLAN_ETHERTYPE);*/
    }


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

    /* On Change of ATPC Parameter */
    private void onAptcChange() {
        int value = getSelectedOption(matpcStatus, Options.ENABLE_DISABLE);
        if (value == EnableDisable.ENABLE) {
            mTxPowerRow.setVisibility(View.GONE);
        } else {
            mTxPowerRow.setVisibility(View.VISIBLE);
        }
    }

    /* On Change of IpAddress Parameter */
    private void onIpAddressTypeChange() {
        int ipAddressType = getSelectedOption(mIPAddressType, Options.IP_ADDRESS_TYPE);
        boolean isStatic = ipAddressType == IPAddressType.STATIC;
        mIPAddress.setEnabled(isStatic);
        mGateway.setEnabled(isStatic);
        mNetMask.setEnabled(isStatic);
        if (mConfiguration != null) {
            if (isStatic) {
                mIPAddress.setText(mConfiguration.getIpAddress());
                mGateway.setText(mConfiguration.getGatewayIp());
                mNetMask.setText(mConfiguration.getNetMask());
            } else {
                mIPAddress.setText(mConfiguration.getDhcpAddress());
                mGateway.setText(mConfiguration.getDhcpGateway());
                mNetMask.setText(mConfiguration.getDhcpMask());
            }
        }
    }

    /* Showing the Progress Bar */
    private void showProgress(String message) {
        showProgress(message, 0, null);
    }

    /* Showing the Progress Bar */
    private void showProgress(String message, int timeout, final Runnable finishCallback) {
        final String ssid = mSharedPreference.getSsid();
        progress = new ProgressDialog(this);
        progress.setMessage(message);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        if (timeout > 0) {
            new CountDownTimer(timeout, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Log.i(ConfigurationActivity.class.getName(), "closing show progress.....");
                    //connectToWifi(ssid, "");
                    finishCallback.run();
                }
            }.start();
        }
    }

    /* Loading the configuration obtained from the device using KWN Socket Interface */
    private void loadConfiguration() {
        showProgress("Please wait fetching config...");
        Log.i(ConfigurationActivity.class.getName(), "Please wait fetching config...");
        subscription = RouterService.getInstance().sendWithTimeOut(new Configuration().getPacket(), new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(packet);
                        progress.dismiss();
                        Log.i(ConfigurationActivity.class.getName(), "onSuccess load Configuration.....");
                        subscription.cancel();
                    }
                });
            }

            @Override
            public void onError(String msg, Exception e) {
                super.onError(msg, e);
                progress.dismiss();
                // subscription.cancel();
                Log.e(ConfigurationActivity.class.getName(), "Configuration request failed: " + msg, e);
                //Toast.makeText(ConfigurationActivity.this, "Error occured", Toast.LENGTH_LONG).show();
            }
        }, 5000);
    }

    public void showToastOnUI(String message) {
    }

    /* Update the UI with obtained parameters using KWN Socket Interface */
    private void updateUI(KeywestPacket packet) {
        mConfiguration = new Configuration(packet);
        /*if (mConfiguration.getDeviceMode() == ONE) {
            mDeviceMode.setText("AP");
        } else {
            mDeviceMode.setText("SU");
        }*/
        mDeviceMode.setSelection(Options.DEV_MODE.findPositionByKey(mConfiguration.getDeviceMode()));
        setDevmodeOptions();
        mCountryCode.setSelection(Options.COUNTRY_CODE_OPTIONS.findPositionByKey(mConfiguration.getCountryCode()));
        mSSID.setText(mConfiguration.getSsid());
        mMode.setSelection(Options.OPERATIONAL_MODE.findPositionByKey(mConfiguration.getMode()));
        setBandwidthOptions();
        mChannelBandwidth.setSelection(bandwidthOptions.findPositionByKey(mConfiguration.getChannelBW()));
        if (mConfiguration.getChannel() == ZERO) {
            mChannelNumber.setText("Auto");
        } else {
            mChannelNumber.setText(Integer.toString(mConfiguration.getChannel()));
        }
        mDdrsStatus.setSelection(Options.ENABLE_DISABLE.findPositionByKey(mConfiguration.getDdrsStatus()));
        mSpatialStream.setSelection(Options.SPATIAL_STREAM.findPositionByKey(mConfiguration.getSpacialStream()));
        setMCSOptions();
        matpcStatus.setSelection(Options.ENABLE_DISABLE.findPositionByKey(mConfiguration.getAtpcStatus()));
        onAptcChange();
        mTxPower.setText(Integer.toString(mConfiguration.getTranmitPower()));
        mIPAddressType.setSelection(Options.IP_ADDRESS_TYPE.findPositionByKey(mConfiguration.getIpAddrType()));
        onIpAddressTypeChange();
        mCustName.setText(mConfiguration.getCustName());
        mLinkId.setText(mConfiguration.getLinkId());
        mDistance.setText(Integer.toString(mConfiguration.getDistance()));
        mVlanStatus.setSelection(Options.ENABLE_DISABLE.findPositionByKey(mConfiguration.getVlanStatus()));
        setVLANOptions();
        mVlanMode.setSelection(Options.VLAN_MODE.findPositionByKey(mConfiguration.getVlanMode()));
        mVlanMgmtID.setText(Integer.toString(mConfiguration.getVlanMgmtId()));
        mVlanAccID.setText(Integer.toString(mConfiguration.getVlanAccessId()));
        /*mSvlanID.setText(Integer.toString(mConfiguration.getVlanSvlanId()));
        mSvlanethertype.setSelection(Options.SVLAN_ETHERTYPE.getKeyByValue(mConfiguration.getVlanEtherType()));*/
    }

    private String getTextValue(TextView v, String defaultValue) {
        if (v.getText() != null) {
            return v.getText().toString();
        }
        return defaultValue;
    }

    private Integer getTextValue(TextView v, int defaultValue) {
        if (v.getText() != null && v.getText().length() > 0) {
            try {
                defaultValue = Integer.parseInt(v.getText().toString());
            } catch (NumberFormatException ne) {
            }
            return defaultValue;
        }
        return defaultValue;
    }

    /* Set the configuration when APPLY button is clicked */
    public void setConfiguration(View v) {
        showProgress("Applying Configuration...", 20 * 1000, new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        Log.i(ConfigurationActivity.class.getName(), "onFinish setConfiguration.....");
                        progress.dismiss();
                        showDiscovery();
                    }
                }.start();
            }
        });
        Configuration configuration = new Configuration();
        /*configuration.setDeviceMode("AP".equals(mDeviceMode.getText().toString()) ? DeviceMode.AP : DeviceMode.SU);*/
        configuration.setDeviceMode(getSelectedOption(mDeviceMode, Options.DEV_MODE));
        configuration.setCountryCode(getSelectedOption(mCountryCode, Options.COUNTRY_CODE_OPTIONS));
        configuration.setSsid(getTextValue(mSSID, ""));
        configuration.setMode(getSelectedOption(mMode, Options.OPERATIONAL_MODE));
        configuration.setChannelBW(getSelectedOption(mChannelBandwidth, bandwidthOptions));
        String channel = getTextValue(mChannelNumber, "");
        if (channel.equalsIgnoreCase("Auto")) {
            configuration.setChannel(ZERO);
        } else {
            configuration.setChannel(Integer.parseInt(channel));
        }
        configuration.setDdrsStatus(getSelectedOption(mDdrsStatus, Options.ENABLE_DISABLE));
        configuration.setSpacialStream(getSelectedOption(mSpatialStream, Options.SPATIAL_STREAM));
        configuration.setModulationIndex(getSelectedOption(mMcsIndex, mcsOptions));
        configuration.setMinModulationIndex(getSelectedOption(mMinMcsIndex, mcsOptions));
        configuration.setMaxModulationIndex(getSelectedOption(mMaxMcsIndex, mcsOptions));
        configuration.setAtpcStatus(getSelectedOption(matpcStatus, Options.ENABLE_DISABLE));
        configuration.setTranmitPower(getTextValue(mTxPower, 0));
        configuration.setIpAddrType(getSelectedOption(mIPAddressType, Options.IP_ADDRESS_TYPE));
        configuration.setIpAddress(getTextValue(mIPAddress, ""));
        configuration.setGatewayIp(getTextValue(mGateway, ""));
        configuration.setNetMask(getTextValue(mNetMask, ""));
        configuration.setLinkId(getTextValue(mLinkId, ""));
        configuration.setCustName(getTextValue(mCustName, ""));
        configuration.setDistance(getTextValue(mDistance, 5));
        configuration.setVlanStatus(getSelectedOption(mVlanStatus, Options.ENABLE_DISABLE));
        configuration.setVlanMode(getSelectedOption(mVlanMode, Options.VLAN_MODE));
        configuration.setVlanMgmtId(getTextValue(mVlanMgmtID, 1));
        configuration.setVlanAccessId(getTextValue(mVlanAccID, 10));
        /*configuration.setVlanTrunkOption(getSelectedOption(mVlanTrunkOpt, Options.TRUNK_OPT));
        configuration.setVlanTrunkId(getTextValue(mVlanTrunkID,""));
        configuration.setVlanSvlanId(getTextValue(mSvlanID, 100));
        configuration.setVlanEtherType(String.valueOf(getSelectedOption(mSvlanethertype, Options.SVLAN_ETHERTYPE)));*/

        KeywestPacket setpacket = configuration.buildPacketFromUI();
        Log.i(ConfigurationActivity.class.getName(), "applying configuration setConfiguration.....");
        RouterService.getInstance().sendRequest(setpacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(KeywestPacket packet) {
                Log.i(ConfigurationActivity.class.getName(), "on success applying configuration setConfiguration.....");
            }

            @Override
            public void onError(String msg, Exception e) {
                Log.i(ConfigurationActivity.class.getName(), "on error applying configuration setConfiguration.....");
            }
        });
    }

    /* On Change Device Mode Options*/
    private void setDevmodeOptions() {
        int device_mode = getSelectedOption(mDeviceMode, Options.DEV_MODE);
        mDistval.setVisibility(View.GONE);
        if (device_mode == DeviceMode.AP) {
            mOpmodeval.setVisibility(View.VISIBLE);
            mBwidthval.setVisibility(View.VISIBLE);
            mChanval.setVisibility(View.VISIBLE);
            mLinkidval.setVisibility(View.GONE);
        } else {
            mOpmodeval.setVisibility(View.GONE);
            mBwidthval.setVisibility(View.GONE);
            mChanval.setVisibility(View.GONE);
            mLinkidval.setVisibility(View.VISIBLE);
        }
    }

    /* Set Channel bandwidth Options */
    private void setBandwidthOptions() {
        int operationalMode = getSelectedOption(mMode, Options.OPERATIONAL_MODE);
        bandwidthOptions = new Options();
        if (operationalMode == OperationalMode._11A) {
            bandwidthOptions.add(Bandwidth._20MHZ, "20MHz");
        } else if (operationalMode == OperationalMode._11NA) {
            bandwidthOptions.add(Bandwidth._20MHZ, "20MHz");
            bandwidthOptions.add(Bandwidth._40MHZ, "40MHz");
        } else if (operationalMode == OperationalMode._11AC) {
            bandwidthOptions.add(Bandwidth._20MHZ, "20MHz");
            bandwidthOptions.add(Bandwidth._40MHZ, "40MHz");
            bandwidthOptions.add(Bandwidth._80MHZ, "80MHz");
        }
        initSpinner(mChannelBandwidth, bandwidthOptions);
    }

    /* Set MCS Index Options */
    private void setMCSOptions() {
        int spatialStream = getSelectedOption(mSpatialStream, Options.SPATIAL_STREAM);
        int ddrs = getSelectedOption(mDdrsStatus, Options.ENABLE_DISABLE);
        int start = (spatialStream == SpatialStream.DUAL) ? 10 : 0;
        int end = (spatialStream == SpatialStream.AUTO) ? (start + 20) : (start + 10);
        mcsOptions = new Options();
        for (int i = start; i < end; i++) {
            mcsOptions.add(i, "MCS" + i);
        }
        initSpinner(mMcsIndex, mcsOptions);
        initSpinner(mMinMcsIndex, mcsOptions);
        initSpinner(mMaxMcsIndex, mcsOptions);
        int defaultMaxValue = mcsOptions.findPositionByKey(end - 1);
        if (ddrs == EnableDisable.ENABLE) {
            mMcsIndexRow.setVisibility(View.GONE);
            if (spatialStream == SpatialStream.AUTO) {
                mMinMcsIndexRow.setVisibility(View.GONE);
                mMaxMcsIndexRow.setVisibility(View.GONE);
            } else {
                mMinMcsIndexRow.setVisibility(View.VISIBLE);
                mMaxMcsIndexRow.setVisibility(View.VISIBLE);
            }
        } else {
            mMcsIndexRow.setVisibility(View.VISIBLE);
            mMinMcsIndexRow.setVisibility(View.GONE);
            mMaxMcsIndexRow.setVisibility(View.GONE);
        }
        if (mConfiguration != null) {
            mMcsIndex.setSelection(mcsOptions.findPositionByKey(mConfiguration.getModulationIndex()));
            mMinMcsIndex.setSelection(mcsOptions.findPositionByKey(mConfiguration.getMinModulationIndex()));
            if (spatialStream == SpatialStream.AUTO) {
                mMaxMcsIndex.setSelection(mcsOptions.findPositionByKey(defaultMaxValue, defaultMaxValue));
            } else {
                mMaxMcsIndex.setSelection(mcsOptions.findPositionByKey(mConfiguration.getMaxModulationIndex(), defaultMaxValue));
            }
        }
    }

    /* Set vlan status */
    private void setVLANOptions() {

        int vlanstat = getSelectedOption(mVlanStatus, Options.ENABLE_DISABLE);
        int dmode = getSelectedOption(mDeviceMode, Options.DEV_MODE);
        int vmode = getSelectedOption(mVlanMode, Options.VLAN_MODE);

        mVlanMode.setEnabled(false);
        mVlanmodeval.setVisibility(View.GONE);
        mMgmtidval.setVisibility(View.GONE);
        mAccidval.setVisibility(View.GONE);
        /*mTrunkoptval.setVisibility(View.GONE);
        mTrunkvlanidval.setVisibility(View.GONE);
        mSvlanidval.setVisibility(View.GONE);
        mSvlanethtypeval.setVisibility(View.GONE);*/

        if (vlanstat == EnableDisable.ENABLE) {
            mVlanmodeval.setVisibility(View.VISIBLE);
            mMgmtidval.setVisibility(View.VISIBLE);
            mVlanMode.setEnabled(true);
            if (dmode == DeviceMode.AP) {
                mVlanMode.setEnabled(false);
                mVlanMode.setSelection(Options.VLAN_MODE.findPositionByKey(VlanMode.TRANSPARENT));

            } else if (dmode == DeviceMode.SU) {
                if (vmode == VlanMode.ACCESS) {
                    mAccidval.setVisibility(View.VISIBLE);
                }
                //TODO; uncomment the following for future vlan changes
                /*int trunkopt = getSelectedOption(mVlanTrunkOpt, Options.TRUNK_OPT);*/
                /*if (vlanmode == VlanMode.TRUNK) {
                    mTrunkoptval.setVisibility(View.VISIBLE);
                    if (trunkopt == TrunkOpt.LIST) {
                        mTrunkvlanidval.setVisibility(View.VISIBLE);
                    }
                }

                if (vlanmode == VlanMode.QinQ) {
                    mTrunkoptval.setVisibility(View.VISIBLE);
                    if (trunkopt == TrunkOpt.LIST) {
                        mTrunkvlanidval.setVisibility(View.VISIBLE);
                    }
                    mSvlanidval.setVisibility(View.VISIBLE);
                    mSvlanethtypeval.setVisibility(View.VISIBLE);
                }*/
            }
        }
    }

    private int stringToInt(String value) {
        int conValue = 0;
        try {
            conValue = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        return conValue;
    }

    /* Redirect to the Discovery Activity */
    public void showDiscovery() {
        this.startActivity(new Intent(this, DiscoveryActivity.class));
        this.finish();
    }
    /* Redirect to the Home Activity */
    public void showHome() {
        this.startActivity(new Intent(this, HomeActivity.class));
        this.finish();
    }
}