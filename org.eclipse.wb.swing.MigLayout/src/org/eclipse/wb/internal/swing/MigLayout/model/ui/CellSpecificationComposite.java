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

import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for editing {@link CellConstraintsSupport} as text.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class CellSpecificationComposite extends Composite {
	////////////////////////////////////////////////////////////////////////////
	//
	// UI
	//
	////////////////////////////////////////////////////////////////////////////
	private final ErrorMessageTextField m_field;
	private final Text m_textWidget;
	private final Listener m_listener = new Listener() {
		public void handleEvent(Event e) {
			String s = m_textWidget.getText();
			toCell(s);
		}
	};
	private boolean m_updatingCell;
	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////
	private CellConstraintsSupport m_cell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CellSpecificationComposite(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		// prepare field/widget
		m_field = new ErrorMessageTextField(this, SWT.BORDER);
		m_textWidget = (Text) m_field.getControl();
		// listen for modification
		m_textWidget.addListener(SWT.Modify, m_listener);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean setFocus() {
		return m_textWidget.setFocus();
	}

	/**
	 * Updates this field from {@link CellConstraintsSupport}.
	 */
	public void fromCell(CellConstraintsSupport cell) {
		if (!m_updatingCell) {
			m_cell = cell;
			String specification = m_cell.getString();
			if (!m_textWidget.getText().equals(specification)) {
				setText(specification);
			}
		}
	}

	/**
	 * Uses text from {@link #m_textWidget} to update {@link CellConstraintsSupport}.
	 */
	private void toCell(String s) {
		m_updatingCell = true;
		try {
			m_cell.setString(s);
			notifyModified(true);
			m_field.setErrorMessage(null);
		} catch (Throwable e) {
			notifyModified(false);
			m_field.setErrorMessage(e.getMessage());
		} finally {
			m_updatingCell = false;
		}
	}

	/**
	 * Notifies {@link SWT#Modify} listener that this field was updated, with given valid state.
	 */
	private void notifyModified(boolean valid) {
		Event event = new Event();
		event.doit = valid;
		notifyListeners(SWT.Modify, event);
	}

	/**
	 * Sets text to {@link #m_textWidget}.
	 */
	private void setText(String text) {
		m_textWidget.removeListener(SWT.Modify, m_listener);
		try {
			m_textWidget.setText(text);
			m_field.setErrorMessage(null);
		} finally {
			m_textWidget.addListener(SWT.Modify, m_listener);
		}
	}
}
