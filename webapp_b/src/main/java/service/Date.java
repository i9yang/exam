package service;

import com.nhnent.servlet.HttpRequest;
import com.nhnent.servlet.HttpResponse;
import com.nhnent.servlet.SimpleServlet;

import java.io.Writer;
import java.text.SimpleDateFormat;

public class Date implements SimpleServlet {
	@Override
	public void service(HttpRequest req, HttpResponse res){
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			Writer writer = res.getWriter();
			writer.write("Date b.com : ");
			writer.write(sdf.format(new java.util.Date()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
