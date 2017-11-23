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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.RootAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.eval.evaluators.AnonymousEvaluationError;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.exception.MultipleConstructorsError;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.core.annotations.WaitForAutoBuildAfter;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author scheglov_ke
 */
public class SwingParserTest extends SwingModelTest {
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
  // Entry points
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No any Swing, eRCP or RCP classes, for no GUI toolkit.
   */
  public void test_noToolkit() throws Exception {
    try {
      parseContainer(
          "public class Test {",
          "  public static void main(String[] args) {",
          "    Object justSomeCreation = new Object();",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.PARSER_NO_TOOLKIT, e.getCode());
    }
  }

  /**
   * No known entry point and no {@link ClassInstanceCreation}-s, so probably not GUI class.
   */
  public void test_bad_DataBean() throws Exception {
    try {
      parseContainer(
          "public class Test {",
          "  private String m_name;",
          "  public String getName() {",
          "    return m_name;",
          "  }",
          "  public void setName(String name) {",
          "    m_name = name;",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.PARSER_NOT_GUI, e.getCode());
      assertTrue(DesignerExceptionUtils.isWarning(e));
    }
  }

  /**
   * Test for case of empty main() method - no root can be found, so exception thrown.
   */
  public void test_emptyMain() throws Exception {
    try {
      parseContainer(
          "class Test {",
          "  JButton button; // just to have reference on Swing",
          "  public static void main(String[] args) {",
          "  }",
          "}");
      fail();
    } catch (Throwable e_) {
      NoEntryPointError e = (NoEntryPointError) DesignerExceptionUtils.getRootCause(e_);
      assertThat(e.getEditor()).isNotNull();
      assertThat(e.getTypeDeclaration()).isNotNull();
    }
  }

  /**
   * Test when there are no root method, so exception thrown.
   */
  public void test_noMain() throws Exception {
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test {",
          "  JButton button; // just to have reference on Swing",
          "}");
      fail();
    } catch (NoEntryPointError e) {
      assertThat(e.getEditor()).isNotNull();
      assertThat(e.getTypeDeclaration()).isNotNull();
    }
  }

  /**
   * No root component, and unit has compilation errors. Most probably parsing failed because of
   * these errors.
   */
  public void test_noRootComponent_withCompilationErrors() throws Exception {
    try {
      m_ignoreCompilationProblems = true;
      parseContainer(
          "// filler filler filler",
          "public class Test {",
          "  JPanel panel; // just to have reference on Swing",
          "  public static void main(String[] args) {",
          "    JButton button = new JButton() // note, no ';' at the end",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(ICoreExceptionConstants.PARSER_NO_ROOT_WHEN_COMPILATION_ERRORS, de.getCode());
    }
  }

  /**
   * Automatically use constructor as entry point: good guess.
   */
  public void test_goodSuperClass_useConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public void Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_parse_unknownSuperClassForAnonymous() throws Exception {
    m_ignoreCompilationProblems = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public void Test() {",
            "    new UnknownType() {};",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * If several constructors, then default (without parameters) should be used.
   */
  public void test_severalConstructors_useDefault() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "  public Test(int a) {",
        "  }",
        "}");
  }

  /**
   * Several constructors, but no default (without parameters), so fail.
   */
  public void test_severalConstructors_noDefault() throws Exception {
    try {
      parseContainer(
          "public class Test extends JPanel {",
          "  public Test(double a) {",
          "  }",
          "  public Test(int a) {",
          "  }",
          "}");
      fail();
    } catch (MultipleConstructorsError e) {
      assertThat(e.getEditor()).isNotNull();
      assertThat(e.getTypeDeclaration()).isNotNull();
    }
  }

  /**
   * Test for using {@link EventQueue#invokeLater(Runnable)} in <code>main()</code>.
   */
  public void test_EventQueue_invokeLater() throws Exception {
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    EventQueue.invokeLater(new Runnable() {",
        "      public void run() {",
        "        new Test();",
        "      }",
        "    });",
        "  }",
        "  public Test() {",
        "    JFrame frame = new JFrame();",
        "  }",
        "}");
  }

  /**
   * Test for using {@link SwingUtilities#invokeLater(Runnable)} in <code>main()</code>.
   */
  public void test_SwingUtilities_invokeLater() throws Exception {
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    SwingUtilities.invokeLater(new Runnable() {",
        "      public void run() {",
        "        new Test();",
        "      }",
        "    });",
        "  }",
        "  public Test() {",
        "    JFrame frame = new JFrame();",
        "  }",
        "}");
  }

  /**
   * Test for using {@link EventQueue#invokeAndWait(Runnable)} in <code>main()</code>.
   */
  public void test_EventQueue_invokeAndWait() throws Exception {
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) throws Exception {",
        "    EventQueue.invokeAndWait(new Runnable() {",
        "      public void run() {",
        "        new Test();",
        "      }",
        "    });",
        "  }",
        "  public Test() {",
        "    JFrame frame = new JFrame();",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JFrame} {local-unique: frame} {/new JFrame()/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  /**
   * Test for using {@link SwingUtilities#invokeAndWait(Runnable)} in <code>main()</code>.
   */
  public void test_SwingUtilities_invokeAndWait() throws Exception {
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) throws Exception {",
        "    SwingUtilities.invokeAndWait(new Runnable() {",
        "      public void run() {",
        "        new Test();",
        "      }",
        "    });",
        "  }",
        "  public Test() {",
        "    JFrame frame = new JFrame();",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JFrame} {local-unique: frame} {/new JFrame()/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  /**
   * Test for using @wbp.parser.entryPoint to force starting execution flow from some constructor,
   * even if we don't know superclass.
   */
  public void test_entryPointTag_forConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public Test() {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test for using @wbp.parser.entryPoint to force starting execution flow from method.
   */
  public void test_entryPointTag_forMethod() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public void foo() {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    panel.refresh();
    assertNoErrors(panel);
    // try to set property, just to see that it goes into correct method
    panel.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test {",
        "  /**",
        "  * @wbp.parser.entryPoint",
        "  */",
        "  public void foo() {",
        "    JPanel panel = new JPanel();",
        "    panel.setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Automatically use constructor as entry point: good guess.
   */
  public void test_alwaysTryConstructor_success() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public Test() {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Automatically use constructor as entry point: no, still no GUI in constructor.
   */
  public void test_alwaysTryConstructor_fail() throws Exception {
    try {
      parseContainer(
          "public class Test {",
          "  JButton button; // just to have reference on Swing",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (Throwable e_) {
      Throwable e = DesignerExceptionUtils.getRootCause(e_);
      assertThat(e).isExactlyInstanceOf(NoEntryPointError.class);
    }
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
  public void test_panelButton_1a() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText(\"New button\");",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    // check JPanel itself
    {
      assertInstanceOf(ConstructorCreationSupport.class, panel.getCreationSupport());
      assertInstanceOf(RootAssociation.class, panel.getAssociation());
      assertSame(JPanel.class, panel.getDescription().getComponentClass());
      assertEquals(IPreferenceConstants.TOOLKIT_ID, panel.getDescription().getToolkit().getId());
      assertEquals("Swing toolkit", panel.getDescription().getToolkit().getName());
      assertTrue(panel.getDescription().toString().length() != 0);
      // check nodes
      assertRelatedNodes(panel, new String[]{"new JPanel()", "panel.add(button)"});
    }
    // check (1) JPanel's child - JButton
    assertEquals(2, panel.getChildrenJava().size());
    assertTrue(panel.getChildrenJava().get(0) instanceof FlowLayoutInfo);
    {
      ContainerInfo button = (ContainerInfo) panel.getChildrenJava().get(1);
      // check creation support
      CreationSupport creationSupport = button.getCreationSupport();
      assertTrue(creationSupport instanceof ConstructorCreationSupport);
      assertSame(JButton.class, button.getDescription().getComponentClass());
      // check nodes
      assertRelatedNodes(button, new String[]{
          "new JButton()",
          "button.setText(\"New button\")",
          "panel.add(button)"});
    }
  }

  /**
   * In addition to {@link #test_panelButton_1a()} this test demonstrates:
   * 
   * 1. passing parent into method;
   * 
   * @author scheglov_ke
   */
  public void test_panelButton_1b() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "    createButton(panel);",
            "  }",
            "  private static void createButton(Container parent) {",
            "    JButton button = new JButton();",
            "    button.setText(\"New button\");",
            "    parent.add(button);",
            "  }",
            "}");
    // check JPanel itself
    {
      // check nodes
      assertRelatedNodes(panel, new String[]{
          "new JPanel()",
          "parent.add(button)",
          "createButton(panel)"});
    }
    // check (1) JPanel's child - JButton
    assertEquals(2, panel.getChildrenJava().size());
    assertTrue(panel.getChildrenJava().get(0) instanceof FlowLayoutInfo);
    {
      ContainerInfo button = (ContainerInfo) panel.getChildrenJava().get(1);
      // check nodes
      assertRelatedNodes(button, new String[]{
          "new JButton()",
          "button.setText(\"New button\")",
          "parent.add(button)"});
    }
    //
    assert_creation(panel);
  }

  /**
   * Support for setLayout().
   */
  public void test_panelButton_2() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText(\"New button\");",
            "      panel.add(button, BorderLayout.WEST);",
            "    }",
            "  }",
            "}");
    //
    assertEquals(2, panel.getChildrenJava().size());
    assertTrue(panel.getChildrenJava().get(0) instanceof BorderLayoutInfo);
    assertTrue(panel.getLayout() instanceof BorderLayoutInfo);
    assertTrue(panel.getChildrenJava().get(1) instanceof ContainerInfo);
    // check that JavaEventListener works
    final boolean objectWasSet[] = new boolean[1];
    panel.addBroadcastListener(new JavaInfoSetObjectAfter() {
      public void invoke(JavaInfo target, Object o) throws Exception {
        if (target == panel) {
          objectWasSet[0] = true;
        }
      }
    });
    // check creation
    assertFalse(objectWasSet[0]);
    assert_creation(panel);
    panel.refresh();
    assertTrue(objectWasSet[0]);
  }

  /**
   * We should be able to parse forms in default package.
   */
  public void test_parse_defaultPackage() throws Exception {
    setFileContentSrc(
        "MyButton.java",
        getSource(
            "// filler filler filler",
            "// filler filler filler",
            "public class MyButton extends javax.swing.JButton {",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        (ContainerInfo) parseSource(
            "",
            "Test.java",
            getSource(
                "public class Test extends javax.swing.JPanel {",
                "  public Test() {",
                "    add(new MyButton());",
                "  }",
                "}"));
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: MyButton} {empty} {/add(new MyButton())/}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Roots
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we skip non-root objects.
   */
  public void test_canBeRoot() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  public Test() {",
            "    setBackground(Color.green);",
            "    Button button = new Button(\"My button\");",
            "  }",
            "}");
    assertTrue(frame.getCreationSupport() instanceof ThisCreationSupport);
  }

  /**
   * Test that we select biggest root.
   */
  public void test_severalRoots() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  public Test() {",
            "    setBackground(Color.green);",
            "    JButton button = new JButton(\"My button\");",
            "  }",
            "}");
    assertTrue(frame.getCreationSupport() instanceof ThisCreationSupport);
  }

  public void test_localConstructor() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo frame =
        parseContainer(
            "class Test {",
            "  private JFrame frame;",
            "  public static void main(String[] args) {",
            "    Test window = new Test();",
            "    window.frame.setVisible(true);",
            "  }",
            "  public Test() {",
            "    initialize();",
            "  }",
            "  private void initialize() {",
            "    frame = new JFrame();",
            "    frame.setEnabled(false);",
            "  }",
            "}");
    frame.refresh();
    assertFalse(((JFrame) frame.getObject()).isEnabled());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We try actual constructor first. It does not throw any exception, so it is used.
   */
  public void test_constructorEvaluation_goodActual_success() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "    setText('A');",
            "  }",
            "  public MyButton(int value) {",
            "    setText('B');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton(0));",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton(0))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {empty} {/add(new MyButton(0))/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // refresh
    panel.refresh();
    // MyButton was evaluated using actual constructor...
    assertEquals("B", ((JButton) button.getObject()).getText());
    assertNoErrors(panel);
    // not placeholder
    {
      assertFalse(button.isPlaceholder());
      assertThat(PlaceholderUtils.getExceptions(button)).isEmpty();
    }
  }

  /**
   * We try first actual constructor, but it throws exception. So, default constructor is used.
   */
  public void test_constructorEvaluation_exceptionActual_goodDefault_success() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "    setText('A');",
            "  }",
            "  public MyButton(int value) {",
            "    setText('B');",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton(0));",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton(0))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {empty} {/add(new MyButton(0))/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // refresh
    panel.refresh();
    // MyButton was evaluated using default constructor...
    assertEquals("A", ((JButton) button.getObject()).getText());
    // ...actual constructor thrown logged exception
    List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(badNodes).hasSize(1);
    {
      BadNodeInformation badNode = badNodes.get(0);
      ASTNode node = badNode.getNode();
      Throwable e = DesignerExceptionUtils.getRootCause(badNode.getException());
      assertEquals("new MyButton(0)", m_lastEditor.getSource(node));
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage()).isEqualTo("actual");
    }
    // not placeholder
    {
      assertFalse(button.isPlaceholder());
      assertThat(PlaceholderUtils.getExceptions(button)).hasSize(1);
    }
  }

  /**
   * Actual constructor is default, so exception should be logged only once.
   */
  public void test_constructorEvaluation_exceptionActual_sameDefault() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // refresh
    panel.refresh();
    // "MyButton" has placeholder object - JPanel
    {
      assertThat(button.getObject()).isInstanceOf(JPanel.class);
      assertTrue(button.isPlaceholder());
      assertThat(PlaceholderUtils.getExceptions(button)).hasSize(1);
    }
    // check logged exceptions
    List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(badNodes).hasSize(1);
    {
      BadNodeInformation badNode = badNodes.get(0);
      ASTNode node = badNode.getNode();
      Throwable e = DesignerExceptionUtils.getRootCause(badNode.getException());
      assertEquals("new MyButton()", m_lastEditor.getSource(node));
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage()).isEqualTo("actual");
    }
  }

  /**
   * We try first actual constructor, but it throws exception. So, we try default constructor, but
   * it also throws exception. So we use placeholder instead.
   */
  public void test_constructorEvaluation_exceptionActual_exceptionDefault() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "    throw new IllegalStateException('default');",
            "  }",
            "  public MyButton(int value) {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton(0));",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton(0))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {empty} {/add(new MyButton(0))/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // refresh
    panel.refresh();
    // "MyButton" has placeholder object - JPanel
    {
      assertThat(button.getObject()).isInstanceOf(JPanel.class);
      assertTrue(button.isPlaceholder());
      assertThat(PlaceholderUtils.getExceptions(button)).hasSize(2);
    }
    // check logged exceptions
    List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(badNodes).hasSize(2);
    {
      BadNodeInformation badNode = badNodes.get(0);
      ASTNode node = badNode.getNode();
      Throwable e = DesignerExceptionUtils.getRootCause(badNode.getException());
      assertEquals("new MyButton(0)", m_lastEditor.getSource(node));
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage()).isEqualTo("actual");
    }
    {
      BadNodeInformation badNode = badNodes.get(1);
      ASTNode node = badNode.getNode();
      Throwable e = DesignerExceptionUtils.getRootCause(badNode.getException());
      assertEquals("new MyButton(0)", m_lastEditor.getSource(node));
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage()).isEqualTo("default");
    }
  }

  /**
   * For standard AWT/Swing components actual constructor should be used.
   */
  public void test_constructorEvaluation_standardComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('abc'));",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton('abc'))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton('abc'))/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    panel.refresh();
    // JButton was evaluated using actual constructor
    assertEquals("abc", ((JButton) button.getObject()).getText());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Windows
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check that all {@link Frame}'s are disposed, so not visible.
   */
  public void test_windowsDisposing() throws Exception {
    parseContainer(
        "// filler filler filler",
        "class Test extends JFrame {",
        "  Test() {",
        "    setVisible(true);",
        "  }",
        "}");
    {
      Frame[] frames = Frame.getFrames();
      for (int i = 0; i < frames.length; i++) {
        Frame frame = frames[i];
        assertFalse(frame.isVisible());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compilation errors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using undefined method.
   */
  public void test_compilationErrors_undefinedMethod() throws Exception {
    m_ignoreCompilationProblems = true;
    try {
      ContainerInfo panel =
          parseContainer(
              "public class Test extends JPanel {",
              "  public Test() {",
              "    initData();",
              "  }",
              "}");
      assertNotNull(panel);
    } finally {
      m_ignoreCompilationProblems = false;
      do_projectDispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special classes
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_unknownClass() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    Integer integer = new Integer(0);",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).isEmpty();
  }

  /**
   * Creation of inner non-static class should be ignored.
   */
  @WaitForAutoBuildAfter
  public void test_nonStaticInnerClass() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyPanel());",
            "  }",
            "  public class MyPanel extends JPanel {",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).isEmpty();
  }

  /**
   * We can not use CGLib to create non-abstract version of standard Swing class (from system
   * {@link ClassLoader}).
   */
  public void test_abstractStandardSwingClass() throws Exception {
    ContainerInfo component =
        parseContainer(
            "public class Test extends JComponent {",
            "  public Test() {",
            "    // filler filler filler",
            "  }",
            "}");
    assertThat(component.getDescription().getComponentClass()).isSameAs(Container.class);
    //
    component.refresh();
    assertNoErrors(component);
  }

  /**
   * Parse factory should ignore interface creations.
   */
  @WaitForAutoBuildAfter
  public void test_ignoreInterfaces() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    Border border = BorderFactory.createEtchedBorder();",
            "    setBorder(border);",
            "  }",
            "  public class MyPanel extends JPanel {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * If instance of anonymous {@link Component} subclass is created, create instead nearest
   * non-abstract {@link Component} superclass.
   */
  public void test_newAnonymousClass() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton() {};",
            "    add(button);",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We should execute all <code>add(Type)</code> methods, if <code>Type</code> is {@link Component}
   * subclass.
   */
  public void test_execute_addMethod() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void add(Container container) {",
            "    super.add(container);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We should execute all <code>add(Type[,...])</code> methods, if <code>Type</code> is
   * {@link Component} subclass.
   */
  public void test_execute_addMethod_withConstraints() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void add(Component component, String title) {",
            "    super.add(component);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    add(new JButton(), 'title');",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/add(new JButton(), 'title')/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton(), 'title')/}");
  }

  /**
   * We don't support using {@link SuperMethodInvocation} as association, so we rewrite it to be
   * just {@link MethodInvocation}.
   */
  public void test_SuperMethodInvocation_association() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    super.setLayout(new GridBagLayout());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(new GridBagLayout())/}",
        "  {new: java.awt.GridBagLayout} {empty} {/setLayout(new GridBagLayout())/}");
    refresh();
    // GridBagLayout is set
    assertInstanceOf("java.awt.GridBagLayout", panel.getContainer().getLayout());
    // source rewritten as needed
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JList
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link JList#JList(Object[])} should not be used with <code>null</code> argument.
   */
  public void test_JList_new_ObjectArray() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JList list = new JList((Object[]) null);",
            "    add(list);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link JList#JList(java.util.Vector)} should not be used with <code>null</code> argument.
   */
  public void test_JList_new_Vector() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JList list = new JList((java.util.Vector) null);",
            "    add(list);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link JList#setListData(Object[])} should not be used with <code>null</code> argument.
   */
  public void test_JList_setListData_ObjectArray() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JList list = new JList();",
            "    list.setListData((Object[]) null);",
            "    add(list);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link JList#setListData(java.util.Vector)} should not be used with <code>null</code> argument.
   */
  public void test_JList_setListData_Vector() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JList list = new JList();",
            "    list.setListData((java.util.Vector) null);",
            "    add(list);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JComboBox
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link JComboBox#setModel(javax.swing.ComboBoxModel)} should not be used with <code>null</code>
   * argument.
   */
  public void test_JComboBox_setModel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JComboBox combo = new JComboBox();",
            "    combo.setModel(null);",
            "    add(combo);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link JComboBox#JComboBox(javax.swing.ComboBoxModel)} should not be used with
   * <code>null</code> argument.
   */
  public void test_JComboBox_constructor_ComboBoxModel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JComboBox combo = new JComboBox((javax.swing.ComboBoxModel) null);",
            "    add(combo);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link JComboBox#JComboBox(Object[])} should not be used with <code>null</code> argument.
   */
  public void test_JComboBox_constructor_Objects() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JComboBox combo = new JComboBox((Object[]) null);",
            "    add(combo);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * {@link JComboBox#JComboBox(java.util.Vector)} should not be used with <code>null</code>
   * argument.
   */
  public void test_JComboBox_constructor_Vector() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JComboBox combo = new JComboBox((java.util.Vector) null);",
            "    add(combo);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We should ignore {@link JComboBox#setRenderer(javax.swing.ListCellRenderer)} invocation if it
   * is done with anonymous implementation.
   */
  public void test_JComboBox_setRenderer_anonymous() throws Exception {
    useStrictEvaluationMode(false);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "    combo.setRenderer(new ListCellRenderer() {",
            "      public Component getListCellRendererComponent(JList list, Object value,"
                + " int index, boolean isSelected, boolean cellHasFocus) {",
            "        return null;",
            "      }",
            "    });",
            "  }",
            "}");
    panel.refresh();
    // evaluation of anonymous "new ListCellRenderer() {}" causes exception, replaced with "null"
    {
      List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
      assertThat(badNodes).hasSize(1);
      BadNodeInformation badNode = badNodes.get(0);
      Throwable rootException = DesignerExceptionUtils.getRootCause(badNode.getException());
      assertThat(rootException).isExactlyInstanceOf(AnonymousEvaluationError.class);
      assertThat(m_lastEditor.getSource(badNode.getNode())).startsWith("new ListCellRenderer() {");
    }
    // ...but we ignore "setRenderer(null)", so JComboBox has some valid renderer
    JComboBox combo = (JComboBox) panel.getChildrenComponents().get(0).getComponent();
    assertNotNull(combo.getRenderer());
  }

  /**
   * We should ignore {@link JComboBox#setRenderer(javax.swing.ListCellRenderer)} invocation with
   * <code>null</code> argument.
   */
  public void test_JComboBox_setRenderer_null() throws Exception {
    useStrictEvaluationMode(false);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "    combo.setRenderer(null);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    //
    JComboBox combo = (JComboBox) panel.getChildrenComponents().get(0).getComponent();
    assertNotNull(combo.getRenderer());
  }

  /**
   * We should ignore {@link AbstractButton#setModel(javax.swing.ButtonModel)} invocation with
   * <code>null</code> argument.
   */
  public void test_AbstractButton_setModel_null() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    button.setModel(null);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "parser.preferredRoot"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using "preferredRoot" parameter to force root selection.
   */
  public void test_preferredRoot_singlePreferred() throws Exception {
    setJavaContentSrc("test", "MyPanel", new String[]{
        "public class MyPanel extends JPanel {",
        "  public MyPanel() {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <parameters>",
        "    <parameter name='parser.preferredRoot'>true</parameter>",
        "  </parameters>",
        "</component>"});
    // standard JPanel has bigger hierarchy, so without "parser.preferredRoot" is would be selected 
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    MyPanel myPanel = new MyPanel();",
        "    //",
        "    JPanel panel = new JPanel();",
        "    panel.add(new JButton());",
        "    panel.add(new JButton());",
        "    panel.add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{new: test.MyPanel} {local-unique: myPanel} {/new MyPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test for using "preferredRoot" parameter to force root selection.
   */
  public void test_preferredRoot_twoPreferred() throws Exception {
    setJavaContentSrc("test", "MyPanel", new String[]{
        "public class MyPanel extends JPanel {",
        "  public MyPanel() {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <parameters>",
        "    <parameter name='parser.preferredRoot'>true</parameter>",
        "  </parameters>",
        "</component>"});
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    MyPanel panel_1 = new MyPanel();",
        "    //",
        "    MyPanel panel_2 = new MyPanel();",
        "    panel_2.add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{new: test.MyPanel} {local-unique: panel_2} {/new MyPanel()/ /panel_2.add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/panel_2.add(new JButton())/}");
  }

  /**
   * Test for using "@wbp.parser.preferredRoot" comment to force root selection.
   */
  public void test_preferredRoot_useEndOfLineComment() throws Exception {
    parseContainer(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JPanel myPanel = new JPanel(); // @wbp.parser.preferredRoot",
        "    //",
        "    JPanel panel = new JPanel();",
        "    panel.add(new JButton());",
        "    panel.add(new JButton());",
        "    panel.add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: myPanel} {/new JPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Event listener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_eventListeners() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    addKeyListener(new KeyAdapter() {});",
            "  }",
            "}");
    assertEquals(1, panel.getRelatedNodes().size());
    ASTNode relatedNode = panel.getRelatedNodes().get(0);
    assertTrue(m_lastEditor.getSource(relatedNode).contains("addKeyListener"));
  }
}
