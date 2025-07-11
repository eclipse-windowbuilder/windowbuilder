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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;
import org.eclipse.wb.swing.SwingImages;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link AbstractGridBagLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class GridBagSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
	private final AbstractGridBagLayoutInfo m_layout;
	private final ComponentInfo m_component;
	private final GridHelper m_gridHelper = new GridHelper(this, false);

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridBagSelectionEditPolicy(AbstractGridBagLayoutInfo layout, ComponentInfo component) {
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
		List<Handle> handlesList = new ArrayList<>();
		// add move handle
		handlesList.add(createMoveHandle());
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
	protected IFigure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
		IEditPartViewer viewer = getHost().getViewer();
		final AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
		if (horizontal) {
			return new AbstractPopupFigure(viewer, 9, 5) {
				@Override
				protected ImageDescriptor getImageDescriptor() {
					switch (constraints.getHorizontalAlignment()) {
					case LEFT :
						return CoreImages.ALIGNMENT_H_SMALL_LEFT;
					case CENTER :
						return CoreImages.ALIGNMENT_H_SMALL_CENTER;
					case RIGHT :
						return CoreImages.ALIGNMENT_H_SMALL_RIGHT;
					case FILL :
						return CoreImages.ALIGNMENT_H_SMALL_FILL;
					}
					return null;
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
					switch (constraints.getVerticalAlignment()) {
					case TOP :
						return CoreImages.ALIGNMENT_V_SMALL_TOP;
					case CENTER :
						return CoreImages.ALIGNMENT_V_SMALL_CENTER;
					case BOTTOM :
						return CoreImages.ALIGNMENT_V_SMALL_BOTTOM;
					case FILL :
						return CoreImages.ALIGNMENT_V_SMALL_FILL;
					case BASELINE :
						return SwingImages.ALIGNMENT_V_SMALL_BASELINE;
					case BASELINE_ABOVE :
						return SwingImages.ALIGNMENT_V_SMALL_BASELINE_ABOVE;
					case BASELINE_BELOW :
						return SwingImages.ALIGNMENT_V_SMALL_BASELINE_BELOW;
					}
					return null;
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
				AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
				if (horizontal) {
					constraints.setX(cells.x);
					constraints.setWidth(cells.width);
				} else {
					constraints.setY(cells.y);
					constraints.setHeight(cells.height);
				}
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
				// grab/fill both
				if (c == 'o') {
					setFillBoth();
				}
				// horizontal
				if (c == 'h') {
					flipGrab(true);
				} else if (c == 'l') {
					setAlignment(ColumnInfo.Alignment.LEFT);
				} else if (c == 'c') {
					setAlignment(ColumnInfo.Alignment.CENTER);
				} else if (c == 'r') {
					setAlignment(ColumnInfo.Alignment.RIGHT);
				} else if (c == 'f') {
					setAlignment(ColumnInfo.Alignment.FILL);
				}
				// vertical
				if (c == 'v') {
					flipGrab(false);
				} else if (c == 't') {
					setAlignment(RowInfo.Alignment.TOP);
				} else if (c == 'm') {
					setAlignment(RowInfo.Alignment.CENTER);
				} else if (c == 'b') {
					setAlignment(RowInfo.Alignment.BOTTOM);
				} else if (c == 'F') {
					setAlignment(RowInfo.Alignment.FILL);
				} else if (c == 'L') {
					setAlignment(RowInfo.Alignment.BASELINE);
				} else if (c == 'A') {
					setAlignment(RowInfo.Alignment.BASELINE_ABOVE);
				} else if (c == 'B') {
					setAlignment(RowInfo.Alignment.BASELINE_BELOW);
				}
			}
		}
	}

	/**
	 * Sets grab/fill for both dimensions.
	 */
	private void setFillBoth() {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
				constraints.getColumn().setWeight(1.0);
				constraints.getRow().setWeight(1.0);
				constraints.setAlignment(ColumnInfo.Alignment.FILL, RowInfo.Alignment.FILL);
			}
		});
	}

	/**
	 * Flips horizontal/vertical grab.
	 */
	private void flipGrab(final boolean horizontal) {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
				DimensionInfo dimension;
				if (horizontal) {
					dimension = constraints.getColumn();
				} else {
					dimension = constraints.getRow();
				}
				double weight = dimension.getWeight();
				dimension.setWeight(weight != 0.0 ? 0.0 : 1.0);
			}
		});
	}

	/**
	 * Sets the horizontal alignment.
	 */
	private void setAlignment(final ColumnInfo.Alignment alignment) {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
				constraints.setHorizontalAlignment(alignment);
			}
		});
	}

	/**
	 * Sets the vertical alignment.
	 */
	private void setAlignment(final RowInfo.Alignment alignment) {
		execute(new RunnableEx() {
			@Override
			public void run() throws Exception {
				AbstractGridBagConstraintsInfo constraints = m_layout.getConstraints(m_component);
				constraints.setVerticalAlignment(alignment);
			}
		});
	}

	/**
	 * Executes given {@link RunnableEx} as edit operation.
	 */
	private void execute(final RunnableEx runnable) {
		ExecutionUtils.run(m_component, runnable);
	}
}
