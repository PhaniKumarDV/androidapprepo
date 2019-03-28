package com.hitsquadtechnologies.sifyconnect.View;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hitsquadtechnologies.sifyconnect.R;

public class signinActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword,mTestLog,mTestsigin;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initialization();

    }

    private void initialization(){

        auth = FirebaseAuth.getInstance();
        //btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //btnResetPassword = (Button) findViewById(R.id.btn_reset_password);
        //mTestsigin = (Button) findViewById(R.id.sigin);
        //mTestLog = (Button) findViewById(R.id.Login);

        registerToFirebase();

    }

    private void registerToFirebase(){


       /* mTestLog.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(signinActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });*/

       /* btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (validation())
                {
                   // authentication(email,password);
                }
            }
        });*/

      /*  btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(signinActivity.this, LoginActivity.class));
                finish();
            }
        });*/


        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(signinActivity.this, ResetPasswordActivity.class));
            }
        });
    }


    private void authentication(String email,String password)
    {
        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(signinActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Toast.makeText(signinActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful())
                        {
                            Toast.makeText(signinActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(signinActivity.this, DiscoveryActivity.class));
                            finish();
                        }
                    }
                });
    }



    private boolean validation()
    {
        flag = true;
        if (TextUtils.isEmpty(inputEmail.getText().toString().trim()))
        {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            flag = false;
            return flag;
        }

        if (TextUtils.isEmpty(inputPassword.getText().toString().trim()))
        {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            flag = false;
            return flag;
        }

        if (inputPassword.getText().toString().trim().length() < 6)
        {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            flag = false;
            return flag;
        }
        return flag;
    }

}
