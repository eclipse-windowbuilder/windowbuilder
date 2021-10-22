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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.editor.structure.property.PropertyCategoryProviderProvider;
import org.eclipse.wb.core.editor.structure.property.PropertyListProcessor;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.structure.property.ComponentsPropertiesPage;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import java.util.List;

/**
 * Test for {@link ComponentsPropertiesPage}.
 *
 * @author scheglov_ke
 */
public class ComponentsPropertiesPageTest extends SwingGefTest {
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
  // PropertyCategoryProvider_Provider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return {@link PropertyCategory#NORMAL} for all properties.
   */
  public static class CategoryProvider2 implements PropertyCategoryProviderProvider {
    public PropertyCategoryProvider get(List<ObjectInfo> objects) {
      return new PropertyCategoryProvider() {
        public PropertyCategory getCategory(Property property) {
          return PropertyCategory.NORMAL;
        }
      };
    }
  }

  /**
   * Test for using {@link PropertyCategoryProviderProvider}.
   */
  public void test_PropertyCategoryProvider_Provider() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    // filler filler filler",
            "  }",
            "}");
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> providerClass = CategoryProvider2.class;
      testBundle.addClass(providerClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.propertiesPageCategoryProviders",
          "<provider class='" + providerClass.getName() + "'/>");
      testBundle.install();
      try {
        canvas.select(panel);
        Property property = m_propertyTable.forTests_getProperty(0);
        // in normal situation first property is SYSTEM, but we force NORMAL
        assertSame(PropertyCategory.NORMAL, m_propertyTable.forTests_getCategory(property));
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyList_Processor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return {@link PropertyCategory#NORMAL} for all properties.
   */
  public static class PropertyProcessor implements PropertyListProcessor {
    private static ComplexProperty m_propertyProcessorWrapper = new ComplexProperty("ALL",
        "(all properties)");

    public void process(List<ObjectInfo> objects, List<Property> properties) {
      m_propertyProcessorWrapper.setProperties(properties);
      properties.clear();
      properties.add(m_propertyProcessorWrapper);
    }
  }

  /**
   * Test for using {@link PropertyListProcessor}.
   */
  public void test_PropertyList_Processor() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    // filler filler filler",
            "  }",
            "}");
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> processorClass = PropertyProcessor.class;
      testBundle.addClass(processorClass);
      testBundle.addExtension("org.eclipse.wb.core.propertiesPageProcessors", "<processor class='"
          + processorClass.getName()
          + "'/>");
      testBundle.install();
      try {
        canvas.select(panel);
        // all properties are in one
        assertEquals(1, m_propertyTable.forTests_getPropertiesCount());
        Property property = m_propertyTable.forTests_getProperty(0);
        assertEquals("ALL", property.getTitle());
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }
}
