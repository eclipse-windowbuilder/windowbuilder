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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link FormLayoutInfo} groups.
 *
 * @author scheglov_ke
 */
public class FormLayoutGroupsTest extends AbstractFormLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parsing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setColumnGroups(new int[][]{new int[]{1, 3}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_0 = columns.get(0);
			FormColumnInfo column_1 = columns.get(1);
			FormColumnInfo column_2 = columns.get(2);
			// check group
			{
				List<FormColumnInfo> group_0 = layout.getColumnGroup(column_0);
				assertNotNull(group_0);
				assertTrue(group_0.contains(column_0));
				assertTrue(group_0.contains(column_2));
				assertSame(group_0, layout.getColumnGroup(column_2));
				//
				assertEquals(0, layout.getDimensionGroupIndex(column_0));
				assertEquals(0, layout.getDimensionGroupIndex(column_2));
			}
			// no group for column_1
			{
				assertNull(layout.getColumnGroup(column_1));
				assertEquals(-1, layout.getDimensionGroupIndex(column_1));
			}
		} finally {
			panel.refresh_dispose();
		}
	}

	@Test
	public void test_removeFromGroup() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setColumnGroups(new int[][]{new int[]{1, 4}, new int[]{2, 3}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_1 = columns.get(1);
			//
			assertNotNull(layout.getColumnGroup(column_1));
			layout.deleteColumn(1);
			assertNull(layout.getColumnGroup(column_1));
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,});",
				"    setLayout(formLayout);",
				"    formLayout.setColumnGroups(new int[][]{new int[]{1, 3}});",
				"  }",
				"}");
	}

	@Test
	public void test_unGroupColumns() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setColumnGroups(new int[][]{new int[]{1, 2}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_0 = columns.get(0);
			FormColumnInfo column_1 = columns.get(1);
			// check existing groups
			assertNotNull(layout.getColumnGroup(column_0));
			assertNotNull(layout.getColumnGroup(column_1));
			// un-group
			{
				List<FormColumnInfo> columnsToUnGroup = new ArrayList<>();
				columnsToUnGroup.add(column_1);
				layout.unGroupColumns(columnsToUnGroup);
			}
			// not more groups
			assertNull(layout.getColumnGroup(column_0));
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,});",
				"    setLayout(formLayout);",
				"  }",
				"}");
	}

	@Test
	public void test_groupNew() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_0 = columns.get(0);
			FormColumnInfo column_1 = columns.get(1);
			FormColumnInfo column_2 = columns.get(2);
			FormColumnInfo column_3 = columns.get(3);
			FormColumnInfo column_4 = columns.get(4);
			// group 0 and 3
			{
				List<FormColumnInfo> columnsToGroup = new ArrayList<>();
				columnsToGroup.add(column_0);
				columnsToGroup.add(column_3);
				layout.groupColumns(columnsToGroup);
			}
			// group 2 and 4 and (ignored) gap
			{
				List<FormColumnInfo> columnsToGroup = new ArrayList<>();
				columnsToGroup.add(column_2);
				columnsToGroup.add(column_4);
				columnsToGroup.add(column_1);
				layout.groupColumns(columnsToGroup);
			}
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.RELATED_GAP_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,});",
				"    formLayout.setColumnGroups(new int[][]{new int[]{1, 4}, new int[]{3, 5}});",
				"    setLayout(formLayout);",
				"  }",
				"}");
	}

	@Test
	public void test_ignoreOne() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		String expectedSource = m_lastEditor.getSource();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_0 = columns.get(0);
			// try to group single column, ignored
			{
				List<FormColumnInfo> columnsToGroup = new ArrayList<>();
				columnsToGroup.add(column_0);
				layout.groupColumns(columnsToGroup);
			}
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(expectedSource, m_lastEditor);
	}

	@Test
	public void test_groupWithExisting() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setColumnGroups(new int[][]{new int[]{1, 2}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_0 = columns.get(0);
			FormColumnInfo column_1 = columns.get(1);
			FormColumnInfo column_2 = columns.get(2);
			// we have existing group for column_0
			assertNotNull(layout.getColumnGroup(column_0));
			assertSame(layout.getColumnGroup(column_0), layout.getColumnGroup(column_1));
			// group
			{
				List<FormColumnInfo> columnsToGroup = new ArrayList<>();
				columnsToGroup.add(column_0);
				columnsToGroup.add(column_2);
				layout.groupColumns(columnsToGroup);
			}
			// column_2 should be in same group as column_0 and column_1
			assertSame(layout.getColumnGroup(column_0), layout.getColumnGroup(column_1));
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,});",
				"    setLayout(formLayout);",
				"    formLayout.setColumnGroups(new int[][]{new int[]{1, 2, 3}});",
				"  }",
				"}");
	}

	@Test
	public void test_groupIgnoreTwoGroups() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setColumnGroups(new int[][]{new int[]{1, 2}, new int[]{3, 4}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		String expectedSource = m_lastEditor.getSource();
		//
		panel.refresh();
		try {
			List<FormColumnInfo> columns = layout.getColumns();
			FormColumnInfo column_2 = columns.get(0);
			FormColumnInfo column_3 = columns.get(3);
			// check existing groups
			assertNotNull(layout.getColumnGroup(column_2));
			assertNotNull(layout.getColumnGroup(column_3));
			assertNotSame(layout.getColumnGroup(column_2), layout.getColumnGroup(column_3));
			// try to group column_2 and column_3 from different groups, ignored
			{
				List<FormColumnInfo> columnsToGroup = new ArrayList<>();
				columnsToGroup.add(column_2);
				columnsToGroup.add(column_3);
				layout.groupColumns(columnsToGroup);
			}
			// still different groups
			assertNotSame(layout.getColumnGroup(column_2), layout.getColumnGroup(column_3));
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(expectedSource, m_lastEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rows
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_groupRows() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setRowGroups(new int[][]{new int[]{1, 2}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormRowInfo> rows = layout.getRows();
			FormRowInfo row_0 = rows.get(0);
			FormRowInfo row_1 = rows.get(1);
			FormRowInfo row_2 = rows.get(2);
			// we have existing group for row_0
			assertNotNull(layout.getRowGroup(row_0));
			assertSame(layout.getRowGroup(row_0), layout.getRowGroup(row_1));
			// group
			{
				List<FormRowInfo> rowsToGroup = new ArrayList<>();
				rowsToGroup.add(row_0);
				rowsToGroup.add(row_2);
				layout.groupRows(rowsToGroup);
			}
			// row_2 should be in same group as row_0 and row_1
			assertSame(layout.getRowGroup(row_0), layout.getRowGroup(row_1));
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,",
				"        FormSpecs.DEFAULT_ROWSPEC,",
				"        FormSpecs.DEFAULT_ROWSPEC,});",
				"    setLayout(formLayout);",
				"    formLayout.setRowGroups(new int[][]{new int[]{1, 2, 3}});",
				"  }",
				"}");
	}

	@Test
	public void test_unGroupRows() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,});",
						"    setLayout(formLayout);",
						"    formLayout.setRowGroups(new int[][]{new int[]{1, 2}});",
						"  }",
						"}");
		FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			List<FormRowInfo> rows = layout.getRows();
			FormRowInfo row_0 = rows.get(0);
			FormRowInfo row_1 = rows.get(1);
			// check existing groups
			assertNotNull(layout.getRowGroup(row_0));
			assertNotNull(layout.getRowGroup(row_1));
			assertEquals(0, layout.getDimensionGroupIndex(row_0));
			assertEquals(0, layout.getDimensionGroupIndex(row_1));
			// un-group
			{
				List<FormRowInfo> rowsToUnGroup = new ArrayList<>();
				rowsToUnGroup.add(row_1);
				layout.unGroupRows(rowsToUnGroup);
			}
			// no more groups
			assertNull(layout.getRowGroup(row_0));
			assertEquals(-1, layout.getDimensionGroupIndex(row_0));
		} finally {
			panel.refresh_dispose();
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    FormLayout formLayout = new FormLayout(new ColumnSpec[] {",
				"        FormSpecs.DEFAULT_COLSPEC,},",
				"      new RowSpec[] {",
				"        FormSpecs.DEFAULT_ROWSPEC,",
				"        FormSpecs.DEFAULT_ROWSPEC,});",
				"    setLayout(formLayout);",
				"  }",
				"}");
	}
}
