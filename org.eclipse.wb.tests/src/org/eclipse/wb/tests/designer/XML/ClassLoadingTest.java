/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XML;

import com.google.common.collect.Iterators;

import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IJavaProject;

import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Tests for various {@link ClassLoader} tricks in XML.
 *
 * @author scheglov_ke
 */
public class ClassLoadingTest extends XwtModelTest {
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
	public void test_installNewBundleWithContributions() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addClass(ClassForBundle.class);
			testBundle.addExtension(
					"org.eclipse.wb.core.toolkits",
					"<toolkit id='org.eclipse.wb.rcp' myAttr='0'/>");
			testBundle.install();
			{
				// classes can be loaded from this "test" Bundle
				testBundle.getBundle().loadClass(className);
				// extensions are applied
				assertTrue(hasToolkitElementWith("myAttr"));
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
	public void test_useClasspathBundle() throws Exception {
		TestBundle testBundle = new TestBundle("org.eclipse.wb.tests.testBundle-0");
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addClass(ClassForBundle.class);
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.rcp'>",
					"  <classLoader-bundle bundle='" + testBundle.getId() + "'/>",
			"</toolkit>"});
			testBundle.install();
			{
				parse("<Shell/>");
				// ClassForBundle is visible in RCP ClassLoader
				m_lastLoader.loadClass(className);
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-bundle</code> to contribute Bundle into toolkit
	 * {@link ClassLoader}.
	 * <p>
	 * Namespaces are set so that class is not visible.
	 */
	public void test_useClasspathBundle_namespaces() throws Exception {
		TestBundle testBundle = new TestBundle("org.eclipse.wb.tests.testBundle-0");
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addClass(ClassForBundle.class);
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.rcp'>",
					"  <classLoader-bundle bundle='" + testBundle.getId() + "' namespaces='no.such'/>",
			"</toolkit>"});
			testBundle.install();
			{
				parse("<Shell/>");
				// ClassForBundle is visible in RCP ClassLoader
				try {
					m_lastLoader.loadClass(className);
					fail();
				} catch (ClassNotFoundException e) {
				}
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-library</code> to contribute JAR into toolkit
	 * {@link ClassLoader}.
	 */
	public void test_useClasspathLibrary_singleJar() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.rcp'>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
			"</toolkit>"});
			testBundle.install();
			{
				parse("<Shell/>");
				// ClassForBundle is visible in RCP ClassLoader
				m_lastLoader.loadClass(className);
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-library</code> to contribute two JAR's into toolkit
	 * {@link ClassLoader}, where {@link Class} from one JAR references {@link Class} in other JAR.
	 */
	public void test_useClassLoaderLibrary_toDependentJars() throws Exception {
		TestBundle testBundle = new TestBundle("org.eclipse.wb.tests.testBundle-1");
		try {
			String className = ClassForBundle.class.getName();
			String className2 = ClassForBundle2.class.getName();
			testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
			testBundle.addJar("myClasses2.jar").addClass(ClassForBundle2.class).close();
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.rcp'>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses2.jar'/>",
			"</toolkit>"});
			testBundle.install();
			{
				parse("<Shell/>");
				// ClassForBundle & ClassForBundle2 are visible in RCP ClassLoader
				m_lastLoader.loadClass(className);
				m_lastLoader.loadClass(className2);
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * Test for using <code>classLoader-library</code> to contribute two JAR's wrapped in jar-bundle.
	 */
	public void test_useClassLoaderLibrary_twoDependentJars_packedBundle() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			String className = ClassForBundle.class.getName();
			String className2 = ClassForBundle2.class.getName();
			testBundle.addJar("myClasses.jar").addClass(ClassForBundle.class).close();
			testBundle.addJar("myClasses2.jar").addClass(ClassForBundle2.class).close();
			testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
					"<toolkit id='org.eclipse.wb.rcp'>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
					"  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses2.jar'/>",
			"</toolkit>"});
			testBundle.install(true);
			{
				parse("<Shell/>");
				// ClassForBundle & ClassForBundle2 are visible in RCP ClassLoader
				m_lastLoader.loadClass(className);
				m_lastLoader.loadClass(className2);
			}
		} finally {
			testBundle.dispose();
		}
	}

	/**
	 * There was problem: because of using project {@link ClassLoader} as parent several times, we
	 * returned same resource from {@link IJavaProject} also several times. So, some tweak required
	 * here.
	 */
	public void test_getResources() throws Exception {
		setFileContentSrc("test/myFile.txt", "");
		waitForAutoBuild();
		//
		parse("<Shell/>");
		// only one resource
		Enumeration<URL> resEnumeration = m_lastLoader.getResources("test/myFile.txt");
		Iterator<URL> resIterator = Iterators.forEnumeration(resEnumeration);
		assertEquals(1, Iterators.size(resIterator));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean hasToolkitElementWith(String attribute) {
		List<IConfigurationElement> toolkitElements =
				DescriptionHelper.getToolkitElements("org.eclipse.wb.rcp");
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
}
