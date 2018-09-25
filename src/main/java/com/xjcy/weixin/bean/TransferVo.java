package com.xjcy.weixin.bean;

public class TransferVo {
	public String openid;
	public String partner_trade_no;
	public long amount;
	public String desc;
	public String spbill_create_ip;
	public String check_name;
	public String re_user_name;
	public String error;
	
	/**
	 * 公众号预付单
	 * 
	 * @param openId
	 * @return
	 */
	public static TransferVo test(String openId)
	{
		TransferVo tf = new TransferVo();
		tf.openid = openId;
		tf.partner_trade_no = System.nanoTime() +"";
		tf.amount = 30; //最低为0.3元
		tf.spbill_create_ip = "172.0.0.1";
		tf.desc = "test";
		tf.check_name = "NO_CHECK";
		return tf;
	}
}
