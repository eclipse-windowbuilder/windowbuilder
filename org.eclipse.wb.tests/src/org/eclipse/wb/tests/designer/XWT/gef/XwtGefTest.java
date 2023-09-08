/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XWT.gef;

import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.Activator;
import org.eclipse.wb.internal.xwt.editor.XwtEditor;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.editor.AbstractXmlGefTest;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Button;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract super class for XWT GEF tests.
 *
 * @author scheglov_ke
 */
public abstract class XwtGefTest extends AbstractXmlGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		configureForTestPreferences(Activator.getToolkit().getPreferences());
	}

	@Override
	@After
	public void tearDown() throws Exception {
		configureDefaultPreferences(Activator.getToolkit().getPreferences());
		super.tearDown();
	}

	/**
	 * Configures created project.
	 */
	@Override
	protected void configureNewProject() throws Exception {
		BTestUtils.configure(m_testProject);
		m_testProject.addPlugin("com.ibm.icu");
		m_testProject.addPlugin("org.eclipse.core.databinding");
		m_testProject.addPlugin("org.eclipse.core.databinding.beans");
		m_testProject.addPlugin("org.eclipse.core.databinding.observable");
		m_testProject.addPlugin("org.eclipse.core.databinding.property");
		m_testProject.addPlugin("org.eclipse.jface.databinding");
		m_testProject.addPlugin("org.eclipse.xwt");
		m_testProject.addPlugin("org.eclipse.xwt.forms");
		m_testProject.addPlugin("org.pushingpixels.trident");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures test values for toolkit preferences.
	 */
	protected void configureForTestPreferences(IPreferenceStore preferences) {
		// direct edit
		preferences.setValue(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, false);
	}

	/**
	 * Configures default values for toolkit preferences.
	 */
	protected void configureDefaultPreferences(IPreferenceStore preferences) {
		preferences.setToDefault(org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE);
		preferences.setToDefault(org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Open "Design" and fetch
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Opens {@link AbstractXmlEditor} with given XML source.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends XmlObjectInfo> T openEditor(String... lines) throws Exception {
		IFile file = setFileContentSrc("test/Test.xwt", getTestSource(lines));
		openDesign(file);
		return (T) m_contentObject;
	}

	@Override
	protected final String getEditorID() {
		return XwtEditor.ID;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Java source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getJavaSourceToAssert() {
		return getFileContentSrc("test/Test.java");
	}

	@Override
	protected String[] getJavaSource_decorate(String... lines) {
		lines =
				CodeUtils.join(new String[]{
						"package test;",
						"import org.eclipse.swt.SWT;",
						"import org.eclipse.swt.events.*;",
						"import org.eclipse.swt.graphics.*;",
						"import org.eclipse.swt.widgets.*;",
						"import org.eclipse.swt.layout.*;",
						"import org.eclipse.swt.custom.*;",
						"import org.eclipse.jface.layout.*;",
				"import org.eclipse.jface.viewers.*;"}, lines);
		return lines;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XML source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getTestSource_namespaces() {
		String newLine = "\n\t";
		return newLine
				+ "xmlns:wbp='http://www.eclipse.org/wb/xwt'"
				+ newLine
				+ "xmlns:t='clr-namespace:test'"
				+ newLine
				+ "xmlns='http://www.eclipse.org/xwt/presentation'"
				+ newLine
				+ "xmlns:x='http://www.eclipse.org/xwt'";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads {@link CreationTool} with {@link Button} without text.
	 */
	protected final XmlObjectInfo loadButton() throws Exception {
		return loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
	}

	/**
	 * Loads {@link CreationTool} with {@link Button} with text.
	 */
	protected final XmlObjectInfo loadButtonWithText() throws Exception {
		return loadCreationTool("org.eclipse.swt.widgets.Button");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// test.MyComponent support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares empty <code>test.MyComponent</code> class with additional lines in type body.
	 */
	protected final void prepareMyComponent(String... lines) throws Exception {
		prepareMyComponent(lines, ArrayUtils.EMPTY_STRING_ARRAY);
	}

	/**
	 * Prepares empty <code>test.MyComponent</code> class with additional lines in type body, and with
	 * special <code>wbp-component.xml</code> description.
	 */
	protected final void prepareMyComponent(String[] javaLines, String[] descriptionLines)
			throws Exception {
		// java
		{
			String[] lines =
					new String[]{
							"package test;",
							"import org.eclipse.swt.SWT;",
							"import org.eclipse.swt.widgets.*;",
							"public class MyComponent extends Composite {",
							"  public MyComponent(Composite parent, int style) {",
							"    super(parent, style);",
			"  }"};
			lines = CodeUtils.join(lines, javaLines);
			lines = CodeUtils.join(lines, new String[]{"}"});
			setFileContentSrc("test/MyComponent.java", getSourceDQ(lines));
		}
		// description
		{
			String[] lines =
					new String[]{
							"<?xml version='1.0' encoding='UTF-8'?>",
			"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>"};
			descriptionLines = removeFillerLines(descriptionLines);
			lines = CodeUtils.join(lines, descriptionLines);
			lines = CodeUtils.join(lines, new String[]{"</component>"});
			setFileContentSrc("test/MyComponent.wbp-component.xml", getSourceDQ(lines));
		}
		waitForAutoBuild();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Box
	//
	////////////////////////////////////////////////////////////////////////////
	protected void prepareBox() throws Exception {
		prepareBox(100, 50);
	}

	protected void prepareBox(int width, int height) throws Exception {
		setFileContentSrc(
				"test/Box.java",
				getJavaSource(
						"public class Box extends org.eclipse.swt.widgets.Button {",
						"  public Box(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  protected void checkSubclass () {",
						"  }",
						"  public Point computeSize (int wHint, int hHint, boolean changed) {",
						"    return new Point(" + width + ", " + height + ");",
						"  }",
						"}"));
		waitForAutoBuild();
	}

	protected ControlInfo loadBox() throws Exception {
		return loadCreationTool("test.Box");
	}
}