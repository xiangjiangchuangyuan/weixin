package com.xjcy.weixin.bean;

import com.xjcy.util.StringUtils;

public class UnifiedOrder {
	public String body;
	public String out_trade_no;
	public long total_fee;
	public String spbill_create_ip;
	public String sub_mch_id;
	public String openid;

	/**
	 * APP预付单测试
	 * 
	 * @return
	 */
	public static UnifiedOrder test() {
		return test(null);
	}

	/**
	 * 公众号预付单
	 * 
	 * @param openId
	 * @return
	 */
	public static UnifiedOrder test(String openId) {
		UnifiedOrder order = new UnifiedOrder();
		order.body = "test";
		order.out_trade_no = System.nanoTime() + "";
		order.spbill_create_ip = "172.0.0.1";
		order.total_fee = 1;
		if (StringUtils.isNotBlank(openId))
			order.openid = openId;
		return order;
	}
}
