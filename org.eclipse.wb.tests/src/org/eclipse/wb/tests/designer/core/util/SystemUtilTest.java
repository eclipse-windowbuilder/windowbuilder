package org.eclipse.wb.tests.designer.core.util;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class SystemUtilTest extends Assert {
	@Test
	public void testCanParseModernJavaVersion() throws Exception {
		assertEquals(1.8111f, checkCanParseJavaVersion("1.8.111"), 0.000001);
		//
		assertEquals(9.0f, checkCanParseJavaVersion("9"), 0.000001);
		assertEquals(9.0f, checkCanParseJavaVersion("9.0"), 0.000001);
		assertEquals(9.01f, checkCanParseJavaVersion("9.0.1"), 0.000001);
		//
		assertEquals(10.0f, checkCanParseJavaVersion("10"), 0.000001);
		assertEquals(10.0f, checkCanParseJavaVersion("10.0"), 0.000001);
		assertEquals(10.01f, checkCanParseJavaVersion("10.0.1"), 0.000001);
		//
		assertEquals(".1+10 should be ignored", 10.0f, checkCanParseJavaVersion("10.0.1+10"), 0.000001);
	}

	private float checkCanParseJavaVersion(String javaVersion) throws Exception {
		int JAVA_VERSION_TRIM_SIZE = 3; // private constant from SystemUtils: JAVA_VERSION_TRIM_SIZE = 3;
		Method toJavaVersionIntArray =
				SystemUtils.class.getDeclaredMethod("toJavaVersionIntArray", String.class, int.class);
		toJavaVersionIntArray.setAccessible(true);
		Method toVersionFloat = SystemUtils.class.getDeclaredMethod("toVersionFloat", int[].class);
		toVersionFloat.setAccessible(true);
		//
		int[] javaVersionIntArray =
				(int[]) toJavaVersionIntArray.invoke(null, javaVersion, JAVA_VERSION_TRIM_SIZE);
		Float versionFloat = (Float) toVersionFloat.invoke(null, javaVersionIntArray);
		return versionFloat;
	}
}
