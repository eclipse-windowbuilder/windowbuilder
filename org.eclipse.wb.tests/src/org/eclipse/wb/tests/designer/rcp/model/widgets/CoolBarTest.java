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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.widgets.CoolBarInfo;
import org.eclipse.wb.internal.rcp.model.widgets.CoolItemInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Test for {@link CoolBar}.
 * 
 * @author scheglov_ke
 */
public class CoolBarTest extends RcpModelTest {
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
   * Test for {@link CoolBarInfo#isHorizontal()}.
   */
  public void test_isHorizontal() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar toolBar_1 = new CoolBar(this, SWT.NONE);",
            "    CoolBar toolBar_2 = new CoolBar(this, SWT.VERTICAL);",
            "  }",
            "}");
    shell.refresh();
    {
      CoolBarInfo toolBar_1 = shell.getChildren(CoolBarInfo.class).get(0);
      assertTrue(toolBar_1.isHorizontal());
      assertTrue(getFlowContainer(toolBar_1).isHorizontal());
    }
    {
      CoolBarInfo toolBar_2 = shell.getChildren(CoolBarInfo.class).get(1);
      assertFalse(toolBar_2.isHorizontal());
      assertFalse(getFlowContainer(toolBar_2).isHorizontal());
    }
  }

  /**
   * {@link CoolBar} with {@link CoolItem}'s.
   */
  public void test_parseItems() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "      {",
            "        CoolItem item = new CoolItem(coolBar, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        CoolItem item = new CoolItem(coolBar, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    // check items
    List<CoolItemInfo> items = coolBar.getItems();
    assertEquals(2, items.size());
    CoolItemInfo item_0 = items.get(0);
    CoolItemInfo item_1 = items.get(1);
    // text
    assertEquals("000", ReflectionUtils.invokeMethod2(item_0.getObject(), "getText"));
    assertEquals("111", ReflectionUtils.invokeMethod2(item_1.getObject(), "getText"));
    // bounds
    {
      Rectangle modelBounds = item_0.getModelBounds();
      assertThat(modelBounds.width).isGreaterThan(15);
      assertThat(modelBounds.height).isGreaterThan(20);
    }
    // no setControl() invocations
    assertNull(item_0.getControl());
    Assertions.assertThat(item_0.getPresentation().getChildrenTree()).containsOnly();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setControl()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CoolItem#setControl(org.eclipse.swt.widgets.Control)}.
   */
  public void test_setControl_get() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    {",
            "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
            "      {",
            "        Button button = new Button(coolBar, SWT.NONE);",
            "        item.setControl(button);",
            "        button.setText('My Button');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    CoolItemInfo item = coolBar.getItems().get(0);
    ControlInfo button = coolBar.getChildrenControls().get(0);
    // "button" is set using setControl()
    assertSame(button, item.getControl());
    assertThat(item.getSimpleContainerChildren()).containsExactly(button);
    // check that "button" is wide
    {
      assertThat(button.getBounds().width).isGreaterThan(70);
      assertThat(item.getBounds().width).isGreaterThan(80);
    }
    // check hierarchy: "button" should be in "item", but not in "coolBar"
    {
      assertThat(item.getPresentation().getChildrenTree()).containsExactly(button);
      assertThat(item.getPresentation().getChildrenGraphical()).containsExactly(button);
      assertThat(coolBar.getPresentation().getChildrenTree()).containsExactly(item);
      assertThat(coolBar.getPresentation().getChildrenGraphical()).containsExactly(item);
    }
  }

  /**
   * Test for {@link CoolItemInfo#command_CREATE(ControlInfo)}.
   */
  public void test_setControl_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    {",
            "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    CoolItemInfo item = coolBar.getItems().get(0);
    // no control initially
    assertNull(item.getControl());
    assertThat(item.getSimpleContainerChildren()).isEmpty();
    // set Button on "item"
    ControlInfo button = BTestUtils.createButton();
    simpleContainer_CREATE(item, button);
    // check result
    assertSame(button, item.getControl());
    assertThat(item.getSimpleContainerChildren()).containsExactly(button);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "    {",
        "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
        "      {",
        "        Button button = new Button(coolBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link CoolItemInfo#command_ADD(ControlInfo)}.
   */
  public void test_setControl_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    {",
            "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    CoolItemInfo item = coolBar.getItems().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    // no control initially
    assertNull(item.getControl());
    // set Button on "item"
    simpleContainer_ADD(item, button);
    // check result
    assertSame(button, item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "    {",
        "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
        "      {",
        "        Button button = new Button(coolBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move {@link ControlInfo} from one {@link CoolItemInfo} to other.
   */
  public void test_setControl_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    {",
            "      CoolItem item_1 = new CoolItem(coolBar, SWT.SEPARATOR);",
            "    }",
            "    {",
            "      CoolItem item_2 = new CoolItem(coolBar, SWT.SEPARATOR);",
            "      {",
            "        Button button = new Button(coolBar, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    CoolItemInfo item_1 = coolBar.getItems().get(0);
    CoolItemInfo item_2 = coolBar.getItems().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        coolBar.getChildrenJava().indexOf(item_2) + 1,
        coolBar.getChildrenJava().indexOf(button));
    // move "button" on "item_1"
    simpleContainer_ADD(item_1, button);
    assertNull(item_2.getControl());
    assertSame(button, item_1.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "    {",
        "      CoolItem item_1 = new CoolItem(coolBar, SWT.SEPARATOR);",
        "      {",
        "        Button button = new Button(coolBar, SWT.NONE);",
        "        item_1.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      CoolItem item_2 = new CoolItem(coolBar, SWT.SEPARATOR);",
        "    }",
        "  }",
        "}");
    // now "button" is after "item_1"
    assertEquals(
        coolBar.getChildrenJava().indexOf(item_1) + 1,
        coolBar.getChildrenJava().indexOf(button));
  }

  /**
   * When we move {@link CoolItemInfo} with {@link ControlInfo}, they should move together.
   */
  public void test_setControl_MOVEItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    {",
            "      CoolItem item_1 = new CoolItem(coolBar, SWT.NONE);",
            "    }",
            "    {",
            "      CoolItem item_2 = new CoolItem(coolBar, SWT.SEPARATOR);",
            "      {",
            "        Button button = new Button(coolBar, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    CoolItemInfo item_1 = coolBar.getItems().get(0);
    CoolItemInfo item_2 = coolBar.getItems().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        coolBar.getChildrenJava().indexOf(item_2) + 1,
        coolBar.getChildrenJava().indexOf(button));
    // move "item_2" before "item_1"
    flowContainer_MOVE(coolBar, item_2, item_1);
    assertSame(button, item_2.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "    {",
        "      CoolItem item_2 = new CoolItem(coolBar, SWT.SEPARATOR);",
        "      {",
        "        Button button = new Button(coolBar, SWT.NONE);",
        "        item_2.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      CoolItem item_1 = new CoolItem(coolBar, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // "button" is still after "item_2"
    assertEquals(
        coolBar.getChildrenJava().indexOf(item_2) + 1,
        coolBar.getChildrenJava().indexOf(button));
  }

  /**
   * When we move {@link ControlInfo} out from {@link CoolItemInfo}, the
   * {@link CoolItem#setControl(org.eclipse.swt.widgets.Control)} invocation should be removed.
   */
  public void test_setControl_moveOut() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    {",
            "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
            "      {",
            "        Button button = new Button(coolBar, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    CoolItemInfo item = coolBar.getItems().get(0);
    ControlInfo button = item.getControl();
    // move "button" on "shell"
    flowContainer_MOVE(rowLayout, button, null);
    assertNull(item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "    {",
        "      CoolItem item = new CoolItem(coolBar, SWT.SEPARATOR);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    // add item
    {
      CoolItemInfo coolItem = createJavaInfo("org.eclipse.swt.widgets.CoolItem", null);
      flowContainer_CREATE(coolBar, coolItem, null);
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "      {",
        "        CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
            "      {",
            "        CoolItem item = new CoolItem(coolBar, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        CoolItem item = new CoolItem(coolBar, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CoolBarInfo coolBar = (CoolBarInfo) shell.getChildrenControls().get(0);
    // move item
    List<CoolItemInfo> items = coolBar.getItems();
    CoolItemInfo item_2 = items.get(1);
    CoolItemInfo item_1 = items.get(0);
    flowContainer_MOVE(coolBar, item_2, item_1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      CoolBar coolBar = new CoolBar(this, SWT.FLAT);",
        "      {",
        "        CoolItem item = new CoolItem(coolBar, SWT.NONE);",
        "        item.setText('111');",
        "      }",
        "      {",
        "        CoolItem item = new CoolItem(coolBar, SWT.NONE);",
        "        item.setText('000');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static FlowContainer getFlowContainer(CoolBarInfo coolBar) {
    return new FlowContainerFactory(coolBar, true).get().get(0);
  }
}