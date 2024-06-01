#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import requests

def main():
    # 获取命令行参数（排除脚本名称本身）
    args = sys.argv[1:]

    if not args:
        print("请提供至少一个链接。")
        return

    print("您提供的链接内容如下：")
    for arg in args:
        get(arg)

def get(url):
    r = requests.get(url)
    print(r.text)

if __name__ == "__main__":
    main()