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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.widgets.Composite;

import java.awt.FlowLayout;

/**
 * Model for {@link FlowLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class FlowLayoutInfo extends GenericFlowLayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FlowLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		new LayoutAssistantSupport(this) {
			@Override
			protected AbstractAssistantPage createLayoutPage(Composite parent) {
				return new FlowLayoutAssistantPage(parent, m_layout);
			}
		};
	}
}
