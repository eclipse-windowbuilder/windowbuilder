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
import javax.swing.border.EtchedBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link EtchedBorder}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class EtchedBorderComposite extends AbstractBorderComposite {
	private final RadioField m_typeField;
	private final ColorField m_highlightField;
	private final ColorField m_shadowField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EtchedBorderComposite(Composite parent) {
		super(parent, "EtchedBorder");
		GridLayoutFactory.create(this);
		m_typeField =
				createRadioField(
						ModelMessages.EtchedBorderComposite_etchType,
						EtchedBorder.class,
						new String[]{"LOWERED", "RAISED"},
						new String[]{
								ModelMessages.EtchedBorderComposite_etchLowered,
								ModelMessages.EtchedBorderComposite_etchRaised});
		m_highlightField = createColorField(ModelMessages.EtchedBorderComposite_highlightColor);
		m_shadowField = createColorField(ModelMessages.EtchedBorderComposite_shadowColor);
		// set defaults values
		ExecutionUtils.runRethrow(() -> m_typeField.setValue(EtchedBorder.LOWERED));
	}

	static {
		COMPOSITE_CLASSES.put(EtchedBorderComposite.class, EtchedBorder.class::isAssignableFrom);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		if (borderValue.getValue() instanceof EtchedBorder ourBorder) {
			int etchType = ourBorder.getEtchType();
			Color highlightColor = ourBorder.getHighlightColor();
			Color shadowColor = ourBorder.getShadowColor();
			// OK, this is our Border
			return ExecutionUtils.runLogLater(() -> {
				m_typeField.setValue(etchType);
				m_highlightField.setValue(highlightColor);
				m_shadowField.setValue(shadowColor);
			});
		}
		// no, we don't know this Border
		return null;
	}

	@Override
	public String getSource() {
		String typeSource = m_typeField.getSource();
		String highlightSource = m_highlightField.getSource();
		String shadowSource = m_shadowField.getSource();
		if (highlightSource == null && shadowSource == null) {
			return "new javax.swing.border.EtchedBorder(" + typeSource + ")";
		}
		return "new javax.swing.border.EtchedBorder("
		+ typeSource
		+ ", "
		+ highlightSource
		+ ", "
		+ shadowSource
		+ ")";
	}
}
