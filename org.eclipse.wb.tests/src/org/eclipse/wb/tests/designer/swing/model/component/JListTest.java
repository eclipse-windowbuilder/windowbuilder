/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.property.editor.models.list.ListModelPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Tests for {@link JList} support.
 *
 * @author scheglov_ke
 */
public class JListTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parsing() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JList list = new JList();",
				"    add(list);",
				"    list.setModel(new AbstractListModel() {",
				"      String[] values = {'111', '222', '333'};",
				"      public int getSize() {",
				"        return values.length;",
				"      }",
				"      public Object getElementAt(int i) {",
				"        return values[i];",
				"      }",
				"    });",
				"  }",
				"}");
		refresh();
		ComponentInfo listInfo = getJavaInfoByName("list");
		JList<?> listObject = (JList<?>) listInfo.getObject();
		// validate model
		{
			ListModel<?> model = listObject.getModel();
			assertNotNull(model);
			assertEquals(3, model.getSize());
			assertEquals("111", model.getElementAt(0));
			assertEquals("222", model.getElementAt(1));
			assertEquals("333", model.getElementAt(2));
		}
		// validate items from editor
		{
			Property modelProperty = listInfo.getPropertyByTitle("model");
			String[] actualItems = ListModelPropertyEditor.getItems(modelProperty);
			assertArrayEquals(new String[] { "111", "222", "333" }, actualItems);
		}
	}

	/**
	 * Field "values" exists in model, but we can not evaluate it.
	 */
	@Test
	public void test_nullModelValues() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JList list = new JList();",
				"    add(list);",
				"    list.setModel(new AbstractListModel() {",
				"      String[] values = null;",
				"      public int getSize() {",
				"        return values.length;",
				"      }",
				"      public Object getElementAt(int i) {",
				"        return values[i];",
				"      }",
				"    });",
				"  }",
				"}");
		refresh();
		ComponentInfo listInfo = getJavaInfoByName("list");
		JList<?> listObject = (JList<?>) listInfo.getObject();
		// no items in model
		{
			ListModel<?> model = listObject.getModel();
			assertNotNull(model);
			assertEquals(0, model.getSize());
		}
	}

	/**
	 * {@link JList#setSelectedIndex(int)} should be after {@link JList#setModel(ListModel)}.
	 */
	@Test
	public void test_setSelectedIndex() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JList list = new JList();",
				"    add(list);",
				"    list.setModel(new AbstractListModel() {",
				"      String[] values = {'111', '222', '333'};",
				"      public int getSize() {",
				"        return values.length;",
				"      }",
				"      public Object getElementAt(int i) {",
				"        return values[i];",
				"      }",
				"    });",
				"  }",
				"}");
		refresh();
		ComponentInfo listInfo = getJavaInfoByName("list");
		//
		listInfo.getPropertyByTitle("selectedIndex").setValue(1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JList list = new JList();",
				"    add(list);",
				"    list.setModel(new AbstractListModel() {",
				"      String[] values = {'111', '222', '333'};",
				"      public int getSize() {",
				"        return values.length;",
				"      }",
				"      public Object getElementAt(int i) {",
				"        return values[i];",
				"      }",
				"    });",
				"    list.setSelectedIndex(1);",
				"  }",
				"}");
	}

	@Test
	public void test_modelEditor() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JList list = new JList();",
				"    add(list);",
				"  }",
				"}");
		refresh();
		//
		ComponentInfo listInfo = getJavaInfoByName("list");
		Property modelProperty = listInfo.getPropertyByTitle("model");
		// no items initially
		assertEquals(0, ListModelPropertyEditor.getItems(modelProperty).length);
		// set new items
		ListModelPropertyEditor.setItems(modelProperty, new String[]{"aaa", "bbb"});
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JList list = new JList();",
				"    list.setModel(new AbstractListModel() {",
				"      String[] values = new String[] {'aaa', 'bbb'};",
				"      public int getSize() {",
				"        return values.length;",
				"      }",
				"      public Object getElementAt(int index) {",
				"        return values[index];",
				"      }",
				"    });",
				"    add(list);",
				"  }",
				"}");
	}
}
