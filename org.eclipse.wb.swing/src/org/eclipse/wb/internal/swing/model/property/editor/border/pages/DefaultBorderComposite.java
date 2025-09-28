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

import org.eclipse.swt.widgets.Composite;

import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that does invoke
 * {@link JComponent#setBorder(javax.swing.border.Border)}, so does not change default
 * {@link Border}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class DefaultBorderComposite extends AbstractBorderComposite {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultBorderComposite(Composite parent) {
		super(parent, "(default)");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean setBorder(Border border) throws Exception {
		return false;
	}

	@Override
	public String getSource() {
		return null;
	}
}
