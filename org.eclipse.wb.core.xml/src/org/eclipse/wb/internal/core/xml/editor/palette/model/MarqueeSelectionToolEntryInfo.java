/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.editor.palette.model;

import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.xml.Messages;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementation of {@link EntryInfo} that activates {@link MarqueeSelectionTool}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.palette
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