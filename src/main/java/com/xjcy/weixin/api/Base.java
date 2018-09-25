package com.xjcy.weixin.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.xjcy.util.MD5;
import com.xjcy.util.ObjectUtils;
import com.xjcy.util.http.WebClient;

public abstract class Base {
	private static final Logger logger = Logger.getLogger(Base.class);

	protected static String getJSON(String url) {
		String json = WebClient.downloadString(url);
		logger.debug("Get access token " + json);
		return json;
	}

	protected static byte[] getBytes(String str) {
		return ObjectUtils.string2Byte(str, "utf-8");
	}

	protected static String getString(byte[] data) {
		return ObjectUtils.byte2String(data, "utf-8");
	}

	protected static byte[] uploadData(String url, byte[] postData) {
		byte[] data = WebClient.uploadData(url, postData);
		if (data != null && data.length < 200) {
			logger.debug("uploadData " + getString(data));
		}
		return data;
	}

	protected static String uploadData(String url, byte[] postData, SSLSocketFactory ssl) {
		String json = WebClient.uploadData(url, postData, ssl);
		logger.debug("uploadData " + json);
		return json;
	}

	protected static String uploadString(String url, String postStr) {
		String json = WebClient.uploadString(url, postStr);
		logger.debug("uploadString " + json);
		return json;
	}

	protected static String uploadMultipartData(String uploadURL, String qrcodeUrl) {
		try {
			// 1.建立连接
			URL url = new URL(uploadURL);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection(); // 打开链接

			// 1.1输入输出设置
			httpUrlConn.setDoInput(true);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setUseCaches(false); // post方式不能使用缓存
			// 1.3设置边界
			String BOUNDARY = "----------" + System.currentTimeMillis();
			httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			// 请求正文信息
			// 第一部分：
			// 2.将文件头输出到微信服务器
			StringBuilder sb = new StringBuilder();
			sb.append("--"); // 必须多两道线
			sb.append(BOUNDARY);
			sb.append("\r\n");
			sb.append("Content-Disposition: form-data;name=\"media\";filename=\"qrcode.jpg\"\r\n");
			sb.append("Content-Type:image/jpeg\r\n\r\n");
			// 获得输出流
			OutputStream outputStream = httpUrlConn.getOutputStream();
			// 将表头写入输出流中：输出表头
			outputStream.write(getBytes(sb.toString()));

			// 3.将文件以流的方式写入到微信服务器中
			InputStream in = new URL(qrcodeUrl).openStream();
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				outputStream.write(bufferOut, 0, bytes);
			}
			in.close();
			// 4.将结尾部分输出到微信服务器
			byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
			outputStream.write(foot);
			outputStream.flush();
			outputStream.close();

			// 5.将微信服务器返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			String str = ObjectUtils.input2String(inputStream);
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			return str;
		} catch (IOException e) {
			logger.error("Upload faild", e);
			return null;
		}
	}

	protected synchronized String getRandamStr() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	protected synchronized long getTimestamp() {
		return System.currentTimeMillis() / 1000;
	}

	protected static String getSign(Map<String, Object> map, String apiKey) {
		StringBuffer sb = new StringBuffer();
		SortedMap<String, Object> sortmap = new TreeMap<>(map);
		Object obj;
		for (String key : sortmap.keySet()) {
			obj = sortmap.get(key);
			if (obj == null || "".equals(obj.toString()))
				continue;
			sb.append(key);
			sb.append("=");
			sb.append(obj);
			sb.append("&");
		}
		sb.append("key=");
		sb.append(apiKey);
		return MD5.encodeByMD5(sb.toString()).toUpperCase();
	}

	protected static String getValue(Map<String, Object> mapreturn, String key) {
		if (mapreturn == null)
			return null;
		if (mapreturn.containsKey(key)) {
			return mapreturn.get(key).toString();
		}
		return null;
	}
}
