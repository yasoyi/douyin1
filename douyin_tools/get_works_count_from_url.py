#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
抖音用户作品数量获取脚本 - 使用Selenium从URL获取
"""
import time
import sys
import argparse
import os
import json
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.edge.options import Options
from selenium.webdriver.edge.service import Service
from selenium.common.exceptions import WebDriverException
import urllib.request
import socket

# 配置Edge选项
edge_options = Options()
edge_options.add_argument('--headless')  # 无头模式，不显示浏览器窗口
edge_options.add_argument('--no-sandbox')
edge_options.add_argument('--disable-dev-shm-usage')
edge_options.add_argument('--disable-gpu')
edge_options.add_argument('--window-size=1920,1080')
# 添加网络相关选项
edge_options.add_argument('--disable-web-security')
edge_options.add_argument('--allow-running-insecure-content')
edge_options.add_argument('--ignore-certificate-errors')
edge_options.add_argument('--ignore-ssl-errors')
edge_options.add_argument('--disable-extensions')
edge_options.add_argument('--disable-plugins')

def check_network_connectivity(debug=True):
    """检查网络连接"""
    try:
        urllib.request.urlopen('https://www.douyin.com', timeout=10)
        if debug:
            print("网络连接正常")
        return True
    except Exception as e:
        if debug:
            print(f"网络连接检查失败: {e}")
        return False

def get_user_works_count_from_url(url, debug=True):
    """
    使用Selenium从URL获取用户作品数量
    :param url: 用户页面URL
    :param debug: 是否输出调试信息
    :return: 作品数量
    """
    # 首先检查网络连接
    if debug:
        print("检查网络连接...")
    if not check_network_connectivity(debug):
        raise Exception("无法连接到网络，请检查网络连接")
    
    try:
        # 使用本地EdgeDriver
        if debug:
            print("使用本地EdgeDriver...")
        # 检查两个可能的EdgeDriver路径
        edge_driver_paths = [
            os.path.join(os.path.dirname(__file__), "msedgedriver", "msedgedriver.exe"),
            os.path.join(os.path.dirname(__file__), "msedgedriver-win64", "msedgedriver.exe")
        ]
        
        edge_driver_path = None
        for path in edge_driver_paths:
            if os.path.exists(path):
                edge_driver_path = path
                break
                
        if edge_driver_path is None:
            raise Exception("未找到EdgeDriver，请确保msedgedriver.exe存在于msedgedriver或msedgedriver-win64目录中")
            
        if debug:
            print(f"找到本地EdgeDriver: {edge_driver_path}")
        service = Service(edge_driver_path)
        if debug:
            print("EdgeDriver准备就绪")
    except Exception as e:
        if debug:
            print(f"EdgeDriver准备失败: {e}")
        raise
    
    try:
        # 启动Edge浏览器
        if debug:
            print("正在启动Edge浏览器...")
        driver = webdriver.Edge(service=service, options=edge_options)
        if debug:
            print("Edge浏览器启动成功")
    except WebDriverException as e:
        if debug:
            print(f"浏览器启动失败: {e}")
        raise
    except Exception as e:
        if debug:
            print(f"创建WebDriver时发生未知错误: {e}")
        raise

    try:
        # 打开页面
        if debug:
            print(f"正在访问: {url}")
        driver.get(url)
        if debug:
            print("页面加载完成")
        
        # 等待页面加载完成
        wait = WebDriverWait(driver, 15)  # 增加等待时间
        
        # 等待作品数量元素出现
        if debug:
            print("正在查找作品数量元素...")
        works_element = wait.until(
            EC.presence_of_element_located((By.XPATH, "//span[contains(text(), '作品')]//following-sibling::span[@data-e2e='user-tab-count']"))
        )
        
        # 获取作品数量
        works_count = works_element.text
        if debug:
            print(f"找到作品数量: {works_count}")
        return works_count
    except Exception as e:
        # 尝试其他可能的选择器
        try:
            if debug:
                print("尝试备用选择器...")
            works_element = wait.until(
                EC.presence_of_element_located((By.XPATH, "//span[@data-e2e='user-tab-count']"))
            )
            works_count = works_element.text
            if debug:
                print(f"通过备用选择器找到作品数量: {works_count}")
            return works_count
        except Exception as e2:
            if debug:
                print(f"获取作品数量时出错: {e}")
                print(f"备用选择器也失败: {e2}")
                # 打印页面源码用于调试
                try:
                    page_source = driver.page_source
                    print(f"页面标题: {driver.title}")
                    print(f"页面URL: {driver.current_url}")
                    if len(page_source) < 10000:  # 避免打印过长的内容
                        print(f"部分页面源码: {page_source[:2000]}")
                    else:
                        print(f"页面源码长度: {len(page_source)} 字符")
                except Exception as e3:
                    print(f"获取页面信息失败: {e3}")
            raise
    finally:
        # 关闭浏览器
        try:
            driver.quit()
            if debug:
                print("浏览器已关闭")
        except Exception as e:
            if debug:
                print(f"关闭浏览器时出错: {e}")


def main():
    parser = argparse.ArgumentParser(description='使用Selenium从URL获取抖音用户作品数量')
    parser.add_argument('-u', '--url', required=True, help='抖音用户页面URL, 例如: https://www.douyin.com/user/MS4wLjABAAAAhdP7RzxbZIlM-8R1EUX4QGb5brkOPJEsNSfE6mnKQlg?from_tab_name=main&vid=7571812136054934963')
    parser.add_argument('-j', '--json', action='store_true', help='以JSON格式输出结果')
    
    args = parser.parse_args()

    try:
        # 当使用-j参数时，不输出调试信息
        debug = not args.json
        works_count = get_user_works_count_from_url(args.url, debug)
        message = f"用户页面 {args.url} 的作品数量: {works_count}"
        
        if args.json:
            # 以JSON格式输出
            result = {
                "success": True,
                "number": works_count,
                "message": message,
                "url": args.url
            }
            print(json.dumps(result, ensure_ascii=False))
        else:
            print(message)
    except Exception as e:
        if args.json:
            # 以JSON格式输出错误
            result = {
                "success": False,
                "error": str(e),
                "url": args.url
            }
            print(json.dumps(result, ensure_ascii=False))
        else:
            print(f"获取作品数量时出错: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()