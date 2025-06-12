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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.EnumerationValuesPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link EnumerationValuesPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class EnumerationValuesPropertyEditorTest extends SwingModelTest {
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
	// Bad
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_bad_notArray() throws Exception {
		prepareMyPanel("'notArrayValue'");
		check_bad();
	}

	@Test
	public void test_bad_notObjectArray() throws Exception {
		prepareMyPanel("new int[0]");
		check_bad();
	}

	@Test
	public void test_bad_isArray_notArrayValue() throws Exception {
		prepareMyPanel("new Object[]{'a', 'b'}");
		check_bad();
	}

	@Test
	public void test_bad_isArray_notMultiple3() throws Exception {
		prepareMyPanel("new Object[]{'a', null, '0', 'b'}");
		check_bad();
	}

	@Test
	public void test_bad_isArray_notStringObjectString_1() throws Exception {
		prepareMyPanel("new Object[]{null, null, '0'}");
		check_bad();
	}

	@Test
	public void test_bad_isArray_notStringObjectString_2() throws Exception {
		prepareMyPanel("new Object[]{'a', null, null}");
		check_bad();
	}

	@Test
	public void test_bad_isArray_notStringObjectString_3() throws Exception {
		prepareMyPanel("new Object[]{'a', null, '0', 'b', null, null}");
		check_bad();
	}

	/**
	 * Checks that prepared "MyPanel" has bad "enumerationValues" property, so ignored.
	 */
	private void check_bad() throws Exception {
		Property property = getValueProperty();
		// has property, but not our editor
		assertNotInstanceOf(EnumerationValuesPropertyEditor.class, property.getEditor());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_0() throws Exception {
		prepareMyPanel("new Object[]{'a', 1, '1', 'b', 2, '2'}");
		GenericProperty property = getValueProperty();
		PropertyEditor editor = property.getEditor();
		// test IValueSourcePropertyEditor
		{
			IValueSourcePropertyEditor sourceEditor = (IValueSourcePropertyEditor) editor;
			assertEquals(null, sourceEditor.getValueSource(0));
			assertEquals("1", sourceEditor.getValueSource(1));
			assertEquals("2", sourceEditor.getValueSource(2));
		}
		// no text
		assertEquals(null, getPropertyText(property));
		assertEquals(null, ((IClipboardSourceProvider) editor).getClipboardSource(property));
		// set value
		property.setValue(1);
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setValue(1);",
				"  }",
				"}");
		// check new state
		assertEquals("a", getPropertyText(property));
		assertEquals("1", ((IClipboardSourceProvider) editor).getClipboardSource(property));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares "MyPanel" class and its "MyPanelBeanInfo".
	 */
	private void prepareMyPanel(String attributeValue) throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setValue(Object value) {",
						"  }",
						"}"));
		String[] lines =
			{
					"import java.beans.*;",
					"public class MyPanelBeanInfo extends SimpleBeanInfo {",
					"  private PropertyDescriptor[] m_descriptors;",
					"  public MyPanelBeanInfo() {",
					"    try {",
					"      m_descriptors = new PropertyDescriptor[1];",
					"      m_descriptors[0] = new PropertyDescriptor('value', MyPanel.class, null, 'setValue');",
					"      m_descriptors[0].setValue('enumerationValues', " + attributeValue + ");",
					"    } catch (Throwable e) {",
					"    }",
					"  }",
					"  public PropertyDescriptor[] getPropertyDescriptors() {",
					"    return m_descriptors;",
					"  }",
			"}"};
		setFileContentSrc("test/MyPanelBeanInfo.java", getTestSource(lines));
		waitForAutoBuild();
	}

	private GenericProperty getValueProperty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// prepare property
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("value");
		assertNotNull(property);
		return property;
	}
}