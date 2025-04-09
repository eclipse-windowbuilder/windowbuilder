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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;

import javax.swing.JMenuItem;

/**
 * Model for {@link JMenuItem}.
 *
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public class JMenuItemInfo extends ContainerInfo implements IAdaptable {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JMenuItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	private final IMenuItemInfo m_itemImpl = new MenuItemImpl();

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
			return adapter.cast(m_itemImpl);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		// item bounds fetches by using SwingImageUtils.fetchMenuVisualData()
		if (getParent() instanceof JMenuInfo || getParent() instanceof JPopupMenuInfo) {
			refresh_fetch_super();
			return;
		}
		// other container
		super.refresh_fetch();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuItemInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuItemInfo}.
	 *
	 * @author scheglov_ke
	 */
	private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public MenuItemImpl() {
			super(JMenuItemInfo.this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return JMenuItemInfo.this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public Rectangle getBounds() {
			return JMenuItemInfo.this.getBounds();
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
