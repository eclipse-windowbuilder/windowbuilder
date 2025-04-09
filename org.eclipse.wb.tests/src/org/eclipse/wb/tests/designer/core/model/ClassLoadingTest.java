/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.runtime.IConfigurationElement;

import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.List;

/**
 * Tests for various {@link ClassLoader} tricks.
 *
 * @author scheglov_ke
 */
public class ClassLoadingTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_installNewBundleWithContributions() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addClass(ClassForBundle.class);
			testBundle.addExtension(
					"org.eclipse.wb.core.toolkits",
					"<toolkit id='org.eclipse.wb.swing' myAttr='0'/>");
			testBundle.install();
			try {
				// classes can be loaded from this "test" Bundle
				testBundle.getBundle().loadClass(className);
				// extensions are applied
				assertTrue(hasToolkitElementWith("myAttr"));
			} finally {
				testBundle.uninstall();
			}
		} finally {
			testBundle.dispose();
		}
		assertFalse(hasToolkitElementWith("myAttr"));
	}

	/**
	 * Test for using <code>classLoader-bundle</code> to contribute Bundle into toolkit
	 * {@link ClassLoader}.
	 */
	@Test
	public void test_useClasspathBundle() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addClass(ClassForBundle.class);
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.swing'>",
					"  <classLoader-bundle bundle='" + testBundle.getId() + "'/>",
			"</toolkit>"});
			testBundle.install();
			try {
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"      // filler",
						"  }",
						"}");
				// ClassForBundle is visible in Swing ClassLoader
				m_lastLoader.loadClass(className);
			} finally {
				testBundle.uninstall();
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-library</code> to contribute JAR into toolkit
	 * {@link ClassLoader}.
	 */
	@Test
	public void test_useClasspathLibrary_singleJar() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.swing'>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
			"</toolkit>"});
			testBundle.install();
			try {
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"      // filler",
						"  }",
						"}");
				// ClassForBundle is visible in Swing ClassLoader
				m_lastLoader.loadClass(className);
			} finally {
				testBundle.uninstall();
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-library</code> to contribute two JAR's into toolkit
	 * {@link ClassLoader}, where {@link Class} from one JAR references {@link Class} in other JAR.
	 */
	@Test
	public void test_useClassLoaderLibrary_toDependentJars() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			String className2 = ClassForBundle2.class.getName();
			testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
			testBundle.addJar("myClasses2.jar").addClass(ClassForBundle2.class).close();
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.swing'>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses2.jar'/>",
			"</toolkit>"});
			testBundle.install();
			try {
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"      // filler",
						"  }",
						"}");
				// ClassForBundle & ClassForBundle2 are visible in Swing ClassLoader
				m_lastLoader.loadClass(className);
				m_lastLoader.loadClass(className2);
			} finally {
				testBundle.uninstall();
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-library</code> to contribute two JAR's wrapped in jar-bundle.
	 */
	@Test
	public void test_useClassLoaderLibrary_twoDependentJars_packedBundle() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			String className2 = ClassForBundle2.class.getName();
			testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
			testBundle.addJar("myClasses2.jar").addClass(ClassForBundle2.class).close();
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.swing'>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses2.jar'/>",
			"</toolkit>"});
			testBundle.install(true);
			try {
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"      // filler",
						"  }",
						"}");
				// ClassForBundle & ClassForBundle2 are visible in Swing ClassLoader
				m_lastLoader.loadClass(className);
				m_lastLoader.loadClass(className2);
			} finally {
				testBundle.uninstall();
			}
		} finally {
			testBundle.dispose();
		}
	}

	private static boolean hasToolkitElementWith(String attribute) {
		List<IConfigurationElement> toolkitElements =
				DescriptionHelper.getToolkitElements("org.eclipse.wb.swing");
		for (IConfigurationElement toolkitElement : toolkitElements) {
			if (toolkitElement.getAttribute(attribute) != null) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ClassForBundle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We use this class to put it into new {@link Bundle}.
	 */
	public static class ClassForBundle {
	}
	/**
	 * We use this class to check referencing other classes, from separate JAR's.
	 */
	public static class ClassForBundle2 extends ClassForBundle {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// wbp-meta/ConfigureClassLoader.mvel
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sometimes users want to initialize environment, configure static objects.
	 */
	@Test
	public void test_ConfigureClassLoader() throws Exception {
		setFileContentSrc(
				"test/MyObject.java",
				getSource(
						"package test;",
						"public class MyObject {",
						"  public static int m_value = 0;",
						"  public static void setValue(int value) {",
						"    m_value = value;",
						"  }",
						"}"));
		setFileContent("wbp-meta/ConfigureClassLoader.mvel", "test.MyObject.setValue(123);");
		waitForAutoBuild();
		//
		parseContainer(
				"public class Test extends JFrame {",
				"  public Test() {",
				"      // filler",
				"  }",
				"}");
		assertNoErrors(m_lastParseInfo);
		// check that "MyObject.m_value" was initialized to "123"
		Class<?> myObjectClass = m_lastLoader.loadClass("test.MyObject");
		assertEquals(123, ReflectionUtils.getFieldInt(myObjectClass, "m_value"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getPackage()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * There was bug that our {@link ClassLoader}s did not provide {@link Package}s.
	 */
	@Test
	public void test_getPackage() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  private String bundle = getClass().getPackage().getName();",
						"}"));
		waitForAutoBuild();
		//
		ContainerInfo panel =
				parseJavaInfo(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"      // filler",
						"  }",
						"}");
		panel.refresh();
		assertEquals("test", ReflectionUtils.getFieldString(panel.getObject(), "bundle"));
	}
}
