package com.xjcy.weixin;

import com.xjcy.weixin.api.MP;
import com.xjcy.weixin.api.MiniApp;
import com.xjcy.weixin.api.Pay;

/**
 * Hello world!
 *
 */
public class WeixinApi {
	private static MiniApp miniApp;
	private static MP mp;
	private static Pay pay;

	public static void registerMiniApp(String appId, String appSecret) {
		miniApp = new MiniApp(appId, appSecret);
	}

	public static void registerMiniAppPay(String mchId, String apiKey, String notifyUrl) {
		miniApp.setPayConfig(mchId, apiKey, notifyUrl);
	}

	public static void registerMP(String appId, String appSecret) {
		mp = new MP(appId, appSecret);
	}

	public static void registerPay() {
		pay = new Pay();
	}

	public static MiniApp app() {
		return miniApp;
	}

	public static MP mp() {
		return mp;
	}

	public static Pay pay() {
		return pay;
	}
}
