package com.xjcy.weixin.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.xjcy.util.JSONUtils;
import com.xjcy.util.MD5;
import com.xjcy.util.ObjectUtils;
import com.xjcy.util.StringUtils;
import com.xjcy.util.XMLUtils;
import com.xjcy.util.http.WebClient;
import com.xjcy.weixin.bean.PayResult;
import com.xjcy.weixin.bean.TransferVo;
import com.xjcy.weixin.bean.UnifiedOrder;

public class MiniApp extends Base {

	private String _token2;
	private long _tokenTime2;
	private String appId;
	private String appSecret;
	private String mchId;
	private String apiKey;
	private String notifyUrl;
	private static final long TIME = 7100 * 1000;

	private static final String POST_WXACODE_STR = "{\"scene\": \"%s\", \"page\": \"%s\", \"width\": 430}";
	private static final String POST_SEND_IMAGE = "{\"touser\":\"%s\",\"msgtype\":\"image\",\"image\":{\"media_id\":\"%s\"}}";

	private static final String URL_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private static final String URL_WXACODE = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s";
	private static final String URL_CUSTOM_SEND = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s";
	private static final String URL_SEND_TEMPLETE = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=%s";
	private static final String URL_TRANSFERS = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	private static final String URL_CREATE_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	private static final String URL_MEDIA_UPLOAD_IMAGE = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=image";

	public MiniApp(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}

	public void setPayConfig(String mchId, String apiKey, String notifyUrl) {
		this.mchId = mchId;
		this.apiKey = apiKey;
		this.notifyUrl = notifyUrl;
	}

	public synchronized String getAccessToken() {
		if (StringUtils.isEmpty(_token2) || (System.currentTimeMillis() - _tokenTime2) > TIME) {
			String json = getJSON(String.format(URL_TOKEN, appId, appSecret));
			if (!json.contains("errcode")) {
				_token2 = JSONUtils.getString(json, "access_token");
				_tokenTime2 = System.currentTimeMillis();
			}
		}
		return _token2;
	}

	public byte[] createWxacode(String scene, String path) {
		byte[] postData = getBytes(String.format(POST_WXACODE_STR, scene, path));
		return uploadData(String.format(URL_WXACODE, getAccessToken()), postData);
	}

	public String uploadImage(String url) {
		String json = uploadMultipartData(String.format(URL_MEDIA_UPLOAD_IMAGE, getAccessToken()), url);
		System.out.println(json);
		if (json != null && !json.contains("\"errcode\""))
			return JSONUtils.getString(json, "media_id");
		return null;
	}

	public void sendImage(String openId, String mediaId) {
		if (openId != null && mediaId != null) {
			byte[] postData = getBytes(String.format(POST_SEND_IMAGE, openId, mediaId));
			uploadData(String.format(URL_CUSTOM_SEND, getAccessToken()), postData);
		}
	}

	public boolean sendTemplate(String postStr) {
		String json = uploadString(String.format(URL_SEND_TEMPLETE, getAccessToken()), postStr);
		return json != null && !json.contains("\"errcode\"");
	}

	public boolean transferCash(TransferVo tf) {
		Map<String, Object> map = new HashMap<>();
		try {
			map.put("mch_appid", this.appId);// 应用ID
			map.put("mchid", mchId);// 商户号
			map.put("nonce_str", getRandamStr());// 随机字符串
			map.put("partner_trade_no", tf.partner_trade_no);// 商户订单号
			map.put("openid", tf.openid);
			map.put("check_name", tf.check_name);
			if ("FORCE_CHECK".equals(tf.check_name))
				map.put("re_user_name", tf.re_user_name);
			map.put("amount", tf.amount);// 总金额(单位分)
			map.put("desc", tf.desc);// 总金额(单位分)
			map.put("spbill_create_ip", tf.spbill_create_ip); // 调用接口的机器Ip地址

			// 增加签名
			map.put("sign", getSign(map, this.apiKey));// 签名

			SSLSocketFactory ssl = WebClient.getSSLSocketFactory("apiclient_cert.p12", "1447261502");
			String return_xml = uploadData(URL_TRANSFERS, XMLUtils.toXML(map).getBytes(), ssl);
			Map<String, Object> result = XMLUtils.doXMLParse(return_xml);
			String returnCode = getValue(result, "return_code");
			String resultCode = getValue(result, "result_code");
			if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode))
				return true;
			else {
				tf.error = getValue(result, "err_code_des");
				return false;
			}
		} catch (Exception e) {
			tf.error = e.getMessage();
			return false;
		}
	}

	public Map<String, Object> createUnifiedOrder(UnifiedOrder order) {
		Map<String, Object> map = new HashMap<>();
		map.put("appid", this.appId);// 应用ID
		map.put("mch_id", this.mchId);// 商户号
		// map.put("device_info", "");//设备号
		map.put("nonce_str", getRandamStr());// 随机字符串
		if (order.body != null && order.body.length() > 64)
			map.put("body", order.body.substring(0, 64));// 商品描述
		else
			map.put("body", order.body);// 商品描述
		// map.put("detail", "");//商品详情
		// map.put("attach", "");//附加数据
		map.put("out_trade_no", order.out_trade_no);// 商户订单号
		// map.put("fee_type", "");//货币类型
		map.put("total_fee", order.total_fee);// 总金额(单位分)
		map.put("spbill_create_ip", order.spbill_create_ip);// 客户端IP
		// map.put("time_start", "");//交易起始时间
		// map.put("time_expire", "");//交易结束时间
		// map.put("goods_tag", "");//商品标记
		map.put("notify_url", this.notifyUrl);// 通知地址
		if (StringUtils.isNotBlank(order.sub_mch_id))
			map.put("sub_mch_id", order.sub_mch_id);// 子商户订单号

		if (StringUtils.isEmpty(order.openid))
			map.put("trade_type", "APP");// 交易类型
		else {
			map.put("trade_type", "JSAPI");// 交易类型
			map.put("openid", order.openid);
		}

		map.put("sign", getSign(map, this.apiKey));// 签名
		// map.put("limit_pay", "");// 指定支付方式

		// post调取方法
		String return_xml = uploadString(URL_CREATE_UNIFIEDORDER, XMLUtils.toXML(map));
		Map<String, Object> result = XMLUtils.doXMLParse(return_xml);
		String returnCode = getValue(result, "return_code");
		String resultCode = getValue(result, "result_code");
		if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
			String prepayid = getValue(result, "prepay_id");
			Map<String, Object> maplast = new HashMap<>();
			if (StringUtils.isEmpty(order.openid)) {
				maplast.put("appid", "wx1f3bb7b3d7b22a6b");
				maplast.put("noncestr", getRandamStr());
				maplast.put("partnerid", "1447261502");
				maplast.put("prepayid", prepayid);
				maplast.put("timestamp", getTimestamp());
				maplast.put("package", "Sign=WXPay");
				maplast.put("sign", getSign(maplast, this.apiKey));
			} else {
				maplast.put("appId", "wx1f3bb7b3d7b22a6b");
				maplast.put("nonceStr", getRandamStr());
				maplast.put("package", "prepay_id=" + prepayid);
				maplast.put("timeStamp", getTimestamp());
				maplast.put("signType", "MD5");
				maplast.put("paySign", getSign(maplast, this.apiKey));
			}
			return maplast;
		}
		return null;
	}

	public PayResult callback(BufferedReader reader) throws IOException {
		String xml = XMLUtils.deserialize(reader);
		Map<String, Object> result = XMLUtils.doXMLParse(xml);
		// 退款通知
		if (result.containsKey("req_info")) {
			byte[] data = Base64.getDecoder().decode(result.get("req_info").toString());
			byte[] key = MD5.encodeByMD5(this.apiKey).toLowerCase().getBytes();
			Security.addProvider(new BouncyCastleProvider());
			String str = ObjectUtils.decryptData(data, key);
			if (StringUtils.isNotBlank(str)) {
				Map<String, Object> result2 = XMLUtils.doXMLParse(str);
				PayResult msg = new PayResult(true);
				msg.mch_id = getValue(result, "mch_id");
				msg.appid = getValue(result, "appid");
				// 商户订单号
				msg.out_trade_no = getValue(result2, "out_trade_no");
				msg.success_time = getValue(result2, "success_time");
				msg.refund_fee = getValue(result2, "refund_fee");
				return msg;
			}
			return null;
		}
		// 支付完成通知
		String returnCode = getValue(result, "return_code");
		String resultCode = getValue(result, "result_code");
		if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
			String sign = getValue(result, "sign");
			// 签名验证
			result.remove("sign");
			if (getSign(result, this.apiKey).equals(sign)) {
				PayResult msg = new PayResult(false);
				msg.appid = getValue(result, "appid");
				msg.mch_id = getValue(result, "mch_id");
				msg.openid = getValue(result, "openid");
				msg.trade_type = getValue(result, "trade_type");
				// 商户订单号
				msg.out_trade_no = getValue(result, "out_trade_no");
				msg.time_end = getValue(result, "time_end");
				msg.cash_fee = getValue(result, "cash_fee");
				return msg;
			}
		}
		return null;
	}
}
