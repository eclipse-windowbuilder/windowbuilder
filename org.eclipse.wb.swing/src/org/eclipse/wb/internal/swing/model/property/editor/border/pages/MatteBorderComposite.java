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
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import java.awt.Color;
import java.awt.Insets;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link MatteBorder}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class MatteBorderComposite extends AbstractBorderComposite {
	private final ColorField m_colorField;
	private final IntegerField m_topField;
	private final IntegerField m_leftField;
	private final IntegerField m_bottomField;
	private final IntegerField m_rightField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MatteBorderComposite(Composite parent) {
		super(parent, "MatteBorder");
		GridLayoutFactory.create(this);
		m_colorField = createColorField(ModelMessages.MatteBorderComposite_color);
		m_topField = createIntegerField(ModelMessages.MatteBorderComposite_top);
		m_leftField = createIntegerField(ModelMessages.MatteBorderComposite_left);
		m_bottomField = createIntegerField(ModelMessages.MatteBorderComposite_bottom);
		m_rightField = createIntegerField(ModelMessages.MatteBorderComposite_right);
		// set defaults values
		m_colorField.setValue(Color.BLACK);
		m_topField.setValue(1);
		m_leftField.setValue(1);
		m_bottomField.setValue(1);
		m_rightField.setValue(1);
	}

	static {
		COMPOSITE_CLASSES.put(MatteBorderComposite.class, MatteBorder.class::isAssignableFrom);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		if (borderValue.getValue() instanceof MatteBorder ourBorder) {
			Insets borderInsets = ourBorder.getBorderInsets();
			Color matteColor = ourBorder.getMatteColor();
			// OK, this is our Border
			return ExecutionUtils.runLogLater(() -> {
				m_colorField.setValue(matteColor);
				m_topField.setValue(borderInsets.top);
				m_leftField.setValue(borderInsets.left);
				m_bottomField.setValue(borderInsets.bottom);
				m_rightField.setValue(borderInsets.right);
			});
		}
		// no, we don't know this Border
		return null;
	}

	@Override
	public String getSource() {
		return "new javax.swing.border.MatteBorder("
				+ m_topField.getSource()
				+ ", "
				+ m_leftField.getSource()
				+ ", "
				+ m_bottomField.getSource()
				+ ", "
				+ m_rightField.getSource()
				+ ", (java.awt.Color) "
				+ m_colorField.getSource()
				+ ")";
	}
}
