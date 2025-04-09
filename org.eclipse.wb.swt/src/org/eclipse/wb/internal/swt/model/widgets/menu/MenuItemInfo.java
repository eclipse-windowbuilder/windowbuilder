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
package org.eclipse.wb.internal.swt.model.widgets.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveManager;
import org.eclipse.wb.internal.swt.model.widgets.live.menu.MenuItemLiveManager;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class representing menu item model for SWT menu item object.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets.menu
 */
public final class MenuItemInfo extends ItemInfo implements IAdaptable {
	private final MenuItemInfo m_this = this;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroadcastListeners();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcast
	//
	////////////////////////////////////////////////////////////////////////////
	private void addBroadcastListeners() {
		addBroadcastListener(m_stylePropertyListener);
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
				// add child menu for item creation with SWT.CASCADE style
				if (child == m_this) {
					if (JavaInfoUtils.hasTrueParameter(m_this, "MenuItem.createCascadeMenu")) {
						addSubMenu();
					}
					removeBroadcastListener(this);
				}
			}
		});
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
					throws Exception {
				// copy sub-menu
				if (javaInfo == m_this) {
					if (hasSubMenu()) {
						MenuInfo menuInfo = getChildren(MenuInfo.class).get(0);
						final JavaInfoMemento menuMemento = JavaInfoMemento.createMemento(menuInfo);
						commands.add(new ClipboardCommand() {
							private static final long serialVersionUID = 0L;

							@Override
							public void execute(JavaInfo javaInfo) throws Exception {
								MenuInfo menu = (MenuInfo) menuMemento.create(javaInfo);
								menu.command_CREATE((MenuItemInfo) javaInfo);
								menuMemento.apply();
							}
						});
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Sub menu related
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the sub-menu associated with this {@link MenuItemInfo}, or <code>null</code> is it has
	 *         no sub-menu.
	 */
	public MenuInfo getSubMenu() {
		List<MenuInfo> subMenus = getChildren(MenuInfo.class);
		Assert.isLegal(subMenus.size() <= 1);
		return !subMenus.isEmpty() ? subMenus.get(0) : null;
	}

	/**
	 * @return <code>true</code> if this menu item contains sub menu.
	 */
	private boolean hasSubMenu() {
		return !getChildren(MenuInfo.class).isEmpty();
	}

	/**
	 * Adds a sub-menu to this item child. This required to items with SWT.CASCADE style set.
	 */
	private void addSubMenu() throws Exception {
		if (!hasSubMenu()) {
			JavaInfo menu =
					JavaInfoUtils.createJavaInfo(
							getEditor(),
							Menu.class,
							new ConstructorCreationSupport());
			AssociationObject association =
					AssociationObjects.invocationChild("%parent%.setMenu(%child%)", true);
			JavaInfoUtils.add(menu, association, this, null);
		}
	}

	/**
	 * Removes sub-menu of this item, if any.
	 */
	private void deleteSubMenu() throws Exception {
		if (hasSubMenu()) {
			getChildren(MenuInfo.class).get(0).delete();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new MenuItemStylePresentation(this);

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Style change listener
	//
	////////////////////////////////////////////////////////////////////////////
	private final JavaEventListener m_stylePropertyListener = new JavaEventListener() {
		@Override
		public void setPropertyExpression(final GenericPropertyImpl property,
				final String[] source,
				Object[] value,
				final boolean[] shouldSet) throws Exception {
			if (property.getJavaInfo() == m_this && "Style".equals(property.getTitle())) {
				final boolean wasCascade = ((Integer) property.getValue() & SWT.CASCADE) != 0;
				final boolean wasSeparator = ((Integer) property.getValue() & SWT.SEPARATOR) != 0;
				String src = source[0];
				final boolean becomesCascade = src != null && src.indexOf("SWT.CASCADE") != -1;
				final boolean becomesSeparator = src != null && src.indexOf("SWT.SEPARATOR") != -1;
				// do nothing if SWT.CASCADE/SEPARATOR neither set nor reset
				if (wasCascade == becomesCascade && wasSeparator == becomesSeparator) {
					return;
				}
				// OK, we have something to change
				ExecutionUtils.run(m_this, new RunnableEx() {
					@Override
					public void run() throws Exception {
						// add/remove subMenu
						if (becomesCascade) {
							addSubMenu();
						} else {
							deleteSubMenu();
						}
						// remove "setText" when setting SWT.SEPARATOR
						if (becomesSeparator) {
							getPropertyByTitle("text").setValue(Property.UNKNOWN_VALUE);
						}
					}
				});
			}
		}

		@Override
		public void addBefore(JavaInfo parent, JavaInfo child) throws Exception {
			// MenuInfo set for this MenuItemInfo, make it CASCADE
			if (parent == m_this && child instanceof MenuInfo) {
				setStyleSource("org.eclipse.swt.SWT.CASCADE");
			}
		}

		@Override
		public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
				throws Exception {
			// MenuInfo moved from this MenuItemInfo, remove CASCADE
			if (oldParent == m_this && child instanceof MenuInfo) {
				setStyleSource("org.eclipse.swt.SWT.NONE");
			}
			// MenuInfo moved to this MenuItemInfo, remove CASCADE
			if (newParent == m_this && child instanceof MenuInfo) {
				setStyleSource("org.eclipse.swt.SWT.CASCADE");
			}
		}

		/**
		 * Sets new value of "style" property.
		 */
		private void setStyleSource(String source) throws Exception {
			removeBroadcastListener(this);
			try {
				GenericProperty styleProperty = (GenericProperty) getPropertyByTitle("Style");
				styleProperty.setExpression(source, Property.UNKNOWN_VALUE);
			} finally {
				addBroadcastListener(this);
			}
		}
	};
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
	// IMenuItemInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuItemInfo}.
	 *
	 * @author scheglov_ke
	 */
	private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo, IMenuPolicy {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public MenuItemImpl() {
			super(m_this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return m_this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Optional.ofNullable(m_this.getImage()).map(ImageDescriptor::createFromImage).orElse(null);
		}

		@Override
		public Rectangle getBounds() {
			return m_this.getBounds();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IMenuItemInfo
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuInfo getMenu() {
			MenuInfo subMenu = getSubMenu();
			return MenuObjectInfoUtils.getMenuInfo(subMenu);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Policy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuPolicy getPolicy() {
			return this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Validation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean validateCreate(Object object) {
			// nothing can be dropped on MenuItem
			return false;
		}

		@Override
		public boolean validatePaste(Object mementoObject) {
			// nothing can be dropped on MenuItem
			return false;
		}

		@Override
		public boolean validateMove(Object object) {
			if (object instanceof MenuInfo menuInfo) {
				// don't move Menu on its child Item
				if (menuInfo.isParentOf(m_this)) {
					return false;
				}
				return true;
			}
			// not a MenuInfo
			return false;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Operations
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void commandCreate(Object object, Object nextObject) throws Exception {
		}

		@Override
		public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
			return Collections.emptyList();
		}

		@Override
		public void commandMove(Object object, Object nextObject) throws Exception {
			MenuInfo menuInfo = (MenuInfo) object;
			menuInfo.command_ADD(m_this);
			// schedule selection
			MenuObjectInfoUtils.setSelectingObject(menuInfo);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Live support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected SwtLiveManager getLiveComponentsManager() {
		return new MenuItemLiveManager(this);
	}
}
