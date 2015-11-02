package com.nhnent.servlet;

import java.util.Map;

public interface HttpRequest {
	String getParameter(String key);
	Map<String, String> getHeaderMap();
}
