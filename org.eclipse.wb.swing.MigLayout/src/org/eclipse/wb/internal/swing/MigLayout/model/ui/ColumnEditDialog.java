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

import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo.Alignment;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;

import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for editing {@link MigColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class ColumnEditDialog extends DimensionEditDialog<MigColumnInfo, Alignment> {
  private static final List<AlignmentDescription<Alignment>> ALIGNMENTS = new ArrayList<>();
  static {
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.DEFAULT,
            ModelMessages.ColumnEditDialog_alignmentDefault));
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.LEFT,
            ModelMessages.ColumnEditDialog_alignmentLeft));
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.CENTER,
            ModelMessages.ColumnEditDialog_alignmentCenter));
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.RIGHT,
            ModelMessages.ColumnEditDialog_alignmentRight));
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.FILL,
            ModelMessages.ColumnEditDialog_alignmentFill));
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.LEADING,
            ModelMessages.ColumnEditDialog_alignmentLeading));
    ALIGNMENTS.add(
        new AlignmentDescription<Alignment>(Alignment.TRAILING,
            ModelMessages.ColumnEditDialog_alignmentTrailing));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnEditDialog(Shell parentShell, MigLayoutInfo layout, MigColumnInfo column) {
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
  protected Alignment getAlignment(MigColumnInfo dimension) {
    return dimension.getAlignment(false);
  }

  @Override
  protected void setAlignment(MigColumnInfo dimension, Alignment alignment) {
    dimension.setAlignment(alignment);
  }
}
