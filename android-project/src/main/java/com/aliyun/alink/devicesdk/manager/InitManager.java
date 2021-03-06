package com.aliyun.alink.devicesdk.manager;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.devicesdk.app.AppLog;
import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IoTApiClientConfig;
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTDMConfig;
import com.aliyun.alink.linkkit.api.IoTH2Config;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
import com.aliyun.alink.linksdk.cmp.api.ConnectSDK;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.hubapi.HubApiRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.id2.Id2ItlsSdk;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tools.AError;

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

public class InitManager {
    private static final String TAG = "InitManager";

    /**
     * ?????????????????????????????????????????????deviceSecret??? ??????????????????????????????
     * ???????????????????????????
     * 1.??????????????????????????????????????????
     * 2.????????????????????? pk???dn???
     * @param context ?????????
     * @param productKey ????????????
     * @param deviceName ???????????? ????????????????????????
     * @param productSecret ????????????
     * @param listener ??????????????????
     */
    public static void registerDevice(Context context, String productKey, String deviceName, String productSecret, IConnectSendListener listener) {
        DeviceInfo myDeviceInfo = new DeviceInfo();
        myDeviceInfo.productKey = productKey;
        myDeviceInfo.deviceName = deviceName;
        myDeviceInfo.productSecret = productSecret;
        LinkKitInitParams params = new LinkKitInitParams();
        params.connectConfig = new IoTApiClientConfig();
        // ????????????????????????????????????????????? connectConfig ??? domain ?????????
        params.deviceInfo = myDeviceInfo;
        HubApiRequest hubApiRequest = new HubApiRequest();
        hubApiRequest.path = "/auth/register/device";
        // ????????????????????????
        LinkKit.getInstance().deviceRegister(context, params, hubApiRequest, listener);
    }



    /**
     * Android ????????? SDK ?????????????????????
     * @param context ?????????
     * @param productKey ????????????
     * @param deviceName ????????????
     * @param deviceSecret ????????????
     * @param productSecret ????????????
     * @param callback ???????????????????????????
     */
    public static void init(final Context context, String productKey, String deviceName, String deviceSecret, String productSecret, final IDemoCallback callback) {
        // ???????????????????????????
        DeviceInfo deviceInfo = new DeviceInfo();
        // ????????????
        deviceInfo.productKey = productKey;
        // ????????????
        deviceInfo.deviceName = deviceName;
        // ????????????
        deviceInfo.deviceSecret = deviceSecret;
        // ????????????
        deviceInfo.productSecret = productSecret;
        //  ??????????????????
        IoTApiClientConfig userData = new IoTApiClientConfig();
        // ?????????????????????????????????????????????????????????????????????????????????
        /**
         * ??????????????????????????????
         * ?????????????????????????????????????????????????????????????????????????????????????????????
         * ??????????????????????????????????????????????????????????????????????????????
         * ??????????????????????????????
         */
        Map<String, ValueWrapper> propertyValues = new HashMap<>();
        // ??????
//        propertyValues.put("LightSwitch", new ValueWrapper.BooleanValueWrapper(0));

        final LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = deviceInfo;
        params.propertyValues = propertyValues;
        params.connectConfig = userData;

        /**
         * ??????????????????????????????
         */
        IoTH2Config ioTH2Config = new IoTH2Config();
        ioTH2Config.clientId = "client-id";
        ioTH2Config.endPoint = "https://" + productKey + ioTH2Config.endPoint;// ????????????
        //ioTH2Config.endPoint = "https://11.158.128.17:9999"???// h2????????????
        params.iotH2InitParams = ioTH2Config;
        Id2ItlsSdk.init(context);
        /**
         * ???????????????????????????????????????????????????????????????
         * Mqtt ??????????????????
         * ????????????????????????????????????????????????
         */
        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(productKey, deviceName, deviceSecret);
        // ?????? receiveOfflineMsg = !cleanSession, ??????????????????????????? false
        clientConfig.receiveOfflineMsg = true;//cleanSession=0 ??????????????????
        clientConfig.receiveOfflineMsg = false;//cleanSession=1 ?????????????????????
        // ?????? mqtt ?????????????????????"{pk}.iot-as-mqtt.cn-shanghai.aliyuncs.com:1883" ,???????????????????????????????????????????????????
        // ?????????????????? itls
        if ("itls_secret".equals(deviceSecret)){
            clientConfig.channelHost = productKey + ".itls.cn-shanghai.aliyuncs.com:1883";//??????
            clientConfig.productSecret = productSecret;
            clientConfig.secureMode = 8;
        }
        params.mqttClientConfig = clientConfig;

        /**
         * ?????????????????????????????????APP??????
         * ?????????????????????APP????????????????????????????????????
         */
        IoTDMConfig ioTDMConfig = new IoTDMConfig();
        // ???????????????????????????????????????????????????
        // ????????????????????????????????????CoAP????????????????????????????????????????????????????????????????????????????????????????????????enableThingModel??????
        ioTDMConfig.enableLocalCommunication = false;
        // ??????????????????????????????????????????????????????????????????????????????
        // ??????????????????????????????init???????????????????????????????????????????????????????????????????????????????????????onInitDone
        ioTDMConfig.enableThingModel = false;
        // ????????????????????????
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ioTDMConfig.enableGateway = false;
        // ????????????????????????????????????????????????
        ioTDMConfig.enableLogPush = false;

        params.ioTDMConfig = ioTDMConfig;

        // ??????clientId ???????????????????????????????????????
        //MqttConfigure.clientId = "aabbccdd";

        //????????????????????????
        MqttConfigure.deviceToken = DemoApplication.deviceToken;
        MqttConfigure.clientId = DemoApplication.clientId;

        LinkKit.getInstance().registerOnPushListener(notifyListener);
        /**
         * ?????????????????????
         * onError ????????????????????????????????????????????????????????????????????????????????????????????????
         * onInitDone ???????????????
         */
        LinkKit.getInstance().init(context, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                AppLog.d(TAG, "onError() called with: error = [" + getAErrorString(error) + "]");
                DASHelper.getInstance().notifyConnectionStatus(ConnectState.CONNECTFAIL);
                callback.onError(error);
            }

            @Override
            public void onInitDone(Object data) {
                AppLog.d(TAG, "onInitDone() called with: data = [" + data + "]");
                initDAS(context, params);
                DASHelper.getInstance().notifyConnectionStatus(ConnectState.CONNECTED);
                callback.onInitDone(data);
            }
        });
    }

    private static void initDAS(Context context, LinkKitInitParams params) {
        if (context != null && params != null && params.deviceInfo != null
                && !TextUtils.isEmpty(params.deviceInfo.productKey)
                && !TextUtils.isEmpty(params.deviceInfo.productKey)
                && TextUtils.isEmpty(params.deviceInfo.deviceSecret)
                && !TextUtils.isEmpty(MqttConfigure.deviceToken)) {
            DASHelper.getInstance().init(context.getApplicationContext(),
                    params.deviceInfo.productKey,
                    params.deviceInfo.deviceName);
        }
    }

    /**
     * ???????????????????????? MQTT ????????????????????????????????????
     */
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
            // ???????????????????????????  data = {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
            AppLog.d(TAG, "onNotify() called with: connectId = [" + connectId + "], topic = [" + topic + "], aMessage = [" + data + "]");

            if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/ext/rrpc/")) {
                ToastUtils.showToast("?????????????????????RRPC?????????topic=" + topic + ",data=" + data);
                //?????? topic=/ext/rrpc/1138654706478941696//a1ExY4afKY1/testDevice/user/get
                //AppLog.d(TAG, "receice Message=" + new String((byte[]) aMessage.data));
                // ???????????????????????????  {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
                MqttPublishRequest request = new MqttPublishRequest();
                request.isRPC = false;
                request.topic = topic;
                String[] array = topic.split("/");
                String resId = array[3];
                request.msgId = resId;
                // TODO ?????????????????????????????? ????????????
                request.payloadObj = "{\"id\":\"" + resId + "\", \"code\":\"200\"" + ",\"data\":{} }";
                LinkKit.getInstance().publish(request, new IConnectSendListener() {
                    @Override
                    public void onResponse(ARequest aRequest, AResponse aResponse) {
                        // ????????????
//                        ToastUtils.showToast("????????????RRPC??????????????????");
                    }

                    @Override
                    public void onFailure(ARequest aRequest, AError aError) {
                        // ????????????
//                        ToastUtils.showToast("????????????RRPC??????????????????");
                    }
                });
            } else if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/sys/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/rrpc/request/")) {
                ToastUtils.showToast("??????????????????RRPC?????????topic=" + topic + ",data=" + data);
//                    AppLog.d(TAG, "receice Message=" + new String((byte[]) aMessage.data));
                // ???????????????????????????  {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
                MqttPublishRequest request = new MqttPublishRequest();
                // ?????? 0 ??? 1??? ??????0
//                request.qos = 0;
                request.isRPC = false;
                request.topic = topic.replace("request", "response");
                String[] array = topic.split("/");
                String resId = array[6];
                request.msgId = resId;
                // TODO ?????????????????????????????? ????????????
                request.payloadObj = "{\"id\":\"" + resId + "\", \"code\":\"200\"" + ",\"data\":{} }";
//                    aResponse.data =
                LinkKit.getInstance().publish(request, new IConnectSendListener() {
                    @Override
                    public void onResponse(ARequest aRequest, AResponse aResponse) {
//                        ToastUtils.showToast("????????????RRPC??????????????????");
                    }

                    @Override
                    public void onFailure(ARequest aRequest, AError aError) {
//                        ToastUtils.showToast("????????????RRPC??????????????????");
                    }
                });
            } else if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/sys/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/broadcast/request/")) {
                /**
                 * topic ?????????/sys/${pk}/${dn}/broadcast/request/+
                 * ?????????????????????????????????????????????????????????ack??????????????????????????????????????????????????????ack
                 * ?????????/sys/a14NQ5RLiZA/android_lp_test1/broadcast/request/1229336863924294656
                 * ????????????????????????????????????Base64???????????????????????????????????????
                 * ???????????? org.apache.commons.codec.binary.Base64.encodeBase64String("broadcastContent".getBytes())
                 */
                //
                ToastUtils.showToast("?????????????????????????????????topic=" + topic + ",data=" + data);
                //TODO ???????????????????????????????????????

            } else if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/broadcast/" + DemoApplication.productKey )) {
                //
                /**
                 * topic ???????????????????????????????????????topic ?????????/broadcast/${pk}/${?????????action}????????????????????????topic??????
                 * ?????????/broadcast/a14NQ5RLiZA/oldBroadcast
                 * ????????????????????????????????????Base64???????????????????????????????????????
                 * ???????????? org.apache.commons.codec.binary.Base64.encodeBase64String("broadcastContent".getBytes())
                 */
                ToastUtils.showToast("???????????????????????????topic=" + topic + ",data=" + data);
                //TODO ?????????????????????????????????
            } else {
                ToastUtils.showToast("?????????????????????topic=" + topic + ",data=" + data);
                /**
                 * TODO
                 * ????????????????????? topic ???????????????
                 */
            }
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
         * @param connectState {@link ConnectState}
         *     CONNECTED, ????????????
         *     DISCONNECTED, ?????????
         *     CONNECTING, ?????????
         *     CONNECTFAIL; ????????????
         */
        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            AppLog.d(TAG, "onConnectStateChange() called with: connectId = [" + connectId + "], connectState = [" + connectState + "]");
            DASHelper.getInstance().notifyConnectionStatus(connectState);
        }
    };

    public static String getAErrorString(AError error) {
        if (error == null) {
            return null;
        }
        return JSONObject.toJSONString(error);
    }
}
