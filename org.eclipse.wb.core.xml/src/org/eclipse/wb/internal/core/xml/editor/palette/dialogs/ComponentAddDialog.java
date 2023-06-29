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
package org.eclipse.wb.internal.core.xml.editor.palette.dialogs;

import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.xml.Messages;
import org.eclipse.wb.internal.core.xml.editor.palette.command.Command;
import org.eclipse.wb.internal.core.xml.editor.palette.command.ComponentAddCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for adding new {@link ComponentEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.palette.ui
 */
public final class ComponentAddDialog extends ComponentAbstractDialog {
	private final PaletteInfo m_palette;
	private final CategoryInfo m_initialCategory;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentAddDialog(Shell parentShell,
			EditorContext context,
			PaletteInfo palette,
			CategoryInfo initialCategory) {
		super(parentShell,
				context,
				Messages.ComponentAddDialog_title,
				Messages.ComponentAddDialog_message);
		m_palette = palette;
		m_initialCategory = initialCategory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	private ComboDialogField m_categoryField;

	@Override
	protected void createControls(Composite container) {
		super.createControls(container);
		m_visibleField.setSelection(true);
		// category
		{
			m_categoryField = createCategoryField(m_palette, m_initialCategory);
			doCreateField(m_categoryField, Messages.ComponentAddDialog_categoryLabel);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command createCommand() {
		String id = "custom_" + System.currentTimeMillis();
		String name = m_nameField.getText().trim();
		String description = getDescriptionText();
		boolean hidden = m_visibleField.getSelection();
		String className = m_classField.getText();
		CategoryInfo category = m_palette.getCategories().get(m_categoryField.getSelectionIndex());
		return new ComponentAddCommand(id, name, description, hidden, className, category);
	}
}
