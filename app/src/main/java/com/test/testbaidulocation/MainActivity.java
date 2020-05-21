package com.test.testbaidulocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getName();

    private static int num;

    // lib 库选择的【全量定位】+【基础地图】
    public LocationClient mLocationClient = null;
    public BDAbstractLocationListener myListener = new MyLocationListener();
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private Button mButton;
    private ScrollView mScrollView;
    private TextView mContent1;
    private TextView mContent2;
    private TextView mSDKVersion;
    private TextView mCoordType;


    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> mPermissionList = new ArrayList<>();
    private final static int REQUEST_PERMISSIONS_CODE = 1;

    // 要与 Application 中的一致
//    private final static String COORTYPE = "bd09ll";
//    private final static String COORTYPE = "bd09";
    private final static String COORTYPE = "gcj02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate" + " Java ThreadId = " + Thread.currentThread().getId()
                + ", Android Tid = " + android.os.Process.myTid());
        setContentView(R.layout.activity_main);

        initMap();
        initLocation();

        mButton = (Button) this.findViewById(R.id.btn1);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                if (Build.VERSION.SDK_INT >= 23) {
                    judgePermission();
                } else {
                    startLocation();
                }
            }
        });
        mButton.requestFocus();
        mScrollView = (ScrollView) this.findViewById(R.id.scrollView);
        mContent1 = (TextView) this.findViewById(R.id.tv1_content);
        mContent2 = (TextView) this.findViewById(R.id.tv2_content);
        mSDKVersion = (TextView) this.findViewById(R.id.tv_sdk_ver);
        mSDKVersion.setText("SDK Version：" + mLocationClient.getVersion());
        mCoordType = (TextView) this.findViewById(R.id.tv_coord_type);
        mCoordType.setText("坐标系类型：" + COORTYPE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void judgePermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (this.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            startLocation();
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            this.requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = this.shouldShowRequestPermissionRationale(permissions[i]);
                        if (showRequestPermission) {//
//                            judgePermission();//重新申请权限
//                            return;
                        } else {
                            //已经禁止
                        }
                    }
                }
                startLocation();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopLocation();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        super.onDestroy();
    }

    private void initMap() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //普通地图 ,mBaiduMap是地图控制器对象
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
    }

    // sdk 问题参考：http://lbsyun.baidu.com/index.php?title=android-locsdk/qa1
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度；GPS定位精度在10m左右，百度的Wi-Fi定位服务精度目前在20m左右，百度的基站定位服务精度目前在200m左右；
        //高精度模式定位：同时使用网络定位和GPS定位
        //低功耗模式定位：只会使用网络定位（Wi-Fi定位和基站定位）
        //设备模式定位：不需要连接网络，只使用GPS进行定位

        option.setCoorType(COORTYPE);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll
        //（1）WGS84：表示GPS获取的坐标；
        //（2）GCJ02：是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系；
        //（3）BD09：为百度坐标系，在GCJ02坐标系基础上再次加密。其中BD09II表示百度经纬度坐标，BD09MC表示百度墨卡托米制坐标。

        option.setScanSpan(0);
        //可选，默认0，即仅定位一次，若设置span大于0，则发起定位请求的间隔需要大于等于span ms才是有效的
        //当setScanSpan < 1000时，为 APP主动请求定位；
        // 当setScanSpan>=1000时，为定时定位模式（setScanSpan的值就是定时定位的时间间隔）

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认 false，设置是否使用 GPS，GPS 定位在室内是不可以使用的，一般情况下，GPS 定位精度在 10m 左右

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIsNeedAltitude(true);
        //可选，默认false，设置是否需要海拔信息

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.disableCache(true);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "onReceiveLocation" + " Java ThreadId = " + Thread.currentThread().getId()
                    + ", Android Tid = " + android.os.Process.myTid());
            num++;
            StringBuffer buffer = new StringBuffer("第");
            buffer.append(num);
            buffer.append("次:\n");

            //获取定位结果
            buffer.append("Time:");
            buffer.append(location.getTime() + "\n");    //获取定位时间

            buffer.append("Longitude(经度):");
            buffer.append(location.getLongitude() + "\n");    //获取经度信息

            buffer.append("Latitude(纬度):");
            buffer.append(location.getLatitude() + "\n");    //获取纬度信息

            buffer.append("Radius(半径):");
            buffer.append(location.getRadius() + "\n");    //获取定位精准度

            buffer.append("Addr:");
            Log.d(TAG, "onReceiveLocation...getAddrStr=" + location.getAddrStr());
            buffer.append(location.getAddrStr() + "\n");    //获取地址信息

            buffer.append("LocationDescribe:");
            Log.d(TAG, "onReceiveLocation...getLocationDescribe=" + location.getLocationDescribe());
            buffer.append(location.getLocationDescribe() + "\n");    //获取当前位置描述信息

            buffer.append("Country:");
            buffer.append(location.getCountry() + "\n");    //获取国家信息

            buffer.append("CountryCode:");
            buffer.append(location.getCountryCode() + "\n");    //获取国家码

            buffer.append("Province:");
            buffer.append(location.getProvince() + "\n");    //获取省份信息

            buffer.append("City:");
            buffer.append(location.getCity() + "\n");    //获取城市信息

            buffer.append("CityCode:");
            buffer.append(location.getCityCode() + "\n");    //获取城市码

            buffer.append("District:");
            buffer.append(location.getDistrict() + "\n");    //获取区县信息

            buffer.append("Town:");
            buffer.append(location.getTown() + "\n");    //获取乡镇

            buffer.append("Street:");
            buffer.append(location.getStreet() + "\n");    //获取街道信息
            buffer.append("StreetNumber:");
            buffer.append(location.getStreetNumber() + "\n");    //获取街道码

            buffer.append("BuildingName:");
            buffer.append(location.getBuildingName() + "\n");//室内精准定位下，获取楼宇名称
            buffer.append("BuildingID:");
            buffer.append(location.getBuildingID() + "\n");//室内精准定位下，获取楼宇ID

            buffer.append("Floor:");
            buffer.append(location.getFloor() + "\n");//室内精准定位下，获取当前位置所处的楼层信息

            buffer.append("IndoorLocationSurpportBuidlingName:");
            buffer.append(location.getIndoorLocationSurpportBuidlingName() + "\n");    //TODO TEST

            buffer.append("IndoorSurpportPolygon:");
            buffer.append(location.getIndoorSurpportPolygon() + "\n");    //TODO TEST

            List<Poi> pois = location.getPoiList(); //获取当前位置周边POI信息
            Log.d(TAG, "onReceiveLocation...pois = " + pois);
            if (pois != null) {
                buffer.append("PoiList:");
                for (Poi p : pois) {
                    buffer.append(p.getName() + ",");
                }
                buffer.append("\n");
            }

            buffer.append("LocType:");
            buffer.append(location.getLocType() + "\n");    //获取定位类型

            buffer.append("LocTypeDescription:");
            buffer.append(location.getLocTypeDescription() + "\n");//获取定位类型描述

            int locType = location.getLocType();
            switch (locType) {
                case BDLocation.TypeGpsLocation:
                    //当前为GPS定位结果，可获取以下信息
                    buffer.append("LocType is TypeGpsLocation[\n");
                    buffer.append("Speed:");
                    buffer.append(location.getSpeed() + "\n");    //获取当前速度，单位：公里每小时
                    buffer.append("SatelliteNumber:");
                    buffer.append(location.getSatelliteNumber() + "\n");    //获取当前卫星数
                    buffer.append("Altitude:");
                    buffer.append(location.getAltitude() + "\n");    //获取海拔高度信息，单位米
                    buffer.append("Direction:");
                    buffer.append(location.getDirection() + "\n");    //获取方向信息，单位度
                    buffer.append("]\n");
                    break;
                case BDLocation.TypeNetWorkLocation:
                    //当前为网络定位结果，可获取以下信息
                    buffer.append("LocType is TypeNetWorkLocation[\n");
                    buffer.append("Operators:");
                    buffer.append(location.getOperators() + "\n");    //获取运营商信息
                    if (location.hasAltitude()) {// 如果有海拔高度
                        buffer.append("Altitude(海拔): ");
                        buffer.append(location.getAltitude() + "\n");// 单位：米
                    }
                    buffer.append("]\n");
                    break;
                case BDLocation.TypeOffLineLocation:
                    buffer.append("LocType is TypeOffLineLocation\n");
                    break;
                case BDLocation.TypeCacheLocation:
                    buffer.append("LocType is TypeCacheLocation\n");
                    break;
                case BDLocation.TypeServerError:
                    //可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com
                    buffer.append("LocType is TypeServerError\n");
                    break;
                case BDLocation.TypeNetWorkException:
                    //当前网络不通
                    buffer.append("LocType is TypeNetWorkException\n");
                    break;
                case BDLocation.TypeCriteriaException:
                    //当前缺少定位依据，可能是用户没有授权，建议弹出提示框让用户开启权限
                    //可进一步参考onLocDiagnosticMessage中的错误返回码
                    buffer.append("LocType is TypeCriteriaException\n");
                    break;
            }
            mContent1.setText(buffer);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            stopLocation();
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         * 自动回调，相同的diagnosticType只会回调一次
         *
         * @param locType           当前定位类型
         * @param diagnosticType    诊断类型（1~9）
         * @param diagnosticMessage 具体的诊断信息释义
         */
        public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
            Log.d(TAG, "onLocDiagnosticMessage...locType = " + locType
                    + ", diagnosticType = " + diagnosticType
                    + ", diagnosticMessage = " + diagnosticMessage);
            if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_GPS) {
                //建议打开GPS
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_WIFI) {
                //建议打开wifi，不必连接，这样有助于提高网络定位精度！
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_LOC_PERMISSION) {
                //定位权限受限，建议提示用户授予APP定位权限！
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_NET) {
                //网络异常造成定位失败，建议用户确认网络状态是否异常！
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CLOSE_FLYMODE) {
                //手机飞行模式造成定位失败，建议用户关闭飞行模式后再重试定位！
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_INSERT_SIMCARD_OR_OPEN_WIFI) {
                //无法获取任何定位依据，建议用户打开wifi或者插入sim卡重试！
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_OPEN_PHONE_LOC_SWITCH) {
                //无法获取有效定位依据，建议用户打开手机设置里的定位开关后重试！
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_SERVER_FAIL) {
                //百度定位服务端定位失败
                //建议反馈location.getLocationID()和大体定位时间到loc-bugs@baidu.com
            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_FAIL_UNKNOWN) {
                //无法获取有效定位依据，但无法确定具体原因
                //建议检查是否有安全软件屏蔽相关定位权限
                //或调用LocationClient.restart()重新启动后重试！
            }
        }

    }

    public void startLocation() {
        if (mLocationClient != null && !mLocationClient.isStarted()) {
            Log.d(TAG, "mLocationClient.start()");
            mLocationClient.start();
        }
    }

    public void stopLocation() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            Log.d(TAG, "mLocationClient.stop()");
            mLocationClient.stop();
        }
    }

}
