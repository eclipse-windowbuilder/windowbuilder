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

import org.eclipse.swt.widgets.Composite;

import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets <code>null</code> {@link Border}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class NoBorderComposite extends AbstractBorderComposite {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NoBorderComposite(Composite parent) {
		super(parent, "(no border)");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean setBorder(Border border) throws Exception {
		return border == null;
	}

	@Override
	public String getSource() throws Exception {
		return "null";
	}
}
