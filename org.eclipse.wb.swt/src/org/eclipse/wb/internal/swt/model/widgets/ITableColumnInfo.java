/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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