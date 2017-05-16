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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Helper for convenient creation/modification of {@link TableViewer}, {@link Table} and
 * {@link TableColumn}.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public final class TableFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TabFactory} for {@link Table} of given {@link TableViewer}.
   */
  public static TableFactory modify(TableViewer viewer) {
    return modify(viewer.getTable());
  }

  /**
   * @return the {@link TabFactory} for given {@link Table}.
   */
  public static TableFactory modify(Table table) {
    return new TableFactory(table);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Table m_table;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TableFactory(Table table) {
    m_table = table;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the <code>headerVisible</code> property.
   */
  public TableFactory headerVisible(boolean value) {
    m_table.setHeaderVisible(value);
    return this;
  }

  /**
   * Sets the <code>linesVisible</code> property.
   */
  public TableFactory linesVisible(boolean value) {
    m_table.setLinesVisible(value);
    return this;
  }

  /**
   * Sets default values for {@link Table} - show header and lines.
   */
  public TableFactory standard() {
    return headerVisible(true).linesVisible(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TableColumnFactory} for new {@link TableColumn} of this {@link Table}.
   */
  public TableColumnFactory newColumn() {
    return newColumn(SWT.NONE);
  }

  /**
   * @return the {@link TableColumnFactory} for new {@link TableColumn} of this {@link Table}.
   */
  public TableColumnFactory newColumn(int style) {
    return TableColumnFactory.create(m_table, style);
  }
}
