/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
import org.eclipse.wb.internal.core.model.property.editor.BooleanObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Point;

import org.junit.Test;

/**
 * Test for {@link BooleanPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class BooleanObjectPropertyEditorTest extends AbstractTextPropertyEditorTest {
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
	 * Test for {@link BooleanObjectPropertyEditor#doubleClick(Property, Point)}.
	 */
	@Test
	public void test_doubleClick() throws Exception {
		prepareBooleanPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// prepare property
		Property property = panel.getPropertyByTitle("foo");
		BooleanObjectPropertyEditor editor = (BooleanObjectPropertyEditor) property.getEditor();
		// unknown -> true
		editor.doubleClick(property, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(true);",
				"  }",
				"}");
		// true -> false
		editor.doubleClick(property, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(false);",
				"  }",
				"}");
		// false -> true
		editor.doubleClick(property, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(true);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link BooleanObjectPropertyEditor#doubleClick(Property, Point)}.
	 */
	@Test
	public void test_doubleClick_fromNull() throws Exception {
		prepareBooleanPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(null);",
						"  }",
						"}");
		panel.refresh();
		// prepare property
		Property property = panel.getPropertyByTitle("foo");
		BooleanObjectPropertyEditor editor = (BooleanObjectPropertyEditor) property.getEditor();
		// null -> true
		editor.doubleClick(property, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(true);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// activate()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link BooleanObjectPropertyEditor#activate(PropertyTable, Property, Point)}.
	 */
	@Test
	public void test_activate_usingKeyboard() throws Exception {
		prepareBooleanPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(true);",
						"  }",
						"}");
		panel.refresh();
		// prepare property
		Property property = panel.getPropertyByTitle("foo");
		BooleanObjectPropertyEditor editor = (BooleanObjectPropertyEditor) property.getEditor();
		// true -> false
		boolean activated = editor.activate(null, property, null);
		assertFalse(activated);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(false);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link BooleanObjectPropertyEditor#activate(PropertyTable, Property, Point)}.
	 */
	@Test
	public void test_activate_clickOnCheckBox() throws Exception {
		prepareBooleanPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(true);",
						"  }",
						"}");
		panel.refresh();
		// prepare property
		Property property = panel.getPropertyByTitle("foo");
		BooleanObjectPropertyEditor editor = (BooleanObjectPropertyEditor) property.getEditor();
		// true -> false
		boolean activated = editor.activate(null, property, new Point(10, 0));
		assertFalse(activated);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(false);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link BooleanObjectPropertyEditor#activate(PropertyTable, Property, Point)}.
	 */
	@Test
	public void test_activate_justClickToSelect() throws Exception {
		prepareBooleanPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(true);",
						"  }",
						"}");
		panel.refresh();
		// prepare property
		Property property = panel.getPropertyByTitle("foo");
		BooleanObjectPropertyEditor editor = (BooleanObjectPropertyEditor) property.getEditor();
		// true -> false
		boolean activated = editor.activate(null, property, new Point(100, 0));
		assertFalse(activated);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(true);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void prepareBooleanPanel() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setFoo(Boolean foo) {",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
