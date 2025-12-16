# 抖音用户作品数量获取工具

这是一个简单的 Python 脚本，用于通过用户 ID 获取抖音用户的作品数量。

## 功能

- 根据抖音用户 主页url  获取该用户的作品数量
- 简化了原始脚本，仅保留核心功能
- 从抖音视频URL下载音频

## 安装依赖

```bash
pip install -r requirements.txt
```

## 使用方法

### 基本用法
```bash
python get_works_count_from_url.py -u <抖音用户页面URL>
```

示例：
```bash
python get_works_count_from_url.py -u "https://www.douyin.com/user/示例用户页面URL"
```

### JSON输出模式
使用 `-j` 或 `--json` 参数以JSON格式输出结果，此模式下不会输出调试信息：

```bash
python get_works_count_from_url.py -u <抖音用户页面URL> -j
```

示例：
```bash
python get_works_count_from_url.py -u "https://www.douyin.com/user/示例用户页面URL" -j
```

## 下载抖音视频音频

### 基本用法
```bash
python get_audio_from_url.py -u <抖音视频页面URL>
```

示例：
```bash
python get_audio_from_url.py -u "https://www.douyin.com/video/示例视频页面URL"
```

### 指定输出目录和文件名
```bash
python get_audio_from_url.py -u <抖音视频页面URL> -o <输出目录> -n <文件名>
```

示例：
```bash
python get_audio_from_url.py -u "https://www.douyin.com/video/示例视频页面URL" -o music -n my_audio
```

### JSON输出模式
使用 `-j` 或 `--json` 参数以JSON格式输出结果，此模式下不会输出调试信息：

```bash
python get_audio_from_url.py -u <抖音视频页面URL> -j
```

## 输出示例

### 正常模式输出
```
检查网络连接...
网络连接正常
使用本地EdgeDriver...
找到本地EdgeDriver: C:\Users\86147\Desktop\douyin1\douyin_tools\msedgedriver\msedgedriver.exe
EdgeDriver准备就绪
正在启动Edge浏览器...
Edge浏览器启动成功
正在访问: https://www.douyin.com/user/示例用户页面URL
页面加载完成
正在查找作品数量元素...
找到作品数量: 15
浏览器已关闭
用户页面 https://www.douyin.com/user/示例用户页面URL 的作品数量: 15
```

### JSON模式输出
```json
{"success": true, "number": "15", "message": "用户页面 https://www.douyin.com/user/示例用户页面URL 的作品数量: 15"}
```