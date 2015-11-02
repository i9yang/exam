package com.nhnent.setting;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Setting {
	private int port;
	private Map<String, VirtualHost> virtualHostMap = new HashMap<>();
	private Map<String, ClassLoader> classLoaderMap = new HashMap<>();
}
