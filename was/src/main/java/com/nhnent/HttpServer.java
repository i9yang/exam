package com.nhnent;

import com.nhnent.setting.Setting;
import com.nhnent.setting.VirtualHost;
import com.nhnent.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class HttpServer extends Thread {
	private static final int NUM_THREADS = 50;
	private final Setting setting;

	public HttpServer() throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject root = (JSONObject) parser.parse(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("config.json")));
		JSONObject server = (JSONObject) root.get("server");

		setting = new Setting();
		setting.setPort(new Integer((String) server.get("port")));

		Map<String, VirtualHost> hostMap = setting.getVirtualHostMap();

		JSONObject serverList;
		JSONObject errorPage;
		for (Object obj : (JSONArray) server.get("list")) {
			serverList = (JSONObject) obj;
			VirtualHost virtual = new VirtualHost();
			virtual.setName((String) serverList.get("name"));
			virtual.setDocument_root((String) serverList.get("document_root"));

			errorPage = (JSONObject) serverList.get("error_page");
			virtual.setErrorPageMap(errorPage);

			hostMap.put((String) serverList.get("name"), virtual);

			ClassUtil.addClass(new File(virtual.getDocument_root()).toURI().toURL());
		}
	}

	public void run() {
		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
		try (ServerSocket server = new ServerSocket(setting.getPort())) {
			log.info("server started");
			while (true) {
				Socket request = server.accept();
				log.info("remoteSocketAddress : {}", request.getRemoteSocketAddress());
				Runnable r = new RequestProcessor(setting, request);
				pool.submit(r);
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
	}

	public static void main(String[] args) throws Exception {
		HttpServer server = new HttpServer();
		server.start();
	}
}
