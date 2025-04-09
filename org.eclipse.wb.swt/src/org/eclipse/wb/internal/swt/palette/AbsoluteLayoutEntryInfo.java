/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swt.palette;

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementation of {@link ToolEntryInfo} that adds {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.editor.palette
 */
public final class AbsoluteLayoutEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = Activator.getImageDescriptor("info/layout/absolute/layout.gif");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbsoluteLayoutEntryInfo() {
		setName(ModelMessages.AbsoluteLayoutEntryInfo_name);
		setDescription(ModelMessages.AbsoluteLayoutEntryInfo_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return ICON;
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
				ToolkitDescription toolkit = m_rootJavaInfo.getDescription().getToolkit();
				AbsoluteLayoutCreationSupport creationSupport = new AbsoluteLayoutCreationSupport();
				m_layout = new AbsoluteLayoutInfo(m_editor, toolkit, creationSupport);
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
