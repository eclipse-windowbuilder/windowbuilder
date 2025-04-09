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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.widgets.TableColumn;

/**
 * Model for SWT table column {@link org.eclipse.swt.widgets.TableColumn}.
 *
 * @author lobas_av
 * @coverage swt.model.widgets
 */
public final class TableColumnInfo extends ItemInfo implements ITableColumnInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableColumnInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public TableColumn getWidget() {
		return (TableColumn) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setWidth(int width) throws Exception {
		getPropertyByTitle("width").setValue(width);
	}
}