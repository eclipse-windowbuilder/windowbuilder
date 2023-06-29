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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.actions;

import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang.ObjectUtils;

/**
 * {@link Action} for modifying alignment of {@link TableWrapDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class SetAlignmentAction<C extends IControlInfo> extends DimensionHeaderAction<C> {
	private final int m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentAction(DimensionHeaderEditPart<C> header,
			String text,
			ImageDescriptor imageDescriptor,
			int alignment) {
		super(header, text, imageDescriptor, AS_RADIO_BUTTON);
		m_alignment = alignment;
		setChecked(ObjectUtils.equals(header.getDimension().getAlignment(), m_alignment));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(TableWrapDimensionInfo<C> dimension) throws Exception {
		dimension.setAlignment(m_alignment);
	}
}