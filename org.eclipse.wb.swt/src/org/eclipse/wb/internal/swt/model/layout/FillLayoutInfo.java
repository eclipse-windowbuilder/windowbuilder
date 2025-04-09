/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

import java.util.List;

/**
 * Model for SWT {@link FillLayout}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class FillLayoutInfo extends GenericFlowLayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FillLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		new FillLayoutAssistant(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accessors
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public FillLayout getLayout() {
		return (FillLayout) super.getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Styles
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean isHorizontal() {
		return getLayout().type == SWT.HORIZONTAL;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		commands.add(new LayoutClipboardCommand<FillLayoutInfo>(control) {
			private static final long serialVersionUID = 0L;

			@Override
			protected void add(FillLayoutInfo layout, ControlInfo control) throws Exception {
				layout.command_CREATE(control, null);
			}
		});
	}
}