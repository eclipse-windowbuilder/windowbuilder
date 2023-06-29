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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

/**
 * Model for {@link TabFolder}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class TabFolderInfo extends AbstractTabFolderInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabFolderInfo(AstEditor editor,
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
	 * @return the {@link TabItemInfo} children.
	 */
	public List<TabItemInfo> getItems2() {
		return getChildren(TabItemInfo.class);
	}

	@Override
	protected String getItemClassName() {
		return "org.eclipse.swt.widgets.TabItem";
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
		AbstractTabItemInfo selectedItem = getSelectedItem();
		if (selectedItem != null) {
			TabFolder tabFolder = (TabFolder) getObject();
			tabFolder.setSelection((TabItem) selectedItem.getObject());
		}
	}
}
