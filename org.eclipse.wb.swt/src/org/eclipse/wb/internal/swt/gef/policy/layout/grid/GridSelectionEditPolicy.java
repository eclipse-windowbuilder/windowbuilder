/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class GridSelectionEditPolicy<C extends IControlInfo>
extends
AbstractGridSelectionEditPolicy {
	private final IGridLayoutInfo<C> m_layout;
	private final C m_component;
	private final GridHelper m_gridHelper = new GridHelper(this, false);

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridSelectionEditPolicy(IGridLayoutInfo<C> layout, C component) {
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
		return m_layout.isActive();
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
		List<Handle> handlesList = new ArrayList<>();
		// add move handle
		handlesList.add(createMoveHandle());
		// add resize handles
		{
			handlesList.add(createSizeHandle(PositionConstants.EAST, 0.25));
			handlesList.add(createSizeHandle(PositionConstants.SOUTH, 0.25));
		}
		// add span handles
		{
			handlesList.add(createSpanHandle(PositionConstants.NORTH, 0.25));
			handlesList.add(createSpanHandle(PositionConstants.WEST, 0.25));
			handlesList.add(createSpanHandle(PositionConstants.EAST, 0.75));
			handlesList.add(createSpanHandle(PositionConstants.SOUTH, 0.75));
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
	@SuppressWarnings("unchecked")
	protected IFigure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
		IEditPartViewer viewer = getHost().getViewer();
		try {
			final IGridDataInfo constraints = m_layout.getGridData2((C) component);
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
		} catch (Throwable e) {
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command createSizeCommand(final boolean horizontal, final Dimension size) {
		return new EditCommand(m_object) {
			@Override
			protected void executeEdit() throws Exception {
				m_layout.command_setSizeHint(m_component, horizontal, size);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Span
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command createSpanCommand(final boolean horizontal, final Rectangle cells) {
		return new EditCommand(m_object) {
			@Override
			protected void executeEdit() throws Exception {
				m_layout.command_setCells(m_component, cells, true);
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
		if (request instanceof KeyRequest keyRequest) {
			if (keyRequest.isPressed()) {
				char c = keyRequest.getCharacter();
				// FILL_BOTH
				if (c == 'o') {
					setFillBoth();
				}
				// horizontal
				if (c == 'h') {
					flipGrab(true);
				} else if (c == 'l') {
					setAlignment(true, SWT.LEFT);
				} else if (c == 'c') {
					setAlignment(true, SWT.CENTER);
				} else if (c == 'r') {
					setAlignment(true, SWT.RIGHT);
				} else if (c == 'f') {
					setAlignment(true, SWT.FILL);
				}
				// vertical
				if (c == 'v') {
					flipGrab(false);
				} else if (c == 't') {
					setAlignment(false, SWT.TOP);
				} else if (c == 'm') {
					setAlignment(false, SWT.CENTER);
				} else if (c == 'b') {
					setAlignment(false, SWT.BOTTOM);
				} else if (c == 'F') {
					setAlignment(false, SWT.FILL);
				}
			}
		}
	}

	/**
	 * Sets the {@link SWT#FILL_BOTH} grab/alignment.
	 */
	private void setFillBoth() {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				IGridDataInfo gridData = m_layout.getGridData2(m_component);
				gridData.setHorizontalGrab(true);
				gridData.setVerticalGrab(true);
				gridData.setHorizontalAlignment(SWT.FILL);
				gridData.setVerticalAlignment(SWT.FILL);
			}
		});
	}

	/**
	 * Flips the horizontal/vertical grab.
	 */
	private void flipGrab(final boolean horizontal) {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				IGridDataInfo gridData = m_layout.getGridData2(m_component);
				if (horizontal) {
					gridData.setHorizontalGrab(!gridData.getHorizontalGrab());
				} else {
					gridData.setVerticalGrab(!gridData.getVerticalGrab());
				}
			}
		});
	}

	/**
	 * Sets the horizontal/vertical alignment.
	 */
	private void setAlignment(final boolean horizontal, final int alignment) {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				IGridDataInfo gridData = m_layout.getGridData2(m_component);
				if (horizontal) {
					gridData.setHorizontalAlignment(alignment);
				} else {
					gridData.setVerticalAlignment(alignment);
				}
			}
		});
	}

	/**
	 * Executes given {@link RunnableEx} as edit operation.
	 */
	private void execute(final RunnableEx runnable) {
		getHost().getViewer().getEditDomain().getCommandStack().execute(new EditCommand(m_object) {
			@Override
			protected void executeEdit() throws Exception {
				runnable.run();
			}
		});
	}
}
