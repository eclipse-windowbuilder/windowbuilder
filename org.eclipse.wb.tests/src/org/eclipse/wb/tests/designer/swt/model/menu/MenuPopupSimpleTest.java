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
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.SelectEditPartTracker;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.core.gef.policy.menu.MenuSelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuItemInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import org.eclipse.draw2d.geometry.Rectangle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests "popup" with single "item".
 *
 * @author scheglov_ke
 */
public class MenuPopupSimpleTest extends RcpGefTest {
	private MenuFeedbackTester menuTester;
	private CompositeInfo shellInfo;
	private MenuInfo popupInfo;
	private MenuItemInfo itemInfo;
	private GraphicalEditPart shellPart;
	private GraphicalEditPart popupPart;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// open editor
		shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu popup = new Menu(this);",
						"    setMenu(popup);",
						"    {",
						"      MenuItem menuItem = new MenuItem(popup, SWT.NONE);",
						"      menuItem.setText('Item 1');",
						"    }",
						"  }",
						"}");
		popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
		itemInfo = popupInfo.getChildrenItems().get(0);
		// prepare EditPart's
		shellPart = canvas.getEditPart(shellInfo);
		popupPart = canvas.getEditPart(popupInfo);
		assertNotNull(shellPart);
		assertNotNull(popupPart);
	}

	@Override
	protected void fetchContentFields() {
		super.fetchContentFields();
		menuTester = new MenuFeedbackTester(canvas);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// clean models
		shellInfo = null;
		popupInfo = null;
		itemInfo = null;
		// clean EditPart's
		shellPart = null;
		popupPart = null;
		// tester
		menuTester = null;
		// continue
		waitEventLoop(0);
		super.tearDown();
	}

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
	 * Popup looks as 16x16 icon and shows drop-down when clicked.
	 */
	@Test
	public void test_clickOnIconToOpen() throws Exception {
		// figure for "popup" is icon 16x16
		{
			Rectangle bounds = popupPart.getFigure().getBounds();
			assertEquals(16, bounds.width);
			assertEquals(16, bounds.height);
		}
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		// click on "popup": drop-down appears
		canvas.click(popupPart);
		{
			List<EditPart> children = popupPart.getChildren();
			assertEquals(1, children.size());
			EditPart dropPart = children.get(0);
			// drop-down has simple "drag tracker"
			assertSame(SelectEditPartTracker.class, dropPart.getDragTracker(null).getClass());
		}
		// click on "shell": drop-down disappears
		canvas.click(shellPart, 100, 100);
		assertTrue(popupPart.getChildren().isEmpty());
	}

	/**
	 * Test that selection of "popup" on design canvas selects it in components tree.
	 */
	@Test
	public void test_clickOnCanvas_selectInTree() throws Exception {
		// initially no selection
		assertSelectionModels();
		assertTreeSelectionModels();
		// click on "popup"
		canvas.click(popupPart);
		// selection expected
		assertSelectionModels(popupInfo);
		assertTreeSelectionModels(popupInfo);
	}

	/**
	 * Drop-down is located directly below "popup" (in absolute coordinates).
	 */
	@Test
	public void test_dropDownBounds() throws Exception {
		// click on "popup": drop-down appears
		canvas.click(popupPart);
		GraphicalEditPart dropPart = (GraphicalEditPart) popupPart.getChildren().get(0);
		assertEquals(
				GraphicalRobot.getAbsoluteBounds(popupPart).getBottomLeft(),
				dropPart.getFigure().getLocation());
	}

	/**
	 * When we select "popup" in components tree, it shows drop-down.
	 */
	@Test
	public void test_selectPopupInTreeToOpen() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		// select "popup" in tree: drop-down appears
		tree.select(popupInfo);
		assertEquals(EditPart.SELECTED_PRIMARY, popupPart.getSelected());
		{
			List<EditPart> children = popupPart.getChildren();
			assertEquals(1, children.size());
			assertInstanceOf(EditPart.class, children.get(0));
		}
		// select nothing tree: drop-down disappears
		tree.select();
		assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		assertTrue(popupPart.getChildren().isEmpty());
	}

	/**
	 * When we select "popup" in components tree, it shows drop-down.<br>
	 * If we then click on "shell" on canvas, "popup" should be closed.
	 */
	@Test
	public void test_selectPopupInTreeToOpen_deselectOnCanvas() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		// select "popup" in tree: drop-down appears
		tree.select(popupInfo);
		assertEquals(EditPart.SELECTED_PRIMARY, popupPart.getSelected());
		{
			List<EditPart> children = popupPart.getChildren();
			assertEquals(1, children.size());
			assertInstanceOf(EditPart.class, children.get(0));
		}
		// select nothing on canvas: drop-down disappears
		canvas.select();
		assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		assertTrue(popupPart.getChildren().isEmpty());
	}

	/**
	 * When we select "item" in components tree, it shows drop-down and becomes selected in GEF.
	 */
	@Test
	public void test_selectItemInTreeToOpen() throws Exception {
		// initially no drop-down EditPart for "popup" and no EditPart for "item"
		assertTrue(popupPart.getChildren().isEmpty());
		assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		canvas.assertNullEditPart(itemInfo);
		// select "item" in tree: drop-down appears, with selected "item"
		GraphicalEditPart itemPart;
		EditPart dropPart;
		{
			tree.select(itemInfo);
			// check "drop-down"
			dropPart = popupPart.getChildren().get(0);
			// check "item" part
			itemPart = canvas.getEditPart(itemInfo);
			assertNotNull(itemPart);
			assertEquals(EditPart.SELECTED_PRIMARY, itemPart.getSelected());
			assertSame(dropPart, itemPart.getParent());
			// "popup" is not selected, we select "item"
			assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		}
		// select "popup", drop-down and "item" should stay same
		{
			tree.select(popupInfo);
			assertSame(dropPart, popupPart.getChildren().get(0));
			assertSame(itemPart, dropPart.getChildren().get(0));
			// check selections
			assertEquals(EditPart.SELECTED_PRIMARY, popupPart.getSelected());
			assertEquals(EditPart.SELECTED_NONE, itemPart.getSelected());
		}
		// select nothing tree: drop-down and "item" disappear
		tree.select();
		assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
		assertTrue(popupPart.getChildren().isEmpty());
		canvas.assertNullEditPart(itemInfo);
	}

	//
	/**
	 * When we delete "popup" it, and its drop-down {@link EditPart}'s are removed.
	 */
	@Test
	public void test_selectThenDelete() throws Exception {
		// initially no drop-down EditPart for "popup" and no EditPart for "item"
		assertTrue(popupPart.getChildren().isEmpty());
		// select "popup": this shows drop-down
		tree.select(popupInfo);
		EditPart dropPart = popupPart.getChildren().get(0);
		assertSame(popupPart, dropPart.getParent());
		// delete "popup"
		popupInfo.delete();
		canvas.assertNullEditPart(popupInfo);
	}

	/**
	 * Test that "popup" uses {@link MenuSelectionEditPolicy}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void test_selectionPolicy() throws Exception {
		MenuSelectionEditPolicy selectionPolicy =
				(MenuSelectionEditPolicy) popupPart.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
		// check selection handles
		{
			List<Handle> selectionHandles =
					(List<Handle>) ReflectionUtils.invokeMethod2(selectionPolicy, "createSelectionHandles");
			assertEquals(0, selectionHandles.size());
		}
		// select and check for feedback
		{
			menuTester.assertMenuNoFeedbacks();
			tree.select(popupInfo);
			menuTester.assertFeedback_selection(popupPart);
		}
		// clear selection: again no menu feedback
		{
			tree.select();
			menuTester.assertMenuNoFeedbacks();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * When we try to drop not menu related object, "popup" ignores it.
	 */
	@Test
	public void test_dropNotMenu() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		// begin creating MenuItem
		loadCreationTool("org.eclipse.swt.widgets.Button");
		// move on "popup": not menu related, so no drop-down
		canvas.moveTo(popupPart);
		assertTrue(popupPart.getChildren().isEmpty());
	}

	/**
	 * When we move {@link CreationTool} over "popup" icon, it shows drop-down, so we can drop into
	 * it.
	 */
	@Test
	public void test_dropNewItem_1() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		// begin creating MenuItem
		loadCreationTool("org.eclipse.swt.widgets.MenuItem");
		// move on "popup": drop-down appears
		{
			canvas.moveTo(popupPart);
			menuTester.assertMenuTargetFeedback(popupPart);
			EditPart dropPart = popupPart.getChildren().get(0);
			GraphicalEditPart itemPart = canvas.getEditPart(itemInfo);
			// no selections
			assertEquals(EditPart.SELECTED_NONE, popupPart.getSelected());
			assertEquals(EditPart.SELECTED_NONE, dropPart.getSelected());
			assertEquals(EditPart.SELECTED_NONE, itemPart.getSelected());
		}
		// click on "shell": feedback/drop-down disappears
		{
			canvas.click(shellPart, 100, 100);
			menuTester.assertMenuNoFeedbacks();
			assertTrue(popupPart.getChildren().isEmpty());
		}
	}

	/**
	 * Add new item before existing "item".
	 */
	@Test
	public void test_dropNewItem_2() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		// begin creating MenuItem
		JavaInfo newItemInfo = loadCreationTool("org.eclipse.swt.widgets.MenuItem");
		// move on "popup": drop-down appears
		GraphicalEditPart itemPart;
		{
			canvas.moveTo(popupPart);
			menuTester.assertMenuTargetFeedback(popupPart);
			itemPart = canvas.getEditPart(itemInfo);
		}
		// move on upper part of "item": add before feedback
		{
			canvas.moveTo(itemPart, 1, 1);
			menuTester.assertMenuLineFeedback(itemPart, IPositionConstants.TOP);
		}
		// do click
		canvas.click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu popup = new Menu(this);",
				"    setMenu(popup);",
				"    {",
				"      MenuItem menuItem = new MenuItem(popup, SWT.NONE);",
				"      menuItem.setText('New Item');",
				"    }",
				"    {",
				"      MenuItem menuItem = new MenuItem(popup, SWT.NONE);",
				"      menuItem.setText('Item 1');",
				"    }",
				"  }",
				"}");
		// EditPart for "newItem" exists and selected
		{
			GraphicalEditPart newItemPart = canvas.getEditPart(newItemInfo);
			assertNotNull(newItemPart);
			assertEquals(EditPart.SELECTED_PRIMARY, newItemPart.getSelected());
		}
	}

	/**
	 * Add new item after existing "item".
	 */
	@Test
	public void test_dropNewItem_3() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		// begin creating MenuItem
		JavaInfo newItemInfo = loadCreationTool("org.eclipse.swt.widgets.MenuItem");
		// move on "popup": drop-down appears
		GraphicalEditPart itemPart;
		{
			canvas.moveTo(popupPart);
			menuTester.assertMenuTargetFeedback(popupPart);
			itemPart = canvas.getEditPart(itemInfo);
		}
		// move on lower part of "item": add after feedback
		{
			canvas.moveTo(itemPart, 1, -1);
			menuTester.assertMenuLineFeedback(itemPart, IPositionConstants.BOTTOM);
		}
		// do click
		canvas.click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu popup = new Menu(this);",
				"    setMenu(popup);",
				"    {",
				"      MenuItem menuItem = new MenuItem(popup, SWT.NONE);",
				"      menuItem.setText('Item 1');",
				"    }",
				"    {",
				"      MenuItem menuItem = new MenuItem(popup, SWT.NONE);",
				"      menuItem.setText('New Item');",
				"    }",
				"  }",
				"}");
		// EditPart for "newItem" exists and selected
		{
			GraphicalEditPart newItemPart = canvas.getEditPart(newItemInfo);
			assertNotNull(newItemPart);
			assertEquals(EditPart.SELECTED_PRIMARY, newItemPart.getSelected());
		}
	}

	/**
	 * Begin adding new item, but reconsider and unload {@link CreationTool}.
	 */
	@Test
	public void test_dropNewItem_4() throws Exception {
		// initially no drop-down EditPart for "popup"
		assertTrue(popupPart.getChildren().isEmpty());
		// begin creating MenuItem
		loadCreationTool("org.eclipse.swt.widgets.MenuItem");
		// move on "popup": drop-down appears
		GraphicalEditPart itemPart;
		{
			canvas.moveTo(popupPart);
			menuTester.assertMenuTargetFeedback(popupPart);
			itemPart = canvas.getEditPart(itemInfo);
		}
		// move to "item": this locks drop-down
		canvas.moveTo(itemPart, 1, -1);
		// move from "item" on "shell": but "item" still visible
		canvas.moveTo(shellPart, 200, 100);
		assertSame(itemPart, canvas.getEditPart(itemInfo));
		// unload CreationTool: now "item" should be removed
		m_viewerCanvas.getEditDomain().loadDefaultTool();
		canvas.assertNullEditPart(itemInfo);
	}
}
