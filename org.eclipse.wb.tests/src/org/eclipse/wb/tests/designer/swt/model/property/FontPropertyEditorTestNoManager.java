/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.LocalResourceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FontPropertyEditor} without {@link LocalResourceManager}.
 *
 * @author lobas_av
 */
public class FontPropertyEditorTestNoManager extends FontPropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				false);
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
	 * No value for property.
	 */
	@Test
	public void test_textSource_noValue() throws Exception {
		Property property = new GenericPropertyNoValue(null, null, FontPropertyEditor.INSTANCE);
		assertNull(PropertyEditorTestUtils.getText(property));
		assertNull(PropertyEditorTestUtils.getClipboardSource(property));
	}

	@Test
	public void test_textSource_constructor_NORMAL() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Font(null, \"MS Shell Dlg\", 12, SWT.NORMAL)",
				"MS Shell Dlg 12",
				"new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.NORMAL)");
	}

	@Test
	public void test_textSource_constructor_BOLD() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Font(null, \"MS Shell Dlg\", 12, SWT.BOLD)",
				"MS Shell Dlg 12 BOLD",
				"new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD)");
	}

	@Test
	public void test_textSource_constructor_ITALIC() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Font(null, \"MS Shell Dlg\", 12, SWT.ITALIC)",
				"MS Shell Dlg 12 ITALIC",
				"new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.ITALIC)");
	}

	@Test
	public void test_textSource_constructor_BOLD_ITALIC() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Font(null, \"MS Shell Dlg\", 12, SWT.BOLD | SWT.ITALIC)",
				"MS Shell Dlg 12 BOLD ITALIC",
				"new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD | org.eclipse.swt.SWT.ITALIC)");
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
}