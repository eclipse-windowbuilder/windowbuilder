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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import java.util.function.Supplier;

/**
 * Abstract editor for some {@link SpinnerModelValue} type.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
abstract class AbstractSpinnerComposite extends Composite {
	protected static final Color COLOR_VALID = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	protected static final Color COLOR_INVALID = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	protected final SpinnerModelDialog m_modelDialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractSpinnerComposite(Composite parent, SpinnerModelDialog modelDialog) {
		super(parent, SWT.NONE);
		m_modelDialog = modelDialog;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the title to display.
	 */
	public abstract String getTitle();

	/**
	 * Sets the {@link SpinnerModelValue} to display/edit. <b>Important:</b> This
	 * method is called from the AWT event dispatch thread.
	 *
	 * @return <code>true</code> if this {@link AbstractSpinnerComposite}
	 *         understands given model.
	 */
	public abstract boolean setModelValue(SpinnerModelValue modelValue);

	/**
	 * @return the error message, or <code>null</code> if model configured correctly.
	 */
	public abstract String validate();

	/**
	 * This method is called from the SWT UI thread. But the returned
	 * {@link Supplier} must be called from the AWT event dispatch thread.
	 *
	 * @return the {@link SpinnerModelValue} that corresponds to this
	 *         {@link AbstractSpinnerComposite} and configuration. This
	 *         {@link SpinnerModelValue} is used later for preview in
	 *         {@link javax.swing.JSpinner}.
	 */
	public abstract Supplier<SpinnerModelValue> getModelValue();

	/**
	 * @return the source to apply.
	 */
	public abstract String getSource() throws Exception;
}
