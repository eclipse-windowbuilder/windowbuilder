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
package org.eclipse.wb.tests.designer.rcp.model.layout;

import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IAction;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class AbsoluteLayoutGefTest extends RcpGefTest {
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
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_canvas_CREATE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		// create Button
		loadCreationButton();
		// use canvas
		canvas.sideMode().create(100, 50);
		canvas.target(composite).in(30, 40).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(30, 40, 100, 50);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_PASTE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Button buttonA = new Button(this, SWT.NONE);",
						"      buttonA.setEnabled(false);",
						"      buttonA.setBounds(10, 10, 100, 50);",
						"    }",
						"  }",
						"}");
		// copy "buttonA"
		{
			// select "buttonA"
			ControlInfo buttonA = composite.getChildrenControls().get(0);
			canvas.select(buttonA);
			// do copy
			IAction copyAction = getCopyAction();
			assertTrue(copyAction.isEnabled());
			copyAction.run();
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
		}
		// move
		canvas.sideMode().create(100, 50);
		canvas.target(composite).inX(50).inY(100).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button buttonA = new Button(this, SWT.NONE);",
				"      buttonA.setEnabled(false);",
				"      buttonA.setBounds(10, 10, 100, 50);",
				"    }",
				"    {",
				"      Button buttonA = new Button(this, SWT.NONE);",
				"      buttonA.setEnabled(false);",
				"      buttonA.setBounds(50, 100, 100, 50);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_MOVE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(30, 40, 100, 50);",
						"    }",
						"  }",
						"}");
		ControlInfo button = composite.getChildrenControls().get(0);
		// move
		canvas.sideMode().beginMove(button);
		canvas.target(composite).inX(50).inY(80).drag();
		canvas.endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(50, 80, 100, 50);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_RESIZE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(30, 40, 100, 50);",
						"    }",
						"  }",
						"}");
		ControlInfo button = composite.getChildrenControls().get(0);
		//
		canvas.beginResize(button, PositionConstants.SOUTH_EAST);
		canvas.dragTo(button, 150, 100).endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(30, 40, 150, 100);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_ADD() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Composite inner = new Composite(this, SWT.NONE);",
						"      inner.setBounds(20, 100, 200, 150);",
						"      {",
						"        Button button = new Button(inner, SWT.NONE);",
						"        button.setBounds(10, 20, 100, 50);",
						"      }",
						"    }",
						"  }",
						"}");
		CompositeInfo inner = (CompositeInfo) composite.getChildrenControls().get(0);
		ControlInfo button = inner.getChildrenControls().get(0);
		// move
		canvas.sideMode().beginMove(button);
		canvas.target(composite).inX(50).inY(20).drag();
		canvas.endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Composite inner = new Composite(this, SWT.NONE);",
				"      inner.setBounds(20, 100, 200, 150);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(50, 20, 100, 50);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		// create Button
		ControlInfo newButton = loadCreationButton();
		// use tree
		tree.moveOn(composite);
		tree.assertFeedback_on(composite);
		tree.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(0, 0, 100, 50);",
				"    }",
				"  }",
				"}");
		tree.assertPrimarySelected(newButton);
	}

	@Test
	public void test_tree_PASTE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Button buttonA = new Button(this, SWT.NONE);",
						"      buttonA.setEnabled(false);",
						"      buttonA.setBounds(10, 10, 100, 50);",
						"    }",
						"  }",
						"}");
		// copy "buttonA"
		{
			// select "buttonA"
			ControlInfo buttonA = composite.getChildrenControls().get(0);
			canvas.select(buttonA);
			// do copy
			IAction copyAction = getCopyAction();
			assertTrue(copyAction.isEnabled());
			copyAction.run();
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
		}
		// use tree
		tree.moveOn(composite);
		tree.assertFeedback_on(composite);
		tree.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button buttonA = new Button(this, SWT.NONE);",
				"      buttonA.setEnabled(false);",
				"      buttonA.setBounds(10, 10, 100, 50);",
				"    }",
				"    {",
				"      Button buttonA = new Button(this, SWT.NONE);",
				"      buttonA.setEnabled(false);",
				"      buttonA.setBounds(0, 0, 100, 50);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_tree_MOVE() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Button buttonA = new Button(this, SWT.NONE);",
						"      buttonA.setBounds(10, 10, 100, 50);",
						"    }",
						"    {",
						"      Button buttonB = new Button(this, SWT.NONE);",
						"      buttonB.setBounds(20, 100, 100, 50);",
						"    }",
						"  }",
						"}");
		ControlInfo buttonA = composite.getChildrenControls().get(0);
		ControlInfo buttonB = composite.getChildrenControls().get(1);
		// use tree
		tree.startDrag(buttonB);
		tree.dragBefore(buttonA);
		tree.assertFeedback_before(buttonA);
		tree.endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Button buttonB = new Button(this, SWT.NONE);",
				"      buttonB.setBounds(20, 100, 100, 50);",
				"    }",
				"    {",
				"      Button buttonA = new Button(this, SWT.NONE);",
				"      buttonA.setBounds(10, 10, 100, 50);",
				"    }",
				"  }",
				"}");
		tree.assertPrimarySelected(buttonB);
	}

	@Test
	public void test_tree_ADD() throws Exception {
		prepareComponent();
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      Composite inner = new Composite(this, SWT.NONE);",
						"      inner.setBounds(20, 100, 200, 150);",
						"      {",
						"        Button button = new Button(inner, SWT.NONE);",
						"        button.setBounds(10, 20, 100, 50);",
						"      }",
						"    }",
						"  }",
						"}");
		CompositeInfo inner = (CompositeInfo) composite.getChildrenControls().get(0);
		ControlInfo button = inner.getChildrenControls().get(0);
		// use tree
		tree.startDrag(button);
		tree.dragOn(composite);
		tree.assertFeedback_on(composite);
		tree.endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      Composite inner = new Composite(this, SWT.NONE);",
				"      inner.setBounds(20, 100, 200, 150);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setSize(100, 50);",
				"    }",
				"  }",
				"}");
	}
}
