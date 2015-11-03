package com.nhnent;

import com.nhnent.servlet.HttpServletRequest;
import com.nhnent.servlet.HttpServletResponse;
import com.nhnent.servlet.SimpleServlet;
import com.nhnent.setting.Setting;
import com.nhnent.setting.VirtualHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestProcessor implements Runnable {
	private Socket connection;
	private Setting setting;
	private Map<String, String> header;
	private final String defaultContentType = "text/html; charset=utf-8";

	public RequestProcessor(Setting setting, Socket connection) throws Exception {
		this.setting = setting;
		this.connection = connection;
		this.header = getHeader();
	}

	public Map<String, String> getHeader() throws Exception{
		Map<String, String> header = new HashMap<>();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		String infoLine = in.readLine();
		String[] requestInfo = infoLine.split(" ");
		header.put("method", requestInfo[0]);

		String[] location = requestInfo[1].split("\\?");
		header.put("path", location[0]);
		if (location.length > 1) {
			header.put("query", location[1]);
		}
		header.put("version", requestInfo[2]);

		String headerStr;
		while (StringUtils.isNotEmpty(headerStr = in.readLine())) {
			header.put(headerStr.split(": ")[0].toLowerCase(), headerStr.split(": ")[1]);
		}

		log.info("{} : {}", header.get("host").split(":")[0], infoLine);

		return header;
	}

	@Override
	public void run() {
		String domain = header.get("host").split(":")[0];
		VirtualHost virtualHost = setting.getVirtualHostMap().get(domain);

		try {
			String responseCode;
			OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
			Writer out = new OutputStreamWriter(raw);
			String root = new File(virtualHost.getDocument_root()).getCanonicalPath();

			if(StringUtils.equals("GET", header.get("method"))) {
				File theFile;

				if (StringUtils.equals(header.get("path"), "/")) {
					theFile = new File(root, virtualHost.getIndex());
					responseCode = "HTTP/1.1 200 OK";
					flushStream(theFile, out, raw, domain, responseCode, defaultContentType);
				} else if (StringUtils.contains(header.get("path"), ".exe") || StringUtils.contains(header.get("path"), "..")) {
					theFile = new File(root, virtualHost.getErrorPageMap().get("403"));
					responseCode = "HTTP/1.1 403 Forbidden";
					flushStream(theFile, out, raw, domain, responseCode, defaultContentType);
				} else {
					try {
						ClassLoader cl = setting.getClassLoaderMap().get(domain);
						Class clazz = cl.loadClass(header.get("path").substring(1));
						Object obj = clazz.newInstance();
						SimpleServlet ss;

						if (obj instanceof SimpleServlet) {
							ss = (SimpleServlet) obj;
							HttpServletRequest request = new HttpServletRequest(header);
							HttpServletResponse response = new HttpServletResponse(new StringWriter());
							ss.service(request, response);

							responseCode = "HTTP/1.1 200 OK";
							sendHeader(out, responseCode, defaultContentType, response.getWriter().toString().length(), domain);
							out.write(response.getWriter().toString());
							out.flush();
						}
					} catch (ClassNotFoundException e) {
						theFile = new File(root, virtualHost.getErrorPageMap().get("404"));
						responseCode = "HTTP/1.1 404 File Not Found";
						flushStream(theFile, out, raw, domain, responseCode, defaultContentType);
					}
				}
			} else {
				responseCode = "HTTP/1.1 200 OK";
				String responseMsg = "Not Support!";

				sendHeader(out, responseCode, defaultContentType, responseMsg.length(), domain);
				out.write(responseMsg);
				out.flush();
			}
		} catch (Exception e) {
			log.error("request handler Exception : ", e);
			try {
				String root = new File(virtualHost.getDocument_root()).getCanonicalPath();
				OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
				Writer out = new OutputStreamWriter(raw);
				File theFile = new File(root, virtualHost.getErrorPageMap().get("500"));
				String responseCode = "HTTP/1.1 500 Internal server error";
				flushStream(theFile, out, raw, domain, responseCode, defaultContentType);
			} catch (Exception ex) {
				log.error("request handler Exception : ", ex);
			}
		} finally {
			try {
				connection.close();
			} catch (Exception ex) {
				log.error("request handler Exception : ", ex);
			}
		}
	}

	private void flushStream(File theFile, Writer out, OutputStream raw, String domain, String responseCode, String contenetType) throws Exception {
		byte[] theData = Files.readAllBytes(theFile.toPath());
		sendHeader(out, responseCode, contenetType, theData.length, domain);
		raw.write(theData);
		raw.flush();
	}

	private void sendHeader(Writer out, String responseCode, String contentType, int length, String serverName) throws Exception {
		out.write(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		out.write("Server: JHTTP 2.0\r\n");
		out.write("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n");
		out.write("Host: " + serverName + "\r\n\r\n");
		out.flush();
	}
}
