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
package org.eclipse.wb.internal.xwt.model.widgets.menu;

import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.xwt.model.widgets.XwtLiveManager;

/**
 * {@link XwtLiveManager} for {@link MenuInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class MenuLiveManager extends XwtLiveManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuLiveManager(MenuInfo menu) {
		super(menu);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LiveComponentsManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addWidget(CompositeInfo shell, WidgetInfo widget) throws Exception {
		XmlObjectUtils.add(widget, Associations.property("data"), shell, null);
	}
}
