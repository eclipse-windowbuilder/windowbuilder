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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 */
public class GenericPropertyDescriptionTest extends SwingModelTest {
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
	// getType()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link GenericPropertyDescription} has type of property, for setter.
	 */
	@Test
	public void test_setter_getType() throws Exception {
		setFileContentSrc(
				"test/MyObject.java",
				getSourceDQ(
						"package test;",
						"public class MyObject {",
						"  public void setA(int value) {",
						"  }",
						"  public void setB(double value) {",
						"  }",
						"  public void setC(String value) {",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		ComponentDescription description = getMyObjectDescription();
		assertPropertyType(description, "setA(int)", int.class);
		assertPropertyType(description, "setB(double)", double.class);
		assertPropertyType(description, "setC(java.lang.String)", String.class);
	}

	/**
	 * Test that {@link GenericPropertyDescription} has type of property, for field.
	 */
	@Test
	public void test_field_getType() throws Exception {
		setFileContentSrc(
				"test/MyObject.java",
				getSourceDQ(
						"package test;",
						"public class MyObject {",
						"  public int a;",
						"  public double b;",
						"  public String c;",
						"}"));
		waitForAutoBuild();
		//
		ComponentDescription description = getMyObjectDescription();
		assertPropertyType(description, "a", int.class);
		assertPropertyType(description, "b", double.class);
		assertPropertyType(description, "c", String.class);
	}

	private static void assertPropertyType(ComponentDescription description,
			String id,
			Class<?> expectedType) {
		GenericPropertyDescription property = description.getProperty(id);
		assertSame(expectedType, property.getType());
	}

	private ComponentDescription getMyObjectDescription() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		// load description
		return ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyObject");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "preferred" JavaBeanAttribute
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If property descriptor has "preferred == Boolean.TRUE", then we should use for corresponding
	 * {@link GenericPropertyDescription} category {@link PropertyCategory#PREFERRED}.
	 * <p>
	 * http://javadude.com/articles/javabeanattributes.html
	 */
	@Test
	public void test_JavaBean_preferred() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  private String m_value;",
						"  public String getValue() {",
						"    return m_value;",
						"  }",
						"  public void setValue(String value) {",
						"    m_value = value;",
						"  }",
						"}"));
		// create test BeanInfo
		setFileContentSrc(
				"test/MyPanelBeanInfo.java",
				getTestSource(new String[]{
						"import java.beans.*;",
						"public class MyPanelBeanInfo extends SimpleBeanInfo {",
						"  private PropertyDescriptor[] m_descriptors;",
						"  public MyPanelBeanInfo() {",
						"    try {",
						"      BeanInfo info = Introspector.getBeanInfo(JPanel.class);",
						"      PropertyDescriptor[] descriptors = info.getPropertyDescriptors();",
						"      m_descriptors = new PropertyDescriptor[descriptors.length + 1];",
						"      System.arraycopy(descriptors, 0, m_descriptors, 0, descriptors.length);",
						"      m_descriptors[descriptors.length] = new PropertyDescriptor('value', MyPanel.class, 'getValue', 'setValue');",
						"      m_descriptors[descriptors.length].setPreferred(true);",
						"    } catch (Throwable e) {",
						"    }",
						"  }",
						"  public PropertyDescriptor[] getPropertyDescriptors() {",
						"    return m_descriptors;",
						"  }",
				"}"}));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("value");
		assertNotNull(property);
		assertSame(PropertyCategory.PREFERRED, property.getCategory());
	}
}
