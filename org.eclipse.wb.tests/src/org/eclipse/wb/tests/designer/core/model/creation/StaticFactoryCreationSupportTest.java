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
package org.eclipse.wb.tests.designer.core.model.creation;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.AbstractExplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FactoryAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.editor.icon.IconPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.IMethod;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Test for {@link StaticFactoryCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class StaticFactoryCreationSupportTest extends SwingModelTest {
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
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse, check for parameters binding to properties.
   */
  public void test_parse() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'SF button'",
            "  */",
            "  public static JButton createButton(String text, Icon icon) {",
            "    return new JButton(text, icon);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public final class Test extends JPanel {",
            "  Test() {",
            "    add(StaticFactory.createButton('button', null));",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check creation support
    {
      StaticFactoryCreationSupport creationSupport =
          (StaticFactoryCreationSupport) button.getCreationSupport();
      assertEquals(
          "static factory: test.StaticFactory createButton(java.lang.String,javax.swing.Icon)",
          creationSupport.toString());
      assertTrue(creationSupport.canDelete());
      assertTrue(creationSupport.getNode() != null);
      assertInstanceOf(
          StaticFactoryCreationSupport.class,
          creationSupport.getLiveComponentCreation());
      // check description
      FactoryMethodDescription description = creationSupport.getDescription();
      assertTrue(description.isFactory());
      assertEquals("test.StaticFactory", description.getDeclaringClass().getName());
      assertEquals("javax.swing.JButton", description.getReturnClass().getName());
      assertEquals(2, description.getParameters().size());
      // check parameter "0"
      {
        ParameterDescription parameter = description.getParameter(0);
        assertEquals("text", parameter.getName());
        assertEquals("\"SF button\"", parameter.getDefaultSource());
        assertEquals("setText(java.lang.String)", parameter.getProperty());
        assertNotNull(parameter.toString());
        // check converter/editor
        assertInstanceOf(StringConverter.class, parameter.getConverter());
        assertInstanceOf(StringPropertyEditor.class, parameter.getEditor());
      }
      // check parameter "1"
      {
        ParameterDescription parameter = description.getParameter(1);
        assertEquals("icon", parameter.getName());
        assertEquals("setIcon(javax.swing.Icon)", parameter.getProperty());
        // check converter/editor
        assertSame(null, parameter.getConverter());
        assertInstanceOf(IconPropertyEditor.class, parameter.getEditor());
      }
    }
    // test accessors
    {
      GenericProperty textProperty = (GenericProperty) button.getPropertyByTitle("text");
      List<ExpressionAccessor> accessors = getGenericPropertyAccessors(textProperty);
      assertEquals(2, accessors.size());
      assertInstanceOf(SetterAccessor.class, accessors.get(0));
      assertInstanceOf(FactoryAccessor.class, accessors.get(1));
      // static factory accessor makes property modified
      assertTrue(textProperty.isModified());
      assertEquals("button", textProperty.getValue());
      // check modification
      {
        // pre-check
        assertRelatedNodes(
            button,
            new String[]{"add(StaticFactory.createButton(\"button\", null))"});
        // modify property and check source
        textProperty.setValue("12345");
        assertRelatedNodes(button, new String[]{"add(StaticFactory.createButton(\"12345\", null))"});
        // set to default
        textProperty.setValue(Property.UNKNOWN_VALUE);
        assertRelatedNodes(
            button,
            new String[]{"add(StaticFactory.createButton(\"SF button\", null))"});
      }
    }
    // check delete
    assertTrue(button.canDelete());
    button.delete();
    assertEditor(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  Test() {",
        "  }",
        "}");
  }

  /**
   * Method with good JavaDoc comments. Argument of factory is bound to "text" property, but without
   * default value.
   */
  public void test_parse2() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  Test() {",
            "    add(StaticFactory.createButton('button'));",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // default value is not set, so "null" used as default "default" value
    GenericProperty textProperty = (GenericProperty) button.getPropertyByTitle("text");
    textProperty.setValue(Property.UNKNOWN_VALUE);
    assertRelatedNodes(button, new String[]{"add(StaticFactory.createButton((String) null))"});
  }

  /**
   * Users want factory-specific tweaks for properties.
   */
  public void test_parse_factoryMethodSpecific_ComponentDescription() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.createButton__.wbp-component.xml",
        getSource(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <properties-preferred names='enabled'/>",
            "</component>"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = StaticFactory.createButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    JavaInfo button = getJavaInfoByName("button");
    // setEnabled(boolean) marked as "preferred"
    {
      GenericPropertyDescription propertyDescription =
          button.getDescription().getProperty("setEnabled(boolean)");
      assertSame(PropertyCategory.PREFERRED, propertyDescription.getCategory());
    }
  }

  /**
   * Method without any description.
   */
  public void test_parse_noFactory() throws Exception {
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "public class StaticFactory_ {",
            "  public static JButton createButton_noFactory() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(StaticFactory_.createButton_noFactory());",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).isEmpty();
  }

  /**
   * Parameter has name "parent", but in reality is not parent, and this may cause
   * {@link NullPointerException} sometimes.
   */
  public void test_parse_invalidParent() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public class StaticFactory {",
            "  public static JButton createButton(int parent) {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    StaticFactory.createButton(0);",
            "  }",
            "}");
    // no component
    assertThat(panel.getChildrenComponents()).isEmpty();
    // warning was logged
    {
      List<EditorWarning> warnings = m_lastState.getWarnings();
      assertThat(warnings).hasSize(1);
      EditorWarning warning = warnings.get(0);
      assertEquals("No parent model for StaticFactory.createButton(0)", warning.getMessage());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding.
   */
  public void test_add() throws Exception {
    // prepare factory
    createModelCompilationUnit(
        "test",
        "StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text_0 '000'",
            "  * @wbp.factory.parameter.source 1 '111'",
            "  */",
            "  public static JButton createButton2(String text_0, String text_1, String text_2, String text_3) {",
            "    return new JButton(text_0, null);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton2'>",
            "    <parameter type='java.lang.String'/>",
            "    <parameter type='java.lang.String'/>",
            "    <parameter type='java.lang.String' defaultSource='\"222\"'/>",
            "    <parameter type='java.lang.String'/>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // prepare new component
    ComponentInfo newComponent;
    {
      FactoryMethodDescription description =
          FactoryDescriptionHelper.getDescription(
              m_lastEditor,
              m_lastLoader.loadClass("test.StaticFactory"),
              "createButton2(java.lang.String,java.lang.String,java.lang.String,java.lang.String)",
              true);
      assertEquals("\"000\"", description.getParameter(0).getDefaultSource());
      assertEquals("\"111\"", description.getParameter(1).getDefaultSource());
      assertEquals("\"222\"", description.getParameter(2).getDefaultSource());
      assertEquals("(java.lang.String) null", description.getParameter(3).getDefaultSource());
      //
      newComponent =
          (ComponentInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              JButton.class,
              new StaticFactoryCreationSupport(description));
    }
    // add component
    {
      SwingTestUtils.setGenerations(
          LocalUniqueVariableDescription.INSTANCE,
          BlockStatementGeneratorDescription.INSTANCE);
      try {
        flowLayout.add(newComponent, null);
      } finally {
        SwingTestUtils.setGenerationDefaults();
      }
    }
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = StaticFactory.createButton2('000', '111', '222', (String) null);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for adding, with invocation to add.
   */
  public void test_add_withInvocation() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <invocation signature='setText(java.lang.String)'><![CDATA['Static Button']]></invocation>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // prepare new component
    ComponentInfo newComponent;
    {
      FactoryMethodDescription description =
          FactoryDescriptionHelper.getDescription(
              m_lastEditor,
              m_lastLoader.loadClass("test.StaticFactory"),
              "createButton()",
              true);
      newComponent =
          (ComponentInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              JButton.class,
              new StaticFactoryCreationSupport(description));
    }
    // add component
    flowLayout.add(newComponent, null);
    // check source
    assertAST(m_lastEditor);
    assertEquals(
        getTestSource(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = StaticFactory.createButton();",
            "      add(button);",
            "      button.setText('Static Button');",
            "    }",
            "  }",
            "}"),
        m_lastEditor.getSource());
  }

  /**
   * Test for adding using local static factory method.
   */
  public void test_add_localFactoryMethod() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  private static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}");
    panel.refresh();
    //
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // prepare new component
    ComponentInfo newComponent;
    {
      FactoryMethodDescription description =
          FactoryDescriptionHelper.getDescription(
              m_lastEditor,
              m_lastLoader.loadClass("test.Test"),
              "createButton()",
              true);
      newComponent =
          (ComponentInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              JButton.class,
              new StaticFactoryCreationSupport(description));
    }
    // add component
    flowLayout.add(newComponent, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = createButton();",
        "      add(button);",
        "    }",
        "  }",
        "  /**",
        "  * @wbp.factory",
        "  */",
        "  private static JButton createButton() {",
        "    return new JButton();",
        "  }",
        "}");
  }

  /**
   * Test for automatic adding {@link StaticFactoryEntryInfo} for local factory methods.
   */
  public void test_parse_localFactoryMethod_contributeToPalette() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  Test() {",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  private static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}");
    // prepare "Local Factories" category
    CategoryInfo factoriesCategory;
    {
      List<CategoryInfo> categories = Lists.newArrayList();
      panel.getBroadcast(PaletteEventListener.class).categories(categories);
      assertThat(categories).hasSize(1);
      factoriesCategory = categories.get(0);
      assertEquals("Local Factories", factoriesCategory.getName());
    }
    // check entry for "createButton()"
    {
      List<EntryInfo> entries = factoriesCategory.getEntries();
      assertThat(entries).hasSize(1);
      StaticFactoryEntryInfo factoryEntry = (StaticFactoryEntryInfo) entries.get(0);
      assertEquals("test.Test", factoryEntry.getFactoryClassName());
      assertEquals("createButton(java.lang.String)", factoryEntry.getMethodSignature());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractExplicitFactoryCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_CREATE_false() throws Exception {
    canUseParent_prepare_createButton();
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public final class Test extends JFrame {",
            "  Test() {",
            "  }",
            "}");
    // createButton() requires JPanel, but we given JFrame
    ComponentInfo button = canUseParent_createButton();
    {
      CreationSupport creationSupport = button.getCreationSupport();
      assertFalse(creationSupport.canUseParent(frame));
      assertFalse(creationSupport.canUseParent(frame));
    }
  }

  /**
   * Test for {@link AbstractExplicitFactoryCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_CREATE_true() throws Exception {
    canUseParent_prepare_createButton();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public final class Test extends JPanel {",
            "  Test() {",
            "  }",
            "}");
    // createButton() requires JPanel, we given it
    ComponentInfo button = canUseParent_createButton();
    {
      CreationSupport creationSupport = button.getCreationSupport();
      assertTrue(creationSupport.canUseParent(panel));
      assertTrue(creationSupport.canUseParent(panel));
    }
  }

  /**
   * Test for {@link AbstractExplicitFactoryCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_MOVE_true() throws Exception {
    canUseParent_prepare_createButton();
    // parse source
    ContainerInfo frame =
        parseContainer(
            "public final class Test extends JFrame {",
            "  Test() {",
            "    {",
            "      JPanel panel_1 = new JPanel();",
            "      getContentPane().add(panel_1);",
            "      panel_1.add(StaticFactory.createButton(panel_1));",
            "    }",
            "    {",
            "      JPanel panel_2 = new JPanel();",
            "      getContentPane().add(panel_2);",
            "    }",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ContainerInfo panel_1 = (ContainerInfo) contentPane.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) contentPane.getChildrenComponents().get(1);
    ComponentInfo button = panel_1.getChildrenComponents().get(0);
    // createButton() requires JPanel, we given it
    {
      CreationSupport creationSupport = button.getCreationSupport();
      assertTrue(creationSupport.canUseParent(panel_2));
      assertTrue(creationSupport.canUseParent(panel_2));
    }
  }

  /**
   * Test for {@link AbstractExplicitFactoryCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_MOVE_false() throws Exception {
    canUseParent_prepare_createButton();
    // parse source
    ContainerInfo frame =
        parseContainer(
            "public final class Test extends JFrame {",
            "  Test() {",
            "    {",
            "      JPanel panel_1 = new JPanel();",
            "      getContentPane().add(panel_1);",
            "      panel_1.add(StaticFactory.createButton(panel_1));",
            "    }",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ContainerInfo panel_1 = (ContainerInfo) contentPane.getChildrenComponents().get(0);
    ComponentInfo button = panel_1.getChildrenComponents().get(0);
    // createButton() requires JPanel, but we given JFrame
    {
      CreationSupport creationSupport = button.getCreationSupport();
      assertFalse(creationSupport.canUseParent(frame));
      assertFalse(creationSupport.canUseParent(frame));
    }
  }

  private void canUseParent_prepare_createButton() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(JPanel parent) {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  private ComponentInfo canUseParent_createButton() throws Exception {
    FactoryMethodDescription description =
        FactoryDescriptionHelper.getDescription(
            m_lastEditor,
            m_lastLoader.loadClass("test.StaticFactory"),
            "createButton(javax.swing.JPanel)",
            true);
    return (ComponentInfo) JavaInfoUtils.createJavaInfo(
        m_lastEditor,
        JButton.class,
        new StaticFactoryCreationSupport(description));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Local static factory method
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using local static factory method. Use it one time.
   */
  public void test_parse_localFactoryMethod_singleInvocation() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  Test() {",
            "    add(createButton('A'));",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  private static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(createButton('A'))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {static factory: test.Test createButton(java.lang.String)} {empty} {/add(createButton('A'))/}");
    // do refresh();
    panel.refresh();
    assertNoErrors(panel);
    {
      JPanel panelObject = (JPanel) panel.getObject();
      assertThat(panelObject.getComponents()).hasSize(1);
    }
  }

  /**
   * Test for using local static factory method. Use it two times.
   */
  public void test_parse_localFactoryMethod_twoInvocations() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  Test() {",
            "    add(createButton('A'));",
            "    add(createButton('B'));",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  private static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(createButton('A'))/ /add(createButton('B'))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {static factory: test.Test createButton(java.lang.String)} {empty} {/add(createButton('A'))/}",
        "  {static factory: test.Test createButton(java.lang.String)} {empty} {/add(createButton('B'))/}");
    // do refresh();
    panel.refresh();
    assertNoErrors(panel);
    {
      JPanel panelObject = (JPanel) panel.getObject();
      assertThat(panelObject.getComponents()).hasSize(2);
    }
  }

  /**
   * Test for using local static factory method. Somehow absence of constructor prevents this.
   * <p>
   * Problem is that when there are no constructor, we add it. But after this
   * {@link IMethod#getSource()} returns invalid source - it seems that updated buffer used, but
   * source range are old. I don't know how to synchronize {@link IMethod} model with updated
   * buffer, so I've added save after adding constructor.
   */
  public void test_parse_localFactoryMethod_noConstructor() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends BasePanel {",
            "  protected void createContents() {",
            "    add(createButton('A'));",
            "    add(createButton('B'));",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  private static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(createButton('A'))/ /add(createButton('B'))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {static factory: test.Test createButton(java.lang.String)} {empty} {/add(createButton('A'))/}",
        "  {static factory: test.Test createButton(java.lang.String)} {empty} {/add(createButton('B'))/}");
    // do refresh();
    panel.refresh();
    assertNoErrors(panel);
    {
      JPanel panelObject = (JPanel) panel.getObject();
      assertThat(panelObject.getComponents()).hasSize(2);
    }
  }
}
