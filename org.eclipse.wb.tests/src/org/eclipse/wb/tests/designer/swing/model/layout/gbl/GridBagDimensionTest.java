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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.junit.Test;

import java.awt.GridBagLayout;
import java.util.List;

/**
 * Test for {@link DimensionInfo}, {@link ColumnInfo} and {@link RowInfo}.
 *
 * @author scheglov_ke
 */
public class GridBagDimensionTest extends AbstractGridBagLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GridBagLayoutInfo#getColumns()} and {@link GridBagLayoutInfo#getRows()}.
	 */
	@Test
	public void test_dimensions_0() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton('button 0 0');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(button, gbc);",
						"    }",
						"    {",
						"      JButton button = new JButton('button 1 0');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 0;",
						"      add(button, gbc);",
						"    }",
						"    {",
						"      JButton button = new JButton('button 1 1');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button, gbc);",
						"    }",
						"    {",
						"      JButton button = new JButton('button 1 2');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 2;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		panel.refresh();
		// columns
		{
			List<ColumnInfo> columns = layout.getColumns();
			assertEquals(2, columns.size());
			{
				ColumnInfo column = columns.get(0);
				assertEquals(0, column.getIndex());
				assertEquals(0, column.getSize());
				assertEquals(0.0, column.getWeight(), 1.0E-6);
				assertFalse(column.hasWeight());
			}
			{
				ColumnInfo column = columns.get(1);
				assertEquals(1, column.getIndex());
				assertEquals(0, column.getSize());
				assertEquals(0.0, column.getWeight(), 1.0E-6);
				assertFalse(column.hasWeight());
			}
		}
		// rows
		{
			List<RowInfo> rows = layout.getRows();
			assertEquals(3, rows.size());
			{
				RowInfo row = rows.get(0);
				assertEquals(0, row.getIndex());
				assertEquals(0, row.getSize());
				assertEquals(0.0, row.getWeight(), 1.0E-6);
				assertFalse(row.hasWeight());
			}
		}
	}

	/**
	 * When {@link GridBagLayout#columnWidths} or {@link GridBagLayout#rowHeights} have more elements
	 * than number of columns/rows used by components, total number of columns/rows is also more.
	 */
	@Test
	public void test_dimensions_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {100, 200, 0};",
						"    layout.rowHeights = new int[] {50, 0};",
						"    layout.columnWeights = new double[] {1.0, 2.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {1.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"  }",
						"}");
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		panel.refresh();
		// columns
		{
			List<ColumnInfo> columns = layout.getColumns();
			assertEquals(2, columns.size());
			{
				ColumnInfo column = columns.get(0);
				assertEquals(0, column.getIndex());
				assertEquals(100, column.getSize());
				assertEquals(1.0, column.getWeight(), 1.0E-6);
				assertTrue(column.hasWeight());
			}
			{
				ColumnInfo column = columns.get(1);
				assertEquals(1, column.getIndex());
				assertEquals(200, column.getSize());
				assertEquals(2.0, column.getWeight(), 1.0E-6);
				assertTrue(column.hasWeight());
			}
		}
		// rows
		{
			List<RowInfo> rows = layout.getRows();
			assertEquals(1, rows.size());
			{
				RowInfo row = rows.get(0);
				assertEquals(0, row.getIndex());
				assertEquals(50, row.getSize());
				assertEquals(1.0, row.getWeight(), 1.0E-6);
				assertTrue(row.hasWeight());
			}
		}
	}
}
