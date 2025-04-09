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
package org.eclipse.wb.internal.core.gef.header;

import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.gef.core.ContextMenuProvider;
import org.eclipse.wb.internal.gef.core.MultiSelectionContextMenuProvider;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.IMenuManager;

/**
 * {@link ContextMenuProvider} for headers.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public final class HeadersContextMenuProvider extends MultiSelectionContextMenuProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public HeadersContextMenuProvider(IEditPartViewer viewer) {
		super(viewer);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MultiSelectionContextMenuProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void buildContextMenu(EditPart editPart, IMenuManager manager) {
		if (editPart instanceof IHeaderMenuProvider headerMenuProvider) {
			headerMenuProvider.buildContextMenu(manager);
		}
	}
}