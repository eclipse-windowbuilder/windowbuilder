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
package org.eclipse.wb.internal.swing.gef;

import org.eclipse.wb.core.gef.policy.validator.AbstractModelClassLayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;

/**
 * Implementation of {@link ILayoutRequestValidator} for ignore all menu objects.
 *
 * @author lobas_av
 * @coverage swing.gef
 */
public final class MenuLayoutRequestValidator extends AbstractModelClassLayoutRequestValidator {
	public static final ILayoutRequestValidator INSTANCE = new MenuLayoutRequestValidator();

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isValidClass(Class<?> clazz) {
		// JMenu-related things usually should not be added on "normal" layouts
		if (JMenuBarInfo.class.isAssignableFrom(clazz)
				|| JPopupMenuInfo.class.isAssignableFrom(clazz)
				|| JMenuItemInfo.class.isAssignableFrom(clazz)) {
			return false;
		}
		// OK, check by type
		return true;
	}
}