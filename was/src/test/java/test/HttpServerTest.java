package test;

import com.nhnent.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Slf4j
public class HttpServerTest {
	@BeforeClass
	public static void init() {
		try {
			Thread t = new Thread(new HttpServer());
			t.start();
		} catch (Exception e) {
			log.error("Server Init error : {} ", e);
		}
	}

	@Test
	public void virtualHostTest() throws Exception {
//		String[] urls = {"http://a.com:8080", "http://b.com:8080"};
//
//		for (String u : urls) {
//			URL url = new URL(u);
//			URLConnection con = url.openConnection();
//			Map<String, List<String>> header = con.getHeaderFields();
//			log.debug("{} : {}",header.get("Host").get(0), url.getHost());
//			assertEquals(header.get("Host").get(0), url.getHost());
//		}
	}
}
