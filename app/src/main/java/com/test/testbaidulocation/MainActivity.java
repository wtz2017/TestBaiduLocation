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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getName();

    private static int num;

    public LocationClient mLocationClient = null;

    public BDAbstractLocationListener myListener = new MyLocationListener();
//BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明

    private Button mButton;
    private ScrollView mScrollView;
    private TextView mContent1;
    private TextView mContent2;
    private TextView mCoordType;

    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> mPermissionList = new ArrayList<>();
    private final static int REQUEST_PERMISSIONS_CODE = 1;

//    private final static String COORTYPE = "bd09ll";
//    private final static String COORTYPE = "bd09";
    private final static String COORTYPE = "gcj02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate" + " Java ThreadId = " + Thread.currentThread().getId()
                + ", Android Tid = " + android.os.Process.myTid());
        setContentView(R.layout.activity_main);

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
        mCoordType = (TextView) this.findViewById(R.id.tv_coord_type);
        mCoordType.setText(COORTYPE);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        initLocation();
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
            // TODO do something
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
                // TODO: 2017/9/29 Do something
                startLocation();
                break;
            default:
                break;
        }
    }

    public String getVersion() {
        return mLocationClient != null ? mLocationClient.getVersion() : null;
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

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType(COORTYPE);
        //可选，默认gcj02，设置返回的定位结果坐标系

        option.setScanSpan(0);
        //可选，默认0，即仅定位一次，若设置span大于0，则发起定位请求的间隔需要大于等于span ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(false);
        //可选，默认false,设置是否使用gps

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.disableCache(true);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "onReceiveLocation" + " Java ThreadId = " + Thread.currentThread().getId()
                    + ", Android Tid = " + android.os.Process.myTid());
            num++;
            StringBuffer buffer = new StringBuffer(num + ":\n");

            //获取定位结果
            buffer.append("Time:");
            buffer.append(location.getTime() + "\n");    //获取定位时间

            buffer.append("LocationID:");
            buffer.append(location.getLocationID() + "\n");    //获取定位唯一ID，v7.2版本新增，用于排查定位问题

            buffer.append("LocType:");
            buffer.append(location.getLocType() + "\n");    //获取定位类型

            buffer.append("Latitude:");
            buffer.append(location.getLatitude() + "\n");    //获取纬度信息

            buffer.append("Longitude:");
            buffer.append(location.getLongitude() + "\n");    //获取经度信息

            buffer.append("Radius:");
            buffer.append(location.getRadius() + "\n");    //获取定位精准度

            buffer.append("Addr:");
            Log.d(TAG, "onReceiveLocation...getAddrStr=" + location.getAddrStr());
            buffer.append(location.getAddrStr() + "\n");    //获取地址信息

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

            buffer.append("Street:");
            buffer.append(location.getStreet() + "\n");    //获取街道信息

            buffer.append("StreetNumber:");
            buffer.append(location.getStreetNumber() + "\n");    //获取街道码

            buffer.append("LocationDescribe:");
            Log.d(TAG, "onReceiveLocation...getLocationDescribe=" + location.getLocationDescribe());
            buffer.append(location.getLocationDescribe() + "\n");    //获取当前位置描述信息

            List<Poi> pois = location.getPoiList(); //获取当前位置周边POI信息
            Log.d(TAG, "onReceiveLocation...pois = " + pois);
            if (pois != null) {
                buffer.append("PoiList:");
                for (Poi p : pois) {
                    buffer.append(p.getName() + ",");
                }
                buffer.append("\n");
            }


            buffer.append("BuildingID:");
            buffer.append(location.getBuildingID() + "\n");    //室内精准定位下，获取楼宇ID

            buffer.append("BuildingName:");
            buffer.append(location.getBuildingName() + "\n");    //室内精准定位下，获取楼宇名称

            buffer.append("Floor:");
            buffer.append(location.getFloor() + "\n");    //室内精准定位下，获取当前位置所处的楼层信息

            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                buffer.append("LocType is TypeGpsLocation:\n");
                //当前为GPS定位结果，可获取以下信息
                buffer.append("Speed:");
                buffer.append(location.getSpeed() + "\n");    //获取当前速度，单位：公里每小时
                buffer.append("SatelliteNumber:");
                buffer.append(location.getSatelliteNumber() + "\n");    //获取当前卫星数
                buffer.append("Altitude:");
                buffer.append(location.getAltitude() + "\n");    //获取海拔高度信息，单位米
                buffer.append("Direction:");
                buffer.append(location.getDirection() + "\n");    //获取方向信息，单位度

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                buffer.append("LocType is TypeNetWorkLocation:\n");
                //当前为网络定位结果，可获取以下信息
                buffer.append("Operators:");
                buffer.append(location.getOperators() + "\n");    //获取运营商信息

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                buffer.append("LocType is TypeOffLineLocation:\n");
                //当前为网络定位结果

            } else if (location.getLocType() == BDLocation.TypeServerError) {
                buffer.append("LocType is TypeServerError:\n");
                //当前网络定位失败
                //可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                buffer.append("LocType is TypeNetWorkException:\n");
                //当前网络不通

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                buffer.append("LocType is TypeCriteriaException:\n");
                //当前缺少定位依据，可能是用户没有授权，建议弹出提示框让用户开启权限
                //可进一步参考onLocDiagnosticMessage中的错误返回码

            }
            mContent1.setText(buffer);

            // TODO: 2017/8/28
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
}
