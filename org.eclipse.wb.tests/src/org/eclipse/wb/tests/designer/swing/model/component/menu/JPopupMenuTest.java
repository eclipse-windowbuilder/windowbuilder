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

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuAssociation;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuSeparatorCreationSupport;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuSeparatorInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * Test for {@link JPopupMenuInfo}.
 * 
 * @author scheglov_ke
 */
public class JPopupMenuTest extends SwingModelTest {
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
   * Test that we can parse {@link JPopupMenu}.
   */
  public void test_parse() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPopupMenu popup = new JPopupMenu();",
            "    addPopup(this, popup);",
            "    {",
            "      JMenuItem item_1 = new JMenuItem('Item 1');",
            "      popup.add(item_1);",
            "    }",
            "    {",
            "      JMenuItem item_2 = new JMenuItem('Item 2');",
            "      popup.add(item_2);",
            "    }",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    // prepare JPopupMenu_Info
    JPopupMenuInfo popupInfo = panelInfo.getChildren(JPopupMenuInfo.class).get(0);
    {
      JPopupMenuAssociation association = (JPopupMenuAssociation) popupInfo.getAssociation();
      assertEquals("addPopup(this, popup)", association.getSource());
    }
    // prepare items
    assertEquals(2, popupInfo.getChildrenItems().size());
    JMenuItemInfo item_0 = popupInfo.getChildrenItems().get(0);
    JMenuItemInfo item_1 = popupInfo.getChildrenItems().get(1);
    // no adapter for random Class
    assertNull(popupInfo.getAdapter(List.class));
    // check IMenuPopupInfo
    {
      IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(popupInfo);
      assertSame(popupInfo, popupObject.getModel());
      // presentation
      assertSame(popupInfo.getDescription().getIcon(), popupObject.getImage());
      assertEquals(new Rectangle(0, 0, 16, 16), popupObject.getBounds());
      // no policy
      assertSame(IMenuPolicy.NOOP, popupObject.getPolicy());
    }
    // check IMenuInfo
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuPopupInfo(popupInfo).getMenu();
      assertSame(menuObject, menuObject.getModel());
      // presentation
      assertNotNull(menuObject.getImage());
      assertThat(menuObject.getBounds().width).isGreaterThan(50);
      assertThat(menuObject.getBounds().height).isGreaterThanOrEqualTo(40);
      // items
      assertFalse(menuObject.isHorizontal());
      {
        List<IMenuItemInfo> items = menuObject.getItems();
        assertEquals(2, items.size());
        assertSame(item_0, items.get(0).getModel());
        assertSame(item_1, items.get(1).getModel());
      }
      // has policy
      assertNotSame(IMenuPolicy.NOOP, menuObject.getPolicy());
    }
  }

  /**
   * Even when {@link JPopupMenu} has no items, it still has non-zero size.
   */
  public void test_noItems() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPopupMenu popup = new JPopupMenu();",
            "    addPopup(this, popup);",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    // do checks
    JPopupMenuInfo popupInfo = panelInfo.getChildren(JPopupMenuInfo.class).get(0);
    // no items
    assertEquals(0, popupInfo.getChildrenItems().size());
    // ...but has size
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuPopupInfo(popupInfo).getMenu();
    assertTrue(menuObject.getBounds().width > 50);
    assertTrue(menuObject.getBounds().height > 10);
  }

  /**
   * Test for {@link IMenuItemInfo} and {@link IMenuInfo} from {@link JPopupMenuInfo}.
   * <p>
   * {@link JSeparator} should be {@link IMenuItemInfo}.
   */
  public void test_IMenuInfo_withSeparator() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPopupMenu popup = new JPopupMenu();",
            "    addPopup(this, popup);",
            "    {",
            "      JMenuItem item_1 = new JMenuItem('Item 1');",
            "      popup.add(item_1);",
            "    }",
            "    {",
            "      JSeparator separator = new JSeparator();",
            "      popup.add(separator);",
            "    }",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    // prepare models
    JPopupMenuInfo popupInfo = panelInfo.getChildren(JPopupMenuInfo.class).get(0);
    JMenuItemInfo itemInfo_1 = (JMenuItemInfo) popupInfo.getChildrenComponents().get(0);
    ComponentInfo separatorInfo = popupInfo.getChildrenComponents().get(1);
    // check IMenuInfo
    {
      IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(popupInfo);
      IMenuInfo menuObject = popupObject.getMenu();
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
        assertThat(bounds.height).isGreaterThan(0).isLessThanOrEqualTo(
            Expectations.get(5, new IntValue[]{new IntValue("mitin-aa", 12)}));
      }
    }
  }

  /**
   * Test that we can add new {@link JPopupMenu}.
   */
  public void test_CREATE() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panelInfo.refresh();
    // add new JPopupMenu
    JPopupMenuInfo popupInfo = (JPopupMenuInfo) createComponent(JPopupMenu.class);
    popupInfo.command_CREATE(panelInfo);
    {
      JPopupMenuAssociation association = (JPopupMenuAssociation) popupInfo.getAssociation();
      assertEquals("addPopup(this, popupMenu)", association.getSource());
    }
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPopupMenu popupMenu = new JPopupMenu();",
        "      addPopup(this, popupMenu);",
        "    }",
        "  }",
        "  private static void addPopup(Component component, final JPopupMenu popup) {",
        "    component.addMouseListener(new MouseAdapter() {",
        "      public void mousePressed(MouseEvent e) {",
        "        if (e.isPopupTrigger()) {",
        "          showMenu(e);",
        "        }",
        "      }",
        "      public void mouseReleased(MouseEvent e) {",
        "        if (e.isPopupTrigger()) {",
        "          showMenu(e);",
        "        }",
        "      }",
        "      private void showMenu(MouseEvent e) {",
        "        popup.show(e.getComponent(), e.getX(), e.getY());",
        "      }",
        "    });",
        "  }",
        "}");
  }

  /**
   * Test that we can move {@link JPopupMenu}.
   */
  public void test_MOVE() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "      JPopupMenu popup = new JPopupMenu();",
            "      addPopup(button_1, popup);",
            "      popup.add(new JMenuItem());",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    // prepare components
    ComponentInfo buttonInfo_1 = panelInfo.getChildrenComponents().get(0);
    ComponentInfo buttonInfo_2 = panelInfo.getChildrenComponents().get(1);
    JPopupMenuInfo popupInfo = buttonInfo_1.getChildren(JPopupMenuInfo.class).get(0);
    // move JPopupMenu_Info
    popupInfo.command_MOVE(buttonInfo_2);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2);",
        "      JPopupMenu popup = new JPopupMenu();",
        "      addPopup(button_2, popup);",
        "      popup.add(new JMenuItem());",
        "    }",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
  }

  /**
   * Test that we can move {@link JPopupMenu} to the container.
   */
  public void test_MOVE_toTheContainer() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPopupMenu popupMenu = new JPopupMenu();",
            "    addPopup(this, popupMenu);",
            "    JPanel innerPanel = new JPanel();",
            "    add(innerPanel);",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    // prepare components
    JPopupMenuInfo popupInfo = getJavaInfoByName("popupMenu");
    ComponentInfo innerPanel = getJavaInfoByName("innerPanel");
    // move JPopupMenu_Info
    popupInfo.command_MOVE(innerPanel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPanel innerPanel = new JPanel();",
        "    add(innerPanel);",
        "    JPopupMenu popupMenu = new JPopupMenu();",
        "    addPopup(innerPanel, popupMenu);",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
  }

  /**
   * Test that we can paste {@link JPopupMenu}.
   */
  public void test_PASTE() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "      {",
            "        JPopupMenu popup = new JPopupMenu();",
            "        addPopup(button_1, popup);",
            "        popup.add(new JMenuItem('Some item'));",
            "      }",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    // prepare components
    ComponentInfo buttonInfo_1 = panelInfo.getChildrenComponents().get(0);
    ComponentInfo buttonInfo_2 = panelInfo.getChildrenComponents().get(1);
    JPopupMenuInfo popupInfo = buttonInfo_1.getChildren(JPopupMenuInfo.class).get(0);
    // do copy/paste
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(popupInfo);
      JPopupMenuInfo popupCopyInfo = (JPopupMenuInfo) memento.create(panelInfo);
      popupCopyInfo.command_CREATE(buttonInfo_2);
      memento.apply();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "      {",
        "        JPopupMenu popup = new JPopupMenu();",
        "        addPopup(button_1, popup);",
        "        popup.add(new JMenuItem('Some item'));",
        "      }",
        "    }",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2);",
        "      {",
        "        JPopupMenu popupMenu = new JPopupMenu();",
        "        addPopup(button_2, popupMenu);",
        "        {",
        "          JMenuItem menuItem = new JMenuItem('Some item');",
        "          popupMenu.add(menuItem);",
        "        }",
        "      }",
        "    }",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
  }

  /**
   * Tests for popup menu to have a special popup menu tracking listener.
   */
  public void test_hasTrackingListener() throws Exception {
    ContainerInfo panelInfo =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JPopupMenu popup = new JPopupMenu();",
            "    addPopup(this, popup);",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    panelInfo.refresh();
    MouseListener[] mouseListeners = panelInfo.getComponent().getMouseListeners();
    assertThat(mouseListeners).isNotEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Separator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing adding separator using {@link JPopupMenu#addSeparator()}.
   */
  public void test_separator_addSeparator() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPopupMenu popup = new JPopupMenu();",
        "    addPopup(this, popup);",
        "    {",
        "      popup.addSeparator();",
        "    }",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/addPopup(this, popup)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPopupMenu} {local-unique: popup} {/new JPopupMenu()/ /addPopup(this, popup)/ /popup.addSeparator()/}",
        "    {void} {void} {/popup.addSeparator()/}");
    JPopupMenuInfo popup = getJavaInfoByName("popup");
    JPopupMenuSeparatorInfo separator = popup.getChildren(JPopupMenuSeparatorInfo.class).get(0);
    // check VoidInvocationVariableSupport
    {
      VariableSupport variable = separator.getVariableSupport();
      assertInstanceOf(VoidInvocationVariableSupport.class, variable);
    }
    // check JPopupMenu_Separator_CreationSupport
    {
      JPopupMenuSeparatorCreationSupport creation =
          (JPopupMenuSeparatorCreationSupport) separator.getCreationSupport();
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
   * Test for parsing adding separator as explicit {@link JPopupMenu.Separator} creation.
   */
  public void test_separator_newSeparator() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPopupMenu popup = new JPopupMenu();",
        "    addPopup(this, popup);",
        "    {",
        "      JPopupMenu.Separator separator = new JPopupMenu.Separator();",
        "      popup.add(separator);",
        "    }",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/addPopup(this, popup)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPopupMenu} {local-unique: popup} {/new JPopupMenu()/ /addPopup(this, popup)/ /popup.add(separator)/}",
        "    {new: javax.swing.JPopupMenu$Separator} {local-unique: separator} {/new JPopupMenu.Separator()/ /popup.add(separator)/}");
  }

  /**
   * Test for adding {@link JPopupMenuSeparatorInfo} using {@link JPopupMenu#addSeparator()}.
   */
  public void test_separator_create_addSeparator() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPopupMenu popup = new JPopupMenu();",
        "    addPopup(this, popup);",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
    JPopupMenuInfo popup = getJavaInfoByName("popup");
    // create separator
    JPopupMenuSeparatorCreationSupport creationSupport =
        new JPopupMenuSeparatorCreationSupport(popup);
    JPopupMenuSeparatorInfo separator =
        (JPopupMenuSeparatorInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            JPopupMenu.Separator.class,
            creationSupport);
    // add separator
    MenuObjectInfoUtils.getMenuPopupInfo(popup).getMenu().getPolicy().commandCreate(separator, null);
    // check creation
    assertNotNull(creationSupport.getInvocation());
    // check variable
    assertInstanceOf(VoidInvocationVariableSupport.class, separator.getVariableSupport());
    // check association
    {
      Association association = separator.getAssociation();
      assertInstanceOf(InvocationVoidAssociation.class, association);
      assertEquals("popup.addSeparator()", association.getSource());
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPopupMenu popup = new JPopupMenu();",
        "    addPopup(this, popup);",
        "    popup.addSeparator();",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
  }
}
