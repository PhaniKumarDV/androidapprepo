package com.keywestnetworks.kwconnect.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.keywestnetworks.kwconnect.R;
import com.keywestnetworks.kwconnect.ServerPrograms.RouterService;
import com.keywestnetworks.kwconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestLTVPacket;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.AuthenticationPacket;
import com.hsq.kw.packet.vo.Configuration;

public class LoginActivity extends BaseActivity {
    EditText mTxUserName, mTxUserPassword;
    Button mBtLogin;
    boolean flag = false;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.onCreate("Login", R.id.toolbar, true);
        loginActivityInit();
    }

    private void loginActivityInit() {
        mSharedPreference = new SharedPreference(LoginActivity.this);
        progress = new ProgressDialog(this);
        progress.setMessage("Logging in...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        if (RouterService.getInstance().isUserAuthenticated()) {
            Toast.makeText(getBaseContext(), "User Already Authenticated", Toast.LENGTH_LONG).show();
            updateUI();
        }

        mTxUserName = (EditText) findViewById(R.id.Tx_UserName);
        mTxUserPassword = (EditText) findViewById(R.id.Tx_UserPassword);
        mBtLogin = (Button) findViewById(R.id.BT_SubmitButton);
        mBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                final String username = mTxUserName.getText().toString();
                final String password = mTxUserPassword.getText().toString();
                mSharedPreference.saveLogindetails(username);
                if (Validate()) {
                    userAuthenticate(username, password);
                }
            }
        });
    }

    private void userAuthenticate(String username, String password) {
        AuthenticationPacket authPacket = new AuthenticationPacket(username, password);
        final KeywestPacket authReqPacket = authPacket.getPacket();
        RouterService.getInstance().authRequest(authReqPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket innerPacket) {
                hideProgress();
                if (innerPacket != null) {
                    KeywestLTVPacket packet = innerPacket.getLTVPacketByType(1);
                    if (packet != null) {
                        byte[] status = packet.getValue();
                        if (status[0] == 1) {
                            sendConfigurationRequest();
                            RouterService.getInstance().loginSuccess();
                            updateUI();
                        } else {
                            RouterService.getInstance().loginFailed();
                        }
                    }
                }
            }
        });
    }

    public void sendConfigurationRequest() {
        KeywestPacket configRequest = new Configuration().getPacket();
        RouterService.getInstance().sendReq(configRequest, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                Configuration configuration = new Configuration(packet);
                mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(), configuration.getDeviceMode(), configuration.getIpAddress());
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progress != null) {
                    progress.hide();
                }
            }
        });
    }

    private void updateUI() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private boolean Validate() {
        flag = true;
        if (TextUtils.isEmpty(mTxUserName.getText().toString())) {
            mTxUserName.setError("Username Required");
            flag = false;
            progress.dismiss();
            return flag;
        }
        if (TextUtils.isEmpty(mTxUserPassword.getText().toString())) {
            mTxUserPassword.setError("Password Required");
            flag = false;
            progress.dismiss();
            return flag;
        }
        return flag;
    }

    @Override
    protected void updateUI(Configuration mConfiguration) {

    }
}