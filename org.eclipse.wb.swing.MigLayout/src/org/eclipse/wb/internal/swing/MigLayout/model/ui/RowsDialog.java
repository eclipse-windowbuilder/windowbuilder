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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Dialog for editing {@link List} of {@link MigRowInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class RowsDialog extends DimensionsDialog<MigRowInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowsDialog(Shell parentShell, MigLayoutInfo layout) {
    super(parentShell, layout, layout.getRows());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DimensionsDialog: strings
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getDialogTitle() {
    return "Edit Rows";
  }

  @Override
  protected String getDialogMessage() {
    return "Adding, removing or rearranging rows applies immediately.";
  }

  @Override
  protected String getViewerTitle() {
    return "Row &Specifications:";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DimensionsDialog: dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void moveDimensionsUp(Iterable<MigRowInfo> dimensions) throws Exception {
    for (MigRowInfo row : dimensions) {
      int index = row.getIndex();
      m_layout.moveRow(index, index - 1);
    }
  }

  @Override
  protected void moveDimensionsDown(Iterable<MigRowInfo> dimensions) throws Exception {
    for (MigRowInfo row : dimensions) {
      int index = row.getIndex();
      m_layout.moveRow(index, index + 2);
    }
  }

  @Override
  protected boolean editSelectedDimension(MigRowInfo row) {
    return new RowEditDialog(getShell(), m_layout, row).open() == OK;
  }

  @Override
  protected MigRowInfo createNewDimension(int targetIndex) throws Exception {
    MigRowInfo row = new MigRowInfo(m_layout);
    m_layout.insertRow(targetIndex);
    return row;
  }
}
