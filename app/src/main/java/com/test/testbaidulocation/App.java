package com.test.testbaidulocation;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
//        SDKInitializer.setCoordType(CoordType.BD09LL);// 要与 Activity 中的 COORTYPE 一致
        SDKInitializer.setCoordType(CoordType.GCJ02);// 要与 Activity 中的 COORTYPE 一致
    }

}
