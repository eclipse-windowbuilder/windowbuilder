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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.SelectionActionsSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link AbsoluteLayoutInfo} selection action's.
 *
 * @author lobas_av
 */
public class AbsoluteLayoutSelectionActionsTest extends RcpModelTest {
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
	// Common
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_selectionActions() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(70, 27, 83, 22);",
						"      button.setText('New Button1');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(41, 129, 134, 84);",
						"      button.setText('New Button');",
						"    }",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setBounds(286, 135, 134, 120);",
						"        {",
						"          Label label = new Label(composite, SWT.NONE);",
						"          label.setBounds(41, 53, 51, 13);",
						"          label.setText('New Label');",
						"        }",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(2);
		ControlInfo label = composite.getChildrenControls().get(0);
		setupSelectionActions(shell);
		setupSelectionActions(composite);
		shell.refresh();
		// prepare "button" selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		selectedObjects.add(button);
		// prepare actions
		List<Object> actions = new ArrayList<>();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		// check actions
		assertEquals(17, actions.size()); // 12 action's, 5 separator's
		assertNotNull(findAction(actions, "Align left edges"));
		assertNotNull(findAction(actions, "Align horizontal centers"));
		assertNotNull(findAction(actions, "Align right edges"));
		assertNotNull(findAction(actions, "Align top edges"));
		assertNotNull(findAction(actions, "Align vertical centers"));
		assertNotNull(findAction(actions, "Align bottom edges"));
		assertNotNull(findAction(actions, "Replicate width"));
		assertNotNull(findAction(actions, "Replicate height"));
		assertNotNull(findAction(actions, "Space equally, horizontally"));
		assertNotNull(findAction(actions, "Space equally, vertically"));
		assertNotNull(findAction(actions, "Center horizontally in window"));
		assertNotNull(findAction(actions, "Center vertically in window"));
		// check enabled
		assertFalse(findAction(actions, "Align left edges").isEnabled());
		assertFalse(findAction(actions, "Align horizontal centers").isEnabled());
		assertFalse(findAction(actions, "Align right edges").isEnabled());
		assertFalse(findAction(actions, "Align top edges").isEnabled());
		assertFalse(findAction(actions, "Align vertical centers").isEnabled());
		assertFalse(findAction(actions, "Align bottom edges").isEnabled());
		assertFalse(findAction(actions, "Replicate width").isEnabled());
		assertFalse(findAction(actions, "Replicate height").isEnabled());
		assertFalse(findAction(actions, "Space equally, horizontally").isEnabled());
		assertFalse(findAction(actions, "Space equally, vertically").isEnabled());
		assertTrue(findAction(actions, "Center horizontally in window").isEnabled());
		assertTrue(findAction(actions, "Center vertically in window").isEnabled());
		// prepare "button composite" selection
		selectedObjects.clear();
		selectedObjects.add(button);
		selectedObjects.add(composite);
		// prepare actions
		actions.clear();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		assertTrue(findAction(actions, "Align left edges").isEnabled());
		assertTrue(findAction(actions, "Align horizontal centers").isEnabled());
		assertTrue(findAction(actions, "Align right edges").isEnabled());
		assertTrue(findAction(actions, "Align top edges").isEnabled());
		assertTrue(findAction(actions, "Align vertical centers").isEnabled());
		assertTrue(findAction(actions, "Align bottom edges").isEnabled());
		assertTrue(findAction(actions, "Replicate width").isEnabled());
		assertTrue(findAction(actions, "Replicate height").isEnabled());
		assertTrue(findAction(actions, "Space equally, horizontally").isEnabled());
		assertTrue(findAction(actions, "Space equally, vertically").isEnabled());
		assertTrue(findAction(actions, "Center horizontally in window").isEnabled());
		assertTrue(findAction(actions, "Center vertically in window").isEnabled());
		// prepare "button label" selection
		selectedObjects.clear();
		selectedObjects.add(button);
		selectedObjects.add(label);
		// prepare actions
		actions.clear();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		assertTrue(findAction(actions, "Align left edges").isEnabled());
		assertTrue(findAction(actions, "Align horizontal centers").isEnabled());
		assertTrue(findAction(actions, "Align right edges").isEnabled());
		assertTrue(findAction(actions, "Align top edges").isEnabled());
		assertTrue(findAction(actions, "Align vertical centers").isEnabled());
		assertTrue(findAction(actions, "Align bottom edges").isEnabled());
		assertTrue(findAction(actions, "Replicate width").isEnabled());
		assertTrue(findAction(actions, "Replicate height").isEnabled());
		assertFalse(findAction(actions, "Space equally, horizontally").isEnabled());
		assertFalse(findAction(actions, "Space equally, vertically").isEnabled());
		assertTrue(findAction(actions, "Center horizontally in window").isEnabled());
		assertTrue(findAction(actions, "Center vertically in window").isEnabled());
		// check wrong selection
		selectedObjects.clear();
		selectedObjects.add(button);
		selectedObjects.add(label);
		selectedObjects.add(new TestObjectInfo());
		// prepare actions
		actions.clear();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		assertTrue(actions.isEmpty());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Horizontal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * One parent selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_left_edges_1a() throws Exception {
		check_align_horizontal(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(20, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(20, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align left edges", true);
	}

	/**
	 * One parent selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_left_edges_1b() throws Exception {
		check_align_horizontal(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align left edges", false);
	}

	/**
	 * Two parent's selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_left_edges_2a() throws Exception {
		check_align_horizontal2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(30, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(25, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align left edges", true);
	}

	/**
	 * Two parent's selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_left_edges_2b() throws Exception {
		check_align_horizontal2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align left edges", false);
	}

	/**
	 * One parent selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_right_edges_1a() throws Exception {
		check_align_horizontal(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(20, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align right edges", true);
	}

	/**
	 * One parent selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_right_edges_1b() throws Exception {
		check_align_horizontal(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(-40, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align right edges", false);
	}

	/**
	 * Two parent's selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_right_edges_2a() throws Exception {
		check_align_horizontal2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(80, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(25, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align right edges", true);
	}

	/**
	 * Two parent's selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_right_edges_2b() throws Exception {
		check_align_horizontal2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(-45, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align right edges", false);
	}

	/**
	 * One parent selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_horizontal_centers_1a() throws Exception {
		check_align_horizontal(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(45, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(20, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align horizontal centers", true);
	}

	/**
	 * One parent selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_horizontal_centers_1b() throws Exception {
		check_align_horizontal(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(-15, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align horizontal centers", false);
	}

	/**
	 * Two parent's selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_horizontal_centers_2a() throws Exception {
		check_align_horizontal2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(55, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(25, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align horizontal centers", true);
	}

	/**
	 * Two parent's selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_horizontal_centers_2b() throws Exception {
		check_align_horizontal2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(-20, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align horizontal centers", false);
	}

	private void check_align_horizontal(String[] expectedSource, String action, boolean toUp)
			throws Exception {
		check_align(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(20, 50, 150, 30);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, expectedSource, action, toUp);
	}

	private void check_align_horizontal2(String[] expectedSource, String action, boolean toUp)
			throws Exception {
		check_align2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 20);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 40, 300, 100);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(25, 55, 150, 30);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, expectedSource, action, toUp);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Vertical
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * One parent selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_top_edges_1a() throws Exception {
		check_align_vertical(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 100, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align top edges", true);
	}

	/**
	 * One parent selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_top_edges_1b() throws Exception {
		check_align_vertical(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 10, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align top edges", false);
	}

	/**
	 * Two parent's selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_top_edges_2a() throws Exception {
		check_align_vertical2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 100, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, 95, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align top edges", true);
	}

	/**
	 * Two parent's selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_top_edges_2b() throws Exception {
		check_align_vertical2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, 5, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align top edges", false);
	}

	/**
	 * One parent selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_bottom_edges_1a() throws Exception {
		// y2:100 + h2:80 - h1:40 = y1:140
		check_align_vertical(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 140, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align bottom edges", true);
	}

	/**
	 * One parent selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_bottom_edges_1b() throws Exception {
		// y2:100 + h2:80 - h1:40 = y1:140
		check_align_vertical(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, -30, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align bottom edges", false);
	}

	/**
	 * Two parent's selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_bottom_edges_2a() throws Exception {
		check_align_vertical2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 140, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, 95, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align bottom edges", true);
	}

	/**
	 * Two parent's selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_bottom_edges_2b() throws Exception {
		check_align_vertical2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, -35, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align bottom edges", false);
	}

	/**
	 * One parent selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_vertical_centers_1a() throws Exception {
		// y2:100 + (h2:80 / 2) - (h1:40 / 2) = y1:120
		check_align_vertical(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 120, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align vertical centers", true);
	}

	/**
	 * One parent selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_vertical_centers_1b() throws Exception {
		// y2:100 + (h2:80 / 2) - (h1:40 / 2) = y1:120
		check_align_vertical(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, -10, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Align vertical centers", false);
	}

	/**
	 * Two parent's selection objects, order: Bottom-Up.
	 */
	@Test
	public void test_align_vertical_centers_2a() throws Exception {
		check_align_vertical2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 120, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, 95, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align vertical centers", true);
	}

	/**
	 * Two parent's selection objects, order: Top-Down.
	 */
	@Test
	public void test_align_vertical_centers_2b() throws Exception {
		check_align_vertical2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, -15, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, "Align vertical centers", false);
	}

	private void check_align_vertical(String[] newSource, String action, boolean toUp)
			throws Exception {
		check_align(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, newSource, action, toUp);
	}

	private void check_align_vertical2(String[] newSource, String action, boolean toUp)
			throws Exception {
		check_align2(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(65, 5, 300, 300);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(5, 95, 100, 80);",
				"        button.setText(\"111\");",
				"      }",
				"    }",
				"  }",
		"}"}, newSource, action, toUp);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Width/Height
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_align_replicate_width() throws Exception {
		check_align(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 100, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Replicate width", true);
	}

	@Test
	public void test_align_replicate_height() throws Exception {
		check_align(new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 40);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, new String[]{
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 80);",
				"      button.setText(\"000\");",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(70, 100, 100, 80);",
				"      button.setText(\"111\");",
				"    }",
				"  }",
		"}"}, "Replicate height", true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Space equally
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for two object's without Ctrl pressed.
	 */
	@Test
	public void test_align_space_equally_1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setSize(600, 400);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(30, 90, 100, 70);",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(40, 200, 50, 30);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		setupSelectionActions(shell);
		shell.refresh();
		// prepare selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		selectedObjects.add(shell.getChildrenControls().get(0));
		selectedObjects.add(shell.getChildrenControls().get(1));
		// prepare actions
		List<Object> actions = new ArrayList<>();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		findAction(actions, "Space equally, horizontally").run();
		findAction(actions, "Space equally, vertically").run();
		//
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setSize(600, 400);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(150, 100, 100, 70);",
				"      button.setText('000');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(400, 270, 50, 30);",
				"      button.setText('111');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for three object's with Ctrl pressed.
	 */
	@Test
	public void test_align_space_equally_2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setSize(400, 400);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(10, 10, 50, 50);",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(90, 90, 60, 60);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(220, 220, 70, 70);",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		setupSelectionActions(shell);
		shell.refresh();
		// prepare selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		selectedObjects.add(shell.getChildrenControls().get(0));
		selectedObjects.add(shell.getChildrenControls().get(1));
		selectedObjects.add(shell.getChildrenControls().get(2));
		// prepare actions
		List<Object> actions = new ArrayList<>();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		try {
			ReflectionUtils.setField(DesignerPlugin.class, "m_ctrlPressed", true);
			findAction(actions, "Space equally, horizontally").run();
			findAction(actions, "Space equally, vertically").run();
		} finally {
			ReflectionUtils.setField(DesignerPlugin.class, "m_ctrlPressed", false);
		}
		//
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setSize(400, 400);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(10, 10, 50, 50);",
				"      button.setText('000');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(110, 110, 60, 60);",
				"      button.setText('111');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(220, 220, 70, 70);",
				"      button.setText('222');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Center in window
	//
	////////////////////////////////////////////////////////////////////////////
	@Ignore
	@Test
	public void test_align_center_in_window() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setSize(600, 400);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(10, 10, 60, 40);",
						"      button.setText('000');",
						"    }",
						"  }",
						"}");
		setupSelectionActions(shell);
		shell.refresh();
		// prepare selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		selectedObjects.add(shell.getChildrenControls().get(0));
		// prepare actions
		List<Object> actions = new ArrayList<>();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		findAction(actions, "Center horizontally in window").run();
		findAction(actions, "Center vertically in window").run();
		//
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setSize(600, 400);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(262, 180, 60, 40);",
				"      button.setText('000');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cases
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ScrolledComposite_onWayToRoot() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.NONE);",
						"    {",
						"      Composite composite = new Composite(scrolledComposite, SWT.NONE);",
						"      composite.setLayout(null);",
						"      {",
						"        Button button_1 = new Button(composite, SWT.NONE);",
						"        button_1.setBounds(10, 10, 100, 20);",
						"      }",
						"      {",
						"        Button button_2 = new Button(composite, SWT.NONE);",
						"        button_2.setBounds(20, 100, 100, 20);",
						"      }",
						"      scrolledComposite.setContent(composite);",
						"      scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		//
		// prepare selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		{
			selectedObjects.add(getJavaInfoByName("button_1"));
			selectedObjects.add(getJavaInfoByName("button_2"));
		}
		// prepare actions
		List<Object> actions;
		{
			CompositeInfo composite = getJavaInfoByName("composite");
			setupSelectionActions(composite);
			actions = new ArrayList<>();
			shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		}
		//
		findAction(actions, "Align left edges").run();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_align(String[] initialSource,
			String[] expectedSource,
			String action,
			boolean toUp) throws Exception {
		CompositeInfo shell = parseComposite(initialSource);
		setupSelectionActions(shell);
		shell.refresh();
		// prepare selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		if (toUp) {
			selectedObjects.add(shell.getChildrenControls().get(1));
			selectedObjects.add(shell.getChildrenControls().get(0));
		} else {
			selectedObjects.add(shell.getChildrenControls().get(0));
			selectedObjects.add(shell.getChildrenControls().get(1));
		}
		// prepare actions
		List<Object> actions = new ArrayList<>();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		findAction(actions, action).run();
		//
		assertEditor(expectedSource);
	}

	private void check_align2(String[] initialSource,
			String[] expectedSource,
			String action,
			boolean toUp) throws Exception {
		CompositeInfo shell = parseComposite(initialSource);
		setupSelectionActions(shell);
		shell.refresh();
		// prepare selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(1);
		setupSelectionActions(composite);
		if (toUp) {
			selectedObjects.add(composite.getChildrenControls().get(0));
			selectedObjects.add(shell.getChildrenControls().get(0));
		} else {
			selectedObjects.add(shell.getChildrenControls().get(0));
			selectedObjects.add(composite.getChildrenControls().get(0));
		}
		// prepare actions
		List<Object> actions = new ArrayList<>();
		shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		findAction(actions, action).run();
		//
		assertEditor(expectedSource);
	}

	private void setupSelectionActions(final CompositeInfo composite) {
		composite.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
					throws Exception {
				AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) composite.getLayout();
				new SelectionActionsSupport<>(layout).addAlignmentActions(objects, actions);
			}
		});
	}
}