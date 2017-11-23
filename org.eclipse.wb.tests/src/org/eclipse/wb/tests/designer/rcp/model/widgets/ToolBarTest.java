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
import org.eclipse.wb.internal.rcp.model.widgets.ToolBarInfo;
import org.eclipse.wb.internal.rcp.model.widgets.ToolItemInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Test for {@link ToolBar}.
 * 
 * @author scheglov_ke
 */
public class ToolBarTest extends RcpModelTest {
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
   * Test for {@link ToolBarInfo#isHorizontal()}.
   */
  public void test_isHorizontal() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar_1 = new ToolBar(this, SWT.NONE);",
            "    ToolBar toolBar_2 = new ToolBar(this, SWT.VERTICAL);",
            "  }",
            "}");
    shell.refresh();
    {
      ToolBarInfo toolBar_1 = shell.getChildren(ToolBarInfo.class).get(0);
      assertTrue(toolBar_1.isHorizontal());
      assertTrue(getFlowContainer(toolBar_1).isHorizontal());
    }
    {
      ToolBarInfo toolBar_2 = shell.getChildren(ToolBarInfo.class).get(1);
      assertFalse(toolBar_2.isHorizontal());
      assertFalse(getFlowContainer(toolBar_2).isHorizontal());
    }
  }

  /**
   * {@link ToolBar} with {@link ToolItem}'s.
   */
  public void test_parseItems() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "      {",
            "        ToolItem item = new ToolItem(toolBar, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        ToolItem item = new ToolItem(toolBar, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    // check items
    List<ToolItemInfo> items = toolBar.getItems();
    assertEquals(2, items.size());
    ToolItemInfo item_0 = items.get(0);
    ToolItemInfo item_1 = items.get(1);
    // text
    assertEquals("000", ReflectionUtils.invokeMethod2(item_0.getObject(), "getText"));
    assertEquals("111", ReflectionUtils.invokeMethod2(item_1.getObject(), "getText"));
    // bounds
    {
      Rectangle modelBounds = item_0.getModelBounds();
      assertTrue(modelBounds.width > 20);
      assertTrue(modelBounds.height > 20);
    }
    // no setControl() invocations
    assertFalse(item_0.isSeparator());
    assertNull(item_0.getControl());
    Assertions.assertThat(item_0.getPresentation().getChildrenTree()).containsOnly();
  }

  /**
   * Test that presentation returns different icons for {@link ToolItem}s with different styles.
   */
  public void test_ToolItem_presentation() throws Exception {
    ToolBarInfo toolBar =
        parseJavaInfo(
            "public class Test extends ToolBar {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    new ToolItem(this, SWT.NONE);",
            "    new ToolItem(this, SWT.PUSH);",
            "    new ToolItem(this, SWT.CHECK);",
            "    new ToolItem(this, SWT.RADIO);",
            "    new ToolItem(this, SWT.DROP_DOWN);",
            "    new ToolItem(this, SWT.SEPARATOR);",
            "  }",
            "}");
    toolBar.refresh();
    // prepare items
    ToolItemInfo itemDefault = toolBar.getItems().get(0);
    ToolItemInfo itemPush = toolBar.getItems().get(1);
    ToolItemInfo itemCheck = toolBar.getItems().get(2);
    ToolItemInfo itemRadio = toolBar.getItems().get(3);
    ToolItemInfo itemDropDown = toolBar.getItems().get(4);
    ToolItemInfo itemSeparator = toolBar.getItems().get(5);
    // check icons
    assertSame(itemDefault.getPresentation().getIcon(), itemPush.getPresentation().getIcon());
    assertNotSame(itemPush.getPresentation().getIcon(), itemRadio.getPresentation().getIcon());
    assertNotSame(itemPush.getPresentation().getIcon(), itemCheck.getPresentation().getIcon());
    assertNotSame(itemRadio.getPresentation().getIcon(), itemCheck.getPresentation().getIcon());
    assertNotSame(itemRadio.getPresentation().getIcon(), itemDropDown.getPresentation().getIcon());
    assertNotSame(itemRadio.getPresentation().getIcon(), itemSeparator.getPresentation().getIcon());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setControl()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ToolItem#setControl(org.eclipse.swt.widgets.Control)}.
   */
  public void test_setControl_get() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    {",
            "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item.setWidth(200);",
            "      {",
            "        Button button = new Button(toolBar, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    ToolItemInfo item = toolBar.getItems().get(0);
    ControlInfo button = toolBar.getChildrenControls().get(0);
    // "button" is set using setControl()
    assertTrue(item.isSeparator());
    assertSame(button, item.getControl());
    assertThat(item.getSimpleContainerChildren()).containsExactly(button);
    // check that "button" is wide
    {
      assertTrue(item.getBounds().width == 200);
      assertTrue(button.getBounds().width > 190);
    }
    // check hierarchy: "button" should be in "item", but not in "toolBar"
    {
      assertThat(item.getPresentation().getChildrenTree()).containsExactly(button);
      assertThat(item.getPresentation().getChildrenGraphical()).containsExactly(button);
      assertThat(toolBar.getPresentation().getChildrenTree()).containsExactly(item);
      assertThat(toolBar.getPresentation().getChildrenGraphical()).containsExactly(item);
    }
  }

  /**
   * Test for {@link ToolItemInfo#command_CREATE(ControlInfo)}.
   */
  public void test_setControl_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    {",
            "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item.setWidth(200);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    ToolItemInfo item = toolBar.getItems().get(0);
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
        "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "    {",
        "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      item.setWidth(200);",
        "      {",
        "        Button button = new Button(toolBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ToolItemInfo#command_ADD(ControlInfo)}.
   */
  public void test_setControl_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    {",
            "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item.setWidth(200);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    ToolItemInfo item = toolBar.getItems().get(0);
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
        "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "    {",
        "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      item.setWidth(200);",
        "      {",
        "        Button button = new Button(toolBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move {@link ControlInfo} from one {@link ToolItemInfo} to other.
   */
  public void test_setControl_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    {",
            "      ToolItem item_1 = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item_1.setWidth(200);",
            "    }",
            "    {",
            "      ToolItem item_2 = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item_2.setWidth(200);",
            "      {",
            "        Button button = new Button(toolBar, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    ToolItemInfo item_1 = toolBar.getItems().get(0);
    ToolItemInfo item_2 = toolBar.getItems().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        toolBar.getChildrenJava().indexOf(item_2) + 1,
        toolBar.getChildrenJava().indexOf(button));
    // move "button" on "item_1"
    simpleContainer_ADD(item_1, button);
    assertNull(item_2.getControl());
    assertSame(button, item_1.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "    {",
        "      ToolItem item_1 = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      item_1.setWidth(200);",
        "      {",
        "        Button button = new Button(toolBar, SWT.NONE);",
        "        item_1.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      ToolItem item_2 = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      item_2.setWidth(200);",
        "    }",
        "  }",
        "}");
    // now "button" is after "item_1"
    assertEquals(
        toolBar.getChildrenJava().indexOf(item_1) + 1,
        toolBar.getChildrenJava().indexOf(button));
  }

  /**
   * When we move {@link ToolItemInfo} with {@link ControlInfo}, they should move together.
   */
  public void test_setControl_MOVEItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    {",
            "      ToolItem item_1 = new ToolItem(toolBar, SWT.NONE);",
            "      item_1.setWidth(200);",
            "    }",
            "    {",
            "      ToolItem item_2 = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item_2.setWidth(200);",
            "      {",
            "        Button button = new Button(toolBar, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    ToolItemInfo item_1 = toolBar.getItems().get(0);
    ToolItemInfo item_2 = toolBar.getItems().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        toolBar.getChildrenJava().indexOf(item_2) + 1,
        toolBar.getChildrenJava().indexOf(button));
    // move "item_2" before "item_1"
    flowContainer_MOVE(toolBar, item_2, item_1);
    assertSame(button, item_2.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "    {",
        "      ToolItem item_2 = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      item_2.setWidth(200);",
        "      {",
        "        Button button = new Button(toolBar, SWT.NONE);",
        "        item_2.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      ToolItem item_1 = new ToolItem(toolBar, SWT.NONE);",
        "      item_1.setWidth(200);",
        "    }",
        "  }",
        "}");
    // "button" is still after "item_2"
    assertEquals(
        toolBar.getChildrenJava().indexOf(item_2) + 1,
        toolBar.getChildrenJava().indexOf(button));
  }

  /**
   * When we move {@link ControlInfo} out from {@link ToolItemInfo}, the
   * {@link ToolItem#setControl(org.eclipse.swt.widgets.Control)} invocation should be removed.
   */
  public void test_setControl_moveOut() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    {",
            "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
            "      item.setWidth(200);",
            "      {",
            "        Button button = new Button(toolBar, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    ToolItemInfo item = toolBar.getItems().get(0);
    ControlInfo button = item.getControl();
    // move "button" on "shell"
    flowContainer_MOVE(rowLayout, button, null);
    assertNull(item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "    {",
        "      ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      item.setWidth(200);",
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
  /**
   * Test for {@link ToolBarInfo#command_absolute_CREATE(ToolItemInfo, ToolItemInfo)}.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    // add items
    {
      ToolItemInfo toolItem = createJavaInfo("org.eclipse.swt.widgets.ToolItem", null);
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createJavaInfo("org.eclipse.swt.widgets.ToolItem", "check");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createJavaInfo("org.eclipse.swt.widgets.ToolItem", "radio");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createJavaInfo("org.eclipse.swt.widgets.ToolItem", "dropDown");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createJavaInfo("org.eclipse.swt.widgets.ToolItem", "separator");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "      {",
        "        ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);",
        "        toolItem.setText('New Item');",
        "      }",
        "      {",
        "        ToolItem toolItem = new ToolItem(toolBar, SWT.CHECK);",
        "        toolItem.setText('Check Item');",
        "      }",
        "      {",
        "        ToolItem toolItem = new ToolItem(toolBar, SWT.RADIO);",
        "        toolItem.setText('Radio Item');",
        "      }",
        "      {",
        "        ToolItem toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);",
        "        toolItem.setText('DropDown Item');",
        "      }",
        "      {",
        "        ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ToolBarInfo#command_absolute_MOVE(ToolItemInfo, ToolItemInfo)}.
   */
  public void test_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
            "      {",
            "        ToolItem item = new ToolItem(toolBar, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        ToolItem item = new ToolItem(toolBar, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ToolBarInfo toolBar = (ToolBarInfo) shell.getChildrenControls().get(0);
    // move item
    List<ToolItemInfo> items = toolBar.getItems();
    flowContainer_MOVE(toolBar, items.get(1), items.get(0));
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      ToolBar toolBar = new ToolBar(this, SWT.FLAT);",
        "      {",
        "        ToolItem item = new ToolItem(toolBar, SWT.NONE);",
        "        item.setText('111');",
        "      }",
        "      {",
        "        ToolItem item = new ToolItem(toolBar, SWT.NONE);",
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
  private static FlowContainer getFlowContainer(ToolBarInfo toolBar) {
    return new FlowContainerFactory(toolBar, true).get().get(0);
  }
}