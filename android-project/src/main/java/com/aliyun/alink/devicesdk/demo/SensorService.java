package com.aliyun.alink.devicesdk.demo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class SensorService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //客户端通过调用startService()方法启动服务时执行该方法，可以执行多次
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service","success");
        return super.onStartCommand(intent, flags, startId);
    }
    //第一次创建服务时执行的方法，且只执行一次
    @Override
    public void onCreate() {
        super.onCreate();
    }
    //客户端调用unBindeService()方法断开服务绑定时执行该方法
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    //服务被销毁时执行的方法，且只执行一次
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
