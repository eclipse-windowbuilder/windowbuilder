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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * Layout assistant for {@link ColumnLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class ColumnLayoutAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnLayoutAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(2);
		// columns
		{
			Group columnsGroup =
					addIntegerProperties(
							this,
							ModelMessages.ColumnLayoutAssistantPage_columnsGroup,
							new String[][]{
								new String[]{"minNumColumns", ModelMessages.ColumnLayoutAssistantPage_minColumns},
								new String[]{"maxNumColumns", ModelMessages.ColumnLayoutAssistantPage_maxColumns},});
			GridDataFactory.create(columnsGroup).fill();
		}
		// spacing
		{
			Group spacingGroup =
					addIntegerProperties(
							this,
							ModelMessages.ColumnLayoutAssistantPage_spacingGroup,
							new String[][]{
								new String[]{
										"horizontalSpacing",
										ModelMessages.ColumnLayoutAssistantPage_spacingHorizontal},
								new String[]{
										"verticalSpacing",
										ModelMessages.ColumnLayoutAssistantPage_spacingVertical},});
			GridDataFactory.create(spacingGroup).fillV();
		}
		// margins for sides
		{
			Group marginsGroup =
					addIntegerProperties(
							this,
							ModelMessages.ColumnLayoutAssistantPage_marginsGroup,
							new String[][]{
								new String[]{"leftMargin", ModelMessages.ColumnLayoutAssistantPage_marginLeft},
								new String[]{"rightMargin", ModelMessages.ColumnLayoutAssistantPage_marginRight},
								new String[]{"topMargin", ModelMessages.ColumnLayoutAssistantPage_marginTop},
								new String[]{"bottomMargin", ModelMessages.ColumnLayoutAssistantPage_marginBottom}});
			GridDataFactory.create(marginsGroup).fillV();
		}
	}
}