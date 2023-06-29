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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.GridData}.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class GridLayoutDataAssistantPage extends AbstractAssistantPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutDataAssistantPage(Composite parent, Object selection) {
		super(parent, selection);
		GridLayoutFactory.create(this).columns(3);
		{
			Group composite = new Group(this, SWT.NONE);
			composite.setText(ModelMessages.GridLayoutDataAssistantPage_alignmentGroup);
			GridLayoutFactory.create(composite).columns(2);
			GridDataFactory.create(composite).fill().grab().spanH(2).spanV(2);
			// Horizontal alignment & grab
			{
				Group horizontalOrientationGroup =
						addChoiceProperty(
								composite,
								"horizontalAlignment",
								ModelMessages.GridLayoutDataAssistantPage_horizontalGroup,
								new Object[][]{
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_horLeft, SWT.LEFT},
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_horCenter, SWT.CENTER},
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_horRight, SWT.RIGHT},
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_horFill, SWT.FILL}});
				//
				addBooleanProperty(
						horizontalOrientationGroup,
						"grabExcessHorizontalSpace",
						ModelMessages.GridLayoutDataAssistantPage_grab);
				GridDataFactory.create(horizontalOrientationGroup).alignHC().fillV().grab();
			}
			// Vertical alignment & grab
			{
				Group verticalOrientationGroup =
						addChoiceProperty(
								composite,
								"verticalAlignment",
								ModelMessages.GridLayoutDataAssistantPage_verticalGroup,
								new Object[][]{
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_verTop, SWT.TOP},
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_verCenter, SWT.CENTER},
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_verBottom, SWT.BOTTOM},
									new Object[]{ModelMessages.GridLayoutDataAssistantPage_verFill, SWT.FILL}});
				//
				addBooleanProperty(
						verticalOrientationGroup,
						"grabExcessVerticalSpace",
						ModelMessages.GridLayoutDataAssistantPage_grab);
				GridDataFactory.create(verticalOrientationGroup).alignHC().fillV().grab();
			}
		}
		// Hints
		{
			Group group =
					addIntegerProperties(
							this,
							ModelMessages.GridLayoutDataAssistantPage_hintsGroup,
							new String[][]{
								{"widthHint", ModelMessages.GridLayoutDataAssistantPage_widthHint},
								{"heightHint", ModelMessages.GridLayoutDataAssistantPage_heightHint}},
							new int[]{SWT.DEFAULT, SWT.DEFAULT});
			GridDataFactory.create(group).fillH().fillV();
		}
		// Spanning
		{
			Group group =
					addIntegerProperties(
							this,
							ModelMessages.GridLayoutDataAssistantPage_spanningGroup,
							new String[][]{
								{"horizontalSpan", ModelMessages.GridLayoutDataAssistantPage_columnSpan},
								{"verticalSpan", ModelMessages.GridLayoutDataAssistantPage_rowSpan}},
							new int[]{1, 1});
			GridDataFactory.create(group).fillH().fillV();
		}
		// Minimum
		{
			addIntegerProperties(
					this,
					ModelMessages.GridLayoutDataAssistantPage_minimumGroup,
					new String[][]{
						{"minimumWidth", ModelMessages.GridLayoutDataAssistantPage_minWidth},
						{"minimumHeight", ModelMessages.GridLayoutDataAssistantPage_minHeight}});
		}
		// Exclude
		{
			addBooleanProperty(this, "exclude", ModelMessages.GridLayoutDataAssistantPage_excludeFlag);
		}
		// Indents
		{
			Group group =
					addIntegerProperties(
							this,
							ModelMessages.GridLayoutDataAssistantPage_indentsGroup,
							new String[][]{
								{"horizontalIndent", ModelMessages.GridLayoutDataAssistantPage_horIndent},
								{"verticalIndent", ModelMessages.GridLayoutDataAssistantPage_verIndent}});
			GridDataFactory.create(group).fillH().fillV();
		}
	}
}