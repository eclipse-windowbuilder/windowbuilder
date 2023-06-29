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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.DoubleObjectPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

/**
 * Test for {@link DoubleObjectPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class DoubleObjectPropertyEditorTest extends AbstractTextPropertyEditorTest {
	private static final DoubleObjectPropertyEditor EDITOR = DoubleObjectPropertyEditor.INSTANCE;

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
	/**
	 * Test for {@link DoubleObjectPropertyEditor#getText(Property)}.
	 */
	public void test_getText() throws Exception {
		assert_getText(null, EDITOR, Property.UNKNOWN_VALUE);
		assert_getText("null", EDITOR, null);
		assert_getText("12.3", EDITOR, Double.valueOf(12.3));
	}

	/**
	 * Test for {@link DoubleObjectPropertyEditor#getEditorText(Property)}.
	 */
	public void test_getEditorText() throws Exception {
		assert_getEditorText(null, EDITOR, Property.UNKNOWN_VALUE);
		assert_getEditorText("null", EDITOR, null);
		assert_getEditorText("12.3", EDITOR, Double.valueOf(12.3));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setEditorText()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link DoubleObjectPropertyEditor#setEditorText(Property, String)}.
	 */
	public void test_setEditorText_value() throws Exception {
		prepareDoublePanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, "12.3");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(12.3);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link DoubleObjectPropertyEditor#setEditorText(Property, String)}.
	 */
	public void test_setEditorText_null() throws Exception {
		prepareDoublePanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, "null");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo((Double) null);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link DoubleObjectPropertyEditor#setEditorText(Property, String)}.
	 */
	public void test_setEditorText_removeValue_emptyString() throws Exception {
		prepareDoublePanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(12.3);",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, "");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Test for {@link DoubleObjectPropertyEditor#setEditorText(Property, String)}.
	 */
	public void test_setEditorText_removeValue_whitespaceString() throws Exception {
		prepareDoublePanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(12.3);",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, " ");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Test for {@link DoubleObjectPropertyEditor#setEditorText(Property, String)}.
	 */
	public void test_setEditorText_invalidValue() throws Exception {
		prepareDoublePanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		//
		final Property property = panel.getPropertyByTitle("foo");
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				setTextEditorText(property, "notDouble");
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.useShell("foo");
				context.clickButton("OK");
			}
		});
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	private void prepareDoublePanel() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setFoo(Double foo) {",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
