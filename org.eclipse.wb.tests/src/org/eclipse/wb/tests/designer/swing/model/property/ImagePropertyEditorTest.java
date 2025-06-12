/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.JFrameInfo;
import org.eclipse.wb.internal.swing.model.property.editor.icon.ImagePropertyEditor;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.resources.IFile;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ImagePropertyEditor}.
 *
 * @author scheglov_ke
 */
public class ImagePropertyEditorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// getText()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getText_noIimage() throws Exception {
		assertImagePropertyText(null, new String[]{
				"public class Test extends JFrame {",
				"  public Test() {",
				"  }",
		"}"});
	}

	@Test
	public void test_getText_null() throws Exception {
		assertImagePropertyText("(null)", new String[]{
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setIconImage(null);",
				"  }",
		"}"});
	}

	@Test
	public void test_getText_fromFile() throws Exception {
		IFile imageFile = TestUtils.createImagePNG(m_testProject, "1.png", 10, 10);
		try {
			String absoluteImagePath = imageFile.getLocation().toPortableString();
			assertImagePropertyText("File: " + absoluteImagePath, new String[]{
					"public class Test extends JFrame {",
					"  public Test() {",
					"    setIconImage(Toolkit.getDefaultToolkit().getImage(\"" + absoluteImagePath + "\"));",
					"  }",
			"}"});
		} finally {
			imageFile.delete(true, null);
		}
	}

	@Disabled
	@Test
	public void test_getText_Class_getResource_1() throws Exception {
		assertImagePropertyText(
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				new String[]{
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setIconImage(Toolkit.getDefaultToolkit().getImage(Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
						"  }",
				"}"});
	}

	@Disabled
	@Test
	public void test_getText_Class_getResource_2() throws Exception {
		assertImagePropertyText(
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				new String[]{
						"public class Test extends JFrame {",
						"  public Test() {",
						"    Image icon = Toolkit.getDefaultToolkit().getImage(Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"));",
						"    setIconImage(icon);",
						"  }",
				"}"});
	}

	@Disabled
	@Test
	public void test_getText_Class_getResource_3() throws Exception {
		assertImagePropertyText(
				"Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
				new String[]{
						"public class Test extends JFrame {",
						"  public Test() {",
						"    java.net.URL url = Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\");",
						"    Image icon = Toolkit.getDefaultToolkit().getImage(url);",
						"    setIconImage(icon);",
						"  }",
				"}"});
	}

	private void assertImagePropertyText(String expectedText, String[] lines) throws Exception {
		m_waitForAutoBuild = true;
		JFrameInfo frame = (JFrameInfo) parseContainer(lines);
		frame.refresh();
		assertNoErrors(frame);
		// property
		Property iconProperty = frame.getPropertyByTitle("iconImage");
		assertEquals(expectedText, getPropertyText(iconProperty));
	}
}
