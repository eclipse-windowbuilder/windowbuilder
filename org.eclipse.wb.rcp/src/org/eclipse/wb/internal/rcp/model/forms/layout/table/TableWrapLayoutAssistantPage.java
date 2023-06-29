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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Layout assistant for {@link TableWrapLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableWrapLayoutAssistantPage(Composite parent, Object selection) {
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
						ModelMessages.TableWrapLayoutAssistantPage_numColumns,
						1);
				new Label(topComposite, SWT.NONE).setText(" ");
				addBooleanProperty(
						topComposite,
						"makeColumnsEqualWidth",
						ModelMessages.TableWrapLayoutAssistantPage_makeEqualColumns);
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
								ModelMessages.TableWrapLayoutAssistantPage_marginsGroup,
								new String[][]{
									new String[]{
											"marginLeft",
											ModelMessages.TableWrapLayoutAssistantPage_marginLeft},
									new String[]{"marginTop", ModelMessages.TableWrapLayoutAssistantPage_marginTop},
									new String[]{
											"marginRight",
											ModelMessages.TableWrapLayoutAssistantPage_marginRight},
									new String[]{
											"marginBottom",
											ModelMessages.TableWrapLayoutAssistantPage_marginBottom}});
				GridDataFactory.create(group).fillV().spanV(2);
			}
			// spacing
			{
				Group group =
						addIntegerProperties(
								groupComposite,
								ModelMessages.TableWrapLayoutAssistantPage_spacingGroup,
								new String[][]{
									new String[]{
											"horizontalSpacing",
											ModelMessages.TableWrapLayoutAssistantPage_spacingHorizontal},
									new String[]{
											"verticalSpacing",
											ModelMessages.TableWrapLayoutAssistantPage_spacingVertical}});
				GridDataFactory.create(group).fillH().grabH();
			}
		}
	}
}