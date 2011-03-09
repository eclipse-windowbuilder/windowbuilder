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

import org.eclipse.swt.layout.RowData;

/**
 * Interface model for SWT {@link RowData}.
 * 
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public interface IRowDataInfo extends ILayoutDataInfo {
  /**
   * Sets width.
   */
  public void setWidth(int width) throws Exception;

  /**
   * Sets height.
   */
  public void setHeight(int height) throws Exception;
}