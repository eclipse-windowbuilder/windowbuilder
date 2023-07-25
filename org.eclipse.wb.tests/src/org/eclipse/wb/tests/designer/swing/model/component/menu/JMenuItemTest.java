/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.component.menu;

import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import java.util.List;

/**
 * Test for {@link JMenuItemInfo}.
 *
 * @author scheglov_ke
 */
public class JMenuItemTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link JMenuItemInfo} and {@link IMenuItemInfo}.
	 */
	public void test_0() throws Exception {
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
						"        JMenuItem item = new JMenuItem('Item');",
						"        menu.add(item);",
						"      }",
						"    }",
						"  }",
						"}");
		frameInfo.refresh();
		// prepare models
		JMenuBarInfo menuBarInfo = frameInfo.getChildren(JMenuBarInfo.class).get(0);
		JMenuInfo menuInfo = menuBarInfo.getChildrenMenus().get(0);
		JMenuItemInfo itemInfo = menuInfo.getChildrenItems().get(0);
		// no adapter for random class
		assertNull(itemInfo.getAdapter(List.class));
		// check IMenuItemInfo
		{
			IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(itemInfo);
			assertSame(itemInfo, itemObject.getModel());
			// presentation
			assertNull(itemObject.getImageDescriptor());
			assertTrue(itemObject.getBounds().width > 40);
			assertTrue(itemObject.getBounds().height > 15);
			// in Swing JMenuItem is just item, without sub-menu
			assertNull(itemObject.getMenu());
			// ...so, no policy
			assertSame(IMenuPolicy.NOOP, itemObject.getPolicy());
		}
	}
}
