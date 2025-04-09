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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * Interface model for {@link ColumnLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public interface IColumnLayoutInfo<C extends IControlInfo> extends ILayoutInfo<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Layout data
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link IColumnLayoutDataInfo} associated with given {@link IControlInfo}.
	 */
	IColumnLayoutDataInfo getColumnData2(C control);
}