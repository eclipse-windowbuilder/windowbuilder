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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

/**
 * Model for {@link ColumnLayoutData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class ColumnLayoutDataInfo extends LayoutDataInfo implements IColumnLayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnLayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setWidthHint(int widthHint) throws Exception {
    getPropertyByTitle("widthHint").setValue(widthHint);
  }

  @Override
  public void setHeightHint(int heightHint) throws Exception {
    getPropertyByTitle("heightHint").setValue(heightHint);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getHorizontalAlignment() throws Exception {
    return (Integer) getPropertyByTitle("horizontalAlignment").getValue();
  }

  @Override
  public void setHorizontalAlignment(int horizontalAlignment) throws Exception {
    getPropertyByTitle("horizontalAlignment").setValue(horizontalAlignment);
  }
}