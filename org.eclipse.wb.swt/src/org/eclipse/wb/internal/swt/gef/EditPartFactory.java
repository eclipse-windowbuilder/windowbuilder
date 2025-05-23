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
package org.eclipse.wb.internal.swt.gef;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuItemInfo;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * Implementation of {@link IEditPartFactory} for SWT.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage swt.gef
 */
public final class EditPartFactory implements IEditPartFactory {
	private final static IEditPartFactory MATCHING_FACTORY =
			new MatchingEditPartFactory(List.of(
					"org.eclipse.wb.internal.swt.model.widgets",
					"org.eclipse.wb.internal.swt.model.jface.viewer"), List.of(
							"org.eclipse.wb.internal.swt.gef.part",
							"org.eclipse.wb.internal.swt.gef.part"));

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		// menu
		{
			if (model instanceof MenuInfo menu) {
				if (menu.isPopup()) {
					IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(menu);
					return MenuEditPartFactory.createPopupMenu(menu, popupObject);
				} else {
					IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
					return createMenuEditPart(menu, menuObject);
				}
			}
			if (model instanceof MenuItemInfo item) {
				IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
				return MenuEditPartFactory.createMenuItem(item, itemObject);
			}
		}
		// most EditPart's can be created using matching
		return MATCHING_FACTORY.createEditPart(context, model);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private org.eclipse.wb.gef.core.EditPart createMenuEditPart(Object model, IMenuInfo menuInfo) {
		return EnvironmentUtils.IS_MAC
				? MenuEditPartFactory.createMenuMac(model, menuInfo)
						: MenuEditPartFactory.createMenu(model, menuInfo);
	}
}