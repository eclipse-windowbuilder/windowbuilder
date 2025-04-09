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
package org.eclipse.wb.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementation of {@link EntryInfo} that activates {@link MarqueeSelectionTool}.
 *
 * @author lobas_av
 * @coverage core.editor.palette
 */
public final class MarqueeSelectionToolEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = DesignerPlugin.getImageDescriptor("palette/MarqueeSelectionTool.png");
	private final MarqueeSelectionTool m_marqueeSelectionTool = new MarqueeSelectionTool();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MarqueeSelectionToolEntryInfo() {
		setName(Messages.MarqueeSelectionToolEntryInfo_name);
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

	@Override
	public Tool createTool() throws Exception {
		return m_marqueeSelectionTool;
	}
}