package com.nhnent.servlet;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpServletRequest implements HttpRequest {
	private final Map<String, String> header;
	private final Map<String, String> parameterMap;

	public HttpServletRequest(Map<String, String> header){
		this.header = header;
		parameterMap = new HashMap<>();
		String query = header.get("query");

		if(StringUtils.isNotEmpty(query)) {
			for (String param : query.split("&")) {
				if (param.length() > 1) {
					parameterMap.put(param.split("=")[0], param.split("=")[1]);
				}
			}
		}
	}

	@Override
	public Map<String, String> getHeaderMap() {
		return header;
	}

	@Override
	public String getParameter(String key){
		return parameterMap.get(key);
	}
}
