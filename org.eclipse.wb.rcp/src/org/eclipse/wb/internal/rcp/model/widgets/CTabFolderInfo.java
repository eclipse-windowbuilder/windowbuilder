/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.custom.CTabFolder;

import java.util.List;

/**
 * Model for {@link CTabFolder}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class CTabFolderInfo extends AbstractTabFolderInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CTabFolderInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link CTabItemInfo} children.
	 */
	public List<CTabItemInfo> getItems2() {
		return getChildren(CTabItemInfo.class);
	}

	@Override
	protected String getItemClassName() {
		return "org.eclipse.swt.custom.CTabItem";
	}

	@Override
	public CTabFolder getWidget() {
		return (CTabFolder) getObject();
	}

	@Override
	public CTabItemInfo getSelectedItem() {
		return (CTabItemInfo) super.getSelectedItem();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		selectItem();
		super.refresh_afterCreate();
	}

	private void selectItem() {
		CTabItemInfo selectedItem = getSelectedItem();
		if (selectedItem != null) {
			CTabFolder tabFolder = getWidget();
			tabFolder.setSelection(selectedItem.getWidget());
		}
	}
}
