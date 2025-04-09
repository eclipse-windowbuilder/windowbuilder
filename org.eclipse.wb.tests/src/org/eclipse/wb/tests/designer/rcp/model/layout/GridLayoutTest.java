/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.junit.Test;

/**
 * Test for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class GridLayoutTest extends RcpModelTest {
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
	@Test
	public void test_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(new GridLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// "button" has horizontal grab, so big width
		ControlInfo button = shell.getChildrenControls().get(0);
		assertTrue(button.getBounds().width > 400);
	}

	/**
	 * When we have two nested {@link Composite} with {@link GridLayout}, it it executes "layout"
	 * method not only during layout itself, but also for calculating preferred size. This corrupts
	 * remembered values for column/row origins/sizes.
	 */
	@Test
	public void test_twoNested() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayoutData(new GridData(GridData.FILL_BOTH));",
						"      composite.setLayout(new GridLayout());",
						"      {",
						"        Button button = new Button(composite, SWT.NONE);",
						"        button.setLayoutData(new GridData(GridData.FILL_BOTH));",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// "button" has grab both, so has big size
		Rectangle buttonBounds;
		{
			ControlInfo button = getJavaInfoByName("button");
			buttonBounds = button.getBounds();
			assertTrue(buttonBounds.width > 400);
			assertTrue(buttonBounds.height > 200);
		}
		// check IGridInfo
		CompositeInfo composite = getJavaInfoByName("composite");
		GridLayoutInfo gridLayout = (GridLayoutInfo) composite.getLayout();
		IGridInfo gridInfo = gridLayout.getGridInfo();
		{
			Interval columnInterval = gridInfo.getColumnIntervals()[0];
			assertEquals(buttonBounds.x, columnInterval.begin());
			assertEquals(buttonBounds.width, columnInterval.length());
		}
		{
			Interval rowInterval = gridInfo.getRowIntervals()[0];
			assertEquals(buttonBounds.y, rowInterval.begin());
			assertEquals(buttonBounds.height, rowInterval.length());
		}
	}

	/**
	 * See (Case 39234).
	 */
	@Test
	public void test_defaultValues() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData =
				org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo.getGridData(button);
		// ask using methods
		assertEquals(SWT.LEFT, gridData.getHorizontalAlignment());
		assertEquals(SWT.CENTER, gridData.getVerticalAlignment());
		assertEquals(false, gridData.getHorizontalGrab());
		assertEquals(false, gridData.getVerticalGrab());
		// ask using properties
		{
			assertEquals(SWT.LEFT, gridData.getPropertyByTitle("horizontalAlignment").getValue());
			assertEquals(SWT.CENTER, gridData.getPropertyByTitle("verticalAlignment").getValue());
		}
	}

	/**
	 * Too smart users may override {@link Control#getLayoutData()} and always return some
	 * {@link GridData} instance, so we can not replace it in {@link Control}. We should store our
	 * <code>GridData2</code> into some other place.
	 */
	@Test
	public void test_getLayoutData_override() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private final GridData m_gridData = new GridData();",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  public Object getLayoutData() {",
						"    return m_gridData;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    new MyComposite(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Switching layouts
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test switching layouts from {@link TableWrapLayout} to {@link GridLayout}, and restore
	 * component positions & alignments.
	 */
	@Test
	public void test_Switching_fromTableWrapLayout() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    {",
						"      TableWrapLayout tableWrapLayout = new TableWrapLayout();",
						"      tableWrapLayout.numColumns = 3;",
						"      setLayout(tableWrapLayout);",
						"    }",
						"    {",
						"      Label label = new Label(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM, 1, 1);",
						"        tableWrapData.grabHorizontal = true;",
						"        label.setLayoutData(tableWrapData);",
						"      }",
						"      label.setText('New Label');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Text text = new Text(this, SWT.BORDER);",
						"      text.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP, 1, 2));",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
						"        tableWrapData.grabVertical = true;",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('New Button');",
						"    }",
						"  }",
						"}");
		composite.refresh();
		// set GridLayout
		GridLayoutInfo gridLayout =
				(GridLayoutInfo) BTestUtils.createLayout("org.eclipse.swt.layout.GridLayout");
		composite.setLayout(gridLayout);
		assertEditor(
				"class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    setLayout(new GridLayout(3, false));",
				"    {",
				"      Label label = new Label(this, SWT.NONE);",
				"      label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));",
				"      label.setText('New Label');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Text text = new Text(this, SWT.BORDER);",
				"      text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1));",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}
}