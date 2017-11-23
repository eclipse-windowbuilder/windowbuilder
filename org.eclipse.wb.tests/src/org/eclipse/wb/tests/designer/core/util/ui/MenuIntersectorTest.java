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
package org.eclipse.wb.tests.designer.core.util.ui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ui.IActionSingleton;
import org.eclipse.wb.internal.core.utils.ui.MenuIntersector;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link MenuIntersector}.
 * 
 * @author lobas_av
 */
public class MenuIntersectorTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test() throws Exception {
    // create common action
    Action deleteAction = new Action("Delete") {
    };
    Action copyAction = new Action("Copy") {
    };
    Action pasteAction = new Action("Paste") {
    };
    // create menus
    IMenuManager manager1 = createMenu(deleteAction, copyAction, pasteAction);
    fillMenu1(manager1);
    IMenuManager manager2 = createMenu(deleteAction, copyAction, pasteAction);
    fillMenu2(manager2);
    // add to list
    List<IMenuManager> menus = Lists.newArrayList();
    menus.add(manager1);
    menus.add(manager2);
    // merge
    IMenuManager main = new MenuManager();
    MenuIntersector.merge(main, menus);
    // assert!
    IContributionItem[] items = main.getItems();
    assertEquals(8, items.length);
    //
    assertInstanceOf(ActionContributionItem.class, items[0]);
    assertSame(copyAction.getText(), ((ActionContributionItem) items[0]).getAction().getText());
    //
    assertInstanceOf(ActionContributionItem.class, items[1]);
    assertSame(pasteAction.getText(), ((ActionContributionItem) items[1]).getAction().getText());
    //
    assertTrue(items[2].isSeparator());
    //
    assertInstanceOf(ActionContributionItem.class, items[3]);
    assertSame(deleteAction.getText(), ((ActionContributionItem) items[3]).getAction().getText());
    //
    assertTrue(items[4].isSeparator());
    //
    assertInstanceOf(ActionContributionItem.class, items[5]);
    assertEquals("Test", ((ActionContributionItem) items[5]).getAction().getText());
    //
    assertInstanceOf(MenuManager.class, items[6]);
    MenuManager sub = (MenuManager) items[6];
    assertEquals("layout", sub.getMenuText());
    //
    IContributionItem[] subItems = sub.getItems();
    assertEquals(1, subItems.length);
    assertEquals("clear", ((ActionContributionItem) subItems[0]).getAction().getText());
  }

  public void test_2() throws Exception {
    // create common action
    Action deleteAction = new Action("Delete") {
    };
    Action copyAction = new Action("Copy") {
    };
    Action pasteAction = new Action("Paste") {
    };
    // create menus
    IMenuManager manager1 = createMenu(deleteAction, copyAction, pasteAction);
    fillMenu1(manager1);
    IMenuManager manager2 = createMenu(deleteAction, copyAction, pasteAction);
    fillMenu2(manager2);
    IMenuManager manager3 = createMenu(deleteAction, copyAction, pasteAction);
    // add to list
    List<IMenuManager> menus = Lists.newArrayList();
    menus.add(manager1);
    menus.add(manager2);
    menus.add(manager3);
    // merge
    IMenuManager main = new MenuManager();
    MenuIntersector.merge(main, menus);
    // assert!
    IContributionItem[] items = main.getItems();
    assertEquals(6, items.length);
    //
    assertInstanceOf(ActionContributionItem.class, items[0]);
    assertSame(copyAction.getText(), ((ActionContributionItem) items[0]).getAction().getText());
    //
    assertInstanceOf(ActionContributionItem.class, items[1]);
    assertSame(pasteAction.getText(), ((ActionContributionItem) items[1]).getAction().getText());
    //
    assertTrue(items[2].isSeparator());
    //
    assertInstanceOf(ActionContributionItem.class, items[3]);
    assertSame(deleteAction.getText(), ((ActionContributionItem) items[3]).getAction().getText());
  }

  /**
   * Test for running {@link IAction}'s after intersection.
   */
  public void test_runWithEvent_run() throws Exception {
    // create actions
    final boolean[] runExecuted_1 = new boolean[1];
    final boolean[] runExecuted_2 = new boolean[1];
    Action action_1 = new Action("The action") {
      @Override
      public void run() {
        runExecuted_1[0] = true;
      }
    };
    Action action_2 = new Action("The action") {
      @Override
      public void run() {
        runExecuted_2[0] = true;
      }
    };
    // create menus
    IMenuManager manager_1 = new MenuManager();
    IMenuManager manager_2 = new MenuManager();
    manager_1.add(action_1);
    manager_2.add(action_2);
    // merge
    IMenuManager main = new MenuManager();
    MenuIntersector.merge(main, ImmutableList.of(manager_1, manager_2));
    // prepare single IAction that wraps two IAction's
    IAction wrapperAction;
    {
      IContributionItem[] items = main.getItems();
      assertThat(items).hasSize(1);
      wrapperAction = ((ActionContributionItem) items[0]).getAction();
    }
    // execute IAction using runWithEvent()
    {
      runExecuted_1[0] = false;
      runExecuted_2[0] = false;
      wrapperAction.runWithEvent(null);
      assertTrue("action_1 executed", runExecuted_1[0]);
      assertTrue("action_2 executed", runExecuted_2[0]);
    }
    // execute IAction using run()
    {
      runExecuted_1[0] = false;
      runExecuted_2[0] = false;
      wrapperAction.run();
      assertTrue("action_1 executed", runExecuted_1[0]);
      assertTrue("action_2 executed", runExecuted_2[0]);
    }
  }

  /**
   * Test that {@link IAction} with {@link IActionSingleton} executed only for first action.
   */
  public void test_IActionSingleton() throws Exception {
    class MyAction extends Action implements IActionSingleton {
      private final boolean[] m_runFlag;

      public MyAction(String text, boolean[] runFlag) {
        super(text);
        m_runFlag = runFlag;
      }

      @Override
      public void run() {
        m_runFlag[0] = true;
      }
    }
    // create actions
    final boolean[] runExecuted_1 = new boolean[1];
    final boolean[] runExecuted_2 = new boolean[1];
    IAction action_1 = new MyAction("The action", runExecuted_1);
    IAction action_2 = new MyAction("The action", runExecuted_2);
    // create menus
    IMenuManager manager_1 = new MenuManager();
    IMenuManager manager_2 = new MenuManager();
    manager_1.add(action_1);
    manager_2.add(action_2);
    // merge
    IMenuManager main = new MenuManager();
    MenuIntersector.merge(main, ImmutableList.of(manager_1, manager_2));
    // prepare single IAction that wraps two IAction's
    IAction wrapperAction;
    {
      IContributionItem[] items = main.getItems();
      assertThat(items).hasSize(1);
      wrapperAction = ((ActionContributionItem) items[0]).getAction();
    }
    // execute IAction using run()
    {
      runExecuted_1[0] = false;
      runExecuted_2[0] = false;
      wrapperAction.run();
      assertTrue("action_1 executed", runExecuted_1[0]);
      assertFalse("action_2 executed", runExecuted_2[0]);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IMenuManager createMenu(Action deleteAction, Action copyAction, Action pasteAction) {
    IMenuManager manager = new MenuManager();
    manager.add(copyAction);
    manager.add(pasteAction);
    manager.add(new Separator());
    manager.add(deleteAction);
    manager.add(new Separator());
    return manager;
  }

  private static void fillMenu1(IMenuManager manager) {
    manager.add(new Action("Test") {
    });
    MenuManager sub = new MenuManager("layout");
    manager.add(sub);
    sub.add(new Action("clear") {
    });
    sub.add(new Action("default") {
    });
    manager.add(new Separator());
    manager.add(new Action("Default") {
    });
  }

  private static void fillMenu2(IMenuManager manager) {
    manager.add(new Action("Test") {
    });
    MenuManager sub = new MenuManager("layout");
    manager.add(sub);
    sub.add(new Action("clear") {
    });
    sub.add(new Action("Default") {
    });
    manager.add(new Separator());
    MenuManager sub2 = new MenuManager("Old");
    manager.add(sub2);
    sub2.add(new Action("fill") {
    });
  }
}