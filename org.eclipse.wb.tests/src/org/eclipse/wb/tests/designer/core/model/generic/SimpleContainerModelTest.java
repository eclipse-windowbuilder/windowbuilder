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
package org.eclipse.wb.tests.designer.core.model.generic;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObjectFactories;
import org.eclipse.wb.core.model.association.AssociationObjectFactory;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerConfigurable;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerConfiguration;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.mock.EasyMockTemplate;
import org.eclipse.wb.tests.designer.tests.mock.MockRunnable;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.IMocksControl;

import java.text.MessageFormat;
import java.util.List;

/**
 * Test for {@link SimpleContainer} and {@link SimpleContainerConfigurable} models.
 * 
 * @author scheglov_ke
 */
public class SimpleContainerModelTest extends SwingModelTest {
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
  // SimpleContainer_Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No special container association, so only {@link Association} from component will be used.
   */
  public void test_getConfigurations_noContainerAssociation() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "NO", "java.awt.Component");
  }

  /**
   * Ask "forCanvas", return common configuration for both - canvas/tree.
   */
  public void test_getConfigurations_forCanvas_common() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "%parent%.add(%child%)", "java.awt.Component");
  }

  /**
   * Ask "forCanvas", return explicit "forCanvas".
   */
  public void test_getConfigurations_forCanvas_explicit() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.canvas", "true"},
            {"simpleContainer.canvas.association", "%parent%.add(%child%)"},
            {"simpleContainer.canvas.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "%parent%.add(%child%)", "java.awt.Component");
  }

  /**
   * Ask "forCanvas", but only "forTree" exist.
   */
  public void test_getConfigurations_forCanvas_onlyForTree() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.tree", "true"},
            {"simpleContainer.tree.association", "%parent%.add(%child%)"},
            {"simpleContainer.tree.component", "java.awt.Component"},});
    assertThat(configurations).isEmpty();
  }

  /**
   * Ask "forTree", return common configuration for both - canvas/tree.
   */
  public void test_getConfigurations_forTree_common() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(false, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
  }

  /**
   * Ask "forTree", return explicit "forTree".
   */
  public void test_getConfigurations_forTree_explicit() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(false, new String[][]{
            {"simpleContainer.tree", "true"},
            {"simpleContainer.tree.association", "%parent%.add(%child%)"},
            {"simpleContainer.tree.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
  }

  /**
   * Several different configurations.
   */
  public void test_getConfigurations_3_count() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component", "java.awt.Component"},
            {"simpleContainer.1", "true"},
            {"simpleContainer.1.association", "%parent%.add(%child%)"},
            {"simpleContainer.1.component", "javax.swing.JButton"},
            {"simpleContainer.5", "true"},
            {"simpleContainer.5.association", "%parent%.add(%child%)"},
            {"simpleContainer.5.component", "javax.swing.JTextField"},});
    assertThat(configurations).hasSize(3);
    assertConfiguration(configurations.get(0), "%parent%.add(%child%)", "java.awt.Component");
    assertConfiguration(configurations.get(1), "%parent%.add(%child%)", "javax.swing.JButton");
    assertConfiguration(configurations.get(2), "%parent%.add(%child%)", "javax.swing.JTextField");
  }

  /**
   * Ignore if <code>simpleContainer</code> value is not <code>true</code>.
   */
  public void test_getConfigurations_ignoreFalse() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "false"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(0);
  }

  /**
   * Use default component/reference validator.
   */
  public void test_getConfigurations_defaultValidators() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.defaultComponent", "java.awt.Component"},
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "%parent%.add(%child%)", "java.awt.Component");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Container validation MVEL scripts
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConfigurations_validateContainer_isContainerType() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.canvas", "isContainerType('java.awt.Component')"},
            {"simpleContainer.canvas.association", "%parent%.add(%child%)"},
            {"simpleContainer.canvas.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "%parent%.add(%child%)", "java.awt.Component");
  }

  public void test_getConfigurations_validateContainer_scriptToFalse() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.canvas", "1 == 2"},
            {"simpleContainer.canvas.association", "%parent%.add(%child%)"},
            {"simpleContainer.canvas.component", "java.awt.Component"},});
    assertThat(configurations).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SimpleContainer_Factory: component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Exception when no "component" validator.
   */
  public void test_getConfigurations_noComponentValidator() throws Exception {
    try {
      getConfigurations(true, new String[][]{
          {"simpleContainer", "true"},
          {"simpleContainer.association", "%parent%.add(%child%)"},});
      fail();
    } catch (Throwable e) {
    }
  }

  public void test_getConfigurations_explicitComponentTypes() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component", "javax.swing.JButton javax.swing.JTextField"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "%parent%.add(%child%)",
        "javax.swing.JButton javax.swing.JTextField");
  }

  /**
   * Use default component validator.
   */
  public void test_getConfigurations_defaultComponent() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.defaultComponent", "java.awt.Component"},
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "%parent%.add(%child%)", "java.awt.Component");
  }

  public void test_getConfigurations_componentValidatorExpression() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component-validator", "isComponentType(java.awt.Component)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "%parent%.add(%child%)",
        "isComponentType(java.awt.Component)");
  }

  public void test_getConfigurations_commandValidatorExpression() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.association", "%parent%.add(%child%)"},
            {"simpleContainer.component-validator", "isComponentType(java.awt.Component)"},
            {"simpleContainer.command-validator", "isGoodCommand()"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "%parent%.add(%child%)",
        "isComponentType(java.awt.Component)");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configurations access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertConfiguration(SimpleContainerConfiguration configuration,
      String association,
      String expectedComponentValidator) {
    assertEquals(association, getAssociationObjectString(configuration));
    if (expectedComponentValidator != null) {
      assertEquals(
          expectedComponentValidator,
          getValidatorString(configuration.getComponentValidator()));
    }
  }

  private static String getAssociationObjectString(SimpleContainerConfiguration configuration) {
    return configuration.getAssociationObjectFactory().toString();
  }

  private static String getValidatorString(Object validator) {
    return validator.toString();
  }

  private List<SimpleContainerConfiguration> getConfigurations(boolean forCanvas,
      String[][] parameters) throws Exception {
    String[] parameterLines = new String[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      String[] parameterPair = parameters[i];
      assertEquals(2, parameterPair.length);
      parameterLines[i] =
          MessageFormat.format(
              "    <parameter name=''{0}''>{1}</parameter>",
              parameterPair[0],
              parameterPair[1]);
    }
    // prepare description
    setFileContentSrc(
        "test/SimplePanel.java",
        getTestSource(
            "public class SimplePanel extends Container {",
            "  public SimplePanel() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/SimplePanel.wbp-component.xml",
        getSource3(new String[]{
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='add'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>"}, parameterLines, new String[]{"  </parameters>", "</component>"}));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends SimplePanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // get configurations
    return new SimpleContainerFactory(panel, forCanvas).getConfigurations();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Duck typing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class MySimpleContainer extends JavaInfo {
    public MySimpleContainer(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
    }

    public List<ObjectInfo> getSimpleContainerChildren() {
      return ImmutableList.of();
    }

    public void command_CREATE(Object component) {
    }

    public void command_ADD(Object component) {
    }
  }

  /**
   * Create/Move operations of {@link SimpleContainerConfigurable} should try to find corresponding
   * CREATE/MOVE methods in container {@link JavaInfo} and use them instead of generic
   * implementation.
   */
  public void test_duckTyping() throws Exception {
    final IMocksControl mocksControl = createStrictControl();
    final JavaInfo component = mocksControl.createMock(JavaInfo.class);
    final MySimpleContainer container = mocksControl.createMock(MySimpleContainer.class);
    final SimpleContainer simpleContainer;
    {
      ContainerObjectValidator validator = ContainerObjectValidators.alwaysTrue();
      SimpleContainerConfiguration configuration =
          new SimpleContainerConfiguration(validator, null);
      simpleContainer = new SimpleContainerConfigurable(container, configuration);
    }
    // isEmpty() == true, because no existing children
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        List<ObjectInfo> children = ImmutableList.<ObjectInfo>of();
        expect(container.getSimpleContainerChildren()).andReturn(children);
      }

      public void codeToTest() throws Exception {
        assertTrue(simpleContainer.isEmpty());
      }
    });
    // isEmpty() == false, because return existing child
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        TestObjectInfo existingChild = new TestObjectInfo();
        List<ObjectInfo> children = ImmutableList.<ObjectInfo>of(existingChild);
        expect(container.getSimpleContainerChildren()).andReturn(children);
      }

      public void codeToTest() throws Exception {
        assertFalse(simpleContainer.isEmpty());
      }
    });
    // getChild() == null, because no existing children
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        List<ObjectInfo> children = ImmutableList.<ObjectInfo>of();
        expect(container.getSimpleContainerChildren()).andReturn(children);
      }

      public void codeToTest() throws Exception {
        assertSame(null, simpleContainer.getChild());
      }
    });
    // getChild() != null, because return existing child
    {
      final TestObjectInfo existingChild = new TestObjectInfo();
      EasyMockTemplate.run(mocksControl, new MockRunnable() {
        public void expectations() throws Exception {
          List<ObjectInfo> children = ImmutableList.<ObjectInfo>of(existingChild);
          expect(container.getSimpleContainerChildren()).andReturn(children);
        }

        public void codeToTest() throws Exception {
          assertSame(existingChild, simpleContainer.getChild());
        }
      });
    }
    // CREATE
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        container.command_CREATE(component);
      }

      public void codeToTest() throws Exception {
        simpleContainer.command_CREATE(component);
      }
    });
    // MOVE
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        container.command_ADD(component);
      }

      public void codeToTest() throws Exception {
        simpleContainer.command_ADD(component);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_validateMethods() throws Exception {
    IMocksControl mocksControl = createStrictControl();
    JavaInfo container = mocksControl.createMock(JavaInfo.class);
    final JavaInfo component = mocksControl.createMock(JavaInfo.class);
    final SimpleContainerConfiguration configuration =
        mocksControl.createMock(SimpleContainerConfiguration.class);
    final SimpleContainer simpleContainer =
        new SimpleContainerConfigurable(container, configuration);
    // validateComponent() = true
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        ContainerObjectValidator validator = ContainerObjectValidators.alwaysTrue();
        expect(configuration.getComponentValidator()).andReturn(validator);
      }

      public void codeToTest() throws Exception {
        assertTrue(simpleContainer.validateComponent(component));
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Models
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link SimpleContainerConfigurable#command_CREATE(Object, Object)}.
   */
  public void test_CREATE() throws Exception {
    prepareSimplePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends SimplePanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare SimpleContainer
    SimpleContainer simpleContainer = new SimpleContainerFactory(panel, true).get().get(0);
    assertTrue(simpleContainer.isEmpty());
    // do CREATE
    ComponentInfo newButton = createJButton();
    {
      assertTrue(simpleContainer.validateComponent(newButton));
    }
    simpleContainer.command_CREATE(newButton);
    assertEditor(
        "// filler filler filler",
        "public class Test extends SimplePanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      setContent(button);",
        "    }",
        "  }",
        "}");
    assertFalse(simpleContainer.isEmpty());
  }

  /**
   * Ensure that we can: create, delete and create new component again.
   */
  public void test_CREATE_twoTimes() throws Exception {
    prepareSimplePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends SimplePanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare SimpleContainer
    SimpleContainer simpleContainer = new SimpleContainerFactory(panel, true).get().get(0);
    assertTrue(simpleContainer.isEmpty());
    // create/delete
    {
      ComponentInfo newButton = createJButton();
      simpleContainer.command_CREATE(newButton);
      newButton.delete();
    }
    // create again
    {
      ComponentInfo newButton = createJButton();
      simpleContainer.command_CREATE(newButton);
    }
    assertEditor(
        "// filler filler filler",
        "public class Test extends SimplePanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      setContent(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link SimpleContainerConfigurable#command_absolute_MOVE(Object, Object)}.
   */
  public void test_MOVE() throws Exception {
    prepareSimplePanel();
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "      SimplePanel SimplePanel = new SimplePanel();",
            "      add(SimplePanel);",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ContainerInfo simplePanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = panel.getChildrenComponents().get(1);
    // prepare SimpleContainer
    SimpleContainer simpleContainer;
    {
      AssociationObjectFactory associationObjectFactory =
          AssociationObjectFactories.invocationChild("%parent%.add(%child%)", false);
      ContainerObjectValidator validator = ContainerObjectValidators.alwaysTrue();
      SimpleContainerConfiguration configuration =
          new SimpleContainerConfiguration(validator, associationObjectFactory);
      simpleContainer = new SimpleContainerConfigurable(simplePanel, configuration);
    }
    // do MOVE
    simpleContainer.command_ADD(button);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    {",
        "      SimplePanel SimplePanel = new SimplePanel();",
        "      add(SimplePanel);",
        "      {",
        "        JButton button = new JButton();",
        "        SimplePanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link SimpleContainer} should automatically copy its child into clipboard.
   */
  public void test_clipboard() throws Exception {
    prepareSimplePanel();
    ContainerInfo rootPanel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      add(panel);",
            "      {",
            "        JButton button = new JButton();",
            "        panel.setContent(button);",
            "      }",
            "    }",
            "  }",
            "}");
    rootPanel.refresh();
    // prepare memento
    JavaInfoMemento memento;
    {
      ContainerInfo panel = (ContainerInfo) rootPanel.getChildrenComponents().get(0);
      memento = JavaInfoMemento.createMemento(panel);
    }
    // do paste
    ContainerInfo newPanel = (ContainerInfo) memento.create(rootPanel);
    ((FlowLayoutInfo) rootPanel.getLayout()).add(newPanel, null);
    memento.apply();
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      add(panel);",
        "      {",
        "        JButton button = new JButton();",
        "        panel.setContent(button);",
        "      }",
        "    }",
        "    {",
        "      SimplePanel simplePanel = new SimplePanel();",
        "      add(simplePanel);",
        "      {",
        "        JButton button = new JButton();",
        "        simplePanel.setContent(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static void prepareSimplePanel() throws Exception {
    prepareSimplePanel_classes();
    AbstractJavaTest.setFileContentSrc(
        "test/SimplePanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setContent'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='simpleContainer'>true</parameter>",
            "    <parameter name='simpleContainer.association'>%parent%.setContent(%child%)</parameter>",
            "    <parameter name='simpleContainer.component'>java.awt.Component</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  static void prepareSimplePanel_classes() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getSourceDQ(
            "package test;",
            "import java.awt.*;",
            "public class MyLayout implements LayoutManager {",
            "  public void addLayoutComponent(String name, Component comp) {",
            "  }",
            "  public  void removeLayoutComponent(Component comp) {",
            "  }",
            "  public Dimension preferredLayoutSize(Container parent) {",
            "    return new Dimension(200, 100);",
            "  }",
            "  public Dimension minimumLayoutSize(Container parent) {",
            "    return new Dimension(200, 100);",
            "  }",
            "  public void layoutContainer(Container parent) {",
            "    int nmembers = parent.getComponentCount();",
            "    for (int i = 0 ; i < nmembers ; i++) {",
            "      Component m = parent.getComponent(i);",
            "      m.setBounds(10, 10, parent.getWidth() - 20, parent.getHeight() - 20);",
            "    }",
            "  }",
            "}"));
    setFileContentSrc(
        "test/SimplePanel.java",
        getSourceDQ(
            "package test;",
            "import java.awt.*;",
            "public class SimplePanel extends Container {",
            "  public SimplePanel() {",
            "    setLayout(new MyLayout());",
            "  }",
            "  public void setContent(Component component) {",
            "    add(component);",
            "  }",
            "}"));
  }
}
