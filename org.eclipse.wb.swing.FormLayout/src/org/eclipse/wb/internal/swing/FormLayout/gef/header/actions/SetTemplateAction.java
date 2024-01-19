/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionTemplate;

import org.eclipse.jface.action.Action;

import org.apache.commons.text.WordUtils;

/**
 * {@link Action} for specifying {@link FormDimensionInfo} as template.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class SetTemplateAction<T extends FormDimensionInfo> extends DimensionHeaderAction<T> {
	private final FormDimensionTemplate m_template;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetTemplateAction(DimensionHeaderEditPart<T> header, FormDimensionTemplate template) {
		super(header, WordUtils.capitalize(template.getTitle()), template.getIcon(), AS_RADIO_BUTTON);
		m_template = template;
		setChecked(header.getDimension().isTemplate(template));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(T dimension) throws Exception {
		if (isChecked()) {
			dimension.setTemplate(m_template);
		}
	}
}