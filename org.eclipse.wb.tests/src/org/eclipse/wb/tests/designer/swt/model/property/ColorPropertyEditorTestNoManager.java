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

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.LocalResourceManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ColorPropertyEditor} without {@link LocalResourceManager}.
 *
 * @author scheglov_ke
 */
public class ColorPropertyEditorTestNoManager extends ColorPropertyEditorTest {
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
		GenericProperty property = new GenericPropertyNoValue(null, null, ColorPropertyEditor.INSTANCE);
		assertNull(PropertyEditorTestUtils.getText(property));
		assertNull(PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * System color using "id" - SWT field.
	 */
	@Test
	public void test_textSource_systemConstant() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"Display.getCurrent().getSystemColor(SWT.COLOR_RED)",
				"COLOR_RED",
				"org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_RED)");
	}

	/**
	 * System color using "id" - direct number.
	 */
	@Test
	public void test_textSource_systemNumber() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"Display.getCurrent().getSystemColor(3)",
				"COLOR_RED",
				"org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_RED)");
	}

	/**
	 * System color using "id" - bad id.
	 */
	@Test
	public void test_textSource_systemBad() throws Exception {
		try {
			assert_getText_getClipboardSource_forSource(
					"Display.getCurrent().getSystemColor(-1)",
					null,
					null);
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Color creation using constructor with separate <code>int</code> values.
	 */
	@Test
	public void test_getText_constructor_ints() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Color(null, 1, 2, 3)",
				"1,2,3",
				"new org.eclipse.swt.graphics.Color(null, 1, 2, 3)");
	}

	/**
	 * Color creation using constructor with RGB argument.
	 */
	@Test
	public void test_getText_constructor_RGB() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Color(null, new RGB(1, 2, 3))",
				"1,2,3",
				"new org.eclipse.swt.graphics.Color(null, 1, 2, 3)");
	}
}