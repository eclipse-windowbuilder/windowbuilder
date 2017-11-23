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
import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.IMoveTargetProvider;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IExposedCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IWrapperControlCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryContainerInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryRootProcessor;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.core.model.WrapperInfoTest;
import org.eclipse.wb.tests.designer.core.model.variables.ThisForcedMethodTest;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.preference.IPreferenceStore;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import static org.easymock.EasyMock.capture;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.assertj.core.api.Assertions;

import java.awt.FlowLayout;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Tests for {@link JavaInfoUtils}.
 * 
 * @author scheglov_ke
 */
public class JavaInfoUtilsTest extends SwingModelTest {
  private static final IPreferenceStore PREFERENCES = ToolkitProvider.DESCRIPTION.getPreferences();

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
  // Assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_assertIsNotDeleted() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // initially all OK
    JavaInfoUtils.assertIsNotDeleted(button);
    // delete, not assertion fails
    button.delete();
    try {
      JavaInfoUtils.assertIsNotDeleted(button);
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeDeclaration()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getTypeDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertSame(m_lastEditor.getAstUnit().types().get(0), JavaInfoUtils.getTypeDeclaration(panel));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodDeclaration()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodDeclaration_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
    assertSame(typeDeclaration.getMethods()[0], JavaInfoUtils.getMethodDeclaration(panel));
  }

  public void test_getMethodDeclaration_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('button');",
            "    add(button);",
            "  }",
            "}");
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
    assertSame(
        typeDeclaration.getMethods()[0],
        JavaInfoUtils.getMethodDeclaration(panel.getChildrenComponents().get(0)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditorState
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#getState(JavaInfo)}.
   */
  public void test_EditorState_getState() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertSame(m_lastState, JavaInfoUtils.getState(panel));
  }

  /**
   * Test for {@link JavaInfoUtils#getClassLoader(JavaInfo)}.
   */
  public void test_EditorState_getEditorLoader() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertSame(m_lastLoader, JavaInfoUtils.getClassLoader(panel));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isLocalField()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#isLocalField(JavaInfo, IField)}.
   */
  public void test_isLocalField() throws Exception {
    IType constants_1 =
        createModelType(
            "test",
            "IConstants_1.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "package test;",
                "public interface IConstants_1 {",
                "  int field_1 = 1;",
                "}"));
    IType constants_2 =
        createModelType(
            "test",
            "IConstants_2.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "package test;",
                "public interface IConstants_2 {",
                "  int field_2 = 2;",
                "}"));
    // parse
    JavaInfo javaInfo =
        parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test extends javax.swing.JPanel implements IConstants_1 {",
                "  int field_3 = 3;",
                "}"));
    // check
    assertTrue(JavaInfoUtils.isLocalField(javaInfo, constants_1.getField("field_1")));
    assertFalse(JavaInfoUtils.isLocalField(javaInfo, constants_2.getField("field_2")));
    {
      IType testType = m_testProject.getJavaProject().findType("test.Test");
      assertTrue(JavaInfoUtils.isLocalField(javaInfo, testType.getField("field_3")));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isImplicitlyCreated()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#isImplicitlyCreated(JavaInfo)}.
   */
  public void test_isImplicitlyCreated() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().add(new JButton());",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ComponentInfo button = contentPane.getChildrenComponents().get(0);
    //
    assertTrue(JavaInfoUtils.isImplicitlyCreated(contentPane));
    assertFalse(JavaInfoUtils.isImplicitlyCreated(button));
    assertFalse(JavaInfoUtils.isImplicitlyCreated(frame));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // scheduleSave()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_scheduleSave() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    String source = m_lastEditor.getSource();
    // do change in ICompilationUnit
    m_lastEditor.getModelUnit().getBuffer().replace(0, 0, "   ");
    // schedule save, no change on disk expected
    JavaInfoUtils.scheduleSave(panel);
    assertEquals(source, getFileContentSrc("test/Test.java"));
    // run UI loop, now changes should be on disk
    waitEventLoop(10);
    assertEquals("   " + source, getFileContentSrc("test/Test.java"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // scheduleOpenNode()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#scheduleOpenNode(JavaInfo, ASTNode)}.
   */
  public void test_scheduleOpenNode() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    MethodDeclaration constructor = JavaInfoUtils.getTypeDeclaration(panel).getMethods()[0];
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    Capture<Integer> openSourcePosition = new Capture<Integer>();
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      pageSite.openSourcePosition(capture(openSourcePosition));
      EasyMock.replay(pageSite);
      // do set
      DesignPageSite.Helper.setSite(panel, pageSite);
    }
    // open Node
    JavaInfoUtils.scheduleOpenNode(panel, constructor);
    // opened only after running messages loop
    assertFalse(openSourcePosition.hasCaptured());
    waitEventLoop(0);
    //
    EasyMock.verify(pageSite);
    assertTrue(openSourcePosition.hasCaptured());
    assertTrue(openSourcePosition.getValue() != 0);
    assertEquals(constructor.getStartPosition(), openSourcePosition.getValue().intValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getParameter(), setParameter()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#getParameter(JavaInfo, String)}.<br>
   * Normal component, created using {@link ClassInstanceCreation}.
   */
  public void test_getParameter_normalComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertEquals("false", JavaInfoUtils.getParameter(button, "layout.has"));
  }

  /**
   * Test for {@link JavaInfoUtils#getParameter(JavaInfo, String)}.<br>
   * Factory component, with parameter in XML {@link FactoryMethodDescription}.
   */
  public void test_getParameter_factoryComponent() throws Exception {
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
            "    <parameters>",
            "      <parameter name='parameter.1'>some value</parameter>",
            "    </parameters>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = StaticFactory.createButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // "parameter.1" exists for component...
    assertEquals("some value", JavaInfoUtils.getParameter(button, "parameter.1"));
    // ...but "parameter.1" was not from ComponentDescription
    assertNull(button.getDescription().getParameter("parameter.1"));
  }

  /**
   * Test for {@link JavaInfoUtils#hasTrueParameter(JavaInfo, String)}.
   */
  public void test_hasTrueParameter() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='trueParameter'>true</parameter>",
            "    <parameter name='falseParameter'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(JavaInfoUtils.hasTrueParameter(panel, "trueParameter"));
    assertFalse(JavaInfoUtils.hasTrueParameter(panel, "falseParameter"));
    assertFalse(JavaInfoUtils.hasTrueParameter(panel, "noSuchParameter"));
  }

  /**
   * Test for {@link JavaInfoUtils#setParameter(JavaInfo, String, String)}.
   */
  public void test_setParameter() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    String parameterName = "noSuchParameter";
    String parameterValue = "the Value";
    // initially no such parameter
    assertNull(JavaInfoUtils.getParameter(panel, parameterName));
    // set parameter
    JavaInfoUtils.setParameter(panel, parameterName, parameterValue);
    assertSame(parameterValue, JavaInfoUtils.getParameter(panel, parameterName));
  }

  public void test_getParameters() throws Exception {
    setJavaContentSrc("test", "MyPanel", new String[]{
        "public class MyPanel extends JPanel {",
        "  public MyPanel() {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <parameters>",
        "    <parameter name='test.parameter.1'>value_1</parameter>",
        "    <parameter name='test.parameter.2'>1000</parameter>",
        "  </parameters>",
        "</component>"});
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // check single parameters
    {
      assertThat(JavaInfoUtils.getParameter(panel, "test.parameter.1")).isEqualTo("value_1");
      assertThat(JavaInfoUtils.getParameter(panel, "test.parameter.2")).isEqualTo("1000");
      assertThat(JavaInfoUtils.getParameter(panel, "test.parameter.3")).isNull();
    }
    // check parameters map
    {
      Map<String, String> parameters = JavaInfoUtils.getParameters(panel);
      assertThat(parameters.get("test.parameter.1")).isEqualTo("value_1");
      assertThat(parameters.get("test.parameter.2")).isEqualTo("1000");
      assertThat(parameters.get("test.parameter.3")).isNull();
    }
    // set new parameter
    JavaInfoUtils.setParameter(panel, "test.parameter.3", "true");
    // check parameters map
    {
      // check mapped values
      Map<String, String> parameters = JavaInfoUtils.getParameters(panel);
      assertThat(parameters.get("test.parameter.1")).isEqualTo("value_1");
      assertThat(parameters.get("test.parameter.2")).isEqualTo("1000");
      assertThat(parameters.get("test.parameter.3")).isEqualTo("true");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#executeScriptParameter(JavaInfo, String)}.
   */
  public void test_executeScriptParameter() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // execute not existing script
    {
      assertSame(null, JavaInfoUtils.executeScriptParameter(panel, "noSuchScript"));
    }
    // execute existing script, use "model"
    {
      JavaInfoUtils.setParameter(panel, "script", "return model;");
      assertSame(panel, JavaInfoUtils.executeScriptParameter(panel, "script"));
    }
    // execute existing script, use "object"
    {
      JavaInfoUtils.setParameter(panel, "script", "return object;");
      assertSame(panel.getObject(), JavaInfoUtils.executeScriptParameter(panel, "script"));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#executeScript(JavaInfo, String)}.
   */
  public void test_executeScript() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // use "model"
    assertSame(panel, JavaInfoUtils.executeScript(panel, "return model;"));
    // use "object"
    assertSame(panel.getObject(), JavaInfoUtils.executeScript(panel, "return object;"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTarget()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we can leave block when move up.
   */
  public void test_getTarget_before_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText('ABC');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, button);
    assertTarget(target, null, getStatement(panel, 1), true);
  }

  /**
   * Test that we can go up after leaving block.
   */
  public void test_getTarget_before_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "    JButton button = new JButton();",
            "    {",
            "      button.setText('ABC');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, button);
    assertTarget(target, null, getStatement(panel, 1), true);
  }

  /**
   * Test that we stop on not related statement.
   */
  public void test_getTarget_before_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "    {",
            "      JButton button = new JButton();",
            "      int a;",
            "      button.setText('ABC');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, button);
    assertTarget(target, null, getStatement(panel, 1, 2), true);
  }

  /**
   * Test that we can stop at first statement of method.
   */
  public void test_getTarget_before_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText('ABC');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, button);
    assertTarget(target, null, getStatement(panel, 0), true);
  }

  /**
   * Test that we can add as last child.
   */
  public void test_getTarget_last_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText('ABC');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  /**
   * Test that we can stop at last statement of method.
   */
  public void test_getTarget_last_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setText('ABC');",
            "    add(button);",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 2), false);
  }

  /**
   * Test that we can add without any child in main().
   */
  public void test_getTarget_last_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  /**
   * Test that we can add without any child in constructor, but with parent statement.
   */
  public void test_getTarget_last_5() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  /**
   * Test that we can add without any child in constructor, but with parent statement in
   * {@link Block}.
   */
  public void test_getTarget_last_parentStatementInBlock() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      setEnabled(true);",
            "      int foo;",
            "    }",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0, 0), false);
  }

  /**
   * Method {@link JPanel#updateUI()} or {@link JPanel#removeAll()} are not executable, so can not
   * be reference, so "setEnabled()" will be used for target.
   * <p>
   * We test also that "this." and "null" expressions are recognized as "this" component.
   */
  public void test_getTarget_last_6_ignoreNonExecutable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "    this.updateUI();",
            "    removeNotify();",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  /**
   * Test that we can add without any child or statement in constructor.
   */
  public void test_getTarget_last_7() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, getMethod("<init>()").getBody(), null, true);
  }

  /**
   * Test that we can add without any child and with statements in different method.
   */
  public void test_getTarget_last_8() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private void configurePanel() {",
            "    setEnabled(true);",
            "  }",
            "  public Test() {",
            "    configurePanel();",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    Statement expectedStatement = getStatement(panel, "configurePanel()", 0);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * When execution flow leaves method that adds new components to container and leads into other
   * method that just configures created components, then target should be in first method.
   * <p>
   * This test: no components in configure(), but has component before configure().
   */
  public void test_getTarget_last_dontUseConfigure_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    {",
            "      button = new JButton();",
            "      add(button);",
            "    }",
            "    configure();",
            "  }",
            "  private void configure() {",
            "    button.setEnabled(true);",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  /**
   * When execution flow leaves method that adds new components to container and leads into other
   * method that just configures created components, then target should be in first method.
   * <p>
   * This test: has component in configure().
   */
  public void test_getTarget_last_dontUseConfigure_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    {",
            "      button = new JButton();",
            "      add(button);",
            "    }",
            "    configure();",
            "  }",
            "  private void configure() {",
            "    add(new JButton('new'));",
            "    button.setEnabled(true);",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, "configure()", 1), false);
  }

  /**
   * When execution flow leaves method that adds new components to container and leads into other
   * method that just configures created components, then target should be in first method.
   * <p>
   * This test: component is only in configure().
   */
  public void test_getTarget_last_dontUseConfigure_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    configure();",
            "  }",
            "  private void configure() {",
            "    add(new JButton('new'));",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, "configure()", 0), false);
  }

  /**
   * If target is NVO, then its parent is not {@link JavaInfo}, but this should not cause problems.
   */
  public void test_getTarget_last_nonVisual() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private JButton m_button = new JButton();",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "    {new: javax.swing.JButton} {field-initializer: m_button} {/new JButton()/}");
    JavaInfo button = getJavaInfoByName("m_button");
    //
    StatementTarget target = JavaInfoUtils.getTarget(button, null);
    assertTarget(target, getBlock(panel), null, true);
  }

  /**
   * Test target after container with children.
   */
  public void test_getTarget_afterContainer_withChildren() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel2 = new JPanel();",
            "      add(panel2);",
            "      {",
            "        JButton button = new JButton();",
            "        button.setText('button');",
            "        panel2.add(button);",
            "      }",
            "      panel2.setEnabled(true);",
            "    }",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  /**
   * Test target after {@link JFrame} with its "contentPane" and children.
   */
  public void test_getTarget_afterContainer_withImplicitChild() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      getContentPane().add(button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      getContentPane().add(button_2);",
            "    }",
            "  }",
            "}");
    //
    StatementTarget target = JavaInfoUtils.getTarget(frame, null);
    assertTarget(target, null, getStatement(frame, 1), false);
  }

  /**
   * Test target: as last child of panel2, i.e. that we don't leave block of "panel2" because is
   * will become invisible.
   */
  public void test_getTarget_10() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel2 = new JPanel();",
            "      add(panel2);",
            "      panel2.setEnabled(true);",
            "    }",
            "  }",
            "}");
    ComponentInfo panel2 = panel.getChildrenComponents().get(0);
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel2, null);
    assertTarget(target, null, getStatement(panel, 0, 2), false);
  }

  /**
   * Test target: as last child of lazy created panel2
   */
  public void test_getTarget_11() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getPanel2());",
            "  }",
            "  private JPanel panel2;",
            "  private JPanel getPanel2() {",
            "    if (panel2 == null) {",
            "      panel2 = new JPanel();",
            "      panel2.setEnabled(true);",
            "    }",
            "    return panel2;",
            "  }",
            "}");
    ComponentInfo panel2 = panel.getChildrenComponents().get(0);
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel2, null);
    Statement expectedStatement = getStatement(panel, "getPanel2()", 0, 1);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * Test that when parent has local variable, we don't leave method that defines it.
   */
  public void test_getTarget_12() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    add(createInnerPanel());",
            "  }",
            "  private JPanel createInnerPanel() {",
            "    JPanel innerPanel = new JPanel();",
            "    return innerPanel;",
            "  }",
            "}");
    // prepare inner panel
    assertEquals(1, panel.getChildrenComponents().size());
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    // check target on inner panel
    StatementTarget target = JavaInfoUtils.getTarget(innerPanel, null);
    assertTarget(target, null, getStatement(panel, "createInnerPanel()", 0), false);
  }

  /**
   * Test target: as last child of "panel", we should not leave block of "panel".
   */
  public void test_getTarget_13() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  private static JPanel panel;",
            "  public static void main(String args[]) {",
            "    {",
            "      panel = new JPanel();",
            "    }",
            "  }",
            "}");
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    // check target
    Statement expectedStatement = getStatement(panel, "main(java.lang.String[])", 0, 0);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * Test target: as last child of "panel", in theory we should not leave block of "panel". However
   * on practice we can not know if we can leave block, for example we should leave block if this is
   * block of child.
   */
  public void test_getTarget_14() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  private static JPanel panel = new JPanel();",
            "  public static void main(String args[]) {",
            "    {",
            "      panel.setEnabled(true);",
            "    }",
            "  }",
            "}");
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    // check target
    Statement expectedStatement = getStatement(panel, "main(java.lang.String[])", 0);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * Test for {@link JavaInfoUtils#getTarget(JavaInfo, JavaInfo)}.<br>
   * We set {@link JavaEventListener#target_isTerminalStatement(JavaInfo, Statement, boolean[])}
   * that say that one of the {@link Statement}'s is terminal.
   */
  public void test_getTarget_15_broadcast() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPanel panel2 = new JPanel();",
            "    add(panel2);",
            "    panel2.setEnabled(true);",
            "    {",
            "      JButton button = new JButton();",
            "      panel2.add(button);",
            "    }",
            "    panel2.setAutoscrolls(true);",
            "    panel2.setEnabled(false);",
            "  }",
            "}");
    final ComponentInfo panel2 = panel.getChildrenComponents().get(0);
    // don't allow "setAutoscrolls"
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void target_isTerminalStatement(JavaInfo parent,
          JavaInfo child,
          Statement statement,
          boolean[] terminal) {
        // only "panel2" should be asked, not "button"
        assertSame(panel2, parent);
        // dirty check for "setAutoscrolls"
        if (m_lastEditor.getSource(statement).contains("setAutoscrolls")) {
          terminal[0] = true;
        }
      }
    });
    //
    StatementTarget target = JavaInfoUtils.getTarget(panel2, null);
    assertTarget(target, null, getStatement(panel, 3), false);
  }

  /**
   * Test for {@link JavaInfoUtils#getTarget(JavaInfo, JavaInfo)}.<br>
   * Target for {@link ExposedPropertyCreationSupport} is same as for its host {@link JavaInfo}.
   */
  public void test_getTarget_16_exposed() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    setEnabled(true);",
            "  }",
            "}");
    // target for "frame" is "after last statement"
    {
      StatementTarget target = JavaInfoUtils.getTarget(frame, null);
      assertTarget(target, null, getStatement(frame, 0), false);
    }
    // target for "contentPane" is same as for "frame"
    {
      ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
      StatementTarget target = JavaInfoUtils.getTarget(contentPane, null);
      assertTarget(target, null, getStatement(frame, 0), false);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#getTarget(JavaInfo, JavaInfo)}.<br>
   * Only children with "visible" {@link Association} can be used as reference.<br>
   * For now "visible" means that {@link Association#getStatement()} is not <code>null</code>.
   */
  public void test_getTarget_16_invisibleAssociation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  private JPanel container = new JPanel();",
            "  public MyPanel() {",
            "    add(container);",
            "    container.add(createClient());",
            "  }",
            "  public Container getContainer() {",
            "    return container;",
            "  }",
            "  protected abstract Component createClient();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    int justSomeStatement;",
            "  }",
            "  protected Component createClient() {",
            "    JButton clientButton = new JButton();",
            "    return clientButton;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public java.awt.Container test.MyPanel.getContainer()} {property} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {local-unique: clientButton} {/new JButton()/ /clientButton/}");
    // check target for placing on "getContainer()"
    {
      ContainerInfo container = (ContainerInfo) panel.getChildrenComponents().get(0);
      StatementTarget target = JavaInfoUtils.getTarget(container, null);
      Block expectedBlock = ((MethodDeclaration) panel.getCreationSupport().getNode()).getBody();
      assertTarget(target, expectedBlock, null, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Forced method
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Empty constructor, no any child or statement.<br>
   * This is just basic test, see {@link ThisForcedMethodTest} for more tests.
   */
  public void test_getTarget_forcedMethod_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    PreferencesRepairer preferencesRepairer = new PreferencesRepairer(PREFERENCES);
    try {
      preferencesRepairer.setValue(GenerationSettings.P_FORCED_METHOD, "init");
      // check target
      StatementTarget target = JavaInfoUtils.getTarget(panel, null);
      assertEditor(
          "// filler filler filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "    init();",
          "  }",
          "  private void init() {",
          "  }",
          "}");
      assertTarget(target, getMethod("init()").getBody(), null, false);
    } finally {
      preferencesRepairer.restore();
    }
  }

  /**
   * Constructor, with related statements and "super" constructor invocation.<br>
   * This is just basic test, see {@link ThisForcedMethodTest} for more tests.
   */
  public void test_getTarget_forcedMethod_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    super();",
            "    setBackground(Color.ORANGE);",
            "  }",
            "}");
    //
    PreferencesRepairer preferencesRepairer = new PreferencesRepairer(PREFERENCES);
    try {
      preferencesRepairer.setValue(GenerationSettings.P_FORCED_METHOD, "init");
      // check target
      StatementTarget target = JavaInfoUtils.getTarget(panel, null);
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    super();",
          "    init();",
          "  }",
          "  private void init() {",
          "    setBackground(Color.ORANGE);",
          "  }",
          "}");
      assertTarget(target, getMethod("init()").getBody(), null, false);
    } finally {
      preferencesRepairer.restore();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTarget: last
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepare_getTarget_last() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled' order='last'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
  }

  /**
   * New components should be added <em>before</em> the <code>setEnabled()</code> invocation.
   */
  public void test_getTarget_order_last1() throws Exception {
    prepare_getTarget_last();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // check target
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    Statement expectedStatement = getStatement(panel, 0);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * New components should be added <em>before</em> the <code>setEnabled()</code> invocation.
   */
  public void test_getTarget_order_last2() throws Exception {
    prepare_getTarget_last();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // check target
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    assertTarget(target, getMethod("<init>()").getBody(), null, true);
  }

  /**
   * New components should be added <em>before</em> <code>processChildren</code> invocation, even if
   * it has references on children.
   */
  public void test_getTarget_order_last3() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  // just some method that accepts children",
            "  public void processChildren(Component[] components) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='processChildren' order='last'>",
            "      <parameter type='java.awt.Component[]'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    processChildren(new Component[]{button});",
            "  }",
            "}");
    // check target
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    Statement expectedStatement = getStatement(panel, 1);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * Yes, there is <code>setExpanded(true)</code> invocation for child of {@link JPanel}, and it
   * should be last, but when we add new children on {@link JPanel} itself, we don't care.
   */
  public void test_getTarget_order_last4() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void setExpanded(boolean expanded) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setExpanded' order='last'>",
            "      <parameter type='boolean'/>",
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
            "    myPanel.setExpanded(true);",
            "  }",
            "}");
    // check target
    StatementTarget target = JavaInfoUtils.getTarget(panel, null);
    Statement expectedStatement = getStatement(panel, 2);
    assertTarget(target, null, expectedStatement, false);
  }

  /**
   * When "lazy" is used for "implicit factory", we create/associate component using artificial
   * invocation of accessor. So, target "before component" should be before this invocation, not
   * before "create" method invocation.
   */
  public void test_getTarget_lazy_beforeImplicitFactory() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JButton addButton() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private MyBar bar;",
            "  private JButton button;",
            "  Test() {",
            "    add(getBar());",
            "  }",
            "  private MyBar getBar() {",
            "    if (bar == null) {",
            "      bar = new MyBar();",
            "      getButton();",
            "    }",
            "    return bar;",
            "  }",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = getBar().addButton();",
            "    }",
            "    return button;",
            "  }",
            "}");
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo existingButton = bar.getChildrenComponents().get(0);
    // check target
    StatementTarget target = JavaInfoUtils.getTarget(panel, existingButton);
    Statement expectedStatement = getStatement(panel, "getBar()", 0, 1);
    assertTarget(target, null, expectedStatement, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // add()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#add(JavaInfo, Association, JavaInfo, JavaInfo)}.<br>
   * By default target is "after last related statement".
   */
  public void test_add_target_defaultAfterLastStatement() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // add
    ComponentInfo button = createJButton();
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.add(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#add(JavaInfo, Association, JavaInfo, JavaInfo)}.<br>
   * Add new component after existing component with exposed child.
   */
  public void test_add_target_afterExposedComponent() throws Exception {
    setFileContentSrc(
        "test/ComplexPanel.java",
        getTestSource(
            "public class ComplexPanel extends JPanel {",
            "  private final JButton button = new JButton();",
            "  public ComplexPanel() {",
            "    add(button);",
            "  }",
            "  public JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      ComplexPanel complexPanel = new ComplexPanel();",
            "      add(complexPanel);",
            "      complexPanel.getButton().setText('text');",
            "    }",
            "  }",
            "}");
    // add
    ComponentInfo button = createJButton();
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.add(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      ComplexPanel complexPanel = new ComplexPanel();",
        "      add(complexPanel);",
        "      complexPanel.getButton().setText('text');",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#addTarget(JavaInfo, Association, JavaInfo, StatementTarget)}.<br>
   * We specify {@link StatementTarget} - after "setFont()".
   */
  public void test_add_target_explicitTarget() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // prepare target
    StatementTarget target;
    {
      Statement setFont_statement = getStatement(panel, 0);
      target = new StatementTarget(setFont_statement, false);
    }
    // add
    ComponentInfo button = createJButton();
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.addTarget(button, associationObject, panel, target);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    setEnabled(false);",
        "  }",
        "}");
    Assertions.assertThat(panel.getChildrenComponents()).containsOnly(button);
  }

  /**
   * Test for {@link JavaInfoUtils#addFirst(JavaInfo, Association, JavaInfo)}.<br>
   * No other components, so just add before all related statements.
   */
  public void test_addFirst_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    assertTrue(panel.getChildrenComponents().isEmpty());
    // add
    ComponentInfo button = createJButton();
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.addFirst(button, associationObject, panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check "components" list
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      assertEquals(1, components.size());
      assertSame(button, components.get(0));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#addFirst(JavaInfo, Association, JavaInfo)}.<br>
   * Add before existing {@link JLabel}.
   */
  public void test_addFirst_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "    add(new JLabel());",
            "  }",
            "}");
    // initially only JLabel in "components"
    ComponentInfo label;
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      assertEquals(1, components.size());
      label = panel.getChildrenComponents().get(0);
    }
    // add
    ComponentInfo button = createJButton();
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.addFirst(button, associationObject, panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    add(new JLabel());",
        "  }",
        "}");
    // check "components" list
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      assertEquals(2, components.size());
      assertSame(button, components.get(0));
      assertSame(label, components.get(1));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#add(JavaInfo, Association, JavaInfo, JavaInfo)}.<br>
   * Creation generic component by default & with specified argument.
   */
  public void test_create_withTypeParameters() throws Exception {
    // prepare generic MyButton
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton<T extends java.lang.Number> extends JButton {",
            "  public MyButton() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton<%T%>()]]></source>",
            "    <typeParameters>",
            "      <typeParameter name='T' type='java.lang.Number' title='Generic type &lt;T&gt;'/>",
            "    </typeParameters>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(panel.getChildrenComponents().isEmpty());
    FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
    // add by default
    {
      ComponentInfo button = createJavaInfo("test.MyButton");
      layout.add(button, null);
      assertEditor(
          "// filler filler filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "    {",
          "      MyButton<Number> myButton = new MyButton<Number>();",
          "      add(myButton);",
          "    }",
          "  }",
          "}");
    }
    // add specified argument
    {
      ComponentInfo button = createJavaInfo("test.MyButton");
      button.putTemplateArgument("T", "java.lang.Double");
      layout.add(button, null);
      assertEditor(
          "// filler filler filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "    {",
          "      MyButton<Number> myButton = new MyButton<Number>();",
          "      add(myButton);",
          "    }",
          "    {",
          "      MyButton<Double> myButton = new MyButton<Double>();",
          "      add(myButton);",
          "    }",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD: association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When container {@link AssociationObject} is not specified, then only {@link Association} from
   * component should be used.
   */
  public void test_add_association_noContainerAssociation() throws Exception {
    prepareMyButton();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // add
    ComponentInfo button = createComponent("test.MyButton");
    JavaInfoUtils.add(button, null, panel, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setEnabled(false);",
        "    {",
        "      MyButton myButton = new MyButton(this);",
        "    }",
        "  }",
        "}");
    assertThat(button.getAssociation()).isInstanceOf(ConstructorParentAssociation.class);
  }

  /**
   * When container {@link AssociationObject} is not required, then only {@link Association} from
   * component should be used.
   */
  public void test_add_association_notRequiredContainerAssociation() throws Exception {
    prepareMyButton();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // add
    ComponentInfo button = createComponent("test.MyButton");
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.add(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setEnabled(false);",
        "    {",
        "      MyButton myButton = new MyButton(this);",
        "    }",
        "  }",
        "}");
    assertThat(button.getAssociation()).isInstanceOf(ConstructorParentAssociation.class);
  }

  /**
   * When container {@link AssociationObject} is required, then it will be mixed with
   * {@link Association} from component.
   */
  public void test_add_association_requiredContainerAssociation() throws Exception {
    prepareMyButton();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // add
    ComponentInfo button = createComponent("test.MyButton");
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", true);
    JavaInfoUtils.add(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setEnabled(false);",
        "    {",
        "      MyButton myButton = new MyButton(this);",
        "      add(myButton);",
        "    }",
        "  }",
        "}");
    {
      CompoundAssociation compoundAssociation = (CompoundAssociation) button.getAssociation();
      List<Association> associations = compoundAssociation.getAssociations();
      assertThat(associations).hasSize(2);
      assertEquals("new MyButton(this)", associations.get(0).getSource());
      assertEquals("add(myButton)", associations.get(1).getSource());
    }
  }

  private void prepareMyButton() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
            "    container.add(this);",
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
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // move()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move inside of same parent.
   */
  public void test_move_inSameParent_local() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
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
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // do move
    JavaInfoUtils.move(button_2, null, panel, button_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
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

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move inside of same parent, {@link LazyVariableSupport}.
   */
  public void test_move_inSameParent_lazy() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    add(getButton_1());",
            "    add(getButton_2());",
            "  }",
            "  private JButton getButton_1() {",
            "    if (button_1 == null) {",
            "      button_1 = new JButton();",
            "    }",
            "    return button_1;",
            "  }",
            "  private JButton getButton_2() {",
            "    if (button_2 == null) {",
            "      button_2 = new JButton();",
            "    }",
            "    return button_2;",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // do move
    JavaInfoUtils.move(button_2, null, panel, button_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button_1;",
        "  private JButton button_2;",
        "  public Test() {",
        "    add(getButton_2());",
        "    add(getButton_1());",
        "  }",
        "  private JButton getButton_1() {",
        "    if (button_1 == null) {",
        "      button_1 = new JButton();",
        "    }",
        "    return button_1;",
        "  }",
        "  private JButton getButton_2() {",
        "    if (button_2 == null) {",
        "      button_2 = new JButton();",
        "    }",
        "    return button_2;",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move inside of same parent.<br>
   * Bad attempt to move component before itself.
   */
  public void test_move_ignoreBecauseBeforeItself() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do move
    JavaInfoUtils.move(button, null, panel, button);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfoUtils.IMoveTargetProvider)}
   * .<br>
   * We implement {@link IMoveTargetProvider} and place component before <code>setEnabled()</code>.
   */
  public void test_move_IMoveTargetProvider() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
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
    final ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    final ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // do move
    final StatementTarget target = new StatementTarget(getStatement(panel, 0), true);
    IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
      public void add() throws Exception {
        panel.addChild(button_2, button_1);
      }

      public void move() throws Exception {
        panel.moveChild(button_2, button_1);
      }

      public StatementTarget getTarget() throws Exception {
        return target;
      }
    };
    JavaInfoUtils.moveProvider(button_2, null, panel, targetProvider);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2);",
        "    }",
        "    setEnabled(false);",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "  }",
        "}");
    // check "components" list
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      assertSame(button_2, components.get(0));
      assertSame(button_1, components.get(1));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move into new parent, before other component.
   */
  public void test_move_otherParent_beforeComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    // do move
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.move(button, associationObject, panel, innerPanel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move into new parent, as last component.
   */
  public void test_move_otherParent_asLast() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    // do move
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.move(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we move "button" in code like this <code>inner.add(new JButton())</code> we should
   * materialize "button" to avoid its removing with association.
   */
  public void test_move_otherParent_materialize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPanel inner = new JPanel();",
            "    add(inner);",
            "    inner.add(new JButton());",
            "  }",
            "}");
    ContainerInfo inner = getJavaInfoByName("inner");
    ComponentInfo button = inner.getChildrenComponents().get(0);
    // do reparent, causes materialize
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    flowLayout.move(button, null);
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPanel inner = new JPanel();",
        "    add(inner);",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move inside of same parent, but using alternative association.
   */
  public void test_move_sameParent_alternativeAssociation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setHeader(Component component) {",
            "    add(component);",
            "  }",
            "  public void setClient(Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setHeader'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "    <method name='setClient'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      setHeader(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do move
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.setClient(%child%)", true);
    JavaInfoUtils.move(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      setClient(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move inside of same parent, but using alternative association.<br>
   * Uses also component with "parent" in constructor.
   */
  public void test_move_sameParent_alternativeAssociation_parentInConstructor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setHeader(Component component) {",
            "    add(component);",
            "  }",
            "  public void setClient(Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setHeader'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "    <method name='setClient'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
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
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton(this);",
            "      setHeader(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do move
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.setClient(%child%)", true);
    JavaInfoUtils.move(button, associationObject, panel, null);
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton(this);",
        "      setClient(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.<br>
   * Move into new parent.<br>
   * Uses also component with "parent" in constructor.
   */
  public void test_move_otherParent_parentInConstructor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setHeader(Component component) {",
            "    add(component);",
            "  }",
            "  public void setClient(Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setHeader'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "    <method name='setClient'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
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
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton(this);",
            "      setHeader(button);",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    // do move
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.add(%child%)", false);
    JavaInfoUtils.move(button, associationObject, innerPanel, null);
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "      {",
        "        MyButton button = new MyButton(innerPanel);",
        "        innerPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.
   * <p>
   * Don't move {@link Statement} in "configure" method.
   */
  public void test_move_dontMoveStatementsInConfigure() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button_2 = new JButton();",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      button_2.setEnabled(false);",
            "      add(button_2);",
            "    }",
            "    configureButton_2();",
            "  }",
            "  public void configureButton_2() {",
            "    button_2.setText('text');",
            "  }",
            "}");
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    // do move
    JavaInfoUtils.move(button_2, null, panel, button_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button_2 = new JButton();",
        "  public Test() {",
        "    {",
        "      button_2.setEnabled(false);",
        "      add(button_2);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "    configureButton_2();",
        "  }",
        "  public void configureButton_2() {",
        "    button_2.setText('text');",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfoUtils#move(JavaInfo, Association, boolean, JavaInfo, JavaInfo)}.
   */
  public void test_move_componentInSeparateMethod() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    createButton();",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner);",
        "    }",
        "  }",
        "  private void createButton() {",
        "    {",
        "      JButton button = new JButton();",
        "      button.setEnabled(true);",
        "      add(button);",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    ContainerInfo inner = getJavaInfoByName("inner");
    // do move
    JavaInfoUtils.move(
        button,
        AssociationObjects.invocationChild("%parent%.add(%child%)", true),
        inner,
        null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    createButton();",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner);",
        "      {",
        "        JButton button = new JButton();",
        "        inner.add(button);",
        "        button.setEnabled(true);",
        "      }",
        "    }",
        "  }",
        "  private void createButton() {",
        "  }",
        "}");
  }

  /**
   * There was implementation when we used {@link MethodInvocation} as target for new association.
   * However after preparing this {@link MethodInvocation} was removed because of moving component
   * from its old parent.
   */
  public void test_move_removeInvocation_whichIsAfterAssociation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled' order='afterAssociation'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  private final MyButton button = new MyButton();",
        "  public Test() {",
        "    JPanel panelA = new JPanel();",
        "    add(panelA);",
        "    ",
        "    JPanel panelB = new JPanel();",
        "    add(panelB);",
        "    ",
        "    panelA.add(button);",
        "    button.setEnabled(false);",
        "  }",
        "}");
    final ComponentInfo button = getJavaInfoByName("button");
    final ContainerInfo panelB = getJavaInfoByName("panelB");
    // install handler for removing setEnabled() invocation
    button.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
        button.removeMethodInvocations("setEnabled(boolean)");
      }
    });
    // do move
    JavaInfoUtils.move(
        button,
        AssociationObjects.invocationChild("%parent%.add(%child%)", true),
        panelB,
        null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final MyButton button = new MyButton();",
        "  public Test() {",
        "    JPanel panelA = new JPanel();",
        "    add(panelA);",
        "    ",
        "    JPanel panelB = new JPanel();",
        "    add(panelB);",
        "    panelB.add(button);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // sort*ByFlow()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#sortComponentsByFlow(java.util.List)}.
   */
  public void test_sortComponentsByFlow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JLabel());",
            "    add(new JTextField());",
            "  }",
            "}");
    ComponentInfo component_0 = panel.getChildrenComponents().get(0);
    ComponentInfo component_1 = panel.getChildrenComponents().get(1);
    // check 0: no components
    {
      List<JavaInfo> components = Lists.<JavaInfo>newArrayList();
      JavaInfoUtils.sortComponentsByFlow(components);
      assertThat(components).isEmpty();
    }
    // check 1: components already in correct order
    {
      List<JavaInfo> components = Lists.<JavaInfo>newArrayList(component_0, component_1);
      JavaInfoUtils.sortComponentsByFlow(components);
      assertThat(components).hasSize(2);
      assertSame(component_0, components.get(0));
      assertSame(component_1, components.get(1));
    }
    // check 2: components in reverse order
    {
      List<JavaInfo> components = Lists.<JavaInfo>newArrayList(component_1, component_0);
      JavaInfoUtils.sortComponentsByFlow(components);
      assertThat(components).hasSize(2);
      assertSame(component_0, components.get(0));
      assertSame(component_1, components.get(1));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int a;",
            "    int b;",
            "  }",
            "}");
    Statement statementA = getStatement(panel, 0);
    Statement statementB = getStatement(panel, 1);
    check_sortNodesByFlow2(statementA, statementB);
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow_withBlock() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int a;",
            "    {",
            "      int b;",
            "    }",
            "  }",
            "}");
    Statement statementA = getStatement(panel, 0);
    Statement statementBlock = getStatement(panel, 1);
    check_sortNodesByFlow2(statementA, statementBlock);
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow_nestedBlockStatement() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      int a;",
            "    }",
            "  }",
            "}");
    Statement blockA = getStatement(panel, 0);
    Statement statementA = getStatement(panel, 0, 0);
    check_sortNodesByFlow(blockA, statementA, true);
    check_sortNodesByFlow(statementA, blockA, false);
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow_nestedBodyDeclarationParts() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private int value;",
            "  public Test() {",
            "  }",
            "}");
    FieldDeclaration fieldDeclaration = (FieldDeclaration) getBodyDeclaration(panel, 0);
    SimpleName valueName = DomGenerics.fragments(fieldDeclaration).get(0).getName();
    check_sortNodesByFlow(fieldDeclaration, valueName, true);
    check_sortNodesByFlow(valueName, fieldDeclaration, false);
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow_nestedStatementParts() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int value;",
            "  }",
            "}");
    VariableDeclarationStatement statement = (VariableDeclarationStatement) getStatement(panel, 0);
    SimpleName valueName = DomGenerics.fragments(statement).get(0).getName();
    check_sortNodesByFlow(statement, valueName, true);
    check_sortNodesByFlow(valueName, statement, false);
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   * <p>
   * Nodes not included into execution flow should be removed.
   */
  public void test_sortNodesByFlow_nodeNotInExecutionFlow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int value;",
            "  }",
            "  public void foo() {",
            "  }",
            "}");
    Statement statement = getStatement(panel, 0);
    MethodDeclaration fooMethod = JavaInfoUtils.getTypeDeclaration(panel).getMethods()[1];
    {
      List<ASTNode> nodes = Lists.<ASTNode>newArrayList(statement, fooMethod);
      ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
      JavaInfoUtils.sortNodesByFlow(flowDescription, true, nodes);
      assertThat(nodes).hasSize(1).containsOnly(statement);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow_withLocalMethodInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    foo();",
            "  }",
            "  public void foo() {",
            "    int a;",
            "  }",
            "}");
    Statement statementInv = getStatement(panel, 0);
    Statement statementVar = getStatement(panel, "foo()", 0);
    check_sortNodesByFlow(statementInv, statementVar, true);
    check_sortNodesByFlow(statementVar, statementInv, false);
  }

  /**
   * Test for {@link JavaInfoUtils#sortNodesByFlow(java.util.List)}.
   */
  public void test_sortNodesByFlow_Statement_itsNode() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "  }",
            "}");
    Statement statement = getStatement(panel, 0);
    ClassInstanceCreation creation =
        (ClassInstanceCreation) m_lastEditor.getEnclosingNode("new JButton");
    check_sortNodesByFlow(statement, creation, true);
    check_sortNodesByFlow(creation, statement, false);
  }

  /**
   * Checks that "node_1" is always sorted before "node_2".
   */
  private void check_sortNodesByFlow2(ASTNode node_1, ASTNode node_2) {
    check_sortNodesByFlow(node_1, node_2, true);
    check_sortNodesByFlow(node_1, node_2, false);
  }

  /**
   * Checks that "node_1" is always sorted before "node_2".
   */
  private void check_sortNodesByFlow(ASTNode node_1, ASTNode node_2, boolean onEnter) {
    ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
    // check 1: nodes already in correct order
    {
      List<ASTNode> nodes = Lists.<ASTNode>newArrayList(node_1, node_2);
      JavaInfoUtils.sortNodesByFlow(flowDescription, onEnter, nodes);
      assertEquals(2, nodes.size());
      assertSame(node_1, nodes.get(0));
      assertSame(node_2, nodes.get(1));
    }
    // check 2: nodes in reverse order
    {
      List<ASTNode> nodes = Lists.<ASTNode>newArrayList(node_2, node_1);
      JavaInfoUtils.sortNodesByFlow(flowDescription, onEnter, nodes);
      assertEquals(2, nodes.size());
      assertSame(node_1, nodes.get(0));
      assertSame(node_2, nodes.get(1));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isCreatedAtTarget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_afterStatement() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int foo;",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "    int bar;",
            "    {",
            "      // empty block",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // after Statement that is before "button" Block
    {
      Statement targetStatement = getStatement(panel, 0);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertFalse(isCreatedAtTarget(button, target));
    }
    // after Statement that defines "button"
    {
      Statement targetStatement = getStatement(panel, 1, 0);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
    // after Statement that is after "button" Block
    {
      Statement targetStatement = getStatement(panel, 2);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
    // after "button" Block itself
    {
      Statement targetStatement = getStatement(panel, 1);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
    // after "empty" Block
    {
      Statement targetStatement = getStatement(panel, 3);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_afterStatement_blocks() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      {",
            "        JButton button = new JButton();",
            "        add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // after inner Block
    {
      Statement targetStatement = getStatement(panel, 0, 0);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
    // after external Block
    {
      Statement targetStatement = getStatement(panel, 0);
      StatementTarget target = new StatementTarget(targetStatement, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_beforeStatement() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int foo;",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "    int bar;",
            "    {",
            "      // empty block",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // before Statement that is before "button" Block
    {
      Statement targetStatement = getStatement(panel, 0);
      StatementTarget target = new StatementTarget(targetStatement, true);
      assertFalse(isCreatedAtTarget(button, target));
    }
    // before Statement that is after "button" Block
    {
      Statement targetStatement = getStatement(panel, 2);
      StatementTarget target = new StatementTarget(targetStatement, true);
      assertTrue(isCreatedAtTarget(button, target));
    }
    // before "button" Block itself
    {
      Statement targetStatement = getStatement(panel, 1);
      StatementTarget target = new StatementTarget(targetStatement, true);
      assertFalse(isCreatedAtTarget(button, target));
    }
    // before "empty" Block
    {
      Statement targetStatement = getStatement(panel, 3);
      StatementTarget target = new StatementTarget(targetStatement, true);
      assertTrue(isCreatedAtTarget(button, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_beginOfBlock() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      // before block",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "    {",
            "      // after block",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // begin of "before" Block
    {
      Block targetBlock = (Block) getStatement(panel, 0);
      StatementTarget target = new StatementTarget(targetBlock, true);
      assertFalse(isCreatedAtTarget(button, target));
    }
    // begin of "button" Block itself
    {
      Block targetBlock = (Block) getStatement(panel, 1);
      StatementTarget target = new StatementTarget(targetBlock, true);
      assertFalse(isCreatedAtTarget(button, target));
    }
    // begin of "after" Block
    {
      Block targetBlock = (Block) getStatement(panel, 2);
      StatementTarget target = new StatementTarget(targetBlock, true);
      assertTrue(isCreatedAtTarget(button, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_ifNodeIsConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    LayoutInfo implicitLayout = panel.getLayout();
    // end of constructor Block
    {
      Block targetBlock = getBlock(panel);
      StatementTarget target = new StatementTarget(targetBlock, false);
      assertTrue(isCreatedAtTarget(implicitLayout, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_endOfBlock() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      // before block",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "    {",
            "      // after block",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // end of "before" Block
    {
      Block targetBlock = (Block) getStatement(panel, 0);
      StatementTarget target = new StatementTarget(targetBlock, false);
      assertFalse(isCreatedAtTarget(button, target));
    }
    // end of "button" Block itself
    {
      Block targetBlock = (Block) getStatement(panel, 1);
      StatementTarget target = new StatementTarget(targetBlock, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
    // end of "after" Block
    {
      Block targetBlock = (Block) getStatement(panel, 2);
      StatementTarget target = new StatementTarget(targetBlock, false);
      assertTrue(isCreatedAtTarget(button, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_afterBodyDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JButton button_0 = new JButton();",
            "  private final JButton button_1 = new JButton();",
            "  private final JButton button_2 = new JButton();",
            "  public Test() {",
            "    add(button_0);",
            "    add(button_1);",
            "    add(button_2);",
            "  }",
            "}");
    // "button_0" is visible in any point of Test()
    {
      ComponentInfo button_0 = panel.getChildrenComponents().get(0);
      Statement targetStatement = getStatement(panel);
      StatementTarget target = new StatementTarget(targetStatement, true);
      assertTrue(isCreatedAtTarget(button_0, target));
    }
    // "button_1" is not visible "after button_0"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 0);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, false);
      assertFalse(isCreatedAtTarget(button_1, target));
    }
    // "button_1" is visible "after button_2"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 2);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, false);
      assertTrue(isCreatedAtTarget(button_1, target));
    }
    // "button_1" is visible "after button_1"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 1);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, false);
      assertTrue(isCreatedAtTarget(button_1, target));
    }
    // "button_1" is visible "after Test()"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 3);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, false);
      assertTrue(isCreatedAtTarget(button_1, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_beforeBodyDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JButton button_0 = new JButton();",
            "  private final JButton button_1 = new JButton();",
            "  private final JButton button_2 = new JButton();",
            "  public Test() {",
            "    add(button_0);",
            "    add(button_1);",
            "    add(button_2);",
            "  }",
            "}");
    // "button_1" is not visible "before button_0"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 0);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, true);
      assertFalse(isCreatedAtTarget(button_1, target));
    }
    // "button_1" is not visible "before button_1"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 1);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, true);
      assertFalse(isCreatedAtTarget(button_1, target));
    }
    // "button_1" is visible "before button_2"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 2);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, true);
      assertTrue(isCreatedAtTarget(button_1, target));
    }
    // "button_1" is visible "before Test()"
    {
      ComponentInfo button_1 = panel.getChildrenComponents().get(1);
      BodyDeclaration targetBodyDeclaration = getBodyDeclaration(panel, 3);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetBodyDeclaration, true);
      assertTrue(isCreatedAtTarget(button_1, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_beginOfTypeDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JButton button_0 = new JButton();",
            "  public Test() {",
            "    add(button_0);",
            "  }",
            "}");
    // "button_0" is not visible "at begin of Test"
    {
      ComponentInfo button_0 = panel.getChildrenComponents().get(0);
      TypeDeclaration targetTypeDeclaration = getTypeDeclaration(panel);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetTypeDeclaration, true);
      assertFalse(isCreatedAtTarget(button_0, target));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#isCreatedAtTarget(JavaInfo, NodeTarget)}.
   */
  public void test_isCreatedAtTarget_endOfTypeDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JButton button_0 = new JButton();",
            "  public Test() {",
            "    add(button_0);",
            "  }",
            "}");
    // "button_0" is visible "at end of Test"
    {
      ComponentInfo button_0 = panel.getChildrenComponents().get(0);
      TypeDeclaration targetTypeDeclaration = getTypeDeclaration(panel);
      BodyDeclarationTarget target = new BodyDeclarationTarget(targetTypeDeclaration, false);
      assertTrue(isCreatedAtTarget(button_0, target));
    }
  }

  private static boolean isCreatedAtTarget(JavaInfo javaInfo, StatementTarget statementTarget) {
    NodeTarget target = new NodeTarget(statementTarget);
    return JavaInfoUtils.isCreatedAtTarget(javaInfo, target);
  }

  private static boolean isCreatedAtTarget(JavaInfo javaInfo,
      BodyDeclarationTarget bodyDeclarationTarget) {
    NodeTarget target = new NodeTarget(bodyDeclarationTarget);
    return JavaInfoUtils.isCreatedAtTarget(javaInfo, target);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getStatementTarget_whenAllCreated()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#getStatementTarget_whenAllCreated(List)}.
   */
  public void test_getStatementTarget_whenAllCreated() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button_1 = new JButton();",
            "    add(button_1);",
            "    //",
            "    JButton button_2 = new JButton();",
            "    add(button_2);",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // fail if no components
    try {
      JavaInfoUtils.getStatementTarget_whenAllCreated(ImmutableList.<JavaInfo>of());
      fail();
    } catch (AssertionFailedException e) {
    }
    // ask for "button_1" and "button_2"
    {
      List<ComponentInfo> components = ImmutableList.of(button_1, button_2);
      StatementTarget target = JavaInfoUtils.getStatementTarget_whenAllCreated(components);
      assertTarget(target, null, getStatement(panel, 2), false);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#getStatementTarget_whenAllCreated(List)}.
   */
  public void test_getStatementTarget_whenAllCreated_fieldInitializer_this() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  JButton button_1 = new JButton();",
            "  JButton button_2 = new JButton();",
            "  public Test() {",
            "    super();",
            "    add(button_1);",
            "    add(button_2);",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // ask for "button_1" and "button_2"
    {
      List<ComponentInfo> components = ImmutableList.of(button_1, button_2);
      StatementTarget target = JavaInfoUtils.getStatementTarget_whenAllCreated(components);
      assertTarget(target, null, getStatement(panel, 0), false);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#getStatementTarget_whenAllCreated(List)}.
   */
  public void test_getStatementTarget_whenAllCreated_fieldInitializer_main() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  static JButton button_1 = new JButton();",
            "  static JButton button_2 = new JButton();",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "    panel.add(button_1);",
            "    panel.add(button_2);",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // ask for "button_1" and "button_2"
    {
      List<ComponentInfo> components = ImmutableList.of(button_1, button_2);
      StatementTarget target = JavaInfoUtils.getStatementTarget_whenAllCreated(components);
      assertTarget(target, getBlock(panel), null, true);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#getStatementTarget_whenAllCreated(List)}.
   */
  public void test_getStatementTarget_whenAllCreated_lazy() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton m_button_2;",
            "  public Test() {",
            "    JButton button_1 = new JButton();",
            "    add(button_1);",
            "    add(getButton_2());",
            "  }",
            "  private JButton getButton_2() {",
            "    if (m_button_2 == null) {",
            "      m_button_2 = new JButton();",
            "    }",
            "    return m_button_2;",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // ask for "button_1" and "button_2"
    {
      List<ComponentInfo> components = ImmutableList.of(button_1, button_2);
      StatementTarget target = JavaInfoUtils.getStatementTarget_whenAllCreated(components);
      assertTarget(target, null, getStatement(button_2, 0), false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // NodeTarget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#getNodeTarget_relativeCreation(JavaInfo, boolean)}.
   */
  public void test_getNodeTarget_relativeCreation_relatedStatement() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // before
    {
      NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_beforeCreation(button);
      StatementTarget statementTarget = nodeTarget.getStatementTarget();
      assertNotNull(statementTarget);
      Statement expectedTargetStatement = getStatement(panel, 0);
      assertTarget(statementTarget, null, expectedTargetStatement, true);
    }
    // after
    {
      NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(button);
      StatementTarget statementTarget = nodeTarget.getStatementTarget();
      assertNotNull(statementTarget);
      Statement expectedTargetStatement = getStatement(panel, 0);
      assertTarget(statementTarget, null, expectedTargetStatement, false);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#getNodeTarget_relativeCreation(JavaInfo, boolean)}.
   */
  public void test_getNodeTarget_relativeCreation_relativeFieldDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JButton button = new JButton();",
            "  public Test() {",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // before
    {
      NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_beforeCreation(button);
      BodyDeclarationTarget bodyDeclarationTarget = nodeTarget.getBodyDeclarationTarget();
      assertNotNull(bodyDeclarationTarget);
      assertSame(getBodyDeclaration(panel, 0), bodyDeclarationTarget.getDeclaration());
      assertTrue(bodyDeclarationTarget.isBefore());
    }
    // after
    {
      NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(button);
      BodyDeclarationTarget bodyDeclarationTarget = nodeTarget.getBodyDeclarationTarget();
      assertNotNull(bodyDeclarationTarget);
      assertSame(getBodyDeclaration(panel, 0), bodyDeclarationTarget.getDeclaration());
      assertFalse(bodyDeclarationTarget.isBefore());
    }
  }

  public void test_getNodeTarget_relativeCreation_wrapperVariableExists() throws Exception {
    WrapperInfoTest.configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "    JButton button = wrapper.getControl();",
            "  }",
            "}");
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(wrappedComponent);
    assertEquals("after JButton button=wrapper.getControl();", nodeTarget.toString().trim());
  }

  public void test_getNodeTarget_relativeCreation_wrapperVariableNoExists() throws Exception {
    WrapperInfoTest.configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "  }",
            "}");
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(wrappedComponent);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestWrapper wrapper = new TestWrapper(this);",
        "    JButton button = wrapper.getControl();",
        "  }",
        "}");
    assertEquals("after JButton button=wrapper.getControl();", nodeTarget.toString().trim());
  }

  public void test_getNodeTarget_relativeCreation_wrapperVariableExistsAsField() throws Exception {
    WrapperInfoTest.configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  private TestWrapper wrapper;",
            "  private JButton button;",
            "  public Test() {",
            "    wrapper = new TestWrapper(this);",
            "    button = wrapper.getControl();",
            "  }",
            "}");
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(wrappedComponent);
    assertEquals("after button=wrapper.getControl();", nodeTarget.toString().trim());
  }

  public void test_getNodeTarget_relativeCreation_wrapperVariableNoExistsAsField() throws Exception {
    WrapperInfoTest.configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  private TestWrapper wrapper;",
            "  public Test() {",
            "    wrapper = new TestWrapper(this);",
            "  }",
            "}");
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(wrappedComponent);
    assertEditor(
        "public class Test extends JPanel {",
        "  private TestWrapper wrapper;",
        "  public Test() {",
        "    wrapper = new TestWrapper(this);",
        "    JButton button = wrapper.getControl();",
        "  }",
        "}");
    assertEquals("after JButton button=wrapper.getControl();", nodeTarget.toString().trim());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addChildExposedByMethod()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#addChildExposedByMethod(JavaInfo, String)}.<br>
   * Test also for exposing {@link InstanceFactoryInfo}.
   */
  public void test_addChildExposedByMethod_exposedInstanceFactory() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  private MyFactory m_myFactory = new MyFactory();",
            "  public MyFactory getFactory() {",
            "    return m_myFactory;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyContainer());",
            "  }",
            "}");
    panel.refresh();
    // initial hierarchy
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyContainer())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyContainer} {empty} {/add(new MyContainer())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    ContainerInfo myContainer = (ContainerInfo) panel.getChildrenComponents().get(0);
    // expose "getFactory()"
    InstanceFactoryInfo exposedFactory =
        (InstanceFactoryInfo) JavaInfoUtils.addChildExposedByMethod(myContainer, "getFactory");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyContainer())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyContainer} {empty} {/add(new MyContainer())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {method: public test.MyFactory test.MyContainer.getFactory()} {property} {}");
    // send broadcast to move "getFactory()" into InstanceFactoryContainerInfo
    InstanceFactoryRootProcessor.INSTANCE.process(panel, ImmutableList.<JavaInfo>of(exposedFactory));
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyContainer())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyContainer} {empty} {/add(new MyContainer())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {method: public test.MyFactory test.MyContainer.getFactory()} {property} {}",
        "  {instance factory container}",
        "    {method: public test.MyFactory test.MyContainer.getFactory()} {property} {}");
    {
      InstanceFactoryContainerInfo container = InstanceFactoryContainerInfo.get(panel);
      List<InstanceFactoryInfo> factories = container.getChildrenFactory();
      assertThat(factories).hasSize(1).contains(exposedFactory);
    }
  }

  /**
   * Test for {@link JavaInfoUtils#addExposedChildren(JavaInfo, Class[])}.
   * <p>
   * We should ignore if getter throws exception.
   */
  public void test_addChildredExposedByMethods_exception() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JButton getFoo() {",
            "    throw new Error();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test for {@link JavaInfoUtils#addExposedChildren(JavaInfo, Class[])}.
   * <p>
   * If component has getter that returns itself, we should ignore it.
   */
  public void test_addChildredExposedByMethods_recursion() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel getFoo() {",
            "    return this;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Even if getter {@link Method} is public, we can invoke it only if declaring class is also
   * public.
   */
  public void test_addChildredExposedByMethods_publicMethod_privateClass() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "class MyPanel extends JPanel {",
            "  private JButton button = new JButton();",
            "  public MyPanel() {",
            "    add(button);",
            "  }",
            "  public JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyPanel());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyPanel())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {empty} {/add(new MyPanel())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Protected {@link Method} is visible not only in subclass, but also in same package.
   */
  public void test_addChildredExposedByMethods_protectedMethod_visibleInSamePackage()
      throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton button = new JButton();",
            "  public MyPanel() {",
            "    add(button);",
            "  }",
            "  protected JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyPanel());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyPanel())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {empty} {/add(new MyPanel())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {method: protected javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isIndirectlyExposed()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#isIndirectlyExposed(JavaInfo)}.
   */
  public void test_isIndirectlyExposed_notExposed() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // validate
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertFalse(JavaInfoUtils.isIndirectlyExposed(button));
  }

  /**
   * Test for {@link JavaInfoUtils#isIndirectlyExposed(JavaInfo)}.
   */
  public void test_isIndirectlyExposed_exposedDirectly() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private final JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // validate
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertFalse(JavaInfoUtils.isIndirectlyExposed(button));
  }

  /**
   * Test for {@link JavaInfoUtils#isIndirectlyExposed(JavaInfo)}.
   */
  public void test_isIndirectlyExposed_exposedIndirectly() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private final JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    JPanel inner = new JPanel();",
            "    add(inner);",
            "    inner.add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // validate
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertTrue(JavaInfoUtils.isIndirectlyExposed(button));
  }

  /**
   * Test for {@link JavaInfoUtils#isIndirectlyExposed(JavaInfo)}.
   */
  public void test_isIndirectlyExposed_exposedDirectly_Wrapper() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // validate
    JavaInfo wrappedComponent = createWrappedComponentMock(true);
    assertFalse(JavaInfoUtils.isIndirectlyExposed(wrappedComponent));
  }

  /**
   * Test for {@link JavaInfoUtils#isIndirectlyExposed(JavaInfo)}.
   */
  public void test_isIndirectlyExposed_exposedUndirectly_Wrapper() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // validate
    JavaInfo wrappedComponent = createWrappedComponentMock(false);
    assertTrue(JavaInfoUtils.isIndirectlyExposed(wrappedComponent));
  }

  private JavaInfo createWrappedComponentMock(final boolean isDirect) throws Exception {
    // exposed wrapper
    final JavaInfo exposedWrapper;
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(CreationSupport.class);
      enhancer.setInterfaces(new Class<?>[]{IExposedCreationSupport.class});
      enhancer.setCallback(new MethodInterceptor() {
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {
          String signature = ReflectionUtils.getMethodSignature(method);
          if (signature.equals("isDirect()")) {
            return isDirect;
          }
          return null;
        }
      });
      CreationSupport exposedCreation = (CreationSupport) enhancer.create();
      exposedWrapper = JavaInfoUtils.createJavaInfo(m_lastEditor, JButton.class, exposedCreation);
    }
    // wrapped component
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(CreationSupport.class);
      enhancer.setInterfaces(new Class<?>[]{IWrapperControlCreationSupport.class});
      enhancer.setCallback(new MethodInterceptor() {
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {
          String signature = ReflectionUtils.getMethodSignature(method);
          if (signature.equals("getWrapperInfo()")) {
            return exposedWrapper;
          }
          return null;
        }
      });
      CreationSupport wrappedCreation = (CreationSupport) enhancer.create();
      return JavaInfoUtils.createJavaInfo(m_lastEditor, JButton.class, wrappedCreation);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoUtils#canMove(JavaInfo)}.
   * <p>
   * {@link FlowLayout} is ordered layout manager, so it allows move only if reordering is allowed.
   */
  public void test_canMove_FlowLayout() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check permissions
    assertFalse(button.getCreationSupport().canReorder());
    assertFalse(button.getCreationSupport().canReparent());
    assertFalse(JavaInfoUtils.canMove(button));
    assertFalse(JavaInfoUtils.canReparent(button));
  }

  /**
   * Test for {@link JavaInfoUtils#canMove(JavaInfo)}.
   * <p>
   * Force move enablement.
   */
  public void test_canMove_forceMoveEnable() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    // set listener to enable "move"
    button.addBroadcastListener(new JavaEventListener() {
      @Override
      public void canMove(JavaInfo javaInfo, boolean[] forceMoveEnable, boolean[] forceMoveDisable)
          throws Exception {
        if (javaInfo == button) {
          forceMoveEnable[0] = true;
        }
      }
    });
    // check permissions
    assertFalse(button.getCreationSupport().canReorder());
    assertFalse(button.getCreationSupport().canReparent());
    assertTrue(JavaInfoUtils.canMove(button));
    assertFalse(JavaInfoUtils.canReparent(button));
  }

  /**
   * Test for {@link JavaInfoUtils#canMove(JavaInfo)}.
   * <p>
   * Force move disabled.
   */
  public void test_canMove_forceMoveDisable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    // check permissions: no listener, so default permissions
    {
      assertTrue(button.getCreationSupport().canReorder());
      assertTrue(button.getCreationSupport().canReparent());
      assertTrue(JavaInfoUtils.canMove(button));
      assertTrue(JavaInfoUtils.canReparent(button));
    }
    // set listener to disabled "move"
    button.addBroadcastListener(new JavaEventListener() {
      @Override
      public void canMove(JavaInfo javaInfo, boolean[] forceMoveEnable, boolean[] forceMoveDisable)
          throws Exception {
        if (javaInfo == button) {
          forceMoveDisable[0] = true;
        }
      }
    });
    // check permissions: our listener disables move
    {
      assertTrue(button.getCreationSupport().canReorder());
      assertTrue(button.getCreationSupport().canReparent());
      assertFalse(JavaInfoUtils.canMove(button));
      assertTrue(JavaInfoUtils.canReparent(button));
    }
  }

  /**
   * Test for {@link JavaInfoUtils#canReparent(JavaInfo)}.
   */
  public void test_canReparent_disabledByAssociation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(Component component, boolean value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Component' child='true'/>",
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
            "    JButton button = new JButton();",
            "    MyPanel myPanel = new MyPanel(button, true);",
            "    add(myPanel);",
            "  }",
            "}");
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = myPanel.getChildrenComponents().get(0);
    // check permissions: no listener, so default permissions
    {
      assertTrue(button.getCreationSupport().canReorder());
      assertTrue(button.getCreationSupport().canReparent());
      assertFalse(button.getAssociation().canDelete());
      assertTrue(JavaInfoUtils.canMove(button));
      assertFalse(JavaInfoUtils.canReparent(button));
    }
  }
}
