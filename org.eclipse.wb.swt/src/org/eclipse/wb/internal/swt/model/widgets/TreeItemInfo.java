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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.support.TreeSupport;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * Model for SWT tree item {@link org.eclipse.swt.widgets.TreeItem}.
 *
 * @author lobas_av
 * @coverage swt.model.widgets
 */
public final class TreeItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeItemInfo(AstEditor editor,
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
	 * @return the parent {@link TreeInfo}.
	 */
	public TreeInfo getTree() {
		if (getParent() instanceof TreeItemInfo) {
			TreeItemInfo parentItem = (TreeItemInfo) getParent();
			return parentItem.getTree();
		}
		return (TreeInfo) getParent();
	}

	/**
	 * @return the {@link TreeItemInfo} children.
	 */
	public List<TreeItemInfo> getItems() {
		return getChildren(TreeItemInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Rectangle bounds = TreeSupport.getBounds(getObject());
			setModelBounds(bounds);
			// apply Tree client area insets
			if (getParent() instanceof TreeItemInfo) {
				Insets insets = getTree().getClientAreaInsets();
				bounds.performTranslate(insets);
				setBounds(bounds);
			}
		}
		// continue in super()
		super.refresh_fetch();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * After any using this {@link TreeItemInfo} as container.
	 */
	public void command_TARGET_after(TreeItemInfo item, TreeItemInfo nextItem) throws Exception {
		getPropertyByTitle("expanded").setValue(true);
	}
}