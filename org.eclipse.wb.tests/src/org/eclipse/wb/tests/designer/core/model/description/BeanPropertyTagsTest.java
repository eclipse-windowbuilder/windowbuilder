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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesFlaggedRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertyTagRule;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link StandardBeanPropertyTagRule}, {@link StandardBeanPropertiesFlaggedRule}, its
 * subclasses and applications.
 * 
 * @author scheglov_ke
 */
public class BeanPropertyTagsTest extends SwingModelTest {
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
  // Property: standard-bean-properties-*
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertPropertyCategory(PropertyCategory category,
      ComponentDescription description,
      String propertyId) {
    assertSame(category, description.getProperty(propertyId).getCategory());
  }

  /**
   * We can use tag "properties-/preferred/advanced/hidden" elements to change
   * {@link PropertyCategory} for multiple standard bean {@link Property}'s at once.
   */
  public void test_propertyCategory() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setA(int value) {",
            "  }",
            "  public void setB(int value) {",
            "  }",
            "  public void setC(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='A'/>",
            "  <properties-advanced names='B'/>",
            "  <properties-hidden names='C'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "setA(int)");
    assertPropertyCategory(PropertyCategory.ADVANCED, description, "setB(int)");
    assertPropertyCategory(PropertyCategory.HIDDEN, description, "setC(int)");
  }

  /**
   * We can use tag "properties-normal" elements to change {@link PropertyCategory} from non-default
   * to normal.
   */
  public void test_propertyCategory_normal() throws Exception {
    setFileContentSrc(
        "test/MyObject0.java",
        getSourceDQ(
            "package test;",
            "public class MyObject0 {",
            "  public void setA(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ("package test;", "public class MyObject extends MyObject0 {", "}"));
    setFileContentSrc(
        "test/MyObject0.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='A'/>",
            "</component>"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-normal names='A'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.NORMAL, description, "setA(int)");
  }

  /**
   * We can use template with "*"-ended at tag "properties-xxx" elements.
   */
  public void test_propertyCategory_template() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setTempValue_1(int value) {",
            "  }",
            "  public void setTempValue_2(int value) {",
            "  }",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='tempValue*'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.NORMAL, description, "setValue(int)");
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "setTempValue_1(int)");
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "setTempValue_2(int)");
  }

  /**
   * Test for flagging field based properties.
   */
  public void test_propertyFlags_forField() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public int foo;",
            "  public int value;",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='value'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "value");
    assertPropertyCategory(PropertyCategory.NORMAL, description, "foo");
  }

  /**
   * When two setters have same name, we need some way to specify exact method with parameter type.
   */
  public void test_propertyFlags_specifyExactMethod() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setText(String text) {",
            "  }",
            "  public void setText(String[] text) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='setText(java.lang.String[])'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "setText(java.lang.String[])");
    assertPropertyCategory(PropertyCategory.NORMAL, description, "setText(java.lang.String)");
  }

  /**
   * When field property name starts with name of some method based property, we should correctly
   * distinguish them.
   */
  public void test_propertyFlags_methodFieldConflict_1() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public int descriptionOffset;",
            "  public void setDescription(String value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='description'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(
        PropertyCategory.PREFERRED,
        description,
        "setDescription(java.lang.String)");
    assertPropertyCategory(PropertyCategory.NORMAL, description, "descriptionOffset");
  }

  /**
   * When field property name is same as name of some method based property, we should correctly
   * distinguish them.
   */
  public void test_propertyFlags_methodFieldConflict_selectMethod() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public int value;",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='m:value'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "setValue(int)");
    assertPropertyCategory(PropertyCategory.NORMAL, description, "value");
  }

  /**
   * When field property name is same as name of some method based property, we should correctly
   * distinguish them.
   */
  public void test_propertyFlags_methodFieldConflict_selectField() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public int value;",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='f:value'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    assertPropertyCategory(PropertyCategory.NORMAL, description, "setValue(int)");
    assertPropertyCategory(PropertyCategory.PREFERRED, description, "value");
  }

  /**
   * Test for using disabling default value of {@link GenericPropertyDescription}.
   * 
   * <pre>
	 *     <properties-noDefaultValue names="valueA valueB"/>
	 * </pre>
   */
  public void test_noDefaultValue() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public int getValueA() {",
            "    return 1;",
            "  }",
            "  public void setValueA(int value) {",
            "  }",
            "  public int getValueB() {",
            "    return 2;",
            "  }",
            "  public void setValueB(int value) {",
            "  }",
            "  public int getValueC() {",
            "    return 3;",
            "  }",
            "  public void setValueC(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-noDefaultValue names='valueA valueB'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    {
      GenericPropertyDescription property = description.getProperty("setValueA(int)");
      assertTrue(property.hasTrueTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG));
    }
    {
      GenericPropertyDescription property = description.getProperty("setValueB(int)");
      assertTrue(property.hasTrueTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG));
    }
    {
      GenericPropertyDescription property = description.getProperty("setValueC(int)");
      assertFalse(property.hasTrueTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tags
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for marking {@link GenericPropertyDescription} with tags.
   * 
   * <pre>
	 *     <property-tag name="text" tag="isText" value="true"/>
	 *     <property-tag name="image" tag="isImage" value="true"/>
	 * </pre>
   */
  public void test_standardPropertyTag_simple() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property-tag name='value' tag='tagName' value='tagValue'/>",
            "  <property-tag name='value' tag='tagName2' value='tagValue2'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    {
      GenericPropertyDescription property = description.getProperty("setValue(int)");
      assertEquals("tagValue", property.getTag("tagName"));
      assertEquals("tagValue2", property.getTag("tagName2"));
      assertNull(property.getTag("no-such-tag"));
    }
  }

  public void test_standardPropertyTag_exactMethodSignature() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setText(String text) {",
            "  }",
            "  public void setText(String[] text) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property-tag name='setText(java.lang.String)' tag='tagName' value='tagValue'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    // setText(java.lang.String) has tag
    {
      GenericPropertyDescription property = description.getProperty("setText(java.lang.String)");
      assertEquals("tagValue", property.getTag("tagName"));
    }
    // setText(java.lang.String[]) does not have tag
    {
      GenericPropertyDescription property = description.getProperty("setText(java.lang.String[])");
      assertNull(property.getTag("tagName"));
    }
  }

  /**
   * Test for using tag "title" to specify different title of {@link GenericPropertyDescription}.
   * 
   * <pre>
	 *     <property-tag name="hTML" tag="title" value="HTML"/>
	 * </pre>
   */
  public void test_setTitleForProperty() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property-tag name='value' tag='title' value='MyTitle'/>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    {
      GenericPropertyDescription property = description.getProperty("setValue(int)");
      assertEquals("MyTitle", property.getTitle());
      assertEquals("MyTitle", property.getTag("title"));
    }
  }

  /**
   * Test for using tag "title" to specify different title of {@link GenericPropertyDescription}.
   * 
   * <pre>
   *     <property id="...">
   *         <tag name="title" value="HTML"/>
   *     </property>
   * </pre>
   */
  public void test_setTitleForProperty2() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setValue(int)'>",
            "    <tag name='title' value='MyTitle'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyObjectDescription();
    {
      GenericPropertyDescription property = description.getProperty("setValue(int)");
      assertEquals("MyTitle", property.getTitle());
      assertEquals("MyTitle", property.getTag("title"));
    }
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
}
