#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import pymysql
from flask import Flask, render_template,url_for,redirect,request
from aliyun import *
from check_login import *
from regist_login import *

app = Flask(__name__)

@app.route('/')
def index():
    return redirect( url_for('user_login') )

@app.route('/user_login',methods=['GET','POST'])
def user_login():
    if request.method=='POST':  # 注册发送的请求为POST请求
        username = request.form['username']
        password = request.form['password']
        if is_null(username,password):
            login_massage = "温馨提示：账号和密码是必填"
            return render_template('login.html', message=login_massage)
        elif is_existed(username, password):
            return render_template('index.html', username=username)
        elif exist_user(username):
            login_massage = "提示：密码错误，请输入正确密码"
            return render_template('login.html', message=login_massage)
        else:
            login_massage = "不存在该用户"
            return render_template('login.html', message=login_massage)
    return render_template('login.html')

@app.route("/regiser",methods=["GET", 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        if is_null(username,password):
            login_massage = "温馨提示：账号和密码是必填"
            return render_template('register.html', message=login_massage)
        elif exist_user(username):
            return redirect(url_for('user_login'))
        else:
            add_user(request.form['username'], request.form['password'] )
            return render_template('index.html', username=username)
    return render_template('register.html')

#查询实时数据库接口
@app.route('/iot',methods=['GET'])
def iot():
    conn = pymysql.connect(host='127.0.0.1', user='root', password='root', port=3306,
                           db='python')
    cur = conn.cursor()
    sql = "SELECT devicevalue,deviceTime1 FROM `python1`"
    cur.execute(sql)
    u = cur.fetchall()
    conn.close()
    return render_template('display.html', u=u)
#查询物模型

@app.route('/iotthing',methods=['GET'])
def iotthing():
    conn = pymysql.connect(host='127.0.0.1', user='root', password='root', port=3306,
                           db='python')
    cur = conn.cursor()
    sql = "select DISTINCT * from python where Identifier='Pressure' and  devicetime=(select max(devicetime) from python where Identifier='Pressure')or Identifier='LightLux' and  devicetime=(select max(devicetime) from python where Identifier='LightLux')or Identifier='Humidity' and  devicetime=(select max(devicetime) from python where Identifier='Humidity')or Identifier='Temperature' and  devicetime=(select max(devicetime) from python where Identifier='Temperature');"
    cur.execute(sql)
    v = cur.fetchall()
    json.dumps(v)
    conn.close()
    return render_template('show.html',v=v)

if __name__ == "__main__":
    app.run(host='127.0.0.1', debug=False)
    QueryDevicePropertyData()

