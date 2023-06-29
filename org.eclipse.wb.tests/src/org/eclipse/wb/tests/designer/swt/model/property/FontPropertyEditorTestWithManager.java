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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

/**
 * Tests for {@link FontPropertyEditor} with <code>SWTResourceManager</code>.
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
	protected void setUp() throws Exception {
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
	public void test_textSource_over_Constructor() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Font(null, \"MS Shell Dlg\", 12, SWT.BOLD)",
				"MS Shell Dlg 12 BOLD",
				"org.eclipse.wb.swt.SWTResourceManager.getFont(\"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD)");
	}

	/**
	 * Font creation using constructor <code>Font(Device, String, int, int)</code>.
	 */
	public void test_textSource_over_Constructor2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// add SWTResourceManager
		ManagerUtils.ensure_SWTResourceManager(shell);
		// set "font" property
		shell.addMethodInvocation(
				"setFont(org.eclipse.swt.graphics.Font)",
				"org.eclipse.wb.swt.SWTResourceManager.getFont(\"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD)");
		shell.refresh();
		//
		Property property = shell.getPropertyByTitle("font");
		assertEquals("MS Shell Dlg 12 BOLD", PropertyEditorTestUtils.getText(property));
		assertEquals(
				"org.eclipse.wb.swt.SWTResourceManager.getFont(\"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD)",
				PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * Font creation using JFace resource <code>JFaceResources.getXXXFont()</code>.
	 */
	public void test_textSource_over_JFace() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"JFaceResources.getBannerFont()",
				"getBannerFont()",
				"org.eclipse.jface.resource.JFaceResources.getBannerFont()");
	}
}