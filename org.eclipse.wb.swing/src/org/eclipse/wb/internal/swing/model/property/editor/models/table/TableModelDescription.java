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
package org.eclipse.wb.internal.swing.model.property.editor.models.table;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.converter.ByteConverter;
import org.eclipse.wb.internal.core.model.property.converter.DoubleConverter;
import org.eclipse.wb.internal.core.model.property.converter.FloatConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.property.converter.LongConverter;
import org.eclipse.wb.internal.core.model.property.converter.ShortConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Information about {@link TableModel}, with ability to edit.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class TableModelDescription {
  private final LinkedList<LinkedList<Object>> m_values = Lists.newLinkedList();
  private final LinkedList<TableColumnDescription> m_columns = Lists.newLinkedList();
  private int m_rowCount;
  private int m_columnCount;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableModelDescription() {
  }

  public TableModelDescription(JTable table) {
    TableModel model = table.getModel();
    m_rowCount = model.getRowCount();
    m_columnCount = model.getColumnCount();
    // copy values
    for (int row = 0; row < m_rowCount; row++) {
      LinkedList<Object> rowValues = Lists.newLinkedList();
      m_values.add(rowValues);
      for (int column = 0; column < m_columnCount; column++) {
        rowValues.add(model.getValueAt(row, column));
      }
    }
    // copy columns 
    for (int column = 0; column < m_columnCount; column++) {
      m_columns.add(new TableColumnDescription(table, column));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getRowCount() {
    return m_rowCount;
  }

  public int getColumnCount() {
    return m_columnCount;
  }

  public Object getValue(int row, int column) {
    return m_values.get(row).get(column);
  }

  public TableColumnDescription getColumn(int index) {
    return m_columns.get(index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations: columns
  //
  ////////////////////////////////////////////////////////////////////////////
  public void insertColumn(int index) {
    m_columns.add(index, new TableColumnDescription());
    m_columnCount++;
    for (List<Object> row : m_values) {
      row.add(index, null);
    }
  }

  public void removeColumn(int index) {
    m_columnCount--;
    m_columns.remove(index);
    for (List<Object> row : m_values) {
      row.remove(index);
    }
  }

  public void setColumnCount(int columnCount) {
    while (m_columnCount < columnCount) {
      insertColumn(m_columnCount);
    }
    while (m_columnCount > columnCount) {
      removeColumn(m_columnCount - 1);
    }
  }

  public void moveColumn(int source, int target) {
    m_columns.add(target, m_columns.remove(source));
    for (List<Object> row : m_values) {
      row.add(target, row.remove(source));
    }
  }

  public void setColumnType(int index, Class<?> columnType) {
    getColumn(index).m_class = columnType;
    for (List<Object> row : m_values) {
      Object value = row.get(index);
      if (value != null && !columnType.isAssignableFrom(value.getClass())) {
        row.set(index, null);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations: rows
  //
  ////////////////////////////////////////////////////////////////////////////
  public void insertRow(int index) {
    m_rowCount++;
    LinkedList<Object> newRow = Lists.newLinkedList();
    m_values.add(newRow);
    for (int column = 0; column < m_columnCount; column++) {
      newRow.add(null);
    }
  }

  public void removeRow(int index) {
    m_rowCount--;
    m_values.remove(index);
  }

  public void setRowCount(int rowCount) {
    while (m_rowCount < rowCount) {
      insertRow(m_rowCount);
    }
    while (m_rowCount > rowCount) {
      removeRow(m_rowCount - 1);
    }
  }

  public void moveRow(int source, int target) {
    m_values.add(target, m_values.remove(source));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getModelSource() throws Exception {
    String source = "";
    source += "new javax.swing.table.DefaultTableModel(\n";
    // add values
    source += "\tnew Object[][] {\n";
    for (int row = 0; row < m_rowCount; row++) {
      source += "\t\t{";
      for (int column = 0; column < m_columnCount; column++) {
        Object value = getValue(row, column);
        if (column != 0) {
          source += ", ";
        }
        source += getValueSource(value, column);
      }
      source += "},\n";
    }
    source += "\t},\n";
    // add column names
    source += "\tnew String[] {\n";
    if (m_columnCount != 0) {
      source += "\t\t";
      for (int columnIndex = 0; columnIndex < m_columnCount; columnIndex++) {
        TableColumnDescription column = m_columns.get(columnIndex);
        if (columnIndex != 0) {
          source += ", ";
        }
        source += StringConverter.INSTANCE.toJavaSource(null, column.m_name);
      }
      source += "\n";
    }
    source += "\t}\n";
    // close constructor
    source += ")";
    // add column class/editable
    {
      String overridenMethods = getModelSource_overridenMethods();
      if (overridenMethods.length() != 0) {
        source += " {\n";
        source += overridenMethods;
        source += "}";
      }
    }
    // final source
    return source;
  }

  private String getModelSource_overridenMethods() {
    String source = "";
    // class
    if (hasNonObjectColumn()) {
      // constants
      source += "\tClass[] columnTypes = new Class[] {\n";
      source += "\t\t";
      for (int columnIndex = 0; columnIndex < m_columnCount; columnIndex++) {
        TableColumnDescription column = m_columns.get(columnIndex);
        if (columnIndex != 0) {
          source += ", ";
        }
        source += column.m_class.getCanonicalName() + ".class";
      }
      source += "\n";
      source += "\t};\n";
      // isCellEditable()
      source += "\tpublic Class getColumnClass(int columnIndex) {\n";
      source += "\t\treturn columnTypes[columnIndex];\n";
      source += "\t}\n";
    }
    // editable
    if (hasNotEditableColumn()) {
      // constants
      source += "\tboolean[] columnEditables = new boolean[] {\n";
      source += "\t\t";
      for (int columnIndex = 0; columnIndex < m_columnCount; columnIndex++) {
        TableColumnDescription column = m_columns.get(columnIndex);
        if (columnIndex != 0) {
          source += ", ";
        }
        source += column.m_editable;
      }
      source += "\n";
      source += "\t};\n";
      // isCellEditable()
      source += "\tpublic boolean isCellEditable(int row, int column) {\n";
      source += "\t\treturn columnEditables[column];\n";
      source += "\t}\n";
    }
    // final source
    return source;
  }

  private boolean hasNonObjectColumn() {
    for (TableColumnDescription column : m_columns) {
      if (column.m_class != Object.class) {
        return true;
      }
    }
    return false;
  }

  private boolean hasNotEditableColumn() {
    for (TableColumnDescription column : m_columns) {
      if (!column.m_editable) {
        return true;
      }
    }
    return false;
  }

  public List<String> getColumnModelInvocations() {
    List<String> invocations = Lists.newArrayList();
    for (int columnIndex = 0; columnIndex < m_columnCount; columnIndex++) {
      TableColumnDescription column = m_columns.get(columnIndex);
      for (String invocation : column.getInvocations()) {
        invocations.add(MessageFormat.format(
            "getColumnModel().getColumn({0}).{1}",
            columnIndex,
            invocation));
      }
    }
    return invocations;
  }

  private static String getValueSource(Object value, int column) throws Exception {
    if (value instanceof String) {
      return StringConverter.INSTANCE.toJavaSource(null, value);
    }
    if (value instanceof Boolean) {
      if (((Boolean) value).booleanValue()) {
        return "Boolean.TRUE";
      } else {
        return "Boolean.FALSE";
      }
    }
    if (value instanceof Integer) {
      return "new Integer(" + IntegerConverter.INSTANCE.toJavaSource(null, value) + ")";
    }
    if (value instanceof Byte) {
      return "new Byte(" + ByteConverter.INSTANCE.toJavaSource(null, value) + ")";
    }
    if (value instanceof Short) {
      return "new Short(" + ShortConverter.INSTANCE.toJavaSource(null, value) + ")";
    }
    if (value instanceof Long) {
      return "new Long(" + LongConverter.INSTANCE.toJavaSource(null, value) + ")";
    }
    if (value instanceof Float) {
      return "new Float(" + FloatConverter.INSTANCE.toJavaSource(null, value) + ")";
    }
    if (value instanceof Double) {
      return "new Double(" + DoubleConverter.INSTANCE.toJavaSource(null, value) + ")";
    }
    return "null";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableModel createTableModel() {
    return new DefaultTableModel() {
      private static final long serialVersionUID = 0L;

      @Override
      public int getColumnCount() {
        return TableModelDescription.this.getColumnCount();
      }

      @Override
      public int getRowCount() {
        return TableModelDescription.this.getRowCount();
      }

      @Override
      public String getColumnName(int column) {
        return getColumn(column).m_name;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        return getColumn(columnIndex).m_class;
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return getColumn(column).m_editable;
      }

      @Override
      public Object getValueAt(int row, int column) {
        return m_values.get(row).get(column);
      }

      @Override
      public void setValueAt(Object value, int row, int column) {
        m_values.get(row).set(column, value);
      }
    };
  }

  public void applyModel(JTable table) {
    TableColumnModel columnModel = table.getColumnModel();
    for (int columnIndex = 0; columnIndex < m_columnCount; columnIndex++) {
      TableColumnDescription columnDescription = m_columns.get(columnIndex);
      TableColumn column = columnModel.getColumn(columnIndex);
      column.setResizable(columnDescription.m_resizable);
      column.setPreferredWidth(columnDescription.m_preferredWidth);
      column.setMinWidth(columnDescription.m_minWidth);
      column.setMaxWidth(columnDescription.m_maxWidth);
    }
  }
}
