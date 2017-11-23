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
package org.eclipse.wb.tests.designer.core.model.parser;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.SuperConstructorArgumentAssociation;
import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.model.creation.CastedSuperInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.Dialog;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.beans.Beans;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * For "real" GUI compilation units (this includes for example Swing and SWT, but not RCP
 * perspective factory) we should really interpret code on execution flow. We need live instances to
 * get default property values and identify property based children. Also, we can not use default
 * constructors, because we should create components <b>exactly</b> as they are created in source
 * code.
 * 
 * @author scheglov_ke
 */
public class ExecuteOnParseTest extends SwingModelTest {
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
  // Byte-code execution flow
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If "super" class invokes in constructor some method, this method should be added into execution
   * flow.
   */
  public void test_byteCodeExecutionFlow_1() throws Exception {
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
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  protected void createContents() {",
            "    super.createContents();",
            "    add(new JButton());",
            "  }",
            "}");
    panel.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
  }

  /**
   * Same as {@link #test_byteCodeExecutionFlow()}, but additional component in constructor.
   */
  public void test_byteCodeExecutionFlow_2() throws Exception {
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
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public Test() {",
            "    add(new JLabel());",
            "  }",
            "  protected void createContents() {",
            "    super.createContents();",
            "    add(new JButton());",
            "  }",
            "}");
    panel.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton())/ /add(new JLabel())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}",
        "  {new: javax.swing.JLabel} {empty} {/add(new JLabel())/}");
  }

  /**
   * Same as {@link #test_byteCodeExecutionFlow()}, but with {@link SuperConstructorInvocation} with
   * parameters.
   */
  public void test_byteCodeExecutionFlow_3() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel(boolean enabled) {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends BasePanel {",
        "  public Test() {",
        "    super(true);",
        "  }",
        "  protected void createContents() {",
        "    super.createContents();",
        "    add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
  }

  /**
   * Same as {@link #test_byteCodeExecutionFlow()}, but
   * <em>does NOT<em> visit <code>createContents</code>, because
   * there are jumps in source method (super constructor). No matter, if these jumps don't affect
   * <code>createContents</code> invocation, we can not know this without decompilation.
   * <p>
   * And now good news. :-)<br>
   * All what was written above is not true anymore, 20080308.<br>
   * We support binary execution flow, so if superclass invokes some method, we will intercept this.
   */
  public void test_byteCodeExecutionFlow_4() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel(boolean enabled) {",
            "    if (enabled) {",
            "      setEnabled(true);",
            "    }",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends BasePanel {",
        "  public Test() {",
        "    super(true);",
        "  }",
        "  protected void createContents() {",
        "    super.createContents();",
        "    add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
  }

  /**
   * Test that method "createClient()" is visited and creates {@link JButton}, that is bound to
   * "this".
   */
  public void test_byteCodeExecutionFlow_5() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    add(createClient());",
            "  }",
            "  protected abstract Component createClient();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  protected Component createClient() {",
            "    JButton clientButton = new JButton();",
            "    return clientButton;",
            "  }",
            "}");
    // "panel" should have one child component: JButton "clientButton", created in createClient()
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertThat(components).hasSize(1);
    {
      ComponentInfo button = components.get(0);
      assertInstanceOf(ConstructorCreationSupport.class, button.getCreationSupport());
      assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
      assertEquals("clientButton", button.getVariableSupport().getName());
      assertInstanceOf(UnknownAssociation.class, button.getAssociation());
    }
    // only one MethodDeclaration is in binary execution flow "before constructor"
    {
      ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
      MethodDeclaration constructor = (MethodDeclaration) panel.getCreationSupport().getNode();
      MethodDeclaration createClientMethod =
          AstNodeUtils.getMethodBySignature(
              (TypeDeclaration) constructor.getParent(),
              "createClient()");
      // ...before refresh()
      {
        List<MethodDeclaration> methodsAfter =
            flowDescription.getBinaryFlowMethodsAfter(constructor.getBody());
        assertThat(methodsAfter).containsOnly(createClientMethod);
      }
      // ...and after refresh()
      panel.refresh();
      {
        List<MethodDeclaration> methodsAfter =
            flowDescription.getBinaryFlowMethodsAfter(constructor.getBody());
        assertThat(methodsAfter).containsOnly(createClientMethod);
      }
    }
  }

  /**
   * Test that method "createClient()" is visited, creates {@link JButton} and "clientButton" is
   * placed before "constructorButton".
   */
  public void test_byteCodeExecutionFlow_6() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    add(createClient());",
            "  }",
            "  protected abstract Component createClient();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    {",
            "      JButton constructorButton = new JButton();",
            "      add(constructorButton);",
            "    }",
            "  }",
            "  protected Component createClient() {",
            "    JButton clientButton = new JButton();",
            "    return clientButton;",
            "  }",
            "}");
    // "panel" should have two child components:
    //    1. JButton "clientButton", created in createClient()
    //    2. JButton "constructorButton", created in Test()
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertThat(components).hasSize(2);
    {
      ComponentInfo button = components.get(0);
      assertInstanceOf(ConstructorCreationSupport.class, button.getCreationSupport());
      assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
      assertEquals("clientButton", button.getVariableSupport().getName());
      assertInstanceOf(UnknownAssociation.class, button.getAssociation());
    }
    {
      ComponentInfo button = components.get(1);
      assertInstanceOf(ConstructorCreationSupport.class, button.getCreationSupport());
      assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
      assertEquals("constructorButton", button.getVariableSupport().getName());
      assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
    }
  }

  /**
   * Test that we don't visit fields more than one time.
   */
  public void test_byteCodeExecutionFlow_7() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    add(createClient());",
            "  }",
            "  protected abstract Component createClient();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  private final JButton fieldButton = new JButton();",
            "  public Test() {",
            "    add(fieldButton);",
            "  }",
            "  protected Component createClient() {",
            "    JButton clientButton = new JButton();",
            "    return clientButton;",
            "  }",
            "}");
    // "panel" should have two child components:
    //    1. JButton "clientButton", created in createClient()
    //    2. JButton "fieldButton", created in field
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertThat(components).hasSize(2);
    {
      ComponentInfo button = components.get(0);
      assertInstanceOf(ConstructorCreationSupport.class, button.getCreationSupport());
      assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
      assertEquals("clientButton", button.getVariableSupport().getName());
      assertInstanceOf(UnknownAssociation.class, button.getAssociation());
    }
    {
      ComponentInfo button = components.get(1);
      assertInstanceOf(ConstructorCreationSupport.class, button.getCreationSupport());
      assertInstanceOf(FieldInitializerVariableSupport.class, button.getVariableSupport());
      assertEquals("fieldButton", button.getVariableSupport().getName());
      assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
    }
  }

  /**
   * Test that we can invoke {@link SuperMethodInvocation} as <em>first</em> statement.
   */
  public void test_byteCodeExecutionFlow_8() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    fillPanel();",
            "  }",
            "  protected void fillPanel() {",
            "    add(new JLabel('super JLabel'));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  protected void fillPanel() {",
            "    super.fillPanel();",
            "    {",
            "      JButton button = new JButton('local JButton');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // "panel" as JavaInfo has only one ComponentInfo
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      assertThat(components).hasSize(1);
      assertEquals("button", components.get(0).getVariableSupport().getName());
    }
    // "panel" as Container has two Component's:
    //    1. JLabel, created in super.fillPanel()
    //    2. JButton, created in local fillPanel()
    {
      Component[] components = panel.getContainer().getComponents();
      assertThat(components).hasSize(2);
      {
        JLabel superJLabel = (JLabel) components[0];
        assertEquals("super JLabel", superJLabel.getText());
      }
      {
        JButton localJButton = (JButton) components[1];
        assertEquals("local JButton", localJButton.getText());
      }
    }
  }

  /**
   * Test that we can invoke {@link SuperMethodInvocation} as <em>last</em> statement.
   */
  public void test_byteCodeExecutionFlow_9() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    fillPanel();",
            "  }",
            "  protected void fillPanel() {",
            "    add(new JLabel('super JLabel'));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  protected void fillPanel() {",
            "    {",
            "      JButton button = new JButton('local JButton');",
            "      add(button);",
            "    }",
            "    super.fillPanel();",
            "  }",
            "}");
    panel.refresh();
    // "panel" as JavaInfo has only one ComponentInfo
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      assertThat(components).hasSize(1);
      assertEquals("button", components.get(0).getVariableSupport().getName());
    }
    // "panel" as Container has two Component's:
    //    1. JButton, created in local fillPanel()
    //    2. JLabel, created in super.fillPanel()
    {
      Component[] components = panel.getContainer().getComponents();
      assertThat(components).hasSize(2);
      {
        JButton localJButton = (JButton) components[0];
        assertEquals("local JButton", localJButton.getText());
      }
      {
        JLabel superJLabel = (JLabel) components[1];
        assertEquals("super JLabel", superJLabel.getText());
      }
    }
  }

  /**
   * {@link JButton} created in "createClient()" is added into exposed {@link Container}.
   */
  public void test_byteCodeExecutionFlow_10() throws Exception {
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

  /**
   * The JFace {@link Dialog} like panel - it has {@link MethodDeclaration} with parameter for which
   * we should create {@link JavaInfo}.
   */
  public void test_byteCodeExecutionFlow_11() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    setLayout(new BorderLayout());",
            "    JPanel contentArea = new JPanel();",
            "    add(contentArea);",
            "    createDialogArea(contentArea);",
            "  }",
            "  protected abstract void createDialogArea(Container parent);",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "    JButton button = new JButton();",
            "    parent.add(button);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: test.MyDialog} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {parameter} {parent} {/parent.add(button)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /parent.add(button)/}");
    // prepare "parent" ContainerInfo
    ContainerInfo parent;
    {
      parent = (ContainerInfo) panel.getChildrenComponents().get(0);
      assertInstanceOf(UnknownAssociation.class, parent.getAssociation());
      assertInstanceOf(MethodParameterCreationSupport.class, parent.getCreationSupport());
      assertInstanceOf(MethodParameterVariableSupport.class, parent.getVariableSupport());
    }
    // check that we set Object for "parent" parameter during refresh()
    panel.refresh();
    assertNotNull(parent.getObject());
    // check target for placing on "parent" in createDialogArea()
    {
      MethodDeclaration createDialogAreaMethod;
      {
        TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
        createDialogAreaMethod =
            AstNodeUtils.getMethodBySignature(
                typeDeclaration,
                "createDialogArea(java.awt.Container)");
      }
      StatementTarget target = JavaInfoUtils.getTarget(parent, null);
      Statement expectedStatement = getStatement(createDialogAreaMethod.getBody(), 1);
      assertTarget(target, null, expectedStatement, false);
    }
  }

  /**
   * The JFace {@link Dialog} like panel, more complex, with several methods.<br>
   * In addition to several methods, configureShell() parameter is alias for "this", so we check
   * that it is correctly bound as child of "this".
   */
  public void test_byteCodeExecutionFlow_12() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    setLayout(new BorderLayout());",
            "    configureShell(this);",
            "    {",
            "      JPanel contentArea = new JPanel();",
            "      add(contentArea);",
            "      createDialogArea(contentArea);",
            "    }",
            "    {",
            "      JPanel buttonBar = new JPanel();",
            "      add(buttonBar, BorderLayout.SOUTH);",
            "      createButtonsForButtonBar(buttonBar);",
            "    }",
            "  }",
            "  protected void configureShell(Container shell) {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "  }",
            "  protected void createButtonsForButtonBar(Container parent) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends MyDialog {",
        "  public Test() {",
        "  }",
        "  protected void configureShell(Container shell) {",
        "    shell.setEnabled(true);",
        "  }",
        "  protected void createDialogArea(Container parent) {",
        "    JButton button_1 = new JButton();",
        "    parent.add(button_1);",
        "  }",
        "  protected void createButtonsForButtonBar(Container parent) {",
        "    JButton button_2 = new JButton();",
        "    parent.add(button_2);",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyDialog} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {parameter} {shell} {/shell.setEnabled(true)/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {parameter} {parent} {/parent.add(button_1)/}",
        "      {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "      {new: javax.swing.JButton} {local-unique: button_1} {/new JButton()/ /parent.add(button_1)/}",
        "    {parameter} {parent} {/parent.add(button_2)/}",
        "      {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "      {new: javax.swing.JButton} {local-unique: button_2} {/new JButton()/ /parent.add(button_2)/}");
  }

  /**
   * The JFace {@link Dialog} like panel, using casted creation support.
   */
  public void test_byteCodeExecutionFlow_13() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    setLayout(new BorderLayout());",
            "    //",
            "    JPanel contentArea = new JPanel();",
            "    add(contentArea);",
            "    //",
            "    Component dialogArea = createDialogArea(contentArea);",
            "  }",
            "  protected Component createDialogArea(Container parent) {",
            "    JPanel dialogArea = new JPanel();",
            "    parent.add(dialogArea);",
            "    return dialogArea;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected Component createDialogArea(Container parent) {",
            "    Container container = (Container) super.createDialogArea(parent);",
            "    JButton button = new JButton();",
            "    container.add(button);",
            "    return button;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: test.MyDialog} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {parameter} {parent} {/super.createDialogArea(parent)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {casted-superInvocation: (Container)super.createDialogArea(parent)} {local-unique: container} {/(Container) super.createDialogArea(parent)/ /container.add(button)/}",
        "      {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "      {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /container.add(button)/ /button/}");
    // check "container"
    {
      ContainerInfo createDialogAreaParent = (ContainerInfo) panel.getChildrenComponents().get(0);
      ContainerInfo container =
          (ContainerInfo) createDialogAreaParent.getChildrenComponents().get(0);
      // check CastedSuperInvocationCreationSupport
      CastedSuperInvocationCreationSupport creationSupport =
          (CastedSuperInvocationCreationSupport) container.getCreationSupport();
      assertEquals(
          "(Container) super.createDialogArea(parent)",
          m_lastEditor.getSource(creationSupport.getNode()));
      assertFalse(creationSupport.canDelete());
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // refresh() also should be OK
    assert_creation(panel);
  }

  /**
   * Overridden method that accepts "primitive" parameter, so does not have {@link JavaInfoUtils}.<br>
   * No any exception should happen.
   */
  public void test_byteCodeExecutionFlow_14() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    int sum = getSum(1, 2);",
            "    if (sum != 3) {",
            "      throw new IllegalStateException('3 expected, but ' + sum + ' found.');",
            "    }",
            "  }",
            "  protected abstract int getSum(int a, int b);",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected int getSum(int a, int b) {",
            "    return a + b;",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Invoke <b>two<b> methods from super constructor. There was bug, that only first method
   * associated with <code>super</code>.
   */
  public void test_byteCodeExecutionFlow_15() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    method_1();",
            "    method_2();",
            "  }",
            "  protected void method_1() {",
            "  }",
            "  protected void method_2() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public Test() {",
            "    super();",
            "  }",
            "  protected void method_1() {",
            "    setEnabled(true);",
            "  }",
            "  protected void method_2() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // check execution flow
    {
      ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
      // prepare methods
      MethodDeclaration constructor;
      MethodDeclaration method_1;
      MethodDeclaration method_2;
      {
        TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
        constructor = AstNodeUtils.getMethodBySignature(typeDeclaration, "<init>()");
        method_1 = AstNodeUtils.getMethodBySignature(typeDeclaration, "method_1()");
        method_2 = AstNodeUtils.getMethodBySignature(typeDeclaration, "method_2()");
      }
      // trace should be empty
      assertThat(flowDescription.getTraceStatements()).isEmpty();
      // "method_1" and "method_2" after constructor
      List<MethodDeclaration> flowMethods =
          flowDescription.getBinaryFlowMethodsAfter(constructor.getBody());
      assertThat(flowMethods).hasSize(2);
      assertSame(method_1, flowMethods.get(0));
      assertSame(method_2, flowMethods.get(1));
    }
    // refresh
    panel.refresh();
    // trace should be still empty
    {
      ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
      assertThat(flowDescription.getTraceStatements()).isEmpty();
    }
  }

  /**
   * Invoke <b>two<b> methods from super constructor. First method initializes field. Second method
   * uses it.
   */
  public void test_byteCodeExecutionFlow_16() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    method_1();",
            "    method_2();",
            "  }",
            "  protected void method_1() {",
            "  }",
            "  protected void method_2() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  private JButton m_button;",
            "  public Test() {",
            "    super();",
            "  }",
            "  protected void method_1() {",
            "    m_button = new JButton();",
            "  }",
            "  protected void method_2() {",
            "    add(m_button);",
            "  }",
            "}");
    panel.refresh();
    // check "m_button"
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      NodeTarget nodeTarget = getNodeStatementTarget(panel, "method_2()", true, 0);
      assertEquals("m_button", button.getVariableSupport().getName());
      assertEquals("m_button", button.getVariableSupport().getReferenceExpression(nodeTarget));
    }
  }

  /**
   * Method <code>configureContentArea</code> parameter should be ignored.<br>
   * No any special meaning for dialogs, but we just want to test "disabling" feature. :-)
   */
  public void test_byteCodeExecutionFlow_17() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JPanel contentArea = new JPanel();",
            "      add(contentArea);",
            "      configureContentArea(contentArea);",
            "    }",
            "  }",
            "  protected void configureContentArea(Container contentArea) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyDialog.configureContentArea_java.awt.Container_.0.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='thisCreation.ignoreBind'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends MyDialog {",
        "  public Test() {",
        "  }",
        "  protected void configureContentArea(Container contentArea) {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyDialog} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  /**
   * Test for case when there are two possible "next" components during binary binding (binary
   * because <code>add2()</code> is marked as executable, but component is not marked as child).
   */
  public void test_byteCodeExecutionFlow_18() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public void add2(Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='add2'>",
            "      <parameter type='java.awt.Component'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse, JButton can be added before JCheckBox and JRadioButton, but correct - only before JCheckBox
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    add2(new JButton());",
        "    add(new JCheckBox());",
        "    add(new JRadioButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/add2(new JButton())/ /add(new JCheckBox())/ /add(new JRadioButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add2(new JButton())/}",
        "  {new: javax.swing.JCheckBox} {empty} {/add(new JCheckBox())/}",
        "  {new: javax.swing.JRadioButton} {empty} {/add(new JRadioButton())/}");
  }

  /**
   * If we attempt to visit single {@link MethodDeclaration} several times, do this only one time.
   * <p>
   * Here first time we visit from binary execution flow, and second time from Test() constructor.
   */
  public void test_byteCodeExecutionFlow_duplicateMethodInvocation() throws Exception {
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
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public Test() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton())/ /createContents()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We should not visit {@link MethodDeclaration} several times from binary execution flow.
   * Especially when there are parameter for which we create {@link JavaInfo}.
   */
  public void test_byteCodeExecutionFlow_duplicateMethodInvocation2() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    createButton(this);",
            "    createButton(this);",
            "  }",
            "  protected void createButton(Container parent) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public Test() {",
            "  }",
            "  protected void createButton(Container parent) {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {parameter} {parent} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We can visit {@link MethodDeclaration} multiple times if it has no parameters and has just
   * <code>return (simpleName);</code>.
   */
  public void test_byteCodeExecutionFlow_duplicateMethodInvocation3() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    if (getObj() == null) {",
            "      throw new IllegalStateException();",
            "    }",
            "    if (getObj() == null) {",
            "      throw new IllegalStateException();",
            "    }",
            "  }",
            "  protected Object getObj() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  private Object m_object = new Object();",
            "  public Test() {",
            "  }",
            "  protected Object getObj() {",
            "    return (m_object);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We can visit {@link MethodDeclaration} multiple times if it is "lazy" creation.
   */
  public void test_byteCodeExecutionFlow_duplicateMethodInvocation_lazy() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    if (getButton() == null) {",
            "      throw new IllegalStateException();",
            "    }",
            "    if (getButton() == null) {",
            "      throw new IllegalStateException();",
            "    }",
            "  }",
            "  protected JButton getButton() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  private Object m_object = new Object();",
            "  public Test() {",
            "  }",
            "  private JButton button;",
            "  protected JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "    }",
            "    return button;",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Attempt to execute infinite recursion. Ignore it.
   */
  public void test_byteCodeExecutionFlow_infiniteRecursion() throws Exception {
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
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public Test() {",
            "  }",
            "  protected void createContents() {",
            "    add(new JButton());",
            "    createContents();",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton())/ /createContents()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We should be able to intercept methods when they are called as result of
   * {@link SuperMethodInvocation}.
   */
  public void test_byteCodeExecutionFlow_callSuper_andIntercept() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    JButton button = createButton();",
            "    button.setEnabled(true); // should not cause NPE",
            "  }",
            "  protected JButton createButton() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  protected void createContents() {",
            "    super.createContents();",
            "  }",
            "  protected JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}");
    assertNoErrors(panel);
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * {@link Method#isBridge()} may cause double "super" invocation, so we should not clear "super"
   * flag.
   */
  public void test_byteCodeExecutionFlow_callSuper_whenBridgeMethod() throws Exception {
    setFileContentSrc(
        "test/PanelA.java",
        getTestSource(
            "public class PanelA extends JPanel {",
            "  protected JComponent createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/PanelB.java",
        getTestSource(
            "public class PanelB extends PanelA {",
            "  protected JButton createButton() {",
            "    return (JButton) super.createButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends PanelB {",
        "  public Test() {",
        "    JButton button = createButton();",
        "    add(button);",
        "  }",
        "  protected JButton createButton() {",
        "    return super.createButton();",
        "  }",
        "}");
    refresh();
    // check that "button" has object
    JavaInfo button = getJavaInfoByName("button");
    assertNotNull(button.getObject());
  }

  /**
   * Just test with potential execution flow, that is not really used.
   */
  public void test_exposedComponents_binaryExecutionFlow_noOverride() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  private JPanel m_inner;",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    m_inner = new JPanel();",
            "    add(m_inner);",
            "  }",
            "  public JPanel getInner() {",
            "    return m_inner;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends BasePanel {",
            "  // no createContents()",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // check hierarchy
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JPanel test.BasePanel.getInner()} {property} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test for case when layout is changed in "super" implementation, i.e. this layout is implicit.
   * However we set object when first time enter into "createContents()" method, i.e. before
   * "super.createContents()". So, it seems that we should try to replace implicit layout after
   * "super".
   */
  public void test_binaryExecutionFlow_setLayoutInSuper() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends BasePanel {",
        "  protected void createContents() {",
        "    super.createContents();",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  /**
   * Sometimes GUI of super class is created in one of the intercepted methods, even before we
   * return from "super" creation, so before we can set "object" for "this". However this means that
   * exposed components will not work in intercepted methods. We should fix this.
   */
  public void test_exposedComponents_binaryExecutionFlow_withOverride2() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  private JPanel m_inner;",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    m_inner = new JPanel();",
            "    add(m_inner);",
            "  }",
            "  public JPanel getInner() {",
            "    return m_inner;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  protected void createContents() {",
            "    super.createContents();",
            "    getInner().add(new JButton());",
            "  }",
            "}");
    assert_creation(panel);
    assertNoErrors(panel);
    // check hierarchy
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JPanel test.BasePanel.getInner()} {property} {/getInner().add(new JButton())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/getInner().add(new JButton())/}");
  }

  /**
   * Sometimes GUI of super class is created in one of the intercepted methods, even before we
   * return from "super" creation, so before we can set "object" for "this". However this means that
   * exposed components will not work in intercepted methods. We should fix this.
   */
  public void test_exposedComponents_binaryExecutionFlow_decideCreationByOverrideResult()
      throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  private JPanel m_inner;",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    if (shouldCreateInner()) {",
            "      m_inner = new JPanel();",
            "      add(m_inner);",
            "    }",
            "  }",
            "  public JPanel getInner() {",
            "    return m_inner;",
            "  }",
            "  public boolean shouldCreateInner() {",
            "    return true;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public boolean shouldCreateInner() {",
            "    return true;",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JPanel test.BasePanel.getInner()} {property} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Sometimes we want to prevent visiting method from binary execution flow.
   * <p>
   * Here we test for <code>binaryExecutionFlow.dontVisit</code> tag support.
   */
  public void test_exposedComponents_binaryExecutionFlow_dontVisit() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  private JPanel m_inner;",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/BasePanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='createContents'>",
            "      <tag name='binaryExecutionFlow.dontVisit' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends BasePanel {",
        "  protected void createContents() {",
        "    add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Sometimes we want to prevent interception for all standard methods in toolkit, i.e. in some
   * package.
   */
  public void test_binaryExecutionFlow_disableForPackage_withException() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  private JPanel m_inner;",
            "  public BasePanel() {",
            "    method_1();",
            "    method_2();",
            "    method_3();",
            "  }",
            "  protected void method_1() {",
            "  }",
            "  protected void method_2() {",
            "  }",
            "  protected void method_3() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/BasePanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='binaryExecutionFlow.dontVisit.package test'>method_2()</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends BasePanel {",
        "  protected void method_1() {",
        "    add(new JButton('1'));",
        "  }",
        "  protected void method_2() {",
        "    add(new JButton('2'));",
        "  }",
        "  protected void method_3() {",
        "    add(new JButton('3'));",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {/add(new JButton('2'))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton('2'))/}");
  }

  /**
   * Sometimes we want to prevent interception for all standard methods in toolkit, i.e. in some
   * package.
   */
  public void test_binaryExecutionFlow_disableForPackage_noExceptions() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  private JPanel m_inner;",
            "  public BasePanel() {",
            "    method_1();",
            "    method_2();",
            "    method_3();",
            "  }",
            "  protected void method_1() {",
            "  }",
            "  protected void method_2() {",
            "  }",
            "  protected void method_3() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/BasePanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='binaryExecutionFlow.dontVisit.package test'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends BasePanel {",
        "  protected void method_1() {",
        "    add(new JButton('1'));",
        "  }",
        "  protected void method_2() {",
        "    add(new JButton('2'));",
        "  }",
        "  protected void method_3() {",
        "    add(new JButton('3'));",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Sometimes we know, that bytecode execution flow is not used in component, so no reason to
   * intercept methods. This makes SmartGWT much faster.
   */
  public void test_byteCodeExecutionFlow_disableForClass() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    createContents();",
            "  }",
            "  public void createContents() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='binaryExecutionFlow.no'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  public void createContents() {",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
    // normal class, not enhanced by CGLib
    assertFalse(ReflectionUtils.isEnchancedClass(panel.getObject().getClass()));
  }

  /**
   * Exposed component may be created in "super" of intercepted method. And we need it directly
   * after "super", so we should try to get its object.
   */
  public void test_exposedComponents_binaryExecutionFlow_prepareObjectsForExposed()
      throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public class BasePanel extends JPanel {",
            "  protected JPanel m_inner;",
            "  public BasePanel() {",
            "    createContents();",
            "  }",
            "  protected void createContents() {",
            "    m_inner = new JPanel();",
            "    add(m_inner);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  protected void createContents() {",
            "    super.createContents();",
            "    m_inner.add(new JButton());",
            "  }",
            "  public boolean shouldCreateInner() {",
            "    return true;",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_inner} {/m_inner.add(new JButton())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/m_inner.add(new JButton())/}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * When we search for exposed children, we should not use binary execution flow.
   */
  public void test_byteCodeExecutionFlow_whenSearchExposedChildren() throws Exception {
    setFileContentSrc(
        "test/BasePanel.java",
        getTestSource(
            "public abstract class BasePanel extends JPanel {",
            "  public BasePanel() {",
            "  }",
            "  public abstract JButton getButton();",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends BasePanel {",
            "  public Test() {",
            "  }",
            "  public JButton getButton() {",
            "    return null;",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.BasePanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    panel.refresh();
  }

  /**
   * We call <code>setVisible()</code> when make screen shot, but this is when refresh already
   * finished, so this should not cause binary execution flow.
   */
  public void test_byteCodeExecutionFlow_ignoreSwingMethods() throws Exception {
    setFileContentSrc(
        "test/Super.java",
        getTestSource(
            "public class Super extends JFrame {",
            "  public void setVisible(boolean b) {",
            "    super.setVisible(b);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "public class Test extends Super {",
            "  public Test() {",
            "  }",
            "  public void setVisible(boolean b) {",
            "    super.setVisible(b);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    // one of the effects was keeping JFrame.visible in "true" 
    JFrame frameObject = (JFrame) frame.getObject();
    assertFalse(frameObject.isVisible());
  }

  /**
   * We should not intercept standard Swing methods.
   */
  public void test_byteCodeExecutionFlow_ignoreSwingMethods2() throws Exception {
    setFileContentSrc(
        "test/Super.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Super extends JPanel {",
            "  // filler filler filler",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends Super {",
            "  public Test() {",
            "  }",
            "  public void paint(Graphics g) {",
            "    super.paint(g);",
            "    setEnabled(false);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // check that "setEnabled(false)" was not executes
    assertTrue(panel.getComponent().isEnabled());
  }

  /**
   * We call "myMethod()" during "fetch" phase, so we should not intercept it. If we will, we will
   * not able to find value of "value" (because "myMethod" is not in execution flow), use default
   * value and fail.
   */
  public void test_byteCodeExecutionFlow_anyParameterValue() throws Exception {
    setFileContentSrc(
        "test/Super.java",
        getTestSource(
            "public class Super extends JFrame {",
            "  public Super() {",
            "  }",
            "  public void setVisible(boolean b) {",
            "    super.setVisible(b);",
            "    myMethod(123);",
            "  }",
            "  public void myMethod(int value) {",
            "    if (value != 123) throw new Error('Invalid value.');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends Super {",
            "  public Test() {",
            "  }",
            "  public void myMethod(int value) {",
            "    super.myMethod(value);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bind children to parents using HierarchyProvider.getChildrenObjects()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using "bindBinary.toDepth" parameter to force root selection then no 'parent'
   * references (exists 'children' references).
   */
  public void test_bindBinary_toDepth() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  private JPanel internalPanel;",
            "  private JButton contentButton;",
            "  public MyPanel(){",
            "    internalPanel = new JPanel();",
            "    add(internalPanel);",
            "    contentButton = getContentButton();",
            "  }",
            "  abstract protected JButton getContentButton();",
            "  // some internal actions for pending attach",
            "  //   contentPanel to 'this'.",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='bindBinary.toDepth'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // contribute special {@link HierarchyProvider}
    TestUtils.addDynamicExtension(COMPONENTS_HIERARCHY_PROVIDERS_POINT_ID, // 
        "  <provider class='" + Test_HierarchyProvider.class.getName() + "'/>");
    //
    try {
      parseContainer(
          "public class Test extends MyPanel {",
          "  public Test() {",
          "  }",
          "  protected JButton getContentButton(){",
          "    return new JButton();",
          "  }",
          "}");
      assertHierarchy(
          "{this: test.MyPanel} {this} {}",
          "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
          "  {new: javax.swing.JButton} {empty} {/new JButton()/}");
      // no errors during refresh
      refresh();
      assertNoErrors(m_lastParseInfo);
    } finally {
      TestUtils.removeDynamicExtension(COMPONENTS_HIERARCHY_PROVIDERS_POINT_ID);
    }
  }

  private static String COMPONENTS_HIERARCHY_PROVIDERS_POINT_ID =
      "org.eclipse.wb.core.componentsHierarchyProviders";

  /**
   * {@link HierarchyProvider} for test.
   */
  public static final class Test_HierarchyProvider extends HierarchyProvider {
    @Override
    public Object[] getChildrenObjects(Object object) throws Exception {
      if (ReflectionUtils.isSuccessorOf(object.getClass(), "test.MyPanel")) {
        Object[] children;
        Object internalPanel = ReflectionUtils.getFieldObject(object, "internalPanel");
        Object contentButton = ReflectionUtils.getFieldObject(object, "contentButton");
        if (contentButton != null) {
          children = new Object[2];
          children[0] = internalPanel;
          children[1] = contentButton;
        } else {
          children = new Object[1];
          children[0] = internalPanel;
        }
        return children;
      }
      return super.getChildrenObjects(object);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout in super() constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link ThisCreationSupport} evaluates {@link SuperConstructorInvocation}, this should
   * cause evaluation of other components.
   */
  public void test_layoutInSuperConstructor_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    super(new BorderLayout());",
            "  }",
            "}");
    panel.refresh();
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {new: java.awt.BorderLayout} {empty} {/super(new BorderLayout());/}");
    // BorderLayout expected
    BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
    assertInstanceOf(BorderLayout.class, layout.getObject());
    assertInstanceOf(EmptyVariableSupport.class, layout.getVariableSupport());
    {
      SuperConstructorArgumentAssociation association =
          (SuperConstructorArgumentAssociation) layout.getAssociation();
      assertEquals("super(new BorderLayout());", association.getSource());
    }
  }

  /**
   * {@link GridBagLayout} (to check that we set object) as argument of
   * {@link SuperConstructorInvocation}.
   */
  public void test_layoutInSuperConstructor_2() throws Exception {
    // parsing will fail, if we don't set GribBagLayout instance after parsing super()
    // (because we need GridBagLayout instance for virtual GridBagConstraints)
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    super(new GridBagLayout());",
        "    add(new JButton());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for
   * {@link JavaEventListener#evaluateAfter(EvaluationContext, org.eclipse.jdt.core.dom.ASTNode)}.
   */
  public void test_evaluateBroadcast() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // listen for ASTNode evaluation
    List<String> expectedNodes_before = ImmutableList.of("setEnabled(false)", "setEnabled(false);");
    List<String> expectedNodes_after = ImmutableList.of("setEnabled(false)", "setEnabled(false);");
    final Iterator<String> expectedIterator_before = expectedNodes_before.iterator();
    final Iterator<String> expectedIterator_after = expectedNodes_after.iterator();
    panel.addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        assertEquals(expectedIterator_before.next(), m_lastEditor.getSource(node));
      }

      @Override
      public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
        assertEquals(expectedIterator_after.next(), m_lastEditor.getSource(node));
      }
    });
    // do refresh(), so initiate "evaluated()"
    assertNoErrors(panel);
    panel.refresh();
    assertNoErrors(panel);
    // all expected ASTNode's should be consumed
    assertFalse(expectedIterator_before.hasNext());
    assertFalse(expectedIterator_after.hasNext());
  }

  public void test_instanceMethod_simple() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton(getMyText());",
            "    add(button);",
            "  }",
            "  private String getMyText() {",
            "    return 'txt';",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertEquals("txt", ((JButton) button.getObject()).getText());
  }

  /**
   * When we intercept method, we should not try to route to abstract {@link MethodDeclaration}.
   */
  public void test_instanceMethod_abstractMethod() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setName(getAbstractName());",
            "  }",
            "  protected abstract String getAbstractName();",
            "}"));
    waitForAutoBuild();
    //
    useStrictEvaluationMode(false);
    ContainerInfo panel =
        parseContainer(
            "public abstract class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  protected abstract String getAbstractName();",
            "}");
    panel.refresh();
    //
    assertEquals("<dynamic>", ((JPanel) panel.getObject()).getName());
  }

  public void test_instanceMethod_complex() throws Exception {
    try {
      parseContainer(
          "public class Test {",
          "  public static void main(String[] args) {",
          "    Test app = new Test();",
          "    app.open();",
          "  }",
          "  public void open() {",
          "    JPanel panel = new JPanel();",
          "    JButton button = new JButton(getMyText());",
          "    panel.add(button);",
          "  }",
          "  private String getMyText() {",
          "    String result = 'txt';",
          "    return result;",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
      assertThat(rootCause).isInstanceOf(DesignerException.class);
      assertEquals(
          ICoreExceptionConstants.EVAL_LOCAL_METHOD_INVOCATION,
          ((DesignerException) rootCause).getCode());
    }
  }

  /**
   * If some method invoked several times, we invoke it only one time.
   */
  public void test_duplicateMethodInvocation_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    addButton();",
            "    addButton();",
            "  }",
            "  private void addButton() {",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * If some method invoked several times, we invoke it only one time.
   * <p>
   * In this case there is only one invocation on execution flow.
   */
  public void test_duplicateMethodInvocation_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    addButton();",
            "  }",
            "  private void addButton() {",
            "    add(new JButton());",
            "  }",
            "  private void disconnectedForExecutionFlow() {",
            "    addButton();",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test that <code>addSeparator()</code> executed only once.
   */
  public void test_duplicateEvaluateExpression() throws Exception {
    ContainerInfo toolBar =
        parseContainer(
            "public class Test extends JToolBar {",
            "  public Test() {",
            "    addSeparator();",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JToolBar} {this} {/addSeparator()/}",
        "  {void} {void} {/addSeparator()/}");
    //
    toolBar.refresh();
    Container container = toolBar.getContainer();
    assertThat(container.getComponents()).hasSize(1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we can execute assignment.
   */
  public void test_executeAssignment() throws Exception {
    ContainerInfo panel =
        (ContainerInfo) parseSource(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String[] args) {",
                "    JPanel panel;",
                "    panel = new JPanel();",
                "  }",
                "}"));
    assert_creation(panel);
  }

  /**
   * Test that assignments done using {@link FieldAccess} are also executed.
   */
  public void test_executeAssignment_FieldAccess() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "  public MyButton(Container container) {",
            "    container.add(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
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
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this).m_value = 1;",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertEquals(1, ReflectionUtils.getFieldInt(button.getObject(), "m_value"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // varArgs, ellipsis
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Java5_varArgs() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public static JLabel createLabel(String text, Object... parameters) {",
            "    return new JLabel(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = MyFactory.createLabel('text', 'p1', 'p2');",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(label)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {static factory: test.MyFactory createLabel(java.lang.String,java.lang.Object[])} {local-unique: label} {/MyFactory.createLabel('text', 'p1', 'p2')/ /add(label)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Standard Swing descriptions
  //
  ////////////////////////////////////////////////////////////////////////////
  private ContainerInfo parsePanelWithButton() throws Exception {
    return parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    JPanel panel = new JPanel();",
        "    {",
        "      JButton button = new JButton();",
        "      button.setText('New button');",
        "      panel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple test case that demonstrates:<br>
   * 1. parsing;<br>
   * 2. related nodes;<br>
   * 3. parent/child link using method "add(Component)";
   */
  public void test_PanelButton_1a() throws Exception {
    ContainerInfo panel = parsePanelWithButton();
    assert_isCleanHierarchy(panel);
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/ /panel.add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /button.setText('New button')/ /panel.add(button)/}");
  }

  public void test_Frame() throws Exception {
    ContainerInfo frame =
        (ContainerInfo) parseSource(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "import java.awt.*;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String[] args) {",
                "    JFrame frame = new JFrame();",
                "    frame.setBackground(Color.YELLOW);",
                "  }",
                "}"));
    assert_isCleanHierarchy(frame);
    assertHierarchy(
        "{new: javax.swing.JFrame} {local-unique: frame} {/new JFrame()/ /frame.setBackground(Color.YELLOW)/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
    assertTrue(frame.getCreationSupport() instanceof ConstructorCreationSupport);
    //
    assertEquals(1, frame.getChildrenComponents().size());
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    assertInstanceOf(ExposedPropertyCreationSupport.class, contentPane.getCreationSupport());
    assertInstanceOf(ImplicitObjectAssociation.class, contentPane.getAssociation());
    //
    assert_creation(frame);
  }

  public void test_FrameButton() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test {",
            "  public static void main(String[] args) {",
            "    JFrame frame = new JFrame();",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText('JButton on JFrame');",
            "      frame.getContentPane().add(button, BorderLayout.NORTH);",
            "    }",
            "  }",
            "}");
    assert_isCleanHierarchy(frame);
    assertHierarchy(
        "{new: javax.swing.JFrame} {local-unique: frame} {/new JFrame()/ /frame.getContentPane()/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {/frame.getContentPane().add(button, BorderLayout.NORTH)/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /button.setText('JButton on JFrame')/ /frame.getContentPane().add(button, BorderLayout.NORTH)/}");
    assertTrue(frame.getCreationSupport() instanceof ConstructorCreationSupport);
    // prepare contentPane
    assertEquals(1, frame.getChildrenComponents().size());
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    // check ExposedPropertyCreationSupport
    {
      CreationSupport creationSupport = contentPane.getCreationSupport();
      assertInstanceOf(ExposedPropertyCreationSupport.class, contentPane.getCreationSupport());
      assertInstanceOf(ImplicitObjectAssociation.class, contentPane.getAssociation());
      //
      assertEquals(frame.getCreationSupport().getNode(), creationSupport.getNode());
      try {
        creationSupport.create(null, null);
        fail();
      } catch (IllegalStateException e) {
      }
      // add
      try {
        creationSupport.add_getSource(null);
        fail();
      } catch (IllegalStateException e) {
      }
      try {
        creationSupport.add_setSourceExpression(null);
        fail();
      } catch (IllegalStateException e) {
      }
    }
    // check ExposedPropertyVariableSupport
    {
      VariableSupport variableSupport = contentPane.getVariableSupport();
      assertTrue(variableSupport instanceof ExposedPropertyVariableSupport);
      // expressions
      {
        NodeTarget target = getNodeStatementTarget(frame, "main(java.lang.String[])", false, 0);
        assertEquals("frame.getContentPane()", variableSupport.getReferenceExpression(target));
        assertEquals("frame.getContentPane().", variableSupport.getAccessExpression(target));
      }
      // name
      assertFalse(variableSupport.hasName());
      try {
        variableSupport.getName();
        fail();
      } catch (IllegalStateException e) {
      }
      try {
        variableSupport.setName("foo");
        fail();
      } catch (IllegalStateException e) {
      }
      // local -> field
      assertFalse(variableSupport.canConvertLocalToField());
      try {
        variableSupport.convertLocalToField();
        fail();
      } catch (IllegalStateException e) {
      }
      // field -> local
      assertFalse(variableSupport.canConvertFieldToLocal());
      try {
        variableSupport.convertFieldToLocal();
        fail();
      } catch (IllegalStateException e) {
      }
    }
    //
    assertEquals(1, contentPane.getChildrenComponents().size());
    ContainerInfo button = (ContainerInfo) contentPane.getChildrenComponents().get(0);
    assertTrue(button.getCreationSupport() instanceof ConstructorCreationSupport);
    //
    assert_creation(frame);
  }

  public void test_ThisFrameButton() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText('JButton on this JFrame');",
            "      getContentPane().add(button, BorderLayout.NORTH);",
            "    }",
            "  }",
            "}");
    assert_isCleanHierarchy(frame);
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {/getContentPane().add(button, BorderLayout.NORTH)/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /button.setText('JButton on this JFrame')/ /getContentPane().add(button, BorderLayout.NORTH)/}");
    //
    assert_creation(frame);
  }

  public void test_otherMethodInvocation() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  static void foo() {",
            "  }",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    foo();",
            "  }",
            "}");
    assertRelatedNodes(panel, new String[]{"new JPanel()"});
  }

  public void test_this_relatedNode_1() throws Exception {
    JavaInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setEnabled(true);",
            "  }",
            "}");
    assertRelatedNodes(panel, new String[]{"setEnabled(true)"});
  }

  public void test_this_relatedNode_2() throws Exception {
    JavaInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    this.setEnabled(true);",
            "  }",
            "}");
    assertRelatedNodes(panel, new String[]{"this.setEnabled(true)"});
  }

  /**
   * Test that invocation of local instance method does not cause any serious problem. However we
   * know that {@link JButton#setText(String)} will be not executed.
   */
  public void test_localMethodInvocation() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    new Test();",
        "  }",
        "  public Test() {",
        "    JPanel panel = new JPanel();",
        "    JButton button = new JButton();",
        "    button.setText(getMyText('some text'));",
        "    panel.add(button);",
        "  }",
        "  public String getMyText(String s) {",
        "    return s;",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/ /panel.add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /button.setText(getMyText('some text'))/ /panel.add(button)/}");
  }

  /**
   * Test for {@link GlobalState#isParsing()}.
   */
  public void test_isParsing() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertFalse(GlobalState.isParsing());
  }

  /**
   * If we can not load "super" {@link Class}, we should not fall with {@link NullPointerException}.
   */
  public void test_ignoreNullSuperClass() throws Exception {
    setFileContentSrc(
        "test/NoClass.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class NoClass extends JPanel {",
            "  // filler",
            "}"));
    waitForAutoBuild();
    getFile("bin/test/NoClass.class").delete(true, null);
    //
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test extends NoClass {",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(ICoreExceptionConstants.PARSER_NO_SUPER_CLASS, de.getCode());
    }
  }

  /**
   * Assert that <code>this.someField = new Component()</code> is handled correctly.
   */
  public void test_assignToThisField() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    this.button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertNotNull(panel.getObject());
    assertNotNull(button.getObject());
  }

  /**
   * Assert that <code>this.someField = new Component()</code> is handled correctly.
   */
  public void test_assignToThisField_applicationPattern() throws Exception {
    parseContainer(
        "public class Test {",
        "  private JPanel panel;",
        "  private JButton button;",
        "  public static void main(String[] args) {",
        "    new Test();",
        "  }",
        "  public Test() {",
        "    this.panel = new JPanel();",
        "    ",
        "    this.button = new JButton();",
        "    this.panel.add(this.button);",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {field-unique: panel} {/new JPanel()/ /this.panel.add(this.button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {field-unique: button} {/new JButton()/ /this.panel.add(this.button)/}");
  }

  /**
   * There was problem that field without initializer considered as "this".
   */
  public void test_useEmptyField() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private LayoutManager layout;",
            "  public Test() {",
            "    setLayout(layout);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // may be Absolute?
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(layout)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  public void test_useUnknownParameter_inConstructor() throws Exception {
    try {
      parseContainer(
          "public class Test extends JPanel {",
          "  private String m_text;",
          "  public Test(String text) {",
          "    m_text = text;",
          "    add(new JButton(m_text));",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException designerCause =
          (DesignerException) DesignerExceptionUtils.getDesignerCause(e);
      assertEquals(ICoreExceptionConstants.EVAL_NO_METHOD_INVOCATION, designerCause.getCode());
      assertThat(designerCause.getMessage()).contains("String text");
    }
  }

  public void test_useUnknownParameter_inLazy() throws Exception {
    try {
      parseContainer(
          "public class Test extends JPanel {",
          "  private String m_text;",
          "  public Test(String text) {",
          "    m_text = text;",
          "    add(getButton());",
          "  }",
          "  private JButton button;",
          "  private JButton getButton() {",
          "    if (button == null) {",
          "      button = new JButton(m_text);",
          "    }",
          "    return button;",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException designerCause =
          (DesignerException) DesignerExceptionUtils.getDesignerCause(e);
      assertEquals(ICoreExceptionConstants.EVAL_NO_METHOD_INVOCATION, designerCause.getCode());
      assertThat(designerCause.getMessage()).contains("String text");
    }
  }

  /**
   * We should not enter into <code>keyPressed()</code>, so no <code>foo()</code> invocation and
   * execution problem back to event, because of no <code>"e"</code> variable.
   */
  public void test_dontVisit_AnonymouseClassDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    addKeyListener(new KeyAdapter() {",
            "      public void keyPressed(KeyEvent e) {",
            "        foo(e != null);",
            "      }",
            "    });",
            "  }",
            "  public void foo(boolean enabled) {",
            "    setEnabled(enabled);",
            "  }",
            "}");
    assertNoErrors(panel);
  }

  /**
   * Test that even if {@link JavaInfo} creation is part of other {@link MethodInvocation}, it is
   * still executed.
   */
  public void test_createAssociateAndInvokeMethod() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton(Container container) {",
        "    container.add(this);",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <constructors>",
        "    <constructor>",
        "      <parameter type='java.awt.Container' parent='true'/>",
        "    </constructor>",
        "  </constructors>",
        "</component>"});
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    boolean enabled = new MyButton(this).isEnabled();",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/new MyButton(this)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {empty} {/new MyButton(this).isEnabled()/}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default parameter values
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfoEvaluationHelper#getDefaultValue(ITypeBinding)}.
   */
  public void test_defaultParameterValues() throws Exception {
    // no binding
    assertSame(null, JavaInfoEvaluationHelper.getDefaultValue(null));
    // primitives
    check_defaultParameterValue("boolean", false);
    check_defaultParameterValue("byte", (byte) 0);
    check_defaultParameterValue("char", (char) 0);
    check_defaultParameterValue("short", (short) 0);
    check_defaultParameterValue("int", 0);
    check_defaultParameterValue("long", 0L);
    check_defaultParameterValue("float", 0.0f);
    check_defaultParameterValue("double", 0.0);
    // String
    check_defaultParameterValue("String", "<dynamic>");
    // collections
    {
      Object defaultValue = getDefaultValue("java.util.ArrayList");
      assertThat(defaultValue).isInstanceOf(ArrayList.class);
    }
    {
      Object defaultValue = getDefaultValue("java.util.LinkedList");
      assertThat(defaultValue).isInstanceOf(LinkedList.class);
    }
    {
      Object defaultValue = getDefaultValue("java.util.Vector");
      assertThat(defaultValue).isInstanceOf(Vector.class);
    }
    {
      Object defaultValue = getDefaultValue("java.util.HashSet");
      assertThat(defaultValue).isInstanceOf(Set.class);
    }
    {
      Object defaultValue = getDefaultValue("java.util.HashMap");
      assertThat(defaultValue).isInstanceOf(Map.class);
    }
    // generic Object
    {
      Object defaultValue = getDefaultValue("Object");
      assertThat(defaultValue).isNull();
    }
  }

  private void check_defaultParameterValue(String typeName, Object expectedValue) throws Exception {
    Object defaultValue = getDefaultValue(typeName);
    assertEquals(expectedValue, defaultValue);
  }

  private Object getDefaultValue(String typeName) throws Exception {
    try {
      createTypeDeclaration(
          "test",
          "Test.java",
          getSource("package test;", "public class Test {", "  public Test("
              + typeName
              + " parameter) {", "  }", "}"));
      SingleVariableDeclaration parameter =
          (SingleVariableDeclaration) m_lastEditor.getEnclosingNode("parameter").getParent();
      ITypeBinding binding = AstNodeUtils.getTypeBinding(parameter);
      Object defaultValue = JavaInfoEvaluationHelper.getDefaultValue(binding);
      return defaultValue;
    } finally {
      if (m_lastModelUnit != null) {
        m_lastModelUnit.delete(true, null);
      }
    }
  }

  /**
   * In non-strict mode default value for unknown parameter will be used, without showing exception
   * to user.
   */
  public void test_useUnknownParameter_nonStrictMode() throws Exception {
    useStrictEvaluationMode(false);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test(String text) {",
            "    add(new JButton(text));",
            "  }",
            "}");
    panel.refresh();
    // default value (empty String) is used for unknown parameter
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertEquals("<dynamic>", ((JButton) button.getObject()).getText());
  }

  /**
   * There was problem that {@link SuperMethodInvocation} incorrectly considered as {@link JavaInfo}
   * creation. So, if it throws exception, we consider it as serious and terminate parsing.
   */
  public void test_exceptionIn_SuperMethodInvocation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public String getFoo() {",
            "    throw new IllegalStateException();",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setName(super.getFoo());",
        "  }",
        "}");
    // one "bad" expected - for "super.getFoo()"
    {
      BadNodesCollection badParserNodes = m_lastState.getBadParserNodes();
      List<BadNodeInformation> badNodes = badParserNodes.nodes();
      assertThat(badNodes).hasSize(1);
      ASTNode badNode = badNodes.get(0).getNode();
      assertEquals("setName(super.getFoo());", m_lastEditor.getSource(badNode));
    }
  }

  /**
   * Test that we provide {@link Vector} value for {@link Vector} unknown parameter.
   */
  public void test_unknownVectorParameter() throws Exception {
    setFileContentSrc(
        "test/MyList.java",
        getTestSource(
            "public class MyList extends JList {",
            "  public MyList(java.util.Vector values) {",
            "    super(values);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    ContainerInfo panel =
        parseContainer(
            "import java.util.Vector;",
            "public class Test extends JPanel {",
            "  public Test(Vector values) {",
            "    MyList list = new MyList(values);",
            "    add(list);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * If we created {@link JavaInfo} for some {@link Expression} however it was not connected to
   * hierarchy, then we should clear its value, to prevent re-using its value again and again. We
   * should evaluate this {@link Expression} as any other.
   */
  public void test_disconnectedModel() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private Object button;",
            "  public MyPanel(JButton button) {",
            "    this.button = button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(new JButton());",
            "  }",
            "}");
    // first refresh();
    refresh();
    Object button_1 = ReflectionUtils.getFieldObject(panel.getObject(), "button");
    assertNotNull(button_1);
    // second refresh();
    refresh();
    Object button_2 = ReflectionUtils.getFieldObject(panel.getObject(), "button");
    assertNotNull(button_2);
    assertNotSame(button_1, button_2);
  }

  /**
   * In addition to first case, there was also second one. When we clear value, we should not clear
   * too much - we should only remove reference on model, but not reference on assigned value.
   */
  public void test_disconnectedModel_useItsVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JLabel object = new JLabel();",
            "    String text = object.toString();",
            "    setName(text);",
            "  }",
            "}");
    refresh();
    // assert that "object" was evaluated
    String name = panel.getComponent().getName();
    assertThat(name).startsWith("javax.swing.JLabel");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Return from method
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_returnFromMethod_validJavaInfo() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    add(createInnerPanel());",
            "  }",
            "  private JPanel createInnerPanel() {",
            "    return new JPanel();",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    assert_creation(panel);
  }

  public void test_returnFromMethod_invalidObject() throws Exception {
    setFileContentSrc(
        "test/MyBadObject.java",
        getSource(
            "package test;",
            "public class MyBadObject {",
            "  public MyBadObject() {",
            "    throw new IllegalStateException();",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    foo();",
            "  }",
            "  private Object foo() {",
            "    return new MyBadObject();",
            "  }",
            "}");
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Beans.isDesignTime()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * During parsing/execution we should ensure that {@link Beans#isDesignTime()} returns
   * <code>true</code>.
   */
  public void test_isDesignTime_forComponent() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "    if (!java.beans.Beans.isDesignTime()) {",
            "      throw new IllegalStateException();",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse, no errors expected
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    assertNoErrors(panel);
    assertEquals(1, panel.getChildrenComponents().size());
    // refresh, again no errors
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link Beans#isDesignTime()} should return <code>true</code> also when we parse/execute root.
   */
  public void test_isDesignTime_forRoot() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    if (!java.beans.Beans.isDesignTime()) {",
            "      throw new IllegalStateException();",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse, no errors expected
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNoErrors(panel);
    // refresh, again no errors
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters of constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test: real values from arguments of super()
   */
  public void test_constructorParameters_real() throws Exception {
    prepare_constructorParameters();
    parseContainer(
        "class Test extends SuperPanel {",
        "  public Test(int _int, boolean _true, boolean _false, String _string, Object _null) {",
        "    super(1, true, false, 'abc', null);",
        "  }",
        "}");
  }

  /**
   * Test: values for super() in {@link Javadoc} tags.
   */
  public void test_constructorParameters_parameters() throws Exception {
    prepare_constructorParameters();
    parseContainer(
        "class Test extends SuperPanel {",
        "  /**",
        "  * @wbp.eval.method.parameter _int 1",
        "  * @wbp.eval.method.parameter _true true",
        "  * @wbp.eval.method.parameter _false false",
        "  * @wbp.eval.method.parameter _string 'ab' + 'c'",
        "  * @wbp.eval.method.parameter _null null",
        "  */",
        "  public Test(int _int, boolean _true, boolean _false, String _string, Object _null) {",
        "    super(_int, _true, _false, _string, _null);",
        "  }",
        "}");
  }

  /**
   * Test for using {@link IThisMethodParameterEvaluator} for unknown constructor parameters.
   */
  public void test_constructorParameters_IThisMethodParameterEvaluator() throws Exception {
    prepare_ThisEvaluatorObject();
    // parse, if all good, it will be parsed
    parseContainer(
        "class Test extends ThisEvaluatorObject {",
        "  public Test(JFrame parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}");
    // check value for "style"
    {
      Expression styleExpression = (Expression) m_lastEditor.getEnclosingNode("style);");
      assertEquals(555, JavaInfoEvaluationHelper.getValue(styleExpression));
    }
  }

  /**
   * Test that explicit value is evaluated correctly, not just fake from
   * {@link IThisMethodParameterEvaluator} used.
   */
  public void test_constructorParameters_IThisMethodParameterEvaluator_explicitValue()
      throws Exception {
    prepare_ThisEvaluatorObject();
    // parse, if all good, it will be parsed
    parseContainer(
        "class Test extends ThisEvaluatorObject {",
        "  public Test(JFrame parent, int style) {",
        "    super(parent, 123);",
        "  }",
        "}");
    // check value for "style"
    {
      Expression styleExpression = (Expression) m_lastEditor.getEnclosingNode("123);");
      assertEquals(123, JavaInfoEvaluationHelper.getValue(styleExpression));
    }
  }

  private void prepare_constructorParameters() throws Exception {
    setFileContentSrc(
        "test/SuperPanel.java",
        getSourceDQ(
            "package test;",
            "public class SuperPanel extends javax.swing.JPanel {",
            "  public SuperPanel(int _int, boolean _true, boolean _false, String _string, Object _null) {",
            "    if (_int != 1 || !_true || _false || !'abc'.equals(_string) || _null != null) {",
            "      throw new IllegalArgumentException();",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ThisEvaluatorObject
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepare_ThisEvaluatorObject() throws Exception {
    setFileContentSrc(
        "test/ThisEvaluatorObject.java",
        getTestSource(
            "public class ThisEvaluatorObject extends JPanel {",
            "  public ThisEvaluatorObject(JFrame parent, int style) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/ThisEvaluatorObject.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <model class='" + ThisEvaluatorJavaInfo.class.getName() + "'/>",
            "</component>"));
    waitForAutoBuild();
  }

  /**
   * {@link JavaInfo} for testing {@link IThisMethodParameterEvaluator}.
   */
  public static class ThisEvaluatorJavaInfo extends ContainerInfo
      implements
        IThisMethodParameterEvaluator {
    public ThisEvaluatorJavaInfo(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
    }

    public Object evaluateParameter(EvaluationContext context,
        MethodDeclaration methodDeclaration,
        String methodSignature,
        SingleVariableDeclaration parameter,
        int index) throws Exception {
      if (index == 0) {
        return new JFrame();
      }
      if (index == 1) {
        return 555;
      }
      return AstEvaluationEngine.UNKNOWN;
    }
  }
}
