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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.resource.EmptyDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.FromListDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IJavaProject;

import static org.assertj.core.api.Assertions.assertThat;

import org.osgi.framework.Bundle;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

/**
 * Tests for {@link IDescriptionVersionsProvider} .
 * 
 * @author scheglov_ke
 */
public class DescriptionVersionsProvidersTest extends SwingModelTest {
  private static final String POINT_TOOLKITS = "org.eclipse.wb.core.toolkits";
  private static final String POINT_VER_FACTORIES =
      "org.eclipse.wb.core.descriptionVersionsProviderFactories";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link EmptyDescriptionVersionsProvider}.
   */
  public void test_providerEmpty() throws Exception {
    IDescriptionVersionsProvider provider = EmptyDescriptionVersionsProvider.INSTANCE;
    assertThat(provider.getVersions(Object.class)).isEmpty();
    assertThat(provider.getVersions(Component.class)).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DescriptionVersionsProvider_FromList
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_providerFromList_noCurrentInList() throws Exception {
    try {
      new FromListDescriptionVersionsProvider(ImmutableList.of("1.0", "2.0", "3.0"), "2.1") {
        @Override
        protected boolean validate(Class<?> componentClass) throws Exception {
          return false;
        }
      };
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  /**
   * Middle version - check this version all other before it.
   */
  public void test_providerFromList_getVersions_middleVersion() throws Exception {
    List<String> allVersions = ImmutableList.of("1.0", "2.0", "3.0");
    String currentVersion = "2.0";
    FromListDescriptionVersionsProvider provider =
        new FromListDescriptionVersionsProvider(allVersions, currentVersion) {
          @Override
          protected boolean validate(Class<?> componentClass) throws Exception {
            return Component.class.isAssignableFrom(componentClass);
          }
        };
    // invalid Class, so no versions
    {
      List<String> versions = provider.getVersions(Object.class);
      assertThat(versions).isEmpty();
    }
    // valid Class, "1.0" and "2.0" expected
    {
      List<String> versions = provider.getVersions(JButton.class);
      assertThat(versions).isEqualTo(ImmutableList.of("2.0", "1.0"));
    }
  }

  /**
   * Latest version - same as for middle, check this version all other before it.
   */
  public void test_providerFromList_getVersions_latestVersion() throws Exception {
    List<String> allVersions = ImmutableList.of("1.0", "2.0", "3.0");
    String currentVersion = "3.0";
    FromListDescriptionVersionsProvider provider =
        new FromListDescriptionVersionsProvider(allVersions, currentVersion) {
          @Override
          protected boolean validate(Class<?> componentClass) throws Exception {
            return Component.class.isAssignableFrom(componentClass);
          }
        };
    //
    List<String> versions = provider.getVersions(JButton.class);
    assertThat(versions).isEqualTo(ImmutableList.of("3.0", "2.0", "1.0"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Component descriptions versions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using {@link IDescriptionVersionsProvider}'s in {@link ComponentDescriptionHelper}.
   */
  public void test_componentResourceVersions_20() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String componentClassName = ComponentWithVersionedDescriptions.class.getName();
      // add library with component
      testBundle.addJar("myClasses.jar").addClass(ComponentWithVersionedDescriptions.class).close();
      // add factory with version "2.0"
      testBundle.addClass(DescriptionVersionsProvider_2.class);
      testBundle.addClass(DescriptionVersionsProviderFactory_2.class);
      testBundle.addExtension(POINT_TOOLKITS, new String[]{
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
          "</toolkit>"});
      testBundle.addExtension(POINT_VER_FACTORIES, "<factory class='"
          + DescriptionVersionsProviderFactory_2.class.getName()
          + "'/>");
      // add descriptions: good for "2.0", bad for "3.0", "1.0" and "base"
      {
        String descriptionPath = componentClassName.replace('.', '/') + ".wbp-component.xml";
        testBundle.setFile("wbp-meta/" + descriptionPath, "bad XML");
        testBundle.setFile("wbp-meta/1.0/" + descriptionPath, "bad XML");
        testBundle.setFile(
            "wbp-meta/2.0/" + descriptionPath,
            getSourceDQ(
                "<?xml version='1.0' encoding='UTF-8'?>",
                "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
                "  <parameters>",
                "    <parameter name='marker'>expectedMarker</parameter>",
                "  </parameters>",
                "</component>"));
        testBundle.setFile("wbp-meta/3.0/" + descriptionPath, "bad XML");
      }
      // install and wait
      testBundle.install();
      // do verify
      try {
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "      // filler",
            "  }",
            "}");
        // ask ComponentDescription, will be successful only if version "2.0" is used
        ComponentDescription description =
            ComponentDescriptionHelper.getDescription(m_lastEditor, componentClassName);
        assertEquals("expectedMarker", description.getParameter("marker"));
      } finally {
        testBundle.uninstall();
        waitEventLoop(10);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for using {@link IDescriptionVersionsProvider}'s in {@link ComponentDescriptionHelper}.
   */
  public void test_componentResourceVersions_default() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String componentClassName = ComponentWithVersionedDescriptions.class.getName();
      // add library with component
      testBundle.addJar("myClasses.jar").addClass(ComponentWithVersionedDescriptions.class).close();
      // add factory with version "2.0"
      testBundle.addClass(DescriptionVersionsProviderFactory_default.class);
      testBundle.addExtension(POINT_TOOLKITS, new String[]{
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <classLoader-library bundle='" + testBundle.getId() + "' jar='myClasses.jar'/>",
          "</toolkit>"});
      testBundle.addExtension(POINT_VER_FACTORIES, "<factory class='"
          + DescriptionVersionsProviderFactory_default.class.getName()
          + "'/>");
      // add descriptions: good for "base", bad for "1.0", "2.0" and "3.0"
      {
        String descriptionPath = componentClassName.replace('.', '/') + ".wbp-component.xml";
        testBundle.setFile(
            "wbp-meta/" + descriptionPath,
            getSourceDQ(
                "<?xml version='1.0' encoding='UTF-8'?>",
                "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
                "  <parameters>",
                "    <parameter name='marker'>expectedMarker</parameter>",
                "  </parameters>",
                "</component>"));
        testBundle.setFile("wbp-meta/1.0/" + descriptionPath, "bad XML");
        testBundle.setFile("wbp-meta/2.0/" + descriptionPath, "bad XML");
        testBundle.setFile("wbp-meta/3.0/" + descriptionPath, "bad XML");
      }
      // install and wait
      testBundle.install();
      // do verify
      try {
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "      // filler",
            "  }",
            "}");
        // ask ComponentDescription, will be successful only if version "base" is used
        ComponentDescription description =
            ComponentDescriptionHelper.getDescription(m_lastEditor, componentClassName);
        assertEquals("expectedMarker", description.getParameter("marker"));
      } finally {
        testBundle.uninstall();
        waitEventLoop(10);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for using {@link IDescriptionVersionsProvider}'s in {@link FactoryDescriptionHelper}.
   */
  public void test_factoryResourceVersions_20() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      Class<FactoryWithVersionedDescriptions> factoryClass = FactoryWithVersionedDescriptions.class;
      String factoryClassName = factoryClass.getName();
      // add library with component
      testBundle.addJar("myClasses.jar").addClass(factoryClass).close();
      // add factory with version "2.0"
      testBundle.addClass(DescriptionVersionsProvider_2.class);
      testBundle.addClass(DescriptionVersionsProviderFactory_2.class);
      testBundle.addExtension(POINT_TOOLKITS, "<toolkit id='org.eclipse.wb.swing'/>");
      testBundle.addExtension(POINT_VER_FACTORIES, "<factory class='"
          + DescriptionVersionsProviderFactory_2.class.getName()
          + "'/>");
      // add descriptions: good for "2.0", bad for "1.0", "3.0" and "all"
      {
        String descriptionPath = factoryClassName.replace('.', '/') + ".wbp-factory.xml";
        testBundle.setFile("wbp-meta/" + descriptionPath, "bad XML");
        testBundle.setFile("wbp-meta/1.0/" + descriptionPath, "bad XML");
        testBundle.setFile(
            "wbp-meta/2.0/" + descriptionPath,
            getSourceDQ(
                "<?xml version='1.0' encoding='UTF-8'?>",
                "<factory>",
                "  <method name='createButton'>",
                "    <description>Some description</description>",
                "  </method>",
                "</factory>"));
        testBundle.setFile("wbp-meta/3.0/" + descriptionPath, "bad XML");
      }
      // install and wait
      testBundle.install();
      // do verify
      try {
        // add JAR from Bundle
        {
          Bundle libBundle = testBundle.getBundle();
          String path = FileLocator.toFileURL(libBundle.getEntry("/myClasses.jar")).getPath();
          m_testProject.addExternalJar(path);
        }
        // parse
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "      // filler",
            "  }",
            "}");
        // ask factory, will be successful only if version "2.0" is used
        Map<String, FactoryMethodDescription> descriptions =
            FactoryDescriptionHelper.getDescriptionsMap(
                m_lastEditor,
                m_lastLoader.loadClass(factoryClassName),
                true);
        assertThat(descriptions).hasSize(1);
      } finally {
        testBundle.uninstall();
        waitEventLoop(10);
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Uses version <code>2.0</code> of "library".
   */
  public static class DescriptionVersionsProviderFactory_2
      implements
        IDescriptionVersionsProviderFactory {
    public Map<String, Object> getVersions(IJavaProject javaProject, ClassLoader classLoader)
        throws Exception {
      return ImmutableMap.<String, Object>of("test.version", "2.0");
    }

    public IDescriptionVersionsProvider getProvider(IJavaProject javaProject,
        ClassLoader classLoader) throws Exception {
      return new DescriptionVersionsProvider_2();
    }
  }
  public static final class DescriptionVersionsProvider_2
      extends
        FromListDescriptionVersionsProvider {
    public DescriptionVersionsProvider_2() {
      super(ImmutableList.of("1.0", "2.0", "3.0"), "2.0");
    }

    @Override
    protected boolean validate(Class<?> componentClass) throws Exception {
      return componentClass.getName().contains("WithVersionedDescriptions");
    }
  }
  /**
   * No versions, so from "wbp-meta" itself.
   */
  public static class DescriptionVersionsProviderFactory_default
      implements
        IDescriptionVersionsProviderFactory {
    public Map<String, Object> getVersions(IJavaProject javaProject, ClassLoader classLoader)
        throws Exception {
      return ImmutableMap.<String, Object>of("test.version", "3.0");
    }

    public IDescriptionVersionsProvider getProvider(IJavaProject javaProject,
        ClassLoader classLoader) throws Exception {
      return null;
    }
  }
  /**
   * We use this class for testing component description versions.
   */
  public static class ComponentWithVersionedDescriptions {
  }
  /**
   * We use this class for testing factory description versions.
   */
  public static class FactoryWithVersionedDescriptions {
    public static JButton createButton() {
      return new JButton();
    }
  }
}
