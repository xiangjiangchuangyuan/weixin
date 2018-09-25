package com.xjcy.weixin.event;

import java.io.IOException;
import java.util.Map;

public interface AuthEvent {

	String doXml() throws IOException;

	void doText(Map<String, Object> message);

}
