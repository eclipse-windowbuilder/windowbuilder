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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractTabItemInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TabFolderInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TabItemInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for {@link TabFolderInfo}.
 *
 * @author scheglov_ke
 */
public class TabFolderTest extends RcpModelTest {
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
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_unselectedTab_setRedraw() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button_1 = new Button(tabFolder, SWT.NONE);",
            "        item_1.setControl(button_1);",
            "      }",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button_2 = new Button(tabFolder, SWT.NONE);",
            "        item_2.setControl(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    ControlInfo button_2 = item_2.getControl();
    // "item_2" is not selected
    assertNotSame(item_2, tabFolder.getSelectedItem());
    // so TabFolder sets empty bounds for "button_2"
    assertTrue(button_2.getBounds().isEmpty());
    // ...but we should not keep it in "disable drawing" state
    assertEquals(0, ReflectionUtils.getFieldInt(button_2.getObject(), "drawCount"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When no items, {@link TabFolderInfo#getSelectedItem()} returns <code>null</code>.
   */
  public void test_getSelectedItem_0() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    assertNull(tabFolder.getSelectedItem());
    // no tree/graphical children
    assertThat(tabFolder.getPresentation().getChildrenTree()).isEmpty();
    assertThat(tabFolder.getPresentation().getChildrenGraphical()).isEmpty();
  }

  /**
   * When no items, {@link TabFolderInfo#getSelectedItem()} returns <code>null</code>.
   */
  public void test_getSelectedItem_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "    TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_1 = tabFolder.getItems2().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    // by default first TabItem is selected, i.e. "item_1"
    assertSame(item_1, tabFolder.getSelectedItem());
    // select "item_2"
    item_2.doSelect();
    // check that "item_2" is selected in model and in GUI
    assertSame(item_2, tabFolder.getSelectedItem());
    Assertions.<Object>assertThat(((TabFolder) tabFolder.getObject()).getSelection()).containsOnly(item_2.getObject());
    // check tree/graphical children
    {
      List<ObjectInfo> children = tabFolder.getPresentation().getChildrenTree();
      assertThat(children).containsExactly(item_1, item_2);
    }
    {
      List<ObjectInfo> children = tabFolder.getPresentation().getChildrenGraphical();
      assertThat(children).containsExactly(item_1, item_2);
    }
  }

  /**
   * Test for {@link AbstractTabItemInfo#doSelect()}.
   */
  public void test_doSelect() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "    TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_1 = tabFolder.getItems2().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    // by default first TabItem is selected, i.e. "item_1"
    assertSame(item_1, tabFolder.getSelectedItem());
    //
    final AtomicInteger refreshCount = new AtomicInteger();
    shell.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        refreshCount.incrementAndGet();
      }
    });
    // select "item_2", refresh expected
    item_2.doSelect();
    assertSame(item_2, tabFolder.getSelectedItem());
    assertEquals(1, refreshCount.get());
    // select "item_2" again, no refresh
    item_2.doSelect();
    assertEquals(1, refreshCount.get());
  }

  /**
   * Test for {@link ObjectEventListener#selecting(ObjectInfo, boolean[])} is implemented for
   * {@link TabFolderInfo}.
   */
  public void test_selecting() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button_1 = new Button(tabFolder, SWT.NONE);",
            "        item_1.setControl(button_1);",
            "      }",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button_2 = new Button(tabFolder, SWT.NONE);",
            "        item_2.setControl(button_2);",
            "      }",
            "    }",
            "    {",
            "      Button button_3 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_1 = tabFolder.getItems2().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    ControlInfo button_1 = item_1.getControl();
    ControlInfo button_2 = item_2.getControl();
    ControlInfo button_3 = shell.getChildrenControls().get(1);
    // "button_3" is external, so nobody will ask to refresh
    {
      boolean[] refresh = new boolean[]{false};
      shell.getBroadcastObject().selecting(button_3, refresh);
      assertThat(refresh[0]).isFalse();
    }
    // select "button_2", refresh expected
    {
      boolean[] refresh = new boolean[]{false};
      shell.getBroadcastObject().selecting(button_2, refresh);
      assertThat(refresh[0]).isTrue();
      assertThat(tabFolder.getSelectedItem()).isSameAs(item_2);
    }
    // again select "button_2", it is already selected, so no refresh
    {
      boolean[] refresh = new boolean[]{false};
      shell.getBroadcastObject().selecting(button_2, refresh);
      assertThat(refresh[0]).isFalse();
      assertThat(tabFolder.getSelectedItem()).isSameAs(item_2);
    }
    // select "button_1", refresh expected
    {
      boolean[] refresh = new boolean[]{false};
      shell.getBroadcastObject().selecting(button_1, refresh);
      assertThat(refresh[0]).isTrue();
      assertThat(tabFolder.getSelectedItem()).isSameAs(item_1);
    }
  }

  /**
   * We should show on design canvas only {@link ControlInfo}'s of expanded {@link TabItemInfo}'s.
   */
  public void test_presentation_getChildren() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button_1 = new Button(tabFolder, SWT.NONE);",
            "        item_1.setControl(button_1);",
            "      }",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button_2 = new Button(tabFolder, SWT.NONE);",
            "        item_2.setControl(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_1 = tabFolder.getItems2().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    ControlInfo button_1 = item_1.getControl();
    ControlInfo button_2 = item_2.getControl();
    // "item_1" is selected by default...
    assertSame(item_1, tabFolder.getSelectedItem());
    // ...so, "button_1" is in graphical children and "button_2" is not
    {
      List<ObjectInfo> childrenGraphical = tabFolder.getPresentation().getChildrenGraphical();
      assertThat(childrenGraphical).containsOnly(item_1, item_2, button_1);
    }
    // select "item_2"...
    item_2.doSelect();
    // check that "item_2" is selected in model and in GUI
    assertSame(item_2, tabFolder.getSelectedItem());
    Assertions.<Object>assertThat(((TabFolder) tabFolder.getObject()).getSelection()).containsOnly(item_2.getObject());
    // ...so, "button_2" is in graphical children and "button_1" is not
    {
      List<ObjectInfo> childrenGraphical = tabFolder.getPresentation().getChildrenGraphical();
      assertThat(childrenGraphical).containsOnly(item_1, item_2, button_2);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link TabFolder} with {@link TabItem}'s.
   */
  public void test_parseItems() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(500, 350);",
            "    setLayout(new FillLayout());",
            "    {",
            "      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "      {",
            "        TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    // check items
    List<TabItemInfo> items = tabFolder.getItems2();
    assertEquals(2, items.size());
    TabItemInfo item_0 = items.get(0);
    TabItemInfo item_1 = items.get(1);
    // text
    assertEquals("000", ReflectionUtils.invokeMethod2(item_0.getObject(), "getText"));
    assertEquals("111", ReflectionUtils.invokeMethod2(item_1.getObject(), "getText"));
    // bounds for "item_0"
    {
      Rectangle modelBounds_0 = item_0.getModelBounds();
      assertThat(modelBounds_0.width).isGreaterThan(30);
      assertThat(modelBounds_0.height).isGreaterThan(17);
    }
    // bounds for "item_1"
    {
      Rectangle modelBounds_1 = item_1.getModelBounds();
      assertThat(modelBounds_1.width).isGreaterThan(30);
      assertThat(modelBounds_1.height).isGreaterThan(17);
    }
    // no setControl() invocations
    assertNull(item_0.getControl());
    assertThat(item_0.getPresentation().getChildrenTree()).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setControl()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TabItem#setControl(org.eclipse.swt.widgets.Control)}.
   */
  public void test_setControl_get() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(500, 350);",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button = new Button(tabFolder, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item = tabFolder.getItems2().get(0);
    ControlInfo button = tabFolder.getChildrenControls().get(0);
    // "button" is set using setControl()
    assertSame(button, item.getControl());
    // check that "button" is wide
    {
      assertThat(button.getBounds().width).isGreaterThan(400);
      assertThat(button.getBounds().height).isGreaterThan(250);
    }
    // check hierarchy: "button" should be in "item", but not in "tabFolder"
    {
      assertThat(item.getPresentation().getChildrenTree()).containsOnly(button);
      assertThat(tabFolder.getPresentation().getChildrenTree()).containsOnly(item);
    }
  }

  /**
   * Test for {@link TabItemInfo#command_CREATE(ControlInfo)}.
   */
  public void test_CREATE_control_onItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item = tabFolder.getItems2().get(0);
    // no control initially
    assertNull(item.getControl());
    // set Button on "item"
    ControlInfo button = BTestUtils.createButton();
    item.command_CREATE(button);
    // check result
    assertSame(button, item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
        "      {",
        "        Button button = new Button(tabFolder, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TabFolderInfo#command_CREATE(ControlInfo, AbstractTabItemInfo)}.
   */
  public void test_CREATE_control_onFolder() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = getJavaInfoByName("tabFolder");
    // create Button
    ControlInfo button = BTestUtils.createButton();
    tabFolder.command_CREATE(button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem tabItem = new TabItem(tabFolder, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "      {",
        "        Button button = new Button(tabFolder, SWT.NONE);",
        "        tabItem.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TabItemInfo#command_ADD(ControlInfo)}.
   */
  public void test_ADD_control_onItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item = tabFolder.getItems2().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    // no control initially
    assertNull(item.getControl());
    // set Button on "item"
    item.command_ADD(button);
    // check result
    assertSame(button, item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
        "      {",
        "        Button button = new Button(tabFolder, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TabFolderInfo#command_MOVE(ControlInfo, AbstractTabItemInfo)}.
   */
  public void test_ADD_control_onFolder() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = getJavaInfoByName("tabFolder");
    ControlInfo button = getJavaInfoByName("button");
    // move "button"
    tabFolder.command_MOVE(button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem tabItem = new TabItem(tabFolder, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "      {",
        "        Button button = new Button(tabFolder, SWT.NONE);",
        "        tabItem.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move {@link ControlInfo} from one {@link TabItemInfo} to other.
   */
  public void test_MOVE_control_toOtherItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button = new Button(tabFolder, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_1 = tabFolder.getItems2().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        tabFolder.getChildrenJava().indexOf(item_2) + 1,
        tabFolder.getChildrenJava().indexOf(button));
    // move "button" on "item_1"
    item_1.command_ADD(button);
    assertNull(item_2.getControl());
    assertSame(button, item_1.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
        "      {",
        "        Button button = new Button(tabFolder, SWT.NONE);",
        "        item_1.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // now "button" is after "item_1"
    assertEquals(
        tabFolder.getChildrenJava().indexOf(item_1) + 1,
        tabFolder.getChildrenJava().indexOf(button));
  }

  /**
   * When we delete {@link TabItemInfo} with {@link ControlInfo}, we should also delete
   * {@link ControlInfo} .
   */
  public void test_setControl_DELETE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button = new Button(tabFolder, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item = tabFolder.getItems2().get(0);
    // delete "item"
    item.delete();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "  }",
        "}");
  }

  /**
   * When we move {@link ControlInfo} out from {@link TabItemInfo}, the
   * {@link TabItem#setControl(org.eclipse.swt.widgets.Control)} invocation should be removed.
   */
  public void test_setControl_moveOut() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button = new Button(tabFolder, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item = tabFolder.getItems2().get(0);
    ControlInfo button = item.getControl();
    // move "button" on "shell"
    fillLayout.command_MOVE(button, null);
    assertNull(item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem item = new TabItem(tabFolder, SWT.NONE);",
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
   * Test for {@link TabFolderInfo#command_CREATE(TabItemInfo, TabItemInfo)}.
   */
  public void test_CREATE_item() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    // add items
    {
      TabItemInfo TabItem = createJavaInfo("org.eclipse.swt.widgets.TabItem");
      tabFolder.command_CREATE(TabItem, null);
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);",
        "        tabItem.setText('New Item');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TabFolderInfo#command_MOVE(TabItemInfo, TabItemInfo)}.
   */
  public void test_MOVE_item_empty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "      {",
            "        TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        TabItem item = new TabItem(tabFolder, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    // move item
    List<TabItemInfo> items = tabFolder.getItems2();
    tabFolder.command_MOVE(items.get(1), items.get(0));
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(tabFolder, SWT.NONE);",
        "        item.setText('111');",
        "      }",
        "      {",
        "        TabItem item = new TabItem(tabFolder, SWT.NONE);",
        "        item.setText('000');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we move {@link TabItemInfo} with {@link ControlInfo}, they should move together.
   */
  public void test_MOVE_item_withControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
            "    {",
            "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
            "      {",
            "        Button button = new Button(tabFolder, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    TabFolderInfo tabFolder = (TabFolderInfo) shell.getChildrenControls().get(0);
    TabItemInfo item_1 = tabFolder.getItems2().get(0);
    TabItemInfo item_2 = tabFolder.getItems2().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        tabFolder.getChildrenJava().indexOf(item_2) + 1,
        tabFolder.getChildrenJava().indexOf(button));
    // move "item_2" before "item_1"
    tabFolder.command_MOVE(item_2, item_1);
    assertSame(button, item_2.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
        "    {",
        "      TabItem item_2 = new TabItem(tabFolder, SWT.NONE);",
        "      {",
        "        Button button = new Button(tabFolder, SWT.NONE);",
        "        item_2.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      TabItem item_1 = new TabItem(tabFolder, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // "button" is still after "item_2"
    assertEquals(
        tabFolder.getChildrenJava().indexOf(item_2) + 1,
        tabFolder.getChildrenJava().indexOf(button));
  }
}