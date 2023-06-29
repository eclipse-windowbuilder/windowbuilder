/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.swt.widgets.Composite;

/**
 * Layout assistant for {@link org.eclipse.swt.custom.StackLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public final class StackLayoutAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StackLayoutAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(1);
		addIntegerProperty(this, "marginWidth", ModelMessages.StackLayoutAssistantPage_marginWidth);
		addIntegerProperty(this, "marginHeight", ModelMessages.StackLayoutAssistantPage_marginHeight);
	}
}