/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.After;
import org.junit.Before;

import javax.swing.JButton;

/**
 * Abstract test for Swing in editor.
 *
 * @author scheglov_ke
 */
public abstract class SwingGefTest extends DesignerEditorTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		configureDefaults(org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION);
		if (m_testProject == null) {
			do_projectCreate();
			configureNewProject();
		}
		configureForTest();
	}

	/**
	 * Configures created project.
	 */
	protected void configureNewProject() throws Exception {
	}

	@Override
	@After
	public void tearDown() throws Exception {
		configureDefaults();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures default preferences.
	 */
	public static void configureForTest() {
		IPreferenceStore preferences = SwingToolkitDescription.INSTANCE.getPreferences();
		preferences.setValue(
				IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
				SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
	}

	/**
	 * Restores default preferences.
	 */
	public static void configureDefaults() {
		IPreferenceStore preferences = SwingToolkitDescription.INSTANCE.getPreferences();
		preferences.setToDefault(IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected ContainerInfo openContainer(String lines) throws Exception {
		return openEditor(lines);
	}

	@SuppressWarnings("unchecked")
	protected <T extends ObjectInfo> T openEditor(String lines) throws Exception {
		ICompilationUnit unit = createModelCompilationUnit("test", "Test.java", getTestSource(lines));
		openDesign(unit);
		// prepare models
		return (T) m_contentJavaInfo;
	}

	/**
	 * Asserts that active {@link AstEditor} has expected Swing source.
	 */
	protected void assertEditor(String lines) {
		AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
		assertEditor(getTestSource(lines), editor);
	}

	/**
	 * @return the source for Swing.
	 */
	public String getTestSource(String source) {
		return """
				package test;
				import java.awt.*;
				import java.awt.event.*;
				import javax.swing.*;
				import javax.swing.border.*;
				%s
				""".formatted(source);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Button for GEF
	//
	////////////////////////////////////////////////////////////////////////////
	protected final void prepareBox() throws Exception {
		prepareBox(100, 50);
	}

	protected final void prepareBox(int width, int height) throws Exception {
		setFileContentSrc(
				"test/Box.java",
				getTestSource("""
						public class Box extends JLabel {
							public Box() {
								setPreferredSize(new Dimension(%d, %d));
								setBackground(Color.PINK);
								setOpaque(true);
							}
							public Box(String text) {
								this();
								setText(text);
							}
						}""".formatted(width, height)));
		waitForAutoBuild();
	}

	protected final ComponentInfo loadCreationBox() throws Exception {
		return loadCreationTool("test.Box");
	}

	/**
	 * Loads {@link CreationTool} with {@link JButton} without text.
	 */
	protected final ComponentInfo loadButton() throws Exception {
		return loadCreationTool("javax.swing.JButton", "empty");
	}

	/**
	 * Loads {@link CreationTool} with {@link JButton} with text.
	 */
	protected final ComponentInfo loadButtonWithText() throws Exception {
		return loadCreationTool("javax.swing.JButton");
	}
}
