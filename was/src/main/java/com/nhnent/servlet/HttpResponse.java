package com.nhnent.servlet;

import java.io.Writer;

public interface HttpResponse {
	Writer getWriter();
	void write(String str);
}
