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

import org.eclipse.wb.core.model.association.RootAssociation;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SuperConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JButton;

/**
 * Test for {@link ThisCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class ThisCreationSupportTest extends SwingModelTest {
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
   * Test for {@link ThisCreationSupport#isJavaInfo(ASTNode)}.
   */
  public void test_isJavaInfo() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    CreationSupport creationSupport = panel.getCreationSupport();
    // null
    {
      ASTNode node = null;
      assertTrue(creationSupport.isJavaInfo(node));
    }
    // ThisExpression
    {
      ASTNode node = m_lastEditor.getAstUnit().getAST().newThisExpression();
      assertTrue(creationSupport.isJavaInfo(node));
    }
    // Test() constructor
    {
      ASTNode node = creationSupport.getNode();
      assertTrue(creationSupport.isJavaInfo(node));
    }
  }

  /**
   * Normal constructor.
   */
  public void test_simple() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setBackground(Color.green);",
            "    JButton button = new JButton('My button');",
            "    add(button);",
            "  }",
            "}");
    {
      ThisCreationSupport creationSupport = (ThisCreationSupport) panel.getCreationSupport();
      assertEquals("this: javax.swing.JPanel", creationSupport.toString());
      // check constructor
      {
        TypeDeclaration typeDeclaration =
            (TypeDeclaration) m_lastEditor.getAstUnit().types().get(0);
        assertSame(typeDeclaration.getMethods()[0], creationSupport.getConstructor());
      }
      // check position
      {
        IMethod constructor = m_lastEditor.getModelUnit().getTypes()[0].getMethods()[0];
        int constructorOffset = constructor.getSourceRange().getOffset();
        assertEquals(constructorOffset, creationSupport.getNode().getStartPosition());
      }
      // check operations
      {
        assertFalse(creationSupport.canReorder());
        assertFalse(creationSupport.canReparent());
      }
    }
    // check children
    assertEquals(2, panel.getChildrenJava().size());
    assertTrue(panel.getChildrenJava().get(0) instanceof FlowLayoutInfo);
    assertTrue(panel.getChildrenJava().get(1) instanceof ContainerInfo);
    // check association
    assertInstanceOf(RootAssociation.class, panel.getAssociation());
    //
    assert_creation(panel);
  }

  /**
   * No constructor, should create default constructor.
   */
  public void test_noConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel implements java.io.Serializable {",
            "}");
    // check creation support
    ThisCreationSupport creationSupport = (ThisCreationSupport) panel.getCreationSupport();
    {
      MethodDeclaration constructor = (MethodDeclaration) creationSupport.getNode();
      assertTrue(constructor.isConstructor());
    }
    // check association
    assertInstanceOf(RootAssociation.class, panel.getAssociation());
    // modify property and ensure that source is valid
    panel.getPropertyByTitle("enabled").setValue(Boolean.FALSE);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel implements java.io.Serializable {",
        "  public Test() {",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_delete_hasDirectComponent() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setBackground(Color.green);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    CreationSupport creationSupport = panel.getCreationSupport();
    // can delete
    assertTrue(creationSupport.canDelete());
    assertTrue(panel.canDelete());
    // do delete
    panel.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // no problem with properties
    panel.getProperties();
    assertThat(panel.getAssociation()).isInstanceOf(RootAssociation.class);
  }

  public void test_delete_hasExposedComponent() throws Exception {
    ContainerInfo frame =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setBackground(Color.green);",
            "    getContentPane().setBackground(Color.green);",
            "    {",
            "      JButton button = new JButton();",
            "      getContentPane().add(button);",
            "    }",
            "  }",
            "}");
    CreationSupport creationSupport = frame.getCreationSupport();
    // can delete
    assertTrue(creationSupport.canDelete());
    assertTrue(frame.canDelete());
    // do delete
    frame.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JFrame {",
        "  public Test() {",
        "  }",
        "}");
    // no problem with properties
    frame.getProperties();
    assertThat(frame.getAssociation()).isInstanceOf(RootAssociation.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareMyPanel() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(boolean enabled) {",
            "    setEnabled(enabled);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='boolean' property='setEnabled(boolean)'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
  }

  /**
   * Test for {@link ThisCreationSupport#addAccessors(GenericPropertyDescription, java.util.List)}.
   */
  public void test_boundProperties() throws Exception {
    prepareMyPanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "class Test extends MyPanel {",
            "  public Test() {",
            "    super(false);",
            "  }",
            "}");
    // check "enabled"
    GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    List<ExpressionAccessor> accessors = getGenericPropertyAccessors(enabledProperty);
    assertThat(accessors).hasSize(2);
    assertThat(accessors.get(0)).isInstanceOf(SetterAccessor.class);
    assertThat(accessors.get(1)).isInstanceOf(SuperConstructorAccessor.class);
    // change "enabled"
    enabledProperty.setValue(true);
    assertEditor(
        "// filler filler filler",
        "class Test extends MyPanel {",
        "  public Test() {",
        "    super(true);",
        "  }",
        "}");
  }

  /**
   * Test for "Constructor" complex property.
   */
  public void test_superProperties() throws Exception {
    prepareMyPanel();
    ContainerInfo panel =
        parseContainer(
            "class Test extends MyPanel {",
            "  public Test() {",
            "    super(false);",
            "  }",
            "}");
    Property superProperty = panel.getPropertyByTitle("Constructor");
    assertNotNull(superProperty);
    //
    Property[] subProperties = getSubProperties(superProperty);
    assertEquals(1, subProperties.length);
    // check sub-property
    {
      GenericProperty enabledProperty = (GenericProperty) subProperties[0];
      assertEquals("enabled", enabledProperty.getTitle());
      assertInstanceOf(BooleanPropertyEditor.class, enabledProperty.getEditor());
      assertSame(boolean.class, enabledProperty.getType());
      assertEquals(false, enabledProperty.getValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // create()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_create_ConstructorInvocation() throws Exception {
    ContainerInfo button =
        parseContainer(
            "public class Test extends JButton {",
            "  public Test() {",
            "    this(false, 'txt');",
            "  }",
            "  public Test(boolean enabled, String text) {",
            "    super(text);",
            "    setEnabled(enabled);",
            "  }",
            "}");
    button.refresh();
    assertEquals("txt", ((JButton) button.getObject()).getText());
    assertEquals(false, ((JButton) button.getObject()).isEnabled());
  }

  public void test_create_SuperConstructorInvocation() throws Exception {
    ContainerInfo button =
        parseContainer(
            "public class Test extends JButton {",
            "  public Test() {",
            "    super('txt');",
            "  }",
            "}");
    button.refresh();
    assertEquals("txt", ((JButton) button.getObject()).getText());
  }

  public void test_create_useDefaultConstructor() throws Exception {
    ContainerInfo button =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JButton {",
            "  public Test() {",
            "  }",
            "}");
    button.refresh();
    assertEquals("", ((JButton) button.getObject()).getText());
  }

  /**
   * We should support constructors with "varArgs".
   */
  public void test_create_varArgs_useSequence() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private String[] names;",
            "  public MyPanel(int value, String ...names) {",
            "    this.names = names;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(1, 'a', 'b', 'c');",
            "  }",
            "}");
    refresh();
    // check "varArgs" value
    String[] names = (String[]) ReflectionUtils.getFieldObject(panel.getObject(), "names");
    assertThat(names).isEqualTo(new String[]{"a", "b", "c"});
  }

  /**
   * We should support constructors with "varArgs".
   */
  public void test_create_varArgs_useArray() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private String[] names;",
            "  public MyPanel(int value, String ...names) {",
            "    this.names = names;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(1, new String[]{'a', 'b', 'c'});",
            "  }",
            "}");
    refresh();
    // check "varArgs" value
    String[] names = (String[]) ReflectionUtils.getFieldObject(panel.getObject(), "names");
    assertThat(names).isEqualTo(new String[]{"a", "b", "c"});
  }

  /**
   * Sometimes code has compilation problems, such as not calling required "super" constructor. This
   * should not cause exception in our code.
   */
  public void test_create_noSuperConstructor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    m_ignoreCompilationProblems = true;
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test extends MyPanel {",
          "  public Test() {",
          "  }",
          "}");
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(ICoreExceptionConstants.EVAL_NO_CONSTRUCTOR, de.getCode());
    }
  }

  /**
   * We can not use CGLib when superclass has non-public constructor.
   * <p>
   * 40274: Can't parse a derived ViewPart
   */
  public void test_create_packagePrivateConstructor_forCGLib() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test extends MyPanel {",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.EVAL_NON_PUBLIC_CONSTRUCTOR, e.getCode());
    }
  }

  /**
   * If something bad happens during "super" constructor invocation, we should report about this
   * with as much details as possible.
   */
  public void test_create_exceptionWithDescription() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel(String s) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    try {
      m_ignoreCompilationProblems = true;
      parseContainer(
          "public class Test extends MyPanel {",
          "  public Test() {",
          "    super(0);",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(ICoreExceptionConstants.EVAL_CGLIB, de.getCode());
    }
  }

  /**
   * Test for intercepting method using description.
   */
  public void test_create_interceptMethod_usingDescription() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setName(getNameToUse());",
            "  }",
            "  protected abstract String getNameToUse();",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSource(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='this.interceptMethod: getNameToUse()'>'some name'</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public abstract class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertEquals("some name", panel.getComponent().getName());
  }

  /**
   * If abstract method is invoked from binary, but not implemented in AST, return default value.
   */
  public void test_create_useDefaultValueForAbstractMethods_Void() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    getFoo();",
            "  }",
            "  protected abstract void getFoo();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public abstract class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNoErrors(panel);
  }

  /**
   * If abstract method is invoked from binary, but not implemented in AST, return default value.
   */
  public void test_create_useDefaultValueForAbstractMethods_String() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  private final String m_foo;",
            "  public MyPanel() {",
            "    m_foo = getFoo();",
            "  }",
            "  protected abstract String getFoo();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public abstract class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    assertEquals("<dynamic>", ReflectionUtils.getFieldObject(panel.getObject(), "m_foo"));
  }

  /**
   * We should not try to intercept "private" methods, because CGLib can not invoke them later.
   */
  public void test_notInterceptPrivateMethod() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "  void foo() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    ReflectionUtils.invokeMethod(panel.getObject(), "foo()");
  }

  /**
   * It is possible that user code call abstract method outside of create operation, i.e. what we
   * are considering as "execution". We still should try our best to prevent
   * {@link AbstractMethodError}
   */
  public void test_abstractMethod_inNonExecution() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "  public void setVisible(boolean visible) {",
            "    super.setVisible(visible);",
            "    foo();",
            "  }",
            "  public abstract void foo();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  public void foo() {",
            "  }",
            "}");
    panel.refresh();
    // call setVisible() which will call abstract method
    panel.getComponent().setVisible(false);
  }
}
