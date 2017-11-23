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
package org.eclipse.wb.tests.designer.XML.model.generic;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerConfigurable;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerConfiguration;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;
import org.eclipse.wb.tests.designer.tests.mock.EasyMockTemplate;
import org.eclipse.wb.tests.designer.tests.mock.MockRunnable;

import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.text.MessageFormat;
import java.util.List;

/**
 * Test for {@link FlowContainer} and {@link FlowContainerConfigurable} models.
 * 
 * @author scheglov_ke
 */
public class FlowContainerModelTest extends AbstractCoreTest {
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
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "alwaysTrue", "direct");
  }

  public void test_getConfigurations_horizontal_complexExpression() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "isHorizontal()"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "isHorizontal()", "direct");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer_Factory: association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No special container association, so "direct" is used.
   */
  public void test_getConfigurations_association_implicitDirect() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "true", "direct");
  }

  /**
   * The "property" association.
   */
  public void test_getConfigurations_association_property() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.x-association", "property myProperty"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "true", "property myProperty");
  }

  /**
   * The "inter" association.
   */
  public void test_getConfigurations_association_inter() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.x-association", "inter myName attrA='a a' attrB='b'"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "true", "inter myName {attrA=a a, attrB=b}");
  }

  public void test_getConfigurations_association_bad() throws Exception {
    try {
      getConfigurations(true, new String[][]{
          {"flowContainer", "true"},
          {"flowContainer.horizontal", "true"},
          {"flowContainer.x-association", "bad association text"},
          {"flowContainer.component", "java.awt.Component"},
          {"flowContainer.reference", "java.awt.Component"},});
      fail();
    } catch (Throwable e) {
      Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
      assertThat(rootCause).isExactlyInstanceOf(AssertionFailedException.class);
    }
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
          {"flowContainer.horizontal", "true"},});
      fail();
    } catch (Throwable e) {
    }
  }

  public void test_getConfigurations_explicitComponentTypes() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.component", "javax.swing.JButton javax.swing.JTextField"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
            {"flowContainer.horizontal", "true"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
        "java.awt.Component",
        "java.awt.Component");
  }

  public void test_getConfigurations_componentValidatorExpression() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.component-validator", "isComponentType(java.awt.Component)"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
          {"flowContainer.horizontal", "true"},});
      fail();
    } catch (Throwable e) {
    }
  }

  public void test_getConfigurations_explicitReferenceTypes() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "javax.swing.JButton javax.swing.JTextField"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
        "java.awt.Component",
        "javax.swing.JButton javax.swing.JTextField");
  }

  public void test_getConfigurations_referenceValidatorExpression() throws Exception {
    List<FlowContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"flowContainer", "true"},
            {"flowContainer.horizontal", "true"},
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference-validator", "isReferenceType(java.awt.Component)"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
            {"flowContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
            {"flowContainer.horizontal", "true"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
            {"flowContainer.canvas.component", "java.awt.Component"},
            {"flowContainer.canvas.reference", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
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
            {"flowContainer.component", "java.awt.Component"},
            {"flowContainer.reference", "java.awt.Component"},
            {"flowContainer.1", "true"},
            {"flowContainer.1.horizontal", "false"},
            {"flowContainer.1.component", "javax.swing.JButton"},
            {"flowContainer.1.reference", "java.awt.Component"},
            {"flowContainer.5", "true"},
            {"flowContainer.5.horizontal", "true"},
            {"flowContainer.5.component", "javax.swing.JTextField"},
            {"flowContainer.5.reference", "javax.swing.JTextField"},});
    assertThat(configurations).hasSize(3);
    assertConfiguration(
        configurations.get(0),
        "true",
        "direct",
        "java.awt.Component",
        "java.awt.Component");
    assertConfiguration(
        configurations.get(1),
        "false",
        "direct",
        "javax.swing.JButton",
        "java.awt.Component");
    assertConfiguration(
        configurations.get(2),
        "true",
        "direct",
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
    assertEquals(association, getAssociationString(configuration));
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

  private static String getAssociationString(FlowContainerConfiguration configuration)
      throws Exception {
    return configuration.getAssociation().toString();
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
    prepareMyComponent();
    setFileContentSrc(
        "test/MyComponent.wbp-component.xml",
        getSource3(new String[]{
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>"}, parameterLines, new String[]{"  </parameters>", "</component>"}));
    waitForAutoBuild();
    // parse
    XmlObjectInfo panel = parse("<t:MyComponent/>");
    // get configurations
    return new FlowContainerFactory(panel, forCanvas).getConfigurations();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Duck typing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class MyFlowContainer extends XmlObjectInfo {
    public MyFlowContainer(EditorContext context,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(context, description, creationSupport);
    }

    public void command_CREATE(Object component, Object nextComponent) {
    }

    public void command_CREATE_after(Object component, Object nextComponent) {
    }

    public void command_MOVE(Object component, Object nextComponent) {
    }

    public void command_MOVE_after(Object component, Object nextComponent) {
    }

    public void command_APPEND_after(Object component, Object nextComponent) {
    }
  }
  private static class MyFlowContainerForAdd extends XmlObjectInfo {
    public MyFlowContainerForAdd(EditorContext context,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(context, description, creationSupport);
    }

    public void command_MOVE_after(Object component, Object nextComponent) {
    }

    public void command_ADD_after(Object component, Object nextComponent) {
    }

    public void command_APPEND_after(Object component, Object nextComponent) {
    }
  }
  private static class MyFlowContainer_useMostSpecific extends XmlObjectInfo {
    public MyFlowContainer_useMostSpecific(EditorContext context,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(context, description, creationSupport);
    }

    @SuppressWarnings("unused")
    public void command_CREATE(Object component, Object nextComponent) {
    }

    public void command_CREATE(XmlObjectInfo component, Object nextComponent) {
    }
  }

  /**
   * Create/Move operations of {@link FlowContainerConfigurable} should try to find corresponding
   * CREATE/MOVE methods in container {@link JavaInfo} and use them instead of generic
   * implementation.
   */
  public void test_duckTyping() throws Exception {
    IMocksControl mocksControl = EasyMock.createStrictControl();
    final XmlObjectInfo component = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo nextComponent = mocksControl.createMock(XmlObjectInfo.class);
    final MyFlowContainer container = mocksControl.createMock(MyFlowContainer.class);
    final FlowContainer flowContainer =
        new FlowContainerConfigurable(container,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                Associations.direct(),
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue()));
    // CREATE
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        container.command_CREATE(component, nextComponent);
        container.command_CREATE_after(component, nextComponent);
        container.command_APPEND_after(component, nextComponent);
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
   * After ADD operation method "command_ADD_after" should be called.
   */
  public void test_duckTyping_ADD() throws Exception {
    IMocksControl mocksControl = EasyMock.createNiceControl();
    final XmlObjectInfo component = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo oldContainer = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo nextComponent = mocksControl.createMock(XmlObjectInfo.class);
    final MyFlowContainerForAdd container = mocksControl.createMock(MyFlowContainerForAdd.class);
    final FlowContainer flowContainer =
        new FlowContainerConfigurable(container,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                Associations.direct(),
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue()));
    // MOVE (as ADD)
    EasyMockTemplate.run(mocksControl, new MockRunnable() {
      public void expectations() throws Exception {
        ReflectionUtils.setField(component, "m_parent", oldContainer);
        container.command_ADD_after(component, nextComponent);
        container.command_APPEND_after(component, nextComponent);
        container.command_MOVE_after(component, nextComponent);
      }

      public void codeToTest() throws Exception {
        System.setProperty("flowContainer.simulateMove", "true");
        try {
          flowContainer.command_MOVE(component, nextComponent);
        } finally {
          System.clearProperty("flowContainer.simulateMove");
        }
      }
    });
  }

  /**
   * Test that most specific version "command_CREATE" method is used.
   */
  public void test_duckTyping_useMostSpecific() throws Exception {
    IMocksControl mocksControl = EasyMock.createStrictControl();
    final XmlObjectInfo component = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo nextComponent = mocksControl.createMock(XmlObjectInfo.class);
    final MyFlowContainer_useMostSpecific container =
        mocksControl.createMock(MyFlowContainer_useMostSpecific.class);
    final FlowContainer flowContainer =
        new FlowContainerConfigurable(container,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                Associations.direct(),
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue()));
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
    IMocksControl mocksControl = EasyMock.createStrictControl();
    XmlObjectInfo container = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo component = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo reference = mocksControl.createMock(XmlObjectInfo.class);
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
    XmlObjectInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:FlowPanel>",
            "  <Button wbp:name='button'/>",
            "</t:FlowPanel>");
    XmlObjectInfo button = getObjectByName("button");
    // prepare FlowContainer
    FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
    // do CREATE
    XmlObjectInfo newButton = createButton();
    {
      assertTrue(flowContainer.isHorizontal());
      assertTrue(flowContainer.validateComponent(newButton));
      assertTrue(flowContainer.validateReference(button));
    }
    flowContainer.command_CREATE(newButton, button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:FlowPanel>",
        "  <Button/>",
        "  <Button wbp:name='button'/>",
        "</t:FlowPanel>");
  }

  /**
   * Ensure that we can: create, delete and create new component again.
   */
  public void test_CREATE_twoTimes() throws Exception {
    prepareFlowPanel();
    XmlObjectInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:FlowPanel/>");
    // prepare FlowContainer
    FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
    // create/delete
    {
      XmlObjectInfo newButton = createButton();
      flowContainer.command_CREATE(newButton, null);
      newButton.delete();
    }
    // create again
    {
      XmlObjectInfo newButton = createButton();
      flowContainer.command_CREATE(newButton, null);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:FlowPanel>",
        "  <Button/>",
        "</t:FlowPanel>");
  }

  /**
   * Test for {@link FlowContainerConfigurable#command_MOVE(Object, Object)}.
   */
  public void test_MOVE() throws Exception {
    prepareFlowPanel();
    XmlObjectInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:FlowPanel>",
            "  <Button wbp:name='button_1'/>",
            "  <Button wbp:name='button_2'/>",
            "</t:FlowPanel>");
    XmlObjectInfo button_1 = getObjectByName("button_1");
    XmlObjectInfo button_2 = getObjectByName("button_2");
    // prepare FlowContainer
    FlowContainer flowContainer =
        new FlowContainerConfigurable(panel,
            new FlowContainerConfiguration(Predicates.alwaysTrue(),
                Predicates.alwaysFalse(),
                Associations.direct(),
                ContainerObjectValidators.alwaysTrue(),
                ContainerObjectValidators.alwaysTrue()));
    // do MOVE
    flowContainer.command_MOVE(button_2, button_1);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:FlowPanel>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_1'/>",
        "</t:FlowPanel>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static void prepareFlowPanel() throws Exception {
    prepareFlowPanel_classes();
    setFileContentSrc(
        "test/FlowPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='flowContainer'>true</parameter>",
            "    <parameter name='flowContainer.horizontal'>true</parameter>",
            "    <parameter name='flowContainer.component'>org.eclipse.swt.widgets.Control</parameter>",
            "    <parameter name='flowContainer.reference'>org.eclipse.swt.widgets.Control</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  static void prepareFlowPanel_classes() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getSource(
            "package test;",
            "import org.eclipse.swt.graphics.Point;",
            "import org.eclipse.swt.widgets.*;",
            "public class MyLayout extends Layout {",
            "  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {",
            "    int count = composite.getChildren().length;",
            "    return new Point(15 + 95 * count + 5 * (count - 1) + 15, 10 + 50 + 10);",
            "  }",
            "  protected void layout(Composite composite, boolean flushCache) {",
            "    Control[] children = composite.getChildren();",
            "    for (int i = 0; i < children.length; i++) {",
            "      Control child = children[i];",
            "      child.setBounds(15 + 100 * i, 10, 95, 50);",
            "    }",
            "  }",
            "}"));
    setFileContentSrc(
        "test/FlowPanel.java",
        getSource(
            "package test;",
            "import org.eclipse.swt.widgets.*;",
            "public class FlowPanel extends Composite {",
            "  public FlowPanel(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new MyLayout());",
            "  }",
            "}"));
  }
}
