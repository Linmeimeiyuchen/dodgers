package com.example.f1285.loginpink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObservable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    Session session = new Session();

    String email = null;
    String password = null;

    //private ArrayAdapter<String> emailAdapter;
    //private AutoCompleteTextView mEmailView;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;
    @BindView(R.id.checkBox_pwMemory) CheckBox _checkBox_pwMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if( getEmailPw(LoginActivity.this, "loginEmail", "email", "設定檔未儲存任何資料") != "設定檔未儲存任何資料"){
            _emailText.setText( getEmailPw(LoginActivity.this, "loginEmail", "email", "設定檔未儲存任何資料") );
        }

        if( getPassword(LoginActivity.this, "loginPassword", "password", "設定檔未儲存任何資料") != "設定檔未儲存任何資料" ){
            _passwordText.setText( getPassword(LoginActivity.this, "loginPassword", "password", "設定檔未儲存任何資料") );
            _checkBox_pwMemory.setChecked(true);
        }

        // LoginButton 事件
        _loginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                if(checkNetwork()){
                    login();
                }else {
                    Toast.makeText(LoginActivity.this, "網路未連接!!請檢查!!", Toast.LENGTH_LONG).show();
                }
            }
        } );

        // SignUp 事件
        _signupLink.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

    }

    public void login(){
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        // Read email and password in textView
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        Log.d(TAG, email);
        Log.d(TAG, password);

        if( !_checkBox_pwMemory.isChecked() ){
            removePassword(LoginActivity.this, "loginPassword", "password");
        }

        HttpPost httpPost = new HttpPost();
        httpPost.execute("http://220.134.198.4:8080/dodgers/public/index.php/auth");

    }

    // 判斷 email 和 password 合理
    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            _emailText.setError("enter a valid email address");
            valid = false;
        }else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        }else  {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        /* 和 DB 溝通 */


        //finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    //URL postUrl = new URL("http://220.134.198.4:8080/dodgers/public/index.php/auth");

    /*---------- 向 server 做 user email and password Request ----------*/
    private class HttpPost extends AsyncTask<String, Object, String>{

        HttpURLConnection connection = null;
        String response = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL postUrl = new URL(params[0]);
                connection = (HttpURLConnection) postUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setDoOutput(true);
                String postData = "account="+email+"&password="+password;
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
            //Log.d(TAG, s);
            if( s != null ){

                try {
                    // json檔 : user correct : {"state":true}
                    //user incorrect : {"state":false,"mes":"user not found"}
                    JSONObject jsonObject = new JSONObject(s);
                    if( jsonObject.getBoolean("state") == true ){
                        Log.d(TAG, "User true!!");
                        _loginButton.setEnabled(false);

                        session.setSession(LoginActivity.this, "login", "token", jsonObject.getString("token"));
                        //setLoginData(LoginActivity.this, "login", "token", jsonObject.getString("token"));
                        Log.d(TAG, "token = "+jsonObject.getString("token"));
                        //setLoginData(LoginActivity.this, "login", "userName", jsonObject.getString("name"));
                        //Log.d(TAG, jsonObject.getString("name"));

                        //顯示載入中 Dialog
                        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Authenticating...");
                        progressDialog.show();
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {

                                        onLoginSuccess();
                                        progressDialog.dismiss();
                                    }
                                }, 3000);

                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this,PulloverActivity.class);
                        startActivity(intent);

                        //儲存登入成功密碼 下次登入直接導入
                        setEmailPw(LoginActivity.this, "loginEmail", "email", email);
                        if( _checkBox_pwMemory.isChecked()){
                            setPassword(LoginActivity.this, "loginPassword", "password", password);
                        }
                    }else {
                        switch (jsonObject.getString("msg")){
                            case "unverify Email":
                                Toast.makeText(LoginActivity.this, "帳號未認證!!", Toast.LENGTH_LONG).show();
                                break;
                            case "user not found":
                                Toast.makeText(LoginActivity.this, "Email或密碼錯誤!!", Toast.LENGTH_LONG).show();
                                break;
                        }
                        //Log.d(TAG, jsonObject.getString("msg"));

                        //Log.d(TAG,"User false!!");
                        //Toast.makeText(LoginActivity.this, "Email或密碼錯誤!!", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    _loginButton.setEnabled(true);
                    Log.d(TAG, "json 錯誤");
                    e.printStackTrace();
                }

            }else {
                Toast.makeText(LoginActivity.this, "伺服器忙碌中...", Toast.LENGTH_LONG).show();
            }

        }
    }
    /*---------- 向 server 做 user email and password Request END ----------*/

    private void setLoginData(Context context, String name, String key, String value){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(key, value);
        PE.commit();
    }

    // 儲存登入成功 Email
    public void setEmailPw(Context context, String name, String key, String value){
        SharedPreferences settings = context.getSharedPreferences(name, 0);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(key, value);
        PE.commit();
        Log.d(TAG, "ID設定檔儲存資料");
    }

    public void setPassword(Context context, String password, String key, String value){
        SharedPreferences settings = context.getSharedPreferences(password, 0);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(key, value);
        PE.commit();
        Log.d(TAG, "PW設定檔儲存資料");
    }

    // 讀取上次成功 Email
    public String getEmailPw(Context context, String name, String key, String def){
        SharedPreferences settings = context.getSharedPreferences(name, 0);
        return settings.getString(key, def);
    }

    public String getPassword(Context context, String name, String key, String def){
        SharedPreferences settings = context.getSharedPreferences(name, 0);
        return settings.getString(key, def);
    }

    public void removePassword(Context context, String password, String key) {
        SharedPreferences settings = context.getSharedPreferences(password, 0);
        SharedPreferences.Editor PE = settings.edit();
        PE.remove(key);
        PE.commit();
    }

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
