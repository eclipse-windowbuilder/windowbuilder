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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.DisplayExpressionPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DisplayExpressionPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class DisplayExpressionPropertyEditorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_noExpression() throws Exception {
		createMyPanel();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// prepare "myFoo" property
		Property fooProperty = panel.getPropertyByTitle("myFoo");
		assertNotNull(fooProperty);
		assertInstanceOf(DisplayExpressionPropertyEditor.class, fooProperty.getEditor());
		// no expression, so no text
		assertNull(getPropertyText(fooProperty));
	}

	@Test
	public void test_hasExpression() throws Exception {
		createMyPanel();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    foo(1 + 2);",
						"  }",
						"}");
		panel.refresh();
		// prepare "myFoo" property
		Property fooProperty = panel.getPropertyByTitle("myFoo");
		assertNotNull(fooProperty);
		assertInstanceOf(DisplayExpressionPropertyEditor.class, fooProperty.getEditor());
		// we should get expression, not just result
		assertEquals("1 + 2", getPropertyText(fooProperty));
	}

	private void createMyPanel() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void foo(int value) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"      <method name='foo'>",
						"        <parameter type='int'>",
						"          <editor id='displayExpression'/>",
						"        </parameter>",
						"      </method>",
						"  </methods>",
						"  <method-single-property title='myFoo' method='foo(int)'/>",
						"</component>"));
		waitForAutoBuild();
	}
}
