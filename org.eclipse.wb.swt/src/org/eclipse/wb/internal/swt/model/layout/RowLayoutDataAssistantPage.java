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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.RowData}.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class RowLayoutDataAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowLayoutDataAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(2);
		// Width & Height
		{
			Group group =
					addIntegerProperties(
							this,
							ModelMessages.RowLayoutDataAssistantPage_sizeGroup,
							new String[][]{
								{"width", ModelMessages.RowLayoutDataAssistantPage_sizeWidth},
								{"height", ModelMessages.RowLayoutDataAssistantPage_sizeHeight}},
							new int[]{SWT.DEFAULT, SWT.DEFAULT});
			GridDataFactory.create(group).fillV();
		}
		// Exclude
		{
			addBooleanProperty(this, "exclude", ModelMessages.RowLayoutDataAssistantPage_excludeFlag);
		}
	}
}