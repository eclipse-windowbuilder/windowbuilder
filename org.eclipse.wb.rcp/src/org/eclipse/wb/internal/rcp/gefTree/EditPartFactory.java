/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.gefTree;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.rcp.gefTree.part.forms.FormHeadEditPart;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * Implementation of {@link IEditPartFactory} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
	private final static IEditPartFactory MATCHING_FACTORY =
			new MatchingEditPartFactory(List.of("org.eclipse.wb.internal.rcp.model"),
					List.of("org.eclipse.wb.internal.rcp.gefTree.part"));

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		// special Composite's
		if (model instanceof CompositeInfo composite) {
			// Form.getHead()
			if (composite.getParent() instanceof FormInfo) {
				FormInfo form = (FormInfo) composite.getParent();
				if (form.getHead() == composite) {
					return new FormHeadEditPart(form);
				}
			}
		}
		// most EditPart's can be created using matching
		return MATCHING_FACTORY.createEditPart(context, model);
	}
}