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
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TabFolderInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TabItemInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabFolderInfo}.
 * 
 * @author scheglov_ke
 */
public class TabFolderTest extends XwtModelTest {
  private static final Rectangle BOUNDS_EMPTY_ITEM = new Rectangle(2, 2, 48, 20);
  private static final Rectangle BOUNDS_BUTTON = new Rectangle(4, 24, 442, 272);

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
    TabFolderInfo folder = parse("<TabFolder/>");
    refresh();
    assertThat(folder.getItems()).isEmpty();
  }

  public void test_parse_withItem_noControl() throws Exception {
    TabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<TabFolder>",
            "  <TabItem wbp:name='item'/>",
            "</TabFolder>");
    refresh();
    TabItemInfo item = getObjectByName("item");
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
    TabFolderInfo folder =
        parse(
            "<TabFolder>",
            "  <TabItem wbp:name='item'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "</TabFolder>");
    refresh();
    TabItemInfo item = getObjectByName("item");
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
    TabFolderInfo folder =
        parse(
            "<TabFolder>",
            "  <TabItem wbp:name='item_1'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button_1'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "  <TabItem wbp:name='item_2'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button_2'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "</TabFolder>");
    refresh();
    TabItemInfo item_1 = getObjectByName("item_1");
    TabItemInfo item_2 = getObjectByName("item_2");
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
   * Test for {@link TabItemInfo#doSelect()}.
   */
  public void test_doSelect() throws Exception {
    TabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<TabFolder>",
            "  <TabItem wbp:name='item_1'/>",
            "  <TabItem wbp:name='item_2'/>",
            "</TabFolder>");
    refresh();
    TabItemInfo item_1 = getObjectByName("item_1");
    TabItemInfo item_2 = getObjectByName("item_2");
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
    TabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<TabFolder/>");
    refresh();
    //
    TabItemInfo newItem = createObject("org.eclipse.swt.widgets.TabItem");
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_CREATE(newItem, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<TabFolder>",
        "  <TabItem text='New Item'/>",
        "</TabFolder>");
  }

  public void test_item_MOVE() throws Exception {
    TabFolderInfo folder =
        parse(
            "<TabFolder>",
            "  <TabItem wbp:name='item_1'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button_1'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "  <TabItem wbp:name='item_2'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button_2'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "</TabFolder>");
    refresh();
    TabItemInfo item_1 = getObjectByName("item_1");
    TabItemInfo item_2 = getObjectByName("item_2");
    //
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_MOVE(item_2, item_1);
    assertXML(
        "<TabFolder>",
        "  <TabItem wbp:name='item_2'>",
        "    <TabItem.control>",
        "      <Button wbp:name='button_2'/>",
        "    </TabItem.control>",
        "  </TabItem>",
        "  <TabItem wbp:name='item_1'>",
        "    <TabItem.control>",
        "      <Button wbp:name='button_1'/>",
        "    </TabItem.control>",
        "  </TabItem>",
        "</TabFolder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands for controls
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_control_CREATE_onItem() throws Exception {
    TabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<TabFolder>",
            "  <TabItem wbp:name='otherItem'/>",
            "  <TabItem wbp:name='item'/>",
            "</TabFolder>");
    refresh();
    TabItemInfo item = getObjectByName("item");
    // initially "otherItem" is selected
    {
      TabItemInfo otherItem = getObjectByName("otherItem");
      assertSame(otherItem, folder.getSelectedItem());
    }
    //
    ControlInfo newButton = createButton();
    SimpleContainer simpleContainer = new SimpleContainerFactory(item, true).get().get(0);
    simpleContainer.command_CREATE(newButton);
    assertSame(item, folder.getSelectedItem());
    assertXML(
        "<TabFolder>",
        "  <TabItem wbp:name='otherItem'/>",
        "  <TabItem wbp:name='item'>",
        "    <TabItem.control>",
        "      <Button/>",
        "    </TabItem.control>",
        "  </TabItem>",
        "</TabFolder>");
  }

  public void test_control_ADD_onItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <TabFolder>",
        "    <TabItem wbp:name='item'/>",
        "  </TabFolder>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    TabItemInfo item = getObjectByName("item");
    XmlObjectInfo button = getObjectByName("button");
    //
    SimpleContainer simpleContainer = new SimpleContainerFactory(item, true).get().get(0);
    simpleContainer.command_ADD(button);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <TabFolder>",
        "    <TabItem wbp:name='item'>",
        "      <TabItem.control>",
        "        <Button wbp:name='button'/>",
        "      </TabItem.control>",
        "    </TabItem>",
        "  </TabFolder>",
        "</Shell>");
  }

  public void test_control_CREATE_onFolder() throws Exception {
    TabFolderInfo folder =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<TabFolder/>");
    refresh();
    //
    ControlInfo newButton = createButtonWithText();
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_CREATE(newButton, null);
    assertXML(
        "<TabFolder>",
        "  <TabItem text='New Item'>",
        "    <TabItem.control>",
        "      <Button text='New Button'/>",
        "    </TabItem.control>",
        "  </TabItem>",
        "</TabFolder>");
  }

  public void test_control_ADD_onFolder() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <TabFolder wbp:name='folder'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    TabFolderInfo folder = getObjectByName("folder");
    XmlObjectInfo button = getObjectByName("button");
    //
    FlowContainer flowContainer = new FlowContainerFactory(folder, true).get().get(0);
    flowContainer.command_MOVE(button, null);
    assertXML(
        "<Shell>",
        "  <TabFolder wbp:name='folder'>",
        "    <TabItem text='New Item'>",
        "      <TabItem.control>",
        "        <Button wbp:name='button'/>",
        "      </TabItem.control>",
        "    </TabItem>",
        "  </TabFolder>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage selected
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageSelected() throws Exception {
    TabFolderInfo panel =
        parse(
            "<TabFolder>",
            "  <TabItem wbp:name='item_1'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button_1'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "  <TabItem wbp:name='item_2'>",
            "    <TabItem.control>",
            "      <Button wbp:name='button_2'/>",
            "    </TabItem.control>",
            "  </TabItem>",
            "</TabFolder>");
    refresh();
    TabItemInfo item_1 = getObjectByName("item_1");
    TabItemInfo item_2 = getObjectByName("item_2");
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

  private static void assertSelectedItem(TabFolderInfo folder, TabItemInfo expected)
      throws Exception {
    assertSame(expected, folder.getSelectedItem());
  }
}