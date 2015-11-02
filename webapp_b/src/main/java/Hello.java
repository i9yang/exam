import com.nhnent.servlet.HttpRequest;
import com.nhnent.servlet.HttpResponse;
import com.nhnent.servlet.SimpleServlet;

import java.io.Writer;

public class Hello implements SimpleServlet{
	@Override
	public void service(HttpRequest req, HttpResponse res){
		try {
			Writer writer = res.getWriter();
			writer.write("Hello b.com : ");
			writer.write(req.getParameter("name"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
