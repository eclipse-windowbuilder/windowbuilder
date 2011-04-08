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
package org.eclipse.wb.internal.swing.model.layout.gbl.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * The dialog for editing {@link ColumnInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout.ui
 */
public final class ColumnEditDialog extends DimensionEditDialog<ColumnInfo, ColumnInfo.Alignment> {
  private static final List<AlignmentDescription<ColumnInfo.Alignment>> ALIGNMENTS =
      Lists.newArrayList();
  static {
    ALIGNMENTS.add(new AlignmentDescription<ColumnInfo.Alignment>(ColumnInfo.Alignment.LEFT,
        ModelMessages.ColumnEditDialog_aLeft));
    ALIGNMENTS.add(new AlignmentDescription<ColumnInfo.Alignment>(ColumnInfo.Alignment.CENTER,
        ModelMessages.ColumnEditDialog_aCenter));
    ALIGNMENTS.add(new AlignmentDescription<ColumnInfo.Alignment>(ColumnInfo.Alignment.RIGHT,
        ModelMessages.ColumnEditDialog_aRight));
    ALIGNMENTS.add(new AlignmentDescription<ColumnInfo.Alignment>(ColumnInfo.Alignment.FILL,
        ModelMessages.ColumnEditDialog_aFill));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnEditDialog(Shell parentShell, AbstractGridBagLayoutInfo layout, ColumnInfo column) {
    super(parentShell,
        layout,
        layout.getColumns(),
        column,
        ModelMessages.ColumnEditDialog_title,
        ALIGNMENTS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ColumnInfo.Alignment getAlignment(ColumnInfo dimension) {
    return dimension.getAlignment();
  }

  @Override
  protected void setAlignment(ColumnInfo dimension, ColumnInfo.Alignment alignment)
      throws Exception {
    dimension.setAlignment(alignment);
  }
}
