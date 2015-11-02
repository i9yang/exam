package com.nhnent.util;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassUtil {
	public static URLClassLoader getClassLoader(URL u) throws Exception {
		return new URLClassLoader(new URL[] {u});
	}
}
