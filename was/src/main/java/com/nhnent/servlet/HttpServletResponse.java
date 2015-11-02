package com.nhnent.servlet;

import java.io.Writer;

public class HttpServletResponse implements HttpResponse{
	private Writer out;

	public HttpServletResponse(Writer out) {
		this.out = out;
	}

	public Writer getWriter(){
		return out;
	}

	public void write(String str){
		try {
			out.write(str);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
