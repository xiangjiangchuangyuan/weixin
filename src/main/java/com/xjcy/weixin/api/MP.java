package com.xjcy.weixin.api;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.xjcy.util.JSONUtils;
import com.xjcy.util.StringUtils;
import com.xjcy.util.XMLUtils;
import com.xjcy.weixin.event.MPEvent;

public class MP extends Base {

	private String appId;
	private String appSecret;
	private String _token;
	private long _tokenTime;
	private static final long TIME = 7100 * 1000;

	private static final String POST_SCENE_ID = "{\"action_name\": \"QR_LIMIT_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %s}}}";
	private static final String POST_SCENE_ID_EXPIRE = "{\"expire_seconds\": %s, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %s}}}";
	private static final String POST_SCENE_STR = "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"%s\"}}}";
	private static final String POST_SCENE_STR_EXPIRE = "{\"expire_seconds\": %s, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"%s\"}}}";

	private static final String URL_OPENID = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private static final String URL_SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
	private static final String URL_CREATEQRCODE = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
	private static final String URL_MEDIA_UPLOAD_IMAGE = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=image";

	public MP(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public String getOpenId(String code) {
		String json = getJSON(String.format(URL_OPENID, appId, appSecret, code));
		if (!json.contains("\"errcode\""))
			return JSONUtils.getString(json, "openid");
		return null;
	}

	public synchronized String getAccessToken() {
		if (StringUtils.isEmpty(_token) || (System.currentTimeMillis() - _tokenTime) > TIME) {
			String json = getJSON(String.format(URL_TOKEN, appId, appSecret));
			if (!json.contains("\"errcode\"")) {
				_token = JSONUtils.getString(json, "access_token");
				_tokenTime = System.currentTimeMillis();
			}
		}
		return _token;
	}

	public String createQrcode(Integer scene, int expireSeconds) {
		String postStr;
		if (expireSeconds > 0)
			postStr = String.format(POST_SCENE_ID_EXPIRE, expireSeconds, scene);
		else
			postStr = String.format(POST_SCENE_ID, scene);
		String json = uploadString(String.format(URL_CREATEQRCODE, getAccessToken()), postStr);
		if (!json.contains("\"errcode\""))
			return JSONUtils.getString(json, "ticket");
		return null;
	}

	public String createQrcode(String scene, int expireSeconds) {
		String postStr;
		if (expireSeconds > 0)
			postStr = String.format(POST_SCENE_STR_EXPIRE, expireSeconds, scene);
		else
			postStr = String.format(POST_SCENE_STR, scene);
		String json = uploadString(URL_CREATEQRCODE, postStr);
		if (!json.contains("\"errcode\""))
			return JSONUtils.getString(json, "ticket");
		return null;
	}

	public String uploadImage(String url) {
		String json = uploadMultipartData(String.format(URL_MEDIA_UPLOAD_IMAGE, getAccessToken()), url);
		System.out.println(json);
		if (json != null && !json.contains("\"errcode\""))
			return JSONUtils.getString(json, "media_id");
		return null;
	}

	public boolean sendTemplate(String content) {
		String json = uploadString(String.format(URL_SEND_TEMPLATE, getAccessToken()), content);
		if (json != null)
			return JSONUtils.getInteger(json, "errcode") == 0;
		return false;
	}

	public void auth(MPEvent mpEvent) throws IOException {
		String authXml = mpEvent.doXml();
		Map<String, Object> map = XMLUtils.doXMLParse(authXml);
		String toUserName = map.get("ToUserName") + "";
		final String fromUserName = map.get("FromUserName") + "";
		String msgType = map.get("MsgType") + "";
		Map<String, Object> message = new HashMap<>();
		message.put("FromUserName", toUserName);
		message.put("ToUserName", fromUserName);
		// 对文本消息进行处理
		if ("text".equals(msgType)) {
			mpEvent.doText(message);
		} else if ("event".equals(msgType)) {
			String event = map.get("Event") + "";
			String eventKey = map.get("EventKey") + "";
			if (event.equals("subscribe")) {
				mpEvent.doSubscribe(eventKey, message);
			} else if (event.equals("unsubscribe")) {
				mpEvent.doUnSubscribe(message);
			} else if (event.equals("SCAN")) {
				mpEvent.doScan(eventKey, message);
			} else if (event.equals("VIEW")) {
				mpEvent.doView(message);
			}
		}
		message.put("CreateTime", new Date().getTime());
		mpEvent.doResponse(message);
	}

}
