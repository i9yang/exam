package com.nhnent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.nhnent.servlet.HttpServletRequest;
import com.nhnent.servlet.HttpServletResponse;
import com.nhnent.servlet.SimpleServlet;
import com.nhnent.setting.Setting;
import com.nhnent.setting.VirtualHost;

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

	public RequestProcessor(Setting setting, Socket connection) throws Exception {
		this.setting = setting;
		this.connection = connection;
		this.header = getHeader();
	}

	public Map<String, String> getHeader(){
		Map<String, String> header = new HashMap<>();

		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String[] requestInfo = in.readLine().split(" ");
			header.put("method", requestInfo[0]);

			String[] location = requestInfo[1].split("\\?");
			header.put("path", location[0]);
			if(location.length > 1) {
				header.put("query", location[1]);
			}
			header.put("version", requestInfo[2]);

			String headerStr;
			while(StringUtils.isNotEmpty(headerStr = in.readLine()) ){
				header.put(headerStr.split(": ")[0].toLowerCase(), headerStr.split(": ")[1]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return header;
	}

	@Override
	public void run() {
		String domain = header.get("host").split(":")[0];
		VirtualHost virtualHost = setting.getVirtualHostMap().get(domain);

		 try {
			String root = new File(virtualHost.getDocument_root()).getCanonicalPath();
		    OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
			Writer out = new OutputStreamWriter(raw);
			File theFile;
			if(StringUtils.equals(header.get("path"), "/")) {
				theFile = new File(root, virtualHost.getIndex());
				 if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
					 byte[] theData = Files.readAllBytes(theFile.toPath());
					 sendHeader(out, "HTTP/1.1 200 OK", "text/html; charset=utf-8", theData.length, domain);
					 raw.write(theData);
					 raw.flush();
				 } else {
					theFile = new File(root, virtualHost.getErrorPageMap().get("404"));
					if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
						byte[] theData = Files.readAllBytes(theFile.toPath());
						sendHeader(out, "HTTP/1.1 404 File Not Found", "text/html; charset=utf-8", theData.length, domain);
						raw.write(theData);
						raw.flush();
					}
				 }
			} else if(StringUtils.contains(header.get("path"), ".exe") || StringUtils.contains(header.get("path"), "..") ) {
				theFile = new File(root, virtualHost.getErrorPageMap().get("403"));
				if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
					byte[] theData = Files.readAllBytes(theFile.toPath());
					sendHeader(out, "HTTP/1.1 403 Forbidden", "text/html; charset=utf-8", theData.length, domain);
					raw.write(theData);
					raw.flush();
				}
			} else {
				try {
					Class clazz = Class.forName(header.get("path").substring(1));
					Object obj = clazz.newInstance();
					SimpleServlet ss;
					if (obj instanceof SimpleServlet) {
						ss = (SimpleServlet) obj;
						HttpServletRequest request = new HttpServletRequest(header);
						HttpServletResponse response = new HttpServletResponse(new StringWriter());
						ss.service(request, response);

						out.write("HTTP/1.1 200 OK" + "\r\n");
						Date now = new Date();
						out.write("Date: " + now + "\r\n");
						out.write("Server: JHTTP 2.0\r\n");
						out.write("Content-length: " + response.getWriter().toString().length() + "\r\n");
						out.write("Content-type: " + "text/html; charset=utf-8" + "\r\n");
						out.write("Host: " + domain + "\r\n\r\n");

						out.write(response.getWriter().toString());
						out.flush();
					}
				}catch (ClassNotFoundException e) {
					theFile = new File(root, virtualHost.getErrorPageMap().get("404"));
					if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
						byte[] theData = Files.readAllBytes(theFile.toPath());
						sendHeader(out, "HTTP/1.1 404 File Not Found", "text/html; charset=utf-8", theData.length, domain);
						raw.write(theData);
						raw.flush();
					}
				}
			}
		 }catch(Exception e) {
			 e.printStackTrace();
			 try {
				String root = new File(virtualHost.getDocument_root()).getCanonicalPath();
			    OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
				Writer out = new OutputStreamWriter(raw);
				File theFile;
				 theFile = new File(root, virtualHost.getErrorPageMap().get("500"));
				if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
					byte[] theData = Files.readAllBytes(theFile.toPath());
					sendHeader(out, "HTTP/1.1 500 Internal server error", "text/html; charset=utf-8", theData.length, domain);
					raw.write(theData);
					raw.flush();
				}
			 }catch (Exception ex) {
				 ex.printStackTrace();
			 }
		 } finally {
            try {
                connection.close();
            } catch (IOException ex) {
	            ex.printStackTrace();
            }
        }
	}


	private void sendHeader(Writer out, String responseCode, String contentType, int length, String serverName) throws IOException {
		out.write(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		out.write("Server: JHTTP 2.0\r\n");
		out.write("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n");
		out.write("Host: " + serverName+ "\r\n\r\n");
		out.flush();
	}
}
