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
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import net.bytebuddy.ByteBuddy;

/**
 * Model for {@link IToolBarManager}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ToolBarManagerInfo extends ContributionManagerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ToolBarManagerInfo(AstEditor editor,
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
	 * Convenience method that returns the {@link ToolBar} contained by this
	 * {@link ToolBarManagerInfo}. Note: We can't override
	 * {@link #getComponentObject()}, as it may return either a {@link ToolBar} or a
	 * {@link ToolBarManager}.
	 */
	protected ToolBar getToolBar() {
		if (getComponentObject() instanceof ToolBar toolBar) {
			return toolBar;
		}
		return null;
	}
	/**
	 * Convenience method that returns the {@link ToolBarManager} contained by this
	 * {@link ToolBarManagerInfo}. Note: We assume that {@link IToolBarManager} is
	 * always implemented by a {@link ToolBarManager}.
	 */
	protected ToolBarManager getManager() {
		return (ToolBarManager) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		{
			ToolBar toolBar = getManager().getControl();
			// if no any items, create one
			if (toolBar.getItemCount() == 0) {
				addEmptyAction();
			}
			// OK, remember as component
			setComponentObject(toolBar);
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		// prepare bounds of underlying ToolBar
		ControlInfo.refresh_fetch(this, null);
		// prepare bounds of IContributionItem's
		ToolItem[] toolItems = getToolBar().getItems();
		for (AbstractComponentInfo contributionItem : getItems()) {
			Object contributionItemObject = contributionItem.getObject();
			for (ToolItem toolItem : toolItems) {
				if (toolItem.getData() == contributionItemObject) {
					Rectangle itemBounds = new Rectangle(toolItem.getBounds());
					contributionItem.setModelBounds(itemBounds);
					break;
				}
			}
		}
		// special support for ToolBarContributionItem
		if (getParent() instanceof ContributionItemInfo) {
			ContributionItemInfo parentItem = (ContributionItemInfo) getParent();
			parentItem.setModelBounds(getModelBounds().getCopy());
			parentItem.setBounds(getBounds().getCopy());
			getBounds().setLocation(0, 0);
		}
		// continue
		super.refresh_fetch();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Inserts new "empty" {@link Action}.
	 */
	private void addEmptyAction() throws Exception {
		String emptyText = JavaInfoUtils.getParameter(this, "emptyText");
		Assert.isNotNull(
				emptyText,
				"IToolBarManager should have parameter 'emptyText' with text to show when there are no Action's.");
		// prepare Action
		Action action;
		{
			ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
			Class<?> actionClass = editorLoader.loadClass("org.eclipse.jface.action.Action");
			action = (Action) new ByteBuddy() //
					.subclass(actionClass) //
					.make() //
					.load(editorLoader) //
					.getLoaded() //
					.getConstructor(String.class) //
					.newInstance(emptyText);
		}
		// append Action and update
		getManager().add(action);
		getManager().update(true);
	}
}
