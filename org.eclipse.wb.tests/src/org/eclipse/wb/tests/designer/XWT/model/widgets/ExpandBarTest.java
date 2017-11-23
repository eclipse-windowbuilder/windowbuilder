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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ExpandBarInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ExpandItemInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.widgets.Control;
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
public class ExpandBarTest extends XwtModelTest {
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
    ExpandBarInfo expandBar =
        parse(
            "// filler filler filler filler filler",
            "<ExpandBar>",
            "  <ExpandItem wbp:name='item_1' text='000'/>",
            "  <ExpandItem wbp:name='item_2' text='111' height='200' expanded='true'/>",
            "</ExpandBar>");
    refresh();
    ExpandItemInfo item_1 = getObjectByName("item_1");
    ExpandItemInfo item_2 = getObjectByName("item_2");
    // check items
    List<ExpandItemInfo> items = expandBar.getItems();
    assertThat(items).containsExactly(item_1, item_2);
    // text
    assertEquals("000", ReflectionUtils.invokeMethod2(item_1.getObject(), "getText"));
    assertEquals("111", ReflectionUtils.invokeMethod2(item_2.getObject(), "getText"));
    // bounds for "item_1"
    {
      Rectangle modelBounds_1 = item_1.getModelBounds();
      assertThat(modelBounds_1.width).isGreaterThan(440);
      assertThat(modelBounds_1.height).isGreaterThan(20);
    }
    // bounds for "item_2"
    {
      Rectangle modelBounds_2 = item_2.getModelBounds();
      assertThat(modelBounds_2.width).isGreaterThan(440);
      assertThat(modelBounds_2.height).isGreaterThan(220);
    }
    // no Control
    assertSame(null, item_1.getControl());
  }

  /**
   * We should show on design canvas only {@link ControlInfo}'s of expanded {@link ExpandItemInfo} 
   * 's.
   */
  public void test_presentationChildrenGraphical() throws Exception {
    ExpandBarInfo expandBar =
        parse(
            "// filler filler filler filler filler",
            "<ExpandBar>",
            "  <ExpandItem wbp:name='item_1' expanded='true'>",
            "    <button wbp:name='button_1'/>",
            "  </ExpandItem>",
            "  <ExpandItem wbp:name='item_2'>",
            "    <button wbp:name='button_2'/>",
            "  </ExpandItem>",
            "</ExpandBar>");
    refresh();
    ExpandItemInfo item_1 = getObjectByName("item_1");
    ExpandItemInfo item_2 = getObjectByName("item_2");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    // "item_1" is expanded and "item_2" - not
    assertEquals(true, item_1.getPropertyByTitle("expanded").getValue());
    assertEquals(false, item_2.getPropertyByTitle("expanded").getValue());
    // ...so, "button_1" is in graphical children and "button_2" is not
    {
      {
        List<ObjectInfo> barChildren = expandBar.getPresentation().getChildrenGraphical();
        Assertions.assertThat(barChildren).containsExactly(item_1, item_2);
      }
      // "item_1"
      {
        List<ObjectInfo> children_1 = item_1.getPresentation().getChildrenGraphical();
        Assertions.assertThat(children_1).containsExactly(button_1);
      }
      {
        List<ObjectInfo> children_1 = item_1.getPresentation().getChildrenTree();
        Assertions.assertThat(children_1).containsExactly(button_1);
      }
      // "item_2"
      {
        List<ObjectInfo> children_2 = item_2.getPresentation().getChildrenGraphical();
        Assertions.assertThat(children_2).isEmpty();
      }
      {
        List<ObjectInfo> children_2 = item_2.getPresentation().getChildrenTree();
        Assertions.assertThat(children_2).containsExactly(button_2);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding {@link ControlInfo} to {@link ExpandItemInfo}.
   * <p>
   * Note, that because of https://bugs.eclipse.org/bugs/show_bug.cgi?id=308061 value of "height" is
   * replaced with preferred height of Control.
   */
  public void test_setControl_CREATE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ExpandBar>",
        "  <ExpandItem wbp:name='item' height='200'/>",
        "</ExpandBar>");
    refresh();
    ExpandItemInfo item = getObjectByName("item");
    // no control initially
    assertSame(null, item.getControl());
    // set Button on "item"
    ControlInfo newButton = createButton();
    simpleContainer_CREATE(item, newButton);
    // check result
    assertSame(newButton, item.getControl());
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ExpandBar>",
        "  <ExpandItem wbp:name='item' height='200' expanded='true'>",
        "    <ExpandItem.control>",
        "      <Button/>",
        "    </ExpandItem.control>",
        "  </ExpandItem>",
        "</ExpandBar>");
  }

  /**
   * Test for moving {@link Control} on {@link ExpandItem}.
   */
  public void test_setControl_moveIn() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ExpandBar>",
        "    <ExpandItem wbp:name='item'/>",
        "  </ExpandBar>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    ExpandItemInfo item = getObjectByName("item");
    ControlInfo button = getObjectByName("button");
    // no control initially
    assertSame(null, item.getControl());
    // set Button on "item"
    simpleContainer_ADD(item, button);
    assertSame(button, item.getControl());
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ExpandBar>",
        "    <ExpandItem wbp:name='item' expanded='true'>",
        "      <ExpandItem.control>",
        "        <Button wbp:name='button'/>",
        "      </ExpandItem.control>",
        "    </ExpandItem>",
        "  </ExpandBar>",
        "</Shell>");
  }

  /**
   * Test for moving {@link Control} from {@link ExpandItem}.
   */
  public void test_setControl_moveOut() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <ExpandBar>",
            "    <ExpandItem wbp:name='item' expanded='true'>",
            "      <ExpandItem.control>",
            "        <Button wbp:name='button'/>",
            "      </ExpandItem.control>",
            "    </ExpandItem>",
            "  </ExpandBar>",
            "</Shell>");
    refresh();
    ExpandItemInfo item = getObjectByName("item");
    ControlInfo button = getObjectByName("button");
    // has control initially
    assertSame(button, item.getControl());
    // move "button" to "shell"
    shell.getLayout().command_MOVE(button, null);
    assertSame(null, item.getControl());
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ExpandBar>",
        "    <ExpandItem wbp:name='item' expanded='true'/>",
        "  </ExpandBar>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create new {@link ExpandItem} on {@link ExpandBar}.
   */
  public void test_CREATE() throws Exception {
    ExpandBarInfo expandBar =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ExpandBar/>");
    assertThat(expandBar.getItems()).isEmpty();
    // add item
    ExpandItemInfo newItem = createObject("org.eclipse.swt.widgets.ExpandItem");
    flowContainer_CREATE(expandBar, newItem, null);
    assertThat(expandBar.getItems()).containsExactly(newItem);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ExpandBar>",
        "  <ExpandItem text='New ExpandItem'/>",
        "</ExpandBar>");
  }

  /**
   * Test for moving {@link ExpandItemInfo}.
   */
  public void test_MOVE() throws Exception {
    ExpandBarInfo expandBar =
        parse(
            "// filler filler filler filler filler",
            "<ExpandBar>",
            "  <ExpandItem wbp:name='item_1' expanded='true'>",
            "    <button wbp:name='button_1'/>",
            "  </ExpandItem>",
            "  <ExpandItem wbp:name='item_2'>",
            "    <button wbp:name='button_2'/>",
            "  </ExpandItem>",
            "</ExpandBar>");
    refresh();
    ExpandItemInfo item_1 = getObjectByName("item_1");
    ExpandItemInfo item_2 = getObjectByName("item_2");
    // move "item_2" before "item_1"
    flowContainer_MOVE(expandBar, item_2, item_1);
    assertXML(
        "// filler filler filler filler filler",
        "<ExpandBar>",
        "  <ExpandItem wbp:name='item_2'>",
        "    <button wbp:name='button_2'/>",
        "  </ExpandItem>",
        "  <ExpandItem wbp:name='item_1' expanded='true'>",
        "    <button wbp:name='button_1'/>",
        "  </ExpandItem>",
        "</ExpandBar>");
  }
}