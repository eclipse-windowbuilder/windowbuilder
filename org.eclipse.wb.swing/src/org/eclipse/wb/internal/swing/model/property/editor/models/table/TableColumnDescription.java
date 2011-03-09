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

import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Information about column in {@link TableModel}, with ability to edit.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class TableColumnDescription {
  public static final int DEFAULT_PREFERRED_WIDTH = 75;
  public static final int DEFAULT_MIN_WIDTH = 15;
  public static final int DEFAULT_MAX_WIDTH = Integer.MAX_VALUE;
  public String m_name;
  public Class<?> m_class;
  public boolean m_editable;
  public boolean m_resizable;
  public int m_preferredWidth;
  public int m_minWidth;
  public int m_maxWidth;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableColumnDescription() {
    m_name = "New column";
    m_class = Object.class;
    m_editable = true;
    m_resizable = true;
    m_preferredWidth = DEFAULT_PREFERRED_WIDTH;
    m_minWidth = DEFAULT_MIN_WIDTH;
    m_maxWidth = DEFAULT_MAX_WIDTH;
  }

  public TableColumnDescription(JTable table, int index) {
    TableModel model = table.getModel();
    TableColumn column = table.getColumnModel().getColumn(index);
    //
    m_name = model.getColumnName(index);
    m_class = model.getColumnClass(index);
    m_editable = model.isCellEditable(0, index);
    m_resizable = column.getResizable();
    m_preferredWidth = column.getPreferredWidth();
    m_minWidth = column.getMinWidth();
    m_maxWidth = column.getMaxWidth();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds invocations (without {@link TableColumn} or {@link TableColumnModel} access code) to
   * update non-default values of this column.
   */
  List<String> getInvocations() {
    List<String> invocations = Lists.newArrayList();
    if (!m_resizable) {
      invocations.add("setResizable(false)");
    }
    if (m_preferredWidth != DEFAULT_PREFERRED_WIDTH) {
      invocations.add("setPreferredWidth(" + m_preferredWidth + ")");
    }
    if (m_minWidth != DEFAULT_MIN_WIDTH) {
      invocations.add("setMinWidth(" + m_minWidth + ")");
    }
    if (m_maxWidth != DEFAULT_MAX_WIDTH) {
      invocations.add("setMaxWidth(" + m_maxWidth + ")");
    }
    return invocations;
  }
}
