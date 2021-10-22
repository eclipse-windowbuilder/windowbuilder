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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.factory.FactoryApplyAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link FactoryApplyAction}.
 *
 * @author scheglov_ke
 */
public class FactoryApplyActionTest extends SwingModelTest {
  private static final String ICON_PATH = "/javax/swing/plaf/basic/icons/JavaCup16.png";

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
  /**
   * Simplest case - no properties, invocations, etc. Just {@link ConstructorCreationSupport}
   * replaced with {@link StaticFactoryCreationSupport}.
   */
  public void test_noParameters() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
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
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // apply factory
    doApply(button, "createButton()");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton();",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Factory with single parameter, but no bound property for parameter.
   */
  public void test_noBoundProperty() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source myText 'text'",
            "  */",
            "  public static JButton createButton(String myText) {",
            "    return new JButton(myText);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // apply factory
    doApply(button, "createButton(java.lang.String)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton('text');",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Factory with single parameter, but bounds property is not set, so default source used.
   */
  public void test_singleBoundProperty_noValue() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'text'",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // apply factory
    doApply(button, "createButton(java.lang.String)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton('text');",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Factory with single parameter, bound to the property <code>text</code>.
   */
  public void test_singleBoundProperty() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'text'",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    button.setText('my text');",
        "    add(button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /button.setText('my text')/ /add(button)/}");
    ComponentInfo button = getJavaInfoByName("button");
    // apply factory
    doApply(button, "createButton(java.lang.String)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton('my text');",
        "    add(button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {static factory: test.StaticFactory createButton(java.lang.String)} {local-unique: button} {/add(button)/ /StaticFactory.createButton('my text')/}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Factory with two parameters, bound to the properties <code>text</code> and <code>icon</code>.
   */
  public void test_twoBoundProperty() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'text'",
            "  * @wbp.factory.parameter.source icon null",
            "  */",
            "  public static JButton createButton(String text, Icon icon) {",
            "    return new JButton(text, icon);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setText('my text');",
            "    button.setIcon(new ImageIcon(getClass().getResource('" + ICON_PATH + "')));",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // apply factory
    doApply(button, "createButton(java.lang.String,javax.swing.Icon)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton('my text', null);",
        "    button.setIcon(new ImageIcon(getClass().getResource('" + ICON_PATH + "')));",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Factory with single parameter, bound to the argument of old constructor.
   */
  public void test_boundToOldConstuctorArgument() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'text'",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('my text');",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // apply factory
    doApply(button, "createButton(java.lang.String)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton('my text');",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Factory with <code>parent</code> parameter.
   */
  public void test_withParent() throws Exception {
    // component with "parent" in constructor
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container, String text) {",
            "    setText(text);",
            "    container.add(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "      <parameter type='java.lang.String' property='setText(java.lang.String)'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'text'",
            "  */",
            "  public static MyButton createButton(Container parent, String text) {",
            "    return new MyButton(parent, text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton(this, 'my text');",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    {
      ConstructorParentAssociation association =
          (ConstructorParentAssociation) button.getAssociation();
      assertEquals("new MyButton(this, \"my text\")", association.getSource());
    }
    // apply factory
    doApply(button, "createButton(java.awt.Container,java.lang.String)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = StaticFactory.createButton(this, 'my text');",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    {
      FactoryParentAssociation association = (FactoryParentAssociation) button.getAssociation();
      assertEquals("StaticFactory.createButton(this, \"my text\")", association.getSource());
    }
  }

  /**
   * Factory parameter is bound to the property, but variable is used.
   */
  public void test_boundPropertyVariable() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'text'",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    String theText = 'my text';",
            "    button.setText(theText);",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // apply factory
    doApply(button, "createButton(java.lang.String)");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = StaticFactory.createButton('text');",
        "    String theText = 'my text';",
        "    button.setText(theText);",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(StaticFactoryCreationSupport.class, button.getCreationSupport());
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Applies factory method of <code>test.StaticFactory</code> to the given component.
   */
  private static void doApply(JavaInfo component, String methodSignature) throws Exception {
    AstEditor editor = component.getEditor();
    // prepare factory
    FactoryMethodDescription factoryDescription;
    {
      Class<?> factoryClass =
          EditorState.get(editor).getEditorLoader().loadClass("test.StaticFactory");
      factoryDescription =
          FactoryDescriptionHelper.getDescription(editor, factoryClass, methodSignature, true);
      assertNotNull("No factory method with signature: " + methodSignature, factoryDescription);
    }
    // apply factory
    FactoryApplyAction action = new FactoryApplyAction(component, factoryDescription);
    action.run();
  }
}
