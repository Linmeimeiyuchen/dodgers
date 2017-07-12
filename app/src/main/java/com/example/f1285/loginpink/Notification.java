package com.example.f1285.loginpink;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by f1285 on 2017/5/20.
 */

public class Notification extends Fragment {

    View rootView;

    private static final String TAG = "NotificationActivity";

    private String[] taiwan = new String[] {"基隆市", "新北市","台北市","宜蘭縣","新竹縣","桃園縣"
            ,"苗栗縣","臺中市","彰化縣","南投縣","嘉義縣","雲林縣","臺南市","高雄市","澎湖縣","金門縣"
            ,"屏東縣","台東縣","花蓮縣"};

    /*-- Address Spinner from Notification.xml --*/
    private Spinner spinnerCity;
    private Spinner spinnerTown;
    private Spinner spinnerRoad;

    ArrayAdapter<String> adapterCity;
    ArrayAdapter<String> adapterTown;
    ArrayAdapter<String> adapterRoad;

    private Button btn_notification;
    private EditText editText_lane;
    private EditText editText_alley;
    private EditText editText_no;

    String type = "";
    String city = null;
    String town = null;
    String road = null;

    //組 URL 用
    private String address = null;
    private int addDataType = 1;
    private String token = null;

    //String stringUrlTown = "http://114.44.122.179:8080/dodgers/public/index.php/findTown/";
    //String stringUrlRoad = "http://114.44.122.179:8080/dodgers/public/index.php/findRoad/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        //取得 user token
        if (!getToken(rootView.getContext(), "login", "token", "null").equals("null")){
            token = getToken(rootView.getContext(), "login", "token", "null");
            Log.d(TAG, "token = "+getToken(rootView.getContext(), "login", "token", "null"));
        }else {
            Log.d(TAG, "no token");
        }

        //get Notification.xml spinner id
        spinnerCity = (Spinner) rootView.findViewById(R.id.spinner_city);
        spinnerTown = (Spinner) rootView.findViewById(R.id.spinner_town);
        spinnerRoad = (Spinner) rootView.findViewById(R.id.spinner_road);

        //address array
        adapterCity = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, taiwan);
        adapterTown = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        adapterRoad = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        //address array put in sp1(spinner 1)
        spinnerCity.setAdapter(adapterCity);
        //set spinnerCity Listener
        spinnerCity.setOnItemSelectedListener(cityListener);
        Log.d("First", spinnerCity.toString());

        //set spinnerTown Listener
        spinnerTown.setOnItemSelectedListener(townListener);

        //set spinnerRoad Listener
        spinnerRoad.setOnItemSelectedListener(roadListener);

        editText_lane = (EditText) rootView.findViewById(R.id.editText_lane);
        editText_alley = (EditText) rootView.findViewById(R.id.editText_alley);
        editText_no = (EditText) rootView.findViewById(R.id.editText_no);

        btn_notification = (Button) rootView.findViewById(R.id.btn_notification);
        btn_notification.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG, "notification button on click!!");

                address = city+town+road;
                if( !editText_lane.getText().toString().trim().equals("") ){
                    address = address + editText_lane.getText().toString().trim() + "巷";
                }
                if( !editText_alley.getText().toString().trim().equals("") ){
                    address = address + editText_alley.getText().toString().trim() + "弄";
                }
                if( !editText_no.getText().toString().trim().equals("") ){
                    address = address + editText_no.getText().toString().trim() + "號";
                }
                Log.d(TAG, address);
                HttpPost httpPost = new HttpPost();
                httpPost.execute("http://220.134.198.4:8080/dodgers/public/index.php/addData");
            }
        });

        return rootView;
    }

    // city spinner listener
    private Spinner.OnItemSelectedListener cityListener =new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            //toast = Toast.makeText(view.getContext(), "你按了"+taiwan[position], Toast.LENGTH_LONG);
            //toast.show();
            Log.d(TAG, spinnerCity.getSelectedItem().toString());
            city = spinnerCity.getSelectedItem().toString();

            type = "city";
            //String stringUrlTown = "http://114.44.122.179:8080/dodgers/public/index.php/findTown/";
            String stringUrlTown = "http://220.134.198.4:8080/dodgers/public/index.php/findTown/";
            stringUrlTown += spinnerCity.getSelectedItem().toString();
            Log.d(TAG, stringUrlTown);
            UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();
            updateAsyncTask.execute(stringUrlTown);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    // town spinner listener
    private Spinner.OnItemSelectedListener townListener =new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            Log.d(TAG, spinnerTown.getSelectedItem().toString());
            town = spinnerTown.getSelectedItem().toString().replaceAll("\\d+","");

            type = "town";
            //String stringUrlRoad = "http://114.44.122.179:8080/dodgers/public/index.php/findRoad/";
            String stringUrlRoad = "http://220.134.198.4:8080/dodgers/public/index.php/findRoad/";
            stringUrlRoad += spinnerCity.getSelectedItem().toString();
            stringUrlRoad += "/";
            stringUrlRoad += spinnerTown.getSelectedItem().toString();
            Log.d(TAG, stringUrlRoad);
            UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();
            updateAsyncTask.execute(stringUrlRoad);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private Spinner.OnItemSelectedListener roadListener = new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            Log.d(TAG, spinnerTown.getSelectedItem().toString());
            road = spinnerRoad.getSelectedItem().toString();

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /*---------- 連結全台道路 Data DB ----------*/
    private class UpdateAsyncTask extends AsyncTask<String, Object, String> {

        HttpURLConnection connection = null;
        String jsonString1 = "";

        @Override
        protected void onPreExecute(){
            Log.d(TAG, "In UpdateAsyncTask mrthod!!");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG,"doInBackground");
            try {
                // 初始化 URL
                URL url = new URL(params[0]);
                // 取得連線物件
                connection = (HttpURLConnection) url.openConnection();
                // 設定 request timeout，讀取超時時間(毫秒)
                connection.setReadTimeout(1500);
                // 設定連線超時時間(毫秒)
                connection.setConnectTimeout(1500);
                // 設定請求方式 POST or GET
                connection.setRequestMethod("GET");
                // 建立請求
                connection.connect();
                //讀取資料
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream(),"UTF-8"));
                jsonString1 = in.readLine();
                in.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return "網路中斷";
            }
            return jsonString1;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute");
            Log.d(TAG, s);

            try {
                JSONArray jsonArray = new JSONArray(s);

                Log.d(TAG,""+jsonArray.length());

                if( type == "city" ) {
                    adapterTown.clear();
                    for (int index = 0; index < jsonArray.length(); index++) {
                        //Log.d(TAG, "" + index);
                        //Log.d(TAG, jsonArray.getString(index));
                        adapterTown.add(jsonArray.getString(index));
                    }
                    spinnerTown.setAdapter(adapterTown);
                }
                if( type == "town" ){
                    adapterRoad.clear();
                    for (int index = 0; index < jsonArray.length(); index++) {
                        //Log.d(TAG, "" + index);
                        //Log.d(TAG, jsonArray.getString(index));
                        adapterRoad.add(jsonArray.getString(index));
                    }
                    spinnerRoad.setAdapter(adapterRoad);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    /*---------- 連結全台道路 Data DB end ----------*/

    /*---------- 向 server request 臨檢站通報 ----------*/
    private class HttpPost extends AsyncTask<String, Object, String> {

        HttpURLConnection connection = null;
        String response = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d(TAG, "start");
                URL postUrl = new URL(params[0]);
                connection = (HttpURLConnection) postUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setDoOutput(true);
                String postData = "address="+address+"&type="+addDataType+"&token="+token;
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

            try {
                // json檔 : user correct : {"state":true}
                //user incorrect : {"state":false,"mes":"user not found"}
                JSONObject jsonObject = new JSONObject(s);
                //Log.d(TAG, jsonObject.getString("msg"));
                if( jsonObject.getBoolean("state") == true ){
                    Log.d(TAG, "pullover true!!");
                    Toast.makeText(rootView.getContext(), "通報成功!!", Toast.LENGTH_LONG).show();
                    //_loginButton.setEnabled(false);

                }else {
                    Log.d(TAG,"pullover false!!");
                    if( jsonObject.getString("msg").equals("this data is too near!")){
                        //Log.d(TAG, "附近臨檢站已通報");
                        Toast.makeText(rootView.getContext(), "附近臨檢站已通報!!", Toast.LENGTH_LONG).show();
                    }else {
                        if( jsonObject.getString("msg").equals("noLogin") ){
                            Toast.makeText(rootView.getContext(), "請重新登入!!", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(rootView.getContext(), "通報失敗!!", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    /*---------- 向 server request 臨檢站通報 END ----------*/

    private String getToken(Context context, String name, String key, String def){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getString(key, def);
    }

}
