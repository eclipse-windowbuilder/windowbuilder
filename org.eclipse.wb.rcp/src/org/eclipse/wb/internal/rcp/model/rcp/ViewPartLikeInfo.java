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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerPopupInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;
import org.eclipse.wb.internal.swt.support.PointSupport;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import org.apache.commons.lang.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Model for {@link ViewPart}-like components.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public abstract class ViewPartLikeInfo extends WorkbenchPartLikeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewPartLikeInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	protected Object m_actionBars;
	private Object m_toolBarManager;
	private Object m_menuManager;
	private Composite m_managersComposite;
	private ToolItem m_menuToolItem;
	private Rectangle m_menuToolItemBounds;

	@Override
	public void render() throws Exception {
		prepareActionBars();
		applyActionBars();
		super.render();
		renderContributionManagers();
	}

	/**
	 * Prepares {@link IActionBars} instance, with {@link ToolBarManager} and {@link MenuManager}.
	 */
	private void prepareActionBars() throws Exception {
		ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
		Class<?> toolBarManagerClass =
				editorLoader.loadClass("org.eclipse.jface.action.ToolBarManager");
		Class<?> menuManagerClass = editorLoader.loadClass("org.eclipse.jface.action.MenuManager");
		Class<?> actionBarsClass = editorLoader.loadClass("org.eclipse.ui.IActionBars");
		// create managers
		m_toolBarManager = toolBarManagerClass.newInstance();
		m_menuManager = menuManagerClass.newInstance();
		// create IActionBars
		m_actionBars =
				Proxy.newProxyInstance(
						editorLoader,
						new Class<?>[]{actionBarsClass},
						new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								if (ReflectionUtils.getMethodSignature(method).equals("getToolBarManager()")) {
									return m_toolBarManager;
								}
								if (ReflectionUtils.getMethodSignature(method).equals("getMenuManager()")) {
									return m_menuManager;
								}
								throw new NotImplementedException();
							}
						});
	}

	/**
	 * Initializes this object with prepared {@link IActionBars}.
	 */
	protected abstract void applyActionBars() throws Exception;

	/**
	 * Creates widgets for {@link ToolBarManager} and {@link MenuManager}.
	 */
	private void renderContributionManagers() throws Exception {
		m_managersComposite = new Composite(m_tabFolder, SWT.NONE);
		GridLayoutFactory.create(m_managersComposite).columns(2).noMargins().noSpacing();
		m_tabFolder.setTopRight(m_managersComposite);
		// create ToolBarManager widget
		{
			ReflectionUtils.invokeMethod(
					m_toolBarManager,
					"createControl(org.eclipse.swt.widgets.Composite)",
					m_managersComposite);
		}
		// create MenuManager widget
		{
			final Menu menu =
					(Menu) ReflectionUtils.invokeMethod(
							m_menuManager,
							"createContextMenu(org.eclipse.swt.widgets.Control)",
							m_tabFolder);
			// bind MenuManager
			final ToolBar menuToolBar = new ToolBar(m_managersComposite, SWT.FLAT | SWT.RIGHT);
			m_menuToolItem = new ToolItem(menuToolBar, SWT.PUSH);
			m_menuToolItem.setImage(Activator.getImage("view_menu.gif"));
			m_menuToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ExecutionUtils.runLog(new RunnableEx() {
						@Override
						public void run() throws Exception {
							org.eclipse.swt.graphics.Rectangle bounds = m_menuToolItem.getBounds();
							org.eclipse.swt.graphics.Point bottomLeft =
									new org.eclipse.swt.graphics.Point(bounds.x, bounds.y + bounds.height);
							bottomLeft = menuToolBar.toDisplay(bottomLeft);
							menu.setLocation(bottomLeft.x, bottomLeft.y);
							menu.setVisible(true);
						}
					});
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		// update tab item height, it should be big enough to show "toolComposite",
		// in other case SWT does not show "toolbar" at all
		{
			int toolHeight = m_managersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 2;
			m_tabFolder.setTabHeight(Math.max(m_tabFolder.getTabHeight(), toolHeight));
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// fetch bounds of menu drop-down
		{
			m_menuToolItemBounds = RectangleSupport.getRectangle(m_menuToolItem.getBounds());
			Point menuToolItemDisplayLocation =
					PointSupport.getPoint(m_menuToolItem.getParent().toDisplay(
							m_menuToolItemBounds.x,
							m_menuToolItemBounds.y));
			// convert into "shot"
			Point parentLocation = CoordinateUtils.getDisplayLocation(m_tabFolder);
			m_menuToolItemBounds.x = menuToolItemDisplayLocation.x - parentLocation.x;
			m_menuToolItemBounds.y = menuToolItemDisplayLocation.y - parentLocation.y;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuPopupInfo for MenuManager_Info
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IMenuPopupInfo} for {@link MenuManagerInfo} in this {@link ViewPartLikeInfo}
	 *         . This method is invoked with ready {@link MenuManagerInfo}, so we don't need to check
	 *         it {@link MenuManagerInfo} exists.
	 */
	public IMenuPopupInfo getMenuImpl(MenuManagerInfo manager) {
		MenuManagerPopupInfo popupInfo = new MenuManagerPopupInfo(manager);
		popupInfo.setBounds(m_menuToolItemBounds);
		return popupInfo;
	}
}
