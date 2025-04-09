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
package org.eclipse.wb.internal.core.editor.palette.command;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that removes {@link EntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class EntryRemoveCommand extends Command {
	public static final String ID = "removeEntry";
	private final String m_id;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public EntryRemoveCommand(EntryInfo entry) {
		m_id = entry.getId();
	}

	public EntryRemoveCommand(Attributes attributes) {
		m_id = attributes.getValue("id");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void execute(PaletteInfo palette) {
		EntryInfo entry = palette.getEntry(m_id);
		if (entry != null) {
			CategoryInfo category = entry.getCategory();
			category.removeEntry(entry);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addAttributes() {
		addAttribute("id", m_id);
	}
}
