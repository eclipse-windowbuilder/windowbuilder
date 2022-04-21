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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.layout.RowData;

/**
 * Model for SWT {@link RowData}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class RowDataInfo extends LayoutDataInfo implements IRowDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setWidth(int width) throws Exception {
    getPropertyByTitle("width").setValue(width);
  }

  @Override
  public void setHeight(int height) throws Exception {
    getPropertyByTitle("height").setValue(height);
  }
}