package com.aliyun.alink.devicesdk.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.devicesdk.demo.R;
import com.aliyun.alink.devicesdk.manager.IDemoCallback;
import com.aliyun.alink.devicesdk.manager.InitManager;
import com.aliyun.alink.dm.api.BaseInfo;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.model.ResponseModel;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.channel.core.base.IOnCallListener;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttInitParams;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.id2.Id2ItlsSdk;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.aliyun.alink.linksdk.tools.ThreadTools;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

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
// Demo ?????????????????????
// 1.????????????????????????????????? ????????????????????????onInitDone???????????????
// 2.????????????????????????????????????OK????????????????????????????????????????????????????????????????????????
//
public class DemoApplication extends Application {
    private static final String TAG = "DemoApplication";


    /**
     * ???????????????????????????
     * ????????????????????????????????????????????????????????????
     */
    public static boolean isInitDone = false;
    public static boolean userDevInfoError = false;
    public static DeviceInfoData mDeviceInfoData = null;


    public static String productKey = "gr6q69gAqhD", deviceName = "oppoR17", deviceSecret = "df6efed8bfe5a07f8d4aa27ecbd6d258", productSecret = null,
            password = null, username = null,clientId = null, deviceToken = null;
    public static Context mAppContext = null;
    private String registerType = null;

    //?????????sdk?????????
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        MqttConfigure.itlsLogLevel = Id2ItlsSdk.DEBUGLEVEL_NODEBUG;
        AppLog.setLevel(ALog.LEVEL_DEBUG);
        com.aliyun.isoc.aps.DASLogger.setEnabled(true);
        // ???????????????????????????65???
        MqttConfigure.setKeepAliveInterval(65);

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        mAppContext = getApplicationContext();
        // ??? raw ????????????????????????
        String testData = getFromRaw();
        AppLog.i(TAG, "sdk version = " + LinkKit.getInstance().getSDKVersion());
        // ????????????
        getDeviceInfoFrom(testData);

        if (userDevInfoError) {
            showToast("??????????????????????????????????????????????????????");
        }

        if (TextUtils.isEmpty(deviceSecret)) {
            tryGetFromSP();
        }

        /**
         * ????????????
         * ??????pk dn ps ????????? ???????????????????????????ds???????????????pk+dn+ds??????????????????????????????????????????ds???????????????????????????
         * ??????????????????????????? ds?????????????????????????????????????????????????????????????????????ds????????????????????????
         * ????????????????????????????????????????????????ds??????????????????????????????????????????ds???????????????????????????????????????????????????ds???????????????
         * ?????????????????????????????????????????????????????????ds?????????????????????????????????????????????????????????
         * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        if (TextUtils.isEmpty(deviceSecret) && !TextUtils.isEmpty(productSecret) && TextUtils.isEmpty(deviceToken)) {

            if (TextUtils.isEmpty(registerType)) {
                AppLog.d(TAG, "api??????????????????");
                InitManager.registerDevice(this, productKey, deviceName, productSecret, new IConnectSendListener() {
                    @Override
                    public void onResponse(ARequest aRequest, AResponse aResponse) {
                        AppLog.d(TAG, "registerDevice onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? "null" : aResponse.data) + "]");
                        if (aResponse != null && aResponse.data != null) {
                            // ???????????????????????????
                            ResponseModel<Map<String, String>> response = JSONObject.parseObject(aResponse.data.toString(),
                                    new TypeReference<ResponseModel<Map<String, String>>>() {
                                    }.getType());
                            if ("200".equals(response.code) && response.data != null && response.data.containsKey("deviceSecret") &&
                                    !TextUtils.isEmpty(response.data.get("deviceSecret"))) {
                                /**
                                 * ds???????????????????????????????????????????????????ds????????????????????????
                                 * ????????????????????????????????????
                                 */
                                deviceSecret = response.data.get("deviceSecret");
                                // getDeviceSecret success, to build connection.
                                // ????????? deviceSecret ??????????????????????????????
                                // ???????????????????????????????????????????????????????????????????????????????????????
                                SharedPreferences preferences = getSharedPreferences("deviceAuthInfo", 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("deviceId", productKey + deviceName);
                                editor.putString("deviceSecret", deviceSecret);
                                //??????????????????
                                editor.commit();

                                LinkKit.getInstance().deinit();
                                connect();
                            } else {
                            }
                        }
                    }

                    @Override
                    public void onFailure(ARequest aRequest, AError aError) {
                        AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
                    }
                });
            } else {

                // docs: https://help.aliyun.com/document_detail/132111.html?spm=a2c4g.11186623.6.600.4e073f827Y7a8y
                MqttInitParams initParams = new MqttInitParams(productKey, productSecret, deviceName, deviceSecret, MqttConfigure.MQTT_SECURE_MODE_TLS);
                if ("regnwl".equals(registerType)) {
                    initParams.registerType = "regnwl"; // ??????????????????
                    AppLog.d(TAG, "mqtt ??????????????????????????????");
                } else {
                    AppLog.d(TAG, "????????????registerType??????");
                    showToast("????????????registerType??????");
                    return;
                }
                LinkKit.getInstance().deviceDynamicRegister(this, initParams, new IOnCallListener() {
                    @Override
                    public void onSuccess(com.aliyun.alink.linksdk.channel.core.base.ARequest request, com.aliyun.alink.linksdk.channel.core.base.AResponse response) {
                        AppLog.i(TAG, "onSuccess() called with: request = [" + request + "], response = [" + response + "]");
                        // response.data is byte array
                        try {
                            String responseData = new String((byte[]) response.data);
                            JSONObject jsonObject = JSONObject.parseObject(responseData);
                            String pk = jsonObject.getString("productKey");
                            String dn = jsonObject.getString("deviceName");
                            // ???????????????????????????
                            String ds = jsonObject.getString("deviceSecret");
                            // ????????????????????????
                            String ci = jsonObject.getString("clientId");
                            String dt = jsonObject.getString("deviceToken");

                            clientId = ci;
                            deviceToken = dt;
                            deviceSecret = ds;

                            // ????????? clientId & deviceToken ??????????????????????????????
                            // ??????????????????????????????????????????????????????????????????????????????app?????????????????????????????????????????????
                            SharedPreferences preferences = getSharedPreferences("deviceAuthInfo", 0);
                            if ((!TextUtils.isEmpty(clientId) && !TextUtils.isEmpty(deviceToken)) || (!TextUtils.isEmpty(deviceSecret))) {
                                showToast("????????????????????????????????????");
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("deviceId", productKey + deviceName);
                                editor.putString("clientId", clientId);
                                editor.putString("deviceToken", deviceToken);
                                editor.putString("deviceSecret", deviceSecret);
                                editor.commit();
                                try {
                                    Thread.sleep(2000);
                                } catch (Exception e){

                                }
                                destroyRegisterConnect(true);
                            } else {
                                showToast("??????????????????????????????????????????????????????????????? " + responseData);
                                destroyRegisterConnect(false);
                            }
                        } catch (Exception e) {
                            showToast("?????????????????????????????????????????????????????????????????????");
                            e.printStackTrace();
                            destroyRegisterConnect(false);
                        }

                    }

                    @Override
                    public void onFailed(com.aliyun.alink.linksdk.channel.core.base.ARequest request, com.aliyun.alink.linksdk.channel.core.base.AError error) {
                        AppLog.w(TAG, "onFailed() called with: request = [" + request + "], error = [" + error + "]");
                        showToast("???????????????????????????????????? " + error);
                        destroyRegisterConnect(false);
                    }

                    @Override
                    public boolean needUISafety() {
                        return false;
                    }
                });
            }
        } else if (!TextUtils.isEmpty(deviceSecret) || !TextUtils.isEmpty(password) || !TextUtils.isEmpty(deviceToken)){
            connect();
        } else {
            AppLog.e(TAG, "res/raw/deviceinfo invalid.");
            if (!userDevInfoError) {
                showToast("???????????????????????????????????????");
            }
            userDevInfoError = true;
        }

    }

    /**
     * ???????????????????????? ???????????????????????????????????????mqtt ???????????? Disconnecting is not allowed from a callback method (32107)
     * @param needConnect
     */
    private void destroyRegisterConnect(final boolean needConnect) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LinkKit.getInstance().stopDeviceDynamicRegister(10 * 1000, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken iMqttToken) {
                            AppLog.d(TAG, "onSuccess() called with: iMqttToken = [" + iMqttToken + "]");
                            if (needConnect) {
                                connect();
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                            AppLog.w(TAG, "onFailure() called with: iMqttToken = [" + iMqttToken + "], throwable = [" + throwable + "]");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * ???????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????SDK??????????????????
     *
     * onError ???????????????
     * onInitDone ???????????????
     *
     * SDK ?????????userName+password+clientId ????????????????????????????????????????????????????????????
     * ?????????????????????InitManager.init????????? deviceSecret, productSecret ????????????
     * MqttConfigure.mqttUserName = username;
     * MqttConfigure.mqttPassWord = password;
     * MqttConfigure.mqttClientId = clientId;
     *
     */
    private void connect() {
        AppLog.d(TAG, "connect() called");
        // SDK?????????
//        MqttConfigure.mqttUserName = username;
//        MqttConfigure.mqttPassWord = password;
//        MqttConfigure.mqttClientId = clientId;
        InitManager.init(this, productKey, deviceName, deviceSecret, productSecret, new IDemoCallback() {

            @Override
            public void onError(AError aError) {
                AppLog.d(TAG, "onError() called with: aError = [" + aError + "]");
                AppLog.d(TAG,Log.getStackTraceString(new Throwable()));
                // ????????????????????????????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????????????????????????????????????????????
                showToast("???????????????");
            }

            @Override
            public void onInitDone(Object data) {
                AppLog.d(TAG, "onInitDone() called with: data = [" + data + "]");
                showToast("???????????????");
                isInitDone = true;
            }
        });

    }

    /**
     * ????????????deviceSecret?????????????????????????????????
     * ??????????????????????????????????????????????????????
     * ???????????????deviceSecret???????????????????????????????????????????????????????????????????????????????????????
     */
    private void tryGetFromSP() {
        AppLog.d(TAG, "tryGetFromSP() called");
        SharedPreferences authInfo = getSharedPreferences("deviceAuthInfo", Activity.MODE_PRIVATE);
        String pkDn = authInfo.getString("deviceId", null);
        String ci = authInfo.getString("clientId", null);
        String dt = authInfo.getString("deviceToken", null);
        String ds = authInfo.getString("deviceSecret", null);
        if (pkDn != null && pkDn.equals(productKey + deviceName) &&
                (!TextUtils.isEmpty(ds) || !TextUtils.isEmpty(dt))) {
            AppLog.d(TAG, "tryGetFromSP update ds from sp.");
            deviceSecret = ds;
            clientId = ci;
            deviceToken = dt;
        } else {
            AppLog.d(TAG, "tryGetFromSP no cache data.");
        }
    }

    private void getDeviceInfoFrom(String testData) {
        AppLog.d(TAG, "getDeviceInfoFrom() called with: testData = [" + testData + "]");
        try {
            if (testData == null) {
                AppLog.e(TAG, "getDeviceInfoFrom: data empty.");
                userDevInfoError = true;
                return;
            }
            Gson mGson = new Gson();
            DeviceInfoData deviceInfoData = mGson.fromJson(testData, DeviceInfoData.class);
            if (deviceInfoData == null) {
                AppLog.e(TAG, "getDeviceInfoFrom: file format error.");
                userDevInfoError = true;
                return;
            }
            AppLog.d(TAG, "getDeviceInfoFrom deviceInfoData=" + deviceInfoData);
            if (checkValid(deviceInfoData)) {
                mDeviceInfoData = new DeviceInfoData();
                mDeviceInfoData.productKey = deviceInfoData.productKey;
                mDeviceInfoData.productSecret = deviceInfoData.productSecret;
                mDeviceInfoData.deviceName = deviceInfoData.deviceName;
                mDeviceInfoData.deviceSecret = deviceInfoData.deviceSecret;
                mDeviceInfoData.username = deviceInfoData.username;
                mDeviceInfoData.password = deviceInfoData.password;
                mDeviceInfoData.clientId = deviceInfoData.clientId;
                mDeviceInfoData.deviceToken = deviceInfoData.deviceToken;
                mDeviceInfoData.registerType = deviceInfoData.registerType;

                userDevInfoError = false;

                mDeviceInfoData.subDevice = new ArrayList<>();
                if (deviceInfoData.subDevice == null) {
                    AppLog.d(TAG, "getDeviceInfoFrom: subDevice empty..");
                    return;
                }
                for (int i = 0; i < deviceInfoData.subDevice.size(); i++) {
                    if (checkValid(deviceInfoData.subDevice.get(i))) {
                        mDeviceInfoData.subDevice.add(deviceInfoData.subDevice.get(i));
                    } else {
                        AppLog.d(TAG, "getDeviceInfoFrom: subDevice info invalid. discard.");
                    }
                }

                productKey = mDeviceInfoData.productKey;
                deviceName = mDeviceInfoData.deviceName;
                deviceSecret = mDeviceInfoData.deviceSecret;
                productSecret = mDeviceInfoData.productSecret;
                password = mDeviceInfoData.password;
                username = mDeviceInfoData.username;
                clientId = mDeviceInfoData.clientId;
                deviceToken = mDeviceInfoData.deviceToken;
                registerType = mDeviceInfoData.registerType;

                AppLog.d(TAG, "getDeviceInfoFrom: final data=" + mDeviceInfoData);
            } else {
                AppLog.e(TAG, "res/raw/deviceinfo error.");
                userDevInfoError = true;
            }

        } catch (Exception e) {
            AppLog.e(TAG, "getDeviceInfoFrom: e", e);
            userDevInfoError = true;
        }

    }

    private boolean checkValid(BaseInfo baseInfo) {
        if (baseInfo == null) {
            return false;
        }
        if (TextUtils.isEmpty(baseInfo.productKey) || TextUtils.isEmpty(baseInfo.deviceName)) {
            return false;
        }
        if (baseInfo instanceof DeviceInfoData) {
            if (TextUtils.isEmpty(((DeviceInfo) baseInfo).productSecret) && TextUtils.isEmpty(((DeviceInfo) baseInfo).deviceSecret) && TextUtils.isEmpty(((DeviceInfoData) baseInfo).password)) {
                return false;
            }
        }
        return true;
    }


    /**
     * ??????????????????????????????????????????????????????
     * ?????????????????? ?????????????????????
     * 1.?????????????????????????????????????????????????????????
     * 2.?????????????????????????????????(raw/deviceinfo)????????????????????? productKey???deviceName??? productSecret;
     * 3.????????????????????????????????????????????????deviceSecret????????????????????????
     *
     * @return
     */
    public String getFromRaw() {
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(getResources().openRawResource(R.raw.deviceinfo));
            bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufReader != null) {
                    bufReader.close();
                }
                if (inputReader != null){
                    inputReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    private void showToast(final String message) {
        ThreadTools.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DemoApplication.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Context getAppContext() {
        return mAppContext;
    }
}
