/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;

import org.eclipse.swt.widgets.Composite;

import java.awt.Color;

import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link SoftBevelBorder}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class SoftBevelBorderComposite extends AbstractBorderComposite {
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
	public SoftBevelBorderComposite(Composite parent) {
		super(parent, "SoftBevelBorder");
		GridLayoutFactory.create(this);
		m_typeField =
				createRadioField(
						ModelMessages.SoftBevelBorderComposite_bevelType,
						BevelBorder.class,
						new String[]{"LOWERED", "RAISED"},
						new String[]{
								ModelMessages.SoftBevelBorderComposite_bevelLowered,
								ModelMessages.SoftBevelBorderComposite_bevelRaised});
		m_highlightOuterField =
				createColorField(ModelMessages.SoftBevelBorderComposite_highlightOuterColor);
		m_highlightInnerField =
				createColorField(ModelMessages.SoftBevelBorderComposite_highlightInnerColor);
		m_shadowOuterField = createColorField(ModelMessages.SoftBevelBorderComposite_shadowOuterColor);
		m_shadowInnerField = createColorField(ModelMessages.SoftBevelBorderComposite_shadowInnerColor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean setBorderValue(BorderValue border) throws Exception {
		if (border instanceof SoftBevelBorderValue ourBorder) {
			m_typeField.setValue(ourBorder.bevelType);
			m_highlightOuterField.setValue(ourBorder.highlightOuterColor);
			m_highlightInnerField.setValue(ourBorder.highlightInnerColor);
			m_shadowOuterField.setValue(ourBorder.shadowOuterColor);
			m_shadowInnerField.setValue(ourBorder.shadowInnerColor);
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
			return "new javax.swing.border.SoftBevelBorder(" + typeSource + ")";
		}
		if (highlightOuterSource == null
				&& highlightInnerSource != null
				&& shadowOuterSource != null
				&& shadowInnerSource == null) {
			return "new javax.swing.border.SoftBevelBorder("
					+ typeSource
					+ ", "
					+ highlightInnerSource
					+ ", "
					+ shadowOuterSource
					+ ")";
		}
		return "new javax.swing.border.SoftBevelBorder("
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

	/**
	 * Wrapper for {@link SoftBevelBorder}.
	 */
	public static class SoftBevelBorderValue extends BorderValue {
		private final int bevelType;
		private final Color highlightOuterColor;
		private final Color highlightInnerColor;
		private final Color shadowOuterColor;
		private final Color shadowInnerColor;

		public SoftBevelBorderValue(SoftBevelBorder border) {
			super(border);
			bevelType = border.getBevelType();
			highlightOuterColor = border.getHighlightOuterColor();
			highlightInnerColor = border.getHighlightInnerColor();
			shadowOuterColor = border.getShadowOuterColor();
			shadowInnerColor = border.getShadowInnerColor();
		}
	}
}
