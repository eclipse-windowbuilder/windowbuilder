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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.xwt.model.widgets.CTabFolderInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CTabItemInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link CTabFolderInfo}.
 * 
 * @author scheglov_ke
 */
public class CTabFolderTest extends XwtModelTest {
  private static final Rectangle BOUNDS_EMPTY_ITEM = new Rectangle(0, 0, 8, 19);
  private static final Rectangle BOUNDS_BUTTON = new Rectangle(2, 22, 446, 276);

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
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse_noItem() throws Exception {
    CTabFolderInfo folder = parse("<CTabFolder/>");
    refresh();
    assertThat(folder.getItems()).isEmpty();
  }

  public void test_parse_withItem_noControl() throws Exception {
    CTabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CTabFolder>",
            "  <CTabItem wbp:name='item'/>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item = getObjectByName("item");
    // hierarchy
    assertThat(folder.getItems()).containsExactly(item);
    assertSame(null, item.getControl());
    // presentation
    {
      IObjectPresentation presentation = folder.getPresentation();
      assertThat(presentation.getChildrenTree()).containsExactly(item);
      assertThat(presentation.getChildrenGraphical()).containsExactly(item);
    }
  }

  public void test_parse_withItem_withControl() throws Exception {
    CTabFolderInfo folder =
        parse(
            "<CTabFolder>",
            "  <CTabItem wbp:name='item'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item = getObjectByName("item");
    ControlInfo button = getObjectByName("button");
    // hierarchy
    assertThat(folder.getItems()).containsExactly(item);
    assertSame(button, item.getControl());
    // presentation "folder"
    {
      IObjectPresentation presentation = folder.getPresentation();
      assertThat(presentation.getChildrenTree()).containsExactly(item);
      assertThat(presentation.getChildrenGraphical()).containsExactly(item, button);
    }
    // presentation "item"
    {
      IObjectPresentation presentation = item.getPresentation();
      assertThat(presentation.getChildrenTree()).containsExactly(button);
      assertThat(presentation.getChildrenGraphical()).isEmpty();
    }
    // bounds
    {
      Rectangle bounds = item.getBounds();
      assertEquals(BOUNDS_EMPTY_ITEM, bounds);
    }
    {
      Rectangle bounds = button.getBounds();
      assertEquals(BOUNDS_BUTTON, bounds);
    }
  }

  public void test_parse_withItems_withControls() throws Exception {
    CTabFolderInfo folder =
        parse(
            "<CTabFolder>",
            "  <CTabItem wbp:name='item_1'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button_1'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "  <CTabItem wbp:name='item_2'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button_2'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item_1 = getObjectByName("item_1");
    CTabItemInfo item_2 = getObjectByName("item_2");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    // hierarchy
    assertThat(folder.getItems()).containsExactly(item_1, item_2);
    assertSame(button_1, item_1.getControl());
    assertSame(button_2, item_2.getControl());
    // presentation
    {
      IObjectPresentation presentation = folder.getPresentation();
      {
        List<ObjectInfo> children = presentation.getChildrenTree();
        assertThat(children).containsExactly(item_1, item_2);
      }
      {
        List<ObjectInfo> children = presentation.getChildrenGraphical();
        assertThat(children).containsExactly(item_1, item_2, button_1);
      }
    }
    // bounds
    {
      Rectangle bounds = item_1.getBounds();
      assertEquals(BOUNDS_EMPTY_ITEM, bounds);
    }
    {
      Rectangle bounds = button_1.getBounds();
      assertEquals(BOUNDS_BUTTON, bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CTabItemInfo#doSelect()}.
   */
  public void test_doSelect() throws Exception {
    CTabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CTabFolder>",
            "  <CTabItem wbp:name='item_1'/>",
            "  <CTabItem wbp:name='item_2'/>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item_1 = getObjectByName("item_1");
    CTabItemInfo item_2 = getObjectByName("item_2");
    // initially "item_1" selected
    assertSame(item_1, folder.getSelectedItem());
    // select "item_2"
    item_2.doSelect();
    assertSame(item_2, folder.getSelectedItem());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands for items
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_item_CREATE() throws Exception {
    CTabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CTabFolder/>");
    refresh();
    //
    CTabItemInfo newItem = createObject("org.eclipse.swt.custom.CTabItem");
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_CREATE(newItem, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CTabFolder>",
        "  <CTabItem text='New Item'/>",
        "</CTabFolder>");
  }

  public void test_item_MOVE() throws Exception {
    CTabFolderInfo folder =
        parse(
            "<CTabFolder>",
            "  <CTabItem wbp:name='item_1'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button_1'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "  <CTabItem wbp:name='item_2'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button_2'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item_1 = getObjectByName("item_1");
    CTabItemInfo item_2 = getObjectByName("item_2");
    //
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_MOVE(item_2, item_1);
    assertXML(
        "<CTabFolder>",
        "  <CTabItem wbp:name='item_2'>",
        "    <CTabItem.control>",
        "      <Button wbp:name='button_2'/>",
        "    </CTabItem.control>",
        "  </CTabItem>",
        "  <CTabItem wbp:name='item_1'>",
        "    <CTabItem.control>",
        "      <Button wbp:name='button_1'/>",
        "    </CTabItem.control>",
        "  </CTabItem>",
        "</CTabFolder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands for controls
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_control_CREATE_onItem() throws Exception {
    CTabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CTabFolder>",
            "  <CTabItem wbp:name='otherItem'/>",
            "  <CTabItem wbp:name='item'/>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item = getObjectByName("item");
    // initially "otherItem" is selected
    {
      CTabItemInfo otherItem = getObjectByName("otherItem");
      assertSame(otherItem, folder.getSelectedItem());
    }
    //
    ControlInfo newButton = createButton();
    SimpleContainer simpleContainer = new SimpleContainerFactory(item, true).get().get(0);
    simpleContainer.command_CREATE(newButton);
    assertSame(item, folder.getSelectedItem());
    assertXML(
        "<CTabFolder>",
        "  <CTabItem wbp:name='otherItem'/>",
        "  <CTabItem wbp:name='item'>",
        "    <CTabItem.control>",
        "      <Button/>",
        "    </CTabItem.control>",
        "  </CTabItem>",
        "</CTabFolder>");
  }

  public void test_control_ADD_onItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <CTabFolder>",
        "    <CTabItem wbp:name='item'/>",
        "  </CTabFolder>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    CTabItemInfo item = getObjectByName("item");
    XmlObjectInfo button = getObjectByName("button");
    //
    SimpleContainer simpleContainer = new SimpleContainerFactory(item, true).get().get(0);
    simpleContainer.command_ADD(button);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <CTabFolder>",
        "    <CTabItem wbp:name='item'>",
        "      <CTabItem.control>",
        "        <Button wbp:name='button'/>",
        "      </CTabItem.control>",
        "    </CTabItem>",
        "  </CTabFolder>",
        "</Shell>");
  }

  public void test_control_CREATE_onFolder() throws Exception {
    CTabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CTabFolder/>");
    refresh();
    //
    ControlInfo newButton = createButtonWithText();
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_CREATE(newButton, null);
    assertXML(
        "<CTabFolder>",
        "  <CTabItem text='New Item'>",
        "    <CTabItem.control>",
        "      <Button text='New Button'/>",
        "    </CTabItem.control>",
        "  </CTabItem>",
        "</CTabFolder>");
  }

  public void test_control_ADD_onFolder() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <CTabFolder wbp:name='folder'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    CTabFolderInfo folder = getObjectByName("folder");
    XmlObjectInfo button = getObjectByName("button");
    //
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_MOVE(button, null);
    assertXML(
        "<Shell>",
        "  <CTabFolder wbp:name='folder'>",
        "    <CTabItem text='New Item'>",
        "      <CTabItem.control>",
        "        <Button wbp:name='button'/>",
        "      </CTabItem.control>",
        "    </CTabItem>",
        "  </CTabFolder>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage selected
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageSelected() throws Exception {
    CTabFolderInfo panel =
        parse(
            "<CTabFolder>",
            "  <CTabItem wbp:name='item_1'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button_1'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "  <CTabItem wbp:name='item_2'>",
            "    <CTabItem.control>",
            "      <Button wbp:name='button_2'/>",
            "    </CTabItem.control>",
            "  </CTabItem>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item_1 = getObjectByName("item_1");
    CTabItemInfo item_2 = getObjectByName("item_2");
    ControlInfo button_1 = getObjectByName("button_1");
    // initially "item_1" is selected
    assertSelectedItem(panel, item_1);
    // notify about "item_2"
    {
      boolean shouldRefresh = notifySelecting(item_2);
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "item_2" is selected
      assertSelectedItem(panel, item_2);
    }
    // notify about "button_1"
    {
      boolean shouldRefresh = notifySelecting(button_1);
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "item_1" is selected
      assertSelectedItem(panel, item_1);
    }
    // second notification about "item_1" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(item_1);
      assertFalse(shouldRefresh);
    }
  }

  private static void assertSelectedItem(CTabFolderInfo folder, CTabItemInfo expected)
      throws Exception {
    assertSame(expected, folder.getSelectedItem());
  }
}