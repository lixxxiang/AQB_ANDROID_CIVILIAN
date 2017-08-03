package com.cgwx.yyfwptz.lixiang.aqb;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.cgwx.yyfwptz.lixiang.entity.Constants;
import com.cgwx.yyfwptz.lixiang.entity.WrongMessage;
import com.cgwx.yyfwptz.lixiang.entity.addAlarm;
import com.cgwx.yyfwptz.lixiang.entity.sendMessage;
import com.google.gson.Gson;
import com.yinglan.scrolllayout.ScrollLayout;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.cgwx.yyfwptz.lixiang.aqb.R.color.color5F;

public class MainActivity extends AppCompatActivity {

    LocationClient mLocClient;
    private UiSettings mUiSettings;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    MapView mMapView;
    BaiduMap mBaiduMap;
    Button mine;
    Toolbar toolbar;
    public static final String POST_URL_ALARM = Constants.prefix +  "mobile/civilian/addAlarm/";
    public static final String POST_URL_ROLL = Constants.prefix +  "mobile/civilian/isAlarmAccepted/";

    private OkHttpClient alarm;
    private OkHttpClient roll;

    Gson gsonAlarm;
    Gson gsonRoll;

    String userTel;
    String userId;
    TextView iconinfo;
    Button call_110;
    TextView done;
    String alarmID;
    View arrangeview;
    View completeview;
    View callpoliceview;
    View failureview;
    long exitTime =0;
    Boolean isSucceed;

    private Timer timer;
    private TimerTask task;
    int count = 0;


    // UI相关
    RadioGroup.OnCheckedChangeListener radioButtonListener;
    boolean isFirstLoc = true; // 是否首次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        done = (TextView) findViewById(R.id.btnRight);
        iconinfo = (TextView) findViewById(R.id.iconInfo);
        call_110 = (Button) findViewById(R.id.call110);
        call_110.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:10086"));
                startActivity(intent);
            }
        });
        gsonAlarm = new Gson();
        gsonRoll = new Gson();

        final Intent intent = getIntent();

        if (intent != null) {
            userTel = intent.getStringExtra("userTel");
            userId = intent.getStringExtra("userId");
            SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
            userTel = sp.getString("userTel", null);
            userId = sp.getString("userId", null);
            Log.e("tel", userTel);
            Log.e("id",userId);
        }
//            /**
//             * for test
//             */
//        userTel = "13123456789";
//        userId = "000001";
//        Log.e("tel", userTel);
//        Log.e("id",userId);

        initToolbar(R.id.toolbar, R.id.toolbar_title, "安全宝");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MineActivity.class);
                intent.putExtra("userTel", userTel);
                Log.e("MainUserTel",userTel);
                startActivity(intent);

            }
        });
        final ImageView cpimage = (ImageView) findViewById(R.id.cpimage);
        TextView cp = (TextView) findViewById(R.id.cp);
        callpoliceview = findViewById(R.id.cpview);
        arrangeview = findViewById(R.id.arrange);
        completeview = findViewById(R.id.complete);
        failureview = findViewById(R.id.failed);



        /**
         * call police;
         */
        cpimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                timer = new Timer();
                alarm = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBodyPost = new FormBody.Builder()
                        .add("civilianId", userId)
                        .add("civilianTel", userTel)
                        .add("longitude", "" + myListener.longi)
                        .add("latitude", "" + myListener.lati)
                        .add("poi", myListener.poi.get(0).getName())
                        .add("address", myListener.address)
                        .build();
                Request requestPost = new Request.Builder()
                        .url(POST_URL_ALARM)
                        .post(requestBodyPost)
                        .build();
                alarm.newCall(requestPost).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String string = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Log.e("return:", string);
                                WrongMessage wm = gsonAlarm.fromJson(string, WrongMessage.class);
                                addAlarm aA = gsonAlarm.fromJson(string, addAlarm.class);
                                if(wm.getStatus() != null){
                                    if(wm.getStatus().equals("500")){
                                        Toast.makeText(MainActivity.this, "服务器连接失败，请稍后重试", Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    if (aA.getMeta().equals("success")) {
                                        Log.e("state:", "报警成功");
                                        alarmID = aA.getAlarmId();
                                        callpoliceview.setVisibility(View.INVISIBLE);
                                        arrangeview.setVisibility(View.VISIBLE);
                                        iconinfo.setText("正在安排警力");

                                        /**
                                         * 轮询
                                         */

                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                // TODO Auto-generated method stub
                                                Message message = new Message();
                                                message.what = 1;
                                                handler.sendMessage(message);
                                            }
                                        }, 1000, 1000);
                                    }
                                }
                            }
                        });
                    }
                });

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.location);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker));
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setOverlookingGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setAddrType("all");
        option.setIsNeedLocationPoiList(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情
            super.handleMessage(msg);

            roll();
            count++;
            if (count > 300) {
                if(timer != null){
                    timer.purge();
                    timer.cancel();
                }
                timer = new Timer();

                callpoliceview.setVisibility(View.INVISIBLE);
                arrangeview.setVisibility(View.INVISIBLE);
                failureview.setVisibility(View.VISIBLE);
                iconinfo.setText("报警失败");
                done.setVisibility(View.VISIBLE);
                isSucceed = false;
            }
        }
    };


    public void roll() {
        roll = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
//        userId = "1";
//        userTel = "13222223333";
        RequestBody requestBodyPost = new FormBody.Builder()
                .add("alarmId", alarmID)
                .build();
        Request requestPost = new Request.Builder()
                .url(POST_URL_ROLL)
                .post(requestBodyPost)
                .build();
        roll.newCall(requestPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("是否接单return:", string);
                        addAlarm aA = gsonRoll.fromJson(string, addAlarm.class);
//                        aA.setMeta("failure");
                        if (aA.getMeta().equals("success")) {
                            arrangeview.setVisibility(View.INVISIBLE);
                            completeview.setVisibility(View.VISIBLE);
                            iconinfo.setText("报警成功");
                            if(timer != null){
                                timer.purge();
                                timer.cancel();
                            }
                            timer = new Timer();
                            done.setVisibility(View.VISIBLE);
                            isSucceed = true;
                        } else if (aA.getMeta().equals("failure")) {
                            Log.e("state：", "无警察接单");
//                            TranslateAnimation tAnim = new TranslateAnimation(0, 0, 0, 400);
//                            tAnim.setDuration(200);
//                            tAnim.setInterpolator(new AccelerateDecelerateInterpolator());
//                            tAnim.setAnimationListener(new Animation.AnimationListener() {
//                                @Override
//                                public void onAnimationStart(Animation animation) {
//                                }
//
//                                @Override
//                                public void onAnimationRepeat(Animation animation) {
//                                }
//
//                                @Override
//                                public void onAnimationEnd(Animation animation) {
//                                    arview.clearAnimation();
//                                    arview.layout(arview.getLeft(), 400, arview.getRight(), 400 + arview.getHeight());
//                                }
//                            });
//                            arview.startAnimation(tAnim);
//                            faview.setVisibility(View.VISIBLE);
//                            done.setVisibility(View.VISIBLE);

                        } else if (aA.getMeta().equals("no police")){
                            if(timer != null){
                                timer.purge();
                                timer.cancel();
                            }
                            timer = new Timer();
                            callpoliceview.setVisibility(View.INVISIBLE);
                            arrangeview.setVisibility(View.INVISIBLE);
                            failureview.setVisibility(View.VISIBLE);
                            iconinfo.setText("报警失败");
                            done.setVisibility(View.VISIBLE);
                            isSucceed = false;
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        public double lati;
        public double longi;
        public String address;
        List<Poi> poi;

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(0)
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            lati = location.getLatitude();
            longi = location.getLongitude();
            address = location.getAddrStr();
            poi = location.getPoiList();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        toolbar = (Toolbar) findViewById(id);
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        textView.setTextSize(17);
        textView.setTextColor(R.color.color5F);
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setToolbarRight("完成", null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("sdfs", "提交操作");
                iconinfo.setText("我当前的位置");
                if(!isSucceed){
                    failureview.setVisibility(View.INVISIBLE);
                    callpoliceview.setVisibility(View.VISIBLE);
                    done.setVisibility(View.INVISIBLE);

                }else{
                    completeview.setVisibility(View.INVISIBLE);
                    callpoliceview.setVisibility(View.VISIBLE);
                    done.setVisibility(View.INVISIBLE);

                }
            }
        });
        return toolbar;
    }

    protected void setToolbarRight(String text, @Nullable Integer icon, View.OnClickListener btnClick) {
        if (text != null) {
            done.setText(text);
        }
        done.setOnClickListener(btnClick);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {

            if((System.currentTimeMillis()-exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), "再按一次退出程序，您的案情将报警失败。",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else
            {
                if(timer != null){
                    timer.purge();
                    timer.cancel();
                }

                isSucceed = false;
                finish();

                if (LoginActivity.la != null){
                    LoginActivity.la.finish();
                }
                if (VCodeActivity.va != null){
                    VCodeActivity.va.finish();
                }
                if(MineActivity.ma != null){
                    MineActivity.ma.finish();
                }
                System.exit(0);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
