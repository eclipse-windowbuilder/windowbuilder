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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginElement;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayInputStream;

/**
 * Abstract {@link TestCase} for PDE related tests.
 *
 * @author scheglov_ke
 */
public abstract class AbstractPdeTest extends RcpModelTest {
	protected PdeUtils m_utils;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		setFileContentSrc(
				"testplugin/Activator.java",
				getSourceDQ(
						"package testplugin;",
						"import org.eclipse.ui.plugin.AbstractUIPlugin;",
						"public class Activator extends AbstractUIPlugin {",
						"  public Activator() {",
						"  }",
						"  public static Activator getDefault() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		// prepare PDEUtils
		m_utils = PdeUtils.get(m_project);
		m_utils.ensureSingleton();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		m_project = null;
		m_utils = null;
		do_projectDispose();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Public utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the content of <code>MANIFEST.MF</code> file.
	 */
	public static String getManifest() throws Exception {
		return AbstractJavaProjectTest.getFileContent("META-INF/MANIFEST.MF");
	}

	/**
	 * Sets the content of <code>MANIFEST.MF</code> file.
	 */
	public static void setManifest(String content) throws Exception {
		IFile resource = AbstractJavaProjectTest.getFile("META-INF/MANIFEST.MF");
		IOUtils2.setFileContents(resource, new ByteArrayInputStream(content.getBytes()));
	}

	/**
	 * @return the content of <code>plugin.xml</code> file.
	 */
	public static String getPluginXML() throws Exception {
		return AbstractJavaProjectTest.getFileContent("plugin.xml");
	}

	/**
	 * Creates <code>plugin.xml</code> file in current {@link IProject}.
	 */
	public static void createPluginXML(String... lines) throws Exception {
		PdeUtils.get(m_project).ensureSingleton();
		AbstractJavaProjectTest.setFileContent("plugin.xml", getPluginSource(lines));
		TestProject.waitForAutoBuild();
	}

	/**
	 * Asserts that <code>plugin.xml</code> is same as expected lines.
	 */
	public static void assertPluginXML(String[] expectedLines) throws Exception {
		assertEquals(getPluginSource(expectedLines), getPluginXML());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Protected utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link String} that is good for using as text for <code>plugin.xml</code>.
	 */
	private static String getPluginSource(String[] lines) {
		String source = getSource(lines);
		source = source.replace('\'', '"');
		return source;
	}

	/**
	 * Asserts that given {@link IPluginElement} has expected ID attribute value.
	 */
	public static void assertId(String expectedId, IPluginElement element) {
		assertEquals(expectedId, element.getAttribute("id").getValue());
	}
}