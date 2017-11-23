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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

/**
 * Tests for {@link OrderingSupport} and {@link AbsoluteLayoutInfo}.
 * 
 * @author mitin_aa
 */
public class AbsoluteLayoutOrderingTest extends RcpModelTest {
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
   * Test that "ordering" actions are present and have expected enablement state.
   */
  public void test_enablement() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "      Button button = new Button(this, SWT.NONE);",
            "      Text text = new Text(this, SWT.NONE);",
            "      Table table = new Table(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo text = shell.getChildrenControls().get(1);
    ControlInfo table = shell.getChildrenControls().get(2);
    // check actions for first: button
    {
      IMenuManager orderManager = createOrderManager(button);
      assertNotNull(orderManager);
      checkOrderAction(orderManager, "Bring to Front", false);
      checkOrderAction(orderManager, "Send to Back", true);
      checkOrderAction(orderManager, "Bring Forward", false);
      checkOrderAction(orderManager, "Send Backward", true);
    }
    // check actions for middle: text
    {
      IMenuManager orderManager = createOrderManager(text);
      assertNotNull(orderManager);
      checkOrderAction(orderManager, "Bring to Front", true);
      checkOrderAction(orderManager, "Send to Back", true);
      checkOrderAction(orderManager, "Bring Forward", true);
      checkOrderAction(orderManager, "Send Backward", true);
    }
    // check actions for last: table
    {
      IMenuManager orderManager = createOrderManager(table);
      assertNotNull(orderManager);
      checkOrderAction(orderManager, "Bring to Front", true);
      checkOrderAction(orderManager, "Send to Back", false);
      checkOrderAction(orderManager, "Bring Forward", true);
      checkOrderAction(orderManager, "Send Backward", false);
    }
  }

  public void test_bringToFront() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "      Button button = new Button(this, SWT.NONE);",
            "      Text text = new Text(this, SWT.NONE);",
            "      Table table = new Table(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo text = shell.getChildrenControls().get(1);
    ControlInfo table = shell.getChildrenControls().get(2);
    // run action
    getOrderAction(createOrderManager(table), "Bring to Front").run();
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "      Table table = new Table(this, SWT.NONE);",
        "      Button button = new Button(this, SWT.NONE);",
        "      Text text = new Text(this, SWT.NONE);",
        "  }",
        "}");
    assertThat(shell.getChildrenControls()).containsExactly(table, button, text);
  }

  public void test_bringForward() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "      Button button = new Button(this, SWT.NONE);",
            "      Text text = new Text(this, SWT.NONE);",
            "      Table table = new Table(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo text = shell.getChildrenControls().get(1);
    ControlInfo table = shell.getChildrenControls().get(2);
    // run action
    getOrderAction(createOrderManager(table), "Bring Forward").run();
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "      Button button = new Button(this, SWT.NONE);",
        "      Table table = new Table(this, SWT.NONE);",
        "      Text text = new Text(this, SWT.NONE);",
        "  }",
        "}");
    assertThat(shell.getChildrenControls()).containsExactly(button, table, text);
  }

  public void test_sendToBack() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "      Button button = new Button(this, SWT.NONE);",
            "      Text text = new Text(this, SWT.NONE);",
            "      Table table = new Table(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo text = shell.getChildrenControls().get(1);
    ControlInfo table = shell.getChildrenControls().get(2);
    // run action
    getOrderAction(createOrderManager(button), "Send to Back").run();
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "      Text text = new Text(this, SWT.NONE);",
        "      Table table = new Table(this, SWT.NONE);",
        "      Button button = new Button(this, SWT.NONE);",
        "  }",
        "}");
    assertThat(shell.getChildrenControls()).containsExactly(text, table, button);
  }

  public void test_sendBackward() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(null);",
            "      Button button = new Button(this, SWT.NONE);",
            "      Text text = new Text(this, SWT.NONE);",
            "      Table table = new Table(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo text = shell.getChildrenControls().get(1);
    ControlInfo table = shell.getChildrenControls().get(2);
    // run action
    getOrderAction(createOrderManager(button), "Send Backward").run();
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "      Text text = new Text(this, SWT.NONE);",
        "      Button button = new Button(this, SWT.NONE);",
        "      Table table = new Table(this, SWT.NONE);",
        "  }",
        "}");
    assertThat(shell.getChildrenControls()).containsExactly(text, button, table);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "order" {@link IMenuManager} for single {@link ControlInfo}.
   */
  private static IMenuManager createOrderManager(ControlInfo control) throws Exception {
    // prepare context menu for "control"
    IMenuManager manager = getDesignerMenuManager();
    control.getBroadcastObject().addContextMenu(
        Collections.singletonList(control),
        control,
        manager);
    // select "order" sub-menu
    return findChildMenuManager(manager, "Order");
  }

  /**
   * @return the "order" action with given text.
   */
  private static IAction getOrderAction(IMenuManager orderManager, String text) {
    return findChildAction(orderManager, text);
  }

  /**
   * Checks that there are "order" action with given text and enablement state.
   */
  private static void checkOrderAction(IMenuManager orderManager, String text, boolean enabled) {
    IAction action = getOrderAction(orderManager, text);
    assertNotNull("Can not find action: " + text, action);
    assertEquals(enabled, action.isEnabled());
  }
}