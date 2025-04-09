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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

/**
 * Interface model for {@link ColumnLayoutData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public interface IColumnLayoutDataInfo extends ILayoutDataInfo<ControlInfo> {
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