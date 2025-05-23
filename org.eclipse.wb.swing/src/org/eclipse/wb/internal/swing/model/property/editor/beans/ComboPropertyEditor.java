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
package org.eclipse.wb.internal.swing.model.property.editor.beans;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.custom.CCombo;

/**
 * The {@link PropertyEditor} wrapper for tag's based AWT {@link java.beans.PropertyEditor}.
 *
 * @author lobas_av
 * @coverage swing.property.beans
 */
public final class ComboPropertyEditor extends AbstractComboPropertyEditor
implements
IValueSourcePropertyEditor {
	private final PropertyEditorWrapper m_editorWrapper;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComboPropertyEditor(PropertyEditorWrapper editorWrapper) {
		m_editorWrapper = editorWrapper;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addItems(Property property, CCombo combo) throws Exception {
		for (String item : getTags(property)) {
			combo.add(item);
		}
	}

	@Override
	protected void selectItem(Property property, CCombo combo) throws Exception {
		combo.setText(getText(property));
	}

	@Override
	protected void toPropertyEx(Property property, CCombo combo, int index) throws Exception {
		String[] items = getTags(property);
		m_editorWrapper.setText(property, items[index]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IValueSourcePropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getValueSource(Object value) throws Exception {
		return m_editorWrapper.getSource(value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public PropertyEditorPresentation getPresentation() {
		return m_editorWrapper.getPresentation();
	}

	@Override
	protected String getText(Property property) throws Exception {
		return m_editorWrapper.getText(property);
	}

	@Override
	public void paint(Property property, Graphics graphics, int x, int y, int width, int height) throws Exception {
		m_editorWrapper.paint(property, graphics, x, y, width, height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private String[] getTags(Property property) throws Exception {
		return m_editorWrapper.getTags(property);
	}
}