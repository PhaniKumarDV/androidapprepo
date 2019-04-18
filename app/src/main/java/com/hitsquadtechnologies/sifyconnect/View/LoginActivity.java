package com.hitsquadtechnologies.sifyconnect.View;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hitsquadtechnologies.sifyconnect.R;
import com.hitsquadtechnologies.sifyconnect.ServerPrograms.RouterService;
import com.hitsquadtechnologies.sifyconnect.utils.SharedPreference;
import com.hsq.kw.packet.KeywestLTVPacket;
import com.hsq.kw.packet.KeywestPacket;
import com.hsq.kw.packet.vo.AuthenticationPacket;
import com.hsq.kw.packet.vo.Configuration;

public class LoginActivity extends AppCompatActivity {

    EditText mTxUserName,mTxUserPassword;
    Button mBtLogin,mBtSignup,mTestLog,mTestsigin;
    boolean flag = false;
    ProgressDialog progress;
    int MY_PERMISSIONS_REQUEST_LOCATION = 1001;
    SharedPreference mSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainctivity);

            initialization();
            setPermissions();
    }

    public void setPermissions()
    {

        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);

        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);

    }

   /* @Override
    public void onStart() {
        *//*super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUI();
        }*//*
    }*/

    private  void initialization()
    {
        mSharedPreference = new SharedPreference( LoginActivity.this );
        progress = new ProgressDialog(this);
        progress.setMessage("Logging in...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        mTxUserName = (EditText)findViewById(R.id.Tx_UserName);
        mTxUserPassword = (EditText)findViewById(R.id.Tx_UserPassword);
        mBtLogin = (Button) findViewById(R.id.BT_SubmitButton);
        //mBtSignup = (Button) findViewById(R.id.BT_signin);
        //mTestsigin = (Button) findViewById(R.id.sigin);
        //mTestLog = (Button) findViewById(R.id.Login);


        /*mTestsigin.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity.this,signinActivity.class);
                startActivity(intent);

            }
        });*/

        mBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                progress.show();
                final String usrname = mTxUserName.getText().toString();
                final String password = mTxUserPassword.getText().toString();
                if (validation())
                {
                    authentication(usrname, password);
                }
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                    }
                });*/
            }
        });

        /*mBtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,signinActivity.class);
                startActivity(intent);
            }
        });*/
    }


    private void authentication(String usrname,String password)
    {
        // from here
        AuthenticationPacket authenticationPacket = new AuthenticationPacket(usrname,password);
        final KeywestPacket authRequestPacket = authenticationPacket.getPacket();
        RouterService.getInstance().authRequest(authRequestPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket innerPacket) {
                hideProgress();
                if (innerPacket != null) {
                    KeywestLTVPacket packet = innerPacket.getLTVPacketByType(1);
                    if (packet != null) {
                        byte [] status = packet.getValue();
                        if (status[0] == 1) {
                            // call configuation Request
                            //activity.showToast("Authentication success. sending config request");
                            //Toast.makeText(LoginActivity.this, "Authentication Success", Toast.LENGTH_LONG).show();
                            sendConfigurationRequest();
                            RouterService.getInstance().loginSuccess();
                            //this.startActivity( new Intent( this, LoginActivity.class ) );
                            updateUI();
                        } else {
                            // show toast

                            RouterService.getInstance().loginFailed();
                            //Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                            //activity.showToast("Authentication failed");
                        }
                    }
                }
                        /*Configuration configuration = new Configuration(innerPacket);
                        String ipAddress = configuration.getIpAddress();
                        if (configuration.getIpAddrType() == 2) {
                            ipAddress = configuration.getDhcpAddress();
                        }
                        mSharedPreference.saveLocalDeviceValues(configuration.getDeviceMac(), configuration.getDeviceMode(), ipAddress);*/
            }
        });
        /*mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progress.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            progress.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
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
    private void updateUI()
    {
        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent);
    }

    private boolean validation()
    {
        flag = true;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

       /* if (!mTxUserName.getText().toString().matches(emailPattern))
        {
            mTxUserName.setError("invalid email");
            flag = false;
            progress.dismiss();
            return flag;

        }*/

        if (TextUtils.isEmpty(mTxUserName.getText().toString()))
        {
            mTxUserName.setError("Username Required");
            flag = false;
            progress.dismiss();
            return flag;

        }

        if (TextUtils.isEmpty(mTxUserPassword.getText().toString()))
        {
            mTxUserPassword.setError("Password Required");
            flag = false;
            progress.dismiss();
            return flag;
        }

     return flag;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        /*if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }*/
    }

   /* @Override
    public void onBackPressed() {
        if (RouterService.getInstance().isUserAuthenticated()) {
            super.onBackPressed();
        }
    }*/
}
