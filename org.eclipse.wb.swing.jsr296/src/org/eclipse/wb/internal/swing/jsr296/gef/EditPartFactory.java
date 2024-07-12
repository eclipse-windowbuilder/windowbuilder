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
package org.eclipse.wb.internal.swing.jsr296.gef;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartFactory;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * {@link IEditPartFactory} for JSR-296.
 *
 * @author scheglov_ke
 * @coverage swing.jsr296
 */
public final class EditPartFactory implements IEditPartFactory {
	private final static IEditPartFactory MATCHING_FACTORY =
			new MatchingEditPartFactory(List.of("org.eclipse.wb.internal.swing.jsr296.model"),
					List.of("org.eclipse.wb.internal.swing.jsr296.gef"));

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		return MATCHING_FACTORY.createEditPart(context, model);
	}
}