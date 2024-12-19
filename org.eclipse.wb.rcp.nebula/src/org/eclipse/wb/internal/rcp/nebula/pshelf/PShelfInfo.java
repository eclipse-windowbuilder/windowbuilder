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
package org.eclipse.wb.internal.rcp.nebula.pshelf;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;

import java.util.List;

/**
 * Model for {@link PShelf}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class PShelfInfo extends CompositeInfo {
	private final PShelfInfo m_this = this;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PShelfInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		manageSelectedItem();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<PShelfItemInfo> getItems() {
		return getChildren(PShelfItemInfo.class);
	}

	@Override
	public PShelf getWidget() {
		return (PShelf) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	private PShelfItemInfo m_selectedItem;

	/**
	 * Updates {@link #m_selectedItem} on visible widget delete/move.
	 */
	private void manageSelectedItem() {
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
				if (child == m_selectedItem) {
					m_selectedItem = null;
				}
			}

			@Override
			public void selecting(ObjectInfo object, boolean[] refreshFlag) throws Exception {
				if (object != null && m_this.isParentOf(object)) {
					for (PShelfItemInfo item : getItems()) {
						if (item == object || item.isParentOf(object)) {
							if (m_selectedItem != item) {
								m_selectedItem = item;
								refreshFlag[0] = true;
							}
							break;
						}
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		showSelectedItem();
		super.refresh_afterCreate();
	}

	private void showSelectedItem() throws Exception {
		if (m_selectedItem == null) {
			List<PShelfItemInfo> items = getItems();
			if (!items.isEmpty()) {
				m_selectedItem = items.get(0);
			}
		}
		if (m_selectedItem != null) {
			getWidget().setSelection(m_selectedItem.getWidget());
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		setItemsBounds();
	}

	private void setItemsBounds() throws Exception {
		int y1 = 0;
		int y2 = 0;
		PShelfItem[] itemObjects = getWidget().getItems();
		for (PShelfItem itemObject : itemObjects) {
			y2 += getItemHeight(itemObject);
			setItemBounds(itemObject, y1, y2);
			y1 = y2;
		}
	}

	private void setItemBounds(PShelfItem itemObject, int y1, int y2) {
		for (PShelfItemInfo item : getItems()) {
			if (item.getObject() == itemObject) {
				Rectangle shelfBounds = getModelBounds();
				Rectangle itemBounds = new Rectangle(0, y1, shelfBounds.width, y2 - y1);
				item.setModelBounds(itemBounds);
			}
		}
	}

	private int getItemHeight(PShelfItem itemObject) throws Exception {
		int itemHeaderHeight = ReflectionUtils.getFieldInt(getObject(), "itemHeight");
		int height = itemHeaderHeight;
		if (m_selectedItem != null && itemObject == m_selectedItem.getObject()) {
			Object bodyParent = ReflectionUtils.invokeMethod(itemObject, "getBodyParent()");
			Rectangle bounds = ControlSupport.getBounds(bodyParent);
			height += bounds.height;
		}
		return height;
	}
}
