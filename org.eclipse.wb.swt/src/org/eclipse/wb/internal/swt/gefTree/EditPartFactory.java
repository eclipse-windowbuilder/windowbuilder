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
package org.eclipse.wb.internal.swt.gefTree;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartFactory;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * Implementation of {@link IEditPartFactory} for SWT.
 *
 * @author scheglov_ke
 * @coverage swt.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
	private final static IEditPartFactory MATCHING_FACTORY =
			new MatchingEditPartFactory(List.of("org.eclipse.wb.internal.swt.model.widgets"),
					List.of("org.eclipse.wb.internal.swt.gefTree.part"));

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		// most EditPart's can be created using matching
		return MATCHING_FACTORY.createEditPart(context, model);
	}
}