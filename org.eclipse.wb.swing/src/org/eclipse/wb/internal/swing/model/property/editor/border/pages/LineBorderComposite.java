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
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BooleanField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link LineBorder}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class LineBorderComposite extends AbstractBorderComposite {
	private final ColorField m_colorField;
	private final IntegerField m_thicknessField;
	private final BooleanField m_typeField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LineBorderComposite(Composite parent) {
		super(parent, "LineBorder");
		GridLayoutFactory.create(this);
		m_colorField = createColorField(ModelMessages.LineBorderComposite_color);
		m_thicknessField = createIntegerField(ModelMessages.LineBorderComposite_thinkness);
		m_typeField =
				createBooleanField(ModelMessages.LineBorderComposite_corners, new String[]{
						ModelMessages.LineBorderComposite_cornersSquare,
						ModelMessages.LineBorderComposite_cornersRounded});
		// set defaults values
		m_colorField.setValue(Color.BLACK);
		m_thicknessField.setValue(1);
		m_typeField.setValue(false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		if (borderValue.getValue() instanceof LineBorder ourBorder) {
			Color lineColor = ourBorder.getLineColor();
			int thickness = ourBorder.getThickness();
			boolean roundedCorners = ourBorder.getRoundedCorners();
			// OK, this is our Border
			return ExecutionUtils.runLogLater(() -> {
				m_colorField.setValue(lineColor);
				m_thicknessField.setValue(thickness);
				m_typeField.setValue(roundedCorners);
			});
		}
		// no, we don't know this Border
		return null;
	}

	static {
		COMPOSITE_CLASSES.put(LineBorderComposite.class, LineBorder.class::isAssignableFrom);
	}

	@Override
	public String getSource() {
		String colorSource = m_colorField.getSource();
		String thinknessSource = m_thicknessField.getSource();
		String cornersSource = m_typeField.getSource();
		if ("false".equals(cornersSource)) {
			if ("1".equals(thinknessSource)) {
				return "new javax.swing.border.LineBorder(" + colorSource + ")";
			}
			return "new javax.swing.border.LineBorder(" + colorSource + ", " + thinknessSource + ")";
		}
		return "new javax.swing.border.LineBorder(" + colorSource + ", " + thinknessSource + ", true)";
	}
}
