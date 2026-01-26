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
package org.eclipse.wb.internal.rcp.gef.part.jface.action;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionItemInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ContributionItemInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public class ContributionItemEditPart extends AbstractComponentEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContributionItemEditPart(ContributionItemInfo item) {
		super(item);
	}
}
