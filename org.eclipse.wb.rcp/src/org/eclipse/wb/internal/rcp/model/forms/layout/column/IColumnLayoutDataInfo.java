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

import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

/**
 * Interface model for {@link ColumnLayoutData}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public interface IColumnLayoutDataInfo extends ILayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Hint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the value of <code>widthHint</code> property.
   */
  void setWidthHint(int widthHint) throws Exception;

  /**
   * Sets the value of <code>heightHint</code> property.
   */
  void setHeightHint(int heightHint) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value for {@link ColumnLayoutData#horizontalAlignment} property.
   */
  int getHorizontalAlignment() throws Exception;

  /**
   * Sets value for {@link ColumnLayoutData#horizontalAlignment} property.
   */
  void setHorizontalAlignment(int horizontalAlignment) throws Exception;
}