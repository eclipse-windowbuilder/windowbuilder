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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.palette.AbsoluteLayoutEntryInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link TableWrapLayout} in GEF.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutGefTest extends RcpGefTest {
	private static final int M = 5;
	private static final int S = 5;
	private static final int VS = 25;
	private static final int VG = 5;

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
	 * When we delete component of expandable/collapsible container, selection {@link Handle} receives
	 * ancestor resize event, so tries to update {@link Handle} location. However at this time
	 * component may be already deleted, so we can not ask for its cell/bounds.
	 */
	@Test
	public void test_deleteChildAndAncestorResize() throws Exception {
		CompositeInfo shell =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new TableWrapLayout());",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayout(new TableWrapLayout());",
						"      {",
						"        Label label = new Label(composite, SWT.NONE);",
						"        label.setText('Label');",
						"      }",
						"      {",
						"        Button button = new Button(composite, SWT.NONE);",
						"        button.setText('Button');",
						"      }",
						"    }",
						"  }",
						"}");
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
		ControlInfo button = composite.getChildrenControls().get(1);
		// select "button"
		canvas.select(button);
		waitEventLoop(10);
		// delete
		{
			IAction deleteAction = getDeleteAction();
			assertTrue(deleteAction.isEnabled());
			deleteAction.run();
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new TableWrapLayout());",
					"    {",
					"      Composite composite = new Composite(this, SWT.NONE);",
					"      composite.setLayout(new TableWrapLayout());",
					"      {",
					"        Label label = new Label(composite, SWT.NONE);",
					"        label.setText('Label');",
					"      }",
					"    }",
					"  }",
					"}");
		}
	}

	/**
	 * There was problem that after replacing {@link TableWrapLayout} with "absolute", column/row
	 * headers throw exception.
	 */
	@Test
	public void test_replaceGridLayout_withAbsolute() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		// select "shell", so show headers
		canvas.select(shell);
		waitEventLoop(0);
		// drop "absolute"
		{
			AbsoluteLayoutEntryInfo absoluteEntry = new AbsoluteLayoutEntryInfo();
			absoluteEntry.initialize(m_viewerCanvas, shell);
			absoluteEntry.activate(false);
			canvas.target(shell).in(250, 50).move().click();
			waitEventLoop(0);
		}
		// validate
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setBounds(5, 5, 100, 50);",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setBounds(110, 60, 100, 50);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * When user externally (not using design canvas) changes "numColumns", we should recalculate
	 * positions of controls, in other case we will have incorrect count of column/row headers.
	 */
	@Test
	public void test_change_numColumns() throws Exception {
		CompositeInfo shell =
				openComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    Button button_00 = new Button(this, SWT.NONE);",
						"    Button button_01 = new Button(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		// select "shell", so show headers
		canvas.select(shell);
		// initially: 1 column, 2 rows
		assertEquals(1, layout.getColumns().size());
		assertEquals(2, layout.getRows().size());
		// set: 2 columns, so 1 row
		// this caused exception in headers refresh
		layout.getPropertyByTitle("numColumns").setValue(2);
		assertNoLoggedExceptions();
		assertEquals(2, layout.getColumns().size());
		assertEquals(1, layout.getRows().size());
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    Button button_00 = new Button(this, SWT.NONE);",
				"    Button button_01 = new Button(this, SWT.NONE);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Size hint
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setSizeHint_height() throws Exception {
		CompositeInfo shell =
				openComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new TableWrapLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('New Button');",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		// resize SOUTH of "button"
		canvas.toResizeHandle(button, "resize_size", IPositionConstants.SOUTH).beginDrag();
		canvas.target(button).in(0, 50).drag();
		canvas.endDrag();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        TableWrapData twd_button = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
				"        twd_button.heightHint = 50;",
				"        button.setLayoutData(twd_button);",
				"      }",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_filled() throws Exception {
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new TableWrapLayout());",
						"    {",
						"      Button existingButton = new Button(this, SWT.NONE);",
						"      existingButton.setText('Existing Button');",
						"    }",
						"  }",
						"}");
		//
		loadButtonWithText();
		canvas.moveTo(composite, M, M);
		canvas.assertCommandNull();
	}

	@Test
	public void test_CREATE_virtual_0x0() throws Exception {
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new TableWrapLayout());",
						"  }",
						"}");
		//
		loadButtonWithText();
		canvas.moveTo(composite, M, M);
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_virtual_0x1() throws Exception {
		CompositeInfo composite =
				openComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		//
		loadButtonWithText();
		canvas.moveTo(composite, M + VS + VG, M);
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_appendToColumn_1x0() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"  }",
				"}");
		JavaInfo existingButton = getJavaInfoByName("existingButton");
		//
		loadButtonWithText();
		canvas.target(existingButton).inX(0.5).outY(S + 1).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_appendToRow_0x1() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"  }",
				"}");
		JavaInfo existingButton = getJavaInfoByName("existingButton");
		//
		loadButtonWithText();
		canvas.target(existingButton).inY(0.5).outX(S + 1).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_beforeFirstRow() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"  }",
				"}");
		JavaInfo existingButton = getJavaInfoByName("existingButton");
		//
		loadButtonWithText();
		canvas.target(existingButton).inX(0.5).outY(-2).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_beforeFirstColumn() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"  }",
				"}");
		JavaInfo existingButton = getJavaInfoByName("existingButton");
		//
		loadButtonWithText();
		canvas.target(existingButton).inY(0.5).outX(-2).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('Existing Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_insertColumn() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"  }",
				"}");
		JavaInfo button_1 = getJavaInfoByName("button_1");
		//
		loadButtonWithText();
		canvas.target(button_1).inY(0.5).outX(S / 2).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 3;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_insertRow() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"  }",
				"}");
		JavaInfo button_1 = getJavaInfoByName("button_1");
		//
		loadButtonWithText();
		canvas.target(button_1).inX(0.5).outY(S / 2).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PASTE
	//
	////////////////////////////////////////////////////////////////////////////
	@Ignore
	@Test
	public void test_PASTE_virtual_1x0() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('My Button');",
				"    }",
				"  }",
				"}");
		JavaInfo existingButton = getJavaInfoByName("existingButton");
		//
		doCopyPaste(existingButton);
		canvas.target(existingButton).inX(0.5).outY(S + 1).move();
		canvas.click();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button existingButton = new Button(this, SWT.NONE);",
				"      existingButton.setText('My Button');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('My Button');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE_virtual_1x0() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('Existing Button');",
				"    }",
				"  }",
				"}");
		JavaInfo button = getJavaInfoByName("button");
		//
		canvas.beginDrag(button);
		canvas.target(button).inX(0.5).outY(S + 1).drag();
		canvas.endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new TableWrapLayout());",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('Existing Button');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_ADD_virtual_0x0() throws Exception {
		openComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('Existing Button');",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new TableWrapLayout());",
				"    }",
				"  }",
				"}");
		JavaInfo button = getJavaInfoByName("button");
		JavaInfo composite = getJavaInfoByName("composite");
		//
		canvas.beginDrag(button);
		canvas.dragTo(composite, M + VS / 2, M + VS / 2);
		canvas.endDrag();
		assertEditor(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new FillLayout());",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new TableWrapLayout());",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setText('Existing Button');",
				"      }",
				"    }",
				"  }",
				"}");
	}
}
