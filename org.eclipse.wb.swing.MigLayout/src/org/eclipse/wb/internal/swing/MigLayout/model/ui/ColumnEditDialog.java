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

import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo.Alignment;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * The dialog for editing {@link MigColumnInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class ColumnEditDialog extends DimensionEditDialog<MigColumnInfo, Alignment> {
  private static final List<AlignmentDescription<Alignment>> ALIGNMENTS = Lists.newArrayList();
  static {
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.DEFAULT, "&default"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.LEFT, "&left"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.CENTER, "&center"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.RIGHT, "&right"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.FILL, "&fill"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.LEADING, "l&eading"));
    ALIGNMENTS.add(new AlignmentDescription<Alignment>(Alignment.TRAILING, "&trailing"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnEditDialog(Shell parentShell, MigLayoutInfo layout, MigColumnInfo column) {
    super(parentShell, layout, layout.getColumns(), column, "Column", ALIGNMENTS);
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
