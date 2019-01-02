package com.hitsquadtechnologies.sifyconnect.View;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.constants.Bandwidth;
import com.hitsquadtechnologies.sifyconnect.constants.DeviceMode;
import com.hitsquadtechnologies.sifyconnect.constants.EnableDisable;
import com.hitsquadtechnologies.sifyconnect.constants.IPAddressType;
import com.hitsquadtechnologies.sifyconnect.constants.OperationalMode;
import com.hitsquadtechnologies.sifyconnect.constants.SpatialStream;
import com.hitsquadtechnologies.sifyconnect.utils.Options;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;

public class ConfigurationActivity extends BaseActivity {

    Configuration mConfiguration;
    EditText mSSID, mChannelNumber, mLinkId, mCustName, mTxPower;
    TextView mGateway, mIPAddress, mDeviceMode, mNetMask;
    Button mSetRequest;
    Spinner mChannelBandwidth, mMode, mIPAddressType, mCountryCode, mDdrsStatus,
            mSpatialStream, mMcsIndex, mMinMcsIndex, mMaxMcsIndex, matpcStatus;
    SharedPreference mSharedPreference;
    ProgressDialog progress;
    View mMinMcsIndexRow, mMaxMcsIndexRow, mMcsIndexRow, mTxPowerRow;
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
        mMode = (Spinner) findViewById(R.id.config_cmode);
        mChannelNumber = (EditText) findViewById(R.id.config_channelNumber);
        mIPAddressType = (Spinner) findViewById(R.id.config_ipaddressType);
        mDeviceMode = (TextView) findViewById(R.id.config_Devicemode);
        mIPAddress = (TextView) findViewById(R.id.config_ipaddress);
        mSetRequest = (Button) findViewById(R.id.config_setRequestButton);
        mGateway = (TextView) findViewById(R.id.config_gateway);
        mLinkId = (EditText) findViewById(R.id.config_LinkId);
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
        init();
        loadConfiguration();
    }



    /*
     * SET REQUEST TO SERVER
     *
     */
    private void init() {
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
        initSpinner(mCountryCode, Options.COUNTRY_CODE_OPTIONS);
        initSpinner(mMode, Options.OPERATIONAL_MODE);
        initSpinner(mDdrsStatus, Options.ENABLE_DISABLE);
        initSpinner(mSpatialStream, Options.SPATIAL_STREAM);
        initSpinner(matpcStatus, Options.ENABLE_DISABLE);
    }

    private void onAptcChange() {
        int value = getSelectedOption(matpcStatus, Options.ENABLE_DISABLE);
        if (value == EnableDisable.ENABLE) {
            mTxPowerRow.setVisibility(View.GONE);
        } else {
            mTxPowerRow.setVisibility(View.VISIBLE);
        }
    }

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

    private void showProgress(String message) {
        showProgress(message, 0, null);
    }

    private void showProgress(String message, int timeout, final Runnable finishCallback) {
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
                    progress.dismiss();
                    finishCallback.run();
                }
            }.start();
        }
    }

    private void loadConfiguration() {
        showProgress("Please wait fetching config...");
        RouterService.INSTANCE.sendRequest(new Configuration().getPacket(), new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(packet);
                        progress.dismiss();
                    }
                });
            }

            @Override
            public void onError(String msg, Exception e) {
                super.onError(msg, e);
                progress.dismiss();
                Log.e(ConfigurationActivity.class.getName(), "Configuration request failed: " + msg, e);
                Toast.makeText(ConfigurationActivity.this, "Error occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(KeywestPacket packet) {
        mConfiguration = new Configuration(packet);
        if (mConfiguration.getDeviceMode() == ONE) {
            mDeviceMode.setText("BSU");
        } else {
            mDeviceMode.setText("SU");
        }
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
        mLinkId.setText(Integer.toString(mConfiguration.getLinkId()));
    }

    private String getTextValue(TextView v, String defaultValue) {
        if (v.getText() != null) {
            return v.getText().toString();
        }
        return defaultValue;
    }

    private Integer getTextValue(TextView v, int defaultValue) {
        if (v.getText() != null) {
            return Integer.parseInt(v.getText().toString());
        }
        return defaultValue;
    }

    public void setConfiguration (View v) {
        final String ssid = mSharedPreference.getSsid();
        showProgress("Applying Configuration...", 35 * 1000, new Runnable() {
            @Override
            public void run() {
                connectToWifi(ssid, "");
                new CountDownTimer(3000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        loadConfiguration();
                    }
                }.start();
            }
        });
        Configuration configuration = new Configuration();
        configuration.setDeviceMode("BSU".equals(mDeviceMode.getText().toString()) ? DeviceMode.BSU : DeviceMode.SU);
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
        configuration.setLinkId(stringToInt(getTextValue(mLinkId, "")));
        configuration.setCustName(getTextValue(mCustName, ""));

        KeywestPacket setpacket = configuration.buildPacketFromUI();
        RouterService.INSTANCE.sendRequest(setpacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(KeywestPacket packet) {
            }
        });
    }

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

    private void setMCSOptions() {
        int spatialStream = getSelectedOption(mSpatialStream, Options.SPATIAL_STREAM);
        int ddrs = getSelectedOption(mDdrsStatus, Options.ENABLE_DISABLE);
        int start = spatialStream == SpatialStream.DUAL ? 10 : 0;
        int end = start + 10;
        mcsOptions = new Options();
        for (int i = start; i < end; i++) {
            mcsOptions.add(i, "MCS"+ i);
        }
        initSpinner(mMcsIndex, mcsOptions);
        initSpinner(mMinMcsIndex, mcsOptions);
        initSpinner(mMaxMcsIndex, mcsOptions);
        int defaultMaxValue = mcsOptions.findPositionByKey(end - 1);
        if (ddrs == EnableDisable.ENABLE) {
            mMcsIndexRow.setVisibility(View.GONE);
            mMinMcsIndexRow.setVisibility(View.VISIBLE);
            mMaxMcsIndexRow.setVisibility(View.VISIBLE);
        } else {
            mMcsIndexRow.setVisibility(View.VISIBLE);
            mMinMcsIndexRow.setVisibility(View.GONE);
            mMaxMcsIndexRow.setVisibility(View.GONE);
        }
        if (mConfiguration != null) {
            mMcsIndex.setSelection(mcsOptions.findPositionByKey(mConfiguration.getModulationIndex()));
            mMinMcsIndex.setSelection(mcsOptions.findPositionByKey(mConfiguration.getMinModulationIndex()));
            mMaxMcsIndex.setSelection(mcsOptions.findPositionByKey(mConfiguration.getMaxModulationIndex(), defaultMaxValue));
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
}