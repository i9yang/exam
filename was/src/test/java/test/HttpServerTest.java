package test;

import com.nhnent.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class HttpServerTest {
	/**
	 * FIXME: maven test 시에만 테스트 실패.... 원인규명해야함..
	 */
	@BeforeClass
	public static void init() {
		try {
			HttpServer server = new HttpServer();
			server.start();
		} catch (Exception e) {
			log.error("Server Init error : {} ", e);
		}
	}

	@Test
	public void virtualHostTest() throws Exception {
		String[] urls = {"http://a.com:8080", "http://b.com:8080"};

		for (String u : urls) {
			URL url = new URL(u);
			URLConnection con = url.openConnection();
			Map<String, List<String>> header = con.getHeaderFields();

			log.debug("{} : {}", header.get("Host").get(0), url.getHost());
			assertEquals(header.get("Host").get(0), url.getHost());
		}
	}

	@Test
	public void httpStatusCodeTest() throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("403_1", "http://a.com:8080/test.exe");
		map.put("403_1", "http://a.com:8080/../../../../test");
		map.put("404", "http://a.com:8080/notFound");
		map.put("500", "http://a.com:8080/service.Error");

		for (String k : map.keySet()) {
			URL url = new URL(map.get(k));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.connect();

			log.debug("statusCode : {}, url : {}", con.getResponseCode(), map.get(k));
			assertTrue(StringUtils.contains(k, String.valueOf(con.getResponseCode())));
		}
	}

	@Test
	public void dateServletTest() throws Exception {
		URL url = new URL("http://a.com:8080/service.Date");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String inputLine;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date now = new Date();
		Date resultDate = now;
		while ((inputLine = in.readLine()) != null) {
			log.debug(inputLine);
			resultDate = sdf.parse(inputLine.split(" : ")[1]);
		}

		log.debug("now : {}, resultDate : {}", now, resultDate);
		//10초 안으로 차이나면 성공으로 간주
		assertTrue(now != resultDate && (now.getTime() - resultDate.getTime()) / 1000 < 10);

		in.close();
	}
}
