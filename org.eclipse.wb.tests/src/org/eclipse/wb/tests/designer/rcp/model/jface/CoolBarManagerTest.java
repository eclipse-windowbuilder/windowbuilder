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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.CoolBarManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link CoolBarManagerInfo}.
 * 
 * @author scheglov_ke
 */
public class CoolBarManagerTest extends RcpModelTest {
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
   * Empty {@link CoolBarManager}.
   */
  public void test_0_emptyCoolBarManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test() {",
            "    super(null);",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
            "    return coolBarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addCoolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createCoolBarManager(style)} {local-unique: coolBarManager} {/super.createCoolBarManager(style)/ /coolBarManager/}");
    // check CoolBarManager
    CoolBarManagerInfo coolBarManager = window.getChildren(CoolBarManagerInfo.class).get(0);
    assertEquals(
        "org.eclipse.jface.action.CoolBarManager",
        coolBarManager.getObject().getClass().getName());
    assertEquals(
        "org.eclipse.swt.widgets.CoolBar",
        coolBarManager.getComponentObject().getClass().getName());
    assertNotNull(coolBarManager.getImage());
    assertNotNull(coolBarManager.getBounds());
    assertThat(coolBarManager.getBounds().width).isGreaterThan(400);
    assertThat(coolBarManager.getBounds().height).isGreaterThan(20);
  }

  /**
   * Test for using {@link ICoolBarManager#add(IToolBarManager)}, empty {@link ToolBarManager}.
   */
  public void test_addToolBarManager_empty() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test() {",
            "    super(null);",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
            "    {",
            "      ToolBarManager toolBarManager = new ToolBarManager();",
            "      coolBarManager.add(toolBarManager);",
            "    }",
            "    return coolBarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addCoolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createCoolBarManager(style)} {local-unique: coolBarManager} {/super.createCoolBarManager(style)/ /coolBarManager.add(toolBarManager)/ /coolBarManager/}",
        "    {new: org.eclipse.jface.action.ToolBarManager} {local-unique: toolBarManager} {/new ToolBarManager()/ /coolBarManager.add(toolBarManager)/}");
    CoolBarManagerInfo coolBarManager = window.getChildren(CoolBarManagerInfo.class).get(0);
    ToolBarManagerInfo toolBarManager =
        (ToolBarManagerInfo) coolBarManager.getChildrenJava().get(0);
    // refresh(), check "toolBarManager"
    window.refresh();
    assertThat(toolBarManager.getBounds().width).isGreaterThan(50);
    assertThat(toolBarManager.getBounds().height).isGreaterThan(20);
    // delete "toolBarManager"
    assertTrue(toolBarManager.canDelete());
    toolBarManager.delete();
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test() {",
        "    super(null);",
        "    addCoolBar(SWT.FLAT);",
        "  }",
        "  protected CoolBarManager createCoolBarManager(int style) {",
        "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
        "    return coolBarManager;",
        "  }",
        "}");
  }

  /**
   * Test for using {@link ICoolBarManager#add(IToolBarManager)}, not empty {@link ToolBarManager}.
   */
  public void test_addToolBarManager_notEmpty() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test() {",
            "    super(null);",
            "    createActions();",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    m_action = new Action('Some text') {",
            "    };",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
            "    {",
            "      ToolBarManager toolBarManager = new ToolBarManager();",
            "      coolBarManager.add(toolBarManager);",
            "      toolBarManager.add(m_action);",
            "    }",
            "    return coolBarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addCoolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createCoolBarManager(style)} {local-unique: coolBarManager} {/super.createCoolBarManager(style)/ /coolBarManager.add(toolBarManager)/ /coolBarManager/}",
        "    {new: org.eclipse.jface.action.ToolBarManager} {local-unique: toolBarManager} {/new ToolBarManager()/ /coolBarManager.add(toolBarManager)/ /toolBarManager.add(m_action)/}",
        "      {void} {void} {/toolBarManager.add(m_action)/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: m_action} {/new Action('Some text')/ /toolBarManager.add(m_action)/}");
    CoolBarManagerInfo coolBarManager = window.getChildren(CoolBarManagerInfo.class).get(0);
    ToolBarManagerInfo toolBarManager =
        (ToolBarManagerInfo) coolBarManager.getChildrenJava().get(0);
    ActionContributionItemInfo contributionItem =
        (ActionContributionItemInfo) toolBarManager.getItems().get(0);
    // refresh(), check "contributionItem"
    window.refresh();
    // check "toolBarManager"
    {
      assertThat(toolBarManager.getBounds().width).isGreaterThan(50);
      assertThat(toolBarManager.getBounds().height).isGreaterThan(20);
    }
    // refresh(), check "contributionItem"
    {
      Rectangle bounds = contributionItem.getBounds();
      assertThat(bounds.width).isGreaterThan(50);
      assertThat(bounds.height).isGreaterThan(20);
      assertThat(bounds.x).isGreaterThan(0);
      assertThat(bounds.y).isEqualTo(0);
    }
  }

  /**
   * Test for using {@link ToolBarContributionItem}.
   */
  public void test_addToolBarManager_usingToolBarContributionItem() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test() {",
            "    super(null);",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
            "    {",
            "      ToolBarManager toolBarManager = new ToolBarManager();",
            "      coolBarManager.add(new ToolBarContributionItem(toolBarManager, 'main'));",
            "    }",
            "    return coolBarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addCoolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createCoolBarManager(style)} {local-unique: coolBarManager} {/super.createCoolBarManager(style)/ /coolBarManager.add(new ToolBarContributionItem(toolBarManager, 'main'))/ /coolBarManager/}",
        "    {new: org.eclipse.jface.action.ToolBarContributionItem} {empty} {/coolBarManager.add(new ToolBarContributionItem(toolBarManager, 'main'))/}",
        "      {new: org.eclipse.jface.action.ToolBarManager} {local-unique: toolBarManager} {/new ToolBarManager()/ /new ToolBarContributionItem(toolBarManager, 'main')/}");
    CoolBarManagerInfo coolBarManager = window.getChildren(CoolBarManagerInfo.class).get(0);
    ContributionItemInfo contributionItem = (ContributionItemInfo) coolBarManager.getItems().get(0);
    ToolBarManagerInfo toolBarManager =
        (ToolBarManagerInfo) contributionItem.getChildrenJava().get(0);
    // refresh(), check "toolBarManager"
    window.refresh();
    // check bounds
    {
      Rectangle bounds = contributionItem.getBounds();
      assertThat(bounds.x).isGreaterThan(5);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(400);
      assertThat(bounds.height).isGreaterThan(20);
    }
    {
      Rectangle bounds = toolBarManager.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(400);
      assertThat(bounds.height).isGreaterThan(20);
    }
    // "toolBarManager" can not be deleted
    {
      assertFalse(toolBarManager.canDelete());
      assertFalse(JavaInfoUtils.canReparent(toolBarManager));
    }
    // delete "ToolBarContributionItem"
    {
      assertTrue(contributionItem.canDelete());
      contributionItem.delete();
    }
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test() {",
        "    super(null);",
        "    addCoolBar(SWT.FLAT);",
        "  }",
        "  protected CoolBarManager createCoolBarManager(int style) {",
        "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
        "    return coolBarManager;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Drop {@link ToolBarManager} on {@link CoolBarManager}.
   */
  public void test_CREATE() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test() {",
            "    super(null);",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
            "    return coolBarManager;",
            "  }",
            "}");
    window.refresh();
    CoolBarManagerInfo coolBarManager = window.getChildren(CoolBarManagerInfo.class).get(0);
    // add new ToolBarManager_Info
    ToolBarManagerInfo newManager = createJavaInfo("org.eclipse.jface.action.ToolBarManager");
    coolBarManager.command_CREATE(newManager, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test() {",
        "    super(null);",
        "    addCoolBar(SWT.FLAT);",
        "  }",
        "  protected CoolBarManager createCoolBarManager(int style) {",
        "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
        "    {",
        "      ToolBarManager toolBarManager = new ToolBarManager();",
        "      coolBarManager.add(toolBarManager);",
        "    }",
        "    return coolBarManager;",
        "  }",
        "}");
  }
}