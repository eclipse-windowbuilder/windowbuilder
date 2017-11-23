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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.widgets.ExpandBarInfo;
import org.eclipse.wb.internal.rcp.model.widgets.ExpandItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Test for {@link ExpandBar}.
 * 
 * @author scheglov_ke
 */
public class ExpandBarTest extends RcpModelTest {
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
   * {@link ExpandBar} with {@link ExpandItem}'s.
   */
  public void test_parseItems() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setSize(500, 350);",
            "    setLayout(new FillLayout());",
            "    {",
            "      ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "      {",
            "        ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "        item.setText('111');",
            "        item.setHeight(200);",
            "        item.setExpanded(true);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    // check items
    List<ExpandItemInfo> items = expandBar.getItems();
    assertThat(items).hasSize(2);
    ExpandItemInfo item_0 = items.get(0);
    ExpandItemInfo item_1 = items.get(1);
    // text
    assertEquals("000", ReflectionUtils.invokeMethod2(item_0.getObject(), "getText"));
    assertEquals("111", ReflectionUtils.invokeMethod2(item_1.getObject(), "getText"));
    // bounds for "item_0"
    {
      Rectangle modelBounds_0 = item_0.getModelBounds();
      assertThat(modelBounds_0.width).isGreaterThan(450);
      assertThat(modelBounds_0.height).isGreaterThan(20);
    }
    // bounds for "item_1"
    {
      Rectangle modelBounds_1 = item_1.getModelBounds();
      assertThat(modelBounds_1.width).isGreaterThan(450);
      assertThat(modelBounds_1.height).isGreaterThan(220);
    }
    // no setControl() invocations
    assertNull(item_0.getControl());
    Assertions.assertThat(item_0.getPresentation().getChildrenTree()).isEmpty();
  }

  /**
   * We should show on design canvas only {@link ControlInfo}'s of expanded {@link ExpandItemInfo} 
   * 's.
   */
  public void test_presentationChildrenGraphical() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item_1 = new ExpandItem(expandBar, SWT.NONE);",
            "      item_1.setExpanded(true);",
            "      {",
            "        Button button_1 = new Button(expandBar, SWT.NONE);",
            "        item_1.setControl(button_1);",
            "      }",
            "    }",
            "    {",
            "      ExpandItem item_2 = new ExpandItem(expandBar, SWT.NONE);",
            "      item_2.setExpanded(false);",
            "      {",
            "        Button button_2 = new Button(expandBar, SWT.NONE);",
            "        item_2.setControl(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ExpandBarInfo expandBar = getJavaInfoByName("expandBar");
    ExpandItemInfo item_1 = getJavaInfoByName("item_1");
    ExpandItemInfo item_2 = getJavaInfoByName("item_2");
    ControlInfo button_1 = getJavaInfoByName("button_1");
    // "item_1" is expanded and "item_2" - not
    assertEquals(true, item_1.getPropertyByTitle("expanded").getValue());
    assertEquals(false, item_2.getPropertyByTitle("expanded").getValue());
    // ...so, "button_1" is in graphical children and "button_2" is not
    {
      Assertions.assertThat(expandBar.getPresentation().getChildrenGraphical()).containsExactly(
          item_1,
          item_2);
      Assertions.assertThat(item_1.getPresentation().getChildrenGraphical()).containsExactly(
          button_1);
      Assertions.assertThat(item_2.getPresentation().getChildrenGraphical()).isEmpty();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setControl()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExpandItem#setControl(org.eclipse.swt.widgets.Control)}.
   */
  public void test_setControl_get() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "      item.setExpanded(true);",
            "      item.setHeight(200);",
            "      {",
            "        Button button = new Button(expandBar, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item = expandBar.getItems().get(0);
    ControlInfo button = expandBar.getChildrenControls().get(0);
    // "button" is set using setControl()
    assertSame(button, item.getControl());
    // check that "button" is wide
    {
      assertThat(item.getBounds().height).isGreaterThan(200);
      assertEquals(200, button.getBounds().height);
    }
    // check hierarchy: "button" should be in "item", but not in "expandBar"
    {
      assertThat(item.getPresentation().getChildrenTree()).containsExactly(button);
      assertThat(item.getPresentation().getChildrenGraphical()).containsExactly(button);
      assertThat(expandBar.getPresentation().getChildrenTree()).containsExactly(item);
      assertThat(expandBar.getPresentation().getChildrenGraphical()).containsExactly(item);
    }
  }

  /**
   * Test for {@link ExpandItemInfo#command_CREATE(ControlInfo)}.<br>
   * There is already {@link ExpandItem#setHeight(int)}, so it is kept.
   */
  public void test_setControl_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "      item.setHeight(200);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item = expandBar.getItems().get(0);
    // no control initially
    assertNull(item.getControl());
    // set Button on "item"
    ControlInfo button = BTestUtils.createButton();
    simpleContainer_CREATE(item, button);
    // check result
    assertSame(button, item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "    {",
        "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
        "      item.setExpanded(true);",
        "      {",
        "        Button button = new Button(expandBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "      item.setHeight(200);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ExpandItemInfo#command_CREATE(ControlInfo)}.<br>
   * There are not {@link ExpandItem#setHeight(int)}, so we add new one, with preferred height.
   */
  public void test_setControl_CREATE2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item = expandBar.getItems().get(0);
    // no control initially
    assertNull(item.getControl());
    // set Button on "item"
    ControlInfo button = BTestUtils.createButton();
    simpleContainer_CREATE(item, button);
    // check result
    assertSame(button, item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "    {",
        "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
        "      item.setExpanded(true);",
        "      {",
        "        Button button = new Button(expandBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "      item.setHeight(item.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ExpandItemInfo#command_ADD(ControlInfo)}.
   */
  public void test_setControl_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "      item.setHeight(200);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item = expandBar.getItems().get(0);
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
        "    setLayout(new FillLayout());",
        "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "    {",
        "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
        "      item.setExpanded(true);",
        "      {",
        "        Button button = new Button(expandBar, SWT.NONE);",
        "        item.setControl(button);",
        "      }",
        "      item.setHeight(200);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move {@link ControlInfo} from one {@link ExpandItemInfo} to other.
   */
  public void test_setControl_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item_1 = new ExpandItem(expandBar, SWT.NONE);",
            "      item_1.setHeight(200);",
            "    }",
            "    {",
            "      ExpandItem item_2 = new ExpandItem(expandBar, SWT.NONE);",
            "      {",
            "        Button button = new Button(expandBar, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "      item_2.setHeight(200);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item_1 = expandBar.getItems().get(0);
    ExpandItemInfo item_2 = expandBar.getItems().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertThat(expandBar.getChildrenJava()).containsSequence(item_2, button);
    // move "button" on "item_1"
    simpleContainer_ADD(item_1, button);
    assertNull(item_2.getControl());
    assertSame(button, item_1.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "    {",
        "      ExpandItem item_1 = new ExpandItem(expandBar, SWT.NONE);",
        "      item_1.setExpanded(true);",
        "      {",
        "        Button button = new Button(expandBar, SWT.NONE);",
        "        item_1.setControl(button);",
        "      }",
        "      item_1.setHeight(200);",
        "    }",
        "    {",
        "      ExpandItem item_2 = new ExpandItem(expandBar, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // now "button" is after "item_1"
    assertEquals(
        expandBar.getChildrenJava().indexOf(item_1) + 1,
        expandBar.getChildrenJava().indexOf(button));
  }

  /**
   * When we move {@link ExpandItemInfo} with {@link ControlInfo}, they should move together.
   */
  public void test_setControl_MOVEItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item_1 = new ExpandItem(expandBar, SWT.NONE);",
            "      item_1.setHeight(200);",
            "    }",
            "    {",
            "      ExpandItem item_2 = new ExpandItem(expandBar, SWT.NONE);",
            "      {",
            "        Button button = new Button(expandBar, SWT.NONE);",
            "        item_2.setControl(button);",
            "      }",
            "      item_2.setHeight(200);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item_1 = expandBar.getItems().get(0);
    ExpandItemInfo item_2 = expandBar.getItems().get(1);
    ControlInfo button = item_2.getControl();
    // initially "button" is after "item_2"
    assertEquals(
        expandBar.getChildrenJava().indexOf(item_2) + 1,
        expandBar.getChildrenJava().indexOf(button));
    // move "item_2" before "item_1"
    flowContainer_MOVE(expandBar, item_2, item_1);
    assertSame(button, item_2.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "    {",
        "      ExpandItem item_2 = new ExpandItem(expandBar, SWT.NONE);",
        "      {",
        "        Button button = new Button(expandBar, SWT.NONE);",
        "        item_2.setControl(button);",
        "      }",
        "      item_2.setHeight(200);",
        "    }",
        "    {",
        "      ExpandItem item_1 = new ExpandItem(expandBar, SWT.NONE);",
        "      item_1.setHeight(200);",
        "    }",
        "  }",
        "}");
    // "button" is still after "item_2"
    assertEquals(
        expandBar.getChildrenJava().indexOf(item_2) + 1,
        expandBar.getChildrenJava().indexOf(button));
  }

  /**
   * When we move {@link ControlInfo} out from {@link ExpandItemInfo}, the
   * {@link ExpandItem#setControl(org.eclipse.swt.widgets.Control)} invocation should be removed.
   */
  public void test_setControl_moveOut() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    {",
            "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "      {",
            "        Button button = new Button(expandBar, SWT.NONE);",
            "        item.setControl(button);",
            "      }",
            "      item.setHeight(200);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare components
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    ExpandItemInfo item = expandBar.getItems().get(0);
    ControlInfo button = item.getControl();
    // move "button" on "shell"
    flowContainer_MOVE(shell.getLayout(), button, null);
    assertNull(item.getControl());
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "    {",
        "      ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
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
   * Test for {@link ExpandBarInfo#command_CREATE(ExpandItemInfo, ExpandItemInfo)}.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    // add items
    {
      ExpandItemInfo newItem = createJavaInfo("org.eclipse.swt.widgets.ExpandItem");
      flowContainer_CREATE(expandBar, newItem, null);
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "      {",
        "        ExpandItem expandItem = new ExpandItem(expandBar, SWT.NONE);",
        "        expandItem.setText('New ExpandItem');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ExpandBarInfo#command_MOVE2(ExpandItemInfo, ExpandItemInfo)}.
   */
  public void test_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
            "      {",
            "        ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "        item.setText('000');",
            "      }",
            "      {",
            "        ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
            "        item.setText('111');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ExpandBarInfo expandBar = (ExpandBarInfo) shell.getChildrenControls().get(0);
    // move item
    List<ExpandItemInfo> items = expandBar.getItems();
    ExpandItemInfo item_1 = items.get(1);
    ExpandItemInfo item_0 = items.get(0);
    flowContainer_MOVE(expandBar, item_1, item_0);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      ExpandBar expandBar = new ExpandBar(this, SWT.NONE);",
        "      {",
        "        ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
        "        item.setText('111');",
        "      }",
        "      {",
        "        ExpandItem item = new ExpandItem(expandBar, SWT.NONE);",
        "        item.setText('000');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}