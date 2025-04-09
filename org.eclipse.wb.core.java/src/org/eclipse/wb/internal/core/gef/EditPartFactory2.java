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
package org.eclipse.wb.internal.core.gef;

import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.gef.part.AbstractWrapperEditPart;
import org.eclipse.wb.internal.core.gef.part.DesignRootEditPart;
import org.eclipse.wb.internal.core.gef.part.nonvisual.ArrayObjectEditPart;
import org.eclipse.wb.internal.core.model.DesignRootObject;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;

import org.eclipse.gef.EditPart;

/**
 * Generic implementation of {@link IEditPartFactory} that redirects {@link EditPart}'s creation to
 * {@link IEditPartFactory}'s from extension point.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class EditPartFactory2 implements IEditPartFactory {
	public static final EditPartFactory2 INSTANCE = new EditPartFactory2();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private EditPartFactory2() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		if (model == null) {
			return null;
		}
		// designer root
		if (model instanceof DesignRootObject designRootObject) {
			return new DesignRootEditPart(designRootObject);
		}
		// child array
		if (model instanceof AbstractArrayObjectInfo arrayInfo) {
			ArrayObjectEditPart editPart = new ArrayObjectEditPart(arrayInfo);
			EditPartFactory.configureEditPart(context, editPart);
			return editPart;
		}
		// IWrapperInfo
		if (model instanceof IWrapperInfo) {
			IWrapper wrapper = ((IWrapperInfo) model).getWrapper();
			AbstractWrapperEditPart editPart = new AbstractWrapperEditPart(wrapper);
			EditPartFactory.configureEditPart(context, editPart);
			return editPart;
		}
		// no EditPart found
		return null;
	}
}
