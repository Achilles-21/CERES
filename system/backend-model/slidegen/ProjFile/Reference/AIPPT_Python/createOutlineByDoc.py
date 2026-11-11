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
import random

import requests

from requests_toolbelt.multipart.encoder import MultipartEncoder


class createOutlineByDoc():

    def __init__(self, APPId, APISecret):
        self.APPid = APPId
        self.APISecret = APISecret
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

        timestamp = int(time.time())
        signature = self.get_signature(timestamp)

        headers = {
            "appId": self.APPid,
            "timestamp": str(timestamp),
            "signature": signature,
            "Content-Type": "multipart/form-data; charset=utf-8"
        }

        body = {
            "file_url": require_env("XUNFEI_REFERENCE_FILE_URL"),
            "file_name": "作文.txt",
        }
        data = MultipartEncoder(
            fields=body,
            boundary='------------------' + str(random.randint(1e28, 1e29 - 1))
        )



        headers['Content-Type'] = data.content_type
        url = 'https://zwapi.xfyun.cn/api/aippt/createOutlineByDoc'
        self.header = headers
        response =requests.post(url=url, data=data, headers=headers).text
        resp = json.loads(response)
        print(resp)


if __name__ == '__main__':
    APPId = require_env("XUNFEI_APP_ID")
    APISecret = require_env("XUNFEI_API_SECRET")

    demo = createOutlineByDoc(APPId, APISecret)
    result = demo.create_task()
