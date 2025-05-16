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
package org.eclipse.wb.internal.swing.palette;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementation of {@link ToolEntryInfo} that adds {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.editor.palette
 */
public final class AbsoluteLayoutEntryInfo extends ToolEntryInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbsoluteLayoutEntryInfo() {
		setName(PaletteMessages.AbsoluteLayoutEntryInfo_name);
		setDescription(PaletteMessages.AbsoluteLayoutEntryInfo_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return CoreImages.LAYOUT_ABSOLUTE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolEntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool createTool() throws Exception {
		// prepare factory
		ICreationFactory factory = new ICreationFactory() {
			private AbsoluteLayoutInfo m_layout;

			@Override
			public void activate() throws Exception {
				m_layout = AbsoluteLayoutInfo.createExplicit(m_rootJavaInfo.getEditor());
				m_layout.setObject(null); // force initialize
			}

			@Override
			public Object getNewObject() {
				return m_layout;
			}
		};
		// return tool
		return new CreationTool(factory);
	}
}
