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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Layout assistant for {@link TableWrapData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutDataAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableWrapLayoutDataAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(3);
		{
			Group composite = new Group(this, SWT.NONE);
			composite.setText(ModelMessages.TableWrapLayoutDataAssistantPage_alignmentGroup);
			GridLayoutFactory.create(composite).columns(2);
			GridDataFactory.create(composite).fill().grab().spanH(2).spanV(2);
			// Horizontal alignment & grab
			{
				Group horizontalGroup =
						addChoiceProperty(
								composite,
								"horizontalAlignment",
								ModelMessages.TableWrapLayoutDataAssistantPage_horizontalGroup,
								new Object[][]{
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_haLeft,
											TableWrapData.LEFT},
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_haCenter,
											TableWrapData.CENTER},
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_haRight,
											TableWrapData.RIGHT},
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_haFill,
											TableWrapData.FILL}});
				//
				addBooleanProperty(
						horizontalGroup,
						"grabHorizontal",
						ModelMessages.TableWrapLayoutDataAssistantPage_haGrab);
				GridDataFactory.create(horizontalGroup).alignHC().fillV().grab();
			}
			// Vertical alignment & grab
			{
				Group verticalGroup =
						addChoiceProperty(
								composite,
								"verticalAlignment",
								ModelMessages.TableWrapLayoutDataAssistantPage_verticalGroup,
								new Object[][]{
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_vaTop,
											TableWrapData.TOP},
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_vaCenter,
											TableWrapData.MIDDLE},
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_vaBottom,
											TableWrapData.BOTTOM},
									new Object[]{
											ModelMessages.TableWrapLayoutDataAssistantPage_vaFill,
											TableWrapData.FILL}});
				//
				addBooleanProperty(
						verticalGroup,
						"grabVertical",
						ModelMessages.TableWrapLayoutDataAssistantPage_vaGrab);
				GridDataFactory.create(verticalGroup).alignHC().fillV().grab();
			}
		}
		// Hints
		{
			Group group =
					addIntegerProperties(
							this,
							ModelMessages.TableWrapLayoutDataAssistantPage_hintsGroup,
							new String[][]{
								{"indent", ModelMessages.TableWrapLayoutDataAssistantPage_hintIndent},
								{"maxWidth", ModelMessages.TableWrapLayoutDataAssistantPage_hintMaxWidth},
								{"maxHeight", ModelMessages.TableWrapLayoutDataAssistantPage_hintMaxHeight},
								{"heightHint", ModelMessages.TableWrapLayoutDataAssistantPage_hintHeight}},
							new int[]{0, SWT.DEFAULT, SWT.DEFAULT, SWT.DEFAULT});
			GridDataFactory.create(group).fillH().fillV();
		}
	}
}