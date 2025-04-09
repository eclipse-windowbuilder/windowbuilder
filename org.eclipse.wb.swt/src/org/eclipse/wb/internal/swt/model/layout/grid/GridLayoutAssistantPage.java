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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.GridLayout}.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class GridLayoutAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).spacingV(1).noMargins();
		{
			Composite topComposite = new Composite(this, SWT.NONE);
			GridLayoutFactory.create(topComposite).columns(4);
			GridDataFactory.create(topComposite).fillH().grabH();
			// columns properties
			{
				addIntegerProperty(
						topComposite,
						"numColumns",
						ModelMessages.GridLayoutAssistantPage_numColumns,
						1);
				new Label(topComposite, SWT.NONE).setText(" ");
				addBooleanProperty(
						topComposite,
						"makeColumnsEqualWidth",
						ModelMessages.GridLayoutAssistantPage_makeColumnsEqual);
			}
		}
		{
			Composite groupComposite = new Composite(this, SWT.NONE);
			GridLayoutFactory.create(groupComposite).columns(2);
			GridDataFactory.create(groupComposite).fill().grab();
			// margin for sides
			{
				Group group =
						addIntegerProperties(
								groupComposite,
								ModelMessages.GridLayoutAssistantPage_sideMarginsGroup,
								new String[][]{
									new String[]{"marginLeft", ModelMessages.GridLayoutAssistantPage_marginLeft},
									new String[]{"marginTop", ModelMessages.GridLayoutAssistantPage_marginTop},
									new String[]{"marginRight", ModelMessages.GridLayoutAssistantPage_marginRight},
									new String[]{"marginBottom", ModelMessages.GridLayoutAssistantPage_marginBottom}});
				GridDataFactory.create(group).fillV().spanV(2);
			}
			// margins
			{
				Group group =
						addIntegerProperties(
								groupComposite,
								ModelMessages.GridLayoutAssistantPage_marginsGroup,
								new String[][]{
									new String[]{"marginWidth", ModelMessages.GridLayoutAssistantPage_marginWidth},
									new String[]{"marginHeight", ModelMessages.GridLayoutAssistantPage_marginHeight}});
				GridDataFactory.create(group).fillH().grabH();
			}
			// spacing
			{
				Group group =
						addIntegerProperties(
								groupComposite,
								ModelMessages.GridLayoutAssistantPage_spacingGroup,
								new String[][]{
									new String[]{
											"horizontalSpacing",
											ModelMessages.GridLayoutAssistantPage_spacingHorizontal},
									new String[]{
											"verticalSpacing",
											ModelMessages.GridLayoutAssistantPage_spacingVertical}});
				GridDataFactory.create(group).fillH().grabH();
			}
		}
	}
}