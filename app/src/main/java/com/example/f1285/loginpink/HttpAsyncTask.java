package com.example.f1285.loginpink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*---------- AsyncTask ----------*/
// 有四個 method
// 1.onPreExecute 可以設置或修改 UI，例如顯示進度條
// 2.doInBackground 在後台執行，不可修改 UI
// 3.onProgressUpdate 可以設置或修改 UI，參數需和 AsyncTask 第二參數型態一樣
// 4.onPostExecute 可以設置或修改 UI，使用於 doInBackground 之後，doInBackground return 值給 onPostExecute
/*---------- AsyncTask ----------*/

// <傳入參數, 處理中更新介面參數, 處理後傳出參數>
public class HttpAsyncTask extends AsyncTask<String, Void, String>{

    private static final String TAG = "HttpAsyncTask";
    private static final int TIME_OUT = 1000;
    private static String jsonString1;

    //用來連結網址
    String urlString = "";

    //用來做網頁瀏覽或 URL 讀取請求
    HttpURLConnection connection = null;

    //String jsonString1 = "";


    // 在背景中處理的耗時工作
    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG,"doInBackground");
        StringBuilder sb = new StringBuilder();
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
        Log.d(TAG, "JSON:"+s);
        parseJSON(s);
    }

    private void parseJSON(String s){

    }
}
