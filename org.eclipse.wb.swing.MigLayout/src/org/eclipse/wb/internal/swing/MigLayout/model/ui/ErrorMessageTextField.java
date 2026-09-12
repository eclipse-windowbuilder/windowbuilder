/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Special text field that can show optional error message in top-right corner.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public class ErrorMessageTextField {
	private final ControlDecoration m_controlDecoration;
	private final Text m_control;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ErrorMessageTextField(Composite parent, int style) {
		m_control = new Text(parent, style);
		// prepare decoration
		FieldDecoration standardDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		m_controlDecoration = new ControlDecoration(m_control, SWT.TOP | SWT.RIGHT);
		m_controlDecoration.setImage(standardDecoration.getImage());
		m_controlDecoration.hide();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	public Text getControl() {
		return m_control;
	}

	/**
	 * Shows error message.
	 *
	 * @param message
	 *          the message to show as decorator, or <code>null</code> to hide error message.
	 */
	public void setErrorMessage(String message) {
		if (message != null) {
			m_controlDecoration.setDescriptionText(message);
			m_controlDecoration.show();
		} else {
			m_controlDecoration.setDescriptionText(null);
			m_controlDecoration.hide();
		}
	}
}
