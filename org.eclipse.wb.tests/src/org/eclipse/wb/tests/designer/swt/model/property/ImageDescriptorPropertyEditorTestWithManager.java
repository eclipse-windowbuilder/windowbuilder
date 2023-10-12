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

import static org.eclipse.wb.internal.swt.model.property.editor.image.ImageDescriptorPropertyEditor.getInvocationSource;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImageDescriptorPropertyEditor;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests for {@link ImageDescriptorPropertyEditor} with <code>ResourceManager</code>.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public class ImageDescriptorPropertyEditorTestWithManager extends ImageDescriptorPropertyEditorTest {
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
	 * No value for property.
	 */
	@Test
	public void test_textSource_noValue() throws Exception {
		Property property =
				new GenericPropertyNoValue(null, null, ImageDescriptorPropertyEditor.INSTANCE);
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
	 * Test for {@link ImageDescriptor#createFromFile(Class, String)} with <code>null</code> as
	 * location, so absolute path.
	 */
	@Test
	public void test_textSource_absolutePath() throws Exception {
		File file = createTempImage();
		try {
			String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
			assert_getText_getClipboardSource_forSource(
					getInvocationSource(null, '"' + path + '"'),
					"File: " + path,
					getInvocationSource(null, '"' + path + '"'));
		} finally {
			file.delete();
		}
	}

	/**
	 * Test for {@link ImageDescriptor#createFromFile(Class, String)} with this {@link Class} as
	 * location.
	 */
	@Test
	public void test_textSource_image_over_classpath() throws Exception {
		assert_getText_getClipboardSource_forSource(
				getInvocationSource("getClass()", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""),
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource("{wbp_classTop}", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
	}

	/**
	 * Test for {@link ImageDescriptor#createFromFile(Class, String)} with some other {@link Class} as
	 * location.
	 */
	@Test
	public void test_textSource_image_over_classpath_OtherClass() throws Exception {
		assert_getText_getClipboardSource_forSource(
				getInvocationSource("String.class", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""),
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource("{wbp_classTop}", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code with ResourceManager
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for <code>ImageDescriptor.createFrom(null, absolutePath)</code>.
	 */
	@Test
	public void test_textSource_absolutePath2() throws Exception {
		File file = createTempImage();
		try {
			String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
			assert_getText_getClipboardSource_forSource(
					getInvocationSource(null, '"' + path + '"'),
					"File: " + path,
					getInvocationSource(null, '"' + path + '"'));
		} finally {
			file.delete();
		}
	}

	/**
	 * Test for <code>ImageDescriptor.createFrom(Class, resourcePath)</code>.
	 */
	@Test
	public void test_textSource_image_over_classpath2() throws Exception {
		assert_getText_getClipboardSource_forSource(
				getInvocationSource("getClass()", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""),
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource("{wbp_classTop}", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
	}

	/**
	 * Test for <code>ImageDescriptor.createFrom(Class, resourcePath)</code>, some other
	 * {@link Class} as location.
	 */
	@Test
	public void test_textSource_image_over_classpath_OtherClass2() throws Exception {
		assert_getText_getClipboardSource_forSource(
				getInvocationSource("String.class", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""),
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource("{wbp_classTop}", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
	}
}