/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.AbstractShortcutInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link AbstractShortcutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class AbstractShortcutEditPart extends AbstractComponentEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractShortcutEditPart(AbstractShortcutInfo shortcut) {
		super(shortcut);
	}
}
