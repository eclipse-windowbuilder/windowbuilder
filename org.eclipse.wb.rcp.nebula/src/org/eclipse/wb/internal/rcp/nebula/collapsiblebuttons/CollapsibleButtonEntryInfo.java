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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.nebula.Activator;
import org.eclipse.wb.internal.rcp.nebula.Messages;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link EntryInfo} that allows user to drop new {@link CustomButton} on {@link CollapsibleButtons}
 * .
 *
 * @author sablin_aa
 * @coverage nebula.palette
 */
public final class CollapsibleButtonEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = Activator
			.getImageDescriptor("wbp-meta/org/eclipse/nebula/widgets/collapsiblebuttons/CustomButton.png");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CollapsibleButtonEntryInfo() throws Exception {
		setName(Messages.CollapsibleButtonEntryInfo_name);
		setDescription(Messages.CollapsibleButtonEntryInfo_description);
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
	public boolean initialize(IEditPartViewer editPartViewer, JavaInfo rootJavaInfo) {
		super.initialize(editPartViewer, rootJavaInfo);
		return ProjectUtils.hasType(
				m_javaProject,
				"org.eclipse.nebula.widgets.collapsiblebuttons.CollapsibleButtons");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolEntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool createTool() throws Exception {
		return new CollapsibleButtonDropTool();
	}
}
