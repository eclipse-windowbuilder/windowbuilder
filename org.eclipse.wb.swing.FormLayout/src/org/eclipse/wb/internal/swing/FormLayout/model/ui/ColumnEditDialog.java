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

import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.eclipse.swt.widgets.Shell;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.ConstantSize;

import java.util.List;

/**
 * The dialog for editing {@link FormColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class ColumnEditDialog extends DimensionEditDialog<FormColumnInfo> {
  private static final DefaultAlignmentDescription[] ALIGNMENTS =
      new DefaultAlignmentDescription[]{
          new DefaultAlignmentDescription(ColumnSpec.LEFT, "&left"),
          new DefaultAlignmentDescription(ColumnSpec.CENTER, "&center"),
          new DefaultAlignmentDescription(ColumnSpec.RIGHT, "&right"),
          new DefaultAlignmentDescription(ColumnSpec.FILL, "&fill"),};
  private static final UnitDescription[] UNITS = new UnitDescription[]{
      new UnitDescription(ConstantSize.DIALOG_UNITS_X, "Dialog units"),
      new UnitDescription(ConstantSize.PIXEL, "Pixel"),
      new UnitDescription(ConstantSize.POINT, "Point"),
      new UnitDescription(ConstantSize.MILLIMETER, "Millimeter"),
      new UnitDescription(ConstantSize.CENTIMETER, "Centimeter"),
      new UnitDescription(ConstantSize.INCH, "Inch"),};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnEditDialog(Shell parentShell, FormLayoutInfo layout, FormColumnInfo column) {
    super(parentShell, layout, layout.getColumns(), column, "Column", ALIGNMENTS, UNITS);
  }

  public ColumnEditDialog(Shell parentShell, List<FormColumnInfo> columns, FormColumnInfo column) {
    super(parentShell, null, columns, column, "Column", ALIGNMENTS, UNITS);
  }
}
