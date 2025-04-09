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
package org.eclipse.wb.internal.rcp.gefTree.part.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.AbstractShortcutContainerInfo;

/**
 * {@link EditPart} for {@link AbstractShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
abstract class AbstractShortcutContainerEditPart extends ObjectEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractShortcutContainerEditPart(AbstractShortcutContainerInfo container) {
		super(container);
	}
}
