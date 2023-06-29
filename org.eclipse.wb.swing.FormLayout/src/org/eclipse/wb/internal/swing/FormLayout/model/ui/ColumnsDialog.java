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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ModelMessages;

import org.eclipse.swt.widgets.Shell;

import com.jgoodies.forms.layout.FormSpecs;

import java.text.MessageFormat;
import java.util.List;

/**
 * Dialog for editing {@link List} of {@link FormColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class ColumnsDialog extends DimensionsDialog<FormColumnInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnsDialog(Shell parentShell, FormLayoutInfo layout) {
		super(parentShell, layout, createColumnsCopy(layout), layout.getMinimumSize().width);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy/update
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the deep copy of {@link List} with {@link FormColumnInfo}'s.
	 */
	private static List<FormColumnInfo> createColumnsCopy(final FormLayoutInfo layout) {
		final List<FormColumnInfo> columns = Lists.newArrayList();
		ExecutionUtils.runRethrow(new RunnableEx() {
			@Override
			public void run() throws Exception {
				for (FormColumnInfo column : layout.getColumns()) {
					columns.add(column.copy());
				}
			}
		});
		return columns;
	}

	@Override
	protected void updateLayoutInfo(List<FormColumnInfo> dimensions) throws Exception {
		m_layout.setColumns(dimensions);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DimensionsDialog
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

	@Override
	protected String getMinimalErrorMessage(int minimumDimensions) {
		return MessageFormat.format(ModelMessages.ColumnsDialog_minimalErrorMessage, minimumDimensions);
	}

	@Override
	protected boolean editSelectedDimension(List<FormColumnInfo> dimensions, FormColumnInfo column) {
		return new ColumnEditDialog(getShell(), dimensions, column).open() == OK;
	}

	@Override
	protected FormColumnInfo createNewDimension() throws Exception {
		return new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
	}
}
