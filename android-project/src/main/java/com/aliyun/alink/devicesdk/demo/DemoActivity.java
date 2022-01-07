package com.aliyun.alink.devicesdk.demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.alink.devicesdk.app.AppLog;
import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.devicesdk.app.DeviceInfoData;
import com.aliyun.alink.devicesdk.app.SensorMainActivity;
import com.aliyun.alink.devicesdk.manager.DASHelper;
import com.aliyun.alink.devicesdk.manager.IDemoCallback;
import com.aliyun.alink.devicesdk.manager.InitManager;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.log.IDGenerater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;


/*
 * Copyright (c) 2014-2016 Alibaba Group. All rights reserved.
 * License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

public class DemoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "DemoActivity";

    private TextView errorTV = null;
    private AtomicInteger testDeviceIndex = new AtomicInteger(0);
    String str;
    private TextView PText,LText,HText,TText;
    SensorManager sm;
    int id = 0;
    double countL=0,countP=0,countH=0,countT=0;
    private Button startService;
    Sensor sensorL,sensorP,sensorT,sensorH;
    MyLightSensorListener mls;
    MyPressureSensorListener mps;
    MyHumSensorListener mhs;
    MyTempSensorListener mts;
    MyServiceConnection connection;
    private boolean isSub = false;
    private Map<String, Double> reportData = new HashMap<>();
    private static Map<String, Double> responseData = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        errorTV = findViewById(R.id.id_error_info);
        PText = findViewById(R.id.PText);
        LText = findViewById(R.id.LightText);
        HText = findViewById(R.id.HText);
        TText = findViewById(R.id.TText);
        startService = findViewById(R.id.startService);
        setListener();
        //if(isSub)SubMessage();
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorL = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorP = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorH = sm.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorT = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        //Log.i("Sensor",sensor.getName());
        mts = new MyTempSensorListener();
        mls = new MyLightSensorListener();
        mps = new MyPressureSensorListener();
        mhs = new MyHumSensorListener();
//        startService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("ServiceStart","success");
//                new MyServiceConnection();
//            }
//        });
        if(!isSub){
            SubMessage();
            LinkKit.getInstance().registerOnPushListener(notifyListener);
        }
        isSub = true;
    }

    /**
     * Data数据上报
     */
    //阿里云数据上报发布函数
    void SendData()
    {
        MqttPublishRequest request = new MqttPublishRequest();
        // 设置是否需要应答。
        request.isRPC = false;
        // 设置topic，设备通过该Topic向物联网平台发送消息。
        request.topic = "/sys/gr6q69gAqhD/oppoR17/thing/event/property/post";
        // 设置 qos
        request.qos = 0;
        // data 设置需要发布的数据 json String，其中id字段需要保持自增。
        //示例 属性上报 {"id":"160865432","method":"thing.event.property.post","params":{"LightSwitch":1},"version":"1.0"}
        String data = "{\"id\":\""+id+"\",\"method\":\"thing.event.property.post\",\"params\":{";
        for (String key:reportData.keySet())
        {
            data += "\""+key+"\":"+reportData.get(key)+",";
        }
        data += "\"ss\":\"\"},\"version\":\"1.0\"}";
        request.payloadObj = data;
        id++;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                // 发布成功
            }
            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                // 发布失败
            }
        });
    }

    /**
     * Message订阅
     */
    //消息订阅监听函数
    private static IConnectNotifyListener notifyListener = new IConnectNotifyListener() {
        /**
         * onNotify 会触发的前提是 shouldHandle 没有指定不处理这个topic
         * @param connectId 连接类型，这里判断是否长链 connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param topic 下行的topic
         * @param aMessage 下行的数据内容
         */
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            String data = new String((byte[]) aMessage.data);
            if(data!=null)
            {
                String[] temp = data.split("\\{");
                String[] temp2 = temp[2].split("\\}");
                String[] temp3 = temp2[0].split("\\,");
                for(int i=0; i<temp3.length;i++)
                {
                    String[] temp4 = temp3[i].split(":");
                    if(temp4.length>1){
                        Log.e(temp4[0],temp4[1]);
                        responseData.put(temp4[0].replaceAll("\"","").replaceAll(":",""),Double.valueOf(temp4[1]));
                    }
                }
            }

            // 服务端返回数据示例  data = {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
        }

        /**
         * @param connectId 连接类型，这里判断是否长链 connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param topic 下行topic
         * @return 是否要处理这个topic，如果为true，则会回调到onNotify；如果为false，onNotify不会回调这个topic相关的数据。建议默认为true。
         */
        @Override
        public boolean shouldHandle(String connectId, String topic) {
            return true;
        }

        /**
         * @param connectId 连接类型，这里判断是否长链 connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param connectState {@link com.aliyun.alink.linksdk.cmp.core.base.ConnectState}
         *     CONNECTED, 连接成功
         *     DISCONNECTED, 已断链
         *     CONNECTING, 连接中
         *     CONNECTFAIL; 连接失败
         */
        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            Log.d(TAG, "onConnectStateChange() called with: connectId = [" + connectId + "], connectState = [" + connectState + "]");
        }
    };

    //阿里云消息订阅
    void SubMessage(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        // subTopic 替换成您需要订阅的 topic
        subscribeRequest.topic = "/sys/gr6q69gAqhD/oppoR17/thing/event/property/post_reply";
        subscribeRequest.isSubscribe = true;
        subscribeRequest.qos = 0; // 支持0或者1
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                // 订阅成功
                Log.e("SubSuccess","success");
            }
            @Override
            public void onFailure(AError aError) {
                // 订阅失败
                Log.e("SubFailure","failure");
            }
        });
    }


    /**
     * Sensor
     */
    //光照传感器监听函数
    public class MyLightSensorListener implements SensorEventListener
    {
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            String str="Light:"+event.values[0];
//            for(int i=0;i<event.values.length;i++)
//            {
                //str+="\n"+(i+1)+":"+event.values[i];
                //Log.i("temp",str);
                double num = Double.valueOf(event.values[0]);
                reportData.put("LightLux",num);
                if(countL!=num)
                {
                    SendData();
                    countL=num;
                }
                //countL++;
                LText.setText(str);
//            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    }

    //压力传感器监听函数
    public class MyPressureSensorListener implements SensorEventListener
    {
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            String str="Pressure:"+event.values[0];
//            for(int i=0;i<event.values.length;i++)
//            {
//                str+="\n"+(i+1)+":"+event.values[i];
                double num = Double.valueOf(event.values[0]);
                reportData.put("Pressure",num);
                PText.setText(str);
                if(countP!=num)
                {
                    SendData();
                    countP=num;
                }
                //countP++;
//            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    }

    //温度传感器监听函数
    public class MyTempSensorListener implements SensorEventListener
    {
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            String str="Temp:"+event.values[0];
//            for(int i=0;i<event.values.length;i++)
//            {
//                str+="\n"+(i+1)+":"+event.values[i];
//                Log.i("temp",str);
                double num = Double.valueOf(event.values[0]);
                reportData.put("Temperature",num);
                if(countT!=num){
                    SendData();
                    countT=num;
                }
                //countT++;
                TText.setText(str);
//            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    }

    //湿度传感器监听函数
    public class MyHumSensorListener implements SensorEventListener
    {
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            String str="Hum:"+event.values[0];
//            for(int i=0;i<event.values.length;i++)
//            {
//                str+="\n"+(i+1)+":"+event.values[i];
//                Log.i("temp",str);
                double num = Double.valueOf(event.values[0]);
                reportData.put("Humidity",num);
                if(countH!=num){
                    SendData();
                    countH=num;
                }
                //countH++;
                HText.setText(str);
//            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    }

    private void setListener() {
        try {
            LinearLayout demoLayout = findViewById(R.id.id_demo_layout);
            int size = demoLayout.getChildCount();
            for (int i = 0; i < size; i++) {
                if (i == size - 1) {
                    break;
                }
                View child = demoLayout.getChildAt(i);
                child.setOnClickListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.w(TAG, "setListener exception " + e);
        }
    }


    private boolean checkReady() {
        if (DemoApplication.userDevInfoError) {
            showToast("设备三元组信息res/raw/deviceinfo格式错误");
            return false;
        }
        if (!DemoApplication.isInitDone) {
            showToast("初始化尚未成功，请稍后点击");
            return false;
        }
        errorTV.setVisibility(View.GONE);
        return true;
    }

    /**
     * Service
     * 解绑服务
     */
    public void btnBind(View view){
        if(connection == null){
            connection = new MyServiceConnection();
        }
        Intent intent = new Intent(this,SensorService.class);
        //绑定服务,BIND_AUTO_CREATE表示绑定时自动创建Service
        bindService(intent,connection,BIND_AUTO_CREATE);
    }


    /**
     * Service
     * 解绑服务
     */
    public void btnUnbind(View view){
        if(connection != null){
            //解绑服务，这里需要传入ServiceConnection对象
            unbindService(connection);
            connection = null;
        }
    }

    /**
     * Service
     * 调用绑定的服务里面的方法
     */
    public void btnCallServiceInMethod(View view){
    }

    /**
     * Service
     * 创建类继承ServiceConnection，用于解绑服务方法调用
     */
    public class MyServiceConnection implements ServiceConnection {
        //当客户端正常连接这个服务时，成功绑定到服务时调用该方法。注意IBinder参数对象
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //如果成功绑定，iBinder为MyService里面的IBinder对象
            Log.i("MainActivity","服务绑定成功，内存地址为："+iBinder.toString());
        }
        //当客户端与服务失去连接时调用该方法
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //解绑
            Log.i("MainActivity","服务解绑成功");
        }
    }

    @Override
    public void onClick(View v) {

    }

    private static ArrayList<DeviceInfoData> getTestDataList() {
        ArrayList<DeviceInfoData> infoDataArrayList = new ArrayList<DeviceInfoData>();

        DeviceInfoData test6 = new DeviceInfoData();
        test6.productKey = DemoApplication.productKey;
        test6.deviceName = DemoApplication.deviceName;
        test6.deviceSecret = DemoApplication.deviceSecret;
        infoDataArrayList.add(test6);
        return infoDataArrayList;
    }

    /**
     * 初始化
     * 耗时操作，建议放到异步线程ff
     */
    private void connect() {
        AppLog.d(TAG, "connect() called");
        // SDK初始化
        DeviceInfoData deviceInfoData = getTestDataList().get(testDeviceIndex.getAndIncrement() % getTestDataList().size());
        DemoApplication.productKey = deviceInfoData.productKey;
        DemoApplication.deviceName = deviceInfoData.deviceName;
        DemoApplication.deviceSecret = deviceInfoData.deviceSecret;
        new Thread(new Runnable() {
            @Override
            public void run() {
                InitManager.init(DemoActivity.this, DemoApplication.productKey, DemoApplication.deviceName,
                        DemoApplication.deviceSecret, DemoApplication.productSecret, new IDemoCallback() {

                            @Override
                            public void onError(AError aError) {
                                AppLog.d(TAG, "onError() called with: aError = [" + InitManager.getAErrorString(aError) + "]");
                                // 初始化失败，初始化失败之后需要用户负责重新初始化
                                // 如一开始网络不通导致初始化失败，后续网络恢复之后需要重新初始化

                                if (aError != null) {
//                                    AppLog.d(TAG, "初始化失败，错误信息：" + aError.getCode() + "-" + aError.getSubCode() + ", " + aError.getMsg());
                                    showToast("初始化失败，错误信息：" + aError.getCode() + "-" + aError.getSubCode() + ", " + aError.getMsg());
                                } else {
//                                    AppLog.d(TAG, "初始化失败");
                                    showToast("初始化失败");
                                }
                            }

                            @Override
                            public void onInitDone(Object data) {
                                AppLog.d(TAG, "onInitDone() called with: data = [" + data + "]");
                                DemoApplication.isInitDone = true;
                                showToast("初始化成功");
//                                AppLog.d(TAG, "初始化成功");
                            }
                        });
            }
        }).start();
    }

    /**
     * 耗时操作，建议放到异步线程
     * 反初始化同步接口
     */
    private void deinit() {
        AppLog.d(TAG, "deinit");
        DemoApplication.isInitDone = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 同步接口
                LinkKit.getInstance().deinit();
                DASHelper.getInstance().deinit();
                showToast("反初始化成功");
//                AppLog.d(TAG, "反初始化成功");
            }
        }).start();
    }

//    private void publishTest() {
//        try {
//            AppLog.d(TAG, "publishTest called.");
//            MqttPublishRequest request = new MqttPublishRequest();
//            // 支持 0 和 1， 默认0
//            request.qos = 1;
//            request.isRPC = false;
//            request.topic = "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/update";
//            request.msgId = String.valueOf(IDGenerater.generateId());
//            // TODO 用户根据实际情况填写 仅做参考
//            request.payloadObj = "{\"id\":\"" + request.msgId + "\", \"version\":\"1.0\"" + ",\"params\":{\"state\":\"1\"} }";
//            LinkKit.getInstance().publish(request, new IConnectSendListener() {
//                @Override
//                public void onResponse(ARequest aRequest, AResponse aResponse) {
//                    AppLog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + aResponse + "]");
//                    showToast("发布成功");
//                }
//
//                @Override
//                public void onFailure(ARequest aRequest, AError aError) {
//                    AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
//                    showToast("发布失败 " + (aError != null ? aError.getCode() : "null"));
//                }
//            });
//        } catch (Exception e) {
//            showToast("发布异常 ");
//        }
//    }


    private ScheduledFuture future = null;

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorL!=null) sm.registerListener(mls,sensorL,SensorManager.SENSOR_DELAY_NORMAL);//Light Sensor
        if(sensorP!=null) sm.registerListener(mps,sensorP,SensorManager.SENSOR_DELAY_NORMAL);//Pressure Sensor
        if(sensorH!=null) sm.registerListener(mhs,sensorH,SensorManager.SENSOR_DELAY_NORMAL);//humidity Sensor
        if(sensorT!=null) sm.registerListener(mts,sensorT,SensorManager.SENSOR_DELAY_NORMAL);//Temperature Sensor
        else{

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorL!=null) sm.unregisterListener(mls);//Light Sensor
        if(sensorP!=null) sm.unregisterListener(mps);//Pressure Sensor
        if(sensorT!=null) sm.unregisterListener(mts);//Temperature Sensor
        if(sensorH!=null) sm.unregisterListener(mhs);//Humidity Sensor
        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
