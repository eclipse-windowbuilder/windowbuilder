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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.internal.rcp.model.widgets.TableCursorInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TableInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for "big" SWT {@link Table}.
 *
 * @author scheglov_ke
 */
public class TableTest extends RcpModelTest {
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
	// TableCursor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for parsing with {@link TableCursorInfo}.
	 */
	@Test
	public void test_TableCursor_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Table table = new Table(this, SWT.BORDER);",
						"    {",
						"      TableCursor tableCursor = new TableCursor(table, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
		Assertions.assertThat(table.getChildren(TableCursorInfo.class)).hasSize(1);
		assertTrue(table.hasTableCursor());
	}

	/**
	 * Test for adding new {@link TableCursorInfo}.
	 */
	@Test
	public void test_TableCursor_new() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Table table = new Table(this, SWT.BORDER);",
						"  }",
						"}");
		shell.refresh();
		TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
		assertFalse(table.hasTableCursor());
		//
		TableCursorInfo tableCursor = createJavaInfo("org.eclipse.swt.custom.TableCursor");
		table.command_CREATE(tableCursor);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    Table table = new Table(this, SWT.BORDER);",
				"    {",
				"      TableCursor tableCursor = new TableCursor(table, SWT.NONE);",
				"    }",
				"  }",
				"}");
		assertTrue(table.hasTableCursor());
	}
}