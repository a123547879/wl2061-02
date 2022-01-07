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
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTDMConfig;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.channel.core.base.IOnCallListener;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttInitParams;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.id2.Id2ItlsSdk;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
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
import java.util.HashMap;
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
// Demo 使用注意事项：
// 1.属性上报等与云端的操作 必须在初始化完成onInitDone之后执行；
// 2.初始化的时候请确保网络是OK的，如果初始化失败了，可以在网络连接再次初始化；
//
public class SensorMainActivity extends Application {
    private static final String TAG = "DemoApplication";


    /**
     * 判断是否初始化完成
     * 未初始化完成，所有和云端的长链通信都不通
     */
    public static boolean isInitDone = false;
    public static boolean userDevInfoError = false;
    public static DeviceInfoData mDeviceInfoData = null;


    public static String productKey = "gr6q69gAqhD", deviceName = "oppoR17", deviceSecret = "1f9dd7528f5dfb850406cd70efa1aecd";


    @Override
    public void onCreate() {
        super.onCreate();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.productKey = productKey;// 产品类型
        deviceInfo.deviceName = deviceName;// 设备名称
        deviceInfo.deviceSecret = deviceSecret;// 设备密钥
//        deviceInfo.iotId = "iot-06z00eb6vppmo5m.mqtt.iothub.aliyuncs.com";

        Map<String, ValueWrapper> propertyValues = new HashMap<>();

        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(productKey, deviceName, deviceSecret);

        LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = deviceInfo;
        params.propertyValues = propertyValues;
        params.mqttClientConfig = clientConfig;
        /**
         * 设备初始化建联
         * onError 初始化建联失败，需要用户重试初始化。如因网络问题导致初始化失败。
         * onInitDone 初始化成功
         */
        LinkKit.getInstance().init(SensorMainActivity.this, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                // 初始化失败 error包含初始化错误信息
                Toast.makeText(SensorMainActivity.this,error.getCode(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInitDone(Object data) {
                // 初始化成功 data 作为预留参数
                Toast.makeText(SensorMainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }
        });

        //LinkKitInitParams params = new LinkKitInitParams();
        // ... 其它设置参数保持和认证参数一致，以下是新增的配置参数

        /**
         * 设备是否支持被生活物联网平台APP发现
         * 需要确保开发的APP具备发现该类型设备的权限
         */
        IoTDMConfig ioTDMConfig = new IoTDMConfig();
        // 是否启用本地通信功能，默认不开启，
        // 启用之后会初始化本地通信CoAP相关模块，设备将允许被生活物联网平台的应用发现、绑定、控制，依赖enableThingModel开启
        ioTDMConfig.enableLocalCommunication = true;
        // 是否启用物模型功能，如果不开启，本地通信功能也不支持
        // 默认不开启，开启之后init方法会等到物模型初始化（包含请求物联网平台的物模型）完成之后才返回onInitDone
        ioTDMConfig.enableThingModel = true;
        // 是否启用网关功能
        // 默认不开启，开启之后，初始化的时候会初始化网关模块，获取物联网瓶体网关子设备列表
        ioTDMConfig.enableGateway = false;
        // LinkKitInitParams 设置ioTDMConfig
        params.ioTDMConfig = ioTDMConfig;

    }
}
