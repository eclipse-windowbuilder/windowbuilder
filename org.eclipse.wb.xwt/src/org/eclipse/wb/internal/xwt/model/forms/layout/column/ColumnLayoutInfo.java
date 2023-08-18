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
package org.eclipse.wb.internal.xwt.model.forms.layout.column;

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.ColumnLayoutAssistant;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.ColumnLayoutSelectionActionsSupport;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.IColumnLayoutDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.IColumnLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.GenericFlowLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.xwt.model.layout.LayoutDataClipboardCommand;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.ui.forms.widgets.ColumnLayout;

import java.util.List;

/**
 * Model for {@link ColumnLayout}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.forms
 */
public final class ColumnLayoutInfo extends GenericFlowLayoutInfo
implements
IColumnLayoutInfo<ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnLayoutInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
		new ColumnLayoutAssistant(this);
		new ColumnLayoutSelectionActionsSupport<>(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Components/constraints
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IColumnLayoutDataInfo getColumnData2(ControlInfo control) {
		return getColumnData(control);
	}

	/**
	 * @return {@link ColumnLayoutDataInfo} associated with given {@link ControlInfo}.
	 */
	public static ColumnLayoutDataInfo getColumnData(ControlInfo control) {
		return (ColumnLayoutDataInfo) getLayoutData(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isHorizontal() {
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		// command for adding child
		commands.add(new LayoutClipboardCommand<ColumnLayoutInfo>(control) {
			private static final long serialVersionUID = 0L;

			@Override
			protected void add(ColumnLayoutInfo layout, ControlInfo control) throws Exception {
				layout.command_CREATE(control, null);
			}
		});
		// command for ColumnLayoutData
		commands.add(new LayoutDataClipboardCommand(this, control));
	}
}
