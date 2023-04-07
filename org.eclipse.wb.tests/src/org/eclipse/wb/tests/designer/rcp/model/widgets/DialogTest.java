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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.rcp.model.widgets.DialogInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Test for {@link DialogInfo}.
 *
 * @author scheglov_ke
 */
public class DialogTest extends RcpModelTest {
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
  public void test_parse() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  protected Object result;",
            "  protected Shell shell;",
            "  public Test(Shell parent) {",
            "    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);",
            "    setText('SWT Dialog');",
            "  }",
            "  public Object open() {",
            "    createContents();",
            "    shell.open();",
            "    shell.layout();",
            "    Display display = getParent().getDisplay();",
            "    while (!shell.isDisposed()) {",
            "      if (!display.readAndDispatch()) {",
            "        display.sleep();",
            "      }",
            "    }",
            "    return result;",
            "  }",
            "  private void createContents() {",
            "    shell = new Shell(getParent(), getStyle());",
            "    shell.setSize(450, 300);",
            "    shell.setText(getText());",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Dialog_} {this} {/setText('SWT Dialog')/ /new Shell(getParent(), getStyle())/ /new Shell(getParent(), getStyle())/ /shell.setText(getText())/}",
        "  {new: org.eclipse.swt.widgets.Shell} {field-unique: shell} {/new Shell(getParent(), getStyle())/ /shell.setSize(450, 300)/ /shell.setText(getText())/ /shell.open()/ /shell.layout()/}",
        "    {implicit-layout: absolute} {implicit-layout} {}");
    // we implement "org.eclipse.swt.widgets.Dialog" as non-abstract "org.eclipse.swt.widgets.Dialog_"
    // that should not be cached, else it will hold project/composite ClassLoader in memory
    assertFalse(dialog.getDescription().isCached());
    // refresh()
    dialog.refresh();
    assertNotNull(dialog.getImage());
    // bounds
    {
      Rectangle bounds = dialog.getBounds();
      // x & y coordinates are platform-specific
      // assertEquals(new Rectangle(-10000, -10000, 450, 300), bounds);
      assertEquals(bounds.width, 450);
      assertEquals(bounds.height, 300);
    }
    {
      ShellInfo shell = getJavaInfoByName("shell");
      Rectangle bounds = shell.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
  }

  public void test_parse_RightToLeft() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  protected Shell shell;",
            "  public Test(Shell parent) {",
            "    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RIGHT_TO_LEFT);",
            "    setText('SWT Dialog');",
            "  }",
            "  public Object open() {",
            "    createContents();",
            "    return null;",
            "  }",
            "  private void createContents() {",
            "    shell = new Shell(getParent(), getStyle());",
            "    shell.setSize(450, 300);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Dialog_} {this} {/setText('SWT Dialog')/ /new Shell(getParent(), getStyle())/ /new Shell(getParent(), getStyle())/}",
        "  {new: org.eclipse.swt.widgets.Shell} {field-unique: shell} {/new Shell(getParent(), getStyle())/ /shell.setSize(450, 300)/}",
        "    {implicit-layout: absolute} {implicit-layout} {}");
    dialog.refresh();
    // bounds
    {
      Rectangle bounds = dialog.getBounds();
      // x & y coordinates are platform-specific
      // assertEquals(new Rectangle(-10000, -10000, 450, 300), bounds);
      assertEquals(bounds.width, 450);
      assertEquals(bounds.height, 300);
    }
    {
      ShellInfo shell = getJavaInfoByName("shell");
      Rectangle bounds = shell.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
  }

  public void test_constructorWithStyle() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  protected Object result;",
            "  protected Shell shell;",
            "  public Test(Shell parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Object open() {",
            "    shell = new Shell(getParent(), getStyle());",
            "    return result;",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Dialog_} {this} {/new Shell(getParent(), getStyle())/ /new Shell(getParent(), getStyle())/}",
        "  {new: org.eclipse.swt.widgets.Shell} {field-unique: shell} {/new Shell(getParent(), getStyle())/}",
        "    {implicit-layout: absolute} {implicit-layout} {}");
    // refresh()
    dialog.refresh();
    assertNotNull(dialog.getImage());
    // check style
    assertNotNull(PropertyUtils.getByPath(dialog, "Constructor/arg1"));
  }

  /**
   * If several constructors, than <code>Shell,style</code> should be selected.
   */
  public void test_severalConstructors() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  protected Shell shell;",
            "  public Test(Shell parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Test(Shell parent) {",
            "    this(parent, SWT.NONE);",
            "  }",
            "  public Object open() {",
            "    shell = new Shell(getParent(), getStyle());",
            "    return null;",
            "  }",
            "}");
    dialog.refresh();
    assertNoErrors(dialog);
  }

  public void test_noOpenMethod() throws Exception {
    try {
      parseJavaInfo(
          "public class Test extends Dialog {",
          "  protected Shell shell;",
          "  public Test(Shell parent, int style) {",
          "    super(parent, style);",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.SWT_DIALOG_NO_OPEN_METHOD, de.getCode());
    }
  }

  /**
   * We should try to find "open()" method with any number of parameters.
   */
  public void test_openMethodWithParameters() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  protected Shell shell;",
            "  public Test(Shell parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Object open(String msg) {",
            "    shell = new Shell(getParent(), getStyle());",
            "    return null;",
            "  }",
            "}");
    dialog.refresh();
    assertNoErrors(dialog);
  }

  /**
   * We should ignore "new MyDialog()" creations.
   * <p>
   * 40337: Parse failure when using FileDialog
   */
  public void test_ignoreNewInstance() throws Exception {
    CompositeInfo shell =
        parseJavaInfo(
            "public class Test {",
            "  public static void main (String[] args) {",
            "    Shell shell = new Shell();",
            "    FileDialog dialog = new FileDialog (shell, SWT.SAVE);",
            "  }",
            "}");
    assertHierarchy(
        "{new: org.eclipse.swt.widgets.Shell} {local-unique: shell} {/new Shell()/ /new FileDialog (shell, SWT.SAVE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * In the past we detected "style" parameter just as 1-th parameter of {@link Dialog} constructor.
   * However custom {@link Dialog}-s may use 1-th parameter for different parameter, not just for
   * "style".
   */
  public void test_betterStyleParameterDetection() throws Exception {
    setFileContentSrc(
        "test/MyData.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyData {",
            "  // filler filler filler",
            "}"));
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, MyData data, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  protected MyData m_data;",
            "  protected Shell shell;",
            "  public Test(Shell parent, MyData data, int style) {",
            "    super(parent, style);",
            "    m_data = data;",
            "  }",
            "  public Object open() {",
            "    shell = new Shell(getParent(), getStyle());",
            "    new MyComposite(shell, m_data, SWT.NONE);",
            "    return null;",
            "  }",
            "}");
    dialog.refresh();
    assertNoErrors(dialog);
  }

  /**
   * One user tried to parse {@link Dialog} without main {@link Shell} creation.
   */
  public void test_noMainShell() throws Exception {
    try {
      parseJavaInfo(
          "public class Test extends Dialog {",
          "  protected Object result;",
          "  protected Shell shell;",
          "  public Test(Shell parent) {",
          "    super(parent, 0);",
          "  }",
          "  public Object open() {",
          "    shell.open();",
          "    shell.layout();",
          "    return result;",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.SWT_DIALOG_NO_MAIN_SHELL, de.getCode());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_setMinimalSize() throws Exception {
    parseJavaInfo(
        "public class Test extends Dialog {",
        "  protected Shell shell;",
        "  public Test(Shell parent) {",
        "    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);",
        "    setText('SWT Dialog');",
        "  }",
        "  public Object open() {",
        "    createContents();",
        "    return null;",
        "  }",
        "  private void createContents() {",
        "    shell = new Shell(getParent(), getStyle());",
        "    shell.setSize(450, 300);",
        "  }",
        "}");
    refresh();
    CompositeInfo shell = getJavaInfoByName("shell");
    Dimension preferredSize = shell.getPreferredSize().getCopy();
    // run "Set minimal size" action
    {
      IMenuManager contextMenu = getContextMenu(shell);
      IAction action = findChildAction(contextMenu, "Set minimal size, as after pack()");
      assertNotNull(action);
      action.run();
    }
    assertEditor(
        "public class Test extends Dialog {",
        "  protected Shell shell;",
        "  public Test(Shell parent) {",
        "    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);",
        "    setText('SWT Dialog');",
        "  }",
        "  public Object open() {",
        "    createContents();",
        "    return null;",
        "  }",
        "  private void createContents() {",
        "    shell = new Shell(getParent(), getStyle());",
        "    shell.setSize(" + preferredSize.width + ", " + preferredSize.height + ");",
        "  }",
        "}");
  }

  public void test_contextMenu_removeSize() throws Exception {
    parseJavaInfo(
        "public class Test extends Dialog {",
        "  protected Shell shell;",
        "  public Test(Shell parent) {",
        "    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);",
        "    setText('SWT Dialog');",
        "  }",
        "  public Object open() {",
        "    createContents();",
        "    return null;",
        "  }",
        "  private void createContents() {",
        "    shell = new Shell(getParent(), getStyle());",
        "    shell.setSize(450, 300);",
        "  }",
        "}");
    refresh();
    CompositeInfo shell = getJavaInfoByName("shell");
    // run "Set minimal size" action
    {
      IMenuManager contextMenu = getContextMenu(shell);
      IAction action = findChildAction(contextMenu, "Remove setSize()");
      assertNotNull(action);
      action.run();
    }
    assertEditor(
        "public class Test extends Dialog {",
        "  protected Shell shell;",
        "  public Test(Shell parent) {",
        "    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);",
        "    setText('SWT Dialog');",
        "  }",
        "  public Object open() {",
        "    createContents();",
        "    return null;",
        "  }",
        "  private void createContents() {",
        "    shell = new Shell(getParent(), getStyle());",
        "  }",
        "}");
  }
}