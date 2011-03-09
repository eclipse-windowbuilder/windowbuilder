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
package org.eclipse.wb.tests.designer.ercp.model.widgets.mobile;

import org.eclipse.wb.core.model.association.InvocationChildArrayAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalVariableSupport;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxInfo;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.ercp.ETestUtils;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

import java.util.List;

/**
 * Tests for {@link ListBoxInfo}.
 * 
 * @author scheglov_ke
 */
public class ListBoxTest extends ErcpModelTest {
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
   * <code>ListBox</code> is special control, if there are no items, its size is (0,0) and image is
   * <code>null</code>. But we don't like this, so need some solution to prevent such situation.
   */
  public void test_liveImage() throws Exception {
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    //
    ControlInfo listBox = ETestUtils.createControl("org.eclipse.ercp.swt.mobile.ListBox");
    assertNotNull(listBox.getImage());
  }

  public void test_parse_ListBox() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "  }",
            "}");
    shell.refresh();
    //
    assertInstanceOf(ListBoxInfo.class, shell.getChildrenControls().get(0));
  }

  public void test_add_ListBox() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    //
    assertTrue(shell.getChildrenControls().isEmpty());
    //
    ListBoxInfo listBox = createJavaInfo("org.eclipse.ercp.swt.mobile.ListBox");
    assertNotNull(listBox);
    shell.getLayout().command_CREATE(listBox, null);
    //
    assertEquals(1, shell.getChildrenControls().size());
    assertSame(listBox, shell.getChildrenControls().get(0));
    assertSame(shell, listBox.getParent());
    //
    assertEquals(3, listBox.getItems().size());
    //
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "      listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #1', null, 'heading #1', null), new ListBoxItem('detail #2', null, 'heading #2', null), new ListBoxItem('detail #3', null, 'heading #3', null)});",
        "    }",
        "  }",
        "}");
  }

  /**
   * Parse test: items sets as empty variables.
   */
  public void test_dataItems_1() throws Exception {
    test_dataItems(
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem(\"detail #1\", null, \"heading #1\", null), new ListBoxItem(\"detail #2\", null, \"heading #2\", new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")))});",
            "  }",
            "}"},
        EmptyVariableSupport.class,
        EmptyVariableSupport.class);
  }

  /**
   * Parse test: items sets as local variables.
   */
  public void test_dataItems_2() throws Exception {
    test_dataItems(
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    ListBoxItem item0 = new ListBoxItem(\"detail #1\", null, \"heading #1\", null);",
            "    ListBoxItem item1 = new ListBoxItem(\"detail #2\", null, \"heading #2\", new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
            "    listBox.setDataModel(new ListBoxItem[]{item0, item1});",
            "  }",
            "}"},
        LocalVariableSupport.class,
        LocalVariableSupport.class);
  }

  /**
   * Parse test: items sets as empty and local variables.
   */
  public void test_dataItems_3() throws Exception {
    test_dataItems(
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    ListBoxItem item1 = new ListBoxItem(\"detail #2\", null, \"heading #2\", new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem(\"detail #1\", null, \"heading #1\", null), item1});",
            "  }",
            "}"},
        EmptyVariableSupport.class,
        LocalVariableSupport.class);
  }

  /**
   * Parse test: items array sets as SimpleName.
   */
  public void test_dataItems_4() throws Exception {
    try {
      test_dataItems(
          new String[]{
              "class Test extends Shell {",
              "  public Test() {",
              "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
              "    ListBoxItem item1 = new ListBoxItem(\"detail #2\", null, \"heading #2\", new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
              "    ListBoxItem []items = new ListBoxItem[2];",
              "    items[0] = new ListBoxItem(\"detail #1\", null, \"heading #1\", null);",
              "    items[1] = item1;",
              "    listBox.setDataModel(items);",
              "  }",
              "}"},
          LocalVariableSupport.class,
          LocalVariableSupport.class);
      fail();
    } catch (Throwable e) {
    }
  }

  private void test_dataItems(String[] source,
      Class<?> variableSupportClass0,
      Class<?> variableSupportClass1) throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell = parseComposite(source);
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    // check items
    List<ListBoxItemInfo> items = listBox.getItems();
    assertNotNull(items);
    assertEquals(2, items.size());
    // check item0
    ListBoxItemInfo item0 = items.get(0);
    assertEquals(
        "org.eclipse.ercp.swt.mobile.ListBoxItem",
        item0.getDescription().getComponentClass().getName());
    assertInstanceOf(ConstructorCreationSupport.class, item0.getCreationSupport());
    assertInstanceOf(variableSupportClass0, item0.getVariableSupport());
    assertInstanceOf(InvocationChildArrayAssociation.class, item0.getAssociation());
    // check item0 properties
    Property detailText0 = item0.getPropertyByTitle("detailText");
    assertNotNull(detailText0);
    assertEquals("detail #1", detailText0.getValue());
    //
    Property headingText0 = item0.getPropertyByTitle("headingText");
    assertNotNull(headingText0);
    assertEquals("heading #1", headingText0.getValue());
    //
    Property detailIcon0 = item0.getPropertyByTitle("detailIcon");
    assertNotNull(detailIcon0);
    assertNull(detailIcon0.getValue());
    //
    Property headingIcon0 = item0.getPropertyByTitle("headingIcon");
    assertNotNull(headingIcon0);
    assertNull(headingIcon0.getValue());
    // check item1
    ListBoxItemInfo item1 = items.get(1);
    assertEquals(
        "org.eclipse.ercp.swt.mobile.ListBoxItem",
        item1.getDescription().getComponentClass().getName());
    assertInstanceOf(ConstructorCreationSupport.class, item1.getCreationSupport());
    assertInstanceOf(variableSupportClass1, item1.getVariableSupport());
    assertInstanceOf(InvocationChildArrayAssociation.class, item1.getAssociation());
    // check item0 properties
    Property detailText1 = item1.getPropertyByTitle("detailText");
    assertNotNull(detailText1);
    assertEquals("detail #2", detailText1.getValue());
    //
    Property headingText1 = item1.getPropertyByTitle("headingText");
    assertNotNull(headingText1);
    assertEquals("heading #2", headingText1.getValue());
    //
    Property detailIcon1 = item1.getPropertyByTitle("detailIcon");
    assertNotNull(detailIcon1);
    assertNull(detailIcon1.getValue());
    //
    Property headingIcon1 = item1.getPropertyByTitle("headingIcon");
    assertNotNull(headingIcon1);
    assertNotNull(headingIcon1.getValue());
  }

  /**
   * Delete item sets as empty variable.
   */
  public void test_delete_1() throws Exception {
    check_delete(
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem(\"detail #1\", null, \"heading #1\", null), new ListBoxItem(\"detail #2\", null, \"heading #2\", new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")))});",
            "  }",
            "}"},
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem(\"detail #1\", null, \"heading #1\", null)});",
            "  }",
            "}"});
  }

  /**
   * Delete item sets as local variable.
   */
  public void test_delete_2() throws Exception {
    check_delete(
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    ListBoxItem item0 = new ListBoxItem(\"detail #1\", null, \"heading #1\", null);",
            "    ListBoxItem item1 = new ListBoxItem(\"detail #2\", null, \"heading #2\", new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
            "    item1.setDetailText(\"ZZZ\");",
            "    listBox.setDataModel(new ListBoxItem[]{item0, item1});",
            "  }",
            "}"},
        new String[]{
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    ListBoxItem item0 = new ListBoxItem(\"detail #1\", null, \"heading #1\", null);",
            "    listBox.setDataModel(new ListBoxItem[]{item0});",
            "  }",
            "}"});
  }

  /**
   * Delete <code>setDataModel()</code> method after delete last item.
   */
  public void test_delete_3() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #1', null, 'heading #1', null)});",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    // check items
    List<ListBoxItemInfo> items = listBox.getItems();
    assertNotNull(items);
    assertEquals(1, items.size());
    // delete
    ListBoxItemInfo item = items.get(0);
    item.delete();
    //
    // check items
    items = listBox.getItems();
    assertNotNull(items);
    assertTrue(items.isEmpty());
    assertNull(listBox.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "  }",
        "}");
  }

  private void check_delete(String[] startSource, String[] endSource) throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell = parseComposite(startSource);
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    // check items
    List<ListBoxItemInfo> items = listBox.getItems();
    assertNotNull(items);
    assertEquals(2, items.size());
    // delete
    ListBoxItemInfo item1 = items.get(1);
    item1.delete();
    //
    // check items
    items = listBox.getItems();
    assertNotNull(items);
    assertEquals(1, items.size());
    // check source
    assertEditor(endSource);
  }

  /**
   * Add new item as last item.
   */
  public void test_add_0() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #1', null, 'heading #1', null)});",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    // check items
    List<ListBoxItemInfo> items = listBox.getItems();
    assertNotNull(items);
    assertEquals(1, items.size());
    //
    ListBoxItemInfo newItem = createJavaInfo("org.eclipse.ercp.swt.mobile.ListBoxItem");
    //
    listBox.add(newItem, null);
    // check items
    items = listBox.getItems();
    assertNotNull(items);
    assertEquals(2, items.size());
    assertSame(newItem, items.get(1));
    assertSame(listBox, newItem.getParent());
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #1', null, 'heading #1', null), new ListBoxItem('detail #2', null, 'heading #2', null)});",
        "  }",
        "}");
  }

  /**
   * Add new item as first item.
   */
  public void test_add_1() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #1', null, 'heading #1', null)});",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    // check items
    List<ListBoxItemInfo> items = listBox.getItems();
    assertNotNull(items);
    assertEquals(1, items.size());
    //
    ListBoxItemInfo newItem = createJavaInfo("org.eclipse.ercp.swt.mobile.ListBoxItem");
    //
    listBox.add(newItem, items.get(0));
    // check items
    items = listBox.getItems();
    assertNotNull(items);
    assertEquals(2, items.size());
    assertSame(newItem, items.get(0));
    assertSame(listBox, newItem.getParent());
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #2', null, 'heading #2', null), new ListBoxItem('detail #1', null, 'heading #1', null)});",
        "  }",
        "}");
  }

  /**
   * Add new item when method <code>setDataModel()</code> not exist.
   */
  public void test_add_2() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    // check items
    List<ListBoxItemInfo> items = listBox.getItems();
    assertNotNull(items);
    assertTrue(items.isEmpty());
    // check setData
    assertNull(listBox.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    //
    ListBoxItemInfo newItem = createJavaInfo("org.eclipse.ercp.swt.mobile.ListBoxItem");
    //
    listBox.add(newItem, null);
    // check items
    items = listBox.getItems();
    assertNotNull(items);
    assertEquals(1, items.size());
    assertSame(newItem, items.get(0));
    assertSame(listBox, newItem.getParent());
    // check setData
    assertNotNull(listBox.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail #1', null, 'heading #1', null)});",
        "  }",
        "}");
  }

  /**
   * Test move items into one listBox.
   */
  public void test_move_0() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail0', null, 'heading0', null), new ListBoxItem('detail1', null, 'heading1', null), new ListBoxItem('detail2', null, 'heading2', null)});",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox = (ListBoxInfo) shell.getChildrenControls().get(0);
    assertEquals(3, listBox.getItems().size());
    //
    ListBoxItemInfo item = listBox.getItems().get(0);
    listBox.move(item, null);
    //
    assertEquals(3, listBox.getItems().size());
    assertSame(listBox, item.getParent());
    assertSame(item, listBox.getItems().get(2));
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail1', null, 'heading1', null), new ListBoxItem('detail2', null, 'heading2', null), new ListBoxItem('detail0', null, 'heading0', null)});",
        "  }",
        "}");
    //
    listBox.move(listBox.getItems().get(1), listBox.getItems().get(0));
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox.setDataModel(new ListBoxItem[]{new ListBoxItem('detail2', null, 'heading2', null), new ListBoxItem('detail1', null, 'heading1', null), new ListBoxItem('detail0', null, 'heading0', null)});",
        "  }",
        "}");
  }

  /**
   * Test move items between two listBox's. Moved item location into listBox with one item, and
   * target listBox not contains method <code>setDataModel()</code>. Check delete
   * <code>setDataModel()</code> from source listBox, and add <code>setDataModel()</code> to target
   * listBox.
   */
  public void test_move_1() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox1 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox1.setDataModel(new ListBoxItem[]{new ListBoxItem('detail', null, 'heading', null)});",
            "    ListBox listBox2 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox1 = (ListBoxInfo) shell.getChildrenControls().get(0);
    assertEquals(1, listBox1.getItems().size());
    assertNotNull(listBox1.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    //
    ListBoxInfo listBox2 = (ListBoxInfo) shell.getChildrenControls().get(1);
    assertTrue(listBox2.getItems().isEmpty());
    assertNull(listBox2.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    //
    ListBoxItemInfo item = listBox1.getItems().get(0);
    listBox2.move(item, null);
    //
    assertTrue(listBox1.getItems().isEmpty());
    assertEquals(1, listBox2.getItems().size());
    assertSame(listBox2, item.getParent());
    assertSame(item, listBox2.getItems().get(0));
    assertNull(listBox1.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    assertNotNull(listBox2.getMethodInvocation(ListBoxInfo.SET_DATA_MODEL_SIGNATURE));
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox1 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    ListBox listBox2 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox2.setDataModel(new ListBoxItem[]{new ListBoxItem('detail', null, 'heading', null)});",
        "  }",
        "}");
  }

  /**
   * Test move items between two listBox's.
   */
  public void test_move_2() throws Exception {
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    ListBox listBox1 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox1.setDataModel(new ListBoxItem[]{new ListBoxItem('detail', null, 'heading', null), new ListBoxItem('detail1', null, 'heading1', null)});",
            "    ListBox listBox2 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
            "    listBox2.setDataModel(new ListBoxItem[]{new ListBoxItem('detail0', null, 'heading0', null), new ListBoxItem('detail2', null, 'heading2', null)});",
            "  }",
            "}");
    shell.refresh();
    //
    ListBoxInfo listBox1 = (ListBoxInfo) shell.getChildrenControls().get(0);
    assertEquals(2, listBox1.getItems().size());
    //
    ListBoxInfo listBox2 = (ListBoxInfo) shell.getChildrenControls().get(1);
    assertEquals(2, listBox2.getItems().size());
    //
    ListBoxItemInfo item1 = listBox1.getItems().get(1);
    ListBoxItemInfo item2 = listBox2.getItems().get(1);
    listBox2.move(item1, item2);
    //
    assertEquals(1, listBox1.getItems().size());
    assertEquals(3, listBox2.getItems().size());
    assertSame(listBox2, item1.getParent());
    assertSame(item1, listBox2.getItems().get(1));
    // check source
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    ListBox listBox1 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox1.setDataModel(new ListBoxItem[]{new ListBoxItem('detail', null, 'heading', null)});",
        "    ListBox listBox2 = new ListBox(this, SWT.BORDER, ListBox.LB_STYLE_1LINE_ITEM);",
        "    listBox2.setDataModel(new ListBoxItem[]{new ListBoxItem('detail0', null, 'heading0', null), new ListBoxItem('detail1', null, 'heading1', null), new ListBoxItem('detail2', null, 'heading2', null)});",
        "  }",
        "}");
  }
}