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
     * Data????????????
     */
    //?????????????????????????????????
    void SendData()
    {
        MqttPublishRequest request = new MqttPublishRequest();
        // ???????????????????????????
        request.isRPC = false;
        // ??????topic??????????????????Topic?????????????????????????????????
        request.topic = "/sys/gr6q69gAqhD/oppoR17/thing/event/property/post";
        // ?????? qos
        request.qos = 0;
        // data ??????????????????????????? json String?????????id???????????????????????????
        //?????? ???????????? {"id":"160865432","method":"thing.event.property.post","params":{"LightSwitch":1},"version":"1.0"}
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
                // ????????????
            }
            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                // ????????????
            }
        });
    }

    /**
     * Message??????
     */
    //????????????????????????
    private static IConnectNotifyListener notifyListener = new IConnectNotifyListener() {
        /**
         * onNotify ????????????????????? shouldHandle ???????????????????????????topic
         * @param connectId ??????????????????????????????????????? connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param topic ?????????topic
         * @param aMessage ?????????????????????
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

            // ???????????????????????????  data = {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
        }

        /**
         * @param connectId ??????????????????????????????????????? connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param topic ??????topic
         * @return ?????????????????????topic????????????true??????????????????onNotify????????????false???onNotify??????????????????topic?????????????????????????????????true???
         */
        @Override
        public boolean shouldHandle(String connectId, String topic) {
            return true;
        }

        /**
         * @param connectId ??????????????????????????????????????? connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param connectState {@link com.aliyun.alink.linksdk.cmp.core.base.ConnectState}
         *     CONNECTED, ????????????
         *     DISCONNECTED, ?????????
         *     CONNECTING, ?????????
         *     CONNECTFAIL; ????????????
         */
        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            Log.d(TAG, "onConnectStateChange() called with: connectId = [" + connectId + "], connectState = [" + connectState + "]");
        }
    };

    //?????????????????????
    void SubMessage(){
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        // subTopic ??????????????????????????? topic
        subscribeRequest.topic = "/sys/gr6q69gAqhD/oppoR17/thing/event/property/post_reply";
        subscribeRequest.isSubscribe = true;
        subscribeRequest.qos = 0; // ??????0??????1
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
                // ????????????
                Log.e("SubSuccess","success");
            }
            @Override
            public void onFailure(AError aError) {
                // ????????????
                Log.e("SubFailure","failure");
            }
        });
    }


    /**
     * Sensor
     */
    //???????????????????????????
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

    //???????????????????????????
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

    //???????????????????????????
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

    //???????????????????????????
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
            showToast("?????????????????????res/raw/deviceinfo????????????");
            return false;
        }
        if (!DemoApplication.isInitDone) {
            showToast("???????????????????????????????????????");
            return false;
        }
        errorTV.setVisibility(View.GONE);
        return true;
    }

    /**
     * Service
     * ????????????
     */
    public void btnBind(View view){
        if(connection == null){
            connection = new MyServiceConnection();
        }
        Intent intent = new Intent(this,SensorService.class);
        //????????????,BIND_AUTO_CREATE???????????????????????????Service
        bindService(intent,connection,BIND_AUTO_CREATE);
    }


    /**
     * Service
     * ????????????
     */
    public void btnUnbind(View view){
        if(connection != null){
            //?????????????????????????????????ServiceConnection??????
            unbindService(connection);
            connection = null;
        }
    }

    /**
     * Service
     * ????????????????????????????????????
     */
    public void btnCallServiceInMethod(View view){
    }

    /**
     * Service
     * ???????????????ServiceConnection?????????????????????????????????
     */
    public class MyServiceConnection implements ServiceConnection {
        //??????????????????????????????????????????????????????????????????????????????????????????IBinder????????????
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //?????????????????????iBinder???MyService?????????IBinder??????
            Log.i("MainActivity","???????????????????????????????????????"+iBinder.toString());
        }
        //???????????????????????????????????????????????????
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //??????
            Log.i("MainActivity","??????????????????");
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
     * ?????????
     * ???????????????????????????????????????ff
     */
    private void connect() {
        AppLog.d(TAG, "connect() called");
        // SDK?????????
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
                                // ????????????????????????????????????????????????????????????????????????
                                // ?????????????????????????????????????????????????????????????????????????????????????????????

                                if (aError != null) {
//                                    AppLog.d(TAG, "?????????????????????????????????" + aError.getCode() + "-" + aError.getSubCode() + ", " + aError.getMsg());
                                    showToast("?????????????????????????????????" + aError.getCode() + "-" + aError.getSubCode() + ", " + aError.getMsg());
                                } else {
//                                    AppLog.d(TAG, "???????????????");
                                    showToast("???????????????");
                                }
                            }

                            @Override
                            public void onInitDone(Object data) {
                                AppLog.d(TAG, "onInitDone() called with: data = [" + data + "]");
                                DemoApplication.isInitDone = true;
                                showToast("???????????????");
//                                AppLog.d(TAG, "???????????????");
                            }
                        });
            }
        }).start();
    }

    /**
     * ???????????????????????????????????????
     * ????????????????????????
     */
    private void deinit() {
        AppLog.d(TAG, "deinit");
        DemoApplication.isInitDone = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // ????????????
                LinkKit.getInstance().deinit();
                DASHelper.getInstance().deinit();
                showToast("??????????????????");
//                AppLog.d(TAG, "??????????????????");
            }
        }).start();
    }

//    private void publishTest() {
//        try {
//            AppLog.d(TAG, "publishTest called.");
//            MqttPublishRequest request = new MqttPublishRequest();
//            // ?????? 0 ??? 1??? ??????0
//            request.qos = 1;
//            request.isRPC = false;
//            request.topic = "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/update";
//            request.msgId = String.valueOf(IDGenerater.generateId());
//            // TODO ?????????????????????????????? ????????????
//            request.payloadObj = "{\"id\":\"" + request.msgId + "\", \"version\":\"1.0\"" + ",\"params\":{\"state\":\"1\"} }";
//            LinkKit.getInstance().publish(request, new IConnectSendListener() {
//                @Override
//                public void onResponse(ARequest aRequest, AResponse aResponse) {
//                    AppLog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + aResponse + "]");
//                    showToast("????????????");
//                }
//
//                @Override
//                public void onFailure(ARequest aRequest, AError aError) {
//                    AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
//                    showToast("???????????? " + (aError != null ? aError.getCode() : "null"));
//                }
//            });
//        } catch (Exception e) {
//            showToast("???????????? ");
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
