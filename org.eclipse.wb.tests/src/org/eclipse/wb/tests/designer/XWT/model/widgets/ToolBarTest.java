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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ToolBarInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ToolItemInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Test for {@link ToolBarInfo}.
 * 
 * @author scheglov_ke
 */
public class ToolBarTest extends XwtModelTest {
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <ToolBar wbp:name='toolBar_1'/>",
        "  <ToolBar wbp:name='toolBar_2' x:Style='VERTICAL'/>",
        "</Shell>");
    refresh();
    {
      ToolBarInfo toolBar_1 = getObjectByName("toolBar_1");
      assertTrue(toolBar_1.isHorizontal());
      assertTrue(getFlowContainer(toolBar_1).isHorizontal());
    }
    {
      ToolBarInfo toolBar_2 = getObjectByName("toolBar_2");
      assertFalse(toolBar_2.isHorizontal());
      assertFalse(getFlowContainer(toolBar_2).isHorizontal());
    }
  }

  /**
   * {@link ToolBar} with {@link ToolItem}'s.
   */
  public void test_parseItems() throws Exception {
    ToolBarInfo toolBar =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ToolBar>",
            "  <ToolItem wbp:name='item_1' text='000'/>",
            "  <ToolItem wbp:name='item_2' text='111'/>",
            "</ToolBar>");
    refresh();
    // prepare
    ToolItemInfo item_1 = getObjectByName("item_1");
    ToolItemInfo item_2 = getObjectByName("item_2");
    assertThat(toolBar.getItems()).containsExactly(item_1, item_2);
    // text
    assertEquals("000", ReflectionUtils.invokeMethod(item_1.getObject(), "getText()"));
    assertEquals("111", ReflectionUtils.invokeMethod(item_2.getObject(), "getText()"));
    // item_1
    {
      // bounds
      {
        Rectangle modelBounds = item_1.getModelBounds();
        assertThat(modelBounds.x).isEqualTo(0);
        assertThat(modelBounds.y).isEqualTo(0);
        assertThat(modelBounds.width).isGreaterThan(25).isLessThan(50);
        assertThat(modelBounds.height).isGreaterThan(20).isLessThan(40);
      }
      // no Control
      assertFalse(item_1.isSeparator());
      Assertions.assertThat(item_1.getPresentation().getChildrenTree()).isEmpty();
    }
    // item_2
    {
      // bounds
      {
        Rectangle modelBounds = item_2.getModelBounds();
        assertThat(modelBounds.x).isGreaterThan(25);
        assertThat(modelBounds.y).isEqualTo(0);
        assertThat(modelBounds.width).isGreaterThan(25).isLessThan(50);
        assertThat(modelBounds.height).isGreaterThan(20).isLessThan(40);
      }
    }
  }

  /**
   * Test that presentation returns different icons for {@link ToolItem}s with different styles.
   */
  public void test_ToolItem_presentation() throws Exception {
    ToolBarInfo toolBar =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ToolBar>",
            "  <ToolItem/>",
            "  <ToolItem x:Style='PUSH'/>",
            "  <ToolItem x:Style='CHECK'/>",
            "  <ToolItem x:Style='RADIO'/>",
            "  <ToolItem x:Style='DROP_DOWN'/>",
            "  <ToolItem x:Style='SEPARATOR'/>",
            "</ToolBar>");
    refresh();
    // prepare items
    List<ToolItemInfo> items = toolBar.getItems();
    ToolItemInfo itemDefault = items.get(0);
    ToolItemInfo itemPush = items.get(1);
    ToolItemInfo itemCheck = items.get(2);
    ToolItemInfo itemRadio = items.get(3);
    ToolItemInfo itemDropDown = items.get(4);
    ToolItemInfo itemSeparator = items.get(5);
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
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Control_isEmpty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ToolBar>",
        "  <ToolItem wbp:name='item_1' text='X'/>",
        "  <ToolItem wbp:name='item_2' x:Style='SEPARATOR' text='X'/>",
        "  <ToolItem wbp:name='item_3' x:Style='SEPARATOR' width='100'>",
        "    <ToolItem.control>",
        "      <Button wbp:name='button'/>",
        "    </ToolItem.control>",
        "  </ToolItem>",
        "</ToolBar>");
    refresh();
    ToolItemInfo item_1 = getObjectByName("item_1");
    ToolItemInfo item_2 = getObjectByName("item_2");
    ToolItemInfo item_3 = getObjectByName("item_3");
    ControlInfo button = getObjectByName("button");
    // not container
    assertFalse(item_1.isSeparator());
    // container, empty
    {
      assertTrue(item_2.isSeparator());
      SimpleContainer simpleContainer = new SimpleContainerFactory(item_2, false).get().get(0);
      assertTrue(simpleContainer.isEmpty());
      Assertions.assertThat(item_2.getPresentation().getChildrenTree()).isEmpty();
    }
    // container, not empty
    {
      assertTrue(item_3.isSeparator());
      SimpleContainer simpleContainer = new SimpleContainerFactory(item_3, false).get().get(0);
      assertFalse(simpleContainer.isEmpty());
      Assertions.assertThat(item_3.getPresentation().getChildrenTree()).containsOnly(button);
    }
    // "button" bounds
    {
      Rectangle modelBounds = button.getModelBounds();
      assertEquals(new Rectangle(0, 0, 100, 23), modelBounds);
    }
    {
      Rectangle bounds = button.getBounds();
      assertEquals(new Rectangle(0, 0, 100, 23), bounds);
    }
  }

  public void test_Control_CREATE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ToolBar>",
        "  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'/>",
        "</ToolBar>");
    refresh();
    ToolItemInfo item = getObjectByName("item");
    // no control initially
    Assertions.assertThat(item.getPresentation().getChildrenTree()).isEmpty();
    // set Button on "item"
    ControlInfo newButton = createButton();
    simpleContainer_CREATE(item, newButton);
    // check result
    Assertions.assertThat(item.getPresentation().getChildrenTree()).containsExactly(newButton);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ToolBar>",
        "  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'>",
        "    <ToolItem.control>",
        "      <Button/>",
        "    </ToolItem.control>",
        "  </ToolItem>",
        "</ToolBar>");
  }

  public void test_Control_moveOut() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <ToolBar>",
            "    <ToolItem x:Style='SEPARATOR' width='100'>",
            "      <ToolItem.control>",
            "        <Button wbp:name='button'/>",
            "      </ToolItem.control>",
            "    </ToolItem>",
            "  </ToolBar>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // do move
    flowContainer_MOVE(shell.getLayout(), button, null);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ToolBar>",
        "    <ToolItem x:Style='SEPARATOR' width='100'/>",
        "  </ToolBar>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <ToolBar>",
        "    <ToolItem x:Style='SEPARATOR' width='100'>",
        "  <Button wbp:name='button'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    ToolBarInfo toolBar = parse("<ToolBar/>");
    refresh();
    // add items
    {
      ToolItemInfo toolItem = createObject("org.eclipse.swt.widgets.ToolItem", null);
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createObject("org.eclipse.swt.widgets.ToolItem", "check");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createObject("org.eclipse.swt.widgets.ToolItem", "radio");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createObject("org.eclipse.swt.widgets.ToolItem", "dropDown");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    {
      ToolItemInfo toolItem = createObject("org.eclipse.swt.widgets.ToolItem", "separator");
      flowContainer_CREATE(toolBar, toolItem, null);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ToolBar>",
        "  <ToolItem text='New Item'/>",
        "  <ToolItem x:Style='CHECK' text='Check Item'/>",
        "  <ToolItem x:Style='RADIO' text='Radio Item'/>",
        "  <ToolItem x:Style='DROP_DOWN' text='DropDown Item'/>",
        "  <ToolItem x:Style='SEPARATOR'/>",
        "</ToolBar>");
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