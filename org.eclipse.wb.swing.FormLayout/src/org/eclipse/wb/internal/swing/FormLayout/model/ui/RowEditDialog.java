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

import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;

import org.eclipse.swt.widgets.Shell;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.RowSpec;

import java.util.List;

/**
 * The dialog for editing {@link FormRowInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public class RowEditDialog extends DimensionEditDialog<FormRowInfo> {
  private static final DefaultAlignmentDescription[] ALIGNMENTS =
      new DefaultAlignmentDescription[]{
          new DefaultAlignmentDescription(RowSpec.TOP, "&top"),
          new DefaultAlignmentDescription(RowSpec.CENTER, "&center"),
          new DefaultAlignmentDescription(RowSpec.BOTTOM, "&bottom"),
          new DefaultAlignmentDescription(RowSpec.FILL, "&fill"),};
  private static final UnitDescription[] UNITS = new UnitDescription[]{
      new UnitDescription(ConstantSize.DIALOG_UNITS_Y, "Dialog units"),
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
  public RowEditDialog(Shell parentShell, FormLayoutInfo layout, FormRowInfo row) {
    super(parentShell, layout, layout.getRows(), row, "Row", ALIGNMENTS, UNITS);
  }

  public RowEditDialog(Shell parentShell, List<FormRowInfo> rows, FormRowInfo row) {
    super(parentShell, null, rows, row, "Row", ALIGNMENTS, UNITS);
  }
}
