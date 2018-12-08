package com.hitsquadtechnologies.sifyconnect.View;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationActivity extends BaseActivity {

    Configuration mConfiguration;
    EditText mSSID, mChannelNumber, mLinkId, mCustName;
    TextView mGateway, mIPAddress, mDeviceMode, mNetMask;
    Button mSetRequest;
    Spinner mChannelBandwidth, mMode, mIPAddressType, mCountryCode;
    SharedPreference mSharedPreference;
    ProgressDialog progress;
    public static final int COUNTRYCODE_UL = 5016;
    public static final int COUNTRYCODE_L = 5017;
    public static final int COUNTRYCODE_R = 5011;
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
        mConfiguration = new Configuration();
        init();
        loadConfiguration();
    }



    /*
     * SET REQUEST TO SERVER
     *
     */
    private void init() {
        mChannelBandwidth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    mConfiguration.setChannelBW(position + 3);
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
                if (item != null) {
                    mConfiguration.setMode(position + 1);
                    setBWvalues(position);
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
                if (item != null) {
                    mConfiguration.setIpAddrType(position + 1);
                    if (item.toString().equalsIgnoreCase("Dynamic")) {
                        mIPAddress.setClickable(false);
                        mIPAddress.setEnabled(false);
                        mGateway.setEnabled(false);
                    }
                    if (item.toString().equalsIgnoreCase("Static")) {
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
                if (item != null) {
                    if (position == ZERO) {
                        mConfiguration.setCountryCode(COUNTRYCODE_UL);
                    }
                    if (position == ONE) {
                        mConfiguration.setCountryCode(COUNTRYCODE_L);
                    }
                    if (position == TWO) {
                        mConfiguration.setCountryCode(COUNTRYCODE_R);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if (mDeviceMode.getText().toString().equalsIgnoreCase("BSU")) {
            mConfiguration.setDeviceMode(ONE);
        } else {
            mConfiguration.setDeviceMode(TWO);
        }
    }

    private void showProgress(String message) {

        progress = new ProgressDialog(this);
        progress.setMessage(message);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
    }

    private void loadConfiguration() {
        showProgress("Please wait fetching config...");
        mConfiguration = new Configuration();
        RouterService.INSTANCE.sendRequest(mConfiguration.getPacket(), new RouterService.Callback<KeywestPacket>() {
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
        Configuration configuration = new Configuration(packet);

        mSSID.setText(configuration.getSsid());
        mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(), configuration.getDeviceMode(), configuration.getIpAddress());

        int bw = configuration.getChannelBW();
        mChannelBandwidth.setSelection(bw - 1);
        mConfiguration.setChannelBW(bw);

        int Mode = configuration.getMode();
        setBWvalues(Mode);
        mMode.setSelection(Mode - 1);
        mConfiguration.setMode(Mode);

        switch (configuration.getCountryCode()) {

            case COUNTRYCODE_UL:
                mCountryCode.setSelection(ZERO);
                mConfiguration.setCountryCode(COUNTRYCODE_UL);
                break;

            case COUNTRYCODE_L:
                mCountryCode.setSelection(ONE);
                mConfiguration.setCountryCode(COUNTRYCODE_L);
                break;

            case COUNTRYCODE_R:
                mCountryCode.setSelection(TWO);
                mConfiguration.setCountryCode(COUNTRYCODE_R);
                break;

            default:
                break;
        }

        if (configuration.getIpAddrType() == ONE) {
            mIPAddressType.setSelection(0);
            mConfiguration.setIpAddrType(configuration.getIpAddrType());
            mIPAddress.setText(configuration.getIpAddress());
            mGateway.setText(configuration.getGatewayIp());
        }
        if (configuration.getIpAddrType() == TWO) {
            mIPAddressType.setSelection(1);
            mIPAddress.setClickable(false);
            mIPAddress.setEnabled(false);
            mConfiguration.setIpAddrType(configuration.getIpAddrType());
            mIPAddress.setText(configuration.getIpAddress());
            mGateway.setText(configuration.getGatewayIp());
        }

        if (configuration.getDeviceMode() == ONE) {
            mDeviceMode.setText("BSU");
        } else {
            mDeviceMode.setText("SU");
        }

        mNetMask.setText(configuration.getNetMask());
        mCustName.setText(configuration.getCustName());
        mLinkId.setText(Integer.toString(configuration.getLinkId()));

        if (configuration.getChannel() == ZERO) {
            mChannelNumber.setText("Auto");
        } else {
            mChannelNumber.setText(Integer.toString(configuration.getChannel()));
        }
    }

    public void setConfiguration (View v) {
        showProgress("Loading...");

        new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                progress.dismiss();
            }
        }.start();

        String ssid = mSSID.getText().toString();
        if (ssid != null) {
            mConfiguration.setSsid(mSSID.getText().toString());
        } else {
            mConfiguration.setSsid("");
        }
        String ipaddress = mIPAddress.getText().toString();
        if (ipaddress != null) {
            mConfiguration.setIpAddress(mIPAddress.getText().toString());
        } else {
            mConfiguration.setIpAddress("");
        }
        if (mChannelNumber.getText().toString().equalsIgnoreCase("Auto")) {
            mConfiguration.setChannel(ZERO);
        } else {
            mConfiguration.setChannel(Integer.parseInt(mChannelNumber.getText().toString()));
        }
        String gateway = mGateway.getText().toString();
        if (gateway != null) {
            mConfiguration.setGatewayIp(mGateway.getText().toString());
        } else {
            mConfiguration.setGatewayIp("");
        }

        mConfiguration.setNetMask(mNetMask.getText().toString());
        mConfiguration.setCustName(mCustName.getText().toString());
        mConfiguration.setLinkId(stringToInt(mLinkId.getText().toString()));

        KeywestPacket setpacket = mConfiguration.buildPacketFromUI();
        RouterService.INSTANCE.sendRequest(setpacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(KeywestPacket packet) {
                progress.dismiss();
            }
        });
    }

    private void setBWvalues(int strMode) {
        List<String> bandwidth_arrays = new ArrayList<String>();
        bandwidth_arrays.clear();

        if (strMode == ZERO) {
            bandwidth_arrays.add("20MHz");
        }
        if (strMode == ONE) {
            bandwidth_arrays.add("20MHz");
            bandwidth_arrays.add("40MHz");
            bandwidth_arrays.add("40MHz-");
        }
        if (strMode == TWO) {
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
