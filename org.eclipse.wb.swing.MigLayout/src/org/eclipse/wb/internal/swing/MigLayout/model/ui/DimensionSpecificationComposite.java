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

import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Composite for editing {@link MigDimensionInfo} as text.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class DimensionSpecificationComposite extends Composite {
	private MigDimensionInfo m_dimension;
	private final ErrorMessageTextField m_field;
	private final Text m_textWidget;
	private boolean m_updatingDimension;
	private final Listener m_listener = new Listener() {
		public void handleEvent(Event e) {
			String s = m_textWidget.getText();
			toDimension(s);
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionSpecificationComposite(Composite parent) {
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
	 * Updates this field from {@link MigDimensionInfo}.
	 */
	public void fromDimension(MigDimensionInfo dimension) {
		if (!m_updatingDimension) {
			m_dimension = dimension;
			String specification = m_dimension.getString(false);
			if (!m_textWidget.getText().equals(specification)) {
				setText(specification);
			}
		}
	}

	/**
	 * Uses text from {@link #m_textWidget} to update {@link MigDimensionInfo}.
	 */
	private void toDimension(String s) {
		m_updatingDimension = true;
		try {
			m_dimension.setString(s);
			notifyModified(true);
			m_field.setErrorMessage(null);
		} catch (Throwable e) {
			notifyModified(false);
			m_field.setErrorMessage(e.getMessage());
		} finally {
			m_updatingDimension = false;
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
