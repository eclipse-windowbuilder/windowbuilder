/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;

import org.eclipse.swt.widgets.Composite;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link BevelBorder}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BevelBorderComposite extends AbstractBorderComposite {
	private final RadioField m_typeField;
	private final ColorField m_highlightOuterField;
	private final ColorField m_highlightInnerField;
	private final ColorField m_shadowOuterField;
	private final ColorField m_shadowInnerField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BevelBorderComposite(Composite parent) {
		super(parent, "BevelBorder");
		GridLayoutFactory.create(this);
		m_typeField =
				createRadioField(
						ModelMessages.BevelBorderComposite_bevelType,
						BevelBorder.class,
						new String[]{"LOWERED", "RAISED"},
						new String[]{
								ModelMessages.BevelBorderComposite_typeLowered,
								ModelMessages.BevelBorderComposite_typeRaised});
		m_highlightOuterField =
				createColorField(ModelMessages.BevelBorderComposite_highlightOuterColor);
		m_highlightInnerField =
				createColorField(ModelMessages.BevelBorderComposite_highlightInnerColor);
		m_shadowOuterField = createColorField(ModelMessages.BevelBorderComposite_shadowOuterColor);
		m_shadowInnerField = createColorField(ModelMessages.BevelBorderComposite_shadowInnerColor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean setBorder(Border border) throws Exception {
		if (border instanceof BevelBorder ourBorder) {
			m_typeField.setValue(ourBorder.getBevelType());
			m_highlightOuterField.setValue(ourBorder.getHighlightOuterColor());
			m_highlightInnerField.setValue(ourBorder.getHighlightInnerColor());
			m_shadowOuterField.setValue(ourBorder.getShadowOuterColor());
			m_shadowInnerField.setValue(ourBorder.getShadowInnerColor());
			// OK, this is our Border
			return true;
		} else {
			m_typeField.setValue(BevelBorder.LOWERED);
			// no, we don't know this Border
			return false;
		}
	}

	@Override
	public String getSource() throws Exception {
		String typeSource = m_typeField.getSource();
		String highlightOuterSource = m_highlightOuterField.getSource();
		String highlightInnerSource = m_highlightInnerField.getSource();
		String shadowOuterSource = m_shadowOuterField.getSource();
		String shadowInnerSource = m_shadowInnerField.getSource();
		if (highlightOuterSource == null
				&& highlightInnerSource == null
				&& shadowOuterSource == null
				&& shadowInnerSource == null) {
			return "new javax.swing.border.BevelBorder(" + typeSource + ")";
		}
		if (highlightOuterSource == null
				&& highlightInnerSource != null
				&& shadowOuterSource != null
				&& shadowInnerSource == null) {
			return "new javax.swing.border.BevelBorder("
					+ typeSource
					+ ", "
					+ highlightInnerSource
					+ ", "
					+ shadowOuterSource
					+ ")";
		}
		return "new javax.swing.border.BevelBorder("
		+ typeSource
		+ ", "
		+ highlightOuterSource
		+ ", "
		+ highlightInnerSource
		+ ", "
		+ shadowOuterSource
		+ ", "
		+ shadowInnerSource
		+ ")";
	}
}
