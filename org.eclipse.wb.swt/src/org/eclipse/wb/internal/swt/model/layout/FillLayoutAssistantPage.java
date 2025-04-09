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
 * Layout assistant for {@link org.eclipse.swt.layout.FillLayout}.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class FillLayoutAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FillLayoutAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(2);
		// orientation
		{
			Group orientationGroup =
					addChoiceProperty(
							this,
							"type",
							ModelMessages.FillLayoutAssistantPage_orientationGroup,
							new Object[][]{
								new Object[]{
										ModelMessages.FillLayoutAssistantPage_orientationHorizontal,
										SWT.HORIZONTAL},
								new Object[]{
										ModelMessages.FillLayoutAssistantPage_orientationVertical,
										SWT.VERTICAL}});
			GridDataFactory.create(orientationGroup).fillV();
		}
		// spacing
		{
			Group spacingGroup =
					addIntegerProperties(
							this,
							ModelMessages.FillLayoutAssistantPage_spacingGroup,
							new String[][]{
								new String[]{"marginWidth", ModelMessages.FillLayoutAssistantPage_marginWidth},
								new String[]{"marginHeight", ModelMessages.FillLayoutAssistantPage_marginHeight},
								new String[]{"spacing", ModelMessages.FillLayoutAssistantPage_spacingValue}});
			GridDataFactory.create(spacingGroup).fillV();
		}
	}
}