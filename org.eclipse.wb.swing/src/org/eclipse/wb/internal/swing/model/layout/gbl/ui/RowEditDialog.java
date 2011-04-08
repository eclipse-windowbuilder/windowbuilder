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
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * The dialog for editing {@link FormRowInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout.ui
 */
public final class RowEditDialog extends DimensionEditDialog<RowInfo, RowInfo.Alignment> {
  private static final List<AlignmentDescription<RowInfo.Alignment>> ALIGNMENTS =
      Lists.newArrayList();
  static {
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.TOP,
        ModelMessages.RowEditDialog_aTop));
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.CENTER,
        ModelMessages.RowEditDialog_aCenter));
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.BOTTOM,
        ModelMessages.RowEditDialog_aBottom));
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.FILL,
        ModelMessages.RowEditDialog_aFill));
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.BASELINE,
        ModelMessages.RowEditDialog_aBaseline));
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.BASELINE_ABOVE,
        ModelMessages.RowEditDialog_aAboveBaseline));
    ALIGNMENTS.add(new AlignmentDescription<RowInfo.Alignment>(RowInfo.Alignment.BASELINE_BELOW,
        ModelMessages.RowEditDialog_aBelowBaseline));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowEditDialog(Shell parentShell, AbstractGridBagLayoutInfo layout, RowInfo row) {
    super(parentShell, layout, layout.getRows(), row, ModelMessages.RowEditDialog_title, ALIGNMENTS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected RowInfo.Alignment getAlignment(RowInfo dimension) {
    return dimension.getAlignment();
  }

  @Override
  protected void setAlignment(RowInfo dimension, RowInfo.Alignment alignment) throws Exception {
    dimension.setAlignment(alignment);
  }
}
