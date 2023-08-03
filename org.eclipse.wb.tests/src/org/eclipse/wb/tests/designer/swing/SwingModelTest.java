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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;

import javax.swing.JButton;

/**
 * Abstract super class for Swing tests.
 *
 * @author scheglov_ke
 */
public abstract class SwingModelTest extends AbstractJavaInfoTest {
	private boolean m_convertSingleQuotesToDouble = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
			configureNewProject();
		}
	}

	@Override
	protected void configureToolkits() {
		super.configureToolkits();
		configureDefaults(org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION);
	}

	/**
	 * Configures created project.
	 */
	protected void configureNewProject() throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source
	//
	////////////////////////////////////////////////////////////////////////////
	public void dontConvertSingleQuotesToDouble() {
		m_convertSingleQuotesToDouble = false;
	}

	@SuppressWarnings("unchecked")
	protected final <T extends JavaInfo> T parseJavaInfo(String... lines) throws Exception {
		return (T) parseSource("test", "Test.java", getTestSource(lines));
	}

	public final ContainerInfo parseContainer(String... lines) throws Exception {
		return parseJavaInfo(lines);
	}

	/**
	 * Asserts that active {@link AstEditor} has expected Swing source.
	 */
	public final void assertEditor(String... lines) {
		assertEditor(getTestSource(lines), m_lastEditor);
	}

	/**
	 * @return the source for Swing.
	 */
	public String getTestSource(String... lines) {
		if (m_convertSingleQuotesToDouble) {
			lines = getDoubleQuotes(lines);
		}
		lines =
				CodeUtils.join(new String[]{
						"package test;",
						"import java.awt.*;",
						"import java.awt.event.*;",
						"import javax.swing.*;",
				"import javax.swing.border.*;"}, lines);
		return getSource(lines);
	}

	/**
	 * Creates Swing class with <code>*.wbp-component.xml</code> metadata.
	 */
	public final void setJavaContentSrc(String packageName,
			String className,
			String[] source,
			String[] meta) throws Exception {
		setFileContentSrc(packageName, className + ".java", getTestSource(source));
		if (meta != null) {
			setFileContentSrc(packageName, className + ".wbp-component.xml", getSource(meta));
		}
		waitForAutoBuild();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the new {@link ComponentInfo} using {@link ConstructorCreationSupport}.
	 */
	protected static ComponentInfo createComponent(Class<?> componentClass) throws Exception {
		return createComponent(componentClass.getName());
	}

	/**
	 * @return the new {@link ComponentInfo} using {@link ConstructorCreationSupport}.
	 */
	public static ComponentInfo createComponent(String componentClassName) throws Exception {
		return createJavaInfo(componentClassName);
	}

	/**
	 * @return the new empty {@link JButton}.
	 */
	public static ComponentInfo createJButton() throws Exception {
		return createJavaInfo("javax.swing.JButton", "empty");
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
							"import javax.swing.JPanel;",
							"// filler filler filler filler filler",
							"// filler filler filler filler filler",
							"public class MyComponent extends JPanel {",
							"  public MyComponent() {",
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
}