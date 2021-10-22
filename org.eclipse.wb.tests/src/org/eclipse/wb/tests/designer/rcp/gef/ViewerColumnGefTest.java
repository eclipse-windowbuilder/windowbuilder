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
package org.eclipse.wb.tests.designer.rcp.gef;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Tests for changing layout and GEF.
 *
 * @author scheglov_ke
 */
public class ViewerColumnGefTest extends RcpGefTest {
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
  public void test_dropOnTable() throws Exception {
    CompositeInfo composite =
        openComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new FillLayout());",
            "    {",
            "      Table table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);",
            "      table.setHeaderVisible(true);",
            "    }",
            "  }",
            "}");
    String source = m_lastEditor.getSource();
    TableInfo table = composite.getChildren(TableInfo.class).get(0);
    {
      // drop ViewerColumn
      loadCreationTool("org.eclipse.jface.viewers.TableViewerColumn");
      canvas.moveTo(table);
      waitEventLoop(0);
      canvas.assertCommandNull();
      canvas.click();
      // column-viewer not dropped
      assertEditor(source, m_lastEditor);
    }
    {
      // drop Column
      loadCreationTool("org.eclipse.swt.widgets.TableColumn");
      canvas.moveTo(table);
      waitEventLoop(0);
      canvas.assertCommandNotNull();
      canvas.click();
      // column dropped
      assertEditor(
          "public class Test extends Composite {",
          "  public Test(Composite parent, int style) {",
          "    super(parent, style);",
          "    setLayout(new FillLayout());",
          "    {",
          "      Table table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);",
          "      table.setHeaderVisible(true);",
          "      {",
          "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
          "        tableColumn.setWidth(100);",
          "        tableColumn.setText(\"New Column\");",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_dropOnTableViewer() throws Exception {
    CompositeInfo composite =
        openComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new FillLayout());",
            "    {",
            "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
            "      Table table = tableViewer.getTable();",
            "      table.setHeaderVisible(true);",
            "    }",
            "  }",
            "}");
    TableInfo table = composite.getChildren(TableInfo.class).get(0);
    {
      // drop Column
      loadCreationTool("org.eclipse.swt.widgets.TableColumn");
      canvas.moveTo(table);
      waitEventLoop(0);
      canvas.assertCommandNotNull();
      canvas.click();
      // column dropped
      assertEditor(
          "public class Test extends Composite {",
          "  public Test(Composite parent, int style) {",
          "    super(parent, style);",
          "    setLayout(new FillLayout());",
          "    {",
          "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
          "      Table table = tableViewer.getTable();",
          "      table.setHeaderVisible(true);",
          "      {",
          "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
          "        tableColumn.setWidth(100);",
          "        tableColumn.setText(\"New Column\");",
          "      }",
          "    }",
          "  }",
          "}");
    }
    {
      // drop ViewerColumn
      loadCreationTool("org.eclipse.jface.viewers.TableViewerColumn");
      canvas.moveTo(table);
      waitEventLoop(0);
      canvas.assertCommandNotNull();
      canvas.click();
      // column-viewer  dropped
      assertEditor(
          "public class Test extends Composite {",
          "  public Test(Composite parent, int style) {",
          "    super(parent, style);",
          "    setLayout(new FillLayout());",
          "    {",
          "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER);",
          "      Table table = tableViewer.getTable();",
          "      table.setHeaderVisible(true);",
          "      {",
          "        TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
          "        TableColumn tableColumn = tableViewerColumn.getColumn();",
          "        tableColumn.setWidth(100);",
          "        tableColumn.setText(\"New Column\");",
          "      }",
          "      {",
          "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
          "        tableColumn.setWidth(100);",
          "        tableColumn.setText(\"New Column\");",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }
}
