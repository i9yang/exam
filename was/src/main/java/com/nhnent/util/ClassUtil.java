package com.nhnent.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassUtil {
	private static final Class[] parameters = new Class[] {URL.class};

	public static void addClass(URL u) throws Exception {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		Method method = sysclass.getDeclaredMethod("addURL", parameters);
		method.setAccessible(true);
		method.invoke(sysloader, new Object[] {u});
	}
}
