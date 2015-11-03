package service;

import com.nhnent.servlet.HttpRequest;
import com.nhnent.servlet.HttpResponse;
import com.nhnent.servlet.SimpleServlet;

public class Error implements SimpleServlet {
	@Override
	public void service(HttpRequest req, HttpResponse res) throws Exception{
		if(1==1) throw new RuntimeException("error");
	}
}
