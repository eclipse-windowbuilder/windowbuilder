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
package org.eclipse.wb.tests.designer.core.model.util;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Tests for {@link MorphingSupport}.
 * 
 * @author scheglov_ke
 */
public class MorphingSupportTest extends SwingModelTest {
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
  // Validate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_validate_noAssociationMethod() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void addChild(Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addChild'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyPanel myPanel = new MyPanel();",
            "    add(myPanel);",
            "    myPanel.addChild(new JButton());",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    // validate
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    MorphingTargetDescription morphingTarget = new MorphingTargetDescription(JPanel.class, null);
    String message = validate(myPanel, morphingTarget);
    assertNotNull(message);
  }

  public void test_validate_hasMethod_notAssociation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void addChild(Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='add'>",
            "      <parameter type='java.awt.Component' child='false'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPanel myPanel = new JPanel();",
            "    add(myPanel);",
            "    myPanel.add(new JButton());",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    // validate
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    Class<?> targetClass = m_lastLoader.loadClass("test.MyPanel");
    MorphingTargetDescription morphingTarget = new MorphingTargetDescription(targetClass, null);
    String message = validate(myPanel, morphingTarget);
    assertNotNull(message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link MethodInvocation} that can not exist for new component, should be removed.
   */
  public void test_morph_removeMethodInvocations() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JLabel());",
            "    //",
            "    JButton button = new JButton();",
            "    button.setText('text');",
            "    button.setDefaultCapable(true);",
            "    add(button);",
            "    //",
            "    add(new JLabel());",
            "  }",
            "}");
    assertEquals(3, panel.getChildrenComponents().size());
    // do morphing
    {
      MorphingTargetDescription morphingTarget =
          new MorphingTargetDescription(JTextField.class, null);
      ComponentInfo button = panel.getChildrenComponents().get(1);
      morph(button, morphingTarget);
    }
    // check result
    assertEquals(3, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JLabel());",
        "    //",
        "    JTextField button = new JTextField();",
        "    button.setText('text');",
        "    add(button);",
        "    //",
        "    add(new JLabel());",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(1);
      assertEquals(
          "{new: javax.swing.JTextField} {local-unique: button} {/button.setText(\"text\")/ /add(button)/ /new JTextField()/}",
          result.toString());
      assertSame(JTextField.class, result.getDescription().getComponentClass());
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new JTextField()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
        assertSame(result, variable.getJavaInfo());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) result.getAssociation();
        assertEquals("add(button)", association.getSource());
      }
    }
    // refresh should be successful
    panel.refresh();
  }

  /**
   * {@link Assignment} that can not exist for new component, should be removed.
   */
  public void test_morph_removeFieldAssignments() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.m_value = 1;",
            "    add(button);",
            "  }",
            "}"};
    // parse
    ContainerInfo panel = parseContainer(lines);
    assertEquals(1, panel.getChildrenComponents().size());
    // do morphing
    {
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(JButton.class, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      morph(button, morphingTarget);
    }
    // check result
    assertEquals(1, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(0);
      assertEquals(
          "{new: javax.swing.JButton} {local-unique: button} {/add(button)/ /new JButton()/}",
          result.toString());
      assertSame(JButton.class, result.getDescription().getComponentClass());
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new JButton()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) result.getAssociation();
        assertEquals("add(button)", association.getSource());
      }
    }
  }

  /**
   * If source is created using {@link ClassInstanceCreation} and target has constructor with same
   * parameter types, then use same arguments.
   */
  public void test_morph_useConstructorWithSameArguments() throws Exception {
    String[] lines1 =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('My text');",
            "    add(button);",
            "  }",
            "}"};
    ContainerInfo panel = parseContainer(lines1);
    // do morphing
    {
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(JLabel.class, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      morph(button, morphingTarget);
    }
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JLabel button = new JLabel('My text');",
            "    add(button);",
            "  }",
            "}"};
    // check result
    assertEditor(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JLabel} {local-unique: button} {/add(button)/ /new JLabel('My text')/}");
  }

  /**
   * Test for morphing when {@link ConstructorParentAssociation} is used.
   */
  public void test_morph_ConstructorParentAssociation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton2.java",
        getTestSource(
            "public class MyButton2 extends JButton {",
            "  public MyButton2(Container container, boolean check) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CREATION -->",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
            "  </creation>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    setFileContentSrc(
        "test/MyButton2.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CREATION -->",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton2(%parent%, false)]]></source>",
            "  </creation>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "      <parameter type='boolean'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton(this);",
            "  }",
            "}");
    // do morphing
    {
      Class<?> targetClass = m_lastLoader.loadClass("test.MyButton2");
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(targetClass, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      morph(button, morphingTarget);
    }
    // check result
    assertEquals(1, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton2 button = new MyButton2(this, false);",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(0);
      assertEquals(
          "{new: test.MyButton2} {local-unique: button} {/new MyButton2(this, false)/}",
          result.toString());
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new MyButton2(this, false)", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        ConstructorParentAssociation association =
            (ConstructorParentAssociation) result.getAssociation();
        assertEquals("new MyButton2(this, false)", association.getSource());
      }
    }
  }

  /**
   * Test for morphing when {@link StaticFactoryCreationSupport} is used.
   */
  public void test_morph_StaticFactoryCreationSupport() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = MyFactory.createButton();",
            "    add(button);",
            "  }",
            "}");
    // do morphing
    {
      Class<?> targetClass = JTextField.class;
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(targetClass, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      assertThat(button.getCreationSupport()).isInstanceOf(StaticFactoryCreationSupport.class);
      morph(button, morphingTarget);
    }
    // check result
    assertEquals(1, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JTextField button = new JTextField();",
        "    add(button);",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(0);
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new JTextField()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) result.getAssociation();
        assertEquals("add(button)", association.getSource());
      }
    }
  }

  /**
   * Test for morphing when {@link StaticFactoryCreationSupport} and
   * {@link FactoryParentAssociation} are used.
   */
  public void test_morph_StaticFactoryCreationSupport_FactoryParentAssociation() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public static JButton createButton(Container parent) {",
            "    JButton button = new JButton();",
            "    parent.add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton2.java",
        getTestSource(
            "public class MyButton2 extends JButton {",
            "  public MyButton2(Container container) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton2.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CREATION -->",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton2(%parent%)]]></source>",
            "  </creation>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = MyFactory.createButton(this);",
            "  }",
            "}");
    // do morphing
    {
      Class<?> targetClass = m_lastLoader.loadClass("test.MyButton2");
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(targetClass, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      assertThat(button.getCreationSupport()).isInstanceOf(StaticFactoryCreationSupport.class);
      morph(button, morphingTarget);
    }
    // check result
    assertEquals(1, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton2 button = new MyButton2(this);",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(0);
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new MyButton2(this)", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        ConstructorParentAssociation association =
            (ConstructorParentAssociation) result.getAssociation();
        assertEquals("new MyButton2(this)", association.getSource());
      }
    }
  }

  /**
   * Test for morphing when {@link InstanceFactoryCreationSupport} is used.
   */
  public void test_morph_InstanceFactoryCreationSupport() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final MyFactory factory = new MyFactory();",
            "  public Test() {",
            "    JButton button = factory.createButton();",
            "    add(button);",
            "  }",
            "}");
    // do morphing
    {
      Class<?> targetClass = JTextField.class;
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(targetClass, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      assertThat(button.getCreationSupport()).isInstanceOf(InstanceFactoryCreationSupport.class);
      morph(button, morphingTarget);
    }
    // check result
    assertEquals(1, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  private final MyFactory factory = new MyFactory();",
        "  public Test() {",
        "    JTextField button = new JTextField();",
        "    add(button);",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(0);
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new JTextField()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) result.getAssociation();
        assertEquals("add(button)", association.getSource());
      }
    }
  }

  /**
   * During morphing we move children from source to parent.
   */
  public void test_morph_keepChildren() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyPanel myPanel = new MyPanel();",
            "    add(myPanel);",
            "    myPanel.add(new JButton());",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    // do morphing
    {
      ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(JPanel.class, null);
      morph(myPanel, morphingTarget);
    }
    // check result
    assertEquals(1, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPanel myPanel = new JPanel();",
        "    add(myPanel);",
        "    myPanel.add(new JButton());",
        "  }",
        "}");
    {
      ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
      assertEquals(
          "{new: javax.swing.JPanel} {local-unique: myPanel} {/add(myPanel)/ /myPanel.add(new JButton())/ /new JPanel()/}",
          myPanel.toString());
      assertSame(JPanel.class, myPanel.getDescription().getComponentClass());
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) myPanel.getCreationSupport();
        assertEquals("new JPanel()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) myPanel.getVariableSupport();
        assertEquals("myPanel", variable.getName());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) myPanel.getAssociation();
        assertEquals("add(myPanel)", association.getSource());
      }
      // children
      {
        List<ComponentInfo> children = myPanel.getChildrenComponents();
        assertEquals(1, children.size());
        assertSame(JButton.class, children.get(0).getDescription().getComponentClass());
      }
    }
    // refresh() should work
    panel.refresh();
  }

  /**
   * Morphing and {@link LazyVariableSupport} - it should replace return type for accessor.
   */
  public void test_morph_lazyVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  public JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "    }",
            "    return button;",
            "  }",
            "}");
    assertNoErrors(panel);
    // do morphing
    {
      MorphingTargetDescription morphingTarget =
          new MorphingTargetDescription(JTextField.class, null);
      ComponentInfo button = panel.getChildrenComponents().get(0);
      morph(button, morphingTarget);
    }
    // check result
    assertEditor(
        "public class Test extends JPanel {",
        "  private JTextField button;",
        "  public Test() {",
        "    add(getButton());",
        "  }",
        "  public JTextField getButton() {",
        "    if (button == null) {",
        "      button = new JTextField();",
        "    }",
        "    return button;",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(0);
      assertEquals(
          "{new: javax.swing.JTextField} {lazy: button getButton()} {/button/ /add(getButton())/ /new JTextField()/}",
          result.toString());
      assertSame(JTextField.class, result.getDescription().getComponentClass());
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new JTextField()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LazyVariableSupport variable = (LazyVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) result.getAssociation();
        assertEquals("add(getButton())", association.getSource());
      }
    }
    // refresh should be successful
    panel.refresh();
  }

  /**
   * Something was broken during morphing and prevents any operation with morphing result
   * {@link JavaInfo}. This was caused by using old (source) {@link JavaInfo} in reused
   * {@link VariableSupport}.
   */
  public void test_morph_andSetProperty() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JLabel component = new JLabel();",
            "    add(component);",
            "  }",
            "}"};
    ContainerInfo panel = parseContainer(lines);
    // do morphing
    {
      MorphingTargetDescription morphingTarget = new MorphingTargetDescription(JButton.class, null);
      ComponentInfo component = panel.getChildrenComponents().get(0);
      morph(component, morphingTarget);
    }
    // check result
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton component = new JButton();",
        "    add(component);",
        "  }",
        "}");
    // set property
    ComponentInfo component = panel.getChildrenComponents().get(0);
    component.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton component = new JButton();",
        "    component.setEnabled(false);",
        "    add(component);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs morphing of {@link JavaInfo} into given target.
   */
  public static String validate(JavaInfo javaInfo, MorphingTargetDescription target)
      throws Exception {
    return MorphingSupport.validate("java.awt.Component", javaInfo, target);
  }

  /**
   * Performs morphing of {@link JavaInfo} into given target.
   */
  public static void morph(JavaInfo javaInfo, MorphingTargetDescription target) throws Exception {
    assertNull(validate(javaInfo, target));
    MorphingSupport.morph("java.awt.Component", javaInfo, target);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that "Morph" sub-menu is contributed during broadcast.
   */
  public void test_actions() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // no "Morph" expected for "panel" - wrong variable
    assertNull(getMorphManager(panel));
    // prepare "Morph" sub-menu
    {
      IMenuManager morphManager = getMorphManager(button);
      // targets from component description
      assertNotNull(findChildAction(morphManager, "JCheckBox"));
      assertNotNull(findChildAction(morphManager, "JRadioButton"));
      assertNotNull(findChildAction(morphManager, "JLabel"));
      // special targets
      assertNotNull(findChildAction(morphManager, "&Subclass..."));
      assertNotNull(findChildAction(morphManager, "&Other..."));
      // no such target
      assertNull(findChildAction(morphManager, "JMenu"));
    }
  }

  /**
   * Thoroughly test one action from "Morph" sub-menu.
   */
  public void test_actions_run() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}"};
    ContainerInfo panel = parseContainer(lines);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    IMenuManager morphManager = getMorphManager(button);
    // check one target action thoroughly
    IAction action = findChildAction(morphManager, "JCheckBox");
    assertNotNull(action);
    // "object" methods
    {
      assertEquals(0, action.hashCode());
      assertEquals(action, EasyMock.createStrictMock(action.getClass()));
      assertFalse(action.equals(this));
    }
    // do run, morphing should be performed
    action.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JCheckBox button = new JCheckBox();",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * @return the "Morph" {@link IMenuManager} contributed for given component.
   */
  private static IMenuManager getMorphManager(JavaInfo component) throws Exception {
    MenuManager menuManager = getDesignerMenuManager();
    component.getBroadcastObject().addContextMenu(
        ImmutableList.of(component),
        component,
        menuManager);
    return findChildMenuManager(menuManager, "Morph");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replace children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test invocation {@link JavaEventListener#replaceChildBefore(JavaInfo, JavaInfo, JavaInfo)} and
   * {@link JavaEventListener#replaceChildAfter(JavaInfo, JavaInfo, JavaInfo)} during morphing
   * operations.
   */
  public void test_morph_invoke_replaceChildren() throws Exception {
    // prepare "before" state
    final boolean[] invokeBeforeState = {false};
    final boolean[] parentBeforeState = {false, false};
    final boolean[] childrenBeforeState = {false, false};
    // prepare "after" state
    final boolean[] invokeAfterState = {false};
    final boolean[] parentAfterState = {false, false};
    final boolean[] childrenAfterState = {false, false};
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JLabel());",
            "    //",
            "    JButton button = new JButton();",
            "    button.setText('text');",
            "    button.setDefaultCapable(true);",
            "    add(button);",
            "    //",
            "    add(new JLabel());",
            "  }",
            "}");
    assertEquals(3, panel.getChildrenComponents().size());
    // add listener
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void replaceChildBefore(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        // invoke state
        invokeBeforeState[0] = true;
        // parent state
        parentBeforeState[0] = oldChild.getParent() == parent;
        parentBeforeState[1] = newChild.getParent() == parent;
        // children state
        childrenBeforeState[0] = parent.getChildren().contains(oldChild);
        childrenBeforeState[1] = parent.getChildren().contains(newChild);
      }

      @Override
      public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        // invoke state
        invokeAfterState[0] = true;
        // parent state
        parentAfterState[0] = oldChild.getParent() == parent;
        parentAfterState[1] = newChild.getParent() == parent;
        // children state
        childrenAfterState[0] = parent.getChildren().contains(oldChild);
        childrenAfterState[1] = parent.getChildren().contains(newChild);
      }
    });
    // do morphing
    {
      MorphingTargetDescription morphingTarget =
          new MorphingTargetDescription(JTextField.class, null);
      ComponentInfo button = panel.getChildrenComponents().get(1);
      // check initial state
      assertFalse(invokeBeforeState[0]);
      assertFalse(invokeAfterState[0]);
      // do morph
      morph(button, morphingTarget);
      // check result "before" state
      assertTrue(invokeBeforeState[0]);
      assertTrue(parentBeforeState[0]); // old child is not removed
      assertTrue(childrenBeforeState[0]);
      assertFalse(parentBeforeState[1]); // new child has not yet added 
      assertFalse(childrenBeforeState[1]);
      // check result "after" state
      assertTrue(invokeAfterState[0]);
      assertTrue(parentAfterState[0]); // old child is removed
      assertFalse(childrenAfterState[0]);
      assertTrue(parentAfterState[1]); // new child added
      assertTrue(childrenAfterState[1]);
    }
    // check result
    assertEquals(3, panel.getChildrenComponents().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JLabel());",
        "    //",
        "    JTextField button = new JTextField();",
        "    button.setText('text');",
        "    add(button);",
        "    //",
        "    add(new JLabel());",
        "  }",
        "}");
    {
      ComponentInfo result = panel.getChildrenComponents().get(1);
      assertEquals(
          "{new: javax.swing.JTextField} {local-unique: button} {/button.setText(\"text\")/ /add(button)/ /new JTextField()/}",
          result.toString());
      assertSame(JTextField.class, result.getDescription().getComponentClass());
      // creation
      {
        ConstructorCreationSupport creation =
            (ConstructorCreationSupport) result.getCreationSupport();
        assertEquals("new JTextField()", m_lastEditor.getSource(creation.getCreation()));
      }
      // variable
      {
        LocalUniqueVariableSupport variable =
            (LocalUniqueVariableSupport) result.getVariableSupport();
        assertEquals("button", variable.getName());
      }
      // association
      {
        InvocationChildAssociation association =
            (InvocationChildAssociation) result.getAssociation();
        assertEquals("add(button)", association.getSource());
      }
    }
    // refresh should be successful
    panel.refresh();
  }
}