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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.utils.IAdaptableFactory;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link IAdaptableFactory} for children of {@link MenuManagerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class MenuManagerAdaptableFactory implements IAdaptableFactory {
	private static final String KEY_MENU_ITEM_OBJECT = "KEY_MENU_ITEM_OBJECT";

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptableFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public <T> T getAdapter(Object object, Class<T> adapter) {
		if (adapter.isAssignableFrom(IMenuItemInfo.class) && object instanceof ContributionItemInfo) {
			ContributionItemInfo item = (ContributionItemInfo) object;
			IMenuItemInfo itemObject = (IMenuItemInfo) item.getArbitraryValue(KEY_MENU_ITEM_OBJECT);
			if (itemObject == null) {
				itemObject = new ContributionItemImpl(item);
				item.putArbitraryValue(KEY_MENU_ITEM_OBJECT, itemObject);
			}
			return adapter.cast(itemObject);
		}
		// can not adapt
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuItemInfo for some ContributionItemInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuItemInfo} for {@link ContributionItemInfo}.
	 *
	 * @author scheglov_ke
	 */
	private static final class ContributionItemImpl extends AbstractMenuObject
	implements
	IMenuItemInfo {
		private final ContributionItemInfo m_item;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ContributionItemImpl(ContributionItemInfo item) {
			super(item);
			m_item = item;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return m_item;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Image getImage() {
			return m_item.getImage();
		}

		@Override
		public Rectangle getBounds() {
			return m_item.getBounds();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IMenuItemInfo
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuInfo getMenu() {
			return null;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Policy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuPolicy getPolicy() {
			return IMenuPolicy.NOOP;
		}
	}
}
