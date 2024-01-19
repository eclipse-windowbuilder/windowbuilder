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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.ColumnsDialog;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.RowsDialog;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Property} to display/edit {@link List} of {@link FormDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class DimensionsProperty extends Property {
	private final boolean m_horizontal;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionsProperty(FormLayoutInfo layout, boolean horizontal) {
		super(new Editor(layout, horizontal));
		m_horizontal = horizontal;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return m_horizontal ? "columnSpecs" : "rowSpecs";
	}

	@Override
	public Object getValue() throws Exception {
		return UNKNOWN_VALUE;
	}

	@Override
	public boolean isModified() throws Exception {
		return true;
	}

	@Override
	public void setValue(Object value) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor
	//
	////////////////////////////////////////////////////////////////////////////
	private static class Editor extends TextDialogPropertyEditor {
		private final FormLayoutInfo m_layout;
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public Editor(FormLayoutInfo layout, boolean horizontal) {
			m_layout = layout;
			m_horizontal = horizontal;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected String getText(Property property) throws Exception {
			List<String> titles = new ArrayList<>();
			List<? extends FormDimensionInfo> dimensions =
					m_horizontal ? m_layout.getColumns() : m_layout.getRows();
			for (FormDimensionInfo dimension : dimensions) {
				titles.add(dimension.getToolTip());
			}
			return StringUtils.join(titles.iterator(), ", ");
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Editing
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void openDialog(Property property) throws Exception {
			if (m_horizontal) {
				new ColumnsDialog(DesignerPlugin.getShell(), m_layout).open();
			} else {
				new RowsDialog(DesignerPlugin.getShell(), m_layout).open();
			}
		}
	}
}
