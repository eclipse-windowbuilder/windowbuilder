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
package org.eclipse.wb.tests.designer.swing.model.component;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JTableInfo;
import org.eclipse.wb.internal.swing.model.property.editor.models.table.TableColumnDescription;
import org.eclipse.wb.internal.swing.model.property.editor.models.table.TableModelDescription;
import org.eclipse.wb.internal.swing.model.property.editor.models.table.TableModelPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Tests for {@link JTable} support, more specifically {@link DefaultTableModel}.
 * 
 * @author scheglov_ke
 */
public class JTableTest extends SwingModelTest {
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
   * We should ignore {@link TableModel} from inner class.
   */
  public void test_evaluate_innerTableModel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable(new MyTableModel());",
            "    add(table);",
            "  }",
            "  private class MyTableModel extends DefaultTableModel {",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    JTable tableObject = (JTable) tableInfo.getObject();
    // validate model, we used "null", so JTable installed DefaultTableModel
    {
      TableModel model = tableObject.getModel();
      assertThat(model).isInstanceOf(DefaultTableModel.class);
    }
  }

  public void test_evaluate_valuesAndColumns() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        new Object[]{",
            "          '00', '01', '02'",
            "        },",
            "        new Object[]{",
            "          '10', '11', '12'",
            "        },",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    JTable tableObject = (JTable) tableInfo.getObject();
    // validate model
    {
      TableModel model = tableObject.getModel();
      assertNotNull(model);
      assertEquals(3, model.getColumnCount());
      assertEquals(2, model.getRowCount());
      // values
      assertEquals("00", model.getValueAt(0, 0));
      assertEquals("01", model.getValueAt(0, 1));
      assertEquals("02", model.getValueAt(0, 2));
      assertEquals("10", model.getValueAt(1, 0));
      assertEquals("11", model.getValueAt(1, 1));
      assertEquals("12", model.getValueAt(1, 2));
      // columns
      assertEquals("A", model.getColumnName(0));
      assertEquals("B", model.getColumnName(1));
      assertEquals("C", model.getColumnName(2));
    }
    // validate text from editor
    {
      Property property = tableInfo.getPropertyByTitle("model");
      assertEquals("3 columns, 2 rows", getPropertyText(property));
      assertThat(property.getEditor()).isInstanceOf(TableModelPropertyEditor.class);
    }
  }

  public void test_evaluate_anonymous_noColumnClass() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        new Object[]{",
            "          '00', '01', '02'",
            "        },",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C'",
            "      }",
            "    ) {",
            "    });",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    JTable tableObject = (JTable) tableInfo.getObject();
    // validate model
    {
      TableModel model = tableObject.getModel();
      assertNotNull(model);
      assertEquals(3, model.getColumnCount());
      assertEquals(1, model.getRowCount());
      // values
      assertEquals("00", model.getValueAt(0, 0));
      assertEquals("01", model.getValueAt(0, 1));
      assertEquals("02", model.getValueAt(0, 2));
      // columns
      assertEquals("A", model.getColumnName(0));
      assertEquals("B", model.getColumnName(1));
      assertEquals("C", model.getColumnName(2));
      // column class
      assertSame(Object.class, model.getColumnClass(0));
      assertSame(Object.class, model.getColumnClass(1));
      assertSame(Object.class, model.getColumnClass(2));
    }
  }

  public void test_evaluate_anonymous_withColumnClass() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        new Object[]{",
            "          'str', Boolean.TRUE, 'obj', Integer.valueOf(5)",
            "        },",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C', 'D'",
            "      }",
            "    ) {",
            "      Class[] columnTypes = new Class[] {",
            "        String.class, Boolean.class, Object.class, Integer.class",
            "      };",
            "      public Class getColumnClass(int columnIndex) {",
            "        return columnTypes[columnIndex];",
            "      }",
            "    });",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    JTable tableObject = (JTable) tableInfo.getObject();
    // validate model
    {
      TableModel model = tableObject.getModel();
      assertNotNull(model);
      assertEquals(4, model.getColumnCount());
      assertEquals(1, model.getRowCount());
      // values
      assertEquals("str", model.getValueAt(0, 0));
      assertSame(Boolean.TRUE, model.getValueAt(0, 1));
      assertEquals("obj", model.getValueAt(0, 2));
      assertEquals(Integer.valueOf(5), model.getValueAt(0, 3));
      // columns
      assertEquals("A", model.getColumnName(0));
      assertEquals("B", model.getColumnName(1));
      assertEquals("C", model.getColumnName(2));
      assertEquals("D", model.getColumnName(3));
      // column class
      assertSame(String.class, model.getColumnClass(0));
      assertSame(Boolean.class, model.getColumnClass(1));
      assertSame(Object.class, model.getColumnClass(2));
      assertSame(Integer.class, model.getColumnClass(3));
      // column editable
      assertEquals(true, model.isCellEditable(0, 0));
      assertEquals(true, model.isCellEditable(0, 1));
      assertEquals(true, model.isCellEditable(0, 2));
      assertEquals(true, model.isCellEditable(0, 3));
    }
  }

  public void test_evaluate_anonymous_withColumnEditable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        new Object[]{",
            "          '0', '1', '2'",
            "        },",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C'",
            "      }",
            "    ) {",
            "      boolean[] columnEditables = new boolean[] {",
            "        true, true, false",
            "      };",
            "      public boolean isCellEditable(int row, int column) {",
            "        return columnEditables[column];",
            "      }",
            "    });",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    JTable tableObject = (JTable) tableInfo.getObject();
    // validate model
    {
      TableModel model = tableObject.getModel();
      assertNotNull(model);
      assertEquals(3, model.getColumnCount());
      assertEquals(1, model.getRowCount());
      // values
      assertEquals("0", model.getValueAt(0, 0));
      assertEquals("1", model.getValueAt(0, 1));
      assertEquals("2", model.getValueAt(0, 2));
      // columns
      assertEquals("A", model.getColumnName(0));
      assertEquals("B", model.getColumnName(1));
      assertEquals("C", model.getColumnName(2));
      // column editable
      assertEquals(true, model.isCellEditable(0, 0));
      assertEquals(true, model.isCellEditable(0, 1));
      assertEquals(false, model.isCellEditable(0, 2));
    }
  }

  public void test_evaluate_getColumnModel_invocations() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        new Object[]{",
            "          '0', '1', '2'",
            "        },",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C'",
            "      }",
            "    ));",
            "    table.getColumnModel().getColumn(0).setPreferredWidth(200);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    JTable tableObject = (JTable) tableInfo.getObject();
    // validate TableColumnModel
    {
      assertEquals(200, tableObject.getColumnModel().getColumn(0).getPreferredWidth());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_editor_getText_noModel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // no model, so no text
    {
      Property property = tableInfo.getPropertyByTitle("model");
      assertEquals(null, getPropertyText(property));
      assertThat(property.getEditor()).isInstanceOf(TableModelPropertyEditor.class);
    }
  }

  public void test_editor_getText_someModel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01', '02'},",
            "        {'10', '11', '12'},",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C'",
            "      }",
            "    ) {",
            "    });",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // has model, check text
    {
      Property property = tableInfo.getPropertyByTitle("model");
      assertEquals("3 columns, 2 rows", getPropertyText(property));
      assertThat(property.getEditor()).isInstanceOf(TableModelPropertyEditor.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TableModelDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple case. String values and column names.
   */
  public void test_TableModelDescription_simple() throws Exception {
    JTable table;
    {
      Object[][] values = new Object[][]{{"00", "01"}, {"10", "11"}, {"20", "21"}};
      String[] columnNames = new String[]{"A", "B"};
      TableModel model = new DefaultTableModel(values, columnNames);
      table = new JTable(model);
    }
    // prepare TableModelDescription
    TableModelDescription modelDescription = new TableModelDescription(table);
    assertEquals(3, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // column: 0
    {
      TableColumnDescription column = modelDescription.getColumn(0);
      assertEquals("A", column.m_name);
      assertEquals(true, column.m_editable);
      assertEquals(true, column.m_resizable);
    }
    // column: 1
    {
      TableColumnDescription column = modelDescription.getColumn(1);
      assertEquals("B", column.m_name);
      assertEquals(true, column.m_editable);
      assertEquals(true, column.m_resizable);
    }
    // model source
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "    {'10', '11'},",
                "    {'20', '21'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    assertThat(modelDescription.getColumnModelInvocations()).isEmpty();
  }

  /**
   * Simple case. No column/values.
   */
  public void test_TableModelDescription_noValues() throws Exception {
    JTable table;
    {
      Object[][] values = new Object[][]{};
      String[] columnNames = new String[]{};
      TableModel model = new DefaultTableModel(values, columnNames);
      table = new JTable(model);
    }
    // prepare TableModelDescription
    TableModelDescription modelDescription = new TableModelDescription(table);
    assertEquals(0, modelDescription.getRowCount());
    assertEquals(0, modelDescription.getColumnCount());
    // model source
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "  },",
                "  new String[] {",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    assertThat(modelDescription.getColumnModelInvocations()).isEmpty();
  }

  /**
   * Test various types of values.
   */
  public void test_TableModelDescription_valueTypes() throws Exception {
    JTable table;
    {
      Object[][] values =
          new Object[][]{{
              "str",
              Boolean.TRUE,
              Boolean.FALSE,
              Integer.valueOf(8),
              Byte.valueOf((byte) 8),
              Short.valueOf((short) 8),
              Long.valueOf(8),
              Float.valueOf(8.1f),
              Double.valueOf(8.2d),
              new Object()}};
      String[] columnNames =
          new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"};
      TableModel model = new DefaultTableModel(values, columnNames);
      table = new JTable(model);
    }
    // prepare TableModelDescription
    TableModelDescription modelDescription = new TableModelDescription(table);
    assertEquals(1, modelDescription.getRowCount());
    assertEquals(10, modelDescription.getColumnCount());
    // model source
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'str', Boolean.TRUE, Boolean.FALSE, new Integer(8), "
                    + "new Byte((byte) 8), new Short((short) 8), new Long(8L), "
                    + "new Float(8.1f), new Double(8.2), null"
                    + "},",
                "  },",
                "  new String[] {",
                "    '00', '01', '02', '03', '04', '05', '06', '07', '08', '09'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    assertThat(modelDescription.getColumnModelInvocations()).isEmpty();
  }

  /**
   * Test for properties of column in {@link TableColumnModel}.
   */
  public void test_TableModelDescription_columnProperties() throws Exception {
    JTable table;
    {
      Object[][] values = new Object[][]{{"00", "01"}};
      String[] columnNames = new String[]{"A", "B"};
      TableModel model = new DefaultTableModel(values, columnNames) {
        private static final long serialVersionUID = 0L;

        @Override
        public boolean isCellEditable(int row, int column) {
          if (column == 0) {
            return false;
          }
          return super.isCellEditable(row, column);
        }
      };
      table = new JTable(model);
      // column: 0
      {
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(50);
        column.setPreferredWidth(100);
        column.setMaxWidth(200);
      }
    }
    // prepare TableModelDescription
    TableModelDescription modelDescription = new TableModelDescription(table);
    assertEquals(1, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // column: 0
    {
      TableColumnDescription column = modelDescription.getColumn(0);
      assertEquals("A", column.m_name);
      assertEquals(false, column.m_editable);
      assertEquals(false, column.m_resizable);
      assertEquals(100, column.m_preferredWidth);
      assertEquals(50, column.m_minWidth);
      assertEquals(200, column.m_maxWidth);
    }
    // column: 1
    {
      TableColumnDescription column = modelDescription.getColumn(1);
      assertEquals("B", column.m_name);
      assertEquals(true, column.m_editable);
      assertEquals(true, column.m_resizable);
      assertEquals(75, column.m_preferredWidth);
      assertEquals(15, column.m_minWidth);
      assertEquals(Integer.MAX_VALUE, column.m_maxWidth);
    }
    // model source
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ") {",
                "  boolean[] columnEditables = new boolean[] {",
                "    false, true",
                "  };",
                "  public boolean isCellEditable(int row, int column) {",
                "    return columnEditables[column];",
                "  }",
                "}"}).trim(),
        modelDescription.getModelSource());
    {
      List<String> invocations = modelDescription.getColumnModelInvocations();
      assertThat(invocations).isEqualTo(
          ImmutableList.of(
              "getColumnModel().getColumn(0).setResizable(false)",
              "getColumnModel().getColumn(0).setPreferredWidth(100)",
              "getColumnModel().getColumn(0).setMinWidth(50)",
              "getColumnModel().getColumn(0).setMaxWidth(200)"));
    }
  }

  /**
   * Test for type of column in {@link TableColumnModel}.
   */
  public void test_TableModelDescription_getColumnClass() throws Exception {
    JTable table;
    {
      Object[][] values = new Object[][]{{}};
      String[] columnNames = new String[]{"A", "B", "C", "D"};
      TableModel model = new DefaultTableModel(values, columnNames) {
        private static final long serialVersionUID = 0L;
        Class<?>[] columnTypes = new Class[]{
            String.class,
            Boolean.class,
            Object.class,
            Integer.class};

        @Override
        public Class<?> getColumnClass(int columnIndex) {
          return columnTypes[columnIndex];
        }
      };
      table = new JTable(model);
    }
    // prepare TableModelDescription
    TableModelDescription modelDescription = new TableModelDescription(table);
    assertEquals(1, modelDescription.getRowCount());
    assertEquals(4, modelDescription.getColumnCount());
    // column class
    assertSame(String.class, modelDescription.getColumn(0).m_class);
    assertSame(Boolean.class, modelDescription.getColumn(1).m_class);
    assertSame(Object.class, modelDescription.getColumn(2).m_class);
    assertSame(Integer.class, modelDescription.getColumn(3).m_class);
    // model source
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {null, null, null, null},",
                "  },",
                "  new String[] {",
                "    'A', 'B', 'C', 'D'",
                "  }",
                ") {",
                "  Class[] columnTypes = new Class[] {",
                "    java.lang.String.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Integer.class",
                "  };",
                "  public Class getColumnClass(int columnIndex) {",
                "    return columnTypes[columnIndex];",
                "  }",
                "}"}).trim(),
        modelDescription.getModelSource());
    {
      List<String> invocations = modelDescription.getColumnModelInvocations();
      assertThat(invocations).isEmpty();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations: columns
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TableModelDescription#insertColumn(int)}.
   */
  public void test_TableModelDescription_insertColumn() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01'},",
            "        {'10', '11'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // do operation
    modelDescription.insertColumn(1);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(3, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', null, '01'},",
                "    {'10', null, '11'},",
                "  },",
                "  new String[] {",
                "    'A', 'New column', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    isNewColumn(modelDescription, 1);
  }

  /**
   * Test for {@link TableModelDescription#removeColumn(int)}.
   */
  public void test_TableModelDescription_removeColumn() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01', '02'},",
            "        {'10', '11', '12'},",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(3, modelDescription.getColumnCount());
    // do operation
    modelDescription.removeColumn(1);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '02'},",
                "    {'10', '12'},",
                "  },",
                "  new String[] {",
                "    'A', 'C'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }

  /**
   * Test for {@link TableModelDescription#setColumnCount(int)}.
   */
  public void test_TableModelDescription_setColumnCount() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01'},",
            "        {'10', '11'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // set columns: 4
    modelDescription.setColumnCount(4);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(4, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01', null, null},",
                "    {'10', '11', null, null},",
                "  },",
                "  new String[] {",
                "    'A', 'B', 'New column', 'New column'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    isNewColumn(modelDescription, 2);
    isNewColumn(modelDescription, 3);
    // set columns: 1
    modelDescription.setColumnCount(1);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(1, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00'},",
                "    {'10'},",
                "  },",
                "  new String[] {",
                "    'A'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }

  /**
   * Test for {@link TableModelDescription#moveColumn(int, int)}.
   */
  public void test_TableModelDescription_moveColumn() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01', '02', '03'},",
            "        {'10', '11', '12', '13'},",
            "      },",
            "      new String[]{",
            "        'A', 'B', 'C', 'D'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(4, modelDescription.getColumnCount());
    // move: backward
    modelDescription.moveColumn(2, 1);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(4, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '02', '01', '03'},",
                "    {'10', '12', '11', '13'},",
                "  },",
                "  new String[] {",
                "    'A', 'C', 'B', 'D'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    // move: forward
    modelDescription.moveColumn(1, 2);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(4, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01', '02', '03'},",
                "    {'10', '11', '12', '13'},",
                "  },",
                "  new String[] {",
                "    'A', 'B', 'C', 'D'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    // move: forward 2
    modelDescription.moveColumn(0, 3);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(4, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'01', '02', '03', '00'},",
                "    {'11', '12', '13', '10'},",
                "  },",
                "  new String[] {",
                "    'B', 'C', 'D', 'A'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }

  private static void isNewColumn(TableModelDescription modelDescription, int index) {
    TableColumnDescription column = modelDescription.getColumn(index);
    assertEquals("New column", column.m_name);
    assertEquals(true, column.m_editable);
    assertEquals(true, column.m_resizable);
    assertEquals(TableColumnDescription.DEFAULT_PREFERRED_WIDTH, column.m_preferredWidth);
    assertEquals(TableColumnDescription.DEFAULT_MIN_WIDTH, column.m_minWidth);
    assertEquals(TableColumnDescription.DEFAULT_MAX_WIDTH, column.m_maxWidth);
  }

  /**
   * Test for {@link TableModelDescription#setColumnType(int, Class)}.
   */
  public void test_TableModelDescription_setColumnType() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', Boolean.TRUE},",
            "        {'10', '11'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // do operation
    modelDescription.setColumnType(1, Boolean.class);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', Boolean.TRUE},",
                "    {'10', null},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ") {",
                "  Class[] columnTypes = new Class[] {",
                "    java.lang.Object.class, java.lang.Boolean.class",
                "  };",
                "  public Class getColumnClass(int columnIndex) {",
                "    return columnTypes[columnIndex];",
                "  }",
                "}"}).trim(),
        modelDescription.getModelSource());
    assertThat(modelDescription.getColumnModelInvocations()).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations: rows
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TableModelDescription#insertRow(int)}.
   */
  public void test_TableModelDescription_insertRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01'},",
            "        {'10', '11'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // do operation
    modelDescription.insertRow(1);
    assertEquals(3, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "    {'10', '11'},",
                "    {null, null},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }

  /**
   * Test for {@link TableModelDescription#removeRow(int)}.
   */
  public void test_TableModelDescription_removeRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01'},",
            "        {'10', '11'},",
            "        {'20', '21'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(3, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // do operation
    modelDescription.removeRow(1);
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "    {'20', '21'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }

  /**
   * Test for {@link TableModelDescription#setRowCount(int)}.
   */
  public void test_TableModelDescription_setRowCount() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01'},",
            "        {'10', '11'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(2, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // set rows: 4
    modelDescription.setRowCount(4);
    assertEquals(4, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "    {'10', '11'},",
                "    {null, null},",
                "    {null, null},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    // set rows: 1
    modelDescription.setRowCount(1);
    assertEquals(1, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }

  /**
   * Test for {@link TableModelDescription#moveRow(int, int)}.
   */
  public void test_TableModelDescription_moveRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import javax.swing.table.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTable table = new JTable();",
            "    add(table);",
            "    table.setModel(new DefaultTableModel(",
            "      new Object[][]{",
            "        {'00', '01'},",
            "        {'10', '11'},",
            "        {'20', '21'},",
            "        {'30', '31'},",
            "      },",
            "      new String[]{",
            "        'A', 'B'",
            "      }",
            "    ));",
            "  }",
            "}");
    panel.refresh();
    JTableInfo tableInfo = (JTableInfo) panel.getChildrenComponents().get(0);
    // prepare TableModelDescription
    TableModelDescription modelDescription =
        new TableModelDescription((JTable) tableInfo.getObject());
    assertEquals(4, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    // move: backward
    modelDescription.moveRow(2, 1);
    assertEquals(4, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "    {'20', '21'},",
                "    {'10', '11'},",
                "    {'30', '31'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    // move: forward
    modelDescription.moveRow(1, 2);
    assertEquals(4, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'00', '01'},",
                "    {'10', '11'},",
                "    {'20', '21'},",
                "    {'30', '31'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
    // move: forward 2
    modelDescription.moveRow(0, 3);
    assertEquals(4, modelDescription.getRowCount());
    assertEquals(2, modelDescription.getColumnCount());
    assertEquals(
        getDoubleQuotes2(
            new String[]{
                "new javax.swing.table.DefaultTableModel(",
                "  new Object[][] {",
                "    {'10', '11'},",
                "    {'20', '21'},",
                "    {'30', '31'},",
                "    {'00', '01'},",
                "  },",
                "  new String[] {",
                "    'A', 'B'",
                "  }",
                ")"}).trim(),
        modelDescription.getModelSource());
  }
}
