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
package org.eclipse.wb.tests.designer.swing.model.component.menu;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuSeparatorCreationSupport;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuSeparatorInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * Test for {@link JMenuInfo}.
 * 
 * @author scheglov_ke
 */
public class JMenuTest extends SwingModelTest {
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
   * When {@link JMenu} has no items, its size is very small, so we should add some text to make it
   * bigger.
   */
  public void test_renderEmpty() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "    }",
        "  }",
        "}");
    refresh();
    // check JMenu_Info
    JMenuInfo menuInfo = getJavaInfoByName("menu");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    assertTrue(menuObject.getBounds().width > 100);
    assertTrue(menuObject.getBounds().height > 15);
  }

  /**
   * We should dispose all {@link Image}s related to {@link JMenuInfo}.
   */
  public void test_disposeImages() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "    }",
        "  }",
        "}");
    refresh();
    // prepare JMenu_Info
    JMenuInfo menuInfo = getJavaInfoByName("menu");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    Image asItem = menuInfo.getImage();
    Image asMenu = menuObject.getImage();
    // initially images exist
    assertFalse(asItem.isDisposed());
    assertFalse(asMenu.isDisposed());
    // dispose model
    disposeLastModel();
    assertTrue(asItem.isDisposed());
    assertTrue(asMenu.isDisposed());
  }

  /**
   * Even if we place {@link JMenu} on generic {@link Container}, this should provide correct
   * bounds.
   */
  public void test_onContainer() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      add(menu);",
        "    }",
        "  }",
        "}");
    refresh();
    JMenuInfo menu = getJavaInfoByName("menu");
    // reasonable bounds
    Rectangle bounds = menu.getBounds();
    assertEquals(new Rectangle(0, 0, 450, 300), bounds);
  }

  /**
   * If we have deep hierarchy of {@link JMenu}s, we still should correctly fetch information.
   */
  public void test_deepHierarchy() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    JMenu menu_1 = new JMenu('A');",
        "    JMenu menu_2 = new JMenu('B');",
        "    JMenu menu_3 = new JMenu('C');",
        "    setJMenuBar(menuBar);",
        "    menuBar.add(menu_1);",
        "    menu_1.add(menu_2);",
        "    menu_2.add(menu_3);",
        "  }",
        "}");
    refresh();
    // prepare models
    JMenuInfo menu_1 = getJavaInfoByName("menu_1");
    JMenuInfo menu_2 = getJavaInfoByName("menu_2");
    JMenuInfo menu_3 = getJavaInfoByName("menu_3");
    IMenuInfo menuObject_1 = MenuObjectInfoUtils.getMenuInfo(menu_1);
    IMenuInfo menuObject_2 = MenuObjectInfoUtils.getMenuInfo(menu_2);
    IMenuInfo menuObject_3 = MenuObjectInfoUtils.getMenuInfo(menu_3);
    // reasonable bounds
    assertNotNull(menu_1.getBounds());
    assertNotNull(menu_2.getBounds());
    assertNotNull(menu_3.getBounds());
    assertNotNull(menuObject_1.getBounds());
    assertNotNull(menuObject_2.getBounds());
    assertNotNull(menuObject_3.getBounds());
  }

  /**
   * Test for {@link IMenuItemInfo} and {@link IMenuInfo} from {@link JMenuInfo}.
   */
  public void test_IMenuItemInfo_IMenuInfo() throws Exception {
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "      {",
            "        JMenuItem item_1 = new JMenuItem('Item 1');",
            "        menu.add(item_1);",
            "      }",
            "      {",
            "        JMenuItem item_2 = new JMenuItem('Item 2');",
            "        menu.add(item_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frameInfo.refresh();
    // prepare models
    JMenuInfo menuInfo = getJavaInfoByName("menu");
    JMenuItemInfo itemInfo_1 = getJavaInfoByName("item_1");
    JMenuItemInfo itemInfo_2 = getJavaInfoByName("item_2");
    // no adapter for unknown class
    assertNull(menuInfo.getAdapter(List.class));
    // check IMenuItemInfo
    IMenuItemInfo itemObject;
    {
      itemObject = MenuObjectInfoUtils.getMenuItemInfo(menuInfo);
      assertSame(menuInfo, itemObject.getModel());
      // presentation
      assertNull(itemObject.getImage());
      assertEquals(menuInfo.getBounds(), itemObject.getBounds());
      // menu
      assertSame(MenuObjectInfoUtils.getMenuInfo(menuInfo), itemObject.getMenu());
    }
    // check IMenuInfo
    IMenuInfo menuObject;
    {
      menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
      assertSame(menuObject, menuObject.getModel());
      // presentation
      assertNotNull(menuObject.getImage());
      assertTrue(menuObject.getBounds().width > 50);
      assertTrue(menuObject.getBounds().height > 2 * 15);
      assertFalse(menuObject.isHorizontal());
      // items
      List<IMenuItemInfo> items = menuObject.getItems();
      assertSame(itemInfo_1, items.get(0).getModel());
      assertSame(itemInfo_2, items.get(1).getModel());
    }
    // policy for "item" and "menu" projections are same
    assertSame(itemObject.getPolicy(), menuObject.getPolicy());
  }

  /**
   * Test for {@link IMenuItemInfo} and {@link IMenuInfo} from {@link JMenuInfo}.
   * <p>
   * {@link JSeparator} should be {@link IMenuItemInfo}.
   */
  public void test_IMenuInfo_withSeparator() throws Exception {
    ContainerInfo frameInfo =
        parseContainer(getDoubleQuotes(new String[]{
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "      {",
            "        JMenuItem item_1 = new JMenuItem('Item 1');",
            "        menu.add(item_1);",
            "      }",
            "      {",
            "        JSeparator separator = new JSeparator();",
            "        menu.add(separator);",
            "      }",
            "    }",
            "  }",
            "}"}));
    frameInfo.refresh();
    // prepare models
    JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
    JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
    JMenuItemInfo itemInfo_1 = (JMenuItemInfo) menuInfo.getChildrenComponents().get(0);
    ComponentInfo separatorInfo = menuInfo.getChildrenComponents().get(1);
    // check IMenuInfo
    IMenuInfo menuObject;
    {
      menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
      assertSame(menuObject, menuObject.getModel());
      // presentation
      assertNotNull(menuObject.getImage());
      {
        Rectangle bounds = menuObject.getBounds();
        assertThat(bounds.width > 50);
        assertThat(bounds.height > 2 * 15);
      }
      assertFalse(menuObject.isHorizontal());
      // items
      List<IMenuItemInfo> items = menuObject.getItems();
      assertThat(items).hasSize(2);
      // item_1
      {
        IMenuItemInfo itemObject = items.get(0);
        assertSame(itemInfo_1, itemObject.getModel());
        // presentation
        Rectangle bounds = itemObject.getBounds();
        assertThat(bounds.x).isLessThan(5);
        assertThat(bounds.y).isLessThan(5);
        assertThat(bounds.width).isGreaterThan(50);
        assertThat(bounds.height).isGreaterThan(18);
      }
      // JSeparator
      {
        IMenuItemInfo itemObject = items.get(1);
        assertSame(separatorInfo, itemObject.getModel());
        // presentation
        Rectangle bounds = itemObject.getBounds();
        assertThat(bounds.x).isGreaterThanOrEqualTo(0).isLessThan(5);
        assertThat(bounds.width).isGreaterThan(50);
        assertThat(bounds.height).isGreaterThan(0).isLessThan(5);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We can not drop new invalid objects.
   */
  public void test_IMenuInfo_CREATE_noObject() throws Exception {
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "    }",
            "  }",
            "}");
    frameInfo.refresh();
    // prepare models
    JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
    JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // initially no "item" objects
    assertEquals(0, menuObject.getItems().size());
    // can not drop arbitrary Object
    {
      JavaInfo newObject = createJavaInfo("java.lang.Object");
      assertFalse(policy.validateCreate(newObject));
    }
    // can drop javax.swing.JPopupMenu
    {
      JavaInfo newPopup = createJavaInfo("javax.swing.JPopupMenu");
      assertFalse(policy.validateCreate(newPopup));
    }
  }

  /**
   * We can drop new {@link JMenuItemInfo}.
   */
  public void test_IMenuInfo_CREATE() throws Exception {
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "    }",
            "  }",
            "}");
    frameInfo.refresh();
    // prepare models
    JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
    JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // initially no "item" objects
    assertEquals(0, menuObject.getItems().size());
    // add new JMenuItem_Info
    JMenuItemInfo newItemInfo = (JMenuItemInfo) createComponent(JMenuItem.class);
    assertTrue(policy.validateCreate(newItemInfo));
    policy.commandCreate(newItemInfo, null);
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JMenuItem menuItem = new JMenuItem('New menu item');",
        "        menu.add(menuItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * We can drop any {@link Component}.
   */
  public void test_IMenuInfo_CREATE_component() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "    }",
        "  }",
        "}");
    refresh();
    // prepare models
    JMenuInfo menuInfo = getJavaInfoByName("menu");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // initially no "item" objects
    assertEquals(0, menuObject.getItems().size());
    // add new JButton
    ComponentInfo newComponent = createJButton();
    assertTrue(policy.validateCreate(newComponent));
    policy.commandCreate(newComponent, null);
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JButton button = new JButton();",
        "        menu.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * We can move {@link JMenuItemInfo}'s.
   */
  public void test_IMenuInfo_MOVE() throws Exception {
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "      {",
            "        JMenuItem item_1 = new JMenuItem('Item 1');",
            "        menu.add(item_1);",
            "      }",
            "      {",
            "        JMenuItem item_2 = new JMenuItem('Item 2');",
            "        menu.add(item_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frameInfo.refresh();
    // prepare models
    JMenuInfo menuInfo = getJavaInfoByName("menu");
    JMenuItemInfo itemInfo_1 = getJavaInfoByName("item_1");
    JMenuItemInfo itemInfo_2 = getJavaInfoByName("item_2");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // we accept only JMenuItem_Info
    {
      assertFalse(policy.validateMove(frameInfo));
    }
    // we can not move "item" of JMenu (practically itself) on its "popup"
    {
      IMenuItemInfo menuItemObject = MenuObjectInfoUtils.getMenuItemInfo(menuInfo);
      Object menuItemObject_model = menuItemObject.getToolkitModel();
      assertSame(menuInfo, menuItemObject_model);
      assertFalse(policy.validateMove(menuItemObject_model));
    }
    // move "item_2" before "item_1"
    assertTrue(policy.validateMove(itemInfo_2));
    policy.commandMove(itemInfo_2, itemInfo_1);
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JMenuItem item_2 = new JMenuItem('Item 2');",
        "        menu.add(item_2);",
        "      }",
        "      {",
        "        JMenuItem item_1 = new JMenuItem('Item 1');",
        "        menu.add(item_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * We can paste {@link JMenuItemInfo}'s.
   */
  public void test_IMenuInfo_PASTE() throws Exception {
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "      {",
            "        JMenuItem existingItem = new JMenuItem('Some item');",
            "        menu.add(existingItem);",
            "      }",
            "    }",
            "  }",
            "}");
    frameInfo.refresh();
    // prepare models
    JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
    JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
    JMenuItemInfo existingItemInfo = menuInfo.getChildrenItems().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // paste copy of "existingItemInfo"
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(existingItemInfo);
      List<JavaInfoMemento> mementos = ImmutableList.of(memento);
      assertTrue(policy.validatePaste(mementos));
      policy.commandPaste(mementos, null);
    }
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JMenuItem existingItem = new JMenuItem('Some item');",
        "        menu.add(existingItem);",
        "      }",
        "      {",
        "        JMenuItem menuItem = new JMenuItem('Some item');",
        "        menu.add(menuItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link CompilationUnit} with external {@link Action}.
   */
  private void createExternalAction() throws Exception {
    setFileContentSrc(
        "test/ExternalAction.java",
        getTestSource(
            "public class ExternalAction extends AbstractAction {",
            "  public ExternalAction() {",
            "    putValue(NAME, 'My name');",
            "    putValue(SHORT_DESCRIPTION, 'My short description');",
            "  }",
            "  public void actionPerformed(ActionEvent e) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  /**
   * We should understand {@link JMenu#add(Action)} and create corresponding {@link JMenuItemInfo}.
   */
  public void test_addAction_parse() throws Exception {
    createExternalAction();
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  private ExternalAction action = new ExternalAction();",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "      {",
            "        JMenuItem menuItem = menu.add(action);",
            "      }",
            "    }",
            "  }",
            "}");
    frameInfo.refresh();
    // prepare models
    JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
    JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
    // check JMenuItem_Info
    {
      JMenuItemInfo itemInfo = menuInfo.getChildrenItems().get(0);
      {
        ImplicitFactoryCreationSupport creationSupport =
            (ImplicitFactoryCreationSupport) itemInfo.getCreationSupport();
        assertEquals("menu.add(action)", m_lastEditor.getSource(creationSupport.getNode()));
      }
      assertInstanceOf(LocalUniqueVariableSupport.class, itemInfo.getVariableSupport());
      assertInstanceOf(InvocationVoidAssociation.class, itemInfo.getAssociation());
    }
  }

  /**
   * We can drop existing {@link ActionInfo} instance.
   */
  public void test_IMenuInfo_CREATE_existingAction() throws Exception {
    createExternalAction();
    String[] lines =
        {
            "public class Test extends JFrame {",
            "  private ExternalAction action = new ExternalAction();",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "    }",
            "  }",
            "}"};
    ContainerInfo frameInfo = parseContainer(lines);
    frameInfo.refresh();
    // prepare models
    ActionInfo action = ActionContainerInfo.getActions(frameInfo).get(0);
    test_IMenuInfo_CREATE_forAction(frameInfo, action, new String[]{
        "public class Test extends JFrame {",
        "  private ExternalAction action = new ExternalAction();",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JMenuItem menuItem = menu.add(action);",
        "      }",
        "    }",
        "  }",
        "}"});
  }

  /**
   * We can drop new {@link ActionInfo} instance.
   */
  public void test_IMenuInfo_CREATE_newAction() throws Exception {
    createExternalAction();
    String[] lines =
        {
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu('Menu');",
            "      menuBar.add(menu);",
            "    }",
            "  }",
            "}"};
    ContainerInfo frameInfo = parseContainer(lines);
    frameInfo.refresh();
    // prepare models
    ActionInfo action = createJavaInfo("test.ExternalAction");
    test_IMenuInfo_CREATE_forAction(frameInfo, action, new String[]{
        "public class Test extends JFrame {",
        "  private final ExternalAction externalAction = new ExternalAction();",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JMenuItem menuItem = menu.add(externalAction);",
        "      }",
        "    }",
        "  }",
        "}"});
  }

  /**
   * We can drop new {@link ActionInfo} instance, using {@link LazyVariableSupport}.
   */
  public void test_IMenuInfo_CREATE_newAction_lazy() throws Exception {
    createExternalAction();
    String[] lines1 =
        {
            "public class Test extends JFrame {",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      JMenu menu = new JMenu();",
            "      menuBar.add(menu);",
            "    }",
            "  }",
            "}"};
    ContainerInfo frameInfo = parseContainer(lines1);
    frameInfo.refresh();
    // prepare models
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    ActionInfo action = createJavaInfo("test.ExternalAction");
    JMenuItemInfo newItem =
        test_IMenuInfo_CREATE_forAction0(frameInfo, action, new String[]{
            "public class Test extends JFrame {",
            "  private ExternalAction externalAction;",
            "  private JMenuItem menuItem;",
            "  private JMenu menu;",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      menu = new JMenu();",
            "      menuBar.add(menu);",
            "      getMenuItem();",
            "    }",
            "  }",
            "  private ExternalAction getExternalAction() {",
            "    if (externalAction == null) {",
            "      externalAction = new ExternalAction();",
            "    }",
            "    return externalAction;",
            "  }",
            "  private JMenuItem getMenuItem() {",
            "    if (menuItem == null) {",
            "      menuItem = menu.add(getExternalAction());",
            "    }",
            "    return menuItem;",
            "  }",
            "}"});
    // delete "newItem"
    assertTrue(newItem.canDelete());
    newItem.delete();
    String[] lines =
        {
            "public class Test extends JFrame {",
            "  private ExternalAction externalAction;",
            "  private JMenu menu;",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    {",
            "      menu = new JMenu();",
            "      menuBar.add(menu);",
            "    }",
            "  }",
            "  private ExternalAction getExternalAction() {",
            "    if (externalAction == null) {",
            "      externalAction = new ExternalAction();",
            "    }",
            "    return externalAction;",
            "  }",
            "}"};
    assertEditor(lines);
  }

  /**
   * We can drop new {@link ActionInfo} instance, using {@link LazyVariableSupport}.
   */
  public void test_IMenuInfo_CREATE_newAction_lazy2() throws Exception {
    createExternalAction();
    String[] lines1 =
        {
            "public class Test extends JFrame {",
            "  private JMenu menu;",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    menuBar.add(getMenu());",
            "  }",
            "  private JMenu getMenu() {",
            "    if (menu == null) {",
            "      menu = new JMenu();",
            "    }",
            "    return menu;",
            "  }",
            "}"};
    ContainerInfo frameInfo = parseContainer(lines1);
    frameInfo.refresh();
    // prepare models
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    ActionInfo action = createJavaInfo("test.ExternalAction");
    JMenuItemInfo newItem =
        test_IMenuInfo_CREATE_forAction0(frameInfo, action, new String[]{
            "public class Test extends JFrame {",
            "  private JMenu menu;",
            "  private ExternalAction externalAction;",
            "  private JMenuItem menuItem;",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    menuBar.add(getMenu());",
            "  }",
            "  private JMenu getMenu() {",
            "    if (menu == null) {",
            "      menu = new JMenu();",
            "      getMenuItem();",
            "    }",
            "    return menu;",
            "  }",
            "  private ExternalAction getExternalAction() {",
            "    if (externalAction == null) {",
            "      externalAction = new ExternalAction();",
            "    }",
            "    return externalAction;",
            "  }",
            "  private JMenuItem getMenuItem() {",
            "    if (menuItem == null) {",
            "      menuItem = getMenu().add(getExternalAction());",
            "    }",
            "    return menuItem;",
            "  }",
            "}"});
    // delete "newItem"
    assertTrue(newItem.canDelete());
    newItem.delete();
    String[] lines =
        {
            "public class Test extends JFrame {",
            "  private JMenu menu;",
            "  private ExternalAction externalAction;",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    menuBar.add(getMenu());",
            "  }",
            "  private JMenu getMenu() {",
            "    if (menu == null) {",
            "      menu = new JMenu();",
            "    }",
            "    return menu;",
            "  }",
            "  private ExternalAction getExternalAction() {",
            "    if (externalAction == null) {",
            "      externalAction = new ExternalAction();",
            "    }",
            "    return externalAction;",
            "  }",
            "}"};
    assertEditor(lines);
  }

  /**
   * Test for parsing "item" created in {@link #test_IMenuInfo_CREATE_newAction_lazy2()}.
   */
  public void test_IMenuInfo_CREATE_newAction_lazy3() throws Exception {
    createExternalAction();
    ContainerInfo frameInfo =
        parseContainer(
            "public class Test extends JFrame {",
            "  private JMenu menu;",
            "  private final ExternalAction externalAction = new ExternalAction();",
            "  private JMenuItem menuItem;",
            "  public Test() {",
            "    JMenuBar menuBar = new JMenuBar();",
            "    setJMenuBar(menuBar);",
            "    menuBar.add(getMenu());",
            "  }",
            "  private JMenu getMenu() {",
            "    if (menu == null) {",
            "      menu = new JMenu('Menu');",
            "      getMenuItem();",
            "    }",
            "    return menu;",
            "  }",
            "  private JMenuItem getMenuItem() {",
            "    if (menuItem == null) {",
            "      menuItem = getMenu().add(externalAction);",
            "    }",
            "    return menuItem;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {/setJMenuBar(menuBar)/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {new: javax.swing.JMenuBar} {local-unique: menuBar} {/new JMenuBar()/ /setJMenuBar(menuBar)/ /menuBar.add(getMenu())/}",
        "    {new: javax.swing.JMenu} {lazy: menu getMenu()} {/new JMenu('Menu')/ /getMenu().add(externalAction)/ /menu/ /menuBar.add(getMenu())/}",
        "      {implicit-factory} {lazy: menuItem getMenuItem()} {/getMenu().add(externalAction)/ /menuItem/ /getMenuItem()/}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {new: test.ExternalAction} {field-initializer: externalAction} {/new ExternalAction()/ /getMenu().add(externalAction)/}");
    frameInfo.refresh();
    // prepare models
    JMenuItemInfo item;
    {
      JavaInfo menuBar = frameInfo.getChildren(JMenuBarInfo.class).get(0);
      JavaInfo menu = menuBar.getChildrenJava().get(0);
      item = (JMenuItemInfo) menu.getChildrenJava().get(0);
    }
    // delete "item"
    assertTrue(item.canDelete());
    item.delete();
    assertEditor(
        "public class Test extends JFrame {",
        "  private JMenu menu;",
        "  private final ExternalAction externalAction = new ExternalAction();",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    menuBar.add(getMenu());",
        "  }",
        "  private JMenu getMenu() {",
        "    if (menu == null) {",
        "      menu = new JMenu('Menu');",
        "    }",
        "    return menu;",
        "  }",
        "}");
  }

  private void test_IMenuInfo_CREATE_forAction(ContainerInfo frameInfo,
      ActionInfo action,
      String[] expectedLines) throws Exception {
    JMenuItemInfo itemInfo = test_IMenuInfo_CREATE_forAction0(frameInfo, action, expectedLines);
    // check new JMenuItem_Info
    {
      assertInstanceOf(ImplicitFactoryCreationSupport.class, itemInfo.getCreationSupport());
      assertInstanceOf(LocalUniqueVariableSupport.class, itemInfo.getVariableSupport());
      assertInstanceOf(InvocationVoidAssociation.class, itemInfo.getAssociation());
      // check IMenuItemInfo
      IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(itemInfo);
      assertTrue(itemObject.canMove());
      assertFalse(itemObject.canReparent());
    }
  }

  private JMenuItemInfo test_IMenuInfo_CREATE_forAction0(ContainerInfo frameInfo,
      ActionInfo action,
      String[] expectedLines) throws Exception {
    JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
    JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // initially no "item" objects
    assertEquals(0, menuObject.getItems().size());
    // add ActionInfo
    assertTrue(policy.validateCreate(action));
    policy.commandCreate(action, null);
    assertEditor(expectedLines);
    // return new JMenuItem
    return menuInfo.getChildrenItems().get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Separator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing adding separator using {@link JPopupMenu#addSeparator()}.
   */
  public void test_separatorParse_addSeparator() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      menu.addSeparator();",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {/setJMenuBar(menuBar)/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {new: javax.swing.JMenuBar} {local-unique: menuBar} {/new JMenuBar()/ /setJMenuBar(menuBar)/ /menuBar.add(menu)/}",
        "    {new: javax.swing.JMenu} {local-unique: menu} {/new JMenu('Menu')/ /menuBar.add(menu)/ /menu.addSeparator()/}",
        "      {void} {void} {/menu.addSeparator()/}");
    refresh();
    // prepare models
    JMenuInfo menu = getJavaInfoByName("menu");
    JPopupMenuSeparatorInfo separator = menu.getChildren(JPopupMenuSeparatorInfo.class).get(0);
    // separator has Object
    assertThat(separator.getObject()).isInstanceOf(JPopupMenu.Separator.class);
    // check VoidInvocationVariableSupport
    {
      VariableSupport variable = separator.getVariableSupport();
      assertInstanceOf(VoidInvocationVariableSupport.class, variable);
    }
    // check JMenuSeparatorCreationSupport
    {
      JMenuSeparatorCreationSupport creation =
          (JMenuSeparatorCreationSupport) separator.getCreationSupport();
      assertEquals("void", creation.toString());
      assertSame(
          ((InvocationVoidAssociation) separator.getAssociation()).getInvocation(),
          creation.getNode());
      // validation
      assertTrue(creation.canReorder());
      assertFalse(creation.canReparent());
      assertTrue(creation.canDelete());
    }
    // check association
    assertInstanceOf(InvocationVoidAssociation.class, separator.getAssociation());
  }

  /**
   * Test for parsing adding separator using {@link JPopupMenu#addSeparator()}.
   */
  public void test_separatorParse_newSeparator() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      {",
        "        JPopupMenu.Separator separator = new JPopupMenu.Separator();",
        "        menu.add(separator);",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {/setJMenuBar(menuBar)/}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {new: javax.swing.JMenuBar} {local-unique: menuBar} {/new JMenuBar()/ /setJMenuBar(menuBar)/ /menuBar.add(menu)/}",
        "    {new: javax.swing.JMenu} {local-unique: menu} {/new JMenu('Menu')/ /menuBar.add(menu)/ /menu.add(separator)/}",
        "      {new: javax.swing.JPopupMenu$Separator} {local-unique: separator} {/new JPopupMenu.Separator()/ /menu.add(separator)/}");
    refresh();
  }

  /**
   * We can drop any separator using {@link JMenu#addSeparator()} .
   */
  public void test_separator_CREATE() throws Exception {
    parseContainer(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "    }",
        "  }",
        "}");
    refresh();
    // prepare models
    JMenuInfo menuInfo = getJavaInfoByName("menu");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy policy = menuObject.getPolicy();
    // create separator
    JPopupMenuSeparatorInfo separator;
    {
      JMenuSeparatorCreationSupport creationSupport = new JMenuSeparatorCreationSupport(menuInfo);
      separator =
          (JPopupMenuSeparatorInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              JPopupMenu.Separator.class,
              creationSupport);
    }
    // do add
    assertTrue(policy.validateCreate(separator));
    policy.commandCreate(separator, null);
    assertEditor(
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Menu');",
        "      menuBar.add(menu);",
        "      menu.addSeparator();",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visual inheritance
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests that {@link JMenuBar}, {@link JMenu} and {@link JMenuItem} can be exposed and are
   * correctly bound into hierarchy.
   */
  public void test_visualInheritance() throws Exception {
    createModelType(
        "test",
        "MyFrame.java",
        getTestSource(
            "public class MyFrame extends JFrame {",
            "  private final JMenuBar m_menuBar = new JMenuBar();",
            "  private final JMenu m_menu = new JMenu();",
            "  private final JMenuItem m_menuItem = new JMenuItem();",
            "  public MyFrame() {",
            "    setJMenuBar(m_menuBar);",
            "    m_menuBar.add(m_menu);",
            "    m_menu.add(m_menuItem);",
            "  }",
            "  public JMenuBar getMyMenuBar() {",
            "    return m_menuBar;",
            "  }",
            "  public JMenu getMyMenu() {",
            "    return m_menu;",
            "  }",
            "  public JMenuItem getMyMenuItem() {",
            "    return m_menuItem;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyFrame {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JMenuBar test.MyFrame.getMyMenuBar()} {property} {}",
        "    {method: public javax.swing.JMenu test.MyFrame.getMyMenu()} {property} {}",
        "      {method: public javax.swing.JMenuItem test.MyFrame.getMyMenuItem()} {property} {}");
  }
}
