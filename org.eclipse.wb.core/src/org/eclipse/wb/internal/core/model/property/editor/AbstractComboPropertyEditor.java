/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * The {@link PropertyEditor} for selecting single value using {@link CCombo}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public abstract class AbstractComboPropertyEditor extends TextDisplayPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	private CCombo m_combo;
	private boolean m_doDropDown;

	@Override
	public boolean activate(final PropertyTable propertyTable, final Property property, Point location)
			throws Exception {
		// create combo
		{
			m_combo = new CCombo(propertyTable.getControl(), SWT.READ_ONLY);
			m_doDropDown = true;
			// add items
			addItems(property, m_combo);
			// select item
			selectItem(property, m_combo);
		}
		// add listeners
		m_combo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				propertyTable.deactivateEditor(true);
			}
		});
		m_combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = m_combo.getSelectionIndex();
				toProperty(propertyTable, property, index);
			}
		});
		m_combo.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch (event.keyCode) {
				case SWT.ESC :
					propertyTable.deactivateEditor(false);
					break;
				case SWT.DEL :
					try {
						property.setValue(Property.UNKNOWN_VALUE);
						event.doit = false;
						selectItem(property, m_combo);
					} catch (Throwable e) {
						propertyTable.handleException(e);
						propertyTable.deactivateEditor(false);
					}
					m_combo.setListVisible(false);
					break;
				}
			}
		});
		m_combo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int index = (m_combo.getSelectionIndex() + 1) % m_combo.getItemCount();
				toProperty(propertyTable, property, index);
			}
		});
		// keep editor active
		return true;
	}

	@Override
	public final void setBounds(Rectangle bounds) {
		m_combo.setBounds(bounds);
		// editor created without bounds, so activate it after first setBounds()
		if (m_doDropDown) {
			m_doDropDown = false;
			m_combo.setFocus();
			m_combo.setListVisible(true);
		}
	}

	@Override
	public final void deactivate(PropertyTable propertyTable, Property property, boolean save) {
		if (m_combo != null) {
			m_combo.dispose();
			m_combo = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Abstract methods
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds items to given {@link CCombo}.
	 */
	protected abstract void addItems(Property property, CCombo combo) throws Exception;

	/**
	 * Selects current item in given {@link CCombo}.
	 */
	protected abstract void selectItem(Property property, CCombo combo) throws Exception;

	/**
	 * Transfers data from widget to {@link Property}.
	 */
	protected abstract void toPropertyEx(Property property, CCombo combo, int index)
			throws Exception;

	/**
	 * Transfers data from widget to {@link Property}.
	 */
	private void toProperty(PropertyTable propertyTable, Property property, int index) {
		try {
			toPropertyEx(property, m_combo, index);
		} catch (Throwable e) {
			propertyTable.handleException(e);
		}
		propertyTable.deactivateEditor(false);
	}
}
