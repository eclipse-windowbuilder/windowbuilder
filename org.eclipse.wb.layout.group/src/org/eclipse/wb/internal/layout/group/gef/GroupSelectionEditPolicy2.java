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
package org.eclipse.wb.internal.layout.group.gef;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.layout.group.model.AnchorsSupport;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutUtils;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mitin_aa
 */
public class GroupSelectionEditPolicy2 extends SelectionEditPolicy
implements
IFeedbacksHelper,
LayoutConstants {
	// constants
	public static final String REQ_RESIZE = "_grouplayout_resize";
	private static final int MIN_LEFT_SPACE = 10;
	private static final int INITIAL_RIGHT_SPACE = 10;
	private static final int FIGURES_SPACE = 10;
	// fields
	private final IGroupLayoutInfo m_layout;
	private final AnchorsSupport m_anchorsSupport;
	private final FeedbacksDrawer m_feedbacksDrawer;
	private List<IFigure> m_alignmentFigures;
	private boolean m_resizeInProgress;
	private IFigure m_dragFeedback;
	private java.awt.Rectangle[] m_movingBounds;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GroupSelectionEditPolicy2(IGroupLayoutInfo layoutModel) {
		m_layout = layoutModel;
		m_feedbacksDrawer = new FeedbacksDrawer(this);
		m_anchorsSupport = new AnchorsSupport(m_layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		MoveHandle moveHandle = new MoveHandle(getHost());
		handles.add(moveHandle);
		handles.add(createResizeHandle(PositionConstants.NORTH));
		handles.add(createResizeHandle(PositionConstants.SOUTH));
		handles.add(createResizeHandle(PositionConstants.WEST));
		handles.add(createResizeHandle(PositionConstants.EAST));
		handles.add(createResizeHandle(PositionConstants.SOUTH_EAST));
		handles.add(createResizeHandle(PositionConstants.SOUTH_WEST));
		handles.add(createResizeHandle(PositionConstants.NORTH_WEST));
		handles.add(createResizeHandle(PositionConstants.NORTH_EAST));
		return handles;
	}

	/**
	 * @return the {@link ResizeHandle} for given direction.
	 */
	private Handle createResizeHandle(int direction) {
		ResizeHandle handle = new ResizeHandle(getHost(), direction);
		handle.setDragTracker(new ResizeTracker(direction, REQ_RESIZE));
		return handle;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void showSelection() {
		super.showSelection();
		String componentID = ObjectInfoUtils.getId((JavaInfo) getHostModel());
		LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
		layoutDesigner.updateCurrentState();
		layoutDesigner.paintSelection(m_feedbacksDrawer, componentID);
		if (getHost().getSelected() == EditPart.SELECTED_PRIMARY) {
			showAlignmentFigures();
		}
	}

	@Override
	protected void hideSelection() {
		removeFeedbacks();
		super.hideSelection();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	public void removeFeedbacks() {
		m_feedbacksDrawer.removeFeedbacks();
		if (m_dragFeedback != null) {
			removeFeedback(m_dragFeedback);
			m_dragFeedback = null;
		}
		hideAlignmentFigures();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFeedbacksHelper
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addFeedback2(IFigure figure) {
		addFeedback(figure);
	}

	@Override
	public void translateModelToFeedback(Translatable t) {
		PolicyUtils.translateModelToFeedback(this, t);
		t.performTranslate(getClientAreaOffset());
	}

	private Point getClientAreaOffset() {
		Insets insets = m_layout.getContainerInsets();
		return new Point(insets.left, insets.top);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Routing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
	}

	@Override
	public void showSourceFeedback(Request request) {
		showResizeFeedback((ChangeBoundsRequest) request);
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		removeFeedbacks();
		m_resizeInProgress = false;
		m_movingBounds = null;
	}

	private void showResizeFeedback(ChangeBoundsRequest request) {
		// resizing multiple parts not supported
		if (request.getEditParts().size() > 1) {
			return;
		}
		// hide feedbacks
		m_feedbacksDrawer.removeFeedbacks();
		hideAlignmentFigures();
		// prepare
		int direction = request.getResizeDirection();
		Point location = request.getLocation().getCopy();
		PolicyUtils.translateAbsoluteToModel(this, location);
		LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
		AbstractComponentInfo hostModel = (AbstractComponentInfo) getHostModel();
		if (!m_resizeInProgress) {
			m_resizeInProgress = true;
			String[] resizingComponents = new String[]{ObjectInfoUtils.getId(hostModel)};
			m_movingBounds =
					new java.awt.Rectangle[]{GroupLayoutUtils.getBoundsInLayout(m_layout, hostModel)};
			int[] resizeEdges = setupResizeEdges(direction);
			layoutDesigner.startResizing(
					resizingComponents,
					m_movingBounds,
					new java.awt.Point(location.x, location.y),
					resizeEdges,
					true);
			m_dragFeedback = new Figure();
			m_dragFeedback.setBorder(new LineBorder(AbsolutePolicyUtils.COLOR_OUTLINE));
			addFeedback(m_dragFeedback);
		}
		String id = ObjectInfoUtils.getId(m_layout.getLayoutContainer());
		java.awt.Rectangle[] movedBounds = new java.awt.Rectangle[]{new java.awt.Rectangle()};
		for (int i = 0; i < movedBounds.length; i++) {
			movedBounds[i] = new java.awt.Rectangle();
			movedBounds[i].width = m_movingBounds[i].width;
			movedBounds[i].height = m_movingBounds[i].height;
		}
		layoutDesigner.move(
				new java.awt.Point(location.x, location.y),
				id,
				!DesignerPlugin.isShiftPressed(),
				false,
				movedBounds);
		layoutDesigner.paintMoveFeedback(m_feedbacksDrawer);
		Rectangle newBounds = GroupLayoutUtils.get(movedBounds[0]);
		translateModelToFeedback(newBounds);
		m_dragFeedback.setBounds(newBounds);
	}

	private int[] setupResizeEdges(int direction) {
		int[] resizeEdges = new int[2];
		int horiz = direction & (PositionConstants.EAST | PositionConstants.WEST);
		if (horiz == PositionConstants.WEST) {
			resizeEdges[HORIZONTAL] = LEADING;
		} else if (horiz == PositionConstants.EAST) {
			resizeEdges[HORIZONTAL] = TRAILING;
		} else {
			resizeEdges[HORIZONTAL] = DEFAULT;
		}
		int vert = direction & (PositionConstants.NORTH | PositionConstants.SOUTH);
		if (vert == PositionConstants.NORTH) {
			resizeEdges[VERTICAL] = LEADING;
		} else if (vert == PositionConstants.SOUTH) {
			resizeEdges[VERTICAL] = TRAILING;
		} else {
			resizeEdges[VERTICAL] = DEFAULT;
		}
		return resizeEdges;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Command getCommand(Request request) {
		if (REQ_RESIZE.equals(request.getType())) {
			// resizing multiple components is not supported by Matisse
			if (((ChangeBoundsRequest) request).getEditParts().size() > 1) {
				return null;
			}
			return new EditCommand(m_layout.getAdapter(JavaInfo.class)) {
				@Override
				protected void executeEdit() throws Exception {
					m_layout.command_commit();
				}
			};
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment figures
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the alignment figure for given component and axis.
	 */
	protected IFigure createAlignmentFigure(final AbstractComponentInfo widget,
			final boolean isHorizontal) {
		IEditPartViewer viewer = getHost().getViewer();
		return isHorizontal
				? new HorizontalPopupFigure(viewer, widget)
						: new VerticalPopupFigure(viewer, widget);
	}

	/**
	 * Shows alignment figures for host {@link EditPart} and its siblings.
	 */
	public final void showAlignmentFigures() {
		if (m_alignmentFigures == null) {
			m_alignmentFigures = new ArrayList<>();
			// show cell figures for all children of host's parent
			{
				Collection<? extends EditPart> editParts = getHost().getParent().getChildren();
				for (EditPart editPart : editParts) {
					showAlignmentFigures(editPart);
				}
			}
		}
	}

	/**
	 * Hides alignment figures for this host and its siblings.
	 */
	public final void hideAlignmentFigures() {
		if (m_alignmentFigures != null) {
			for (IFigure figure : m_alignmentFigures) {
				figure.getParent().remove(figure);
			}
			m_alignmentFigures = null;
		}
	}

	/**
	 * Shows all possible alignment figures for given edit part.
	 */
	private void showAlignmentFigures(EditPart editPart) {
		// check model
		AbstractComponentInfo widget;
		{
			Object model = editPart.getModel();
			if (!(model instanceof AbstractComponentInfo)) {
				return;
			}
			widget = (AbstractComponentInfo) model;
		}
		// check if we can show alignment figures for this control
		{
			String showFiguresString = null;
			if (!LayoutPolicyUtils.shouldShowSideFigures(showFiguresString, editPart)) {
				return;
			}
		}
		// show alignment figures
		{
			int offset = INITIAL_RIGHT_SPACE;
			{
				IFigure horizontalFigure = createAlignmentFigure(widget, true);
				if (horizontalFigure != null) {
					offset += horizontalFigure.getSize().width;
					addAlignmentFigure(widget, horizontalFigure, offset);
					offset += FIGURES_SPACE;
				}
			}
			{
				IFigure verticalFigure = createAlignmentFigure(widget, false);
				if (verticalFigure != null) {
					offset += verticalFigure.getSize().width;
					addAlignmentFigure(widget, verticalFigure, offset);
					offset += FIGURES_SPACE;
				}
			}
		}
	}

	/**
	 * Adds alignment figure at given offset from right side of component's cells.
	 */
	private void addAlignmentFigure(AbstractComponentInfo component, IFigure figure, int offset) {
		IFigure layer = getLayer(IEditPartViewer.CLICKABLE_LAYER);
		// prepare rectangle for cells used by component (in layer coordinates)
		Rectangle cellRect;
		{
			cellRect = component.getModelBounds().getCopy();
			PolicyUtils.translateModelToFeedback(this, cellRect);
		}
		// prepare location and size
		Point figureLocation;
		{
			Dimension figureSize = figure.getSize();
			figureLocation = new Point(cellRect.right() - offset, cellRect.y - figureSize.height / 2);
			if (figureLocation.x < cellRect.x + MIN_LEFT_SPACE) {
				return;
			}
		}
		// add alignment figure
		layer.add(figure);
		figure.setLocation(figureLocation);
		m_alignmentFigures.add(figure);
	} ////////////////////////////////////////////////////////////////////////////

	//
	// Popup figures
	//
	////////////////////////////////////////////////////////////////////////////
	private class HorizontalPopupFigure extends AbstractPopupFigure {
		private final AbstractComponentInfo m_component;

		protected HorizontalPopupFigure(IEditPartViewer viewer, AbstractComponentInfo component) {
			super(viewer, 9, 5);
			m_component = component;
		}

		@Override
		protected ImageDescriptor getImageDescriptor() {
			int anchors = m_anchorsSupport.getCurrentAnchors(m_component, true);
			switch (anchors) {
			case AnchorsSupport.RESIZABLE :
				return CoreImages.ALIGNMENT_H_SMALL_FILL;
			case LEADING :
				return CoreImages.ALIGNMENT_H_SMALL_LEFT;
			case TRAILING :
				return CoreImages.ALIGNMENT_H_SMALL_RIGHT;
			default :
				return CoreImages.ALIGNMENT_H_SMALL_DEFAULT;
			}
		}

		@Override
		protected void fillMenu(IMenuManager manager) {
			m_anchorsSupport.fillContributionManager(m_component, manager, true);
		}
	}
	private class VerticalPopupFigure extends AbstractPopupFigure {
		private final AbstractComponentInfo m_component;

		protected VerticalPopupFigure(IEditPartViewer viewer, AbstractComponentInfo component) {
			super(viewer, 5, 9);
			m_component = component;
		}

		@Override
		protected ImageDescriptor getImageDescriptor() {
			int anchors = m_anchorsSupport.getCurrentAnchors(m_component, false);
			switch (anchors) {
			case AnchorsSupport.RESIZABLE :
				return CoreImages.ALIGNMENT_V_SMALL_FILL;
			case LEADING :
				return CoreImages.ALIGNMENT_V_SMALL_TOP;
			case TRAILING :
				return CoreImages.ALIGNMENT_V_SMALL_BOTTOM;
			default :
				return CoreImages.ALIGNMENT_V_SMALL_DEFAULT;
			}
		}

		@Override
		protected void fillMenu(IMenuManager manager) {
			m_anchorsSupport.fillContributionManager(m_component, manager, false);
		}
	}
}
