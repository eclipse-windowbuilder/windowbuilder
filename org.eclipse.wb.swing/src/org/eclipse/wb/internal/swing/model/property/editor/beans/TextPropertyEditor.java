/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.beans;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;

/**
 * The {@link PropertyEditor} wrapper for text based AWT {@link java.beans.PropertyEditor}.
 *
 * @author lobas_av
 * @coverage swing.property.beans
 */
public final class TextPropertyEditor extends AbstractTextPropertyEditor
implements
IValueSourcePropertyEditor,
IClipboardSourceProvider {
	private final PropertyEditorWrapper m_editorWrapper;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TextPropertyEditor(PropertyEditorWrapper editorWrapper) {
		m_editorWrapper = editorWrapper;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean activate(PropertyTable propertyTable, Property property, Point location)
			throws Exception {
		if (getText(property) == null) {
			if (getPresentation() != null && location == null) {
				m_editorWrapper.openDialogEditor(propertyTable, property);
			}
			return false;
		}
		return super.activate(propertyTable, property, location);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getEditorText(Property property) throws Exception {
		return getText(property);
	}

	@Override
	protected boolean setEditorText(Property property, String text) throws Exception {
		if (text.length() == 0) {
			property.setValue(Property.UNKNOWN_VALUE);
		} else {
			m_editorWrapper.setText(property, text);
		}
		return true;
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
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		return m_editorWrapper.getSource(property.getValue());
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
}