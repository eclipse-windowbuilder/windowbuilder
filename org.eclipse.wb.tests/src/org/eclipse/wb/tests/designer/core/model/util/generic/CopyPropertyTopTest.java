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
package org.eclipse.wb.tests.designer.core.model.util.generic;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.generic.CopyPropertyTopSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

/**
 * Test for {@link CopyPropertyTopSupport}.
 *
 * @author scheglov_ke
 */
public class CopyPropertyTopTest extends SwingModelTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_copyExisting() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(String text) {",
						"    setText(text);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='copyPropertyTop from=Constructor/text to=MyButtonText'/>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new MyButton('My text'));",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// test MyButtonText property
		Property property = button.getPropertyByTitle("MyButtonText");
		assertNotNull(property);
		assertSame(PropertyCategory.NORMAL, property.getCategory());
		assertEquals("My text", property.getValue());
		// next time same Property should be returned
		assertSame(property, button.getPropertyByTitle("MyButtonText"));
	}

	@Test
	public void test_ignoreNotExisting() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyButton extends JButton {",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='copyPropertyTop from=noSuchProperty to=anyTitle'/>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new MyButton());",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// test "anyTitle" property
		Property property = button.getPropertyByTitle("anyTitle");
		assertNull(property);
	}
}