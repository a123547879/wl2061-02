package com.aliyun.alink.devicesdk.demo;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.devicesdk.app.AppLog;
import com.aliyun.alink.dm.api.IShadowRRPC;
import com.aliyun.alink.dm.shadow.ShadowResponse;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcHandle;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ThreadTools;

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

public class ShadowActivity extends BaseTemplateActivity {
    private static final String TAG = "ShadowActivity";

    private static long version = 1;

    private static String shadowUpdate = "{" + "\"method\": \"update\"," + "\"state\": {" + "\"reported\": {" +
            "\"color\": \"red\"" + "}" + "}," + "\"version\": {ver}" + "}";

    private String shadowGet = "{" + "\"method\": \"get\"" + "}";

    private String shadowDelete = "{" + "\"method\": \"delete\"," + "\"state\": {" + "\"reported\": {" +
            "\"color\": \"null\"" + "}" + "}," + "\"version\": {ver}" + "}";

    private String shadowDeleteAll = "{\n" +
            "\"method\": \"delete\",\n" +
            "\"state\": {\n" +
            "\"reported\":\"null\"\n" +
            "},\n" +
            "\"version\": {ver}\n" +
            "}";

    @Override
    protected void initViewData() {
        funcTV1.setText("??????????????????");
        funcET1.setText(shadowUpdate);
        funcBT1.setText("??????");

        funcTV2.setText("??????????????????");
        funcET2.setText(shadowGet);
        funcBT2.setText("??????");

        funcTV3.setText("??????????????????");
        funcET3.setText(shadowDelete);
        funcBT3.setText("??????");
        funcRL3.setVisibility(View.VISIBLE);

        funcTV4.setText("??????????????????");
        funcET4.setEnabled(false);
        funcBT4.setText("??????");
        funcRL4.setVisibility(View.VISIBLE);

        onFunc2Click();
    }

    @Override
    protected void onFunc4Click() {
        listenDownStream();
    }

    /**
     * ??????????????????
     */
    @Override
    protected void onFunc3Click() {
        String data = funcET3.getText().toString();
        try {
            data = data.replace("{ver}", String.valueOf(++version));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("??????????????????");
            return;
        }
        LinkKit.getInstance().getDeviceShadow().shadowUpload(data, deleteListener);
    }

    private static IConnectSendListener deleteListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            AppLog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            showToast("????????????????????????");
            try {
                if (aRequest instanceof MqttPublishRequest && aResponse != null) {
                    String dataStr = null;
                    if (aResponse.data instanceof byte[]) {
                        dataStr = new String((byte[]) aResponse.data, "UTF-8");
                    } else if (aResponse.data instanceof String) {
                        dataStr = (String) aResponse.data;
                    } else {
                        dataStr = String.valueOf(aResponse.data);
                    }
                    AppLog.d(TAG, "dataStr = " + dataStr);
                    ShadowResponse<String> response = JSONObject.parseObject(dataStr, new TypeReference<ShadowResponse<String>>() {
                    }.getType());
                    if (response != null && response.version != null && TextUtils.isDigitsOnly(response.version)) {
                        version = Long.valueOf(response.version);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                AppLog.e(TAG, "update version failed.");
            } catch (Exception e) {
                AppLog.e(TAG, "update response parse exception.");
            }
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("????????????????????????");
        }
    };

    /**
     * ??????????????????
     * ????????????????????????????????? ?????? version??????
     */
    @Override
    protected void onFunc2Click() {
        String data = funcET2.getText().toString();
        LinkKit.getInstance().getDeviceShadow().shadowUpload(data, getListener);
    }

    private static IConnectSendListener getListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            AppLog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            showToast("????????????????????????");

            try {
                if (aRequest instanceof MqttPublishRequest && aResponse != null) {
                    String dataStr = null;
                    if (aResponse.data instanceof byte[]) {
                        dataStr = new String((byte[]) aResponse.data, "UTF-8");
                    } else if (aResponse.data instanceof String) {
                        dataStr = (String) aResponse.data;
                    } else {
                        dataStr = String.valueOf(aResponse.data);
                    }
                    AppLog.d(TAG, "dataStr = " + dataStr);
                    ShadowResponse<String> response = JSONObject.parseObject(dataStr, new TypeReference<ShadowResponse<String>>() {
                    }.getType());
                    if (response != null && response.version != null && TextUtils.isDigitsOnly(response.version)) {
                        version = Long.valueOf(response.version);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                AppLog.e(TAG, "update version failed.");
            } catch (Exception e) {
                AppLog.e(TAG, "update response parse exception.");
            }

        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("????????????????????????");
        }
    };

    /**
     * ??????????????????
     * ????????????????????????????????????????????????
     */
    @Override
    protected void onFunc1Click() {
        String data = funcET1.getText().toString();
        try {
            data = data.replace("{ver}", String.valueOf(++version));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("??????????????????");
            return;
        }
        LinkKit.getInstance().getDeviceShadow().shadowUpload(data, reportListener);
    }

    private static IConnectSendListener reportListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            AppLog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            showToast("????????????????????????");
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("????????????????????????");
        }
    };

    /**
     * ?????????????????????????????? topic
     * ????????????????????????????????????
     */
    public void listenDownStream() {
        LinkKit.getInstance().getDeviceShadow().setShadowChangeListener(downListener);
    }

    private static IShadowRRPC downListener = new IShadowRRPC() {
        @Override
        public void onSubscribeSuccess(ARequest aRequest) {
            showToast("??????????????????????????????");
        }

        @Override
        public void onSubscribeFailed(ARequest aRequest, AError aError) {
            showToast("??????????????????????????????");
        }

        @Override
        public void onReceived(ARequest aRequest, AResponse aResponse, IConnectRrpcHandle iConnectRrpcHandle) {
            // TODO user logic
            showToast("??????????????????????????????");
            try {
                if (aRequest != null) {
                    String dataStr = null;
                    if (aResponse.data instanceof byte[]) {
                        dataStr = new String((byte[]) aResponse.data, "UTF-8");
                    } else if (aResponse.data instanceof String) {
                        dataStr = (String) aResponse.data;
                    } else {
                        dataStr = String.valueOf(aResponse.data);
                    }
                    AppLog.d(TAG, "dataStr = " + dataStr);

                    ShadowResponse<String> shadowResponse = JSONObject.parseObject(dataStr, new TypeReference<ShadowResponse<String>>() {
                    }.getType());
                    if (shadowResponse != null && shadowResponse.version != null && TextUtils.isDigitsOnly(shadowResponse.version)) {
                        version = Long.valueOf(shadowResponse.version);
                    }

                    AResponse response = new AResponse();
                    // TODO ????????????????????????
                    // ???????????????????????? ???????????????????????????
                    // ?????????????????????????????????
                    // ???????????????????????????
                    response.data = shadowUpdate.replace("{ver}", String.valueOf(++version));
                    // ???????????? replyTopic ???????????? ?????????????????????
                    iConnectRrpcHandle.onRrpcResponse(null, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onResponseSuccess(ARequest aRequest) {
        }

        @Override
        public void onResponseFailed(ARequest aRequest, AError aError) {
        }
    };


    /**
     * ????????????????????????
     */
    public void reportShadow(View view) {
        ThreadTools.submitTask(new Runnable() {
            @Override
            public void run() {
                String updateParams = "{\n" + "\"method\": \"update\",\n" + "\"state\": {\"reported\": {\"color\": \"red\"}},\"version\":" + (++version) + "}";
                LinkKit.getInstance().getDeviceShadow().shadowUpload(updateParams, mConnectSendListener);
            }
        }, false);
    }

}
