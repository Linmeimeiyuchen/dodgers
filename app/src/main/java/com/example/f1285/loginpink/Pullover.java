package com.example.f1285.loginpink;

//import android.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuView;
import android.text.LoginFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by f1285 on 2017/5/20.
 */

public class Pullover extends Fragment implements OnMapReadyCallback {

    View rootView;

    Session session = new Session();

    private static final String TAG = "Pullover";
    private final double EARTH_RADIUS = 6378137.0;

    private GoogleMap map;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    //是否開啟定位服務
    private boolean getService = false;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    //宣告定位管理控制
    private LocationManager mLocationManager;
    private String locationType = "GPS";
    private Location rootLocation = null;  //當下位置
    private Location gpsLocation = null;
    private Location networkLocation = null;
    private Boolean flagGPSEnable = false;
    private Boolean flagNetworkEnable = false;
    private boolean flagGetGPSDone = false;
    private boolean flagNetworkDone = false;

    private Handler handler = new Handler();

    private TextView text1;     //經度欄位
    private TextView text2;     //緯度欄位
    private Button buttonUpdate;    //臨檢 Data 更新 Button

    //座標變更次數，三十次一個循環
    int locationChangeCount = 0;

    //存放臨檢資訊
    private Data data = new Data();
    /*------ 暫時臨檢資訊 ------*/
    private double[] longit = new double[]{121.497204, 121.4971503, 121.5193533, 121.4811662, 121.466527, 121.491980, 121.420083};
    private double[] latit = new double[]{25.061805, 25.0660304, 25.0627886, 25.0750985, 25.088903, 25.083595, 25.066823};
    private String[] addr = new String[]{"新北市三重區重新路二段78號", "新北市三重區三和路二段91號",
            "民權西路捷運站", "新北市三重區三和路四段216號", "新北市蘆洲區中正路216號",
            "新北市三重區五華街110巷1之4號", "新北市泰山區泰林路三段22號"};
    private int[] type = new int[]{1, 1, 1, 1, 1, 1, 1};

    //String stringUrl = "http://beta.json-generator.com/api/json/get/Ny2Zu3hb7";
    //String stringUrl = "http://114.44.122.179:8080/dodgers/public/index.php/allInfo";
    String stringUrl = "http://220.134.198.4:8080/dodgers/public/index.php/getInfo";

    //鄰近臨檢地 layout
    private LinearLayout lo;

    //臨檢清單layout
    private LinearLayout layout_nearPullover;
    private View view_nearPullover;
    private ScrollView scrollview_nearPullover;

    private int range = 3;      //臨檢站匯入範圍(Km)
    private int distance = 300;     //臨檢站提醒距離(m)
    private boolean remind_mode_shock = true;     //提醒方式(震動)
    private boolean remind_mode_sound = true;      //提醒方式(聲音)
    private boolean hide_nearPullover = false;      //隱藏臨檢清單

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pullover, container, false);

        //range_view = (MenuItem) rootView.findViewById(R.id.range_3);
        //range_view.setChecked(true);

        //取得設定檔
        if (getRememberSetting(rootView.getContext(), "setting", "range", 0) != 0) {
            range = getRememberSetting(rootView.getContext(), "setting", "range", 0);
            Log.d(TAG, "range = " + range);
        }
        if (getRememberSetting(rootView.getContext(), "setting", "remindDistance", 0) != 0) {
            distance = getRememberSetting(rootView.getContext(), "setting", "remindDistance", 0);
            Log.d(TAG, "distance = " + distance);
        }
        if (getRememberSetting(rootView.getContext(), "setting", "remind_shock", 2) != 2) {
            if (getRememberSetting(rootView.getContext(), "setting", "remind_shock", 2) == 1)
                remind_mode_shock = true;
            else remind_mode_shock = false;
        }
        if (getRememberSetting(rootView.getContext(), "setting", "remind_sound", 2) != 2) {
            if (getRememberSetting(rootView.getContext(), "setting", "remind_sound", 2) == 1)
                remind_mode_sound = true;
            else remind_mode_sound = false;
        }
        if (!session.getSession(rootView.getContext(), "login", "token", "null").equals("null")){
            Log.d(TAG, "token = "+session.getSession(rootView.getContext(), "login", "token", "null"));
        }else {
            Log.d(TAG, "no token");
        }

        //經度
        text1 = (TextView) rootView.findViewById(R.id.textView3);
        //緯度
        text2 = (TextView) rootView.findViewById(R.id.textView5);

        /*---- Location code ----*/
        //取得定位權限
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(rootView.getContext(), "定位已開啟", Toast.LENGTH_LONG).show();
            locationServiceInitial();
            //location = locationServiceInitial();
            //final double longitude = location.getLongitude();
            //text1.setText(""+location.getLongitude());
        } else {
            Toast.makeText(rootView.getContext(), "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //開啟設定頁面
        }

        /*---- GoogleMap code ----*/
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) rootView.findViewById(R.id.mapView_now);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        /*---- GoogleMap code end ----*/

        lo = (LinearLayout) rootView.findViewById(R.id.layout_location);

        //臨檢清單layout
        layout_nearPullover = (LinearLayout) rootView.findViewById(R.id.layout_nearPullover);
        view_nearPullover = (View) rootView.findViewById(R.id.view_nearPullover);
        scrollview_nearPullover = (ScrollView) rootView.findViewById(R.id.scrollview_nearPullover);

        //臨檢 Data 更新 Button
        buttonUpdate = (Button) rootView.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (checkNetwork()) {
                    if (rootLocation != null) {

                        //顯示載入中 Dialog
                        final ProgressDialog progressDialog = new ProgressDialog(rootView.getContext(), R.style.Theme_AppCompat_DayNight_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("更新中...");
                        progressDialog.show();
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {

                                        progressDialog.dismiss();
                                    }
                                }, 1500);

                        Log.d(TAG, "range=" + range);
                        Log.d(TAG, "distance=" + distance);
                        UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();
                        Log.d(TAG, stringUrl + "/" + rootLocation.getLatitude() + "/" + rootLocation.getLongitude() + "/" + range);
                        updateAsyncTask.execute(stringUrl + "/" + rootLocation.getLatitude() + "/" + rootLocation.getLongitude() + "/" + range);
                        drawMarker(rootLocation);
                        calculation();
                        //updateAsyncTask.execute("http://hmkcode.appspot.com/rest/controller/get.json");
                        //updateAsyncTask.execute("http://220.134.198.4:8080/dodgers/public/index.php/allInfo");
                    } else {
                        Toast.makeText(rootView.getContext(), "未定位完成!!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(rootView.getContext(), "網路未連接!!請檢查!!", Toast.LENGTH_LONG).show();
                }

            }
        });

        if (getRememberSetting(rootView.getContext(), "setting", "hidNearPullover", 2) != 2) {
            if (getRememberSetting(rootView.getContext(), "setting", "hidNearPullover", 2) == 1) {
                hide_nearPullover = true;
                setHidNearPullover(true);
            } else {
                hide_nearPullover = false;
                setHidNearPullover(false);
            }
        }
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.range_1).setChecked(true);
    }

    private Runnable locationServiceTimer = new Runnable() {

        int TIMEROUT_SEC = 35;
        int count = 0;

        @Override
        public void run() {
            count++;
            Log.d(TAG, "count = " + count);
            if (count % 5 == 0)
                Toast.makeText(rootView.getContext(), "GPS定位中...", Toast.LENGTH_LONG).show();
            if (count > TIMEROUT_SEC) {
                Toast.makeText(rootView.getContext(), "GPS定位時間過長", Toast.LENGTH_LONG).show();

                if (flagNetworkDone == true) {
                    new AlertDialog.Builder(rootView.getContext())
                            .setMessage("GPS定位時間過長\n網路定位完成，是否改用網路定位?")
                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    locationType = "Network";
                                    locationChange(networkLocation);
                                }
                            })
                            .setNegativeButton("繼續等待GPS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNeutralButton("使用上次座標", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    Location Gpslocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    Location Networklocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                    if(Gpslocation != null)
                                        locationChange(Gpslocation);
                                    else{
                                        if(Networklocation != null)
                                            locationChange(Networklocation);
                                    }
                                }
                            }).show();
                }else {
                    new AlertDialog.Builder(rootView.getContext())
                            .setMessage("GPS定位時間過長\n是否繼續等待GPS定位?")
                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNegativeButton("使用上次座標", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    // 取得上次定位座標
                                    Location Gpslocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    Location Networklocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                    if(Gpslocation != null)
                                        locationChange(Gpslocation);
                                    else{
                                        if(Networklocation != null)
                                            locationChange(Networklocation);
                                    }
                                }
                            }).show();
                }
                count = 0;
            }
            if(rootLocation == null) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    //取得 Location 位置
    private void locationServiceInitial() {

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //mLocationManager.requestLocationUpdates(bestProvider, 0, 0, (LocationListener) Pullover.this);

        flagGPSEnable = mLocationManager.isProviderEnabled("gps");
        Log.d(TAG, "flagGPSEnable = " + flagGPSEnable);
        flagNetworkEnable = mLocationManager.isProviderEnabled("network");
        Log.d(TAG, "flagNetworkEnable = " + flagNetworkEnable);

        startAllLocationListener();
        handler.postDelayed(locationServiceTimer, 1000);

        /*
        // 由Criteria物件判斷提供最準確的資訊
        Criteria criteria = new Criteria(); //資訊提供者選取標準
        bestProvider = mLocationManager.getBestProvider(criteria, true);    //選擇精準度最高的提供者

        if (ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(rootView.getContext(), "失敗", Toast.LENGTH_LONG).show();
            Toast.makeText(rootView.getContext(), "請開啟定位服務", Toast.LENGTH_LONG).show();
            return;
            //return null;
        }
        */
        //Location location = mLocationManager.getLastKnownLocation(bestProvider);
        //if (location != null) {
        //    Toast.makeText(rootView.getContext(), "定位成功", Toast.LENGTH_LONG).show();
        //}
        //getLocation(location);
        //return location;
    }

    public void startAllLocationListener() {
        if (ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(rootView.getContext(), "失敗", Toast.LENGTH_LONG).show();
            Toast.makeText(rootView.getContext(), "請開啟定位服務", Toast.LENGTH_LONG).show();
            return;
            //return null;
        }
        mLocationManager.requestLocationUpdates("gps", 0, 0, locationListenerGPS);
        mLocationManager.requestLocationUpdates("network", 0, 0, locationListenerNetwork);
    }

    public final LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "GPS change");
            //Log.d(TAG, "locationType = "+locationType);
            flagGetGPSDone = true;
            gpsLocation = location;

            if(locationType.equals("Network")){
                new AlertDialog.Builder(rootView.getContext())
                        .setTitle("GPS 定位已完成是否改為 GPS ?")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                locationType = "GPS";
                                locationChange(gpsLocation);
                            }
                        })
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }

            if( locationType.equals("GPS") )
                locationChange(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public final LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "Network change");
            //Log.d(TAG, "locationType = "+locationType);
            flagNetworkDone = true;
            networkLocation = location;
            if( locationType.equals("Network") )
                locationChange(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void locationChange(Location location){
        getLocation(location);
        LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude() );
        moveMap( latLng );
        rootLocation = location;
        drawMarker(location);
        //Log.d(TAG, "locationChangeCount = "+locationChangeCount);
        if( locationChangeCount == 0 ){
            //更新臨檢地
            //testLoc();
            stringUrl = stringUrl;
            UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();
            Log.d(TAG, stringUrl + "/" + rootLocation.getLatitude() + "/" + rootLocation.getLongitude() + "/" + range);
            updateAsyncTask.execute(stringUrl + "/" + rootLocation.getLatitude() + "/" + rootLocation.getLongitude() + "/" + range);
        }
        locationChangeCount++;
        if( locationChangeCount == 100 ){
            locationChangeCount = 0;
        }

        //計算距離
        calculation();
    }

    private void getLocation(Location location) {

        if (location != null) {
            setTextView(text1, "" + location.getLongitude());
            setTextView(text2, "" + location.getLatitude());
        } else {
            //Toast.makeText(rootView.getContext(), "無法定位", Toast.LENGTH_LONG).show();
        }
    }

    private void setTextView(TextView textView, String str) {
        textView.setText(str);
    }


    /*---------- Google Map ----------*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Pullover.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mLocationManager.requestLocationUpdates(bestProvider, 1000, 1, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        //map.addMarker(new MarkerOptions().position(new LatLng(25.033408, 121.564099)).title("Marker"));

        if (ActivityCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else{
            if(map!=null) map.setMyLocationEnabled(true);
        }

        // Map 經緯度初始值
        LatLng place = new LatLng(25.033408, 121.564099);
        //Toast.makeText(rootView.getContext(),""+location.getLatitude(),Toast.LENGTH_LONG).show();
        //LatLng place = new LatLng( location.getLatitude(), location.getLongitude() );

        moveMap(place);
    }

    public void moveMap(LatLng place){

        // Map視野設定
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(place)
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
        //mLocationManager.removeUpdates(this);
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /* 在Google Map上放上目前位置的地標圖示。 */
    private void drawMarker(Location location) {
        //GoogleMap.addMarker(markerOption)

        if(map != null && location != null){
            map.clear();
            drawAllPullover();

            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            int radius = 20;
            int strokeWidth = 10;
            int zoom = 16;

            switch (distance){
                case 100:
                    radius = 10;
                    strokeWidth = 5;
                    zoom = 17;
                    break;
                case 300:
                    radius = 20;
                    strokeWidth = 10;
                    zoom = 16;
                    break;
                case 500:
                    radius = 25;
                    strokeWidth = 15;
                    zoom = 15;
                    break;
                case 1000:
                    radius = 35;
                    strokeWidth = 15;
                    zoom = 14;
                    break;
            }

            /*
            map.addMarker(new MarkerOptions()
                .position(gps)
                .title("現在位置")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));    //現在位置標記為藍色
            */
            /*map.addCircle(new CircleOptions()
                    .center(gps)
                    .radius(radius)
                    .strokeWidth(strokeWidth)
                    .strokeColor(Color.argb(255, 255, 255, 255))
                    .fillColor(Color.argb(255, 0, 0, 255)));*/

            map.addCircle(new CircleOptions()
                    .center(gps)
                    //半徑(m)
                    .radius(distance)
                    //外圍邊框(像素)
                    .strokeWidth(5)
                    //外圍邊框顏色
                    .strokeColor(Color.BLUE)
                    //設定範圍顏色 Color.argb(透明度, Red, Green, Blue)
                    .fillColor(Color.argb(100, 0, 0, 255)));
            //.icon(BitmapDescriptorFactory.defaultMarker( float ))) 自訂標定顏色
            //.icon(BitmapDescriptorFactory.fromResource( R.drawable. )) 自訂標定圖示
            //.draggable(true)  長按標記可以拖移
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, zoom));
        }else Toast.makeText(rootView.getContext(), "未定位完成", Toast.LENGTH_LONG).show();
    }

    /* 在Google Map上放上臨檢的地標圖示。 */
    private void drawMarker2(LatLng gps) {
        //GoogleMap.addMarker(markerOption)
        //Log.d(TAG, "drawMarker2");
        if(map != null){
            //map.clear();
            //LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(gps)
                    .title("臨檢")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));    //臨檢站標記為紅色
            //.icon(BitmapDescriptorFactory.defaultMarker( float ))) 自訂標定顏色
            //.icon(BitmapDescriptorFactory.fromResource( R.drawable. )) 自訂標定圖示
            //.draggable(true)  長按標記可以拖移
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
        }
    }
    /*---------- Google Map End ----------*/

    /*---------- 在 map 繪出所有臨檢站 ----------*/
    private void drawAllPullover(){

        for(int index=0; index < data.getSize(); index++){
            LatLng gps = new LatLng(data.getLatituble(index), data.getLongituble(index));
            drawMarker2(gps);
        }
    }



    /*---------- 和 server 取得臨檢資訊 ----------*/
    public void setdata( ){
        Log.d(TAG, "setdata");
        Log.d(TAG, Integer.toString(latit.length) );

        for(int index = 0; index < longit.length; index++){
            data.setLongituble( longit[index] );
            data.setLatitude( latit[index] );
            data.setAddress( addr[index] );
            data.setType( type[index] );
        }

    }

    /*---------- get 臨檢 data ----------*/
    public double getLongituble( int index ){
        return data.getLongituble(index);
    }

    public double getLatituble( int index ){
        return data.getLatituble(index);
    }

    public String getAddress( int index ){
        return data.getAddress(index);
    }

    public int getType( int index ){
        return data.getType(index);
    }

    public int getSize(){
        return data.getSize();
    }

    public void dataClear(){
        data.clear();
    }

    public void calculation(){

        if( rootLocation != null ) {
            for (int index = 0; index < data.getSize(); index++) {
                //double distence = Math.abs(rootLocation.getLongitude() - data.getLongituble(index)) +
                //        Math.abs(rootLocation.getLatitude() - data.getLatituble(index));
                double distence = Math.sqrt( Math.pow(rootLocation.getLongitude() - data.getLongituble(index),2) +
                        Math.pow(rootLocation.getLatitude() - data.getLatituble(index),2) );
                //Log.d(TAG, "distence="+ Double.toString(distence));

                //if (distence < (0.00000900900901*this.range*1000)) {

                    //LatLng gps = new LatLng(data.getLatituble(index), data.getLongituble(index));
                    //Log.d(TAG, "draw");
                    //drawMarker2(gps);

                    // 預設 200m 警報
                    if (distence < (0.00000900900901*this.distance)) {
                        remind();
                    }//if(distence < 0.007) end

                //}
            }
        }else {
            Toast.makeText(rootView.getContext(), "未定位完成!", Toast.LENGTH_LONG).show();
        }
    }

    private void remind(){
        showWaringDialog(rootView.getContext());

        if( remind_mode_shock == true ){
            //取得震動服務
            Vibrator vibrator = (Vibrator) getActivity().getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            //震動 0.1 秒
            vibrator.vibrate(100);
        }
        if( remind_mode_sound == true ){
            //取得音量控制器
            AudioManager audioManager = (AudioManager) getActivity().getApplication().getSystemService(Context.AUDIO_SERVICE);
            //Ringtone defaultRingtone = RingtoneManager.getRingtone(getActivity(), Settings.System.DEFAULT_RINGTONE_URI);
            //RingtoneManager.TYPE_NOTIFICATION;   通知声音
            //RingtoneManager.TYPE_ALARM;  警告
            //RingtoneManager.TYPE_RINGTONE; 铃声
            if( audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL ){

                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(getActivity().getApplicationContext(),
                        RingtoneManager.TYPE_NOTIFICATION);
                final Ringtone currentRingtone = RingtoneManager.getRingtone(getActivity(), uri);
                currentRingtone.play();

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        currentRingtone.stop();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 200);

            }
        }
    }

    //測試臨檢地
    public void testLoc(){
        Log.d(TAG,"TestLocation!!!");
        lo = (LinearLayout) rootView.findViewById(R.id.layout_location);
        dataClear();
        setdata();
        Log.d(TAG, Integer.toString(getSize()));

        lo.removeAllViews();

        for(int index = 0; index < data.getSize(); index++ ){
            TextView temp = new TextView( rootView.getContext() );
            temp.setPadding( 0, 5, 0, 0 );
            temp.setText( data.getAddress(index) );
            TextView temp2 = new TextView( rootView.getContext() );
            temp2.setPadding( 30, 0, 0, 0 );
            temp2.setText( ""+data.getLongituble(index)+", "+data.getLatituble(index) );
            lo.addView(temp);
            lo.addView(temp2);
            //LatLng gps = new LatLng(data.getLatituble(index),data.getLongituble(index));
            //drawMarker2(gps);
        }
    }

    public void showWaringDialog( Context context ) {

        final Dialog warningDialog = new Dialog( context, R.style.warningDialog );
        warningDialog.setContentView(R.layout.warning_dialog);
        Window dialogWindow = warningDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        warningDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                warningDialog.dismiss();
            }
        }, 1000);

    }

    /*---------- 連結臨檢站 Data DB ----------*/
    private class UpdateAsyncTask extends AsyncTask<String, Object, String>{

        HttpURLConnection connection = null;
        String jsonString1 = "";

        @Override
        protected void onPreExecute(){
            Log.d(TAG, "In UpdateAsyncTask mrthod!!");
            super.onPreExecute();
            buttonUpdate.setEnabled(false);
            /*
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            buttonUpdate.setEnabled(true);*/
        }

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

                int responseCode = connection.getResponseCode();
                if( responseCode != 200 ){
                    Log.d(TAG, "Out!!");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return "網路中斷";
            }
            //Log.d(TAG, jsonString1);
            return jsonString1;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute");
            Log.d(TAG, s);

            if( s != null) {
                if( s != "網路中斷"){

                    dataClear();
                    lo.removeAllViews();

                    try {
                        JSONArray jsonArray = new JSONArray(s);
                        //Log.d(TAG, s);
                        //Log.d(TAG,""+jsonArray.length());

                        // 解析 JSON
                        for (int index = 0; index < jsonArray.length(); index++) {
                            //Log.d(TAG, ""+index);

                            // 將資料存進 data 資料型態裡
                            data.setLongituble(Double.parseDouble(jsonArray.getJSONObject(index).getString("longitude")));
                            data.setLatitude(Double.parseDouble(jsonArray.getJSONObject(index).getString("latitude")));
                            data.setAddress(jsonArray.getJSONObject(index).getString("address"));

                            // 顯示在 pullover fragment 裡的 textView
                            TextView temp = new TextView(rootView.getContext());
                            temp.setPadding(0, 5, 0, 0);
                            temp.setText(data.getAddress(index));
                            //TextView temp2 = new TextView(rootView.getContext());
                            //temp2.setPadding(30, 0, 0, 0);
                            //temp2.setText("" + data.getLongituble(index) + ", " + data.getLatituble(index));
                            lo.addView(temp);
                        }
                        drawAllPullover();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(rootView.getContext(), "伺服器忙碌中...", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(rootView.getContext(), "伺服器忙碌中...", Toast.LENGTH_LONG).show();
            }

            buttonUpdate.setEnabled(true);

        }
    }
    /*---------- 連結臨檢站 Data DB end ----------*/

    // set 臨檢站匯入範圍
    public void setRange(int range){
        this.range = range;

        session.setSession(rootView.getContext(), "setting", "range", range);
        //drawMarker(rootLocation);
    }

    // set 臨檢站提醒距離
    public void setRemindDistance(int distance){
        this.distance = distance;
        drawMarker(rootLocation);

        session.setSession(rootView.getContext(), "setting", "remindDistance", distance);
    }

    // set 提醒方式
    public void setRemindMode(String str){

        String set = null;
        int b = 2;

        switch (str){
            case "shock on":
                remind_mode_shock = true;
                Log.d(TAG,"shock on");
                set = "remind_shock";
                b = 1;
                break;
            case  "shock off":
                remind_mode_shock = false;
                Log.d(TAG,"shock off");
                set = "remind_shock";
                b = 0;
                break;
            case "sound on":
                remind_mode_sound = true;
                Log.d(TAG,"sound on");
                set = "remind_sound";
                b = 1;
                break;
            case "sound off":
                remind_mode_sound = false;
                Log.d(TAG,"sound off");
                set = "remind_sound";
                b = 0;
                break;
        }

        session.setSession(rootView.getContext(), "setting", set, b);
    }

    // set 是否隱藏清單
    public void setHidNearPullover(Boolean hide){
        hide_nearPullover = hide;
        int b = 2;

        if( hide_nearPullover == true )
            b = 1;
        else b = 0;

        hidNearPullover(hide);

        session.setSession(rootView.getContext(), "setting", "hidNearPullover", b);
    }

    private void hidNearPullover(Boolean hide){

        if( hide == true ){
            layout_nearPullover.setVisibility(View.GONE);
            view_nearPullover.setVisibility(View.GONE);
            scrollview_nearPullover.setVisibility(View.GONE);
            mMapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.mMapView_layout_height400)));
        }else {
            layout_nearPullover.setVisibility(View.VISIBLE);
            view_nearPullover.setVisibility(View.VISIBLE);
            scrollview_nearPullover.setVisibility(View.VISIBLE);
            mMapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.mMapView_layout_height200)));
        }
    }

    // 檢查網路是否開啟
    private boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void rememberSetting(Context context, String name, String key, int value){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putInt(key, value);
        PE.commit();
        Log.d(TAG, "範圍/距離設定檔存入" );
    }

    private int getRememberSetting(Context context, String name, String key, int def){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getInt(key, def);
    }
}
