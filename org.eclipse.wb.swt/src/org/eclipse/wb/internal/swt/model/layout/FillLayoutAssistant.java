/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Assistant pages provider for {@link FillLayout}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class FillLayoutAssistant extends LayoutAssistantSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FillLayoutAssistant(LayoutInfo layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractAssistantPage createLayoutPage(Composite parent) {
		return new FillLayoutAssistantPage(parent, m_layout);
	}
}
