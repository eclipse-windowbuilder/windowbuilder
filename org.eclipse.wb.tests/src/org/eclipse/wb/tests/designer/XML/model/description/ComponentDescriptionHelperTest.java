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
package org.eclipse.wb.tests.designer.XML.model.description;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.UseModelIfNotAlready;
import org.eclipse.wb.internal.core.xml.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.description.CreationAttributeDescription;
import org.eclipse.wb.internal.core.xml.model.description.CreationDescription;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionProcessor;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionRulesProvider;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Map;

/**
 * Test for {@link ComponentDescriptionHelper}.
 *
 * @author scheglov_ke
 */
public class ComponentDescriptionHelperTest extends AbstractCoreTest {
  private static final String[] ESA = ArrayUtils.EMPTY_STRING_ARRAY;

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
  // Basic
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentDescription} which can not be parsed.
   */
  public void test_bad() throws Exception {
    prepareMyComponent(ESA, new String[]{"bad xml"});
    try {
      getMyDescription();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.DESCRIPTION_LOADING, de.getCode());
    }
  }

  /**
   * Test for basic {@link ComponentDescription} parsing features.
   */
  public void test_basic() throws Exception {
    ComponentDescription description = getDescription("org.eclipse.swt.widgets.Composite");
    assertNotNull(description);
    // class
    assertEquals("org.eclipse.swt.widgets.Composite", description.getComponentClass().getName());
    assertNotNull(description.getModelClass());
    // toolkit
    assertSame(RcpToolkitDescription.INSTANCE, description.getToolkit());
    // presentation
    assertThat(description.getDescription()).isNotEmpty();
    {
      Image icon = description.getIcon();
      assertNotNull(icon);
      assertEquals(16, icon.getBounds().width);
      assertEquals(16, icon.getBounds().height);
    }
  }

  /**
   * Test for {@link UseModelIfNotAlready} annotation.
   */
  public void test_UseModelIfNotAlready() throws Exception {
    setFileContentSrc(
        "test/MyInterface.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public interface MyInterface {",
            "}"));
    setFileContentSrc(
        "test/MyInterface.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <x-model class='" + UseModelIfNotAlreadyInfo.class.getName() + "'/>",
            "</component>"));
    setFileContentSrc(
        "test/BaseComponent.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class BaseComponent {",
            "}"));
    setFileContentSrc(
        "test/BaseComponent.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <x-model class='" + CompositeInfo.class.getName() + "'/>",
            "</component>"));
    setFileContentSrc(
        "test/ComponentA.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class ComponentA extends BaseComponent implements MyInterface {",
            "}"));
    setFileContentSrc(
        "test/ComponentB.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class ComponentB implements MyInterface {",
            "}"));
    waitForAutoBuild();
    // use with TestBundle to provide MyInterfaceInfo model
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addClass(UseModelIfNotAlreadyInfo.class);
      testBundle.addExtension(
          "org.eclipse.wb.core.toolkits",
          new String[]{"<toolkit id='org.eclipse.wb.rcp'/>"});
      testBundle.install();
      // ComponentA has base class
      {
        ComponentDescription description = getDescription("test.ComponentA");
        assertSame(CompositeInfo.class, description.getModelClass());
      }
      // ComponentB is interface only
      {
        ComponentDescription description = getDescription("test.ComponentB");
        assertSame(UseModelIfNotAlreadyInfo.class, description.getModelClass());
      }
    } finally {
      testBundle.dispose();
    }
  }

  @UseModelIfNotAlready(WidgetInfo.class)
  private class UseModelIfNotAlreadyInfo {
  }

  /**
   * Test that we can parse HTML description text, specific for custom component.
   */
  public void test_specificDescription() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "// filler filler filler filler filler",
        "<description>My <p/> description</description>"});
    ComponentDescription description = getMyDescription();
    // description
    assertEquals("My <p/> description", description.getDescription());
  }

  public void test_useIconOfSuperclass() throws Exception {
    prepareMyComponent();
    ComponentDescription description = getMyDescription();
    assertNotNull(description);
    // use class name as description
    assertEquals("test.MyComponent", description.getDescription());
    // icon is same as for Composite
    {
      Image myIcon = description.getIcon();
      Image iconOfComposite = getDescription("org.eclipse.swt.widgets.Composite").getIcon();
      assertTrue(UiUtils.equals(myIcon, iconOfComposite));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractDescription#getArbitraryValue(Object)} and
   * {@link AbstractDescription#putArbitraryValue(Object, Object)}.
   */
  public void test_arbitraries() throws Exception {
    String key = "test.key";
    ComponentDescription description = getDescription("org.eclipse.swt.widgets.Composite");
    assertSame(null, description.getArbitraryValue(key));
    description.putArbitraryValue(key, this);
    assertSame(this, description.getArbitraryValue(key));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String PROCESSORS_POINT_ID = "org.eclipse.wb.core.xml.descriptionProcessors";

  /**
   * Test that {@link IDescriptionProcessor}s are used.
   */
  public void test_IDescriptionProcessor() throws Exception {
    // add dynamic processor and re-load
    addProcessorExtension(MyDescriptionProcessor.class.getName());
    try {
      // no class yet
      assertEquals(null, MyDescriptionProcessor.componentClassName);
      // do process
      {
        parse("<Shell/>");
        prepareMyComponent();
        getMyDescription();
      }
      // we just loaded MyComponent
      assertEquals("test.MyComponent", MyDescriptionProcessor.componentClassName);
    } finally {
      removeProcessorExtension();
    }
  }

  private static void addProcessorExtension(String className) throws Exception {
    String contribution = "  <processor class='" + className + "'/>";
    TestUtils.addDynamicExtension(PROCESSORS_POINT_ID, contribution);
  }

  protected static void removeProcessorExtension() throws Exception {
    TestUtils.removeDynamicExtension(PROCESSORS_POINT_ID);
  }

  public static final class MyDescriptionProcessor implements IDescriptionProcessor {
    private static String componentClassName;

    public void process(EditorContext context, ComponentDescription componentDescription)
        throws Exception {
      componentClassName = componentDescription.getComponentClass().getName();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionRulesProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String PROVIDERS_POINT_ID =
      "org.eclipse.wb.core.xml.descriptionRulesProviders";

  /**
   * Test that {@link IDescriptionRulesProvider}s are used.
   */
  public void test_IDescriptionRulesProvider() throws Exception {
    addRulesProviderExtension(MyDescriptionRulesProvider.class.getName());
    try {
      parse("<Shell/>");
      prepareMyComponent();
      ComponentDescription componentDescription = getMyDescription();
      // our rule puts tag
      assertEquals("myRule", componentDescription.getTag("test.key"));
    } finally {
      removeRulesProviderExtension();
    }
  }

  private static void addRulesProviderExtension(String className) throws Exception {
    String contribution = "  <provider class='" + className + "'/>";
    TestUtils.addDynamicExtension(PROVIDERS_POINT_ID, contribution);
  }

  protected static void removeRulesProviderExtension() throws Exception {
    TestUtils.removeDynamicExtension(PROVIDERS_POINT_ID);
  }

  public static final class MyDescriptionRulesProvider implements IDescriptionRulesProvider {
    public void addRules(Digester digester, EditorContext context, Class<?> componentClass) {
      digester.addRule("component", new Rule() {
        @Override
        public void end(String namespace, String name) throws Exception {
          ComponentDescription componentDescription = (ComponentDescription) digester.peek();
          componentDescription.putTag("test.key", "myRule");
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentDescription#getParameters()}.
   */
  public void test_parameters() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "  <parameters>",
        "    <parameter name='parameter_1'>AAA</parameter>",
        "    <parameter name='parameter_2'>BBB</parameter>",
        "    <parameter name='parameter_3'/>",
        "  </parameters>"});
    ComponentDescription description = getMyDescription();
    // single parameters
    {
      assertEquals("AAA", description.getParameter("parameter_1"));
      assertEquals("BBB", description.getParameter("parameter_2"));
      assertEquals("", description.getParameter("parameter_3"));
      assertNull(description.getParameter("parameter_No"));
    }
    // parameters as Map
    {
      Map<String, String> parameters = description.getParameters();
      assertThat(parameters).contains(
          entry("parameter_1", "AAA"),
          entry("parameter_2", "BBB"),
          entry("parameter_3", ""));
    }
  }

  /**
   * Test for {@link ComponentDescription#hasTrueParameter(String)}.
   */
  public void test_hasTrueParameter() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "  <parameters>",
        "    <parameter name='parameter_1'>true</parameter>",
        "    <parameter name='parameter_2'>false</parameter>",
        "    <parameter name='parameter_3'/>",
        "  </parameters>"});
    ComponentDescription description = getMyDescription();
    assertTrue(description.hasTrueParameter("parameter_1"));
    assertFalse(description.hasTrueParameter("parameter_2"));
    assertFalse(description.hasTrueParameter("parameter_3"));
    assertFalse(description.hasTrueParameter("parameter_No"));
  }

  /**
   * Test that {@link IProject} "wbp-meta" folder it used to get description.
   */
  public void test_descriptionFromProject_meta() throws Exception {
    setFileContentSrc(
        "test/MyComponent.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.swt.widgets.*;",
            "public class MyComponent extends Composite {",
            "  public MyComponent(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContent(
        "wbp-meta/test/MyComponent.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='testParameter'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentDescription description = getMyDescription();
    assertTrue(description.hasTrueParameter("testParameter"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CreationDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CreationDescription_noID() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "// filler filler filler filler filler",
        "<creation>",
        "  <source/>",
        "  <description>My description</description>",
        "  <x-attribute ns='ns1' name='attr1' value='value 1'/>",
        "  <x-attribute name='attr2' value='value 2'/>",
        "</creation>"});
    ComponentDescription description = getMyDescription();
    // creations
    List<CreationDescription> creations = description.getCreations();
    assertThat(creations).hasSize(1);
    // single CreationDescription
    {
      CreationDescription creation = creations.get(0);
      assertSame(creation, description.getCreation(null));
      assertSame(null, creation.getId());
      assertSame(description.getIcon(), creation.getIcon());
      assertEquals("MyComponent", creation.getName());
      assertEquals("My description", creation.getDescription());
      // attributes
      {
        List<CreationAttributeDescription> attributes = creation.getAttributes();
        assertThat(attributes).hasSize(2);
        {
          CreationAttributeDescription attribute = attributes.get(0);
          assertEquals("ns1", attribute.getNamespace());
          assertEquals("attr1", attribute.getName());
          assertEquals("value 1", attribute.getValue());
        }
        {
          CreationAttributeDescription attribute = attributes.get(1);
          assertEquals(null, attribute.getNamespace());
          assertEquals("attr2", attribute.getName());
          assertEquals("value 2", attribute.getValue());
        }
      }
    }
  }

  public void test_CreationDescription_withID() throws Exception {
    TestUtils.createImagePNG(m_testProject, "src/test/MyComponent_my.png", 5, 10);
    prepareMyComponent(ESA, new String[]{
        "// filler filler filler filler filler",
        "<creation id='my' name='My name'>",
        "  <source/>",
        "  <description>My description</description>",
        "</creation>"});
    ComponentDescription description = getMyDescription();
    // creations
    List<CreationDescription> creations = description.getCreations();
    assertThat(creations).hasSize(1);
    // "my" CreationDescription
    {
      CreationDescription creation = description.getCreation("my");
      assertEquals("my", creation.getId());
      {
        Image icon = creation.getIcon();
        assertEquals(5, icon.getBounds().width);
        assertEquals(10, icon.getBounds().height);
      }
      assertEquals("My name", creation.getName());
      assertEquals("My description", creation.getDescription());
      // attributes
      {
        List<CreationAttributeDescription> attributes = creation.getAttributes();
        assertThat(attributes).hasSize(0);
      }
    }
    // no such creation
    assertSame(null, description.getCreation("noSuchCreation"));
  }

  /**
   * No "creation" element, so use default {@link CreationDescription}.
   */
  public void test_CreationDescription_default() throws Exception {
    prepareMyComponent(ESA, new String[]{"  <description>Some description</description>"});
    waitForAutoBuild();
    ComponentDescription description = getMyDescription();
    // check
    CreationDescription creation = description.getCreation(null);
    assertNotNull(creation);
    assertEquals("Some description", creation.getDescription());
    // it should be in all creations
    assertThat(description.getCreations()).containsExactly(creation);
  }

  /**
   * Load {@link CreationDescription} with specified <code>parameter</code> elements.
   */
  public void test_CreationDescription_withParameters() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "  <creation id='withParameters'>",
        "    <source>NA</source>",
        "    <parameter name='name_1'>value_1</parameter>",
        "    <parameter name='name_2'>value_2</parameter>",
        "  </creation>",});
    waitForAutoBuild();
    ComponentDescription description = getMyDescription();
    // check
    CreationDescription creation = description.getCreation("withParameters");
    assertThat(creation.getParameters()).contains(
        entry("name_1", "value_1"),
        entry("name_2", "value_2"));
  }

  /**
   * Load {@link CreationDescription} with specified <code>x-content</code> element.
   */
  public void test_CreationDescription_withContent() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "  <creation>",
        "    <source>NA</source>",
        "    <x-content>My content</x-content>",
        "  </creation>",});
    waitForAutoBuild();
    ComponentDescription description = getMyDescription();
    // check
    CreationDescription creation = description.getCreation(null);
    assertEquals("My content", creation.getContent());
  }
}