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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * Dialog field for selecting directory in file system.
 *
 * @author scheglov_ke
 */
public class StringButtonDirectoryDialogField extends StringButtonDialogField {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringButtonDirectoryDialogField() {
		super(new Adapter());
		setButtonLabel("...");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Control[] doFillIntoGrid(Composite parent, int columns) {
		Control[] controls = super.doFillIntoGrid(parent, columns);
		GridDataFactory.create(getChangeControl(null));
		return controls;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Adapter
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class Adapter implements IStringButtonAdapter {
		////////////////////////////////////////////////////////////////////////////
		//
		// IStringButtonAdapter
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void changeControlPressed(DialogField field) {
			StringButtonDirectoryDialogField directoryField = (StringButtonDirectoryDialogField) field;
			DirectoryDialog directoryDialog =
					new DirectoryDialog(directoryField.getLabelControl(null).getShell());
			directoryDialog.setFilterPath(directoryField.getText());
			String newDirectory = directoryDialog.open();
			if (newDirectory != null) {
				directoryField.setText(newDirectory);
			}
		}
	}
}
