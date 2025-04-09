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
package org.eclipse.wb.internal.rcp.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.swt.model.layout.LayoutAssistantSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Assistant pages provider for {@link StackLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public final class StackLayoutAssistant extends LayoutAssistantSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StackLayoutAssistant(LayoutInfo layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractAssistantPage createLayoutPage(Composite parent) {
		return new StackLayoutAssistantPage(parent, m_layout);
	}
}
