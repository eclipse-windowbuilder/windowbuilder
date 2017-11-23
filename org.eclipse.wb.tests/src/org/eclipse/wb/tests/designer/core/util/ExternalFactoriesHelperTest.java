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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

import org.osgi.framework.Bundle;

import java.util.List;

/**
 * Tests for {@link ExternalFactoriesHelper}.
 * 
 * @author scheglov_ke
 */
public class ExternalFactoriesHelperTest extends DesignerTestCase {
  private static final String POINT_ID = "org.eclipse.wb.tests.testPoint";

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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    waitEventLoop(0);
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addRemoveDynamicExtension_element() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <!-- filler filler filler filler filler filler -->",
              "  <testObject id='someId'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // we can find id
    {
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject");
      assertThat(elements).hasSize(1);
      assertEquals("someId", elements.get(0).getAttribute("id"));
    }
    // remove dynamic extension
    TestUtils.removeDynamicExtension(POINT_ID);
    {
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject");
      assertThat(elements).isEmpty();
    }
  }

  /**
   * We don't wait for removing extension, however we validate that {@link IConfigurationElement} is
   * child of valid (so not removed) {@link IExtension}, and don't return it in other case.
   */
  public void test_addRemoveDynamicExtension_noWait() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <!-- filler filler filler filler filler filler -->",
              "  <testObject id='someId'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // we can find id
    {
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject");
      assertThat(elements).hasSize(1);
      assertEquals("someId", elements.get(0).getAttribute("id"));
    }
    // remove dynamic extension, no wait
    TestUtils.removeDynamicExtension_noWait(POINT_ID);
    {
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject");
      assertThat(elements).isEmpty();
    }
    // do wait
    TestUtils.removeDynamicExtension(POINT_ID);
  }

  public void test_getElements_withPriority() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <testObject priority='1'/>",
              "  <testObject priority='2'/>",
              "  <testObject priority='3'/>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // check order
    try {
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject");
      assertThat(elements).hasSize(3);
      assertEquals("3", ExternalFactoriesHelper.getRequiredAttribute(elements.get(0), "priority"));
      assertEquals("2", ExternalFactoriesHelper.getRequiredAttribute(elements.get(1), "priority"));
      assertEquals("1", ExternalFactoriesHelper.getRequiredAttribute(elements.get(2), "priority"));
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getRequiredAttribute
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExternalFactoriesHelper#getRequiredAttribute(IConfigurationElement, String)}.
   */
  public void test_getRequiredAttribute() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <!-- filler filler filler filler filler filler -->",
              "  <testObject id='someId'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // verify
    try {
      IConfigurationElement element =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject").get(0);
      // OK
      assertEquals("someId", ExternalFactoriesHelper.getRequiredAttribute(element, "id"));
      // no attribute
      try {
        ExternalFactoriesHelper.getRequiredAttribute(element, "noSuchAttribute");
        fail();
      } catch (IllegalArgumentException e) {
      }
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  /**
   * Test for
   * {@link ExternalFactoriesHelper#getRequiredAttributeInteger(IConfigurationElement, String)}.
   */
  public void test_getRequiredAttributeInteger() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <!-- filler filler filler filler filler filler -->",
              "  <testObject value='555' badValue='notInteger'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // verify
    try {
      IConfigurationElement element =
          ExternalFactoriesHelper.getElements(POINT_ID, "testObject").get(0);
      // OK
      assertEquals(555, ExternalFactoriesHelper.getRequiredAttributeInteger(element, "value"));
      // bad value
      try {
        ExternalFactoriesHelper.getRequiredAttributeInteger(element, "badValue");
        fail();
      } catch (NumberFormatException e) {
      }
      // no attribute
      try {
        ExternalFactoriesHelper.getRequiredAttributeInteger(element, "noSuchAttribute");
        fail();
      } catch (IllegalArgumentException e) {
      }
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getElementsInstances
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExternalFactoriesHelper#getElementsInstances(Class, String, String)}.
   */
  public void test_getElementsInstances_newElement() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <testObject class='" + TestObject_1.class.getName() + "'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // we can find id
    {
      List<Object> instances =
          ExternalFactoriesHelper.getElementsInstances(Object.class, POINT_ID, "testObject");
      assertThat(instances).hasSize(1);
      Object instance = instances.get(0);
      assertThat(instance).isInstanceOf(TestObject_1.class);
      assertEquals("1", instance.toString());
    }
    // remove dynamic extension
    TestUtils.removeDynamicExtension(POINT_ID);
    {
      List<Object> instances =
          ExternalFactoriesHelper.getElementsInstances(Object.class, POINT_ID, "testObject");
      assertThat(instances).isEmpty();
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getElementsInstances(Class, String, String)}.
   * <p>
   * Singleton.
   */
  public void test_getElementsInstances_INSTANCE() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <testObject class='" + TestObjectSingleton.class.getName() + "'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // verify
    try {
      List<Object> instances =
          ExternalFactoriesHelper.getElementsInstances(Object.class, POINT_ID, "testObject");
      assertThat(instances).hasSize(1);
      assertSame(TestObjectSingleton.INSTANCE, instances.get(0));
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getElementsInstances(Class, String, String)}.
   */
  public void test_getElementsInstances_withPriority_D1() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <testObject class='" + TestObject_1.class.getName() + "'>",
              "  </testObject>",
              "  <testObject class='" + TestObject_2.class.getName() + "' priority='1'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // check order
    try {
      List<Object> instances =
          ExternalFactoriesHelper.getElementsInstances(Object.class, POINT_ID, "testObject");
      assertThat(instances).hasSize(2);
      assertEquals("2", instances.get(0).toString());
      assertEquals("1", instances.get(1).toString());
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getElementsInstances(Class, String, String)}.
   */
  public void test_getElementsInstances_withPriority_1D() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <testObject class='" + TestObject_2.class.getName() + "' priority='1'>",
              "  </testObject>",
              "  <testObject class='" + TestObject_1.class.getName() + "'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // check order
    try {
      List<Object> instances =
          ExternalFactoriesHelper.getElementsInstances(Object.class, POINT_ID, "testObject");
      assertThat(instances).hasSize(2);
      assertEquals("2", instances.get(0).toString());
      assertEquals("1", instances.get(1).toString());
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getElementsInstances(Class, String, String)}.
   */
  public void test_getElementsInstances_withPriority_123() throws Exception {
    // add dynamic extension
    {
      String contribution =
          getSourceDQ(
              "  <testObject class='" + TestObject_1.class.getName() + "' priority='1'>",
              "  </testObject>",
              "  <testObject class='" + TestObject_2.class.getName() + "' priority='2'>",
              "  </testObject>",
              "  <testObject class='" + TestObject_3.class.getName() + "' priority='3'>",
              "  </testObject>");
      TestUtils.addDynamicExtension(POINT_ID, contribution);
    }
    // check order
    try {
      List<Object> instances =
          ExternalFactoriesHelper.getElementsInstances(Object.class, POINT_ID, "testObject");
      assertThat(instances).hasSize(3);
      assertEquals("3", instances.get(0).toString());
      assertEquals("2", instances.get(1).toString());
      assertEquals("1", instances.get(2).toString());
    } finally {
      TestUtils.removeDynamicExtension(POINT_ID);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // loadBundleClass()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExternalFactoriesHelper#loadBundleClass(String)}.
   */
  public void test_loadBundleClass() throws Exception {
    // from this bundle
    {
      Class<?> clazz = TestObject_1.class;
      String className = clazz.getName();
      assertSame(clazz, ExternalFactoriesHelper.loadBundleClass(className));
    }
    // from this bundle, array
    {
      Class<?> clazz = TestObject_1[].class;
      String className = clazz.getName();
      assertSame(clazz, ExternalFactoriesHelper.loadBundleClass(className));
    }
    // system class
    {
      assertSame(List.class, ExternalFactoriesHelper.loadBundleClass("java.util.List"));
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#loadBundleClass(String)}.
   * <p>
   * Add invalid extension, so throw exception during attempt to load.
   */
  public void test_loadBundleClass_whenInvalidExtension() throws Exception {
    String pointId = "org.eclipse.wb.core.classLoadingContributor";
    TestUtils.addDynamicExtension(pointId, "<contributor no-namespace='foo'/>");
    try {
      try {
        ExternalFactoriesHelper.loadBundleClass("java.util.List");
        fail();
      } catch (ClassNotFoundException e) {
        assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
      }
    } finally {
      TestUtils.removeDynamicExtension(pointId);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExternalFactoriesHelper#getRequiredBundle(String)}.
   */
  public void test_getRequiredBundle_good() throws Exception {
    String id = "org.eclipse.wb.core";
    Bundle expected = Platform.getBundle(id);
    assertEquals(expected, ExternalFactoriesHelper.getRequiredBundle(id));
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getRequiredBundle(String)}.
   */
  public void test_getRequiredBundle_noSuchBundle() throws Exception {
    String id = "no.such.bundle";
    try {
      ExternalFactoriesHelper.getRequiredBundle(id);
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(id);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getImage()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExternalFactoriesHelper#getImage(IConfigurationElement, String)}.
   */
  public void test_getImage_noSuchAttribute() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(POINT_ID, "<testObject/>");
      testBundle.install();
      // work with Bundle
      {
        IConfigurationElement element =
            ExternalFactoriesHelper.getElements(POINT_ID, "testObject").get(0);
        Image image = ExternalFactoriesHelper.getImage(element, "noAttribute");
        assertNull(image);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getImage(IConfigurationElement, String)}.
   */
  public void test_getImage_success() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(POINT_ID, "<testObject icon='icons/test.png'/>");
      testBundle.setFile("icons/test.png", TestUtils.createImagePNG(1, 2));
      testBundle.install();
      // work with Bundle
      {
        IConfigurationElement element =
            ExternalFactoriesHelper.getElements(POINT_ID, "testObject").get(0);
        Image image = ExternalFactoriesHelper.getImage(element, "icon");
        assertNotNull(image);
        assertEquals(1, image.getBounds().width);
        assertEquals(2, image.getBounds().height);
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getImageDescriptor()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExternalFactoriesHelper#getImageDescriptor(IConfigurationElement, String)}.
   */
  public void test_getImageDescriptor_noSuchAttribute() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(POINT_ID, "<testObject/>");
      testBundle.install();
      // work with Bundle
      {
        IConfigurationElement element =
            ExternalFactoriesHelper.getElements(POINT_ID, "testObject").get(0);
        ImageDescriptor descriptor =
            ExternalFactoriesHelper.getImageDescriptor(element, "noAttribute");
        assertNull(descriptor);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ExternalFactoriesHelper#getImageDescriptor(IConfigurationElement, String)}.
   */
  public void test_getImageDescriptor_success() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(POINT_ID, "<testObject icon='res/path/test.png'/>");
      testBundle.setFile("res/path/test.png", TestUtils.createImagePNG(2, 3));
      testBundle.install();
      // work with Bundle
      {
        IConfigurationElement element =
            ExternalFactoriesHelper.getElements(POINT_ID, "testObject").get(0);
        ImageDescriptor descriptor = ExternalFactoriesHelper.getImageDescriptor(element, "icon");
        assertNotNull(descriptor);
        // validate
        Image image = descriptor.createImage();
        try {
          assertEquals(2, image.getBounds().width);
          assertEquals(3, image.getBounds().height);
        } finally {
          image.dispose();
        }
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class TestObject_1 {
    @Override
    public String toString() {
      return "1";
    }
  }
  public static class TestObject_2 {
    @Override
    public String toString() {
      return "2";
    }
  }
  public static class TestObject_3 {
    @Override
    public String toString() {
      return "3";
    }
  }
  public static class TestObjectSingleton {
    public static final Object INSTANCE = new TestObjectSingleton();

    @Override
    public String toString() {
      return "singleton";
    }
  }
}
