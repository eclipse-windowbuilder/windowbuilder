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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.TabFolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model for {@link TabFolder} or {@link CTabFolder}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public abstract class AbstractTabFolderInfo extends CompositeInfo {
	AbstractTabItemInfo m_selectedItem;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractTabFolderInfo(AstEditor editor,
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
	 * @return the selected {@link AbstractTabItemInfo}, may be <code>null</code>, if no items at all.
	 */
	public AbstractTabItemInfo getSelectedItem() {
		if (m_selectedItem != null) {
			return m_selectedItem;
		}
		List<AbstractTabItemInfo> items = getItems();
		return GenericsUtils.getFirstOrNull(items);
	}

	/**
	 * @return the {@link AbstractTabItemInfo} children.
	 */
	public List<AbstractTabItemInfo> getItems() {
		return getChildren(AbstractTabItemInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenTree() throws Exception {
			List<ObjectInfo> children = new ArrayList<>(super.getChildrenTree());
			// don't show in tree any Control's
			for (Iterator<ObjectInfo> I = children.iterator(); I.hasNext();) {
				ObjectInfo child = I.next();
				if (child instanceof ControlInfo) {
					I.remove();
				}
			}
			// OK, show these children
			return children;
		}

		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			List<ObjectInfo> children = super.getChildrenGraphical();
			// prepare Control of selected TabItem
			ControlInfo selectedItemControl;
			{
				AbstractTabItemInfo selectedItem = getSelectedItem();
				selectedItemControl = selectedItem != null ? selectedItem.getControl() : null;
			}
			// remove all Control's except of selected
			for (Iterator<ObjectInfo> I = children.iterator(); I.hasNext();) {
				ObjectInfo child = I.next();
				if (child instanceof ControlInfo && child != selectedItemControl) {
					I.remove();
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
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link AbstractTabItemInfo}.
	 */
	public void command_CREATE(AbstractTabItemInfo item, AbstractTabItemInfo nextItem)
			throws Exception {
		JavaInfoUtils.add(item, null, this, nextItem);
	}

	/**
	 * Moves {@link AbstractTabItemInfo}.
	 */
	public void command_MOVE(AbstractTabItemInfo item, AbstractTabItemInfo nextItem) throws Exception {
		JavaInfoUtils.move(item, null, this, nextItem);
	}

	/**
	 * Creates new {@link ControlInfo}.
	 */
	public void command_CREATE(ControlInfo control, AbstractTabItemInfo nextItem) throws Exception {
		AbstractTabItemInfo item = createItem(nextItem);
		item.command_CREATE(control);
	}

	/**
	 * Moves {@link ControlInfo}.
	 */
	public void command_MOVE(ControlInfo control, AbstractTabItemInfo nextItem) throws Exception {
		AbstractTabItemInfo item = createItem(nextItem);
		item.command_ADD(control);
	}

	/**
	 * Adds new {@link AbstractTabItemInfo} to this {@link AbstractTabFolderInfo}.
	 */
	private AbstractTabItemInfo createItem(AbstractTabItemInfo nextItem) throws Exception {
		AbstractTabItemInfo item =
				(AbstractTabItemInfo) JavaInfoUtils.createJavaInfo(
						getEditor(),
						getItemClassName(),
						new ConstructorCreationSupport());
		command_CREATE(item, nextItem);
		return item;
	}

	protected abstract String getItemClassName();
}
