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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObjectFactories;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerConfigurable;
import org.eclipse.wb.internal.core.model.generic.FlowContainerConfiguration;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.mock.EasyMockTemplate;
import org.eclipse.wb.tests.designer.tests.mock.MockRunnable;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;
import org.easymock.IMocksControl;

import java.text.MessageFormat;
import java.util.List;

/**
 * Test for {@link FlowContainer} and {@link FlowContainerConfigurable} models.
 * 
 * @author scheglov_ke
 */
public class FlowContainerModelTest extends SwingModelTest {
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
  // FlowContainer_Factory: horizontal
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConfigurations_horizontal_trueByDefault() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "alwaysTrue", "%parent%.add(%child%)");
  }

  public void test_getConfigurations_horizontal_complexExpression() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "isHorizontal()"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "isHorizontal()", "%parent%.add(%child%)");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer_Factory: association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No special container association, so only {@link Association} from component will be used.
   */
  public void test_getConfigurations_noContainerAssociation() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "true", "NO");
  }

  /**
   * If no known names of association found, then "invocationChild" is implied, but it should start
   * with "%parent%.".
   */
  public void test_getConfigurations_invalidInvocationChild() throws Exception {
    try {
      getConfigurations(true, new String[][]{
          {"flowContainer", "true"},
          {"flowContainer.horizontal", "true"},
          {"flowContainer.association", "somethingWrong"},
          {"flowContainer.component", "java.awt.Component"},
          {"flowContainer.reference", "java.awt.Component"},});
      fail();
    } catch (Throwable e) {
    }
  }

  /**
   * Specify "invocationChild" association explicitly.
   */
  public void test_getConfigurations_explicitInvocationChild() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "invocationChild %parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer_Factory: component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Exception when no "component" validator.
   */
  public void test_getConfigurations_noComponentValidator() throws Exception {
    try {
      getConfigurations(true, new String[][]{
          {"flowContainer", "true"},
          {"flowContainer.horizontal", "true"},
          {"flowContainer.association", "%parent%.add(%child%)"},});
      fail();
    } catch (Throwable e) {
    }
  }

  public void test_getConfigurations_explicitComponentTypes() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "javax.swing.JButton javax.swing.JTextField"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "javax.swing.JButton javax.swing.JTextField",
        "java.awt.Component");
  }

  /**
   * Use default component validator.
   */
  public void test_getConfigurations_defaultComponent() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer.defaultComponent", "java.awt.Component"},
            {"flowContainer.defaultReference", "java.awt.Component"},
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
  }

  public void test_getConfigurations_componentValidatorExpression() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component-validator", "isComponentType(java.awt.Component)"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "isComponentType(java.awt.Component)",
        "java.awt.Component");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer_Factory: reference
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No reference expression or string. So, error.
   */
  public void test_getConfigurations_noReference() throws Exception {
    try {
      getConfigurations(true, new String[][]{
          {"flowContainer.defaultComponent", "java.awt.Component"},
          {"flowContainer", "true"},
          {"flowContainer.horizontal", "true"},
          {"flowContainer.association", "%parent%.add(%child%)"},});
      fail();
    } catch (Throwable e) {
    }
  }

  public void test_getConfigurations_explicitReferenceTypes() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "javax.swing.JButton javax.swing.JTextField"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "javax.swing.JButton javax.swing.JTextField");
  }

  public void test_getConfigurations_referenceValidatorExpression() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference-validator", "isReferenceType(java.awt.Component)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "isReferenceType(java.awt.Component)");
  }

  /**
   * When no "reference" specified, same predicate as for "component" should be used.
   */
  public void test_getConfigurations_referencesAsComponents() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
  }

  /**
   * Use default reference validator.
   */
  public void test_getConfigurations_defaultReference() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer.defaultComponent", "java.awt.Component"},
            {"flowContainer.defaultReference", "java.awt.Component"},
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer_Factory: canvas/tree parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ask "forCanvas", return common configuration for both - canvas/tree.
   */
  public void test_getConfigurations_forCanvas_common() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
  }

  /**
   * Ask "forCanvas", return explicit "forCanvas".
   */
  public void test_getConfigurations_forCanvas_explicit() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer.canvas", "true"},
            {"flowContainer.canvas.horizontal", "true"},
            {"flowContainer.canvas.association", "%parent%.add(%child%)"},
            {"flowContainer.canvas.component", "java.awt.Component"},
            {"flowContainer.canvas.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
  }

  /**
   * Ask "forCanvas", but only "forTree" exist.
   */
  public void test_getConfigurations_forCanvas_onlyForTree() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer.tree", "true"},
            {"flowContainer.tree.horizontal", "true"},
            {"flowContainer.tree.association", "%parent%.add(%child%)"},
            {"flowContainer.tree.component", "java.awt.Component"},
            {"flowContainer.tree.reference", "java.awt.Component"},});
    assertThat(configurations).isEmpty();
  }

  /**
   * Ask "forTree", return common configuration for both - canvas/tree.
   */
  public void test_getConfigurations_forTree_common() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(false, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
  }

  /**
   * Ask "forTree", return explicit "forTree".
   */
  public void test_getConfigurations_forTree_explicit() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(false, new String[][]{
            {"flowContainer.tree", "true"},
            {"flowContainer.tree.horizontal", "true"},
            {"flowContainer.tree.association", "%parent%.add(%child%)"},
            {"flowContainer.tree.component", "java.awt.Component"},
            {"flowContainer.tree.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
  }

  /**
   * Several different configurations.
   */
  public void test_getConfigurations_3_count() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},
            {"flowContainer.1", "true"},
            {"flowContainer.1.horizontal", "false"},
            {"flowContainer.1.association", "%parent%.add(%child%)"},
            {"flowContainer.1.component", "javax.swing.JButton"},
            {"flowContainer.1.reference", "java.awt.Component"},
            {"flowContainer.5", "true"},
            {"flowContainer.5.horizontal", "true"},
            {"flowContainer.5.association", "%parent%.add(%child%)"},
            {"flowContainer.5.component", "javax.swing.JTextField"},
            {"flowContainer.5.reference", "javax.swing.JTextField"},});
    assertThat(configurations).hasSize(3);
    assertConfiguration(
        configurations.get(0),
        "true",
        "%parent%.add(%child%)",
        "java.awt.Component",
        "java.awt.Component");
    assertConfiguration(
        configurations.get(1),
        "false",
        "%parent%.add(%child%)",
        "javax.swing.JButton",
        "java.awt.Component");
    assertConfiguration(
        configurations.get(2),
        "true",
        "%parent%.add(%child%)",
        "javax.swing.JTextField",
        "javax.swing.JTextField");
  }

  /**
   * Ignore if <code>flowContainer</code> value is not <code>true</code>.
   */
  public void test_getConfigurations_ignoreFalse() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "false"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.association", "%parent%.add(%child%)"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer_Factory: assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertConfiguration(FlowContainerConfiguration configuration,
      String horizontal,
      String association) throws Exception {
    assertConfiguration(configuration, horizontal, association, null, null);
  }

  private static void assertConfiguration(FlowContainerConfiguration configuration,
      String horizontal,
      String association,
      String expectedComponentValidator,
      String expectedReferenceValidator) throws Exception {
    assertEquals(horizontal, getHorizontalPredicateString(configuration));
    assertEquals(association, getAssociationObjectString(configuration));
    if (expectedComponentValidator != null) {
      assertEquals(
          expectedComponentValidator,
          getValidatorString(configuration.getComponentValidator()));
    }
    if (expectedReferenceValidator != null) {
      assertEquals(
          expectedReferenceValidator,
          getValidatorString(configuration.getReferenceValidator()));
    }
  }

  private static String getValidatorString(Object validator) {
    return validator.toString();
  }

  private static String getHorizontalPredicateString(FlowContainerConfiguration configuration) {
    Predicate<Object> predicate = configuration.getHorizontalPredicate();
    if (predicate == Predicates.alwaysTrue()) {
      return "alwaysTrue";
    }
    return predicate.toString();
  }

  private static String getAssociationObjectString(FlowContainerConfiguration configuration)
      throws Exception {
    return configuration.getAssociationObjectFactory().toString();
  }

  private List<FlowContainerConfiguration> getConfigurations(boolean forCanvas,
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
        "test/FlowPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class FlowPanel extends Container {",
            "  public FlowPanel() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/FlowPanel.wbp-component.xml",
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
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // get configurations
    return new FlowContainerFactory(panel, forCanvas).getConfigurations();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Duck typing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class MyFlowContainer extends JavaInfo {
    public MyFlowContainer(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
    }

    public void command_CREATE(Object component, Object nextComponent) {
    }

    public void command_CREATE_after(Object component, Object nextComponent) {
    }

    public void command_MOVE(Object component, Object nextComponent) {
    }

    public void command_MOVE_after(Object component, Object nextComponent) {
    }
  }
  private static class MyFlowContainer_useMostSpecific extends JavaInfo {
    public MyFlowContainer_useMostSpecific(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
    }

    @SuppressWarnings("unused")
    public void command_CREATE(Object component, Object nextComponent) {
    }

    public void command_CREATE(JavaInfo component, Object nextComponent) {
    }
  }

  /**
   * Create/Move operations of {@link FlowContainerConfigurable} should try to find corresponding
   * CREATE/MOVE methods in container {@link JavaInfo} and use them instead of generic
   * implementation.
   */
  public void test_duckTyping() throws Exception {
    IMocksControl mocksControl = createStrictControl();
    final JavaInfo component = mocksControl.createMock(JavaInfo.class);
    final JavaInfo nextComponent = mocksControl.createMock(JavaInfo.class);
    final MyFlowContainer container = mocksControl.createMock(MyFlowContainer.class);
    final FlowContainer flowContainer =
        new FlowContainerConfigurable(container,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                null,
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue(),
                StringUtils.EMPTY));
    // CREATE
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        container.command_CREATE(component, nextComponent);
        container.command_CREATE_after(component, nextComponent);
      }

      public void codeToTest() throws Exception {
        flowContainer.command_CREATE(component, nextComponent);
      }
    });
    // MOVE
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        container.command_MOVE(component, nextComponent);
        container.command_MOVE_after(component, nextComponent);
      }

      public void codeToTest() throws Exception {
        flowContainer.command_MOVE(component, nextComponent);
      }
    });
  }

  /**
   * Test that most specific version "command_CREATE" method is used.
   */
  public void test_duckTyping_useMostSpecific() throws Exception {
    IMocksControl mocksControl = createStrictControl();
    final JavaInfo component = mocksControl.createMock(JavaInfo.class);
    final JavaInfo nextComponent = mocksControl.createMock(JavaInfo.class);
    final MyFlowContainer_useMostSpecific container =
        mocksControl.createMock(MyFlowContainer_useMostSpecific.class);
    final FlowContainer flowContainer =
        new FlowContainerConfigurable(container,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                null,
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue(),
                StringUtils.EMPTY));
    // CREATE
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        container.command_CREATE(component, nextComponent);
      }

      public void codeToTest() throws Exception {
        flowContainer.command_CREATE(component, nextComponent);
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
    final JavaInfo reference = mocksControl.createMock(JavaInfo.class);
    final FlowContainerConfiguration configuration =
        mocksControl.createMock(FlowContainerConfiguration.class);
    final FlowContainer flowContainer = new FlowContainerConfigurable(container, configuration);
    // isHorizontal()
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        Predicate<Object> horizontalPredicate = Predicates.alwaysTrue();
        expect(configuration.getHorizontalPredicate()).andReturn(horizontalPredicate);
      }

      public void codeToTest() throws Exception {
        assertTrue(flowContainer.isHorizontal());
      }
    });
    // validateComponent() = true
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        expect(configuration.getComponentValidator()).andReturn(
            ContainerObjectValidators.alwaysTrue());
      }

      public void codeToTest() throws Exception {
        assertTrue(flowContainer.validateComponent(component));
      }
    });
    // validateReference() = false
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        expect(configuration.getReferenceValidator()).andReturn(
            ContainerObjectValidators.alwaysTrue());
      }

      public void codeToTest() throws Exception {
        assertTrue(flowContainer.validateReference(reference));
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Models
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FlowContainerConfigurable#command_CREATE(Object, Object)}.
   */
  public void test_CREATE() throws Exception {
    prepareFlowPanel();
    ContainerInfo panel =
        parseContainer(
            "class Test extends FlowPanel {",
            "  Test() {",
            "    {",
            "      JButton button_0 = new JButton();",
            "      add(button_0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_0 = panel.getChildrenComponents().get(0);
    // prepare FlowContainer
    FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
    // do CREATE
    ComponentInfo newButton = createJButton();
    {
      assertTrue(flowContainer.isHorizontal());
      assertTrue(flowContainer.validateComponent(newButton));
      assertTrue(flowContainer.validateReference(button_0));
    }
    flowContainer.command_CREATE(newButton, button_0);
    assertEditor(
        "class Test extends FlowPanel {",
        "  Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button_0 = new JButton();",
        "      add(button_0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Ensure that we can: create, delete and create new component again.
   */
  public void test_CREATE_twoTimes() throws Exception {
    prepareFlowPanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare FlowContainer
    FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
    // create/delete
    {
      ComponentInfo newButton = createJButton();
      flowContainer.command_CREATE(newButton, null);
      newButton.delete();
    }
    // create again
    {
      ComponentInfo newButton = createJButton();
      flowContainer.command_CREATE(newButton, null);
    }
    assertEditor(
        "// filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link FlowContainerConfigurable#command_MOVE(Object, Object)}.
   */
  public void test_MOVE() throws Exception {
    prepareFlowPanel();
    ContainerInfo panel =
        parseContainer(
            "class Test extends FlowPanel {",
            "  Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // prepare FlowContainer
    FlowContainer flowContainer =
        new FlowContainerConfigurable(panel,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                AssociationObjectFactories.invocationChild("%parent%.add(%child%)", false),
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue(),
                StringUtils.EMPTY));
    // do MOVE
    flowContainer.command_MOVE(button_2, button_1);
    assertEditor(
        "class Test extends FlowPanel {",
        "  Test() {",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static void prepareFlowPanel() throws Exception {
    prepareFlowPanel_classes();
    AbstractJavaProjectTest.setFileContentSrc(
        "test/FlowPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='add'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='flowContainer'>true</parameter>",
            "    <parameter name='flowContainer.horizontal'>true</parameter>",
            "    <parameter name='flowContainer.association'>%parent%.add(%child%)</parameter>",
            "    <parameter name='flowContainer.component'>java.awt.Component</parameter>",
            "    <parameter name='flowContainer.reference'>java.awt.Component</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  static void prepareFlowPanel_classes() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getSource(
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
            "      m.setBounds(25 + 100 * i, 10, 90, 50);",
            "    }",
            "  }",
            "}"));
    setFileContentSrc(
        "test/FlowPanel.java",
        getSource(
            "package test;",
            "import java.awt.*;",
            "public class FlowPanel extends Container {",
            "  public FlowPanel() {",
            "    setLayout(new MyLayout());",
            "  }",
            "}"));
  }
}
