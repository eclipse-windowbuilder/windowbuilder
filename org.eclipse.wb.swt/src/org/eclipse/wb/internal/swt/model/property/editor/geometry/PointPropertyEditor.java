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
package org.eclipse.wb.internal.swt.model.property.editor.geometry;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.geometry.AbstractGeometryDialog;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;

/**
 * Implementation of {@link PropertyEditor} for {@link Point}.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class PointPropertyEditor extends TextDialogPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new PointPropertyEditor();

	private PointPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		if (property.getValue() instanceof Point point) {
			return "(%d, %d)".formatted(point.x, point.y);
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		// prepare Point to edit
		Point point;
		{
			if (property.getValue() instanceof Point oldPoint) {
				point = new Point(oldPoint.x, oldPoint.y);
			} else {
				point = new Point(0, 0);
			}
		}
		// prepare dialog
		PointDialog pointDialog = new PointDialog(property.getTitle(), point);
		// open dialog
		int result = pointDialog.open();
		if (result == IDialogConstants.IGNORE_ID) {
			property.setValue(Property.UNKNOWN_VALUE);
		} else if (result == IDialogConstants.OK_ID) {
			property.setValue(point);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PointDialog
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class PointDialog extends AbstractGeometryDialog {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PointDialog(String title, Object point) {
			super(title, point);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// GUI
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void createEditors() {
			createEditor(ModelMessages.PointPropertyEditor_xLabel, "x");
			createEditor(ModelMessages.PointPropertyEditor_yLabel, "y");
		}
	}
}