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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerConfigurable;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerConfiguration;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.tests.mock.EasyMockTemplate;
import org.eclipse.wb.tests.designer.tests.mock.MockRunnable;

import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Test for {@link SimpleContainer} and {@link SimpleContainerConfigurable} models.
 * 
 * @author scheglov_ke
 */
public class SimpleContainerModelTest extends AbstractCoreTest {
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
  // Association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No special container association, so "direct" is used.
   */
  public void test_getConfigurations_association_implicitDirect() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "org.eclipse.swt.widgets.Control", "direct");
  }

  /**
   * The "property" association
   */
  public void test_getConfigurations_association_property() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.x-association", "property myProperty"},
            {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "org.eclipse.swt.widgets.Control",
        "property myProperty");
  }

  /**
   * The "inter" association.
   */
  public void test_getConfigurations_association_inter() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.x-association", "inter myName attrA='a a' attrB='b'"},
            {"simpleContainer.component", "java.awt.Component"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(
        configurations.get(0),
        "java.awt.Component",
        "inter myName {attrA=a a, attrB=b}");
  }

  public void test_getConfigurations_association_bad() throws Exception {
    try {
      getConfigurations(true, new String[][]{
          {"simpleContainer", "true"},
          {"simpleContainer.x-association", "bad association text"},
          {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},});
      fail();
    } catch (Throwable e) {
      Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
      assertThat(rootCause).isExactlyInstanceOf(AssertionFailedException.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SimpleContainer_Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ask "forCanvas", return common configuration for both - canvas/tree.
   */
  public void test_getConfigurations_forCanvas_common() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "org.eclipse.swt.widgets.Control", "direct");
  }

  /**
   * Ask "forCanvas", return explicit "forCanvas".
   */
  public void test_getConfigurations_forCanvas_explicit() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.canvas", "true"},
            {"simpleContainer.canvas.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "org.eclipse.swt.widgets.Control", "direct");
  }

  /**
   * Ask "forCanvas", but only "forTree" exist.
   */
  public void test_getConfigurations_forCanvas_onlyForTree() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.tree", "true"},
            {"simpleContainer.tree.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).isEmpty();
  }

  /**
   * Ask "forTree", return common configuration for both - canvas/tree.
   */
  public void test_getConfigurations_forTree_common() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(false, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
  }

  /**
   * Ask "forTree", return explicit "forTree".
   */
  public void test_getConfigurations_forTree_explicit() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(false, new String[][]{
            {"simpleContainer.tree", "true"},
            {"simpleContainer.tree.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
  }

  /**
   * Several different configurations.
   */
  public void test_getConfigurations_3_count() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},
            {"simpleContainer.1", "true"},
            {"simpleContainer.1.component", "org.eclipse.swt.widgets.Button"},
            {"simpleContainer.5", "true"},
            {"simpleContainer.5.component", "org.eclipse.swt.widgets.Text"},});
    assertThat(configurations).hasSize(3);
    assertConfiguration(configurations.get(0), "org.eclipse.swt.widgets.Control", "direct");
    assertConfiguration(configurations.get(1), "org.eclipse.swt.widgets.Button", "direct");
    assertConfiguration(configurations.get(2), "org.eclipse.swt.widgets.Text", "direct");
  }

  /**
   * Ignore if <code>simpleContainer</code> value is not <code>true</code>.
   */
  public void test_getConfigurations_ignoreFalse() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "false"},
            {"simpleContainer.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(0);
  }

  /**
   * Use default component/reference validator.
   */
  public void test_getConfigurations_defaultValidators() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer", "true"},
            {"simpleContainer.defaultComponent", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "org.eclipse.swt.widgets.Control", "direct");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Container validation MVEL scripts
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConfigurations_validateContainer_isContainerType() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.canvas", "isContainerType('org.eclipse.swt.widgets.Control')"},
            {"simpleContainer.canvas.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).hasSize(1);
    assertConfiguration(configurations.get(0), "org.eclipse.swt.widgets.Control", "direct");
  }

  public void test_getConfigurations_validateContainer_scriptToFalse() throws Exception {
    List<SimpleContainerConfiguration> configurations =
        getConfigurations(true, new String[][]{
            {"simpleContainer.canvas", "1 == 2"},
            {"simpleContainer.canvas.component", "org.eclipse.swt.widgets.Control"},});
    assertThat(configurations).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configurations access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertConfiguration(SimpleContainerConfiguration configuration,
      String expectedComponentValidator,
      String expectedAssociation) {
    if (expectedComponentValidator != null) {
      assertEquals(
          expectedComponentValidator,
          getValidatorString(configuration.getComponentValidator()));
      assertEquals(expectedAssociation, getAssociationString(configuration));
    }
  }

  private static String getValidatorString(Object validator) {
    return validator.toString();
  }

  private static String getAssociationString(SimpleContainerConfiguration configuration) {
    return configuration.getAssociation().toString();
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
    List<SimpleContainerConfiguration> configurations =
        new SimpleContainerFactory(panel, forCanvas).getConfigurations();
    for (Iterator<SimpleContainerConfiguration> I = configurations.iterator(); I.hasNext();) {
      SimpleContainerConfiguration configuration = I.next();
      String validatorString = getValidatorString(configuration.getComponentValidator());
      if (validatorString.endsWith(".DragSource")
          || validatorString.endsWith(".DropTarget")
          || validatorString.contains("isPopup()")) {
        I.remove();
      }
    }
    return configurations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Duck typing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class MySimpleContainer extends XmlObjectInfo {
    public MySimpleContainer(EditorContext context,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(context, description, creationSupport);
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
   * CREATE/MOVE methods in container {@link XmlObjectInfo} and use them instead of generic
   * implementation.
   */
  public void test_duckTyping() throws Exception {
    final IMocksControl mocksControl = EasyMock.createStrictControl();
    final XmlObjectInfo component = mocksControl.createMock(XmlObjectInfo.class);
    final MySimpleContainer container = mocksControl.createMock(MySimpleContainer.class);
    final SimpleContainer simpleContainer;
    {
      ContainerObjectValidator validator = ContainerObjectValidators.alwaysTrue();
      Association association = Associations.direct();
      SimpleContainerConfiguration configuration =
          new SimpleContainerConfiguration(validator, association);
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
    IMocksControl mocksControl = EasyMock.createStrictControl();
    XmlObjectInfo container = mocksControl.createMock(XmlObjectInfo.class);
    final XmlObjectInfo component = mocksControl.createMock(XmlObjectInfo.class);
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
    XmlObjectInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:SimplePanel/>");
    // prepare SimpleContainer
    SimpleContainer simpleContainer = new SimpleContainerFactory(panel, true).get().get(0);
    assertTrue(simpleContainer.isEmpty());
    // do CREATE
    XmlObjectInfo newButton = createButton();
    {
      assertTrue(simpleContainer.validateComponent(newButton));
    }
    simpleContainer.command_CREATE(newButton);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:SimplePanel>",
        "  <Button/>",
        "</t:SimplePanel>");
    assertFalse(simpleContainer.isEmpty());
  }

  /**
   * Ensure that we can: create, delete and create new component again.
   */
  public void test_CREATE_twoTimes() throws Exception {
    prepareSimplePanel();
    XmlObjectInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:SimplePanel/>");
    // prepare SimpleContainer
    SimpleContainer simpleContainer = new SimpleContainerFactory(panel, true).get().get(0);
    assertTrue(simpleContainer.isEmpty());
    // create/delete
    {
      XmlObjectInfo newButton = createButton();
      simpleContainer.command_CREATE(newButton);
      newButton.delete();
    }
    // create again
    {
      XmlObjectInfo newButton = createButton();
      simpleContainer.command_CREATE(newButton);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:SimplePanel>",
        "  <Button/>",
        "</t:SimplePanel>");
  }

  /**
   * Test for {@link SimpleContainerConfigurable#command_absolute_MOVE(Object, Object)}.
   */
  public void test_MOVE() throws Exception {
    prepareSimplePanel();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='simplePanel'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    XmlObjectInfo simplePanel = getObjectByName("simplePanel");
    XmlObjectInfo button = getObjectByName("button");
    // prepare SimpleContainer
    SimpleContainer simpleContainer = new SimpleContainerFactory(simplePanel, true).get().get(0);
    // do MOVE
    simpleContainer.command_ADD(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='simplePanel'>",
        "    <Button wbp:name='button'/>",
        "  </t:SimplePanel>",
        "</Shell>");
  }

  /**
   * {@link SimpleContainer} should automatically copy its child into clipboard.
   */
  public void test_clipboard() throws Exception {
    prepareSimplePanel();
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <t:SimplePanel wbp:name='panel'>",
            "    <Button text='my Button'/>",
            "  </t:SimplePanel>",
            "</Shell>");
    refresh();
    // prepare memento
    XmlObjectMemento memento;
    {
      XmlObjectInfo panel = getObjectByName("panel");
      memento = XmlObjectMemento.createMemento(panel);
    }
    // do paste
    ControlInfo newPanel = (ControlInfo) memento.create(shell);
    shell.getLayout().command_CREATE(newPanel, null);
    memento.apply();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button text='my Button'/>",
        "  </t:SimplePanel>",
        "  <t:SimplePanel>",
        "    <Button text='my Button'/>",
        "  </t:SimplePanel>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static void prepareSimplePanel() throws Exception {
    prepareSimplePanel_classes();
    setFileContentSrc(
        "test/SimplePanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='simpleContainer'>true</parameter>",
            "    <parameter name='simpleContainer.component'>org.eclipse.swt.widgets.Control</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  static void prepareSimplePanel_classes() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getSource(
            "package test;",
            "import org.eclipse.swt.graphics.Point;",
            "import org.eclipse.swt.widgets.*;",
            "public class MyLayout extends Layout {",
            "  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {",
            "    return new Point(200, 100);",
            "  }",
            "  protected void layout(Composite parent, boolean flushCache) {",
            "    Control[] children = parent.getChildren();",
            "    for (int i = 0; i < children.length; i++) {",
            "      Control child = children[i];",
            "      child.setBounds(10, 10, parent.getSize().x - 20, parent.getSize().y - 20);",
            "    }",
            "  }",
            "}"));
    setFileContentSrc(
        "test/SimplePanel.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.swt.widgets.*;",
            "public class SimplePanel extends Composite {",
            "  public SimplePanel(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new MyLayout());",
            "  }",
            "}"));
  }
}
