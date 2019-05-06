package com.keywestnetworks.kwconnect.View;

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
import com.keywestnetworks.kwconnect.constants.DeviceMode;
import com.keywestnetworks.kwconnect.constants.EnableDisable;
import com.keywestnetworks.kwconnect.constants.Encrypt;
import com.keywestnetworks.kwconnect.constants.IPAddressType;
import com.keywestnetworks.kwconnect.constants.OperationalMode;
import com.keywestnetworks.kwconnect.constants.SpatialStream;
import com.keywestnetworks.kwconnect.constants.VlanMode;
import com.keywestnetworks.kwconnect.utils.Options;
import com.keywestnetworks.kwconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;

/* This class implements the configuration options for 5GHz Radio */

public class ConfigurationActivity extends BaseActivity {
    Configuration mConfiguration;
    EditText mSSID, mChannelNumber, mLinkId, mCustName, mTxPower, mDistance,
            mVlanMgmtID, mVlanAccID, mVlanTrunkID, mSvlanID, mEncryptKey;
    TextView mMode, mGateway, mIPAddress, mNetMask;
    Button mSetRequest;
    private RouterService.Subscription subscription;
    Spinner mDeviceMode, mChannelBandwidth, mIPAddressType, mCountryCode, mDdrsStatus,
            mSpatialStream, mMcsIndex, mMinMcsIndex, mMaxMcsIndex, matpcStatus, mEncrypt,
            mVlanStatus, mVlanMode, mVlanTrunkOpt, mSvlanethertype;
    View mMinMcsIndexRow, mMaxMcsIndexRow, mMcsIndexRow, mTxPowerRow, mBwidthval, mChanval,
            mLinkidval, mDistval, mVlanmodeval, mMgmtidval, mAccidval, mDdrsview, matpcview,
            mSpatialview, mTrunkoptval, mTrunkvlanidval, mSvlanidval, mSvlanethtypeval, mEnckeyRow;
    private Configuration newConfig = null;
    private Options mcsOptions;
    public static final int ZERO = 0;
    public static final int THREE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        this.onCreate("Configuration", R.id.toolbar, true);
        mSharedPreference = new SharedPreference(ConfigurationActivity.this);
        mCountryCode      = (Spinner) findViewById(R.id.config_countrycode);
        mSSID             = (EditText) findViewById(R.id.config_ssid);
        mChannelBandwidth = (Spinner) findViewById(R.id.config_CBW);
        mBwidthval        = findViewById(R.id.bwidth_val);
        mMode             = (TextView) findViewById(R.id.config_cmode);
        mChannelNumber    = (EditText) findViewById(R.id.config_channelNumber);
        mChanval          = findViewById(R.id.chan_val);
        mEncrypt          = findViewById(R.id.config_encrypt);
        mEncryptKey       = (EditText) findViewById(R.id.config_encryptkey);
        mEnckeyRow        = findViewById(R.id.enckey_row);
        mIPAddressType    = (Spinner) findViewById(R.id.config_ipaddressType);
        mDeviceMode       = (Spinner) findViewById(R.id.config_Devicemode);
        mIPAddress        = (TextView) findViewById(R.id.config_ipaddress);
        mSetRequest       = (Button) findViewById(R.id.config_setRequestButton);
        mGateway          = (TextView) findViewById(R.id.config_gateway);
        mLinkId           = (EditText) findViewById(R.id.config_LinkId);
        mLinkidval        = findViewById(R.id.linkid_val);
        mCustName         = (EditText) findViewById(R.id.config_custName);
        mNetMask          = (TextView) findViewById(R.id.config_netmask);
        mDdrsStatus       = findViewById(R.id.config_DDRS_status);
        mDdrsview         = findViewById(R.id.ddrsstat);
        mSpatialStream    = findViewById(R.id.config_spatial_stream);
        mSpatialview      = findViewById(R.id.spatial_stream);
        mMcsIndex         = findViewById(R.id.config_mcs_index);
        mMcsIndexRow      = findViewById(R.id.mcs_index_row);
        mMaxMcsIndex      = findViewById(R.id.config_max_mcs_index);
        mMaxMcsIndexRow   = findViewById(R.id.max_mcs_index_row);
        mMinMcsIndex      = findViewById(R.id.config_min_mcs_index);
        mMinMcsIndexRow   = findViewById(R.id.min_mcs_index_row);
        matpcStatus       = findViewById(R.id.config_atpc_status);
        matpcview         = findViewById(R.id.atpcstat);
        mTxPower          = findViewById(R.id.config_tx_power);
        mTxPowerRow       = findViewById(R.id.tx_power_row);
        mDistance         = findViewById(R.id.config_dist);
        mDistval          = findViewById(R.id.dist_val);
        mVlanStatus       = findViewById(R.id.config_vlan_status);
        mVlanMode         = findViewById(R.id.config_vlan_mode);
        mVlanmodeval      = findViewById(R.id.vlanmode_val);
        mVlanMgmtID       = findViewById(R.id.config_mgmt_vlanid);
        mMgmtidval        = findViewById(R.id.mgmtid_val);
        mVlanAccID        = findViewById(R.id.config_acc_vlanid);
        mAccidval         = findViewById(R.id.accid_val);
        /*mVlanTrunkOpt   = findViewById(R.id.config_trunk_opt);
        mTrunkoptval      = findViewById(R.id.trunkopt_val);
        mVlanTrunkID      = findViewById(R.id.config_trunk_vlanid);
        mTrunkvlanidval   = findViewById(R.id.trunkvlanid_val);
        mSvlanID          = findViewById(R.id.config_svlanid);
        mSvlanidval       = findViewById(R.id.svlanid_val);
        mSvlanethertype   = findViewById(R.id.config_svlan_ethertype);
        mSvlanethtypeval  = findViewById(R.id.svlanethtype_val);*/
        configurationActivityInit();
        if (RouterService.getInstance().getNewConfiguration() == null) {
            loadConfiguration();
        } else {
            updateUI(RouterService.getInstance().getNewConfiguration());
        }
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
                setMCSOptions(mConfiguration);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mSpatialStream.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setMCSOptions(mConfiguration);
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
                onIpAddressTypeChange(mConfiguration);
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
        mEncrypt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setEncryptOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        initSpinner(mDeviceMode, Options.DEV_MODE);
        initSpinner(mChannelBandwidth, Options.BANDWIDTH);
        initSpinner(mCountryCode, Options.COUNTRY_CODE_OPTIONS);
        initSpinner(mDdrsStatus, Options.ENABLE_DISABLE);
        initSpinner(mSpatialStream, Options.SPATIAL_STREAM);
        initSpinner(matpcStatus, Options.ENABLE_DISABLE);
        initSpinner(mVlanStatus, Options.ENABLE_DISABLE);
        initSpinner(mVlanMode, Options.VLAN_MODE);
        /*initSpinner(mVlanTrunkOpt, Options.TRUNK_OPT);
        initSpinner(mSvlanethertype, Options.SVLAN_ETHERTYPE);*/
        initSpinner(mEncrypt, Options.ENCRYPT);
        setViewOptions();
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
            case R.id.action_apply:
                displaysavedDialog();
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
    private void onIpAddressTypeChange(Configuration mConfiguration) {
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
                        mConfiguration = new Configuration(packet);
                        RouterService.getInstance().setOldConfiguration(mConfiguration);
                        updateUI(mConfiguration);
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
                Log.e(ConfigurationActivity.class.getName(), "Configuration request failed: " + msg, e);
            }
        }, 5000);
    }

    @Override
    protected void updateUI(Configuration mConfiguration) {
        mDeviceMode.setSelection(Options.DEV_MODE.findPositionByKey(mConfiguration.getDeviceMode()));
        mSSID.setText(mConfiguration.getSsid());
        setDevmodeOptions();
        mCountryCode.setSelection(Options.COUNTRY_CODE_OPTIONS.findPositionByKey(mConfiguration.getCountryCode()));
        if (mConfiguration.getMode() == THREE) {
            mMode.setText("11AC");
        }
        mChannelBandwidth.setSelection(Options.BANDWIDTH.findPositionByKey(mConfiguration.getChannelBW()));
        if (mConfiguration.getChannel() == ZERO) {
            mChannelNumber.setText("Auto");
        } else {
            mChannelNumber.setText(Integer.toString(mConfiguration.getChannel()));
        }
        mEncrypt.setSelection(Options.ENCRYPT.findPositionByKey(mConfiguration.getEncryptType()));
        mEncryptKey.setText(mConfiguration.getEncryptKey());
        setEncryptOptions();
        mDdrsStatus.setSelection(Options.ENABLE_DISABLE.findPositionByKey(mConfiguration.getDdrsStatus()));
        mSpatialStream.setSelection(Options.SPATIAL_STREAM.findPositionByKey(mConfiguration.getSpacialStream()));
        setMCSOptions(mConfiguration);
        matpcStatus.setSelection(Options.ENABLE_DISABLE.findPositionByKey(mConfiguration.getAtpcStatus()));
        onAptcChange();
        mTxPower.setText(Integer.toString(mConfiguration.getTranmitPower()));
        mIPAddressType.setSelection(Options.IP_ADDRESS_TYPE.findPositionByKey(mConfiguration.getIpAddrType()));
        onIpAddressTypeChange(mConfiguration);
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
        setViewOptions();
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

    /* Set the configuration when SAVE button is clicked */
    public void saveConfiguration(View v) {

        newConfig = new Configuration();
        int ipAddressType = getSelectedOption(mIPAddressType, Options.IP_ADDRESS_TYPE);

        newConfig.setDeviceMode(getSelectedOption(mDeviceMode, Options.DEV_MODE));
        newConfig.setCountryCode(getSelectedOption(mCountryCode, Options.COUNTRY_CODE_OPTIONS));
        newConfig.setSsid(getTextValue(mSSID, ""));
        newConfig.setMode("11AC".equals(mMode.getText().toString()) ? OperationalMode._11AC : OperationalMode._11AC);
        newConfig.setChannelBW(getSelectedOption(mChannelBandwidth, Options.BANDWIDTH));
        String channel = getTextValue(mChannelNumber, "");
        if (channel.equalsIgnoreCase("Auto")) {
            newConfig.setChannel(ZERO);
        } else {
            newConfig.setChannel(Integer.parseInt(channel));
        }
        newConfig.setDdrsStatus(getSelectedOption(mDdrsStatus, Options.ENABLE_DISABLE));
        newConfig.setSpacialStream(getSelectedOption(mSpatialStream, Options.SPATIAL_STREAM));
        newConfig.setModulationIndex(getSelectedOption(mMcsIndex, mcsOptions));
        newConfig.setMinModulationIndex(getSelectedOption(mMinMcsIndex, mcsOptions));
        newConfig.setMaxModulationIndex(getSelectedOption(mMaxMcsIndex, mcsOptions));
        newConfig.setAtpcStatus(getSelectedOption(matpcStatus, Options.ENABLE_DISABLE));
        newConfig.setTranmitPower(getTextValue(mTxPower, 0));
        newConfig.setIpAddrType(getSelectedOption(mIPAddressType, Options.IP_ADDRESS_TYPE));
        newConfig.setDhcpAddress(mConfiguration.getIpAddress());
        /*if ( ipAddressType == IPAddressType.STATIC ) {
            newConfig.setDhcpAddress(mConfiguration.getDhcpAddress());
            newConfig.setDhcpMask(mConfiguration.getDhcpMask());
            newConfig.setDhcpGateway(mConfiguration.getDhcpGateway());
        }*/
        newConfig.setIpAddress(getTextValue(mIPAddress, ""));
        newConfig.setDhcpMask(mConfiguration.getNetMask());
        newConfig.setNetMask(getTextValue(mNetMask, ""));
        newConfig.setDhcpGateway(mConfiguration.getGatewayIp());
        newConfig.setGatewayIp(getTextValue(mGateway, ""));
        newConfig.setLinkId(getTextValue(mLinkId, ""));
        newConfig.setCustName(getTextValue(mCustName, ""));
        /*newConfig.setDistance(getTextValue(mDistance, 5));*/
        newConfig.setVlanStatus(getSelectedOption(mVlanStatus, Options.ENABLE_DISABLE));
        newConfig.setVlanMode(getSelectedOption(mVlanMode, Options.VLAN_MODE));
        newConfig.setVlanMgmtId(getTextValue(mVlanMgmtID, 1));
        newConfig.setVlanAccessId(getTextValue(mVlanAccID, 10));
        /*configuration.setVlanTrunkOption(getSelectedOption(mVlanTrunkOpt, Options.TRUNK_OPT));
        configuration.setVlanTrunkId(getTextValue(mVlanTrunkID,""));
        configuration.setVlanSvlanId(getTextValue(mSvlanID, 100));
        configuration.setVlanEtherType(String.valueOf(getSelectedOption(mSvlanethertype, Options.SVLAN_ETHERTYPE)));*/
        newConfig.setEncryptType(getSelectedOption(mEncrypt, Options.ENCRYPT));
        newConfig.setEncryptKey(getTextValue(mEncryptKey, ""));

        showProgress("Saving Configuration...", 1 * 1000, new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(500, 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        progress.dismiss();
                    }
                }.start();
            }
        });
        RouterService.getInstance().setNewConfiguration(newConfig);
        updateUI(newConfig);
    }

    /* View based on Login */
    private void setViewOptions() {
        String userval = mSharedPreference.getKeyUsername();
        if (userval.equalsIgnoreCase("installer")) {
            mDdrsview.setVisibility(View.GONE);
            matpcview.setVisibility(View.GONE);
            mSpatialview.setVisibility(View.GONE);
            mMcsIndexRow.setVisibility(View.GONE);
            mMinMcsIndexRow.setVisibility(View.GONE);
            mMaxMcsIndexRow.setVisibility(View.GONE);
            mTxPowerRow.setVisibility(View.GONE);
        }
    }

    /* On Change Device Mode Options */
    private void setDevmodeOptions() {
        int device_mode = getSelectedOption(mDeviceMode, Options.DEV_MODE);
        mDistval.setVisibility(View.GONE);
        if (device_mode == DeviceMode.AP) {
            mBwidthval.setVisibility(View.VISIBLE);
            mChanval.setVisibility(View.VISIBLE);
            mLinkidval.setVisibility(View.GONE);
        } else {
            mBwidthval.setVisibility(View.GONE);
            mChanval.setVisibility(View.GONE);
            mLinkidval.setVisibility(View.VISIBLE);
        }
    }

    /* Set MCS Index Options */
    private void setMCSOptions(Configuration mConfiguration) {
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

    /* Set Encryption Options */
    private void setEncryptOptions() {
        int encryptstat = getSelectedOption(mEncrypt, Options.ENCRYPT);
        if (encryptstat == Encrypt.NONE) {
            mEnckeyRow.setVisibility(View.GONE);
        } else {
            mEnckeyRow.setVisibility(View.VISIBLE);
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

    /* Redirect to the Home Activity */
    public void showHome() {
        this.startActivity(new Intent(this, HomeActivity.class));
        this.finish();
    }
}