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
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ItemInfo}.
 *
 * @author lobas_av
 * @coverage swt.gef.part
 */
public class ItemEditPart extends AbstractComponentEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ItemEditPart(ItemInfo item) {
		super(item);
	}
}