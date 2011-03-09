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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.Statement;

/**
 * Tests for {@link InvocationChildAssociation}.
 * 
 * @author scheglov_ke
 */
public class InvocationChildAssociationTest extends SwingModelTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_invalid_noParentAccess() throws Exception {
    try {
      new InvocationChildAssociation("invalidSource");
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  public void test_parse() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("add(button)", association.getSource());
    assertEquals("add(button)", m_lastEditor.getSource(association.getInvocation()));
    assertEquals("add(button);", m_lastEditor.getSource(association.getStatement()));
    {
      MethodDescription methodDescription = association.getDescription();
      assertNotNull(methodDescription);
      assertEquals("add(java.awt.Component)", methodDescription.getSignature());
    }
  }

  /**
   * Test for support of "associateOnlyFirstTime" parameter tag.
   */
  public void test_parse_associateOnlyFirstTime() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setButton(JButton button) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSource(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CONSTRUCTORS -->",
            "  <methods>",
            "    <method name='setButton'>",
            "      <parameter type='javax.swing.JButton' child='true'>",
            "        <tag name='associateOnlyFirstTime' value='true'/>",
            "      </parameter>",
            "    </method>",
            "  </methods>",
            "  <!-- PARAMETERS -->",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyPanel panel_1 = new MyPanel();",
        "    MyPanel panel_2 = new MyPanel();",
        "    add(panel_1);",
        "    add(panel_2);",
        "    //",
        "    JButton button = new JButton();",
        "    panel_1.setButton(button);",
        "    panel_2.setButton(button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(panel_1)/ /add(panel_2)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {local-unique: panel_1} {/new MyPanel()/ /add(panel_1)/ /panel_1.setButton(button)/}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /panel_1.setButton(button)/ /panel_2.setButton(button)/}",
        "  {new: test.MyPanel} {local-unique: panel_2} {/new MyPanel()/ /add(panel_2)/ /panel_2.setButton(button)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Method has parameter with unknown editor.
   */
  public void test_addProperties_noProperty() throws Exception {
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  public void addChild(Component component, Object constraints) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyContainer.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addChild'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "      <parameter type='java.lang.Object'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyContainer {",
            "  public Test() {",
            "    addChild(new JButton(), new Object());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // we don't have editor for generic java.lang.Object, so no "Association" property
    Property associationProperty = button.getPropertyByTitle("Association");
    assertNull(associationProperty);
  }

  /**
   * Parameter with name in description.
   */
  public void test_addProperties_hasProperty_1() throws Exception {
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  public void addChild(String sourceParameterName, Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyContainer.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addChild'>",
            "      <parameter type='java.lang.String' name='text' defaultSource='\"default text\"'/>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyContainer {",
            "  public Test() {",
            "    addChild('my text', new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare complex "Association" property
    Property associationProperty;
    {
      associationProperty = button.getPropertyByTitle("Association");
      assertNotNull(associationProperty);
      assertTrue(associationProperty.isModified());
      assertTrue(associationProperty.getCategory().isSystem());
      assertInstanceOf(IComplexPropertyEditor.class, associationProperty.getEditor());
      // ensure that second time same property is returned
      assertSame(associationProperty, button.getPropertyByTitle("Association"));
    }
    // prepare "Association/text" property
    Property associationSubProperty;
    {
      Property[] subProperties = getSubProperties(associationProperty);
      associationSubProperty = getPropertyByTitle(subProperties, "text");
      assertNotNull(associationSubProperty);
      assertInstanceOf(StringPropertyEditor.class, associationSubProperty.getEditor());
    }
    // existing value
    assertTrue(associationSubProperty.isModified());
    assertEquals("my text", associationSubProperty.getValue());
    // new value
    associationSubProperty.setValue("new text");
    assertEditor(
        "public class Test extends MyContainer {",
        "  public Test() {",
        "    addChild('new text', new JButton());",
        "  }",
        "}");
    // remove value
    associationSubProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends MyContainer {",
        "  public Test() {",
        "    addChild('default text', new JButton());",
        "  }",
        "}");
  }

  /**
   * Parameter without name, name of parameter from source should be used.
   */
  public void test_addProperties_hasProperty_2() throws Exception {
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  public void addChild(String mySecretName, Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyContainer.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addChild'>",
            "      <parameter type='java.lang.String'/>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyContainer {",
            "  public Test() {",
            "    addChild('my text', new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    Property associationSubProperty = PropertyUtils.getByPath(button, "Association/mySecretName");
    assertNotNull(associationSubProperty);
    assertInstanceOf(StringPropertyEditor.class, associationSubProperty.getEditor());
  }

  /**
   * Parameter with special editor, configured in description.
   */
  public void test_addProperties_hasProperty_3() throws Exception {
    setFileContentSrc(
        "test/MyEnum.java",
        getSourceDQ(
            "package test;",
            "public class MyEnum {",
            "  public static final int A = 1;",
            "  public static final int B = 2;",
            "}"));
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  public void addChild(int position, Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyContainer.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addChild'>",
            "      <parameter type='int'>",
            "        <editor id='staticField'>",
            "          <parameter name='class'>test.MyEnum</parameter>",
            "          <parameter name='fields'>A B</parameter>",
            "        </editor>",
            "      </parameter>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyContainer {",
            "  public Test() {",
            "    addChild(MyEnum.A, new JButton());",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    Property associationSubProperty = PropertyUtils.getByPath(button, "Association/position");
    assertNotNull(associationSubProperty);
    assertInstanceOf(StaticFieldPropertyEditor.class, associationSubProperty.getEditor());
    assertEquals(1, associationSubProperty.getValue());
    assertEquals("A", getPropertyText(associationSubProperty));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // remove()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_remove() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do delete
    assertTrue(button.canDelete());
    button.delete();
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * When we delete child and parent is created directly in association {@link Statement}, we should
   * materialize parent to prevent its removing with association.
   */
  public void test_remove_whenNoParentVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JButton button = new JButton();",
            "    new JPanel().add(button);",
            "  }",
            "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {empty} {/new JPanel().add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /new JPanel().add(button)/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do delete
    button.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JPanel panel = new JPanel();",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // morph()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_morph() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    // prepare "old" button
    ComponentInfo oldButton = panel.getChildrenComponents().get(0);
    InvocationChildAssociation oldAssociation =
        (InvocationChildAssociation) oldButton.getAssociation();
    // new association is based on same invocation
    {
      InvocationChildAssociation newAssociation =
          (InvocationChildAssociation) oldAssociation.getCopy();
      assertSame(oldAssociation.getInvocation(), newAssociation.getInvocation());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // add()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_add() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // add new JButton
    ComponentInfo button =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("javax.swing.JButton"),
            new ConstructorCreationSupport());
    flowLayout.add(button, null);
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("add(button)", association.getSource());
  }

  public void test_add_emptyVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add button
    ComponentInfo button = createJButton();
    JavaInfoUtils.add(
        button,
        new EmptyInvocationVariableSupport(button, "%parent%.add(%child%)", 0),
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.invocationChildNull(),
        panel,
        null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JButton());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // move()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_moveInner() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "    {",
            "      JPanel container = new JPanel();",
            "      add(container);",
            "    }",
            "  }",
            "}");
    FlowLayoutInfo panelLayout = (FlowLayoutInfo) panel.getLayout();
    // move "button"
    ComponentInfo button = panel.getChildrenComponents().get(0);
    panelLayout.move(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel container = new JPanel();",
        "      add(container);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("add(button)", association.getSource());
  }

  public void test_moveInner_lazy() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "    {",
            "      JPanel container = new JPanel();",
            "      add(container);",
            "    }",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "      button.setText('Lazy JButton');",
            "    }",
            "    return button;",
            "  }",
            "}");
    FlowLayoutInfo panelLayout = (FlowLayoutInfo) panel.getLayout();
    // move "button"
    ComponentInfo button = panel.getChildrenComponents().get(0);
    panelLayout.move(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel container = new JPanel();",
        "      add(container);",
        "    }",
        "    add(getButton());",
        "  }",
        "  private JButton button;",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "      button.setText('Lazy JButton');",
        "    }",
        "    return button;",
        "  }",
        "}");
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("add(getButton())", association.getSource());
  }

  public void test_moveInner_lazy_withConstraints() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    {",
            "      JPanel container = new JPanel();",
            "      add(container);",
            "    }",
            "    JLabel constraints = new JLabel();",
            "    add(getButton(), constraints);",
            "  }",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "      button.setText('Lazy JButton');",
            "    }",
            "    return button;",
            "  }",
            "}");
    panel.refresh();
    FlowLayoutInfo panelLayout = (FlowLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    ComponentInfo reference = getJavaInfoByName("container");
    // move "button"
    panelLayout.move(button, reference);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    JLabel constraints = new JLabel();",
        "    add(getButton(), constraints);",
        "    {",
        "      JPanel container = new JPanel();",
        "      add(container);",
        "    }",
        "  }",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "      button.setText('Lazy JButton');",
        "    }",
        "    return button;",
        "  }",
        "}");
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("add(getButton(), constraints)", association.getSource());
  }

  public void test_moveReparent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "    {",
            "      JPanel container = new JPanel();",
            "      add(container);",
            "    }",
            "  }",
            "}");
    // prepare  "container"
    ContainerInfo container = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo containerLayout = (FlowLayoutInfo) container.getLayout();
    // move "button"
    ComponentInfo button = panel.getChildrenComponents().get(0);
    containerLayout.move(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel container = new JPanel();",
        "      add(container);",
        "      {",
        "        JButton button = new JButton();",
        "        container.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
    // check association
    InvocationChildAssociation association = (InvocationChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("container.add(button)", association.getSource());
  }
}
