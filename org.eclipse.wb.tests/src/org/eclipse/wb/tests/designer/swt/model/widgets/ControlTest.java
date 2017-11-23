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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.core.model.association.RootAssociation;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.swt.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.ColorSupport;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

/**
 * @author scheglov_ke
 * @author mitin_aa
 */
public class ControlTest extends RcpModelTest {
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
  public void test_MethodMain() throws Exception {
    CompositeInfo shellInfo =
        parseComposite(
            "class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setSize(320, 240);",
            "    shell.setText('SWT');",
            "    Button button = new Button(shell, SWT.NONE);",
            "    button.setBounds(10, 20, 50, 30);",
            "    button.setText('push');",
            "  }",
            "}");
    assertInstanceOf(RootAssociation.class, shellInfo.getAssociation());
    //
    List<ControlInfo> children = shellInfo.getChildrenControls();
    assertEquals(1, children.size());
    //
    ControlInfo buttonInfo = children.get(0);
    Property buttonProperty = buttonInfo.getPropertyByTitle("text");
    assertNotNull(buttonProperty);
    assertEquals("push", buttonProperty.getValue());
    assertTrue(buttonProperty.getEditor() instanceof StringPropertyEditor);
    //
    Property shellProperty = shellInfo.getPropertyByTitle("text");
    assertNotNull(shellProperty);
    assertEquals("SWT", shellProperty.getValue());
    assertTrue(shellProperty.getEditor() instanceof StringPropertyEditor);
    //
    shellInfo.refresh();
    //
    assertNotNull(shellInfo.getImage());
    assertEquals(
        new org.eclipse.swt.graphics.Rectangle(0, 0, 320, 240),
        shellInfo.getImage().getBounds());
    Insets shellInsets = shellInfo.getClientAreaInsets();
    Rectangle shellModelBounds = shellInfo.getModelBounds();
    Rectangle shellBounds = shellInfo.getBounds();
    Rectangle buttonBounds = buttonInfo.getBounds();
    Rectangle buttonModelBounds = buttonInfo.getModelBounds();
    assertNotNull(shellInsets);
    assertNotNull(shellBounds);
    assertNotNull(shellModelBounds);
    assertNotNull(buttonBounds);
    assertNotNull(buttonModelBounds);
    assertEquals(new Dimension(320, 240), shellModelBounds.getSize());
    assertEquals(new Dimension(320, 240), shellBounds.getSize());
    assertEquals(shellInsets.left, buttonBounds.x - buttonModelBounds.x);
    assertEquals(shellInsets.top, buttonBounds.y - buttonModelBounds.y);
    //
    assertNotNull(buttonInfo.getImage());
    assertNotNull(buttonInfo.getClientAreaInsets());
    assertEquals(new Rectangle(10 + shellInsets.left, 20 + shellInsets.top, 50, 30), buttonBounds);
    assertNotNull(buttonInfo.getPreferredSize());
  }

  /**
   * Test that we can parse "SWT Application" pattern.<br>
   * This checks that we can follow "app.open()" local method invocation.
   */
  public void test_application() throws Exception {
    m_waitForAutoBuild = true;
    parseComposite(
        "public class Test {",
        "  private Shell shell;",
        "  public static void main(String[] args) {",
        "    Test app = new Test();",
        "    app.open();",
        "  }",
        "  public void open() {",
        "    Display display = Display.getDefault();",
        "    createContents();",
        "    shell.open();",
        "    shell.layout();",
        "    while (!shell.isDisposed()) {",
        "      if (!display.readAndDispatch()) {",
        "        display.sleep();",
        "      }",
        "    }",
        "  }",
        "  private void createContents() {",
        "    shell = new Shell();",
        "    shell.setSize(450, 300);",
        "    shell.setText('SWT Application');",
        "    shell.setLayout(new GridLayout());",
        "  }",
        "}");
  }

  /**
   * For eSWT we use same {@link Display} for all applications, so we should ignore creation of new
   * {@link Display}'s by custom applications.
   */
  public void test_newDisplay() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Display display = new Display();",
            "    Shell shell = new Shell(display);",
            "  }",
            "}");
    assertEquals(
        "org.eclipse.swt.widgets.Shell",
        shell.getDescription().getComponentClass().getName());
  }

  public void test_classLoader_1() throws Exception {
    test_classLoader(new String[]{
        "class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "  }",
        "}"});
  }

  public void test_classLoader_2() throws Exception {
    test_classLoader(new String[]{
        "class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}"});
  }

  private void test_classLoader(String[] lines) throws Exception {
    CompositeInfo compositeInfo = parseComposite(lines);
    //
    Class<?> swtClass = m_lastLoader.loadClass("org.eclipse.swt.SWT");
    assertNotNull(swtClass);
    //
    assertEquals(swtClass.getField("BORDER").get(null), SwtSupport.getFlag("BORDER"));
    assertEquals(swtClass.getField("BORDER").getInt(null), SwtSupport.getIntFlag("BORDER"));
    assertEquals(swtClass.getField("BORDER").getInt(null), SwtSupport.BORDER);
    //
    Class<?> shellClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Shell");
    assertNotNull(shellClass);
    //
    compositeInfo.refresh();
    Object control = compositeInfo.getObject();
    //
    assertNotNull(ControlSupport.getBounds(control));
    assertNotNull(ControlSupport.toDisplay(control, 0, 0));
    assertNotNull(ControlSupport.getStyle(control));
  }

  /**
   * Test that descriptions for SWT are shared, when needed.
   */
  public void test_sharingDescriptions() throws Exception {
    CompositeInfo shell_1 =
        (CompositeInfo) parseSource(
            "test",
            "Test_1.java",
            getSourceDQ(
                "package test;",
                "public class Test_1 extends org.eclipse.swt.widgets.Shell {",
                "  public Test_1() {",
                "    new org.eclipse.jface.viewers.TableViewer(this, 0);",
                "  }",
                "}"));
    ViewerInfo viewer_1 =
        (ViewerInfo) shell_1.getChildrenControls().get(0).getChildrenJava().get(0);
    CompositeInfo shell_2 =
        (CompositeInfo) parseSource(
            "test",
            "Test_2.java",
            getSourceDQ(
                "package test;",
                "public class Test_2 extends org.eclipse.swt.widgets.Shell {",
                "  public Test_2() {",
                "    new org.eclipse.jface.viewers.TableViewer(this, 0);",
                "  }",
                "}"));
    ViewerInfo viewer_2 =
        (ViewerInfo) shell_2.getChildrenControls().get(0).getChildrenJava().get(0);
    // do checks
    assertSame(shell_1.getDescription(), shell_2.getDescription());
    assertSame(viewer_1.getDescription(), viewer_2.getDescription());
  }

  /**
   * Creation of inner non-static class should be ignored.
   */
  public void test_nonStaticInnerClass() throws Exception {
    m_waitForAutoBuild = true;
    parseSource(
        "test",
        "Test.java",
        getTestSource(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyComposite(this, SWT.NONE);",
            "  }",
            "  private class MyComposite extends Composite {",
            "    public MyComposite(Composite parent, int style) {",
            "      super(parent, style);",
            "    }",
            "  }",
            "}"));
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyComposite(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}");
  }

  /**
   * When we prepare preferred size of control's, we layout inner control's with minimal size. This
   * test checks that after shot we have sizes for controls/layouts that represent required sizes,
   * not minimal ones.
   */
  public void test_preferredBounds_usualBounds() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    setSize(500, 300);",
            "    //",
            "    Composite composite = new Composite(this, SWT.NONE);",
            "    composite.setLayout(new GridLayout());",
            "    //",
            "    Button button = new Button(composite, SWT.NONE);",
            "    button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));",
            "  }",
            "}");
    shell.refresh();
    // check column width
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    GridLayoutInfo gridLayout = (GridLayoutInfo) composite.getLayout();
    int columnWidth = gridLayout.getGridInfo().getColumnIntervals()[0].length;
    assertTrue("More than 400 expected, but " + columnWidth + " found", columnWidth > 400);
  }

  /**
   * Test that {@link Control}-s are disposed.
   */
  public void test_disposeHierarchy() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    Object shellObject = shell.getObject();
    Object buttonObject = button.getObject();
    // not disposed now
    assertFalse(ControlSupport.isDisposed(shellObject));
    assertFalse(ControlSupport.isDisposed(buttonObject));
    // dispose
    disposeLastModel();
    assertTrue(ControlSupport.isDisposed(shellObject));
    assertTrue(ControlSupport.isDisposed(buttonObject));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDefaultValueConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColorDefaultConverter}.
   */
  public void test_ColorConverter() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    shell.refresh();
    Property backgroundProperty = shell.getPropertyByTitle("background");
    // check default value
    {
      Object backgroundValue = backgroundProperty.getValue();
      assertEquals("org.eclipse.swt.graphics.Color", backgroundValue.getClass().getName());
      assertFalse(ColorSupport.isDisposed(backgroundValue));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAbsoluteBounds()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getAbsoluteBounds() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setSize(800, 600);",
            "    //",
            "    Composite composite = new Composite(this, SWT.NONE);",
            "    composite.setBounds(10, 20, 300, 200);",
            "    //",
            "    Button button = new Button(composite, SWT.NONE);",
            "    button.setBounds(5, 10, 100, 50);",
            "  }",
            "}");
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = composite.getChildrenControls().get(0);
    //
    shell.refresh();
    assertEquals(new Rectangle(0, 0, 800, 600), shell.getAbsoluteBounds());
    assertEquals(
        new Rectangle(10 + shell.getClientAreaInsets().left,
            20 + shell.getClientAreaInsets().top,
            300,
            200),
        composite.getAbsoluteBounds());
    assertEquals(
        new Rectangle(5 + composite.getAbsoluteBounds().x,
            10 + composite.getAbsoluteBounds().y,
            100,
            50),
        button.getAbsoluteBounds());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // No variable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that control's without variable still can be created.
   */
  public void test_executeNoVariable() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    shell.refresh();
    assertNotNull(button.getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "expose" action
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_exposeAction() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    // prepare context menu for "button"
    IMenuManager manager = getDesignerMenuManager();
    button.getBroadcastObject().addContextMenu(Collections.singletonList(button), button, manager);
    // check for existing "expose" action
    assertNotNull(findChildAction(manager, "Expose component..."));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_constructorEvaluation_actualOnlyException_placeholder(CompositeInfo shell,
      String exceptionNodeSource) throws Exception {
    check_constructorEvaluation_placeholder(shell);
    check_constructorEvaluation_actualOnlyException(exceptionNodeSource);
  }

  private void check_constructorEvaluation_placeholder(CompositeInfo shell) throws Exception {
    shell.refresh();
    // prepare "MyButton"
    ControlInfo badComponent = shell.getChildrenControls().get(0);
    Object badComponentObject = badComponent.getObject();
    // "MyButton" has placeholder object - Composite
    assertThat(badComponentObject.getClass().getName()).isEqualTo(
        "org.eclipse.swt.widgets.Composite");
    assertTrue(badComponent.isPlaceholder());
    // "shell" has only one Control child (we should remove partially create MyButton instance)
    {
      Object[] children = ContainerSupport.getChildren(shell.getObject());
      assertThat(children).hasSize(1).containsOnly(badComponentObject);
    }
  }

  private void check_constructorEvaluation_actualOnlyException(String exceptionNodeSource) {
    List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(badNodes).hasSize(1);
    {
      BadNodeInformation badNode = badNodes.get(0);
      check_constructorEvaluation_badNode(badNode, exceptionNodeSource, "actual");
    }
  }

  private void check_constructorEvaluation_badNode(BadNodeInformation badNode,
      String exceptionNodeSource,
      String exceptionMessage) {
    ASTNode node = badNode.getNode();
    Throwable nodeException = badNode.getException();
    // check node
    assertEquals(exceptionNodeSource, m_lastEditor.getSource(node));
    // check exception
    {
      Throwable e = DesignerExceptionUtils.getRootCause(nodeException);
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage()).isEqualTo(exceptionMessage);
    }
    // exception should be associated with node
    assertThat(PlaceholderUtils.getExceptions(node)).contains(nodeException);
  }

  /**
   * Actual (is default) constructor throws exception. So, create placeholder.
   */
  public void test_constructorEvaluation_exceptionActual_sameDefault() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(Composite parent, int style) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    MyButton button = new MyButton(this, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyButton(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: button} {/new MyButton(this, SWT.NONE)/}");
    check_constructorEvaluation_actualOnlyException_placeholder(
        shell,
        "new MyButton(this, SWT.NONE)");
  }

  /**
   * Actual constructor throws exception. No default constructor. So, create placeholder.
   */
  public void test_constructorEvaluation_exceptionActual_noDefault() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(Composite parent, int style, int value) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    MyButton button = new MyButton(this, SWT.NONE, 0);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyButton(this, SWT.NONE, 0)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: button} {/new MyButton(this, SWT.NONE, 0)/}");
    check_constructorEvaluation_actualOnlyException_placeholder(
        shell,
        "new MyButton(this, SWT.NONE, 0)");
  }

  /**
   * Actual constructor throws exception. Default constructor throws exception. So, create
   * placeholder.
   */
  public void test_constructorEvaluation_exceptionActual_exceptionDefault() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(Composite parent, int style) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('default');",
            "  }",
            "  public MyButton(Composite parent, int style, int value) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    MyButton button = new MyButton(this, SWT.NONE, 0);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyButton(this, SWT.NONE, 0)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: button} {/new MyButton(this, SWT.NONE, 0)/}");
    // placeholder should be created
    check_constructorEvaluation_placeholder(shell);
    // check logged exceptions
    {
      String exceptionNodeSource = "new MyButton(this, SWT.NONE, 0)";
      List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
      assertThat(badNodes).hasSize(2);
      {
        BadNodeInformation badNode = badNodes.get(0);
        check_constructorEvaluation_badNode(badNode, exceptionNodeSource, "actual");
      }
      {
        BadNodeInformation badNode = badNodes.get(1);
        check_constructorEvaluation_badNode(badNode, exceptionNodeSource, "default");
      }
    }
  }

  /**
   * Exception in actual constructor. So, default constructor is used, successfully.
   */
  public void test_constructorEvaluation_exceptionActual_goodDefault() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(Composite parent, int style) {",
            "    super(parent, style);",
            "    setText('A');",
            "  }",
            "  public MyButton(Composite parent, int style, int value) {",
            "    super(parent, style);",
            "    setText('B');",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    MyButton button = new MyButton(this, SWT.NONE, 0);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyButton(this, SWT.NONE, 0)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: button} {/new MyButton(this, SWT.NONE, 0)/}");
    shell.refresh();
    // prepare MyButton
    ControlInfo button = shell.getChildrenControls().get(0);
    Object buttonObject = button.getObject();
    // MyButton was evaluated using default constructor...
    assertEquals("A", ReflectionUtils.invokeMethod(buttonObject, "getText()"));
    assertFalse(button.isPlaceholder());
    // only one (good) instance of MyButton should be on Shell
    {
      Object[] children = ContainerSupport.getChildren(shell.getObject());
      assertThat(children).hasSize(1).containsOnly(buttonObject);
    }
    // check logged exceptions
    check_constructorEvaluation_actualOnlyException("new MyButton(this, SWT.NONE, 0)");
  }

  /**
   * If placeholder used instead of real object, we can not access its exposed components.
   */
  public void test_constructorEvaluation_placeholder_ignoreExposed() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(Composite parent, int style, int value) {",
            "    super(parent, style);",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "  public Control getExposed() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    MyButton button = new MyButton(this, SWT.NONE, 0);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyButton(this, SWT.NONE, 0)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: button} {/new MyButton(this, SWT.NONE, 0)/}");
  }

  /**
   * If instance of anonymous {@link Control} subclass is created, create instead nearest
   * non-abstract {@link Control} super class.
   */
  public void test_newAnonymousControl() throws Exception {
    setFileContentSrc(
        "test/MyAbstractButton.java",
        getTestSource(
            "public class MyAbstractButton extends Button {",
            "  public MyAbstractButton(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyAbstractButton(this, SWT.NONE) {};",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new MyAbstractButton(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: test.MyAbstractButton} {empty} {/new MyAbstractButton(this, SWT.NONE)/}");
    // refresh
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * If some method invoked two times, visit it only first time.
   */
  public void test_duplicateMethodInvocation() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    createButton(this, 0);",
            "    createButton(this, 1);",
            "  }",
            "  private void createButton(Composite parent, int value) {",
            "    new Button(parent, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/createButton(this, 0)/ /createButton(this, 1)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(parent, SWT.NONE)/}");
    //
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Sometimes (because of unsupported code patterns) "parent" argument becomes <code>null</code>.
   * New should check for this and show specific error message.
   */
  public void test_nullAsParent() throws Exception {
    try {
      parseComposite(
          "public class Test extends Shell {",
          "  public Test() {",
          "    Button button = new Button(null, SWT.NONE);",
          "  }",
          "}");
    } catch (Throwable e) {
      DesignerException de = (DesignerException) DesignerExceptionUtils.getRootCause(e);
      assertEquals(IExceptionConstants.NULL_PARENT, de.getCode());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Unknown parameter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If unknown {@link Composite} passed into method, this should be handled correctly.
   */
  public void test_unknownCompositeParameter() throws Exception {
    parseComposite(
        "public class Test {",
        "  /**",
        "  * @wbp.parser.entryPoint",
        "  */",
        "  public void createContent(Composite parent) {",
        "    Composite composite = new Composite(parent, SWT.NONE);",
        "    new Button(composite, SWT.NONE);",
        "  }",
        "}");
    assertHierarchy(
        "{new: org.eclipse.swt.widgets.Composite} {local-unique: composite} {/new Composite(parent, SWT.NONE)/ /new Button(composite, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Button} {empty} {/new Button(composite, SWT.NONE)/}");
  }

  /**
   * It seems that sometimes users may dispose our shared {@link Shell}. Try to avoid this.
   */
  public void test_disposeSharedShell() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent) {",
            "    super(parent, SWT.NONE);",
            "  }",
            "}");
    composite.refresh();
    // dispose "parent", i.e. shared Shell
    Object shell = ControlSupport.getParent(composite.getObject());
    ControlSupport.dispose(shell);
    // try to refresh again, new Shell should be created and used
    composite.refresh();
  }

  /**
   * Some users try to put some other parameter before standard "parent" and "style".
   */
  public void test_parentNotAsFirstParameter() throws Exception {
    useStrictEvaluationMode(false);
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(int flag, Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    // has "parent" and "style"
    Composite compositeObject = (Composite) composite.getObject();
    assertNotNull(compositeObject.getParent());
    assertEquals(SWT.LEFT_TO_RIGHT, compositeObject.getStyle());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // computerSize() interception
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should not intercept {@link Control#computeSize(int, int, boolean)} and return
   * <code>null</code>.
   */
  public void test_dontIntercept_computeSize_1() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Point computeSize(int wHint, int hHint, boolean changed) {",
            "    if (1 == 2) {",
            "      return new Point(1, 2);",
            "    }",
            "    return super.computeSize(wHint, hHint, changed);",
            "  }",
            "}");
    composite.refresh();
  }

  /**
   * We should not intercept {@link Control#computeSize(int, int)} and return <code>null</code>.
   */
  public void test_dontIntercept_computeSize_2() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Point computeSize(int wHint, int hHint) {",
            "    if (1 == 2) {",
            "      return new Point(1, 2);",
            "    }",
            "    return super.computeSize(wHint, hHint);",
            "  }",
            "}");
    composite.refresh();
  }
}