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
package org.eclipse.wb.internal.swt.model.widgets.live.menu;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveManager;

/**
 * Special {@link SwtLiveManager} for SWT menu.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swt.model.widgets.live
 */
public class MenuLiveManager extends SwtLiveManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuLiveManager(AbstractComponentInfo component) {
		super(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LiveComponentsManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addWidget(CompositeInfo shell, WidgetInfo widget) throws Exception {
		JavaInfoUtils.add(widget, getAssociation_(widget), shell, null);
	}

	private static AssociationObject getAssociation_(WidgetInfo widget) throws Exception {
		InvocationChildAssociation asBar =
				new InvocationChildAssociation("%parent%.setMenuBar(%child%)");
		InvocationChildAssociation asSub = new InvocationChildAssociation("%parent%.setMenu(%child%)");
		Association association = new CompoundAssociation(asBar, asSub);
		return new AssociationObject(association, true);
	}
}
