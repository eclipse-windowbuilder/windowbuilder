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
import org.eclipse.wb.internal.swt.model.property.editor.image.ImagePropertyEditor;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.LocalResourceManager;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Tests for {@link ImagePropertyEditor} without {@link LocalResourceManager}.
 *
 * @author lobas_av
 */
public class ImagePropertyEditorTestNoManager extends ImagePropertyEditorTest {
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
		Property property = new GenericPropertyNoValue(null, null, ImagePropertyEditor.INSTANCE);
		assertNull(PropertyEditorTestUtils.getText(property));
		assertNull(PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * "null" value for property.
	 */
	@Test
	public void test_textSource_nullValue() throws Exception {
		assert_getText_getClipboardSource_forSource("null", "(null)", "null");
	}

	/**
	 * Image creation using constructor with absolute file path.
	 */
	@Test
	public void test_textSource_absolutePath() throws Exception {
		File file = createTempImage();
		try {
			String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
			assert_getText_getClipboardSource_forSource("new Image(null, \"" + path + "\")", "File: "
					+ path, "new org.eclipse.swt.graphics.Image(null, \"" + path + "\")");
		} finally {
			file.delete();
		}
	}

	/**
	 * Image creation using constructor with input stream (over class resource).
	 */
	@Disabled
	@Test
	public void test_textSource_image_over_classpath() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				"new org.eclipse.swt.graphics.Image(null, {wbp_classTop}.getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))");
	}

	/**
	 * Image creation using constructor with input stream (over class resource).
	 */
	@Disabled
	@Test
	public void test_textSource_image_over_classpath_OtherClass() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Image(null, java.lang.String.class.getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				"new org.eclipse.swt.graphics.Image(null, {wbp_classTop}.getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))");
	}
}