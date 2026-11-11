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


class createOutline():

    def __init__(self, APPId, APISecret, Text):
        self.APPid = APPId
        self.APISecret = APISecret
        self.text = Text
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

    def create_task(self):
        url = 'https://zwapi.xfyun.cn/api/aippt/createOutline'
        timestamp = int(time.time())
        signature = self.get_signature(timestamp)
        body = self.getbody(self.text)

        headers = {
            "appId": self.APPid,
            "timestamp": str(timestamp),
            "signature": signature,
            "Content-Type": "application/json; charset=utf-8"
        }
        self.header = headers
        response = requests.request("POST", url=url, data=json.dumps(body), headers=headers).text
        resp = json.loads(response)
        print(resp)


    def getbody(self, text):
        body = {
            "query": text,
            "is_figure":True
        }
        return body

if __name__ == '__main__':
    APPId = require_env("XUNFEI_APP_ID")
    APISecret = require_env("XUNFEI_API_SECRET")

    Text = "集团客户部2023年工作总结"
    demo = createOutline(APPId, APISecret, Text)
    demo.create_task()
