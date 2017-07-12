package com.example.f1285.loginpink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    String name = null;
    String email = null;
    String password = null;
    String password_check = null;

    @BindView(R.id.signup_name) TextView _nameText;
    @BindView(R.id.signup_email) TextView _emailText;
    @BindView(R.id.signup_password) TextView _passwordText;
    @BindView(R.id.signup_password_check) TextView _passwordCheckText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);


        // when user click signup button
        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                if(checkNetwork()) {
                    signup();
                }else {
                    Toast.makeText(SignupActivity.this, "網路未連接!!請檢查!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        // when user click login link
        _loginLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick( View view ){
                finish();
            }
        });
    }

    public void signup(){
        Log.d(TAG, "Signup");

        if( !validate() ){
            onSignupFailed();
            return;
        }

        HttpPost httpPost = new HttpPost();
        httpPost.execute("http://220.134.198.4:8080/dodgers/public/index.php/createUser");
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);     //用來回傳值給呼叫此 Activity 之 class

        /*  和 DB 溝通 */
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText( getBaseContext(), "Signup failed", Toast.LENGTH_LONG ).show();

        _signupButton.setEnabled(true);
    }

    // 檢視是否有效
    public boolean validate(){
        boolean valid = true;

        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        password_check = _passwordCheckText.getText().toString();

        // name 不得為空或是小於3個字元
        if( name.isEmpty() || name.length() < 3 ){
            _nameText.setError("at least 3 characters");
            valid = false;
        }else {
            _nameText.setError(null);
        }

        // email 不得為空，並交由 android去判別是否為email格式
        if( email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
            _emailText.setError("enter a valid email address");
            valid = false;
        }else {
            _passwordText.setError(null);
        }

        // password 不得為空，且字元數需界於 4~10
        if( password.isEmpty() || password.length() < 4 || password.length() > 10 ){
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        }else {
            // 檢查 comfirm password 是否和 password 一樣
            //Log.d(TAG, password);
            //Log.d(TAG, password_check);
            if( password.equals(password_check) ){
                _passwordText.setError(null);
            }else {
                _passwordCheckText.setError("password and confirm password is different");
                valid = false;
            }
        }

        return valid;
    }

    /*---------- 向 server 做 user email and password Request ----------*/
    private class HttpPost extends AsyncTask<String, Object, String> {

        HttpURLConnection connection = null;
        String response = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL postUrl = new URL(params[0]);
                connection = (HttpURLConnection) postUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(1500);
                connection.setReadTimeout(1500);
                connection.setDoOutput(true);
                String postData = "name="+name+"&account="+email+"&password="+password;
                OutputStream out = connection.getOutputStream();
                out.write(postData.getBytes());
                out.flush();
                out.close();

                int responseCode = connection.getResponseCode();
                if(responseCode == 200){

                    Log.d(TAG, "OK!!");
                    InputStream in = connection.getInputStream();
                    BufferedReader read = new BufferedReader(new InputStreamReader(in));
                    response = read.readLine();
                    Log.d(TAG, response);

                }else {
                    Log.d(TAG, "Out!!");
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if( s != null ){
                try {
                    // json檔 : user correct : {"state":true}
                    //user incorrect : {"state":false,"mes":"user not found"}
                    JSONObject jsonObject = new JSONObject(s);
                    if( jsonObject.getBoolean("state") == true ){
                        Log.d(TAG, "Signup true!!");
                        _signupButton.setEnabled(false);

                        //顯示載入中 Dialog
                        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Authenticating...");
                        progressDialog.show();
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {

                                        onSignupSuccess();
                                        progressDialog.dismiss();
                                    }
                                }, 5000);
                        Toast.makeText(SignupActivity.this, "註冊成功!!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(SignupActivity.this, "請至Email收取認證信件!!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setClass(SignupActivity.this,LoginActivity.class);
                        startActivity(intent);

                    }else {
                        Log.d(TAG,"Signup false!!");
                        Toast.makeText(SignupActivity.this, "註冊失敗!!", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else {
                Toast.makeText(SignupActivity.this, "伺服器忙碌中...", Toast.LENGTH_LONG).show();
            }

        }
    }
    /*---------- 向 server 做 user email and password Request END ----------*/

    // 檢查網路是否開啟
    private boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

}
