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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Dialog for editing {@link List} of {@link MigColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class ColumnsDialog extends DimensionsDialog<MigColumnInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnsDialog(Shell parentShell, MigLayoutInfo layout) {
		super(parentShell, layout, layout.getColumns());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DimensionsDialog: strings
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getDialogTitle() {
		return ModelMessages.ColumnsDialog_dialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return ModelMessages.ColumnsDialog_dialogMessage;
	}

	@Override
	protected String getViewerTitle() {
		return ModelMessages.ColumnsDialog_viewerTitle;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DimensionsDialog: dimensions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void moveDimensionsUp(Iterable<MigColumnInfo> dimensions) throws Exception {
		for (MigColumnInfo column : dimensions) {
			int index = column.getIndex();
			m_layout.moveColumn(index, index - 1);
		}
	}

	@Override
	protected void moveDimensionsDown(Iterable<MigColumnInfo> dimensions) throws Exception {
		for (MigColumnInfo column : dimensions) {
			int index = column.getIndex();
			m_layout.moveColumn(index, index + 2);
		}
	}

	@Override
	protected boolean editSelectedDimension(MigColumnInfo column) {
		return new ColumnEditDialog(getShell(), m_layout, column).open() == OK;
	}

	@Override
	protected MigColumnInfo createNewDimension(int targetIndex) throws Exception {
		MigColumnInfo column = new MigColumnInfo(m_layout);
		m_layout.insertColumn(targetIndex);
		return column;
	}
}
