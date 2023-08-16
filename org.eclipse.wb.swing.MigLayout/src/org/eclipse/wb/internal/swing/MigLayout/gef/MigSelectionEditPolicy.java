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
package org.eclipse.wb.internal.swing.MigLayout.gef;

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
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.policy
 */
public final class MigSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
	private final MigLayoutInfo m_layout;
	private final ComponentInfo m_component;
	private final MigGridHelper m_gridHelper = new MigGridHelper(this, false);

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MigSelectionEditPolicy(MigLayoutInfo layout, ComponentInfo component) {
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
				MigLayoutInfo.getConstraints((ComponentInfo) component);
		if (horizontal) {
			return new AbstractPopupFigure(viewer, 9, 5) {
				@Override
				protected ImageDescriptor getImage() {
					return constraints.getSmallAlignmentImage(true);
				}

				@Override
				protected void fillMenu(IMenuManager manager) {
					constraints.fillHorizontalAlignmentMenu(manager);
				}
			};
		} else {
			return new AbstractPopupFigure(viewer, 5, 9) {
				@Override
				protected ImageDescriptor getImage() {
					return constraints.getSmallAlignmentImage(false);
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
				m_layout.command_setCells(m_component, cells);
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
					setAlignment(MigColumnInfo.Alignment.DEFAULT);
				} else if (c == 'l') {
					setAlignment(MigColumnInfo.Alignment.LEFT);
				} else if (c == 'c') {
					setAlignment(MigColumnInfo.Alignment.CENTER);
				} else if (c == 'r') {
					setAlignment(MigColumnInfo.Alignment.RIGHT);
				} else if (c == 'f') {
					setAlignment(MigColumnInfo.Alignment.FILL);
				} else if (c == 'q') {
					setAlignment(MigColumnInfo.Alignment.LEADING);
				} else if (c == 'w') {
					setAlignment(MigColumnInfo.Alignment.TRAILING);
				}
				// vertical
				if (c == 'D') {
					setAlignment(MigRowInfo.Alignment.DEFAULT);
				} else if (c == 't') {
					setAlignment(MigRowInfo.Alignment.TOP);
				} else if (c == 'm') {
					setAlignment(MigRowInfo.Alignment.CENTER);
				} else if (c == 'b') {
					setAlignment(MigRowInfo.Alignment.BOTTOM);
				} else if (c == 'F') {
					setAlignment(MigRowInfo.Alignment.FILL);
				} else if (c == 'a') {
					setAlignment(MigRowInfo.Alignment.BASELINE);
				}
			}
		}
	}

	/**
	 * Sets the horizontal alignment.
	 */
	private void setAlignment(final MigColumnInfo.Alignment alignment) {
		ExecutionUtils.run(m_layout, new RunnableEx() {
			public void run() throws Exception {
				CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(m_component);
				constraints.setHorizontalAlignment(alignment);
				constraints.write();
			}
		});
	}

	/**
	 * Sets the vertical alignment.
	 */
	private void setAlignment(final MigRowInfo.Alignment alignment) {
		ExecutionUtils.run(m_layout, new RunnableEx() {
			public void run() throws Exception {
				CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(m_component);
				constraints.setVerticalAlignment(alignment);
				constraints.write();
			}
		});
	}
}
