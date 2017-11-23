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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link ApplicationWindowInfo}.
 * 
 * @author scheglov_ke
 */
public class ApplicationWindowTest extends RcpModelTest {
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
   * Just parsing for some {@link ApplicationWindow}.
   */
  public void test_0() throws Exception {
    parseJavaInfo(
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "  }",
        "  protected Control createContents(Composite parent) {",
        "    Composite container = (Composite) super.createContents(parent);",
        "    Button button = new Button(container, SWT.NONE);",
        "    return container;",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {}",
        "  {parameter} {parent} {/super.createContents(parent)/}",
        "    {casted-superInvocation: (Composite)super.createContents(parent)} {local-unique: container} {/(Composite) super.createContents(parent)/ /new Button(container, SWT.NONE)/ /container/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "      {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(container, SWT.NONE)/}");
  }

  /**
   * Parse {@link ApplicationWindow} without references on SWT classes.
   */
  public void test_1() throws Exception {
    ApplicationWindowInfo window =
        (ApplicationWindowInfo) parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "import org.eclipse.jface.action.*;",
                "import org.eclipse.jface.window.*;",
                "public class Test extends ApplicationWindow {",
                "  public Test() {",
                "    super(null);",
                "  }",
                "}"));
    assertNoErrors(window);
  }

  /**
   * Method {@link Window#close()} is dangerous, it may cause lock up.
   */
  public void test_ignoreMethod_close() throws Exception {
    ApplicationWindowInfo window =
        (ApplicationWindowInfo) parseSource(
            "test",
            "Test.java",
            getSource(
                "import org.eclipse.jface.action.*;",
                "import org.eclipse.jface.window.*;",
                "public class Test extends ApplicationWindow {",
                "  public Test() {",
                "    super(null);",
                "  }",
                "  public boolean close() {",
                "    return false;",
                "  }",
                "}"));
    window.refresh();
    // ask close()
    Shell shell = (Shell) window.getComponentObject();
    ReflectionUtils.invokeMethod(window.getObject(), "close()");
    assertTrue(shell.isDisposed());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IContributionManager's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IToolBarManager}.
   */
  public void test_managers_ToolBarManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test() {",
            "    super(null);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolBarManager = super.createToolBarManager(style);",
            "    return toolBarManager;",
            "  }",
            "}");
    window.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addToolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createToolBarManager(style)} {local-unique: toolBarManager} {/super.createToolBarManager(style)/ /toolBarManager/}");
    // check ToolBarManager
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    assertEquals(
        "org.eclipse.jface.action.ToolBarManager",
        toolBarManager.getObject().getClass().getName());
    assertEquals(
        "org.eclipse.swt.widgets.ToolBar",
        toolBarManager.getComponentObject().getClass().getName());
    assertNotNull(toolBarManager.getImage());
    assertNotNull(toolBarManager.getBounds());
    assertThat(toolBarManager.getBounds().width).isGreaterThan(400);
    assertThat(toolBarManager.getBounds().height).isGreaterThan(20);
  }

  /**
   * Dangling {@link MenuManager} should not cause exception. We should bind
   * {@link IContributionManager} only if it is returned.
   */
  public void test_managers_ignoreDangling() throws Exception {
    parseJavaInfo(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test() {",
        "    super(null);",
        "    addMenuBar();",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = new MenuManager();",
        "    MenuManager dangling = new MenuManager();",
        "    return menuManager;",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addMenuBar()/}",
        "  {new: org.eclipse.jface.action.MenuManager} {local-unique: menuManager} {/new MenuManager()/ /menuManager/}");
    refresh();
  }
}