package com.xjcy.weixin.event;

import java.io.IOException;
import java.util.Map;

public interface MPEvent extends AuthEvent {

	void doSubscribe(String eventKey, Map<String, Object> message);

	void doUnSubscribe(Map<String, Object> message);

	void doScan(String eventKey, Map<String, Object> message);

	void doView(Map<String, Object> message);

	void doResponse(Map<String, Object> message) throws IOException;
}
