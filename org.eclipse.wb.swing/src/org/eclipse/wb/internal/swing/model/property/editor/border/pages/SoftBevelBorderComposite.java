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

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
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
		// set defaults values
		ExecutionUtils.runRethrow(() -> m_typeField.setValue(BevelBorder.LOWERED));
	}

	static {
		COMPOSITE_CLASSES.put(SoftBevelBorderComposite.class, SoftBevelBorder.class::isAssignableFrom);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		if (borderValue.getValue() instanceof SoftBevelBorder ourBorder) {
			int bevelType = ourBorder.getBevelType();
			Color highlightOuterColor = ourBorder.getHighlightOuterColor();
			Color highlightInnerColor = ourBorder.getHighlightInnerColor();
			Color shadowOuterColor = ourBorder.getShadowOuterColor();
			Color shadowInnerColor = ourBorder.getShadowInnerColor();
			// OK, this is our Border
			return ExecutionUtils.runLogLater(() -> {
				m_typeField.setValue(bevelType);
				m_highlightOuterField.setValue(highlightOuterColor);
				m_highlightInnerField.setValue(highlightInnerColor);
				m_shadowOuterField.setValue(shadowOuterColor);
				m_shadowInnerField.setValue(shadowInnerColor);
			});
		}
		// no, we don't know this Border
		return null;
	}

	@Override
	public String getSource() {
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
}
