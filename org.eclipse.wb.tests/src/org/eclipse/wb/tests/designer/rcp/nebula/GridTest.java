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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.rcp.nebula.grid.GridColumnGroupInfo;
import org.eclipse.wb.internal.rcp.nebula.grid.GridColumnInfo;
import org.eclipse.wb.internal.rcp.nebula.grid.GridInfo;
import org.eclipse.wb.internal.rcp.nebula.grid.GridItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;

import net.miginfocom.layout.Grid;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test for {@link Grid} items models.
 *
 * @author sablin_aa
 */
public class GridTest extends AbstractNebulaTest {
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
	 * General test {@link GridColumnInfo} & {@link GridColumnGroupInfo} & {@link GridItemInfo}.
	 */
	@Test
	public void test_General() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.grid.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Grid grid = new Grid(this, SWT.NONE);",
						"    {",
						"      GridColumnGroup group = new GridColumnGroup(grid, SWT.NONE);",
						"      {",
						"        GridColumn column = new GridColumn(group, SWT.NONE);",
						"        column.setWidth(150);",
						"      }",
						"    }",
						"    {",
						"      GridItem item1 = new GridItem(grid, SWT.NONE);",
						"      {",
						"        GridItem item2 = new GridItem(item1, SWT.NONE);",
						"      }",
						"    }",
						"  }",
						"}");
		// refresh() also should be successful
		shell.refresh();
		// info
		GridInfo grid = (GridInfo) shell.getChildren(CompositeInfo.class).get(0);
		assertEquals(2, grid.getChildren().size());
		assertEquals(1, grid.getChildren(GridColumnGroupInfo.class).size());
		assertEquals(1, grid.getChildren(GridItemInfo.class).size());
		int headerHeight = grid.getWidget().getHeaderHeight();
		int groupHeaderHeight = grid.getWidget().getGroupHeaderHeight();
		// column group
		GridColumnGroupInfo group = grid.getChildren(GridColumnGroupInfo.class).get(0);
		{
			Rectangle bounds = group.getBounds();
			Assertions.assertThat(bounds.width).isEqualTo(150);
			Assertions.assertThat(bounds.height).isEqualTo(headerHeight);
		}
		assertEquals(1, group.getChildren(GridColumnInfo.class).size());
		// column
		{
			GridColumnInfo column = group.getChildren(GridColumnInfo.class).get(0);
			Rectangle bounds = column.getBounds();
			Assertions.assertThat(bounds.width).isEqualTo(150);
			Assertions.assertThat(bounds.height).isEqualTo(headerHeight - groupHeaderHeight);
		}
		// item
		GridItemInfo item = grid.getChildren(GridItemInfo.class).get(0);
		{
			Rectangle bounds = item.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(100);
			Assertions.assertThat(bounds.height).isLessThan(25); // collapsed
		}
		assertEquals(1, item.getChildren().size());
		// subitem
		{
			GridItemInfo subItem = item.getChildren(GridItemInfo.class).get(0);
			Rectangle bounds = subItem.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(100);
			Assertions.assertThat(bounds.height).isEqualTo(0); // parent item collapsed
		}
	}

	/**
	 * Test expanded {@link GridItemInfo}.
	 */
	@Test
	public void test_Expanded() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.grid.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Grid grid = new Grid(this, SWT.NONE);",
						"    {",
						"      GridColumnGroup group = new GridColumnGroup(grid, SWT.NONE);",
						"      {",
						"        GridColumn column = new GridColumn(group, SWT.NONE);",
						"        column.setWidth(150);",
						"      }",
						"    }",
						"    {",
						"      GridColumn column = new GridColumn(grid, SWT.NONE);",
						"      column.setWidth(100);",
						"    }",
						"    {",
						"      GridItem item1 = new GridItem(grid, SWT.NONE);",
						"      item1.setExpanded(true);",
						"      {",
						"        GridItem item2 = new GridItem(item1, SWT.NONE);",
						"      }",
						"    }",
						"  }",
						"}");
		// refresh() also should be successful
		shell.refresh();
		// info
		CompositeInfo grid = shell.getChildren(CompositeInfo.class).get(0);
		// item
		GridItemInfo item = grid.getChildren(GridItemInfo.class).get(0);
		{
			Rectangle bounds = item.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(100);
			Assertions.assertThat(bounds.height).isGreaterThan(30); // expanded
		}
		assertEquals(1, item.getChildren().size());
		// subitem
		{
			GridItemInfo subItem = item.getChildren(GridItemInfo.class).get(0);
			Rectangle bounds = subItem.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(100);
			Assertions.assertThat(bounds.height).isGreaterThan(15); // parent item expanded
		}
	}

	/**
	 * Test for {@link GridColumn} adding when exists {@link GridItem}'s (expression must be placed
	 * directly before first {@link GridItem}).
	 */
	@Test
	public void test_addColumn() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.grid.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Grid grid = new Grid(this, SWT.NONE);",
						"    {",
						"      GridColumnGroup group = new GridColumnGroup(grid, SWT.NONE);",
						"      {",
						"        GridColumn column = new GridColumn(group, SWT.NONE);",
						"        column.setWidth(150);",
						"      }",
						"    }",
						"    {",
						"      GridItem item1 = new GridItem(grid, SWT.NONE);",
						"      {",
						"        GridItem item2 = new GridItem(item1, SWT.NONE);",
						"      }",
						"    }",
						"  }",
						"}");
		// refresh() also should be successful
		shell.refresh();
		// info
		GridInfo grid = shell.getChildren(GridInfo.class).get(0);
		// create new column
		GridColumnInfo column =
				(GridColumnInfo) JavaInfoUtils.createJavaInfo(
						grid.getEditor(),
						"org.eclipse.nebula.widgets.grid.GridColumn",
						new ConstructorCreationSupport());
		JavaInfoUtils.add(column, null, grid, null);
		assertEditor(
				"import org.eclipse.nebula.widgets.grid.*;",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    Grid grid = new Grid(this, SWT.NONE);",
				"    {",
				"      GridColumnGroup group = new GridColumnGroup(grid, SWT.NONE);",
				"      {",
				"        GridColumn column = new GridColumn(group, SWT.NONE);",
				"        column.setWidth(150);",
				"      }",
				"    }",
				"    {",
				"      GridColumn gridColumn = new GridColumn(grid, SWT.NONE);",
				"      gridColumn.setText('New Column');",
				"      gridColumn.setWidth(150);",
				"    }",
				"    {",
				"      GridItem item1 = new GridItem(grid, SWT.NONE);",
				"      {",
				"        GridItem item2 = new GridItem(item1, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}
}