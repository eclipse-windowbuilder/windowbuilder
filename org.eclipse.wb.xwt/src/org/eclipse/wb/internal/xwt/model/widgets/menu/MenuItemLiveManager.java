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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.xwt.model.widgets.XwtLiveManager;

/**
 * Special {@link XwtLiveManager} for {@link MenuItemInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class MenuItemLiveManager extends XwtLiveManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuItemLiveManager(MenuItemInfo item) {
		super(item);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LiveComponentsManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addWidget(CompositeInfo shell, WidgetInfo widget) throws Exception {
		XmlObjectInfo menu =
				XmlObjectUtils.createObject(
						widget.getContext(),
						"org.eclipse.swt.widgets.Menu",
						new ElementCreationSupport());
		XmlObjectUtils.add(menu, Associations.property("data"), shell, null);
		XmlObjectUtils.add(widget, Associations.direct(), menu, null);
	}
}
