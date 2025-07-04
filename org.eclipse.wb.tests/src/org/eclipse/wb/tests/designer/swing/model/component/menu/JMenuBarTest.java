/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.component.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.RootAssociation;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Test for {@link JMenuBarInfo}.
 *
 * @author scheglov_ke
 */
public class JMenuBarTest extends SwingModelTest {
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
	 * Test that we can parse {@link JMenuBar} with {@link JMenu} and two {@link JMenuItem}'s.
	 */
	@Test
	public void test_parse() throws Exception {
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
		// check components tree
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
		assertEquals(2, menuInfo.getChildrenItems().size());
	}

	/**
	 * When {@link JMenuBar} has no items, its height is zero, so we should add some text to make it
	 * taller.
	 */
	@Test
	public void test_renderEmpty() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JMenuBar menuBar = new JMenuBar();",
						"    setJMenuBar(menuBar);",
						"  }",
						"}");
		frame.refresh();
		// check JMenuBar_Info
		JMenuBarInfo menuBar = frame.getChildren(JMenuBarInfo.class).get(0);
		assertTrue(menuBar.getBounds().height > 10);
	}

	/**
	 * Test for parsing {@link JMenuBar} as "this" component.
	 */
	@Test
	public void test_standaloneJMenuBar() throws Exception {
		JMenuBarInfo barInfo =
				(JMenuBarInfo) parseContainer(getDoubleQuotes(new String[]{
						"public class Test extends JMenuBar {",
						"  public Test() {",
						"    JMenu menu = new JMenu('Menu');",
						"    add(menu);",
						"  }",
				"}"}));
		barInfo.refresh();
		assertInstanceOf(RootAssociation.class, barInfo.getAssociation());
		// IMenuInfo
		IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(barInfo);
		{
			ImageDescriptor image = menuObject.getImageDescriptor();
			assertNotNull(image);
			ImageData imageData = image.getImageData(100);
			assertEquals(imageData.x, 0);
			assertEquals(imageData.y, 0);
			Assertions.assertThat(imageData.width).isGreaterThan(40);
			Assertions.assertThat(imageData.height).isGreaterThan(20);
		}
		{
			Rectangle bounds = menuObject.getBounds();
			assertEquals(bounds.x, 0);
			assertEquals(bounds.y, 0);
			Assertions.assertThat(bounds.width).isGreaterThan(40);
			Assertions.assertThat(bounds.height).isGreaterThan(20);
		}
	}

	/**
	 * Test for parsing {@link JMenuBar} as "this" component.
	 * <p>
	 * There was problem that when {@link JMenuBar} has two empty {@link JMenu}, both of them don't
	 * have <code>"(Add items here)"</code> message.
	 */
	@Test
	public void test_standaloneJMenuBar_2() throws Exception {
		JMenuBarInfo barInfo =
				(JMenuBarInfo) parseContainer(getDoubleQuotes(new String[]{
						"public class Test extends JMenuBar {",
						"  public Test() {",
						"    {",
						"      JMenu menu = new JMenu('New menu');",
						"      add(menu);",
						"    }",
						"    {",
						"      JMenu menu = new JMenu('New menu');",
						"      add(menu);",
						"    }",
						"  }",
				"}"}));
		barInfo.refresh();
		IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(barInfo);
		List<IMenuItemInfo> items = menuObject.getItems();
		Assertions.assertThat(items).hasSize(2);
		// JMenu "A"
		{
			IMenuInfo menu = items.get(0).getMenu();
			Rectangle bounds = menu.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(100);
			Assertions.assertThat(bounds.height).isGreaterThan(20);
		}
		// JMenu "B"
		{
			IMenuInfo menu = items.get(1).getMenu();
			Rectangle bounds = menu.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(100);
			Assertions.assertThat(bounds.height).isGreaterThan(20);
		}
	}

	/**
	 * Test for {@link JMenuBarInfo} and its {@link IMenuInfo}.
	 */
	@Test
	public void test_IMenuInfo_access() throws Exception {
		ContainerInfo frameInfo =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JMenuBar menuBar = new JMenuBar();",
						"    setJMenuBar(menuBar);",
						"    {",
						"      JMenu menu_1 = new JMenu('Menu 1');",
						"      menuBar.add(menu_1);",
						"    }",
						"    {",
						"      JMenu menu_2 = new JMenu('Menu 2');",
						"      menuBar.add(menu_2);",
						"    }",
						"  }",
						"}");
		frameInfo.refresh();
		ContainerInfo contentPaneInfo = (ContainerInfo) frameInfo.getChildrenComponents().get(0);
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		// prepare children JMenu's
		JMenuInfo menuInfo_1;
		JMenuInfo menuInfo_2;
		{
			List<JMenuInfo> childrenMenus = menuBarInfo.getChildrenMenus();
			assertEquals(2, childrenMenus.size());
			menuInfo_1 = childrenMenus.get(0);
			menuInfo_2 = childrenMenus.get(1);
		}
		// ask some not existing adapter
		assertNull(menuBarInfo.getAdapter(List.class));
		// check IMenuInfo for JMenuBar_Info
		{
			IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuBarInfo);
			assertNotNull(menuObject);
			// model
			assertSame(menuBarInfo, menuObject.getModel());
			// presentation
			{
				// no image for "bar"
				assertNull(menuObject.getImageDescriptor());
				// "bar" has same width as "contentPane"
				assertEquals(contentPaneInfo.getBounds().width, menuObject.getBounds().width);
				// items on "bar" are placed horizontally
				assertTrue(menuObject.isHorizontal());
			}
			// "items" on "bar" as JMenu's, we have exactly two
			List<IMenuItemInfo> items = menuObject.getItems();
			assertEquals(2, items.size());
			assertSame(menuInfo_1, items.get(0).getModel());
			assertSame(menuInfo_2, items.get(1).getModel());
		}
	}

	/**
	 * We can drop new {@link JMenuInfo}.
	 */
	@Test
	public void test_IMenuInfo_CREATE() throws Exception {
		ContainerInfo frameInfo =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JMenuBar menuBar = new JMenuBar();",
						"    setJMenuBar(menuBar);",
						"  }",
						"}");
		frameInfo.refresh();
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuBarInfo);
		IMenuPolicy policy = menuObject.getPolicy();
		// initially no "menu" objects
		assertEquals(0, menuObject.getItems().size());
		// can not drop not JMenu_Info
		{
			JavaInfo newObject = createJavaInfo("java.lang.Object");
			assertFalse(policy.validateCreate(newObject));
		}
		// add new JMenu_Info
		JMenuInfo newMenuInfo = (JMenuInfo) createComponent(JMenu.class);
		assertTrue(policy.validateCreate(newMenuInfo));
		policy.commandCreate(newMenuInfo, null);
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    JMenuBar menuBar = new JMenuBar();",
				"    setJMenuBar(menuBar);",
				"    {",
				"      JMenu menu = new JMenu('New menu');",
				"      menuBar.add(menu);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * We can drop any new {@link ComponentInfo}, such as {@link JButton}.
	 */
	@Test
	public void test_IMenuInfo_CREATE_JButton() throws Exception {
		ContainerInfo frameInfo =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JMenuBar menuBar = new JMenuBar();",
						"    setJMenuBar(menuBar);",
						"  }",
						"}");
		frameInfo.refresh();
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuBarInfo);
		IMenuPolicy policy = menuObject.getPolicy();
		// initially no "menu" objects
		assertEquals(0, menuObject.getItems().size());
		// add new JButton
		ComponentInfo newButtonInfo = createJButton();
		assertTrue(policy.validateCreate(newButtonInfo));
		policy.commandCreate(newButtonInfo, null);
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    JMenuBar menuBar = new JMenuBar();",
				"    setJMenuBar(menuBar);",
				"    {",
				"      JButton button = new JButton();",
				"      menuBar.add(button);",
				"    }",
				"  }",
				"}");
		// now we have IMenuItemInfo for JButton
		{
			List<IMenuItemInfo> items = menuObject.getItems();
			assertEquals(1, items.size());
			IMenuItemInfo item = items.get(0);
			// model
			assertSame(newButtonInfo, item.getModel());
			// presentation
			assertSame(newButtonInfo.getImage(), item.getImageDescriptor());
			assertEquals(newButtonInfo.getBounds(), item.getBounds());
			// no sub-menu
			assertNull(item.getMenu());
			assertSame(IMenuPolicy.NOOP, item.getPolicy());
			// we always get same IMenuItemInfo for same JButton
			assertTrue(menuObject.getItems().contains(item));
		}
	}

	/**
	 * We can move {@link JMenuInfo}'s.
	 */
	@Test
	public void test_IMenuInfo_MOVE() throws Exception {
		ContainerInfo frameInfo =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JMenuBar menuBar = new JMenuBar();",
						"    setJMenuBar(menuBar);",
						"    {",
						"      JMenu menu_1 = new JMenu('Menu 1');",
						"      menuBar.add(menu_1);",
						"    }",
						"    {",
						"      JMenu menu_2 = new JMenu('Menu 2');",
						"      menuBar.add(menu_2);",
						"    }",
						"  }",
						"}");
		frameInfo.refresh();
		// prepare models
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		JMenuInfo menuInfo_1 = menuBarInfo.getChildrenMenus().get(0);
		JMenuInfo menuInfo_2 = menuBarInfo.getChildrenMenus().get(1);
		IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuBarInfo);
		IMenuPolicy policy = menuObject.getPolicy();
		// move "menu_2" before "menu_1"
		assertTrue(policy.validateMove(menuInfo_2));
		policy.commandMove(menuInfo_2, menuInfo_1);
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    JMenuBar menuBar = new JMenuBar();",
				"    setJMenuBar(menuBar);",
				"    {",
				"      JMenu menu_2 = new JMenu('Menu 2');",
				"      menuBar.add(menu_2);",
				"    }",
				"    {",
				"      JMenu menu_1 = new JMenu('Menu 1');",
				"      menuBar.add(menu_1);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * We can paste {@link JMenuInfo}'s.
	 */
	@Test
	public void test_IMenuInfo_PASTE() throws Exception {
		ContainerInfo frameInfo =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    JMenuBar menuBar = new JMenuBar();",
						"    setJMenuBar(menuBar);",
						"    {",
						"      JMenu existingMenu = new JMenu('Some menu');",
						"      menuBar.add(existingMenu);",
						"      {",
						"        JMenuItem item_1 = new JMenuItem('Item 1');",
						"        existingMenu.add(item_1);",
						"        item_1.setEnabled(false);",
						"      }",
						"      {",
						"        JMenuItem item_2 = new JMenuItem('Item 2');",
						"        existingMenu.add(item_2);",
						"      }",
						"    }",
						"  }",
						"}");
		frameInfo.refresh();
		// prepare models
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		JMenuInfo existingMenuInfo = menuBarInfo.getChildrenMenus().get(0);
		IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuBarInfo);
		IMenuPolicy policy = menuObject.getPolicy();
		// paste copy of "existingMenu"
		{
			JavaInfoMemento memento = JavaInfoMemento.createMemento(existingMenuInfo);
			List<JavaInfoMemento> mementos = List.of(memento);
			assertTrue(policy.validatePaste(mementos));
			policy.commandPaste(mementos, null);
		}
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    JMenuBar menuBar = new JMenuBar();",
				"    setJMenuBar(menuBar);",
				"    {",
				"      JMenu existingMenu = new JMenu('Some menu');",
				"      menuBar.add(existingMenu);",
				"      {",
				"        JMenuItem item_1 = new JMenuItem('Item 1');",
				"        existingMenu.add(item_1);",
				"        item_1.setEnabled(false);",
				"      }",
				"      {",
				"        JMenuItem item_2 = new JMenuItem('Item 2');",
				"        existingMenu.add(item_2);",
				"      }",
				"    }",
				"    {",
				"      JMenu existingMenu = new JMenu('Some menu');",
				"      menuBar.add(existingMenu);",
				"      {",
				"        JMenuItem item_1 = new JMenuItem('Item 1');",
				"        item_1.setEnabled(false);",
				"        existingMenu.add(item_1);",
				"      }",
				"      {",
				"        JMenuItem item_2 = new JMenuItem('Item 2');",
				"        existingMenu.add(item_2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adding {@link JMenuBarInfo} on {@link JFrame}.
	 */
	@Test
	public void test_CREATE_noChildren() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		frame.refresh();
		// add new JMenuBar_Info
		JMenuBarInfo menuBar = (JMenuBarInfo) createComponent(JMenuBar.class);
		menuBar.command_CREATE(frame);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    {",
				"      JMenuBar menuBar = new JMenuBar();",
				"      setJMenuBar(menuBar);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Adding {@link JMenuBarInfo} on {@link JFrame}.
	 */
	@Test
	public void test_CREATE_withStatement() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    setEnabled(false);",
						"  }",
						"}");
		frame.refresh();
		// add new JMenuBar_Info
		JMenuBarInfo menuBar = (JMenuBarInfo) createComponent(JMenuBar.class);
		menuBar.command_CREATE(frame);
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setEnabled(false);",
				"    {",
				"      JMenuBar menuBar = new JMenuBar();",
				"      setJMenuBar(menuBar);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Adding {@link JMenuBarInfo} on {@link JFrame}.
	 */
	@Test
	public void test_CREATE_withChildren() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public class Test extends JFrame {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      getContentPane().add(button);",
						"    }",
						"  }",
						"}");
		frame.refresh();
		// add new JMenuBar_Info
		JMenuBarInfo menuBar = (JMenuBarInfo) createComponent(JMenuBar.class);
		menuBar.command_CREATE(frame);
		assertEditor(
				"public class Test extends JFrame {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      getContentPane().add(button);",
				"    }",
				"    {",
				"      JMenuBar menuBar = new JMenuBar();",
				"      setJMenuBar(menuBar);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * We can not add new {@link JMenuItem} before exposed {@link JMenuItem}.
	 */
	@Test
	public void test_CREATE_exposedItem_badReference() throws Exception {
		String[] lines =
			{
					"public class MyFrame extends JFrame {",
					"  private JMenuBar menuBar;",
					"  private JMenu menu;",
					"  private JMenuItem item;",
					"  public MyFrame() {",
					"    setJMenuBar(getMainJMenuBar());",
					"  }",
					"  public JMenuBar getMainJMenuBar() {",
					"    if (menuBar == null) {",
					"      menuBar = new JMenuBar();",
					"      menuBar.add(getMenu());",
					"    }",
					"    return menuBar;",
					"  }",
					"  public JMenu getMenu() {",
					"    if (menu == null) {",
					"      menu = new JMenu('New SubMenu');",
					"      menu.add(getItem());",
					"    }",
					"    return menu;",
					"  }",
					"  public JMenuItem getItem() {",
					"    if (item == null) {",
					"      item = new JMenuItem('New Item');",
					"    }",
					"    return item;",
					"  }",
			"}"};
		setFileContentSrc("test/MyFrame.java", getTestSource(lines));
		waitForAutoBuild();
		// parse
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyFrame {",
						"  public Test() {",
						"  }",
						"}");
		frame.refresh();
		assertHierarchy(
				"{this: test.MyFrame} {this} {}",
				"  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
				"    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
				"  {method: public javax.swing.JMenuBar test.MyFrame.getMainJMenuBar()} {property} {}",
				"    {method: public javax.swing.JMenu test.MyFrame.getMenu()} {property} {}",
				"      {method: public javax.swing.JMenuItem test.MyFrame.getItem()} {property} {}");
		// prepare models
		JMenuBarInfo menuBarInfo = frame.getChildren(JMenuBarInfo.class).get(0);
		JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
		JMenuItemInfo itemInfo = menuInfo.getChildren(JMenuItemInfo.class).get(0);
		// "item" is implicit and can not be reference
		assertTrue(MenuObjectInfoUtils.isImplicitObject(itemInfo));
	}
}
