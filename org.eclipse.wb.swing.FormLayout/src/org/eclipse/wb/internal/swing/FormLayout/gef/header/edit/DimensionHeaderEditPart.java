/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.FormLayout.gef.header.edit;

import org.eclipse.wb.core.gef.header.Headers;
import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetTemplateAction;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionTemplate;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for column/row header of {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public abstract class DimensionHeaderEditPart<T extends FormDimensionInfo>
extends
GraphicalEditPart implements IHeaderMenuProvider {
	protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
	protected static final Color COLOR_GAP = DrawUtils.getShiftedColor(COLOR_NORMAL, -32);
	protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
	protected static final Color GROUP_COLORS[] = new Color[]{
			new Color(null, 200, 255, 200),
			new Color(null, 255, 210, 170),
			new Color(null, 180, 255, 255),
			new Color(null, 255, 255, 180),
			new Color(null, 230, 180, 255)};
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	protected final FormLayoutInfo m_layout;
	protected final T m_dimension;
	private final Figure m_containerFigure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionHeaderEditPart(FormLayoutInfo layout, T dimension, Figure containerFigure) {
		m_layout = layout;
		m_dimension = dimension;
		m_containerFigure = containerFigure;
		setModel(dimension);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the index of this {@link FormDimensionInfo}.
	 */
	public abstract int getIndex();

	/**
	 * @return the host {@link FormLayoutInfo}.
	 */
	public final FormLayoutInfo getLayout() {
		return m_layout;
	}

	/**
	 * @return the {@link FormDimensionInfo} model.
	 */
	public final T getDimension() {
		return m_dimension;
	}

	/**
	 * @return the offset of {@link Figure} with headers relative to the absolute layer.
	 */
	public final Point getOffset() {
		Point offset = new Point(0, 0);
		FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
		return offset;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dragging
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Tool getDragTracker(Request request) {
		return new ParentTargetDragEditPartTracker(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshVisuals() {
		// update tooltip
		getFigure().setToolTipText(m_dimension.getToolTip());
		// update background
		{
			if (m_dimension.isGap()) {
				getFigure().setBackgroundColor(COLOR_GAP);
			} else {
				int group = m_layout.getDimensionGroupIndex(m_dimension);
				if (group != -1) {
					getFigure().setBackgroundColor(GROUP_COLORS[group % GROUP_COLORS.length]);
				} else {
					getFigure().setBackgroundColor(COLOR_NORMAL);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	protected Image getImage(String name) {
		return Activator.getImage(name);
	}

	protected ImageDescriptor getImageDescriptor(String name) {
		return Activator.getImageDescriptor(name);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds template actions to the given {@link IMenuManager}.
	 */
	protected final void addTemplateActions(IMenuManager manager, FormDimensionTemplate[] templates) {
		for (int i = 0; i < templates.length; i++) {
			FormDimensionTemplate template = templates[i];
			manager.add(new SetTemplateAction<>(this, template));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		super.performRequest(request);
		if (request.getType() == RequestConstants.REQ_OPEN) {
			editDimension();
		}
	}

	/**
	 * Opens the {@link FormDimensionInfo} edit dialog.
	 */
	protected abstract void editDimension();
}
