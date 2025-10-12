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
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import java.awt.Insets;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link EmptyBorder}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class EmptyBorderComposite extends AbstractBorderComposite {
	private final IntegerField m_topField;
	private final IntegerField m_leftField;
	private final IntegerField m_bottomField;
	private final IntegerField m_rightField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EmptyBorderComposite(Composite parent) {
		super(parent, "EmptyBorder");
		GridLayoutFactory.create(this);
		m_topField = createIntegerField(ModelMessages.EmptyBorderComposite_top);
		m_leftField = createIntegerField(ModelMessages.EmptyBorderComposite_left);
		m_bottomField = createIntegerField(ModelMessages.EmptyBorderComposite_bottom);
		m_rightField = createIntegerField(ModelMessages.EmptyBorderComposite_right);
		// set defaults values
		m_topField.setValue(0);
		m_leftField.setValue(0);
		m_bottomField.setValue(0);
		m_rightField.setValue(0);
	}

	static {
		// Check for identity because EmptyBorder is sub-classed by MatteBorder
		COMPOSITE_CLASSES.put(EmptyBorderComposite.class, EmptyBorderComposite::contains);
	}

	/**
	 * @return {@code true}, if this composite can manage the given border.
	 */
	private static boolean contains(Class<?> border) {
		if (MatteBorder.class.isAssignableFrom(border)) {
			return false;
		}
		return EmptyBorder.class.isAssignableFrom(border);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		if (borderValue.getValue() != null && borderValue.getValue().getClass() == EmptyBorder.class) {
			EmptyBorder ourBorder = (EmptyBorder) borderValue.getValue();
			Insets borderInsets = ourBorder.getBorderInsets();
			// OK, this is our Border
			return ExecutionUtils.runLogLater(() -> {
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
		return "new javax.swing.border.EmptyBorder("
				+ m_topField.getSource()
				+ ", "
				+ m_leftField.getSource()
				+ ", "
				+ m_bottomField.getSource()
				+ ", "
				+ m_rightField.getSource()
				+ ")";
	}
}
