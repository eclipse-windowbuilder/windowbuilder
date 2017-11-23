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
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CoolBarInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CoolItemInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

/**
 * Test for {@link CoolBarInfo}.
 * 
 * @author scheglov_ke
 */
public class CoolBarTest extends XwtModelTest {
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <CoolBar wbp:name='toolBar_1'/>",
        "  <CoolBar wbp:name='toolBar_2' x:Style='VERTICAL'/>",
        "</Shell>");
    refresh();
    {
      CoolBarInfo toolBar_1 = getObjectByName("toolBar_1");
      assertTrue(toolBar_1.isHorizontal());
      assertTrue(getFlowContainer(toolBar_1).isHorizontal());
    }
    {
      CoolBarInfo toolBar_2 = getObjectByName("toolBar_2");
      assertFalse(toolBar_2.isHorizontal());
      assertFalse(getFlowContainer(toolBar_2).isHorizontal());
    }
  }

  /**
   * {@link CoolBar} with {@link CoolItem}'s.
   */
  public void test_items_withSize() throws Exception {
    CoolBarInfo toolBar =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CoolBar>",
            "  <CoolItem wbp:name='item_1' size='200, 50'/>",
            "  <CoolItem wbp:name='item_2' size='100, 50'/>",
            "</CoolBar>");
    refresh();
    // prepare
    CoolItemInfo item_1 = getObjectByName("item_1");
    CoolItemInfo item_2 = getObjectByName("item_2");
    assertThat(toolBar.getItems()).containsExactly(item_1, item_2);
    // item_1
    {
      // bounds
      {
        Rectangle modelBounds = item_1.getModelBounds();
        assertEquals(new Rectangle(0, 0, 202, 50), modelBounds);
      }
      // no Control
      Assertions.assertThat(item_1.getPresentation().getChildrenTree()).isEmpty();
    }
    // item_2
    {
      // bounds
      {
        Rectangle modelBounds = item_2.getModelBounds();
        assertEquals(new Rectangle(202, 0, 248, 50), modelBounds);
      }
    }
  }

  /**
   * {@link CoolBar} with {@link CoolItem}'s.
   */
  public void test_items_noSize() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CoolBar>",
        "  <CoolItem wbp:name='item_1'/>",
        "  <CoolItem wbp:name='item_2'/>",
        "</CoolBar>");
    refresh();
    CoolItemInfo item_1 = getObjectByName("item_1");
    CoolItemInfo item_2 = getObjectByName("item_2");
    // item_1
    {
      Rectangle modelBounds = item_1.getModelBounds();
      assertEquals(new Rectangle(0, 0, 20, 25), modelBounds);
    }
    // item_2
    {
      Rectangle modelBounds = item_2.getModelBounds();
      assertEquals(new Rectangle(20, 0, 430, 25), modelBounds);
    }
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
        "<CoolBar>",
        "  <CoolItem wbp:name='item_2'/>",
        "  <CoolItem wbp:name='item_3' size='100, 50'>",
        "    <CoolItem.control>",
        "      <Button wbp:name='button' text='My Button'/>",
        "    </CoolItem.control>",
        "  </CoolItem>",
        "</CoolBar>");
    refresh();
    CoolItemInfo item_2 = getObjectByName("item_2");
    CoolItemInfo item_3 = getObjectByName("item_3");
    ControlInfo button = getObjectByName("button");
    // empty
    {
      SimpleContainer simpleContainer = new SimpleContainerFactory(item_2, false).get().get(0);
      assertTrue(simpleContainer.isEmpty());
      Assertions.assertThat(item_2.getPresentation().getChildrenTree()).isEmpty();
    }
    // not empty
    {
      SimpleContainer simpleContainer = new SimpleContainerFactory(item_3, false).get().get(0);
      assertFalse(simpleContainer.isEmpty());
      Assertions.assertThat(item_3.getPresentation().getChildrenTree()).containsOnly(button);
    }
    // "button" bounds
    {
      Rectangle modelBounds = button.getModelBounds();
      assertEquals(new Rectangle(11, 0, 419, 50), modelBounds);
    }
    {
      Rectangle bounds = button.getBounds();
      assertEquals(new Rectangle(11, 0, 419, 50), bounds);
    }
  }

  public void test_Control_CREATE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CoolBar>",
        "  <CoolItem wbp:name='item' x:Style='SEPARATOR' width='100'/>",
        "</CoolBar>");
    refresh();
    CoolItemInfo item = getObjectByName("item");
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
        "<CoolBar>",
        "  <CoolItem wbp:name='item' x:Style='SEPARATOR' width='100'>",
        "    <CoolItem.control>",
        "      <Button/>",
        "    </CoolItem.control>",
        "  </CoolItem>",
        "</CoolBar>");
  }

  public void test_Control_moveOut() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <CoolBar>",
            "    <CoolItem x:Style='SEPARATOR' width='100'>",
            "      <CoolItem.control>",
            "        <Button wbp:name='button'/>",
            "      </CoolItem.control>",
            "    </CoolItem>",
            "  </CoolBar>",
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
        "  <CoolBar>",
        "    <CoolItem x:Style='SEPARATOR' width='100'/>",
        "  </CoolBar>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <CoolBar>",
        "    <CoolItem x:Style='SEPARATOR' width='100'>",
        "  <Button wbp:name='button'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    CoolBarInfo coolBar = parse("<CoolBar/>");
    refresh();
    // add item
    {
      CoolItemInfo newItem = createObject("org.eclipse.swt.widgets.CoolItem", null);
      flowContainer_CREATE(coolBar, newItem, null);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CoolBar>",
        "  <CoolItem/>",
        "</CoolBar>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static FlowContainer getFlowContainer(CoolBarInfo toolBar) {
    return new FlowContainerFactory(toolBar, true).get().get(0);
  }
}