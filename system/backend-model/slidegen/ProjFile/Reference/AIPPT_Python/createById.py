import os

def require_env(name):
    value = os.environ.get(name, "")
    if not value:
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value

# -*- coding:utf-8 -*-
import hashlib
import hmac
import base64
import json
import time

import requests



class createById():

    def __init__(self, APPId, APISecret, Text, sid, outline):
        self.APPid = APPId
        self.APISecret = APISecret
        self.text = Text
        self.sid = sid
        self.outline = outline
        self.header = {}

    def get_signature(self, ts):
        try:
            # Vendor authentication signs MD5(app_id + timestamp) with HMAC-SHA1.
            auth = self.md5(self.APPid + str(ts))
            return self.hmac_sha1_encrypt(auth, self.APISecret)
        except Exception as e:
            print(e)
            return None

    def hmac_sha1_encrypt(self, encrypt_text, encrypt_key):
        return base64.b64encode(
            hmac.new(encrypt_key.encode('utf-8'), encrypt_text.encode('utf-8'), hashlib.sha1).digest()).decode('utf-8')

    def md5(self, text):
        return hashlib.md5(text.encode('utf-8')).hexdigest()



    def createById(self):

        timestamp = int(time.time())
        signature = self.get_signature(timestamp)

        headers = {
            "appId": self.APPid,
            "timestamp": str(timestamp),
            "signature": signature,
            "Content-Type": "application/json; charset=utf-8"
        }
        self.header = headers

        body = {
            "sid": self.sid
        }
        url = 'https://zwapi.xfyun.cn/api/aippt/createBySid'
        response = requests.request("POST", url=url, json=body, headers=headers).text
        resp = json.loads(response)
        print(resp)
        if 0 == resp['code']:
            print('创建PPT任务成功')
            return resp['data']['sid']
        else:
            print('创建PPT任务失败')
            return None

    def get_process(self, sid):
        print("sid:" + sid)
        print(self.header)
        if (None != sid):
            response = requests.request("GET", url=f"https://zwapi.xfyun.cn/api/aippt/progress?sid={sid}",
                                        headers=self.header).text
            print(response)
            return response
        else:
            return None

    def get_result(self):

        task_id = self.createById()
        while (True):
            time.sleep(1)
            response = self.get_process(task_id)
            resp = json.loads(response)
            process = resp['data']['process']
            if (process == 100):
                PPTurl = resp['data']['pptUrl']
                break
        return PPTurl




if __name__ == '__main__':
    APPId = require_env("XUNFEI_APP_ID")
    APISecret = require_env("XUNFEI_API_SECRET")

    Text = "集团客户部2023年工作总结"
    sid = "xxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    outline =" "
    demo = createById(APPId, APISecret, Text, sid, outline)
    result = demo.get_result()
    print("生成的PPT请从此地址获取：\n" + result)
