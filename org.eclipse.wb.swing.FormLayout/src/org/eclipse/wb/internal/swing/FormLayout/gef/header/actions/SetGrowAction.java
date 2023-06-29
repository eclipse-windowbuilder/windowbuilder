/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.FormLayout.gef.header.actions;

import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.jgoodies.forms.layout.FormSpec;

/**
 * {@link Action} for modifying grow of {@link FormDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class SetGrowAction<T extends FormDimensionInfo> extends DimensionHeaderAction<T> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetGrowAction(DimensionHeaderEditPart<T> header,
			String text,
			ImageDescriptor imageDescriptor) {
		super(header, text, imageDescriptor, AS_CHECK_BOX);
		setChecked(header.getDimension().hasGrow());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(T dimension) throws Exception {
		double weight = dimension.hasGrow() ? FormSpec.NO_GROW : FormSpec.DEFAULT_GROW;
		dimension.setWeight(weight);
	}
}