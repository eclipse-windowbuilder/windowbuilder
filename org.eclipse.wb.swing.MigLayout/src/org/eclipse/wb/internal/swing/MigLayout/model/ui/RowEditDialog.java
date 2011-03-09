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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo.Alignment;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * The dialog for editing {@link MigRowInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class RowEditDialog extends DimensionEditDialog<MigRowInfo, Alignment> {
  private static final List<AlignmentDescription<Alignment>> ALIGNMENTS = Lists.newArrayList();
  static {
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.DEFAULT, "&default"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.TOP, "&top"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.CENTER, "&center"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.BOTTOM, "&bottom"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.FILL, "&fill"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.BASELINE, "b&aseline"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowEditDialog(Shell parentShell, MigLayoutInfo layout, MigRowInfo row) {
    super(parentShell, layout, layout.getRows(), row, "Row", ALIGNMENTS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Alignment getAlignment(MigRowInfo dimension) {
    return dimension.getAlignment(false);
  }

  @Override
  protected void setAlignment(MigRowInfo dimension, Alignment alignment) {
    dimension.setAlignment(alignment);
  }
}
