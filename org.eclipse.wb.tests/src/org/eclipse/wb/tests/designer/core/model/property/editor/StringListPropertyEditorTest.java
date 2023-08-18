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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StringListPropertyEditor;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.tests.common.PropertyWithTitle;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test for {@link StringListPropertyEditor}.
 *
 * @author sablin_aa
 */
public class StringListPropertyEditorTest extends AbstractTextPropertyEditorTest {
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
	// Configure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Top level class, all fields are valid. Parameters also containing special code
	 * <code>null</code>.
	 */
	@Test
	public void test_configure_valid() throws Exception {
		Map<String, Object> parameters = getEditorParameters();
		StringListPropertyEditor editor = createEditor(StringListPropertyEditor.class, parameters);
		assertEditorConfiguration(editor, parameters);
	}

	/**
	 * Parameter fail test.
	 */
	@Test
	public void test_configure_parameters() throws Exception {
		Map<String, Object> parameters = getEditorParameters();
		// remove conditions from parameters
		parameters.remove("strings");
		// test
		try {
			createEditor(StringListPropertyEditor.class, parameters);
			fail();
		} catch (AssertionFailedException e) {
			// OK
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link StringListPropertyEditor#getValueSource(Object)}.
	 */
	@Test
	public void test_getValueSource() throws Exception {
		Map<String, Object> parameters = getEditorParameters();
		StringListPropertyEditor editor = createEditor(StringListPropertyEditor.class, parameters);
		assertEquals("\"String_3\"", editor.getValueSource("String_3"));
	}

	/**
	 * Test for {@link StringListPropertyEditor#getClipboardSource(Object)}.
	 */
	@Test
	public void test_getClipboardSource() throws Exception {
		Map<String, Object> parameters = getEditorParameters();
		StringListPropertyEditor editor = createEditor(StringListPropertyEditor.class, parameters);
		assert_getClipboardSource("\"String_2\"", editor, new String("String_2"));
		assert_getClipboardSource(null, editor, null);
	}

	/**
	 * Test for {@link StringListPropertyEditor#getText(Object)}.
	 */
	@Test
	public void test_getText() throws Exception {
		Map<String, Object> parameters = getEditorParameters();
		StringListPropertyEditor editor = createEditor(StringListPropertyEditor.class, parameters);
		assert_getText("String_1", editor, new String("String_1"));
		assert_getText(null, editor, null);
		// user value
		String user_value = "user value";
		assert_getText(user_value, editor, user_value);
	}

	/**
	 * Test for ignore case parameter.
	 */
	@Test
	public void test_ignoreCase() throws Exception {
		Map<String, Object> parameters = getEditorParameters();
		// ignore case mode
		{
			parameters.put("ignoreCase", new String("true"));
			StringListPropertyEditor editor = createEditor(StringListPropertyEditor.class, parameters);
			assert_getText("String_1", editor, new String("STRING_1"));
		}
	}

	/**
	 * Test that {@link StringListPropertyEditor} can work not only with {@link GenericProperty}, but
	 * also with simple {@link Property}.
	 */
	@Test
	public void test_setValue_simpleProperty() throws Exception {
		StringListPropertyEditor propertyEditor = new StringListPropertyEditor();
		propertyEditor.configure(new String[]{"A", "B", "C"});
		// prepare property
		final AtomicReference<Object> valueSet = new AtomicReference<>();
		Property property = new PropertyWithTitle(propertyEditor, "test") {
			@Override
			public void setValue(Object value) throws Exception {
				valueSet.set(value);
			}
		};
		// select item, so set value
		setComboPropertyValue(property, 1);
		assertSame("B", valueSet.get());
	}

	/**
	 * Test for "defaultValue"
	 */
	@Test
	public void test_defaultValue() throws Exception {
		// create contents
		setJavaContentSrc("test", "MyButton", new String[]{
				"import javax.swing.JButton;",
				"class MyButton extends JButton {",
				"  public void setSelect(java.lang.String value) {",
				"  }",
		"}"}, new String[]{
				"<?xml version='1.0' encoding='UTF-8'?>",
				"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
				"  <property id='setSelect(java.lang.String)'>",
				"    <editor id='stringList'>",
				"      <parameter name='ignoreCase'>false</parameter>",
				"      <parameter-list name='strings'>default</parameter-list>",
				"      <parameter-list name='strings'>string</parameter-list>",
				"      <parameter-list name='strings'>value</parameter-list>",
				"    </editor>",
				"    <defaultValue value=\"'default'\"/>",
				"  </property>",
		"</component>"});
		waitForAutoBuild();
		//
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    MyButton button = new MyButton();",
						"    button.setSelect('string');",
						"    add(button);",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Property property = button.getPropertyByTitle("select");
		// initial value
		assertEquals(property.getValue(), "string");
		// check default
		assertEquals(((GenericProperty) property).getDefaultValue(), "default");
		// set "zero" item, with default value
		setComboPropertyValue(property, 0);
		assertEquals(property.getValue(), "default");
		// check source
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    MyButton button = new MyButton();",
				"    add(button);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<String, Object> getEditorParameters() {
		//<editor id="stringList">
		//	<parameter name="ignoreCase">false</parameter>
		//	<parameter-list name="strings">String_1</parameter-list>
		//	<parameter-list name="strings">String_2</parameter-list>
		//	<parameter-list name="strings">String_3</parameter-list>
		//</editor>
		HashMap<String, Object> params = Maps.newHashMap();
		params.put("ignoreCase", new String("false"));
		params.put("strings", Lists.newArrayList("String_1", "String_2", "String_3"));
		return params;
	}

	/**
	 * Asserts that given {@link StringListPropertyEditor} has expected configuration.
	 */
	@SuppressWarnings("unchecked")
	private void assertEditorConfiguration(StringListPropertyEditor editor,
			Map<String, Object> parameters) throws Exception {
		assertContainsOnly(editor, "m_strings", (List<String>) parameters.get("strings"));
		assertFalse((Boolean) getFieldValue(editor, "m_ignoreCase"));
	}
}
