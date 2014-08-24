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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ModelMessages;

import org.eclipse.swt.widgets.Shell;

import com.jgoodies.forms.layout.FormSpecs;

import java.text.MessageFormat;
import java.util.List;

/**
 * Dialog for editing {@link List} of {@link FormRowInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class RowsDialog extends DimensionsDialog<FormRowInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowsDialog(Shell parentShell, FormLayoutInfo layout) {
    super(parentShell, layout, createRowsCopy(layout), layout.getMinimumSize().width);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy/update
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the deep copy of {@link List} with {@link FormRowInfo}'s.
   */
  private static List<FormRowInfo> createRowsCopy(final FormLayoutInfo layout) {
    final List<FormRowInfo> rows = Lists.newArrayList();
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        for (FormRowInfo row : layout.getRows()) {
          rows.add(row.copy());
        }
      }
    });
    return rows;
  }

  @Override
  protected void updateLayoutInfo(List<FormRowInfo> dimensions) throws Exception {
    m_layout.setRows(dimensions);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DimensionsDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getDialogTitle() {
    return ModelMessages.RowsDialog_dialogTitle;
  }

  @Override
  protected String getDialogMessage() {
    return ModelMessages.RowsDialog_dialogMessage;
  }

  @Override
  protected String getViewerTitle() {
    return ModelMessages.RowsDialog_viewerTitle;
  }

  @Override
  protected String getMinimalErrorMessage(int minimumDimensions) {
    return MessageFormat.format(ModelMessages.RowsDialog_minimalErrorMesssage, minimumDimensions);
  }

  @Override
  protected boolean editSelectedDimension(List<FormRowInfo> dimensions, FormRowInfo column) {
    return new RowEditDialog(getShell(), dimensions, column).open() == OK;
  }

  @Override
  protected FormRowInfo createNewDimension() throws Exception {
    return new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC);
  }
}
