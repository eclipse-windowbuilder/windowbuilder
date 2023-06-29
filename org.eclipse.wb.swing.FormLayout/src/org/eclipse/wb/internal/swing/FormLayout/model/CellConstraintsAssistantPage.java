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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.jgoodies.forms.layout.CellConstraints;

import java.util.List;

/**
 * Layout assistant for {@link com.jgoodies.forms.layout.CellConstraints}.
 *
 * @author lobas_av
 * @coverage swing.FormLayout.model
 */
public class CellConstraintsAssistantPage extends AbstractAssistantPage {
	private final FormLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CellConstraintsAssistantPage(Composite parent,
			FormLayoutInfo layout,
			List<ObjectInfo> objects) {
		super(parent, objects);
		m_layout = layout;
		GridLayoutFactory.create(this).columns(3);
		// horizontal alignments
		{
			Group horizontalGroup =
					addChoiceProperty(
							this,
							"h alignment",
							ModelMessages.CellConstraintsAssistantPage_horizontalGroup,
							new Object[][]{
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_haDefault,
										CellConstraints.DEFAULT},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_haLeft,
										CellConstraints.LEFT},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_haCenter,
										CellConstraints.CENTER},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_haRight,
										CellConstraints.RIGHT},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_haFill,
										CellConstraints.FILL}});
			GridDataFactory.modify(horizontalGroup).fill();
		}
		// vertical alignments
		{
			Group verticalGroup =
					addChoiceProperty(
							this,
							"v alignment",
							ModelMessages.CellConstraintsAssistantPage_verticalGroup,
							new Object[][]{
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_vaDefault,
										CellConstraints.DEFAULT},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_vaTop,
										CellConstraints.TOP},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_vaCenter,
										CellConstraints.CENTER},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_vaBottom,
										CellConstraints.BOTTOM},
								new Object[]{
										ModelMessages.CellConstraintsAssistantPage_vaFill,
										CellConstraints.FILL}});
			GridDataFactory.modify(verticalGroup).fill();
		}
		// grid
		{
			Group gridGroup =
					addIntegerProperties(
							this,
							ModelMessages.CellConstraintsAssistantPage_gridGroup,
							new String[][]{
								{"grid x", ModelMessages.CellConstraintsAssistantPage_gridX},
								{"grid y", ModelMessages.CellConstraintsAssistantPage_gridY},
								{"grid width", ModelMessages.CellConstraintsAssistantPage_gridWidth},
								{"grid height", ModelMessages.CellConstraintsAssistantPage_gridHeight}});
			GridDataFactory.modify(gridGroup).fill();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractAssistantPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ObjectInfo getEditObject() {
		return m_layout;
	}

	@Override
	protected Property getCustomProperty(Object object, String propertyName) throws Exception {
		if (object instanceof ComponentInfo) {
			ComponentInfo component = (ComponentInfo) object;
			return FormLayoutInfo.getConstraints(component).getPropertyByTitle(propertyName);
		}
		return null;
	}
}