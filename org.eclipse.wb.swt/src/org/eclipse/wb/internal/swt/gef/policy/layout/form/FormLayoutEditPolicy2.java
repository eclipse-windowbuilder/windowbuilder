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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.OutlineImageFigure;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.IHeadersProvider;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.KeyboardMovingLayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.snapping.IFeedbackProxy;
import org.eclipse.wb.internal.core.gef.policy.snapping.ISnapPointsListener;
import org.eclipse.wb.internal.core.gef.policy.snapping.SnapPoint;
import org.eclipse.wb.internal.core.gef.policy.snapping.SnapPoints;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutPreferences;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mitin_aa
 */
public class FormLayoutEditPolicy2 extends KeyboardMovingLayoutEditPolicy
implements
IFeedbackProxy,
IHeadersProvider {
	private final FormLayoutInfo m_layout;
	private final FormLayoutVisualDataProvider m_visualDataProvider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutEditPolicy2(FormLayoutInfo layout) {
		super();
		m_layout = layout;
		m_visualDataProvider = new FormLayoutVisualDataProvider(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Decorate Child
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(EditPart child) {
		if (m_layout.getControls().contains(child.getModel())) {
			child.installEditPolicy(EditPolicy.SELECTION_ROLE, new FormSelectionEditPolicy2(m_layout));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ControlsLayoutRequestValidator.INSTANCE;
	}

	@Override
	protected String getResizeRequestType() {
		return AbsoluteBasedSelectionEditPolicy.REQ_RESIZE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	private int m_frozenYValue;
	private Figure m_createFeedback;
	private TextFeedback m_locationFeedback;
	private TextFeedback m_sizeFeedback;
	private SnapPoints m_snapPoints;

	@Override
	protected void showLayoutTargetFeedback(Request request) {
		// prepare snapping
		if (request instanceof ChangeBoundsRequest) {
			getMoveSnapPoints();
			showMoveFeedback((ChangeBoundsRequest) request);
		} else if (request instanceof CreateRequest) {
		} else if (request instanceof PasteRequest) {
		}
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		if (m_snapPoints != null) {
			m_snapPoints.removeFeedbacks();
			m_snapPoints = null;
		}
		if (m_moveFeedback != null) {
			removeFeedback(m_moveFeedback);
			m_moveFeedback = null;
		}
		if (m_locationFeedback != null) {
			m_locationFeedback.remove();
			m_locationFeedback = null;
		}
		m_frozenYValue = 0;
	}

	private boolean isFreezeVerticalAxis(Request request) {
		return request.isControlKeyPressed() && m_frozenYValue != 0;
	}

	private SnapPoints createSnapPoints(ISnapPointsListener listener) {
		List<IAbstractComponentInfo> allComponents = getAllComponents();
		FormLayoutSnapPointsProvider snapPointsProvider =
				new FormLayoutSnapPointsProvider(m_layout, m_visualDataProvider, allComponents);
		return new SnapPoints(m_visualDataProvider, this, snapPointsProvider, listener, allComponents);
	}

	/**
	 * @return special layer above feedback layer for to add text feedbacks
	 */
	private Layer getTextFeedbackLayer() {
		return getLayer(IEditPartViewer.FEEDBACK_LAYER_ABV_1);
	}

	/**
	 * Creates line figure, translates given model coordinates into feedback and adds created line
	 * into feedback layer
	 */
	protected final Polyline createLineFeedback(int x1, int y1, int x2, int y2) {
		// prepare points
		Point begin = new Point(x1, y1);
		Point end = new Point(x2, y2);
		translateModelToFeedback(begin);
		translateModelToFeedback(end);
		// create feedback
		Polyline line = new Polyline();
		line.addPoint(begin);
		line.addPoint(end);
		line.setLineStyle(SWT.LINE_DOT);
		addFeedback(line);
		return line;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFeedbackProxy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Figure addHorizontalFeedbackLine(int y, int x, int width) {
		Polyline line = createLineFeedback(x, y, x + width, y);
		line.setForegroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
		return line;
	}

	@Override
	public Figure addHorizontalMiddleLineFeedback(int y, int x, int width) {
		Polyline line = createLineFeedback(x, y, x + width, y);
		line.setForegroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
		line.setLineStyle(SWT.LINE_DASH);
		return line;
	}

	@Override
	public Figure addOutlineFeedback(Rectangle bounds) {
		// prepare bounds
		Rectangle feedbackBounds = bounds.getCopy();
		translateModelToFeedback(feedbackBounds);
		// create feedback
		Figure outline = new Figure();
		outline.setBorder(new LineBorder(AbsolutePolicyUtils.COLOR_OUTLINE));
		outline.setBounds(feedbackBounds);
		addFeedback(outline);
		return outline;
	}

	@Override
	public Figure addVerticalFeedbackLine(int x, int y, int height) {
		Polyline line = createLineFeedback(x, y, x, y + height);
		line.setForegroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
		return line;
	}

	@Override
	public Figure addVerticalMiddleLineFeedback(int x, int y, int height) {
		Polyline line = createLineFeedback(x, y, x, y + height);
		line.setForegroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
		line.setLineStyle(SWT.LINE_DASH);
		return line;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	private Figure m_moveFeedback;
	private MoveListener m_moveListener;

	private void getMoveSnapPoints() {
		if (m_snapPoints == null) {
			m_moveListener = new MoveListener();
			m_snapPoints = createSnapPoints(m_moveListener);
		}
	}

	private void showMoveFeedback(ChangeBoundsRequest request) {
		// first of all remove an old feedback
		if (m_moveFeedback != null) {
			removeFeedback(m_moveFeedback);
			m_moveFeedback = null;
		}
		// is this edit parts moved by using a keyboard
		boolean isKeyboardMoving = isKeyboardMoving();
		// some preparations
		List<EditPart> editParts = request.getEditParts();
		List<IAbstractComponentInfo> modelList = new ArrayList<>();
		Rectangle[] relativeBounds = new Rectangle[editParts.size()];
		Rectangle widgetBounds;
		// calculate model bounds and create move feedback
		if (editParts.size() > 1) {
			GraphicalEditPart firstEditPart = (GraphicalEditPart) editParts.get(0);
			widgetBounds = new Rectangle(firstEditPart.getFigure().getBounds());
			m_moveFeedback = new OutlineImageFigure(null);
			//
			for (EditPart editPart : editParts) {
				IAbstractComponentInfo model = (IAbstractComponentInfo) editPart.getModel();
				Rectangle bounds = ((GraphicalEditPart) editPart).getFigure().getBounds();
				//
				widgetBounds.union(bounds);
				modelList.add(model);
				//
				m_moveFeedback.add(new OutlineImageFigure(model.getImage(),
						AbsolutePolicyUtils.COLOR_OUTLINE), bounds);
			}
			//
			List<Figure> moveFeedbackFigures = m_moveFeedback.getChildren();
			for (int i = 0; i < moveFeedbackFigures.size(); ++i) {
				Figure figure = moveFeedbackFigures.get(i);
				figure.getBounds().performTranslate(-widgetBounds.x, -widgetBounds.y);
				relativeBounds[i] = figure.getBounds().getCopy();
			}
		} else {
			EditPart editPart = editParts.get(0);
			IAbstractComponentInfo model = (IAbstractComponentInfo) editPart.getModel();
			//
			widgetBounds = ((GraphicalEditPart) editPart).getFigure().getBounds().getCopy();
			relativeBounds[0] = new Rectangle(new Point(), widgetBounds.getSize());
			modelList.add(model);
			//
			m_moveFeedback = new OutlineImageFigure(model.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
		}
		// make figure bounds as absolute
		FigureUtils.translateFigureToAbsolute2(
				((GraphicalEditPart) editParts.get(0)).getFigure().getParent(),
				widgetBounds);
		// translate feedback coordinates into model coordinates (ex., SWT shell control
		// has caption and border area, so (0,0) point offset is border left and caption
		// size, i.e. insets.
		translateAbsoluteToModel(widgetBounds);
		// see where the widget(s) moved
		Point moveDelta = request.getMoveDelta();
		widgetBounds.x = widgetBounds.x + moveDelta.x;
		if (isFreezeVerticalAxis(request)) {
			widgetBounds.y = m_frozenYValue;
		} else {
			widgetBounds.y = m_frozenYValue = widgetBounds.y + moveDelta.y;
		}
		addFeedback(m_moveFeedback);
		// Case 42313
		Point moveLocation = isKeyboardMoving() ? moveDelta : request.getLocation();
		// do drag
		m_snapPoints.processBounds(moveLocation, modelList, widgetBounds, 0);
		// update move feedback: translate bounds back into feedback coordinates, apply bounds for feedback figure
		Rectangle feedbackBounds = widgetBounds.getCopy();
		translateModelToFeedback(feedbackBounds);
		m_moveFeedback.setBounds(feedbackBounds);
		// create text feedback
		if (m_locationFeedback == null && isShowTextFeedback() && !isKeyboardMoving) {
			m_locationFeedback = new TextFeedback(getTextFeedbackLayer());
			m_locationFeedback.add();
		}
		// update text feedback
		if (m_locationFeedback != null) {
			EditPart firstEditPart = editParts.get(0);
			m_locationFeedback.setText(getLocationHintText(firstEditPart, widgetBounds.x, widgetBounds.y));
			Point locationFeedbackLocation = getLocationHintLocation(feedbackBounds);
			m_locationFeedback.setLocation(locationFeedbackLocation);
		}
	}

	private Point getLocationHintLocation(Rectangle widgetBounds) {
		return widgetBounds.getLocation().getTranslated(-30, -25);
	}

	/**
	 * Return string that should be used in size hint for given size in pixels. Usually it returns
	 * just Integer.toString(), but some layouts, for example GWT AbsolutePanel support not only
	 * pixels, but also inches, centimeters, etc. We keep size units for such layouts, so want to show
	 * user size in current units, not in plain pixels.
	 *
	 * @param editPart
	 *          An {@link EditPart} which is currently resizing
	 * @param x
	 *          A current width of <code>editPart</code>
	 * @param y
	 *          A current height of <code>editPart</code>
	 * @return string representing current size of component
	 */
	private String getSizeHintString(EditPart editPart, int width, int height) {
		return MessageFormat.format("{0} x {1}", width, height);
	}

	/**
	 * Return string representing the current component location during moving
	 *
	 * @param editPart
	 *          An {@link EditPart} which is currently moving
	 * @param x
	 *          A current x location of <code>editPart</code>
	 * @param y
	 *          A current y location of <code>editPart</code>
	 * @return string representing the current component location during moving
	 */
	private String getLocationHintText(EditPart editPart, int x, int y) {
		return MessageFormat.format("{0} x {1}", x, y);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		return m_moveListener.getCommand();
	}

	private class MoveListener implements ISnapPointsListener {
		private CompoundEditCommand m_command;

		public Command getCommand() {
			return m_command;
		}

		@Override
		public void boundsChanged(Rectangle bounds,
				final List<? extends IAbstractComponentInfo> components,
				SnapPoint[] snapPoints,
				final int[] directions) {
			m_command = new CompoundEditCommand(m_layout);
			for (int i = 0; i < snapPoints.length; i++) {
				SnapPoint snapPoint = snapPoints[i];
				if (snapPoint != null) {
					m_command.add(snapPoint.getCommand());
				} else {
					final int moveDirection = directions[i];
					final boolean isHorizontal = i == 0;
					EditCommand command =
							new FormLayoutSnapPointsProvider.MoveFreelyCommand(m_layout,
									bounds,
									components,
									moveDirection,
									isHorizontal,
									m_visualDataProvider);
					m_command.add(command);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Helpers/Misc
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if move/resize hint feedback enabled for the layout
	 */
	private boolean isShowTextFeedback() {
		return true;
	}

	private List<IAbstractComponentInfo> getAllComponents() {
		List<IAbstractComponentInfo> components = new ArrayList<>();
		components.addAll(m_layout.getComposite().getChildrenControls());
		return components;
	}

	private void translateModelToFeedback(Translatable t) {
		PolicyUtils.translateModelToFeedback(this, t);
		t.performTranslate(m_visualDataProvider.getClientAreaOffset());
	}

	private void translateAbsoluteToModel(Translatable t) {
		PolicyUtils.translateAbsoluteToModel(this, t);
		t.performTranslate(m_visualDataProvider.getClientAreaOffset().getNegated());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHeadersProvider
	//
	////////////////////////////////////////////////////////////////////////////
	private FormHeaderLayoutEditPolicy m_headersPolicyHorizontal;
	private FormHeaderLayoutEditPolicy m_headersPolicyVertical;

	@Override
	public EditPart createHeaderEditPart(boolean isHorizontal, Object model) {
		return new FormHeaderEditPart(m_layout, model, isHorizontal, getHostFigure());
	}

	@Override
	public void buildContextMenu(IMenuManager manager, boolean isHorizontal) {
		FormHeaderLayoutEditPolicy headersPolicy =
				isHorizontal ? m_headersPolicyHorizontal : m_headersPolicyVertical;
		headersPolicy.buildContextMenu(manager);
	}

	@Override
	public LayoutEditPolicy getContainerLayoutPolicy(boolean isHorizontal) {
		FormHeaderLayoutEditPolicy headersPolicy =
				new FormHeaderLayoutEditPolicy(m_layout, this, isHorizontal);
		if (isHorizontal) {
			m_headersPolicyHorizontal = headersPolicy;
		} else {
			m_headersPolicyVertical = headersPolicy;
		}
		return headersPolicy;
	}

	@Override
	public List<?> getHeaders(boolean isHorizontal) {
		FormLayoutPreferences preferences = m_layout.getPreferences();
		List<Integer> values =
				isHorizontal ? preferences.getHorizontalPercents() : preferences.getVerticalPercents();
		List<FormLayoutPreferences.PercentsInfo> results = new ArrayList<>();
		for (Integer percent : values) {
			results.add(new FormLayoutPreferences.PercentsInfo(percent));
		}
		return results;
	}

	@Override
	public void handleDoubleClick(boolean isHorizontal) {
	}
}
