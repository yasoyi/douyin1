#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
抖音视频音频下载脚本
"""
import argparse
import json
import os
import re
import sys
import time
import urllib.request

import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.common.exceptions import WebDriverException
from selenium.webdriver.edge.options import Options
from selenium.webdriver.edge.service import Service

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

def download_audio_from_url(url, output_dir="audio", filename=None, debug=True):
    """
    使用Selenium从抖音视频URL下载音频
    :param url: 视频页面URL
    :param output_dir: 音频输出目录
    :param filename: 音频文件名（不含扩展名）
    :param debug: 是否输出调试信息
    :return: 音频文件路径
    """
    # 首先检查网络连接
    if debug:
        print("检查网络连接...")
    if not check_network_connectivity(debug):
        raise Exception("无法连接到网络，请检查网络连接")
    
    # 确保输出目录存在
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
        if debug:
            print(f"创建输出目录: {output_dir}")
    
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
        
        # 等待页面加载完成，增加等待时间
        time.sleep(5)  # 等待JavaScript渲染完成
        
        # 获取页面源码
        page_source = driver.page_source
        
        # 尝试从页面源码中提取视频/音频链接
        audio_urls = []
        
        # 方法1: 查找包含音频的JSON数据
        # 寻找playAddr字段，可能是音频或视频地址
        play_addr_matches = re.findall(r'"playAddr":"([^"]+)"', page_source)
        for match in play_addr_matches:
            # 解码Unicode转义序列
            decoded_url = match.encode().decode('unicode_escape')
            if decoded_url.startswith('http'):
                audio_urls.append(decoded_url)
                
        # 方法2: 查找其他可能的音频链接
        url_matches = re.findall(r'https?://[^\s"\'<>]+?\.(?:mp4|mp3|aac|m4a|wav)', page_source)
        audio_urls.extend(url_matches)
        
        # 方法3: 在script标签中查找JSON数据
        script_matches = re.findall(r'<script[^>]*>(.*?)</script>', page_source, re.DOTALL)
        for script_content in script_matches:
            # 在script内容中查找playAddr
            inner_matches = re.findall(r'"playAddr":"([^"]+)"', script_content)
            for match in inner_matches:
                decoded_url = match.encode().decode('unicode_escape')
                if decoded_url.startswith('http'):
                    if decoded_url not in audio_urls:  # 避免重复
                        audio_urls.append(decoded_url)
        
        if not audio_urls:
            raise Exception("未能在页面中找到音频链接")
            
        # 过滤掉可能的视频链接，优先选择音频链接
        audio_candidates = []
        for audio_url in audio_urls:
            # 优先选择明确是音频的链接
            if any(ext in audio_url for ext in ['.mp3', '.aac', '.m4a', '.wav']):
                audio_candidates.insert(0, audio_url)  # 插入到开头
            else:
                audio_candidates.append(audio_url)
        
        # 选择第一个有效的音频链接
        audio_url = audio_candidates[0]
        if debug:
            print(f"找到音频链接: {audio_url}")
        
        # 获取视频标题作为默认文件名
        if not filename:
            title_match = re.search(r'<title[^>]*>(.*?)</title>', page_source, re.IGNORECASE)
            if title_match:
                filename = re.sub(r'[<>:"/\\|?*\x00-\x1F]', '_', title_match.group(1))[:50]
            else:
                filename = "douyin_audio_" + str(int(time.time()))
        else:
            # 如果提供了文件名，也添加时间戳确保唯一性
            filename = filename + "_" + str(int(time.time()))
                
        # 确定文件扩展名
        if audio_url.endswith('.mp3'):
            extension = '.mp3'
        elif audio_url.endswith('.aac'):
            extension = '.aac'
        elif audio_url.endswith('.m4a'):
            extension = '.m4a'
        elif audio_url.endswith('.wav'):
            extension = '.wav'
        elif audio_url.endswith('.mp4'):
            extension = '.mp4'  # 视频格式，但包含音频
        else:
            extension = '.mp3'  # 默认
            
        # 完整文件路径
        # 总是添加时间戳，确保文件名唯一性
        timestamp = str(int(time.time()))
        name, ext = os.path.splitext(filename + extension)
        file_path = os.path.join(output_dir, f"{name}_{timestamp}{ext}")
        
        # 检查文件是否已存在，如果存在则添加计数器
        counter = 1
        while os.path.exists(file_path):
            file_path = os.path.join(output_dir, f"{name}_{timestamp}_{counter}{ext}")
            counter += 1
        
        # 下载音频文件
        if debug:
            print(f"正在下载音频到: {file_path}")
            
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'Referer': 'https://www.douyin.com/'
        }
        
        response = requests.get(audio_url, headers=headers, stream=True)
        response.raise_for_status()
        
        with open(file_path, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
                
        if debug:
            print(f"音频下载完成: {file_path}")
            
        return file_path
        
    except Exception as e:
        if debug:
            print(f"下载音频时出错: {e}")
            # 打印部分页面源码用于调试
            try:
                page_source = driver.page_source
                print(f"页面标题: {driver.title}")
                print(f"页面URL: {driver.current_url}")
                if len(page_source) < 5000:  # 避免打印过长的内容
                    print(f"部分页面源码: {page_source[:1000]}")
            except Exception as e2:
                print(f"获取页面信息失败: {e2}")
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

def find_video_url(url, works_count_diff):
    """
    根据URL和作品数量差查找视频URL
    :param url: 抖音用户或作品页面URL
    :param works_count_diff: 作品数量差
    :return: 视频URL列表
    """
    # 首先检查网络连接
    if not check_network_connectivity():
        raise Exception("无法连接到网络，请检查网络连接")
    
    # 使用Selenium获取页面内容
    try:
        # 使用本地EdgeDriver
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
            
        service = Service(edge_driver_path)
        
        # 启动Edge浏览器
        driver = webdriver.Edge(service=service, options=edge_options)
        
        # 打开页面
        driver.get(url)
        
        # 等待页面加载完成
        time.sleep(5)
        
        # 获取页面源码
        page_source = driver.page_source
        
        # 解析HTML，提取视频链接
        soup = BeautifulSoup(page_source, 'html.parser')
        video_links = []
        
        # 查找所有video标签的链接
        video_tags = soup.find_all('a', href=re.compile(r'^/video/'))
        for tag in video_tags:
            href = tag['href']
            full_url = f"https://www.douyin.com{href}"
            video_links.append(full_url)
        
        # 根据作品数量差获取指定数量的视频
        video_urls = video_links[:works_count_diff]
        
        return video_urls
    
    except Exception as e:
        print(f"查找视频URL时出错: {e}")
        return []
    finally:
        # 关闭浏览器
        try:
            driver.quit()
        except Exception as e:
            print(f"关闭浏览器时出错: {e}")

def main():
    parser = argparse.ArgumentParser(description='从抖音用户主页下载最新作品的音频')
    parser.add_argument('-u', '--url', required=True, help='抖音用户主页URL')
    parser.add_argument('-o', '--output', default='audio', help='音频输出目录 (默认: audio)')
    parser.add_argument('-n', '--name', help='音频文件名（不含扩展名）')
    parser.add_argument('-j', '--json', action='store_true', help='以JSON格式输出结果')
    parser.add_argument('-c', '--count', type=int, default=1, help='要下载的作品数量 (默认: 1)')
    
    args = parser.parse_args()

    try:
        # 当使用-j参数时，不输出调试信息
        debug = not args.json
        
        # 先查找视频URL
        video_urls = find_video_url(args.url, args.count)
        
        # 如果没有找到视频，直接返回错误
        if not video_urls:
            raise Exception("未找到任何视频")
        
        # 输出找到的视频数量
        if debug:
            print(f"找到 {len(video_urls)} 个视频")
            for i, url in enumerate(video_urls):
                print(f"视频 {i+1}: {url}")
        
        # 下载每个视频的音频
        results = []
        for i, url in enumerate(video_urls):
            try:
                file_path = download_audio_from_url(url, args.output, args.name, debug)
                message = f"音频已保存到: {file_path}"
                
                if args.json:
                    result = {
                        "success": True,
                        "file_path": file_path,
                        "message": message,
                        "video_url": url
                    }
                    results.append(result)
                else:
                    print(message)
            except Exception as e:
                if args.json:
                    result = {
                        "success": False,
                        "error": str(e),
                        "video_url": url
                    }
                    results.append(result)
                else:
                    print(f"下载视频 {url} 的音频时出错: {e}")
        
        # 如果是JSON输出，输出所有结果
        if args.json:
            print(json.dumps(results, ensure_ascii=False))
    
    except Exception as e:
        if args.json:
            # 以JSON格式输出错误
            result = {
                "success": False,
                "error": str(e)
            }
            print(json.dumps(result, ensure_ascii=False))
        else:
            print(f"执行时出错: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()