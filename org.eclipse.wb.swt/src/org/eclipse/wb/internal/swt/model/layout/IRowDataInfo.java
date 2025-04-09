/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.layout.RowData;

/**
 * Interface model for SWT {@link RowData}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public interface IRowDataInfo extends ILayoutDataInfo<ControlInfo> {
	/**
	 * Sets width.
	 */
	public void setWidth(int width) throws Exception;

	/**
	 * Sets height.
	 */
	public void setHeight(int height) throws Exception;
}