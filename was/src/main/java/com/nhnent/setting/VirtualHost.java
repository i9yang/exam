package com.nhnent.setting;

import lombok.Data;

import java.util.Map;

@Data
public class VirtualHost {
	private String name;
	private String document_root;
	private String index = "index.html";

	private Map<String, String> errorPageMap;
}
