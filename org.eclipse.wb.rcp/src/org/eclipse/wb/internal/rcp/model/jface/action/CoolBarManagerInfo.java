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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;

/**
 * Model for {@link ICoolBarManager}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class CoolBarManagerInfo extends ContributionManagerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CoolBarManagerInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<ToolBarManagerInfo> getToolBarManagers() {
		return getChildren(ToolBarManagerInfo.class);
	}

	/**
	 * Convenience method that returns the {@link CoolBarManager} contained by this
	 * {@link CoolBarManagerInfo}. Note: We assume that {@link ICoolBarManager} is
	 * always implemented by a {@link CoolBarManager}.
	 */
	protected CoolBarManager getManager() {
		return (CoolBarManager) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		// force update for CoolBarManager, to ensure that CoolBar widget is create and filled (re-filled)
		getManager().update(true);
		// prepare CoolBar widget
		{
			CoolBar coolBar = getManager().getControl();
			// if no any items, create one
			if (coolBar.getItemCount() == 0) {
				CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
				// use ToolBar on CoolItem
				ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT);
				coolItem.setControl(toolBar);
				// show message using ToolItem
				ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
				toolItem.setText("Empty CoolBarManager, add new ToolBarManager(s)");
				// configure sizes
				Point toolBar_preferredSize = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point item_preferredSize =
						coolItem.computeSize(toolBar_preferredSize.x, toolBar_preferredSize.y);
				coolItem.setSize(item_preferredSize);
			}
			// OK, remember as component
			setComponentObject(coolBar);
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		// prepare bounds of underlying CoolBar
		ControlInfo.refresh_fetch(this, null);
		// process children
		super.refresh_fetch();
		tweakToolBarManagerBounds();
	}

	/**
	 * Each child {@link ToolBarManager} is represented as {@link ToolBar} widget, but it is more
	 * convenient to use bounds of {@link CoolItem} with this {@link ToolBar}.
	 */
	private void tweakToolBarManagerBounds() throws Exception {
		CoolBar coolBar = getManager().getControl();
		CoolItem[] coolItems = coolBar.getItems();
		for (ToolBarManagerInfo toolBarManager : getChildren(ToolBarManagerInfo.class)) {
			ToolBar toolBar = toolBarManager.getManager().getControl();
			for (CoolItem coolItem : coolItems) {
				if (coolItem.getControl() == toolBar) {
					Rectangle newBounds = new Rectangle(coolItem.getBounds());
					Rectangle oldBounds = toolBarManager.getBounds();
					final int deltaX = oldBounds.x - newBounds.x;
					final int deltaY = oldBounds.y - newBounds.y;
					toolBarManager.accept(new ObjectInfoVisitor() {
						@Override
						public void endVisit(ObjectInfo objectInfo) throws Exception {
							if (objectInfo instanceof AbstractComponentInfo component) {
								component.getBounds().performTranslate(deltaX, deltaY);
							}
						}
					});
					toolBarManager.setBounds(newBounds);
				}
			}
		}
	}
}
