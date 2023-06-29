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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.RowLayout}.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class RowLayoutAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowLayoutAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(2);
		// orientation
		{
			Group orientationGroup =
					addChoiceProperty(
							this,
							"type",
							ModelMessages.RowLayoutAssistantPage_orientationGroup,
							new Object[][]{
								new Object[]{
										ModelMessages.RowLayoutAssistantPage_orientationHorizontal,
										SWT.HORIZONTAL},
								new Object[]{
										ModelMessages.RowLayoutAssistantPage_orientationVertical,
										SWT.VERTICAL}});
			GridDataFactory.create(orientationGroup).fillV();
		}
		// margins
		{
			Group spacingGroup =
					addIntegerProperties(
							this,
							ModelMessages.RowLayoutAssistantPage_marginGroup,
							new String[][]{
								new String[]{"marginWidth", ModelMessages.RowLayoutAssistantPage_marginWidth},
								new String[]{"marginHeight", ModelMessages.RowLayoutAssistantPage_marginHeight},
								new String[]{"spacing", ModelMessages.RowLayoutAssistantPage_spacingValue}});
			GridDataFactory.create(spacingGroup).fillV();
		}
		// options
		{
			Group optionsGroup =
					addBooleanProperties(
							this,
							ModelMessages.RowLayoutAssistantPage_optionsGroup,
							new String[][]{
								new String[]{"wrap", "wrap"},
								new String[]{"pack", "pack"},
								new String[]{"fill", "fill"},
								new String[]{"justify", "justify"}});
			GridDataFactory.create(optionsGroup).fill();
		}
		// margins for sides
		{
			Group spacingGroup =
					addIntegerProperties(
							this,
							ModelMessages.RowLayoutAssistantPage_sideMarginsGroup,
							new String[][]{
								new String[]{"marginLeft", ModelMessages.RowLayoutAssistantPage_marginLeft},
								new String[]{"marginRight", ModelMessages.RowLayoutAssistantPage_marginRight},
								new String[]{"marginTop", ModelMessages.RowLayoutAssistantPage_marginTop},
								new String[]{"marginBottom", ModelMessages.RowLayoutAssistantPage_marginBottom}});
			GridDataFactory.create(spacingGroup).fillV();
		}
	}
}