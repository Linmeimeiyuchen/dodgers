package com.example.f1285.loginpink;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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


public class PulloverActivity extends AppCompatActivity {

    private static final String TAG = "PulloverActivity";

    MenuItem item_range;
    MenuItem item_remindDistance;
    MenuItem item_remindMode;
    MenuItem item_setting;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    TextView longitude;
    TextView latitude;
    TextView dialog_longitude;
    TextView dialog_latitude;
    TextView user_name;

    //組 URL 用
    private String str_longitude = null;
    private String str_latitude = null;
    private int addDataType = 1;
    private String str_token = null;

    //fragment宣告
    Pullover pullover_fragment = new Pullover();
    Notification notification_fragment = new Notification();
    About about_fragment = new About();

    public int range = 10;
    private String type = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pullover);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Pullover System");
        getSupportActionBar().setTitle("Dodgers");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //取得 token
        if( !getLoginData(this, "login", "token", "null").equals("null") ){
            str_token = getLoginData(this, "login", "token", "null");
            Log.d(TAG, "token = "+str_token);
        }else {
            Log.d(TAG, "no token");
        }

        user_name = (TextView) findViewById(R.id.toolbar_user_name);
        //取得 user name
        if( !getLoginData(this, "login", "name", "null").equals("null") ){
            user_name.setText( getLoginData(this, "login", "name", "null"));
        }else {
            user_name.setText("null");
        }

        // 觸動一鍵回報
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PulloverActivity.this);
                LayoutInflater inflater = PulloverActivity.this.getLayoutInflater();
                View ckDialogView = inflater.inflate(R.layout.ck_dialog, null);

                Log.d(TAG, "token = "+str_token);

                // pullover fragment 裡面的 textView
                longitude = (TextView) findViewById(R.id.textView3);
                latitude = (TextView) findViewById(R.id.textView5);
                // check dialog 裡面的 textView
                dialog_longitude = (TextView) ckDialogView.findViewById(R.id.dialog_longitude);
                dialog_latitude = (TextView) ckDialogView.findViewById(R.id.dialog_latitude);

                str_longitude = (String) longitude.getText();
                str_latitude = (String) latitude.getText();
                dialog_longitude.setText( str_longitude );
                dialog_latitude.setText( str_latitude );

                final AlertDialog alertDialog = builder.setView( ckDialogView )
                        .setPositiveButton("通報", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                type = "notification";      //type 設定為通報
                                HttpPost httpPost = new HttpPost();
                                httpPost.execute("http://220.134.198.4:8080/dodgers/public/index.php/addData");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                alertDialog.show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pullover, menu);
        item_range = menu.findItem(R.id.range);
        item_remindDistance = menu.findItem(R.id.remind_distance);
        item_remindMode = menu.findItem(R.id.remind_mode);
        item_setting = menu.findItem(R.id.setting);

        replySetting( item_range, item_remindDistance, item_remindMode, item_setting );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        switch (id){
            /*------ 臨檢站範圍 menu item ------*/
            case R.id.range_1:
                pullover_fragment.setRange(1);
                item.setChecked(true);
                rememberSetting(this, "setting", "range", 1);
                Log.d(TAG,"range_1");
                break;
            case R.id.range_3:
                pullover_fragment.setRange(3);
                item.setChecked(true);
                rememberSetting(this, "setting", "range", 3);
                Log.d(TAG,"range_3");
                break;
            case R.id.range_5:
                pullover_fragment.setRange(5);
                item.setChecked(true);
                rememberSetting(this, "setting", "range", 5);
                Log.d(TAG,"range_5");
                break;
            case R.id.range_10:
                pullover_fragment.setRange(10);
                rememberSetting(this, "setting", "range", 10);
                item.setChecked(true);
                Log.d(TAG,"range_10");
                break;
            case R.id.range_30:
                pullover_fragment.setRange(30);
                item.setChecked(true);
                rememberSetting(this, "setting", "range", 30);
                Log.d(TAG,"range_30");
                break;
            case R.id.range_50:
                pullover_fragment.setRange(50);
                item.setChecked(true);
                rememberSetting(this, "setting", "range", 50);
                Log.d(TAG,"range_50");
                break;

            /*------- 離醒距離 menu item -------*/
            case R.id.distance_100m:
                pullover_fragment.setRemindDistance(100);
                item.setChecked(true);
                rememberSetting(this, "setting", "remindDistance", 100);
                break;
            case R.id.distance_300m:
                pullover_fragment.setRemindDistance(300);
                item.setChecked(true);
                rememberSetting(this, "setting", "remindDistance", 300);
                break;
            case R.id.distance_500m:
                pullover_fragment.setRemindDistance(500);
                item.setChecked(true);
                //rememberSetting(this, "setting", "remindDistance", 500);
                break;
            case R.id.distance_1km:
                pullover_fragment.setRemindDistance(1000);
                item.setChecked(true);
                rememberSetting(this, "setting", "remindDistance", 1000);
                break;

            /*------- 提醒方式 menu item -------*/
            case R.id.shock:
                if(item.isChecked()){
                    item.setChecked(false);
                    pullover_fragment.setRemindMode("shock off");
                    rememberSetting(this, "setting", "remind_shock", 0);
                    Log.d(TAG,"shock off");
                }else {
                    item.setChecked(true);
                    pullover_fragment.setRemindMode("shock on");
                    rememberSetting(this, "setting", "remind_shock", 1);
                    Log.d(TAG,"shock on");
                }
                break;
            case R.id.sound:
                if(item.isChecked()){
                    item.setChecked(false);
                    pullover_fragment.setRemindMode("sound off");
                    rememberSetting(this, "setting", "remind_sound", 0);
                    //Log.d(TAG,"sound off");
                }else {
                    item.setChecked(true);
                    pullover_fragment.setRemindMode("sound on");
                    rememberSetting(this, "setting", "remind_sound", 1);
                    //Log.d(TAG,"sound on");
                }
                break;

            /*------- Setting menu item -------*/
            case R.id.hide_nearPullover:
                if(item.isChecked()){
                    item.setChecked(false);
                    pullover_fragment.setHidNearPullover(false);
                    rememberSetting(this, "setting", "hidNearPullover", 0);
                }else {
                    item.setChecked(true);
                    pullover_fragment.setHidNearPullover(true);
                    rememberSetting(this, "setting", "hidNearPullover", 1);
                }
                break;

            case R.id.logout:
                type = "logout";
                LogoutAsyncTask logoutAsyncTask = new LogoutAsyncTask();
                logoutAsyncTask.execute("http://220.134.198.4:8080/dodgers/public/index.php/logout");
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0:
                    return pullover_fragment;
                case 1:
                    return notification_fragment;
                case 2:
                    return about_fragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    //return "Pullover";
                    return "提醒系統";
                case 1:
                    //return "Notification";
                    return "通報系統";
                case 2:
                    //return "About";
                    return "關於";
                default:
                    return null;
            }
            //return null;
        }
    }

    /*---------- 向 server request 臨檢站通報 ----------*/
    private class HttpPost extends AsyncTask<String, Object, String> {

        HttpURLConnection connection = null;
        String response = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d(TAG, "start");
                Log.d(TAG, params[0]);
                URL postUrl = new URL(params[0]);
                connection = (HttpURLConnection) postUrl.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                String postData = "longitude="+str_longitude+"&latitude="+str_latitude+"&type="+addDataType+"&token="+str_token;
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
                    //user nologin : {"state": false,"msg": "noLogin"}
                    JSONObject jsonObject = new JSONObject(s);
                    //Log.d(TAG, jsonObject.getString("msg"));
                    if( jsonObject.getBoolean("state") == true ){
                        Log.d(TAG, "pullover true!!");
                        Toast.makeText(PulloverActivity.this, "通報成功!!", Toast.LENGTH_LONG).show();
                        //_loginButton.setEnabled(false);

                    }else {
                        Log.d(TAG,"pullover false!!");
                        if( jsonObject.getString("msg").equals("this data is too near!")){
                            //Log.d(TAG, "附近臨檢站已通報");
                            Toast.makeText(PulloverActivity.this, "附近臨檢站已通報!!", Toast.LENGTH_LONG).show();
                        }else {
                            if( jsonObject.getString("msg").equals("noLogin") ){
                                Toast.makeText(PulloverActivity.this, "請重新登入!!", Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(PulloverActivity.this, "通報失敗!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    /*---------- 向 server request 臨檢站通報 END ----------*/

    /*---------- Logout ----------*/
    private class LogoutAsyncTask extends AsyncTask<String, Object, String> {

        HttpURLConnection connection = null;
        String response = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d(TAG, "start");
                Log.d(TAG, params[0]);
                URL postUrl = new URL(params[0]);
                connection = (HttpURLConnection) postUrl.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                String postData = "token="+str_token;
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
                    //user nologin : {"state": false,"msg": "noLogin"}
                    JSONObject jsonObject = new JSONObject(s);
                    //Log.d(TAG, jsonObject.getString("msg"));
                    if( jsonObject.getBoolean("state") == true ){
                        Log.d(TAG, "Logout true!!");
                        Toast.makeText(PulloverActivity.this, "登出成功!!", Toast.LENGTH_LONG).show();
                        //_loginButton.setEnabled(false);

                    }else {
                        Toast.makeText(PulloverActivity.this, "登出成功!!", Toast.LENGTH_LONG).show();
                        Log.d(TAG,"登出失敗!!");
                    }

                } catch (JSONException e) {
                    Toast.makeText(PulloverActivity.this, "登出成功!!", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"登出失敗!!");
                    e.printStackTrace();
                }
            }

        }
    }
    /*---------- Logout END ----------*/

    private void replySetting( MenuItem item_range, MenuItem item_remindDistance, MenuItem item_remindMode, MenuItem item_setting ){

        if( getRememberSetting(this, "setting", "range", 0) != 0 ){
            switch ( getRememberSetting(this, "setting", "range", 0) ){
                case 1:
                    setMenuItem( item_range, R.id.range_1, true );
                    break;
                case 3:
                    setMenuItem( item_range, R.id.range_3, true );
                    break;
                case 5:
                    setMenuItem( item_range, R.id.range_5, true );
                    break;
                case 10:
                    setMenuItem( item_range, R.id.range_10, true );
                    break;
                case 30:
                    setMenuItem( item_range, R.id.range_30, true );
                    break;
                case 50:
                    setMenuItem( item_range, R.id.range_50, true );
                    break;
            }}
        if( getRememberSetting(this, "setting", "remindDistance", 0) != 0 ){
            switch (getRememberSetting(this, "setting", "remindDistance", 0)){
                case 100:
                    setMenuItem( item_remindDistance, R.id.distance_100m, true );
                    break;
                case 300:
                    setMenuItem( item_remindDistance, R.id.distance_300m, true );
                    break;
                case 500:
                    setMenuItem( item_remindDistance, R.id.distance_500m, true );
                    break;
                case 1000:
                    setMenuItem( item_remindDistance, R.id.distance_1km, true );
                    break;
            }}
        if( getRememberSetting(this, "setting", "remind_shock", 2) != 2 ){
            switch (getRememberSetting(this, "setting", "remind_shock", 2)){
                case 0:
                    setMenuItem( item_remindMode, R.id.shock, false );
                    break;
                case 1:
                    setMenuItem( item_remindMode, R.id.shock, true );
                    break;
            }}
        if( getRememberSetting(this, "setting", "remind_sound", 2) != 2 ){
            switch (getRememberSetting(this, "setting", "remind_sound", 2)){
                case 0:
                    setMenuItem( item_remindMode, R.id.sound, false );
                    break;
                case 1:
                    setMenuItem( item_remindMode, R.id.sound, true );
                    break;
            }}
        if( getRememberSetting(this, "setting", "hidNearPullover", 2) != 2 ){
            switch (getRememberSetting(this, "setting", "hidNearPullover", 2)){
                case 0:
                    setMenuItem( item_setting, R.id.hide_nearPullover, false );
                    break;
                case 1:
                    setMenuItem( item_setting, R.id.hide_nearPullover, true );
                    break;
            }}
    }

    private void setMenuItem(MenuItem item, int id, boolean b){
        item.getSubMenu().findItem(id).setChecked(b);
    }

    private void rememberSetting(Context context, String name, String key, int value){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putInt(key, value);
        PE.commit();
        Log.d(TAG, "範圍/距離設定檔存入" );
        Log.d(TAG, name+" "+key+" "+value);
    }

    private int getRememberSetting(Context context, String name, String key, int def){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getInt(key, def);
    }

    private String getLoginData(Context context, String name, String key, String def){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getString(key, def);
    }
}
