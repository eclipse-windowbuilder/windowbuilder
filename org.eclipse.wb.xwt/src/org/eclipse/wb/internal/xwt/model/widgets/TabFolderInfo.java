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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

/**
 * Model for {@link TabFolder}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class TabFolderInfo extends CompositeInfo {
	private final StackContainerSupport<TabItemInfo> m_stackContainer =
			new StackContainerSupport<>(this) {
		@Override
		protected List<TabItemInfo> getChildren() {
			return getItems();
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabFolderInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link TabItemInfo} children.
	 */
	public List<TabItemInfo> getItems() {
		return getChildren(TabItemInfo.class);
	}

	/**
	 * @return the selected {@link TabItemInfo}.
	 */
	public TabItemInfo getSelectedItem() {
		return m_stackContainer.getActive();
	}

	/**
	 * Sets the selected {@link TabItemInfo}.
	 */
	public void setSelectedItem(TabItemInfo item) {
		m_stackContainer.setActive(item);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new XmlObjectPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			List<ObjectInfo> children = super.getChildrenGraphical();
			// add Control of selected TabItem
			{
				TabItemInfo selectedItem = getSelectedItem();
				if (selectedItem != null) {
					ControlInfo control = selectedItem.getControl();
					if (control != null) {
						children.add(control);
					}
				}
			}
			// OK, show these children
			return children;
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		// select item
		{
			TabItemInfo item = getSelectedItem();
			if (item != null) {
				((TabFolder) getObject()).setSelection((TabItem) item.getObject());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new {@link ControlInfo}.
	 */
	public void command_CREATE(ControlInfo control, TabItemInfo nextItem) throws Exception {
		TabItemInfo newItem = createNewItem(nextItem);
		SimpleContainer simpleContainer = new SimpleContainerFactory(newItem, true).get().get(0);
		simpleContainer.command_CREATE(control);
	}

	/**
	 * Move existing {@link ControlInfo} on this {@link TabFolderInfo}.
	 */
	public void command_MOVE(ControlInfo control, TabItemInfo nextItem) throws Exception {
		TabItemInfo newItem = createNewItem(nextItem);
		SimpleContainer simpleContainer = new SimpleContainerFactory(newItem, true).get().get(0);
		simpleContainer.command_ADD(control);
	}

	/**
	 * Creates new {@link TabItem}.
	 */
	private TabItemInfo createNewItem(TabItemInfo nextItem) throws Exception {
		TabItemInfo newItem =
				(TabItemInfo) XmlObjectUtils.createObject(
						getContext(),
						"org.eclipse.swt.widgets.TabItem",
						new ElementCreationSupport());
		FlowContainer flowContainer = new FlowContainerFactory(this, true).get().get(0);
		flowContainer.command_CREATE(newItem, nextItem);
		return newItem;
	}
}
