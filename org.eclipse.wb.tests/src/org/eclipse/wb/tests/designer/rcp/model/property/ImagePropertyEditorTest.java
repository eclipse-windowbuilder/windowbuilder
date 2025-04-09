/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.rcp.model.property;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImagePropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;
import org.eclipse.wb.tests.designer.swt.model.property.PropertyEditorTestUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import java.io.File;

/**
 * Tests for {@link ImagePropertyEditor}.
 *
 * @author lobas_av
 */
public abstract class ImagePropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected final GenericProperty createImagePropertyForSource(String imageSource) throws Exception {
		m_waitForAutoBuild = true;
		ShellInfo shell = (ShellInfo) parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setImage(" + imageSource + ");",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
		// we should have value for "image"
		{
			Image image = shell.getWidget().getImage();
			assertNotNull(image);
		}
		//
		return (GenericProperty) shell.getPropertyByTitle("image");
	}

	/**
	 * Checks the results of {@link ImagePropertyEditor#getText()} and
	 * {@link ImagePropertyEditor#getClipboardSource()} when Image is set using given source.
	 */
	protected final void assert_getText_getClipboardSource_forSource(String imageSource,
			String expectedText,
			String expectedClipboardSource) throws Exception {
		Property property = createImagePropertyForSource(imageSource);
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