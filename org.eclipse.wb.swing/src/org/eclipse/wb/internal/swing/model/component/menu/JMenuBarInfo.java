/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;

import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.LookAndFeel;

/**
 * Model for {@link JMenuBar}.
 *
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public final class JMenuBarInfo extends ContainerInfo implements IAdaptable {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JMenuBarInfo(AstEditor editor,
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
	 * @return the {@link JMenuInfo} children.
	 */
	public List<JMenuInfo> getChildrenMenus() {
		return getChildren(JMenuInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	private MenuVisualData m_visualData;

	/**
	 * @return <code>true</code> if {@link JMenuBar} has visible {@link JMenu} elements. Note, that
	 *         just checking {@link JMenuBar#getComponentCount()} if not enough, because some
	 *         {@link LookAndFeel}'s (for example Substance) add special components.
	 */
	private static boolean hasJMenuChildren(JMenuBar menuBar) {
		Component[] components = menuBar.getComponents();
		for (Component component : components) {
			if (component instanceof JMenu) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void refresh_afterCreate() throws Exception {
		// add text, if no "real" items
		{
			JMenuBar menuBar = (JMenuBar) getObject();
			if (!hasJMenuChildren(menuBar)) {
				menuBar.add(new JMenu(IMenuInfo.NO_ITEMS_TEXT));
			}
		}
		// continue
		super.refresh_afterCreate();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		AbstractComponentInfo parentInfo = (AbstractComponentInfo) getParent();
		Container parentContainer =
				parentInfo != null ? (Container) parentInfo.getComponentObject() : null;
		m_visualData = SwingImageUtils.fetchMenuVisualData(getContainer(), parentContainer);
		super.refresh_fetch();
		MenuUtils.setItemsBounds(m_visualData, getChildrenComponents());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds this {@link JMenuBarInfo} to given {@link ContainerInfo}, such as
	 * {@link JFrame}, {@link JDialog} and {@link javax.swing.JApplet JApplet}.
	 */
	public void command_CREATE(ContainerInfo container) throws Exception {
		AssociationObject association =
				AssociationObjects.invocationChild("%parent%.setJMenuBar(%child%)", true);
		JavaInfoUtils.addFirst(this, association, container);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	private final IMenuInfo m_menuImpl = new MenuImpl();
	private final IMenuPolicy m_menuPolicyImpl = new JMenuPolicyImpl(this);

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IMenuInfo.class)) {
			return adapter.cast(m_menuImpl);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractMenuImpl
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Abstract superclass for {@link IMenuObjectInfo} implementations.
	 *
	 * @author scheglov_ke
	 */
	private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
		public MenuAbstractImpl() {
			super(JMenuBarInfo.this);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuInfo}.
	 *
	 * @author scheglov_ke
	 */
	private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo {
		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return JMenuBarInfo.this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public ImageDescriptor getImageDescriptor() {
			if (m_visualData == null || m_visualData.m_menuImage == null) {
				return null;
			}
			return ImageDescriptor.createFromImage(m_visualData.m_menuImage);
		}

		@Override
		public Rectangle getBounds() {
			return m_visualData.m_menuBounds;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean isHorizontal() {
			return true;
		}

		@Override
		public List<IMenuItemInfo> getItems() {
			return MenuUtils.getItems(JMenuBarInfo.this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Policy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuPolicy getPolicy() {
			return m_menuPolicyImpl;
		}
	}
}
