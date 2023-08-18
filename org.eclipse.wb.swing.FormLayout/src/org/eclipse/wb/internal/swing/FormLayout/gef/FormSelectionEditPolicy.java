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
package org.eclipse.wb.internal.swing.FormLayout.gef;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.FormLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.CellConstraints.Alignment;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.policy
 */
public final class FormSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
	private final FormLayoutInfo m_layout;
	private final ComponentInfo m_component;
	private final FormGridHelper m_gridHelper = new FormGridHelper(this, false);

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormSelectionEditPolicy(FormLayoutInfo layout, ComponentInfo component) {
		super(component);
		m_layout = layout;
		m_component = component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isActiveLayout() {
		return m_component.getParent().getChildren().contains(m_layout);
	}

	@Override
	protected IGridInfo getGridInfo() {
		return m_layout.getGridInfo();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handlesList = Lists.newArrayList();
		// add move handle
		handlesList.add(createMoveHandle());
		// add span handles
		{
			handlesList.add(createSpanHandle(IPositionConstants.NORTH, 0.25));
			handlesList.add(createSpanHandle(IPositionConstants.WEST, 0.25));
			handlesList.add(createSpanHandle(IPositionConstants.EAST, 0.75));
			handlesList.add(createSpanHandle(IPositionConstants.SOUTH, 0.75));
		}
		//
		return handlesList;
	}

	@Override
	protected void showPrimarySelection() {
		super.showPrimarySelection();
		m_gridHelper.showGridFeedback();
	}

	@Override
	protected void hideSelection() {
		m_gridHelper.eraseGridFeedback();
		super.hideSelection();
	}

	@Override
	protected Figure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
		IEditPartViewer viewer = getHost().getViewer();
		final CellConstraintsSupport constraints =
				FormLayoutInfo.getConstraints((ComponentInfo) component);
		if (horizontal) {
			return new AbstractPopupFigure(viewer, 9, 5) {
				@Override
				protected ImageDescriptor getImageDescriptor() {
					return constraints.getSmallAlignmentImageDescriptor(true);
				}

				@Override
				protected void fillMenu(IMenuManager manager) {
					constraints.fillHorizontalAlignmentMenu(manager);
				}
			};
		} else {
			return new AbstractPopupFigure(viewer, 5, 9) {
				@Override
				protected ImageDescriptor getImageDescriptor() {
					return constraints.getSmallAlignmentImageDescriptor(false);
				}

				@Override
				protected void fillMenu(IMenuManager manager) {
					constraints.fillVerticalAlignmentMenu(manager);
				}
			};
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Span
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command createSpanCommand(final boolean horizontal, final Rectangle cells) {
		return new EditCommand(m_layout) {
			@Override
			protected void executeEdit() throws Exception {
				CellConstraintsSupport support = FormLayoutInfo.getConstraints(m_component);
				support.setSpan(horizontal, cells.getTranslated(1, 1));
				support.write();
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Keyboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		if (request instanceof KeyRequest) {
			KeyRequest keyRequest = (KeyRequest) request;
			if (keyRequest.isPressed()) {
				char c = keyRequest.getCharacter();
				// horizontal
				if (c == 'd') {
					setAlignment(true, CellConstraints.DEFAULT);
				} else if (c == 'l') {
					setAlignment(true, CellConstraints.LEFT);
				} else if (c == 'f') {
					setAlignment(true, CellConstraints.FILL);
				} else if (c == 'c') {
					setAlignment(true, CellConstraints.CENTER);
				} else if (c == 'r') {
					setAlignment(true, CellConstraints.RIGHT);
				}
				// vertical
				if (c == 'D') {
					setAlignment(false, CellConstraints.DEFAULT);
				} else if (c == 't') {
					setAlignment(false, CellConstraints.TOP);
				} else if (c == 'F') {
					setAlignment(false, CellConstraints.FILL);
				} else if (c == 'm') {
					setAlignment(false, CellConstraints.CENTER);
				} else if (c == 'b') {
					setAlignment(false, CellConstraints.BOTTOM);
				}
			}
		}
	}

	/**
	 * Sets the horizontal/vertical alignment.
	 */
	private void setAlignment(final boolean horizontal, final Alignment alignment) {
		ExecutionUtils.run(m_layout, new RunnableEx() {
			@Override
			public void run() throws Exception {
				CellConstraintsSupport support = FormLayoutInfo.getConstraints(m_component);
				if (horizontal) {
					support.setAlignH(alignment);
				} else {
					support.setAlignV(alignment);
				}
				support.write();
			}
		});
	}
}
