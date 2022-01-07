# encoding=utf-8
import time
import sys
import hashlib
import hmac
import base64
import stomp
import ssl
import schedule
import threading
import pymysql.cursors
import json
import time
import datetime
import string

#from flask import Flask
from aliyunsdkcore.client import AcsClient
from aliyunsdkcore.acs_exception.exceptions import ClientException
from aliyunsdkcore.acs_exception.exceptions import ServerException
from aliyunsdkcore.auth.credentials import AccessKeyCredential
from aliyunsdkcore.auth.credentials import StsTokenCredential
from aliyunsdkiot.request.v20180120.CreateProductRequest import CreateProductRequest
from aliyunsdkiot.request.v20180120.DeleteProductRequest import DeleteProductRequest
from aliyunsdkiot.request.v20180120.QueryProductRequest import QueryProductRequest
from aliyunsdkiot.request.v20180120.UpdateProductTagsRequest import UpdateProductTagsRequest
from aliyunsdkiot.request.v20180120.UpdateProductRequest import UpdateProductRequest
from aliyunsdkiot.request.v20180120.RegisterDeviceRequest import RegisterDeviceRequest
from aliyunsdkiot.request.v20180120.QueryDevicePropertyDataRequest import QueryDevicePropertyDataRequest
from aliyunsdkiot.request.v20180120.QueryDevicePropertyStatusRequest import QueryDevicePropertyStatusRequest

def add():
    global Success
    global ProductKeyvalue
    credentials = AccessKeyCredential('LTAI5tKGCjuMoxfeoQrsq9rX', 'gitAx6KLJssWymM33iI2wwtWONcOlT')
    client = AcsClient(region_id='cn-shanghai', credential=credentials)
    request = CreateProductRequest()
    request.set_accept_format('json')
    request.set_IotInstanceId("iot-06z00eb6vppmo5m")
    request.set_ProductName("demo")
    request.set_NodeType(0)
    response = client.do_action_with_exception(request)
    #print(type(response))
    data2 = response
    data3 = json.loads(data2) #json 转字典
    Success = data3['Success']#状态
    print(Success)
    if Success == True:
        ProductKeyvalue = str(data3['ProductKey'])#产品码
    else:
        print("no ok!")
    #print(data3)
    #print(data3['Success'])
    #ProductKeyvalue = data3['ProductKey']
    #print(data2)

    # python2:  print(response)
    print(str(response, encoding='utf-8'))

def delete():
    credentials = AccessKeyCredential('LTAI5tKGCjuMoxfeoQrsq9rX', 'gitAx6KLJssWymM33iI2wwtWONcOlT')
    # use STS Token
    # credentials = StsTokenCredential('<your-access-key-id>', '<your-access-key-secret>', '<your-sts-token>')
    client = AcsClient(region_id='cn-shanghai', credential=credentials)

    request = DeleteProductRequest()
    request.set_accept_format('json')

    request.set_IotInstanceId("iot-06z00eb6vppmo5m")
    request.set_ProductKey(ProductKeyvalue)

    response = client.do_action_with_exception(request)
    # python2:  print(response)
    print(str(response, encoding='utf-8'))
def Check():
    credentials = AccessKeyCredential('LTAI5tKGCjuMoxfeoQrsq9rX', 'gitAx6KLJssWymM33iI2wwtWONcOlT')
    # use STS Token
    # credentials = StsTokenCredential('<your-access-key-id>', '<your-access-key-secret>', '<your-sts-token>')
    client = AcsClient(region_id='cn-shanghai', credential=credentials)

    request = QueryProductRequest()
    request.set_accept_format('json')
    request.set_IotInstanceId("iot-06z00eb6vppmo5m")
    request.set_ProductKey(ProductKeyvalue)

    response = client.do_action_with_exception(request)
    # python2:  print(response)
    print(str(response, encoding='utf-8'))
def Change():
    credentials = AccessKeyCredential('LTAI5tKGCjuMoxfeoQrsq9rX', 'gitAx6KLJssWymM33iI2wwtWONcOlT')
    # use STS Token
    # credentials = StsTokenCredential('<your-access-key-id>', '<your-access-key-secret>', '<your-sts-token>')
    client = AcsClient(region_id='cn-shanghai', credential=credentials)

    request = UpdateProductRequest()
    request.set_accept_format('json')

    request.set_IotInstanceId("iot-06z00eb6vppmo5m")
    request.set_ProductKey(ProductKeyvalue)
    request.set_ProductName("demo2")

    response = client.do_action_with_exception(request)
    # python2:  print(response)
    print(str(response, encoding='utf-8'))

def RegisterDevice():
    credentials = AccessKeyCredential('LTAI5tKGCjuMoxfeoQrsq9rX', 'gitAx6KLJssWymM33iI2wwtWONcOlT')
    # use STS Token
    # credentials = StsTokenCredential('<your-access-key-id>', '<your-access-key-secret>', '<your-sts-token>')
    client = AcsClient(region_id='cn-shanghai', credential=credentials)

    request = RegisterDeviceRequest()
    request.set_accept_format('json')
    request.set_IotInstanceId("iot-06z00eb6vppmo5m")
    request.set_ProductKey("gr6q69gAqhD")
    request.set_DeviceName("oppoR18")
    response = client.do_action_with_exception(request)
    # python2:  print(response)
    print(str(response, encoding='utf-8'))

def QueryDevicePropertyData():
    # global name
    # global value
    # global devicetime
    # global RequestId
    credentials = AccessKeyCredential('LTAI5tKGCjuMoxfeoQrsq9rX', 'gitAx6KLJssWymM33iI2wwtWONcOlT')
    # use STS Token
    # credentials = StsTokenCredential('<your-access-key-id>', '<your-access-key-secret>', '<your-sts-token>')
    client = AcsClient(region_id='cn-shanghai', credential=credentials)

    request = QueryDevicePropertyStatusRequest()
    request.set_accept_format('json')

    request.set_IotInstanceId("iot-06z00eb6vppmo5m")
    request.set_ProductKey("gr6q69gAqhD")
    request.set_DeviceName("oppoR17")

    response = client.do_action_with_exception(request)
    # python2:  print(response)
    print(str(response, encoding='utf-8'))
    '''
    print('*******************************************')
    mydict = json.loads(response)
    RequestId = mydict["RequestId"]
    i = 0
    for i in range(3):
        list1 = mydict['Data']['List']['PropertyStatusInfo'][i]
        name = list1['Identifier']
        value = list1['Value']
        devicetime = list1['Time']
        print(list1)

    #real_list = list1['Identifier'],list1['Value'],list1['Time']
    #list2 = mydict['Data']['List']
    '''
    return response

def read_dict_only_k(dict_str):
    for k,p in dict_str.items():
        return k

#amqp
def read_dict_only_p(dict_str):
    for k,p in dict_str.items():

        return p



def connect_and_subscribe(conn):
    accessKey = "LTAI5tKGCjuMoxfeoQrsq9rX"
    accessSecret = "gitAx6KLJssWymM33iI2wwtWONcOlT"
    consumerGroupId = "IbNGduhMyHrgPySUhbq9000100"
    # iotInstanceId：实例ID。
    iotInstanceId = "iot-06z00eb6vppmo5m"
    clientId = "123456789"
    # 签名方法：支持hmacmd5，hmacsha1和hmacsha256。
    signMethod = "hmacsha1"
    timestamp = current_time_millis()
    # userName组装方法，请参见AMQP客户端接入说明文档。
    # 若使用二进制传输，则userName需要添加encode=base64参数，服务端会将消息体base64编码后再推送。具体添加方法请参见下一章节“二进制消息体说明”。
    username = clientId + "|authMode=aksign" + ",signMethod=" + signMethod \
               + ",timestamp=" + timestamp + ",authId=" + accessKey \
               + ",iotInstanceId=" + iotInstanceId \
               + ",consumerGroupId=" + consumerGroupId + "|"
    signContent = "authId=" + accessKey + "&timestamp=" + timestamp
    # 计算签名，password组装方法，请参见AMQP客户端接入说明文档。
    password = do_sign(accessSecret.encode("utf-8"), signContent.encode("utf-8"))

    conn.set_listener('', MyListener(conn))
    conn.connect(username, password, wait=True)
    # 清除历史连接检查任务，新建连接检查任务
    schedule.clear('conn-check')
    schedule.every(1).seconds.do(do_check, conn).tag('conn-check')

class MyListener(stomp.ConnectionListener):
    def __init__(self, conn):
        self.conn = conn

    def on_error(self, frame):
        print('received an error "%s"' % frame.body)

    def on_message(self, frame):
        connect = pymysql.Connect(
            host='localhost',
            port=3306,
            user='root',
            passwd='root',
            db='Python',
            charset='utf8'
        )

        print('received a message "%s"' % frame.body)
        print('**********'+frame.body)
        QueryDevicePropertyData()
        List = str(QueryDevicePropertyData(), 'utf-8')
        mydict = json.loads(List)
        i = 0
        cursor = connect.cursor()
        for i in range(4):
            list1 = mydict['Data']['List']['PropertyStatusInfo'][i]
            name = list1['Identifier']
            value = str(str(list1['Value']))[0:str(str(list1['Value'])).index('.',0,str(str(list1['Value'])).__len__())+2]
            devicetime = int(list1['Time'])
            #print(type(devicetime))
            time_local = time.localtime(devicetime / 1000)
            dt = time.strftime("%Y-%m-%d %H:%M:%S", time_local)
            #print(dt)
            real_list = (list1['Identifier'], value, dt)
            #print(type (devicetime))
            sql = "INSERT INTO python (Identifier, Value, devicetime) VALUES ( '%s', '%s', '%s')"
            cursor.execute(sql % real_list)
        demo = frame.body
        #print("**********"+demo)
        mydict1 = json.loads(demo)
        print(mydict1)
        df = mydict1['items']
        deviceName = read_dict_only_k(df)
        devicevalue = str(read_dict_only_p(df)['value'])[0:str(read_dict_only_p(df)['value']).index('.',0,str(read_dict_only_p(df)['value']).__len__())+2]
        # print(devicevalue)
        deviceTime1 = int(read_dict_only_p(df)['time'])
        time_local1 = time.localtime(deviceTime1 / 1000)
        dt1 = time.strftime("%Y-%m-%d %H:%M:%S", time_local1)

        real_list1 = (deviceName,devicevalue,dt1)
        sql1 = "INSERT INTO python1 (deviceName, devicevalue, devicetime1) VALUES ( '%s', '%s', '%s')"
        cursor.execute(sql1 % real_list1)
        cursor.close()
        #print("*****"+deviceName,devicevalue,deviceTime1)
        #print("++++++++++++"+deviceName)


        #productvalue = df.get('value')
        #print(productvalue)
        #deviceName = df.get('deviceName')
        #itemslist = df.get('items')['LightLux']['value']
        #itemslistValue = df.get('items')['LightLux']['value']
        #itemslisttime = df.get('items')['LightLux']['time']
        #dfList = (productKey,deviceName,)

        #print(dfList)

    def on_heartbeat_timeout(self):
        print('on_heartbeat_timeout')

    def on_connected(self, headers):
        print("successfully connected")
        conn.subscribe(destination='/topic/#', id=1, ack='auto')
        print("successfully subscribe")

    def on_disconnected(self):
        print('disconnected')
        connect_and_subscribe(self.conn)
def current_time_millis():
    return str(int(round(time.time() * 1000)))

def do_sign(secret, sign_content):
    m = hmac.new(secret, sign_content, digestmod=hashlib.sha1)
    return base64.b64encode(m.digest()).decode("utf-8")
# 检查连接，如果未连接则重新建连
def do_check(conn):
    print('check connection, is_connected: %s', conn.is_connected())
    if (not conn.is_connected()):
        try:
            connect_and_subscribe(conn)
        except Exception as e:
            print('disconnected,', e)
# 定时任务方法，检查连接状态
def connection_check_timer():
    while 1:
        schedule.run_pending()
        time.sleep(1000)
#  接入域名，请参见AMQP客户端接入说明文档。这里直接填入域名，不需要带amqps://前缀
conn = stomp.Connection([('1453410822279370.iot-amqp.cn-shanghai.aliyuncs.com', 61614)])
conn.set_ssl(for_hosts=[('1453410822279370.iot-amqp.cn-shanghai.aliyuncs.com', 61614)], ssl_version=ssl.PROTOCOL_TLS)
try:
    connect_and_subscribe(conn)
except Exception as e:
    print('connecting failed')
    raise e
# 异步线程运行定时任务，检查连接状态
thread = threading.Thread(target=connection_check_timer)
thread.start()

if __name__ == '__main__':
    add()
    if Success == True:#是否创造成功
        print(ProductKeyvalue)
        Change()
        Check()
        RegisterDevice()
        delete()