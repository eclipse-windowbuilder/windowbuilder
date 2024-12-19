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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerPopupInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.ui.forms.widgets.Form;

import java.util.List;

/**
 * Model for {@link Form}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormInfo extends CompositeInfo {
	private ToolBarManagerInfo m_toolBarManager;
	private MenuManagerInfo m_menuManager;
	private MenuManagerPopupInfo m_menuManagerPopup;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// remove setHeadClient() invocation when move out "head client" ControlInfo
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				if (child instanceof ControlInfo && newParent != oldParent && oldParent == getHead()) {
					removeMethodInvocations("setHeadClient(org.eclipse.swt.widgets.Control)");
				}
			}
		});
	}

	@Override
	public Form getWidget() {
		return (Form) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createExposedChildren() throws Exception {
		m_toolBarManager =
				(ToolBarManagerInfo) JavaInfoUtils.addChildExposedByMethod(this, "getToolBarManager");
		m_menuManager = (MenuManagerInfo) JavaInfoUtils.addChildExposedByMethod(this, "getMenuManager");
		m_menuManagerPopup = new MenuManagerPopupInfo(m_menuManager);
		super.createExposedChildren();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exposed head/body/managers support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the exposed {@link ToolBarManagerInfo}.
	 */
	public ToolBarManagerInfo getToolBarManager() {
		return m_toolBarManager;
	}

	/**
	 * @return the exposed {@link MenuManagerInfo}.
	 */
	public MenuManagerInfo getMenuManager() {
		return m_menuManager;
	}

	/**
	 * @return the "head" {@link CompositeInfo}.
	 */
	public CompositeInfo getHead() {
		return getExposedComposite("getHead");
	}

	/**
	 * @return the "body" {@link CompositeInfo}.
	 */
	public CompositeInfo getBody() {
		return getExposedComposite("getBody");
	}

	/**
	 * @return the {@link CompositeInfo} exposed using method with given name.
	 */
	private CompositeInfo getExposedComposite(String methodName) {
		CompositeInfo exposedComposite = null;
		for (CompositeInfo child : getChildren(CompositeInfo.class)) {
			if (child.getCreationSupport() instanceof ExposedPropertyCreationSupport) {
				ExposedPropertyCreationSupport creationSupport =
						(ExposedPropertyCreationSupport) child.getCreationSupport();
				if (creationSupport.getMethod().getName().equals(methodName)) {
					exposedComposite = child;
					break;
				}
			}
		}
		Assert.isNotNull(exposedComposite);
		return exposedComposite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Head client
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "head client" {@link ControlInfo}, may be <code>null</code> if no "head client"
	 *         set.
	 */
	public ControlInfo getHeadClient() {
		CompositeInfo head = getHead();
		List<ControlInfo> controls = head.getChildrenControls();
		return !controls.isEmpty() ? controls.get(0) : null;
	}

	/**
	 * Sets new "head client" {@link ControlInfo}. No existing "head control" should exist.
	 */
	public void setHeadClient(ControlInfo control) throws Exception {
		Assert.isNull(getHeadClient());
		// create/move ControlInfo
		if (control.getParent() == null) {
			JavaInfoUtils.add(control, null, getHead(), null);
		} else {
			JavaInfoUtils.move(control, null, getHead(), null);
		}
		// add setHeadClient() invocation
		{
			String source = TemplateUtils.format("{0}.setHeadClient({1})", this, control);
			Expression setHeadClientExpression = control.addExpressionStatement(source);
			addRelatedNodes(setHeadClientExpression);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// fetch bounds of menu hyperlink
		{
			Object /* FormHeading */ headObject = getWidget().getHead();
			Object /* TitleRegion */ titleRegionObject = ReflectionUtils.getFieldObject(headObject, "titleRegion");
			Object /* MenuHyperlink */ menuHyperlinkObject =
					ReflectionUtils.getFieldObject(titleRegionObject, "menuHyperlink");
			Rectangle menuHyperlinkBounds = CoordinateUtils.getBounds(getObject(), menuHyperlinkObject);
			m_menuManagerPopup.setBounds(menuHyperlinkBounds);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuPopupInfo for MenuManager_Info
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IMenuPopupInfo} for {@link MenuManagerInfo} in this {@link FormInfo}. This
	 *         method is invoked with ready {@link MenuManagerInfo}, so we don't need to check it
	 *         {@link MenuManagerInfo} exists.
	 */
	public IMenuPopupInfo getMenuImpl(MenuManagerInfo manager) {
		Assert.isTrue(m_menuManager == manager);
		return m_menuManagerPopup;
	}
}
