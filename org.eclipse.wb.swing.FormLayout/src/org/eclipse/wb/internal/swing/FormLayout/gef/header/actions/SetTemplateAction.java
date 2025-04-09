/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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