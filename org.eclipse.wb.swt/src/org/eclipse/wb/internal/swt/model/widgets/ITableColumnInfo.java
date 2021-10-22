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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.swt.widgets.TableColumn;

/**
 * Interface model of {@link TableColumn}.
 *
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public interface ITableColumnInfo extends IItemInfo {
  /**
   * Sets width in pixels.
   */
  public void setWidth(int width) throws Exception;
}