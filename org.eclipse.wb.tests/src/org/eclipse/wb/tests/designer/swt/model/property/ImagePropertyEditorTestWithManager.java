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

import static org.eclipse.wb.internal.swt.model.property.editor.image.ImagePropertyEditor.getInvocationSource;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.jface.resource.ManagerContainerInfo;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImagePropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.LocalResourceManager;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests for {@link ImagePropertyEditor} with {@link LocalResourceManager}.
 *
 * @author lobas_av
 */
public class ImagePropertyEditorTestWithManager extends ImagePropertyEditorTest {
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
			assert_getText_getClipboardSource_forSource(
					"new Image(null, \"" + path + "\")",
					"File: " + path,
					getInvocationSource(shell(), null, '"' + path + '"'));
		} finally {
			file.delete();
		}
	}

	/**
	 * Image creation using constructor with input stream (over class resource).
	 */
	@Test
	public void test_textSource_image_over_classpath() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource(shell(), "{wbp_classTop}", "/javax/swing/plaf/basic/icons/JavaCup16.png"));
	}

	/**
	 * Image creation using constructor with input stream (over class resource).
	 */
	@Test
	public void test_textSource_image_over_classpath_OtherClass() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Image(null, java.lang.String.class.getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource(shell(), "{wbp_classTop}", "/javax/swing/plaf/basic/icons/JavaCup16.png"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code with LocalResourceManager
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Image creation using constructor with absolute file path.
	 */
	@Test
	public void test_textSource_absolutePath2() throws Exception {
		File file = createTempImage();
		CompositeInfo shell = shell();
		try {
			String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
			assert_getText_getClipboardSource_forSource2(
					getInvocationSource(shell, null, '"' + path + '"'),
					"File: " + path,
					getInvocationSource(shell, null, '"' + path + '"'));
		} finally {
			file.delete();
		}
	}

	/**
	 * Image creation using constructor with input stream (over class resource).
	 */
	@Test
	public void test_textSource_image_over_classpath2() throws Exception {
		CompositeInfo shell = shell();
		assert_getText_getClipboardSource_forSource2(
				getInvocationSource(shell, "getClass()", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""),
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource(shell, "{wbp_classTop}", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
	}

	/**
	 * Image creation using constructor with input stream (over class resource).
	 */
	@Test
	public void test_textSource_image_over_classpath_OtherClass2() throws Exception {
		CompositeInfo shell = shell();
		assert_getText_getClipboardSource_forSource2(
				getInvocationSource(shell, "java.lang.String.class", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""),
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				getInvocationSource(shell, "{wbp_classTop}", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
	}

	/**
	 * Checks the results of {@link ImagePropertyEditor#getText()} and
	 * {@link ImagePropertyEditor#getClipboardSource()} when image is set using given source.
	 */
	private void assert_getText_getClipboardSource_forSource2(String imageSource,
			String expectedText,
			String expectedClipboardSource) throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		waitForAutoBuild();
		// add ResourceManager
		ManagerContainerInfo.getResourceManagerInfo(shell);
		// set "image" property
		shell.addMethodInvocation("setImage(org.eclipse.swt.graphics.Image)", imageSource);
		shell.refresh();
		//
		Property property = shell.getPropertyByTitle("image");
		assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
		assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * The call to setImage() must occur AFTER the resource manager was created.
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
		shell.addMethodInvocation("setImage(org.eclipse.swt.graphics.Image)",
				getInvocationSource(shell, "java.lang.String.class", "\"/javax/swing/plaf/basic/icons/JavaCup16.png\""));
		shell.refresh();
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    createResourceManager();",
				"    setImage(localResourceManager.create(ImageDescriptor.createFromFile(String.class, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),this);",
				"  }",
				"}");
	}

	private CompositeInfo shell() throws Exception {
		return parseComposite("public class Test extends Shell {}");
	}
}