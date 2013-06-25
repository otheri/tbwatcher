package com.otheri.tbwatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.otheri.commons.Consts;
import com.otheri.commons.io.Input;
import com.otheri.commons.io.Output;
import com.otheri.commons.msg.Request;
import com.otheri.commons.msg.Response;
import com.otheri.oop.server.MessageUtils;
import com.otheri.oop.server.OOPClass;
import com.otheri.oop.server.OOPMethod;
import com.otheri.oop.server.OOPService;

public class TBWatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 6032945714705151791L;

	private OOPService oopService;
	private CharsetDecoder charsetDecoder;
	private Exception error;

	public void init(ServletConfig config) throws ServletException {

		try {
			// 读取配置并初始化

			charsetDecoder = Charset.forName(Consts.ENCODING).newDecoder();
			oopService = new OOPService();

			oopService.putClass(TBWatcherService.class);

		} catch (Exception e) {
			e.printStackTrace();
			error = e;
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("doGet");
		response.setContentType("text/html;charset=UTF-8");

		String servletPath = request.getServletPath();
		String queryString = request.getQueryString();
		System.out.println(servletPath);
		System.out.println(queryString);

		if (servletPath.startsWith("/")) {
			servletPath = servletPath.substring(1);
		}
		String[] paths = servletPath.split("/");
		// for (String x : paths) {
		// System.out.println(x);
		// }
		if (paths != null && paths.length == 2) {
			Request req = new Request();
			req.setDomain(paths[0]);
			req.setMethod(paths[1]);
			req.setTimestamp(System.currentTimeMillis());

			JSONObject content = new JSONObject();
			if (null != queryString) {
				String[] params = queryString.split("&");
				for (String param : params) {
					String[] temp = param.split("=");
					content.put(temp[0],
							URLDecoder.decode(temp[1], Consts.ENCODING));
				}
			}

			req.setContent(content.toJSONString());

			Output out = new Output(response.getOutputStream());
			Response resp = null;
			try {
				resp = oopService.execute(req);
			} catch (Exception e) {
				resp = MessageUtils.failure(req, e.toString());
			}
			out.write(JSON.toJSONBytes(resp));
			out.close();
		} else {
			PrintWriter out = response.getWriter();

			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<head></head>");
			sb.append("<body>");
			if (oopService != null) {

				sb.append("<h1>------OOP Running------</h1><br /><br />");

				HashMap<String, OOPClass> classes = oopService.getClasses();
				HashMap<String, String> classesNoBinding = oopService
						.getClassesNoBinding();

				{
					sb.append("<h2>---Registed Classes---</h2><br />");
					Iterator<Entry<String, OOPClass>> iterator = classes
							.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<String, OOPClass> entry = iterator.next();
						OOPClass clazz = entry.getValue();

						sb.append("&nbsp&nbsp&nbsp");
						sb.append("domain = ");
						sb.append(entry.getKey());
						sb.append("<br />");
						sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
						sb.append("class = ");
						sb.append(clazz.realClassObject.getClass().getName());
						sb.append("<br />");

						{
							HashMap<String, OOPMethod> methods = clazz.methods;
							Iterator<Entry<String, OOPMethod>> im = methods
									.entrySet().iterator();
							if (methods.size() > 0) {
								sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
								sb.append("---Registed Methods---<br />");
								while (im.hasNext()) {
									Entry<String, OOPMethod> em = im.next();
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append("name = ");
									sb.append(em.getKey());
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append("type = ");
									sb.append(em.getValue().methodType);
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append("&&&");
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append(em.getValue().toString());
									sb.append("<br />");
								}
							} else {
								sb.append("<font color='#FF0000'>");
								sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
								sb.append("---No Methods Registed---<br />");
								sb.append("<br />");
								sb.append("</font>");
							}
						}

						{
							HashMap<String, OOPMethod> methodsNoBinding = clazz.methodsNoBinding;
							Iterator<Entry<String, OOPMethod>> im = methodsNoBinding
									.entrySet().iterator();
							if (methodsNoBinding.size() > 0) {
								sb.append("<font color='#FF0000'>");
								sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
								sb.append("---No Registed Methods (same name)---<br />");
								while (im.hasNext()) {
									Entry<String, OOPMethod> em = im.next();
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append("name = ");
									sb.append(em.getKey());
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append("type = ");
									sb.append(em.getValue().methodType);
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append("&&&");
									sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
									sb.append(em.getValue().toString());
									sb.append("<br />");
								}
								sb.append("</font>");
							}
						}

						sb.append("<br />");

					}
				}

				sb.append("<br /><br /><br />");

				{
					if (classesNoBinding.size() > 0) {
						Iterator<Entry<String, String>> iterator = classesNoBinding
								.entrySet().iterator();
						sb.append("<font color='#FF0000'>");
						sb.append("<h2>---No Registed Classes(same domain)---</h2><br />");
						while (iterator.hasNext()) {
							Entry<String, String> entry = iterator.next();
							sb.append("&nbsp&nbsp&nbsp");
							sb.append("domain = ");
							sb.append(entry.getKey());
							sb.append("<br />");
							sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
							sb.append("class = ");
							sb.append(entry.getValue());
							sb.append("<br />");
						}
						sb.append("</font>");
					}
				}
			} else {
				sb.append("<h1>---OOP init failure : ")
						.append(error.toString()).append("---</h1>");
			}
			sb.append("</body>");
			sb.append("</html>");

			out.println(sb.toString());

			out.flush();
			out.close();
		}

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		System.out.println("doPost");
		Input in = new Input(request.getInputStream());
		Output out = new Output(response.getOutputStream());

		Response resp = null;
		Request req = null;
		try {
			byte[] data = in.readAll();
			// String str = new String(data, "UTF-8");
			// System.out.println(str);
			req = JSON.parseObject(data, 0, data.length, charsetDecoder,
					Request.class);
			in.close();

			resp = oopService.execute(req);

		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
			resp = MessageUtils.failure(req, ite.getTargetException().toString());
		} catch (Exception e) {
			e.printStackTrace();
			resp = MessageUtils.failure(req, e.toString());
		}
		out.write(JSON.toJSONBytes(resp));
		out.close();
	}
}
