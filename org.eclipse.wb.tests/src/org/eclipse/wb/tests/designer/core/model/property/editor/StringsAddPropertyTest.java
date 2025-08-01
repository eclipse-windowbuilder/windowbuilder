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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.configurable.StringsAddPropertyEditor;
import org.eclipse.wb.internal.core.model.property.configurable.StringsAddPropertyFactory;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringsAddPropertyFactory} and {@link StringsAddPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class StringsAddPropertyTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_0() throws Exception {
		createMyPanel();
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"      <method name='addItem'>",
						"        <parameter type='java.lang.String'/>",
						"      </method>",
						"  </methods>",
						"  <add-property id='stringsAdd' title='items'>",
						"    <parameter name='addMethod'>addItem</parameter>",
						"  </add-property>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    addItem('aaa');",
						"    addItem('bbb');",
						"  }",
						"}");
		panel.refresh();
		// prepare "items" property
		Property itemsProperty = panel.getPropertyByTitle("items");
		assertNotNull(itemsProperty);
		// initial state
		{
			assertEquals("[aaa,bbb]", getPropertyText(itemsProperty));
			assertTrue(itemsProperty.isModified());
			assertArrayEquals(new String[] { "aaa", "bbb" }, (String[]) itemsProperty.getValue());
		}
		// set new items
		{
			itemsProperty.setValue(new String[]{"000", "111", "222"});
			assertEditor(
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    addItem('000');",
					"    addItem('111');",
					"    addItem('222');",
					"  }",
					"}");
			assertEquals("[000,111,222]", getPropertyText(itemsProperty));
			assertTrue(itemsProperty.isModified());
			assertArrayEquals(new String[] { "000", "111", "222" }, (String[]) itemsProperty.getValue());
		}
	}

	@Test
	public void test_removeMethods() throws Exception {
		createMyPanel();
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"      <method name='addItem'>",
						"        <parameter type='java.lang.String'/>",
						"      </method>",
						"  </methods>",
						"  <add-property id='stringsAdd' title='items'>",
						"    <parameter name='addMethod'>addItem</parameter>",
						"    <parameter name='removeMethods'>clear() insertItem(java.lang.String,int)</parameter>",
						"  </add-property>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    clear();",
						"    addItem('aaa');",
						"    addItem('bbb');",
						"    insertItem('ccc', 1);",
						"  }",
						"}");
		panel.refresh();
		// prepare "items" property
		Property itemsProperty = panel.getPropertyByTitle("items");
		assertNotNull(itemsProperty);
		// initial state
		{
			assertEquals("[aaa,bbb]", getPropertyText(itemsProperty));
			assertTrue(itemsProperty.isModified());
			assertArrayEquals(new String[] { "aaa", "bbb" }, (String[]) itemsProperty.getValue());
		}
		// set new items
		{
			itemsProperty.setValue(new String[]{"000", "111", "222"});
			assertEditor(
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    addItem('000');",
					"    addItem('111');",
					"    addItem('222');",
					"  }",
					"}");
			assertEquals("[000,111,222]", getPropertyText(itemsProperty));
			assertTrue(itemsProperty.isModified());
			assertArrayEquals(new String[] { "000", "111", "222" }, (String[]) itemsProperty.getValue());
		}
	}

	private void createMyPanel() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void clear() {",
						"  }",
						"  public void addItem(String item) {",
						"  }",
						"  public void insertItem(String item, int index) {",
						"  }",
						"}"));
	}
}
