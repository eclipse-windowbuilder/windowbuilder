/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapColumnInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapRowInfo;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.model.forms.AbstractFormsTest;
import org.eclipse.wb.tests.designer.swt.model.jface.ViewerTest;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.junit.Test;

import java.util.List;

/**
 * Test for {@link TableWrapLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutTest extends AbstractFormsTest {
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
	 * Test for parsing empty {@link TableWrapLayoutInfo}.
	 */
	@Test
	public void test_parseEmpty() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new TableWrapLayout());",
						"  }",
						"}");
		shell.refresh();
		shell.refresh_dispose();
	}

	/**
	 * Fillers should be filtered out from presentation children.
	 */
	@Test
	public void test_excludeFillersFromPresentationChildren() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"  }",
						"}");
		//
		shell.refresh();
		try {
			assertEquals(2, shell.getChildrenControls().size());
			ControlInfo button = shell.getChildrenControls().get(0);
			ControlInfo filler = shell.getChildrenControls().get(1);
			//
			IObjectPresentation presentation = shell.getPresentation();
			{
				List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
				assertTrue(presentationChildren.contains(button));
				assertFalse(presentationChildren.contains(filler));
			}
			{
				List<ObjectInfo> presentationChildren = presentation.getChildrenGraphical();
				assertTrue(presentationChildren.contains(button));
				assertFalse(presentationChildren.contains(filler));
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	/**
	 * Test for {@link IGridInfo}.
	 */
	@Test
	public void test_gridInfo() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData();",
						"        tableWrapData.colspan = 2;",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button_0 = shell.getChildrenControls().get(0);
		ControlInfo button_1 = shell.getChildrenControls().get(3);
		ControlInfo button_2 = shell.getChildrenControls().get(4);
		//
		shell.refresh();
		try {
			IGridInfo gridInfo = layout.getGridInfo();
			assertNotNull(gridInfo);
			// check count of column/row
			assertEquals(2, gridInfo.getColumnCount());
			assertEquals(3, gridInfo.getRowCount());
			// check column intervals
			{
				int[] columnOrigins = layout.getColumnOrigins();
				Interval[] columnIntervals = gridInfo.getColumnIntervals();
				assertEquals(columnOrigins.length, columnIntervals.length);
				for (int i = 0; i < columnOrigins.length; i++) {
					int origin = columnOrigins[i];
					Interval interval = columnIntervals[i];
					assertEquals(origin, interval.begin());
				}
			}
			// check row intervals
			{
				int[] rowOrigins = layout.getRowOrigins();
				Interval[] rowIntervals = gridInfo.getRowIntervals();
				assertEquals(rowOrigins.length, rowIntervals.length);
			}
			// check component cells
			{
				assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_0));
				assertEquals(new Rectangle(1, 1, 1, 1), gridInfo.getComponentCells(button_1));
				assertEquals(new Rectangle(0, 2, 2, 1), gridInfo.getComponentCells(button_2));
			}
			// check cells rectangle
			{
				Rectangle rectangle = gridInfo.getCellsRectangle(new Rectangle(0, 0, 1, 1));
				assertEquals(5, rectangle.x);
				assertEquals(5, rectangle.y);
			}
			// insets
			assertEquals(new Insets(0, 0, 0, 0), gridInfo.getInsets());
			// check "virtual" feedback sizes
			{
				assertEquals(25, gridInfo.getVirtualColumnSize());
				assertEquals(5, gridInfo.getVirtualColumnGap());
				assertEquals(25, gridInfo.getVirtualRowSize());
				assertEquals(5, gridInfo.getVirtualRowGap());
			}
			// check occupied cells
			{
				assertSame(button_0, gridInfo.getOccupied(0, 0));
				assertSame(button_1, gridInfo.getOccupied(1, 1));
				assertSame(button_2, gridInfo.getOccupied(0, 2));
				assertSame(button_2, gridInfo.getOccupied(1, 2));
				assertNull(gridInfo.getOccupied(1, 0));
				assertNull(gridInfo.getOccupied(0, 1));
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	/**
	 * Test cells when {@link Shell#setSize(int, int)} is used.
	 */
	@Test
	public void test_gridInfo2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setSize(300, 200);",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    new Button(this, SWT.NONE);",
						"    new Button(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button_0 = shell.getChildrenControls().get(0);
		ControlInfo button_1 = shell.getChildrenControls().get(1);
		//
		shell.refresh();
		IGridInfo gridInfo = layout.getGridInfo();
		assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_0));
		assertEquals(new Rectangle(0, 1, 1, 1), gridInfo.getComponentCells(button_1));
	}

	/**
	 * Test for {@link IGridInfo} when there are not controls.
	 */
	@Test
	public void test_gridInfo_empty() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			IGridInfo gridInfo = layout.getGridInfo();
			assertEquals(0, gridInfo.getRowIntervals().length);
			assertEquals(0, gridInfo.getColumnIntervals().length);
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setCells()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setCells_horizontalSpan() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    new Label(this, SWT.RIGHT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		shell.refresh();
		try {
			TableWrapDataInfo layoutData = layout.getTableWrapData(button);
			// check initial TableWrapData
			{
				assertEquals(0, getInt(layoutData, "x"));
				assertEquals(0, getInt(layoutData, "y"));
				assertEquals(1, getInt(layoutData, "width"));
				assertEquals(1, getInt(layoutData, "height"));
			}
			// set horizontal span
			layout.command_setCells(button, new Rectangle(0, 0, 2, 1), true);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 2));",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.RIGHT);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
			// check TableWrapData
			{
				assertEquals(0, getInt(layoutData, "x"));
				assertEquals(0, getInt(layoutData, "y"));
				assertEquals(2, getInt(layoutData, "width"));
				assertEquals(1, getInt(layoutData, "height"));
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_setCells_horizontalSpan2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 2);",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.RIGHT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('000');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.RIGHT);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('111');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_setCells_verticalSpan() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    new Label(this, SWT.RIGHT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		shell.refresh();
		try {
			TableWrapDataInfo layoutData = layout.getTableWrapData(button);
			// check initial TableWrapData
			{
				assertEquals(0, getInt(layoutData, "x"));
				assertEquals(0, getInt(layoutData, "y"));
				assertEquals(1, getInt(layoutData, "width"));
				assertEquals(1, getInt(layoutData, "height"));
			}
			// set vertical span
			layout.command_setCells(button, new Rectangle(0, 0, 1, 2), true);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1));",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.LEFT);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
			// check TableWrapData
			{
				assertEquals(0, getInt(layoutData, "x"));
				assertEquals(0, getInt(layoutData, "y"));
				assertEquals(1, getInt(layoutData, "width"));
				assertEquals(2, getInt(layoutData, "height"));
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_setCells_verticalSpan2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1);",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('111');",
						"    }",
						"    new Label(this, SWT.RIGHT);",
						"  }",
						"}");
		shell.refresh();
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(1);
		//
		layout.command_setCells(button, new Rectangle(1, 1, 1, 1), true);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('000');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.RIGHT);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('111');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_setCells_move() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(2);
		//
		shell.refresh();
		try {
			layout.command_setCells(button, new Rectangle(1, 0, 1, 1), true);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('222');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
			// check x/y for new filler
			{
				ControlInfo filler = shell.getChildrenControls().get(2);
				TableWrapDataInfo layoutData = layout.getTableWrapData(filler);
				assertEquals(0, getInt(layoutData, "x"));
				assertEquals(1, getInt(layoutData, "y"));
				assertEquals(1, getInt(layoutData, "width"));
				assertEquals(1, getInt(layoutData, "height"));
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * When we delete {@link ControlInfo}, it should be replaced with filler.
	 */
	@Test
	public void test_delete_replaceWithFillers() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(2);
		//
		shell.refresh();
		try {
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.LEFT);",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	/**
	 * When we delete column, we should keep at least one column.
	 */
	@Test
	public void test_delete_keepOneColumn() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		shell.refresh();
		try {
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_delete_removeEmptyDimensions() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(5);
		//
		shell.refresh();
		try {
			{
				TableWrapDataInfo layoutData = layout.getTableWrapData(button);
				assertEquals(1, getInt(layoutData, "x"));
				assertEquals(2, getInt(layoutData, "y"));
			}
			//
			button.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_inEmptyCell() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    new Label(this, SWT.RIGHT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    new Label(this, SWT.RIGHT);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_insertRow() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    new Label(this, SWT.RIGHT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, false, 1, true);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.LEFT);",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    new Label(this, SWT.RIGHT);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_insertColumn() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.LEFT);",
						"    new Label(this, SWT.RIGHT);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, true, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 3;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    new Label(this, SWT.LEFT);",
					"    new Label(this, SWT.RIGHT);",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_insertColumnRow() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 0, true, 0, true);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"  }",
					"}");
			// delete - should return in initial state
			newButton.delete();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_appendRow() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 0, false, 2, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_appendColumn() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 2, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 3;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_appendColumnRow() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, false, 1, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_insertColumnHorizontalSpan() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 2);",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, true, 1, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 3;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 3));",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('222');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_insertRowVerticalSpan() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1);",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, false, 1, true);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 3, 1));",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('222');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	/**
	 * Test for parsing "not balanced" {@link TableWrapLayoutInfo} and adding into <code>null</code>
	 * cell.
	 */
	@Test
	public void test_CREATE_notBalanced() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		shell.refresh();
		//
		ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
		layout.command_CREATE(newButton, 1, false, 1, false);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.NONE);",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('New Button');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE special cases
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_Shell_open() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      shell.setLayout(layout);",
						"    }",
						"    shell.open();",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, false, 0, false);
			assertEditor(
					"public class Test {",
					"  public static void main(String[] args) {",
					"    Shell shell = new Shell();",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      shell.setLayout(layout);",
					"    }",
					"    new Label(shell, SWT.NONE);",
					"    {",
					"      Button button = new Button(shell, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    shell.open();",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_Shell_layout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test {",
						"  public static void main(String[] args) {",
						"    Shell shell = new Shell();",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      shell.setLayout(layout);",
						"    }",
						"    shell.layout();",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 1, false, 0, false);
			assertEditor(
					"public class Test {",
					"  public static void main(String[] args) {",
					"    Shell shell = new Shell();",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      shell.setLayout(layout);",
					"    }",
					"    new Label(shell, SWT.NONE);",
					"    {",
					"      Button button = new Button(shell, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"    shell.layout();",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dimension operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_columnAccess() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('0 x 0');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('0 x 1');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('1 x 1');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		final TableWrapColumnInfo<?> column = layout.getColumns().get(0);
		// check initial values
		assertEquals(0, column.getIndex());
		assertEquals("left", column.getTitle());
		assertFalse(column.getGrab());
		assertEquals(TableWrapData.LEFT, column.getAlignment().intValue());
		// flip grab
		column.flipGrab();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
				"        tableWrapData.grabHorizontal = true;",
				"        button.setLayoutData(tableWrapData);",
				"      }",
				"      button.setText('0 x 0');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
				"        tableWrapData.grabHorizontal = true;",
				"        button.setLayoutData(tableWrapData);",
				"      }",
				"      button.setText('0 x 1');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('1 x 1');",
				"    }",
				"  }",
				"}");
		assertEquals("left, grab", column.getTitle());
		// set alignment
		ExecutionUtils.run(shell, new RunnableEx() {
			@Override
			public void run() throws Exception {
				column.setAlignment(TableWrapData.FILL);
			}
		});
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));",
				"      button.setText('0 x 0');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));",
				"      button.setText('0 x 1');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('1 x 1');",
				"    }",
				"  }",
				"}");
		assertEquals("fill, grab", column.getTitle());
		// set different alignment for "0 x 1" button
		{
			ControlInfo button = shell.getChildrenControls().get(2);
			layout.getTableWrapData(button).setHorizontalAlignment(TableWrapData.RIGHT);
			assertNull(column.getAlignment());
		}
		// check other alignments
		{
			column.setAlignment(TableWrapData.CENTER);
			assertEquals("center, grab", column.getTitle());
			//
			column.setAlignment(TableWrapData.RIGHT);
			assertEquals("right, grab", column.getTitle());
		}
		// delete
		column.delete();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('1 x 1');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_rowAccess() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('0 x 0');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('0 x 1');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('1 x 1');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		final TableWrapRowInfo<?> row = layout.getRows().get(1);
		// check initial values
		assertEquals(1, row.getIndex());
		assertEquals("top", row.getTitle());
		assertFalse(row.getGrab());
		assertEquals(TableWrapData.TOP, row.getAlignment().intValue());
		// flip grab
		row.flipGrab();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('0 x 0');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
				"        tableWrapData.grabVertical = true;",
				"        button.setLayoutData(tableWrapData);",
				"      }",
				"      button.setText('0 x 1');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
				"        tableWrapData.grabVertical = true;",
				"        button.setLayoutData(tableWrapData);",
				"      }",
				"      button.setText('1 x 1');",
				"    }",
				"  }",
				"}");
		assertEquals("top, grab", row.getTitle());
		// set alignment
		ExecutionUtils.run(shell, new RunnableEx() {
			@Override
			public void run() throws Exception {
				row.setAlignment(TableWrapData.FILL);
			}
		});
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('0 x 0');",
				"    }",
				"    new Label(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL_GRAB, 1, 1));",
				"      button.setText('0 x 1');",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL_GRAB, 1, 1));",
				"      button.setText('1 x 1');",
				"    }",
				"  }",
				"}");
		assertEquals("fill, grab", row.getTitle());
		// set different alignment for "0 x 1" button
		{
			ControlInfo button = shell.getChildrenControls().get(2);
			layout.getTableWrapData(button).setVerticalAlignment(TableWrapData.BOTTOM);
			assertNull(row.getAlignment());
		}
		// check other alignments
		{
			row.setAlignment(TableWrapData.TOP);
			assertEquals("top, grab", row.getTitle());
			//
			row.setAlignment(TableWrapData.MIDDLE);
			assertEquals("middle, grab", row.getTitle());
			//
			row.setAlignment(TableWrapData.BOTTOM);
			assertEquals("bottom, grab", row.getTitle());
		}
		// delete
		row.delete();
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setText('0 x 0');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteColumn() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 3;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData();",
						"        tableWrapData.colspan = 3;",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('New Button');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				shell.startEdit();
				layout.command_deleteColumn(1, true);
			} finally {
				shell.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      {",
					"        TableWrapData tableWrapData = new TableWrapData();",
					"        tableWrapData.colspan = 2;",
					"        button.setLayoutData(tableWrapData);",
					"      }",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('222');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_deleteColumn_deleteAlsoEmptyRows() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				shell.startEdit();
				layout.command_deleteColumn(1, true);
			} finally {
				shell.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_deleteRow() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        TableWrapData tableWrapData = new TableWrapData();",
						"        tableWrapData.rowspan = 3;",
						"        button.setLayoutData(tableWrapData);",
						"      }",
						"      button.setText('000');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"      button_1.setText('New Button');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				shell.startEdit();
				layout.command_deleteRow(1, true);
			} finally {
				shell.endEdit();
			}
			//
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      {",
					"        TableWrapData tableWrapData = new TableWrapData();",
					"        tableWrapData.rowspan = 2;",
					"        button.setLayoutData(tableWrapData);",
					"      }",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('222');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_deleteRow_deleteAlsoEmptyColumns() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				shell.startEdit();
				layout.command_deleteRow(1, true);
			} finally {
				shell.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE COLUMN
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE_COLUMN_before() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				layout.startEdit();
				layout.command_MOVE_COLUMN(1, 0);
			} finally {
				layout.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_MOVE_COLUMN_after() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				layout.startEdit();
				layout.command_MOVE_COLUMN(0, 2);
			} finally {
				layout.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE ROW
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE_ROW_before() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				layout.startEdit();
				layout.command_MOVE_ROW(1, 0);
			} finally {
				layout.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_MOVE_ROW_after() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			try {
				layout.startEdit();
				layout.command_MOVE_ROW(0, 2);
			} finally {
				layout.endEdit();
			}
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('000');",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('111');",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setText('222');",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(2);
		//
		shell.refresh();
		try {
			layout.command_MOVE(button, 1, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('000');",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('111');",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('222');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_MOVE_out() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayout(new RowLayout());",
						"    }",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
		RowLayoutInfo layout = (RowLayoutInfo) composite.getLayout();
		ControlInfo button = shell.getChildrenControls().get(1);
		//
		shell.refresh();
		try {
			layout.command_MOVE(button, null);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Composite composite = new Composite(this, SWT.NONE);",
					"      composite.setLayout(new RowLayout());",
					"      {",
					"        Button button = new Button(composite, SWT.NONE);",
					"      }",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_MOVE_error_1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 4;",
						"      setLayout(layout);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(7);
		//
		shell.refresh();
		try {
			layout.command_MOVE(button, 1, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    new Label(this, SWT.NONE);",
					"    Button button = new Button(this, SWT.NONE);",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_MOVE_error_2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 3;",
						"      setLayout(layout);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(5);
		//
		shell.refresh();
		try {
			layout.command_MOVE(button, 0, false, 0, false);
			layout.getGridInfo();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    Button button = new Button(this, SWT.NONE);",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADD
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ADD() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"      composite.setLayout(new RowLayout());",
						"      {",
						"        Button button = new Button(composite, SWT.NONE);",
						"      }",
						"    }",
						"  }",
						"}");
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		ControlInfo button = composite.getChildrenControls().get(0);
		//
		shell.refresh();
		try {
			layout.command_ADD(button, 0, false, 1, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Composite composite = new Composite(this, SWT.NONE);",
					"      composite.setLayout(new RowLayout());",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Special cases
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_noReference() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ControlInfo newButton = BTestUtils.createControl("org.eclipse.swt.widgets.Button");
			layout.command_CREATE(newButton, 0, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setText('New Button');",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_CREATE_viewer() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		try {
			ViewerInfo viewer = ViewerTest.createTableViewer(m_lastEditor);
			TableInfo table = (TableInfo) JavaInfoUtils.getWrapped(viewer);
			//
			layout.command_CREATE(table, 0, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
					"      Table table = tableViewer.getTable();",
					"      table.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 1));",
					"    }",
					"  }",
					"}");
		} finally {
			shell.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete layout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that when delete {@link TableWrapLayoutInfo}, fillers are also removed, because there are
	 * not controls that user wants.
	 */
	@Test
	public void test_DELETE_removeFillers() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Button(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		shell.refresh();
		// initially 2 controls - filler and Button
		assertEquals(2, shell.getChildrenControls().size());
		// after delete - only Button
		layout.delete();
		assertEquals(1, shell.getChildrenControls().size());
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    new Button(this, SWT.NONE);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Switching layouts
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test switching layouts from {@link GridLayout} to {@link TableWrapLayout}, and restore
	 * component positions & alignments.
	 */
	@Test
	public void test_Switching_fromGridLayout() throws Exception {
		CompositeInfo composite =
				parseComposite(
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
		composite.refresh();
		// set TableWrapLayout
		TableWrapLayoutInfo tableWrapLayout =
				(TableWrapLayoutInfo) BTestUtils.createLayout("org.eclipse.ui.forms.widgets.TableWrapLayout");
		composite.setLayout(tableWrapLayout);
		assertEditor(
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
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the <code>int</code> value of field with given name.
	 */
	private static int getInt(TableWrapDataInfo layoutData, String fieldName) throws Exception {
		return ReflectionUtils.getFieldInt(layoutData, fieldName);
	}
}