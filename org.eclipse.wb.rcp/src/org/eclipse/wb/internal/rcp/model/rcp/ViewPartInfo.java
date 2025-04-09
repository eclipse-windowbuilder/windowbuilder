/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;

import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Model for {@link ViewPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ViewPartInfo extends ViewPartLikeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewPartInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		contributeExtensionProperty();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private void contributeExtensionProperty() throws Exception {
		new ExtensionPropertyHelper(this, "org.eclipse.ui.views", "view") {
			@Override
			protected Property[] createProperties() {
				return new Property[]{
						createStringProperty("name"),
						createIconProperty("icon"),
						createAttributeProperty(ViewCategoryPropertyEditor.INSTANCE, "category")};
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getGUIMethodName() {
		return "createPartControl";
	}

	@Override
	protected void configureTabItem(CTabItem tabItem) throws Exception {
		configureTabItem_fromExtension(tabItem, "ViewPart");
	}

	/**
	 * Initializes this {@link ViewPart} with {@link IViewSite}.
	 */
	@Override
	protected void applyActionBars() throws Exception {
		ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
		Class<?> viewSiteClass = editorLoader.loadClass("org.eclipse.ui.IViewSite");
		// create IViewSite
		Object viewSite =
				Proxy.newProxyInstance(
						editorLoader,
						new Class<?>[]{viewSiteClass},
						new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								String signature = ReflectionUtils.getMethodSignature(method);
								if (signature.equals("toString()")) {
									return "IViewSite_stub";
								}
								if (signature.equals("hashCode()")) {
									return 0;
								}
								if (signature.equals("getActionBars()")) {
									return m_actionBars;
								}
								if (signature.equals("getId()")) {
									return getID();
								}
								if (signature.equals("getSecondaryId()")) {
									return null;
								}
								if (signature.equals("getWorkbenchWindow()")) {
									return DesignerPlugin.getActiveWorkbenchWindow();
								}
								throw new NotImplementedException(method.toString());
							}
						});
		// call init(IViewSite)
		ReflectionUtils.invokeMethod(getObject(), "init(org.eclipse.ui.IViewSite)", viewSite);
	}
}
