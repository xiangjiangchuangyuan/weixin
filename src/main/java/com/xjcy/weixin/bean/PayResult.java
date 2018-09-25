package com.xjcy.weixin.bean;

/**
 * 微信支付通知结果
 * @author YYDF
 * 2018-05-15
 */
public class PayResult {
	private boolean _isRefund;

	public PayResult(boolean isRefund) {
		this._isRefund = isRefund;
	}
	
	public String appid;
	public String mch_id;
	public String openid;
	public String trade_type;
	public String out_trade_no;
	public String time_end;
	public String cash_fee;
	public String success_time;
	public String refund_fee;

	public boolean isRefund() {
		return _isRefund;
	}
	
	public static final String SUCCESS = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
	public static final String FAIL = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理通知消息失败]]></return_msg></xml>";

	@Override
	public String toString() {
		return "Payresult [mch_id=" + mch_id + ", openid=" + openid + ", trade_type=" + trade_type + ", out_trade_no="
				+ out_trade_no + ", time_end=" + time_end + ", cash_fee=" + cash_fee + "]";
	}
}
