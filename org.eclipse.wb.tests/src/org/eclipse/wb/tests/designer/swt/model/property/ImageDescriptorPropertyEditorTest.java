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

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImageDescriptorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import java.io.File;

/**
 * Tests for {@link ImageDescriptorPropertyEditorTest}.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public abstract class ImageDescriptorPropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected final GenericProperty createImageDescriptorPropertyForSource(String source)
			throws Exception {
		// prepare component with ImageDescriptor property
		setFileContentSrc(
				"test/MyControl.java",
				getSourceDQ(
						"package test;",
						"",
						"import org.eclipse.swt.SWT;",
						"import org.eclipse.swt.widgets.*;",
						"import org.eclipse.jface.resource.*;",
						"",
						"public class MyControl extends Composite {",
						"  public MyControl(Composite composite, int style) {",
						"    super(composite, style);",
						"  }",
						"  public void setImageDescriptor(ImageDescriptor descriptor) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		m_waitForAutoBuild = true;
		CompositeInfo shell =
				(CompositeInfo) parseSource(
						"test",
						"Test.java",
						getSourceDQ(
								"package test;",
								"",
								"import org.eclipse.swt.SWT;",
								"import org.eclipse.swt.widgets.*;",
								"import org.eclipse.jface.resource.*;",
								"",
								"public class Test extends Shell {",
								"  public Test() {",
								"    MyControl control = new MyControl(this, SWT.NONE);",
								"  }",
								"}"));
		ControlInfo control = shell.getChildrenControls().get(0);
		shell.refresh();
		assertNoErrors(shell);
		// set property using "source"
		GenericProperty property = (GenericProperty) control.getPropertyByTitle("imageDescriptor");
		property.setExpression(source, Property.UNKNOWN_VALUE);
		assertNoErrors(shell);
		//
		return property;
	}

	/**
	 * Checks the results of {@link ImageDescriptorPropertyEditor#getText()} and
	 * {@link ImageDescriptorPropertyEditor#getClipboardSource()} when <code>ImageDescriptor</code> is
	 * set using given source.
	 */
	protected final void assert_getText_getClipboardSource_forSource(String source,
			String expectedText,
			String expectedClipboardSource) throws Exception {
		GenericProperty property = createImageDescriptorPropertyForSource(source);
		// check "text" and "clipboardSource"
		assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
		assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * Create blank image 1x1 and save to temporal file.
	 */
	protected static File createTempImage() throws Exception {
		// create temporal file
		File file = File.createTempFile("testcase", ".png");
		// create image
		Image image = new Image(null, 1, 1);
		// save image to disk
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{image.getImageData()};
		loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
		// clear resource
		image.dispose();
		//
		return file;
	}
}