/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.util;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.common.PropertyWithTitle;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.function.Predicate;

/**
 * Tests for {@link PropertyUtils}.
 *
 * @author scheglov_ke
 */
public class PropertyUtilsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyUtils#getText(Property)}.
	 */
	@Test
	public void test_getText() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton('my text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// "enabled" property has no text presentation
		{
			Property property = button.getPropertyByTitle("enabled");
			assertNotNull(property);
			assertNull(PropertyUtils.getText(property));
		}
		// "text" property has text presentation
		{
			Property property = button.getPropertyByTitle("text");
			assertNotNull(property);
			assertEquals("my text", PropertyUtils.getText(property));
		}
	}

	/**
	 * Test for {@link PropertyUtils#getTitles(Property[])}.
	 */
	@Test
	public void test_getTitles_asArray() throws Exception {
		Property property_1 = new PropertyWithTitle("a");
		Property property_2 = new PropertyWithTitle("b");
		Assertions.assertThat(PropertyUtils.getTitles(property_1, property_2)).isEqualTo(new String[]{"a", "b"});
	}

	/**
	 * Test for {@link PropertyUtils#getTitles(List)}.
	 */
	@Test
	public void test_getTitles_asList() throws Exception {
		Property property_1 = new PropertyWithTitle("a");
		Property property_2 = new PropertyWithTitle("b");
		List<Property> properties = List.of(property_1, property_2);
		List<String> expectedTitles = List.of("a", "b");
		Assertions.assertThat(PropertyUtils.getTitles(properties)).isEqualTo(expectedTitles);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getPropertyByTitle()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyUtils#getByTitle(Property[], String)}.
	 */
	@Test
	public void test_getByTitle_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton myButton = new JButton('text');",
						"    add(myButton);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Property[] properties = button.getProperties();
		//
		assertNotNull(PropertyUtils.getByTitle(properties, "enabled"));
		assertNull(PropertyUtils.getByTitle(properties, "noSuchProperty"));
	}

	/**
	 * Test for {@link PropertyUtils#getByTitle(List, String)}.
	 */
	@Test
	public void test_getByTitle_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton myButton = new JButton('text');",
						"    add(myButton);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		List<Property> properties = List.of(button.getProperties());
		//
		assertNotNull(PropertyUtils.getByTitle(properties, "enabled"));
		assertNull(PropertyUtils.getByTitle(properties, "noSuchProperty"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getPropertyByPath()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyUtils#getByPath(JavaInfo, String)}.
	 */
	@Test
	public void test_getByPath_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// top level property
		{
			Property property = PropertyUtils.getByPath(button, "enabled");
			assertNotNull(property);
			assertEquals("enabled", property.getTitle());
		}
		// property in complex property
		{
			Property property = PropertyUtils.getByPath(button, "Constructor/text");
			assertNotNull(property);
			assertEquals("text", property.getTitle());
		}
		// attempt to ask child of simple property
		{
			Property property = PropertyUtils.getByPath(button, "Constructor/text/noChildren");
			assertNull(property);
		}
		// no such top level property
		{
			Property property = PropertyUtils.getByPath(button, "noSuchProperty");
			assertNull(property);
		}
		// no such property in complex property
		{
			Property property = PropertyUtils.getByPath(button, "Constructor/noSuchProperty");
			assertNull(property);
		}
	}

	/**
	 * Test for {@link PropertyUtils#getByPath(List, String)}.
	 */
	@Test
	public void test_getByPath_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		List<Property> properties = Lists.newArrayList(button.getProperties());
		// top level property
		{
			Property property = PropertyUtils.getByPath(properties, "enabled");
			assertNotNull(property);
			assertEquals("enabled", property.getTitle());
		}
		// property in complex property
		{
			Property property = PropertyUtils.getByPath(properties, "Constructor/text");
			assertNotNull(property);
			assertEquals("text", property.getTitle());
		}
		// no such top level property
		{
			Property property = PropertyUtils.getByPath(properties, "noSuchProperty");
			assertNull(property);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getChildren()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyUtils#getChildren(Property)}.
	 */
	@Test
	public void test_getChildren() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton('text', null);",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		Property constructorProperty = PropertyUtils.getByPath(button, "Constructor");
		Property[] subProperties = PropertyUtils.getChildren(constructorProperty);
		Assertions.assertThat(subProperties).hasSize(2);
		assertEquals("text", subProperties[0].getTitle());
		assertEquals("icon", subProperties[1].getTitle());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Filter
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyUtils#getExcludeByTitlePredicate(String...)}.
	 */
	@Test
	public void test_getExcludeByTitlePredicate() throws Exception {
		Predicate<Property> predicate = PropertyUtils.getExcludeByTitlePredicate("a", "c");
		{
			Property property = new PropertyWithTitle("a");
			assertFalse(predicate.test(property));
		}
		{
			Property property = new PropertyWithTitle("b");
			assertTrue(predicate.test(property));
		}
		{
			Property property = new PropertyWithTitle("c");
			assertFalse(predicate.test(property));
		}
	}

	/**
	 * Test for {@link PropertyUtils#getIncludeByTitlePredicate(String...)}.
	 */
	@Test
	public void test_getIncludeByTitlePredicate() throws Exception {
		Predicate<Property> predicate = PropertyUtils.getIncludeByTitlePredicate("a", "c");
		{
			Property property = new PropertyWithTitle("a");
			assertTrue(predicate.test(property));
		}
		{
			Property property = new PropertyWithTitle("b");
			assertFalse(predicate.test(property));
		}
		{
			Property property = new PropertyWithTitle("c");
			assertTrue(predicate.test(property));
		}
	}

	/**
	 * Test for {@link PropertyUtils#getProperties(JavaInfo, Predicate)}.
	 */
	@Test
	public void test_getProperties_withPredicate() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		//  JPanel has "enabled" property
		{
			Property[] properties = panel.getProperties();
			String[] titles = PropertyUtils.getTitles(properties);
			Assertions.assertThat(titles).contains("enabled");
		}
		// use predicate that excludes "enabled"
		{
			Predicate<Property> predicate = PropertyUtils.getExcludeByTitlePredicate("enabled");
			List<Property> properties = PropertyUtils.getProperties(panel, predicate);
			List<String> titles = PropertyUtils.getTitles(properties);
			Assertions.assertThat(titles).doesNotContain("enabled").contains("background");
		}
	}

	/**
	 * Test for {@link PropertyUtils#getExcludeByTitlePredicate(JavaInfo, String)}.
	 */
	@Test
	public void test_getExcludeByTitlePredicate_forParameter() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyPanel extends JPanel {",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='exclude-parameter'>a c</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		Predicate<Property> predicate =
				PropertyUtils.getExcludeByTitlePredicate(panel, "exclude-parameter");
		{
			Property property = new PropertyWithTitle("a");
			assertFalse(predicate.test(property));
		}
		{
			Property property = new PropertyWithTitle("b");
			assertTrue(predicate.test(property));
		}
		{
			Property property = new PropertyWithTitle("c");
			assertFalse(predicate.test(property));
		}
	}

	/**
	 * Test for {@link PropertyUtils#getProperties_excludeByParameter(JavaInfo, String)}.
	 */
	@Test
	public void test_getProperties_excludeByParameter() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyPanel extends JPanel {",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='exclude-parameter'>enabled visible</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		//  JPanel has "enabled" and "visible" properties
		{
			Property[] properties = panel.getProperties();
			String[] titles = PropertyUtils.getTitles(properties);
			Assertions.assertThat(titles).contains("enabled", "visible");
		}
		// but we use predicate that excludes "enabled" and "visible"
		{
			List<Property> properties =
					PropertyUtils.getProperties_excludeByParameter(panel, "exclude-parameter");
			List<String> titles = PropertyUtils.getTitles(properties);
			Assertions.assertThat(titles).doesNotContain("enabled", "visible").contains("background");
		}
	}

	/**
	 * Test for {@link PropertyUtils#filterProperties(List, Predicate)}.
	 */
	@Test
	public void test_filterProperties() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		//  JPanel has "enabled" property
		{
			Property[] properties = panel.getProperties();
			String[] titles = PropertyUtils.getTitles(properties);
			Assertions.assertThat(titles).contains("enabled");
		}
		// use predicate that includes only "enabled"
		{
			List<Property> properties = Lists.newArrayList(panel.getProperties());
			Assertions.assertThat(properties.size()).isGreaterThan(10);
			//
			PropertyUtils.filterProperties(
					properties,
					PropertyUtils.getIncludeByTitlePredicate("enabled"));
			List<String> titles = PropertyUtils.getTitles(properties);
			Assertions.assertThat(titles).hasSize(1).contains("enabled");
		}
	}
}
