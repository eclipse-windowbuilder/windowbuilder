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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.jface.resource.ManagerContainerInfo;
import org.eclipse.wb.internal.swt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;

import org.eclipse.jface.resource.LocalResourceManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FontPropertyEditor} with {@link LocalResourceManager}.
 *
 * @author lobas_av
 */
public class FontPropertyEditorTestWithManager extends FontPropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				true);
	}

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
	// getText(), getClipboardSource()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Font creation using constructor <code>Font(Device, String, int, int)</code>.
	 */
	@Test
	public void test_textSource_over_Constructor() throws Exception {
		CompositeInfo shell = parseComposite("public class Test extends Shell {}");
		assert_getText_getClipboardSource_forSource(
				"new Font(null, \"MS Shell Dlg\", 12, SWT.BOLD)",
				"MS Shell Dlg 12 BOLD",
				FontPropertyEditor.getInvocationSource(shell, "MS Shell Dlg", 12, "org.eclipse.swt.SWT.BOLD"));
	}

	/**
	 * Font creation using constructor <code>Font(Device, String, int, int)</code>.
	 */
	@Test
	public void test_textSource_over_Constructor2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// add ResourceManager
		ManagerContainerInfo.get(shell);
		// set "font" property
		shell.addMethodInvocation(
				"setFont(org.eclipse.swt.graphics.Font)",
				FontPropertyEditor.getInvocationSource(shell, "MS Shell Dlg", 12, "org.eclipse.swt.SWT.BOLD"));
		shell.refresh();
		//
		Property property = shell.getPropertyByTitle("font");
		assertEquals("MS Shell Dlg 12 BOLD", PropertyEditorTestUtils.getText(property));
		assertEquals(
				FontPropertyEditor.getInvocationSource(shell, "MS Shell Dlg", 12, "org.eclipse.swt.SWT.BOLD"),
				PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * Font creation using JFace resource <code>JFaceResources.getXXXFont()</code>.
	 */
	@Test
	public void test_textSource_over_JFace() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"JFaceResources.getBannerFont()",
				"getBannerFont()",
				"org.eclipse.jface.resource.JFaceResources.getBannerFont()");
	}

	/**
	 * The call to setFont() must occur AFTER the resource manager was created.
	 */
	@Test
	public void test_textSource_order() throws Exception {
		CompositeInfo shell = parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.addMethodInvocation("setFont(org.eclipse.swt.graphics.Font)",
				FontPropertyEditor.getInvocationSource(shell, "MS Shell Dlg", 12, "org.eclipse.swt.SWT.BOLD"));
		shell.refresh();
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    createResourceManager();",
				"    setFont(localResourceManager.create(FontDescriptor.createFrom(\"MS Shell Dlg\", 12, SWT.BOLD)));",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),this);",
				"  }",
				"}");
	}
}