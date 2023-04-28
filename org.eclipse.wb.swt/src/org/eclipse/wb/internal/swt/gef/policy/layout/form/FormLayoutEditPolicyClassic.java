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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.OutlineImageFigure;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.IHeadersProvider;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.draw2d.geometry.Transposer;
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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.KeyboardMovingLayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutPreferences;
import org.eclipse.wb.internal.swt.model.layout.form.IFormAttachmentInfo;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Policy implementing 'Classic' version of the FormLayout support.
 *
 * @author mitin_aa
 */
public class FormLayoutEditPolicyClassic<C extends IControlInfo> extends KeyboardMovingLayoutEditPolicy
		implements IHeadersProvider {
	private static final int EXTENSION = 8;
	// model
	private final IFormLayoutInfo<C> layout;
	private final ObjectInfo layoutModel;
	private final FormLayoutInfoImplClassic<C> layoutImpl;
	// colors
	private static final Color offsetColor = AbsolutePolicyUtils.COLOR_FEEDBACK;
	private static final Color baseColor = DrawUtils.getShiftedColor(offsetColor, -32);
	private static final Color controlColor = DrawUtils.getShiftedColor(offsetColor, 32);
	// feedbacks
	private final Map<EditPart, List<Figure>> feedbacks = Maps.newHashMap();
	private Map<EditPart, Figure> moveFeedbacks;
	private int lastMouseQuadrant;
	private int frozenYValue;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutEditPolicyClassic(IFormLayoutInfo<C> layout) {
		this.layout = layout;
		this.layoutModel = layout.getUnderlyingModel();
		this.layoutImpl = (FormLayoutInfoImplClassic<C>) layout.getImpl();
		new BroadcastListenerHelper(layout.getUnderlyingModel(), this, new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				showSelectionFeedbacks();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Decorate Child
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(EditPart child) {
		if (layout.getControls().contains(child.getModel())) {
			child.installEditPolicy(EditPolicy.SELECTION_ROLE, new FormSelectionEditPolicyClassic<C>(layout));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback routing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void showLayoutTargetFeedback(final Request request) {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				if (request instanceof ChangeBoundsRequest) {
					showMoveFeedback((ChangeBoundsRequest) request);
				} else if (request instanceof CreateRequest) {
					showCreateFeedback((CreateRequest) request);
				} else if (request instanceof PasteRequest) {
					showPasteFeedback((PasteRequest) request);
				}
			}
		});
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		if (moveFeedbacks != null) {
			for (Figure feedback : moveFeedbacks.values()) {
				removeFeedback(feedback);
			}
			moveFeedbacks = null;
		}
		removeFeedbacks();
		frozenYValue = 0;
	}

	@Override
	protected String getResizeRequestType() {
		return AbsoluteBasedSelectionEditPolicy.REQ_RESIZE;
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Move feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private void showMoveFeedback(ChangeBoundsRequest request) throws Exception {
		moveChildredCommand = new CompoundEditCommand(layoutModel);
		for (EditPart part : request.getEditParts()) {
			showMoveFeedback(request, (GraphicalEditPart) part);
		}
	}

	private void showMoveFeedback(ChangeBoundsRequest request, GraphicalEditPart part) throws Exception {
		Rectangle widgetBounds = part.getFigure().getBounds().getCopy();
		// make figure bounds as absolute
		FigureUtils.translateFigureToAbsolute2(part.getFigure().getParent(), widgetBounds);
		// translate feedback coordinates into model coordinates (ex., SWT shell control
		// has caption and border area, so (0,0) point offset is border left and caption
		// size, i.e. insets.
		translateAbsoluteToModel(widgetBounds);
		// see where the widget(s) moved
		Point moveDelta = request.getMoveDelta();
		widgetBounds.x = widgetBounds.x + moveDelta.x;
		if (isFreezeVerticalAxis(request)) {
			widgetBounds.y = frozenYValue;
		} else {
			widgetBounds.y = frozenYValue = widgetBounds.y + moveDelta.y;
		}
		showMoveFeedback(part, widgetBounds);
	}

	@SuppressWarnings("unchecked")
	private void showMoveFeedback(GraphicalEditPart part, Rectangle bounds) throws Exception {
		final C control = (C) part.getModel();
		// check for re-parenting: add move command
		if (control.getParent() != layout.getComposite()) {
			moveChildredCommand.add(new EditCommand(layoutModel) {
				@Override
				protected void executeEdit() throws Exception {
					layout.commandMove(control, null);
				}
			});
		}
		// prepare FormSelectionEditPolicy
		FormSelectionEditPolicyClassic<C> formSelectionPolicy = null;
		{
			EditPolicy selectionPolicy = part.getEditPolicy(EditPolicy.SELECTION_ROLE);
			if (selectionPolicy instanceof FormSelectionEditPolicyClassic) {
				formSelectionPolicy = (FormSelectionEditPolicyClassic<C>) selectionPolicy;
			}
		}
		// prepare quadrant
		int quadrant = 0;
		if (formSelectionPolicy != null) {
			if (!isKeyboardMoving()) {
				quadrant = formSelectionPolicy.getMouseQuadrant();
				if (quadrant == -1) {
					quadrant = lastMouseQuadrant;
				}
			}
		}
		lastMouseQuadrant = quadrant;
		if (moveFeedbacks == null) {
			moveFeedbacks = Maps.newHashMap();
		}
		// prepare change bounds feedback
		Figure moveFeedback = moveFeedbacks.get(part);
		if (moveFeedback == null) {
			moveFeedback = new OutlineImageFigure(control.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
			moveFeedbacks.put(part, moveFeedback);
			addFeedback(moveFeedback);
		}
		// location hints feedback
		String xText = "", yText = "";
		TextFeedback xTextFeedback = getTextFeedback(xTextFeedbacks, part, true);
		TextFeedback yTextFeedback = getTextFeedback(yTextFeedbacks, part, false);
		// try to get form data info for to get an attachment if exists
		IFormAttachmentInfo<C> leftAttachment = layoutImpl.getAttachment(control, IPositionConstants.LEFT);
		IFormAttachmentInfo<C> rightAttachment = layoutImpl.getAttachment(control, IPositionConstants.RIGHT);
		IFormAttachmentInfo<C> topAttachment = layoutImpl.getAttachment(control, IPositionConstants.TOP);
		IFormAttachmentInfo<C> bottomAttachment = layoutImpl.getAttachment(control, IPositionConstants.BOTTOM);
		// hide any old move feedbacks (it is more easy to add each time new feedback
		// than track old ones and change their positions)
		removeFeedbacks(part);
		if (formSelectionPolicy != null) {
			formSelectionPolicy.hideSelection();
		}
		// get location
		Point location = bounds.getTopLeft();
		// find attachable controls
		List<C> hAttachables = getAttachableControls(null, true, false);
		List<C> vAttachables = getAttachableControls(null, false, false);
		// West
		if (quadrant == 0 || quadrant == 2) {
			xText = showLeftSideFeedbacks(part, control, bounds, location, xText, leftAttachment, hAttachables, false);
		}
		// North
		if (quadrant == 0 || quadrant == 1) {
			yText = showTopSideFeedbacks(part, control, bounds, location, yText, topAttachment, vAttachables, false);
		}
		// East
		if (quadrant == 1 || quadrant == 3) {
			xText = showRightSideFeedbacks(part, control, bounds, xText, rightAttachment, hAttachables, false);
		}
		// South
		if (quadrant == 2 || quadrant == 3) {
			yText = showBottomSideFeedbacks(part, control, bounds, yText, bottomAttachment, vAttachables, false);
		}
		// Show feedback rectangle and location hints
		{
			ICompositeInfo composite = layout.getComposite();
			Dimension parentSize = composite.getClientArea().getSize();
			Rectangle feedbackBounds = bounds.getCopy();
			translateModelToFeedback(feedbackBounds);
			moveFeedback.setBounds(feedbackBounds);
			updateTextFeedback(xTextFeedback, xText, bounds, parentSize, quadrant == 0 || quadrant == 2, true);
			updateTextFeedback(yTextFeedback, yText, bounds, parentSize, quadrant == 0 || quadrant == 1, false);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move/Re-parent command
	//
	////////////////////////////////////////////////////////////////////////////
	private CompoundEditCommand moveChildredCommand;

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		return moveChildredCommand;
	}

	@Override
	protected Command getAddCommand(ChangeBoundsRequest request) {
		return moveChildredCommand;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private Point startLocation;

	private void showCreateFeedback(CreateRequest request) throws Exception {
		@SuppressWarnings("unchecked")
		final C newChild = (C) request.getNewObject();
		// prepare create command
		createCommand = new CompoundEditCommand(layoutModel);
		createCommand.add(new EditCommand(layout) {
			@Override
			protected void executeEdit() throws Exception {
				layout.commandCreate(newChild, null);
			}
		});
		showCreateFeedback(request, newChild);
	}

	private void showCreateFeedback(CreateRequest request, C newChild) throws Exception {
		if (moveFeedbacks == null) {
			moveFeedbacks = Maps.newHashMap();
		}
		// hide any old create feedbacks (it is more easy to add each time new feedback
		// than track old ones and change their positions)
		removeFeedbacks();
		Figure moveFeedback = moveFeedbacks.get(getHost());
		Image image = newChild.getImage();
		if (moveFeedback == null) {
			moveFeedback = new OutlineImageFigure(image);
			moveFeedbacks.put(getHost(), moveFeedback);
			addFeedback(moveFeedback);
		}
		// host
		GraphicalEditPart host = getHost();
		// location hints
		String xText = "", yText = "";
		TextFeedback locationFeedbackX = getTextFeedback(xTextFeedbacks, host, true);
		TextFeedback locationFeedbackY = getTextFeedback(yTextFeedbacks, host, false);
		// location and parent dimensions
		Point loc = request.getLocation().getCopy();
		ICompositeInfo composite = layout.getComposite();
		Dimension parentSize = composite.getClientArea().getSize();
		translateAbsoluteToModel(loc);
		// prepare feedback and parent bounds
		// set size
		Dimension size = request.getSize();
		Point topLeftPoint;
		Dimension preferredSize;
		if (size != null) {
			// size-on-drop info update
			topLeftPoint = new Point(startLocation.x, startLocation.y);
			preferredSize = new Dimension(size.width + loc.x - startLocation.x, size.height + loc.y - startLocation.y);
			// prevent axis fixing
			frozenYValue = 0;
		} else {
			preferredSize = newChild.getPreferredSize();
			int y;
			// freeze vertical axis if needed
			boolean freezeVerticalAxis = isFreezeVerticalAxis(request);
			if (freezeVerticalAxis) {
				y = frozenYValue;
			} else {
				y = frozenYValue = loc.y - preferredSize.height / 2;
			}
			topLeftPoint = new Point(loc.x - preferredSize.width / 2, y);
		}
		Rectangle bounds = new Rectangle(topLeftPoint.x, topLeftPoint.y, preferredSize.width, preferredSize.height);
		// store drag start location
		if (size == null) {
			startLocation = bounds.getLocation();
		}
		// get attachable controls
		List<C> ctrls = getAttachableControls(null, false, true);
		// West
		{
			xText = showLeftSideFeedbacks(host, newChild, bounds, loc, xText, null, ctrls, true);
			updateTextFeedback(locationFeedbackX, xText, bounds, parentSize, true, true);
		}
		// North
		{
			yText = showTopSideFeedbacks(host, newChild, bounds, loc, yText, null, ctrls, true);
			updateTextFeedback(locationFeedbackY, yText, bounds, parentSize, true, false);
		}
		// if user draws the control
		if (size != null) {
			if (createControlHintFeedbackX == null) {
				createControlHintFeedbackX = createTextFeedback(true);
				createControlHintFeedbackX.add();
			}
			if (createControlHintFeedbackY == null) {
				createControlHintFeedbackY = createTextFeedback(false);
				createControlHintFeedbackY.add();
			}
			// East
			{
				xText = showRightSideFeedbacks(host, newChild, bounds, xText, null, ctrls, true);
				updateTextFeedback(createControlHintFeedbackX, xText, bounds, parentSize, false, true);
			}
			// South
			{
				yText = showBottomSideFeedbacks(host, newChild, bounds, yText, null, ctrls, true);
				updateTextFeedback(createControlHintFeedbackY, yText, bounds, parentSize, false, false);
			}
		}
		// Show feedback rectangle
		Rectangle feedbackBounds = bounds.getCopy();
		translateModelToFeedback(feedbackBounds);
		moveFeedback.setBounds(feedbackBounds);
	}

	private void updateTextFeedback(TextFeedback feedback, String text, Rectangle bounds, Dimension parentSize_,
			boolean isLeading, boolean isHorizontal) {
		feedback.setText(text);
		Transposer t = new Transposer(!isHorizontal);
		Dimension textSize = t.t(feedback.getSize());
		Rectangle textBounds = t.t(bounds.getCopy());
		Dimension parentSize = t.t(parentSize_.getCopy());
		textBounds.y = parentSize.height + 1;
		int x = isLeading ? textBounds.x : textBounds.right();
		x -= textSize.width / 2;
		if (x < 0) {
			x = 0;
		} else if (x + textSize.width > parentSize.width) {
			x = parentSize.width - textSize.width;
		}
		textBounds.x = x;
		textBounds = t.t(textBounds);
		translateModelToFeedback(textBounds);
		feedback.setLocation(textBounds.getLocation());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create command
	//
	////////////////////////////////////////////////////////////////////////////
	private CompoundEditCommand createCommand;

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		// return command created during feedback showing
		return createCommand;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paste
	//
	////////////////////////////////////////////////////////////////////////////
	private void showPasteFeedback(PasteRequest request) throws Exception {
		List<IObjectInfo> pastingComponents = GlobalState.getPasteRequestProcessor().getPastingComponents(request);
		if (moveFeedbacks == null) {
			moveFeedbacks = Maps.newHashMap();
		}
		// remove create feedback
		if (moveFeedbacks == null) {
			moveFeedbacks = Maps.newHashMap();
		}
		// hide any old create feedbacks
		removeFeedbacks();
		// host
		GraphicalEditPart host = getHost();
		// location hints
		String xText = "", yText = "";
		TextFeedback locationFeedbackX = getTextFeedback(xTextFeedbacks, host, true);
		TextFeedback locationFeedbackY = getTextFeedback(yTextFeedbacks, host, false);
		//
		Point loc = request.getLocation().getCopy();
		translateAbsoluteToModel(loc);
		//
		Figure moveFeedback = moveFeedbacks.get(getHost());
		Rectangle bounds;
		// calculate model bounds and create move feedback
		Rectangle[] relativeBounds = new Rectangle[pastingComponents.size()];
		if (pastingComponents.size() > 1) {
			if (moveFeedback == null) {
				moveFeedback = new OutlineImageFigure(null);
				moveFeedbacks.put(getHost(), moveFeedback);
				addFeedback(moveFeedback);
			}
			bounds = new Rectangle();
			// calculate widgets' relative placement
			int offsetX = Integer.MAX_VALUE;
			int offsetY = Integer.MAX_VALUE;
			for (IObjectInfo objInfo : pastingComponents) {
				@SuppressWarnings("unchecked")
				C model = (C) objInfo;
				Rectangle modelBounds = model.getBounds().getCopy();
				offsetX = Math.min(offsetX, modelBounds.x);
				offsetY = Math.min(offsetY, modelBounds.y);
			}
			// make union rectangle with relative bounds and prepare union rectangle
			// feedback
			for (int i = 0; i < pastingComponents.size(); ++i) {
				C control = getControlFromList(pastingComponents, i);
				Rectangle modelBounds = control.getBounds().getCopy();
				relativeBounds[i] = new Rectangle(modelBounds.x - offsetX, modelBounds.y - offsetY, modelBounds.width,
						modelBounds.height);
				bounds.union(relativeBounds[i]);
				moveFeedback.add(new OutlineImageFigure(control.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE),
						relativeBounds[i]);
			}
		} else {
			C model = getControlFromList(pastingComponents, 0);
			//
			bounds = new Rectangle(new Point(0, 0), model.getBounds().getSize());
			relativeBounds[0] = bounds.getCopy();
			//
			if (moveFeedback == null) {
				moveFeedback = new OutlineImageFigure(model.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
				moveFeedbacks.put(getHost(), moveFeedback);
				addFeedback(moveFeedback);
			}
		}
		bounds.x = loc.x - bounds.width / 2;
		if (isFreezeVerticalAxis(request)) {
			bounds.y = frozenYValue;
		} else {
			bounds.y = frozenYValue = loc.y - bounds.height / 2;
		}
		List<C> ctrls = getAttachableControls(null, false, true);
		ICompositeInfo composite = layout.getComposite();
		Dimension parentSize = composite.getClientArea().getSize();
		// prepare create command
		createCommand = new CompoundEditCommand(layoutModel);
		createCommand
				.add(GlobalState.getPasteRequestProcessor().getPasteCommand(request, new IPasteComponentProcessor() {
					@Override
					@SuppressWarnings("unchecked")
					public void process(Object component) throws Exception {
						layout.commandCreate((C) component, null);
					}
				}));
		for (int i = 0; i < pastingComponents.size(); ++i) {
			final C pasted = getControlFromList(pastingComponents, i);
			final Dimension size = new Dimension(relativeBounds[i].width, relativeBounds[i].height);
			final Rectangle pastedBounds = new Rectangle(bounds.x + relativeBounds[i].x, bounds.y + relativeBounds[i].y,
					relativeBounds[i].width, relativeBounds[i].height);
			// West
			{
				xText = showLeftSideFeedbacks(host, pasted, pastedBounds, loc, xText, null, ctrls, true);
				if (i == 0) {
					updateTextFeedback(locationFeedbackX, xText, pastedBounds, parentSize, true, true);
				}
			}
			// North
			{
				yText = showTopSideFeedbacks(host, pasted, pastedBounds, loc, yText, null, ctrls, true);
				if (i == 0) {
					updateTextFeedback(locationFeedbackY, yText, pastedBounds, parentSize, true, false);
				}
			}
			// keep size
			createCommand.add(new EditCommand(layout) {
				@Override
				protected void executeEdit() throws Exception {
					if (size.width != pasted.getPreferredSize().width) {
						layout.setAttachmentOffset(pasted, IPositionConstants.RIGHT, pastedBounds.x + size.width);
					}
					if (size.height != pasted.getPreferredSize().height) {
						layout.setAttachmentOffset(pasted, IPositionConstants.BOTTOM, pastedBounds.y + size.height);
					}
				}
			});
		}
		Rectangle feedbackBounds = bounds.getCopy();
		translateModelToFeedback(feedbackBounds);
		// update paste feedback
		moveFeedback.setBounds(feedbackBounds);
	}

	@SuppressWarnings("unchecked")
	private C getControlFromList(List<IObjectInfo> list, int i) {
		return (C) list.get(i);
	}

	@Override
	protected Command getPasteCommand(PasteRequest request) {
		return createCommand;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Side feedbacks shared methods
	//
	////////////////////////////////////////////////////////////////////////////
	private String showBottomSideFeedbacks(GraphicalEditPart part, C control, Rectangle bounds, String locationY,
			IFormAttachmentInfo<C> bottomAttachment, List<C> attachables, boolean createControl) {
		int layoutMarginTop = FormUtils.getLayoutMarginTop(layout);
		// prepare values from preferences
		int sens = layout.getPreferences().getSnapSensitivity();
		int vMargin = layout.getPreferences().getVerticalContainerGap();
		int vpMargin = layout.getPreferences().getVerticalPercentsGap();
		int vwMargin = layout.getPreferences().getVerticalComponentGap();
		int parentHeight = layout.getContainerSize().height;
		int y = bounds.bottom();
		boolean controlFound = false;
		Command cmd = null;
		if (useSnap()) {
			if (vwMargin > -1) {
				C child = null;
				int cmdSide = 0;
				int cmdOffset = 0;
				for (Iterator<C> I = sortControlsByAxisRange(attachables, true, bounds.right()).iterator(); I.hasNext()
						&& !controlFound;) {
					child = I.next();
					Rectangle childBounds = FormSelectionEditPolicyClassic.getControlModelBounds(child);
					int left = Math.min(bounds.x, childBounds.x);
					int right = Math.max(bounds.right(), childBounds.right());
					if (FormUtils.between(childBounds.y - vwMargin - y, 0, sens)) {
						controlFound = true;
						setVBounds(bounds, childBounds.y - vwMargin, createControl);
						addVLine(part, child, childBounds.y, controlColor);
						addVLine(part, left, right, childBounds.y - vwMargin, offsetColor);
						locationY = getChildSideString(child, "y") + "-" + vwMargin;
						cmdSide = IPositionConstants.TOP;
						cmdOffset = -vwMargin;
					} else if (FormUtils.between(childBounds.y - y, -sens, vwMargin)) {
						controlFound = true;
						setVBounds(bounds, childBounds.y, createControl);
						addVLine(part, left, right, childBounds.y, offsetColor);
						addVLine(part, child, childBounds.y, controlColor);
						locationY = getChildSideString(child, "y");
						cmdSide = IPositionConstants.TOP;
						cmdOffset = 0;
					} else if (FormUtils.between(childBounds.bottom() - y, -sens, sens)) {
						controlFound = true;
						setVBounds(bounds, childBounds.bottom(), createControl);
						addVLine(part, left, right, childBounds.bottom(), offsetColor);
						addVLine(part, child, childBounds.bottom(), controlColor);
						locationY = getChildSideString(child, "bottom");
						cmdSide = IPositionConstants.BOTTOM;
						cmdOffset = 0;
					}
				}
				if (controlFound) {
					cmd = getBindToControlCommand(createControl, control, IPositionConstants.BOTTOM, child, cmdSide,
							cmdOffset);
				}
			}
			if (!controlFound) {
				boolean snappedToPercent = false;
				List<Integer> formLayoutVPercents = layout.getPreferences().getVerticalPercents();
				for (Integer percent : formLayoutVPercents) {
					int percentY = (int) ((double) percent.intValue() / 100 * parentHeight) + layoutMarginTop;
					int offset = 0;
					if (FormUtils.between(percentY - vpMargin - y, 0, sens) && vpMargin > -1) {
						setVBounds(bounds, percentY - vpMargin, createControl);
						addVLine(part, percentY, baseColor);
						addVLine(part, percentY - vpMargin, offsetColor);
						locationY = percent.toString() + "%-" + vpMargin;
						offset = -vpMargin;
						snappedToPercent = true;
					} else if (FormUtils.between(percentY - y, -sens, vpMargin)) {
						setVBounds(bounds, percentY, createControl);
						addVLine(part, percentY, offsetColor);
						locationY = percent.toString() + "%";
						snappedToPercent = true;
					}
					if (snappedToPercent) {
						if (createControl) {
							cmd = new CreateToPercentOffsetCommand(control, IPositionConstants.BOTTOM,
									percent.intValue(), offset);
						} else {
							cmd = new MoveToPercentOffsetCommand(control, IPositionConstants.BOTTOM, percent.intValue(),
									offset);
						}
						break;
					}
				}
				if (!snappedToPercent) {
					int bottomPoint = parentHeight - vMargin + layoutMarginTop;
					if (vMargin > -1 && y > bottomPoint) {
						setVBounds(bounds, bottomPoint, createControl);
						addVLine(part, bottomPoint, offsetColor);
						locationY = "--> " + String.valueOf(vMargin);
						if (createControl) {
							cmd = new CreateToMarginCommand(control, IPositionConstants.BOTTOM,
									IPositionConstants.BOTTOM, vMargin);
						} else {
							cmd = new MoveToMarginCommand(control, IPositionConstants.BOTTOM, IPositionConstants.BOTTOM,
									vMargin);
						}
					} else {
						setVBounds(bounds, FormUtils.snapGrid(bounds.bottom(), sens), createControl);
						locationY = getLocationString(bottomAttachment, bounds.bottom() - layoutMarginTop,
								parentHeight);
						if (createControl) {
							cmd = new CreateToOffsetCommand(control, IPositionConstants.BOTTOM, parentHeight,
									FormUtils.snapGrid(y - layoutMarginTop, sens));
						} else {
							cmd = new MoveToOffsetCommand(control, IPositionConstants.BOTTOM, parentHeight,
									FormUtils.snapGrid(y - layoutMarginTop, sens));
						}
					}
				}
			}
		} else {
			locationY = getLocationString(bottomAttachment, bounds.bottom(), parentHeight);
			if (createControl) {
				cmd = new CreateToOffsetCommand(control, IPositionConstants.BOTTOM, parentHeight, y - layoutMarginTop);
			} else {
				cmd = new MoveToOffsetCommand(control, IPositionConstants.BOTTOM, parentHeight, y - layoutMarginTop);
			}
		}
		addVLine(part, bounds.bottom(), layout.getComposite().getClientArea().width, IColorConstants.red);
		if (createControl) {
			createCommand.add(cmd);
		} else {
			moveChildredCommand.add(cmd);
		}
		return locationY;
	}

	private String showRightSideFeedbacks(GraphicalEditPart part, C control, Rectangle bounds, String locationX,
			IFormAttachmentInfo<C> rightAttachment, List<C> attachables, boolean createControl) {
		// prepare values from preferences
		int layoutMarginLeft = FormUtils.getLayoutMarginLeft(layout);
		int hMargin = layout.getPreferences().getHorizontalContainerGap();
		int hwMargin = layout.getPreferences().getHorizontalComponentGap();
		int hpMargin = layout.getPreferences().getHorizontalPercentsGap();
		int sens = layout.getPreferences().getSnapSensitivity();
		// prepare feedback and parent bounds
		int parentWidth = layout.getContainerSize().width;
		int x = bounds.right();
		boolean controlFound = false;
		Command cmd = null;
		if (useSnap()) {
			if (hwMargin > -1) {
				C child = null;
				int cmdSide = 0;
				int cmdOffset = 0;
				for (Iterator<C> I = sortControlsByAxisRange(attachables, false, bounds.bottom()).iterator(); I
						.hasNext() && !controlFound;) {
					child = I.next();
					Rectangle childBounds = FormSelectionEditPolicyClassic.getControlModelBounds(child);
					int top = Math.min(bounds.y, childBounds.y);
					int bottom = Math.max(bounds.bottom(), childBounds.bottom());
					if (FormUtils.between(childBounds.x - hwMargin - x, 0, sens)) {
						controlFound = true;
						setHBounds(bounds, childBounds.x - hwMargin, createControl);
						addHLine(part, child, childBounds.x, controlColor);
						addHLine(part, top, bottom, childBounds.x - hwMargin, offsetColor);
						locationX = getChildSideString(child, "x") + "-" + hwMargin;
						cmdSide = IPositionConstants.LEFT;
						cmdOffset = -hwMargin;
					} else if (FormUtils.between(childBounds.x - x, -sens, hwMargin)) {
						controlFound = true;
						setHBounds(bounds, childBounds.x, createControl);
						addHLine(part, top, bottom, childBounds.x, offsetColor);
						addHLine(part, child, childBounds.x, controlColor);
						locationX = getChildSideString(child, "x");
						cmdSide = IPositionConstants.LEFT;
						cmdOffset = 0;
					} else if (FormUtils.between(childBounds.right() - x, -sens, sens)) {
						controlFound = true;
						setHBounds(bounds, childBounds.right(), createControl);
						addHLine(part, top, bottom, childBounds.right(), offsetColor);
						addHLine(part, child, childBounds.right(), controlColor);
						locationX = getChildSideString(child, "right");
						cmdSide = IPositionConstants.RIGHT;
						cmdOffset = 0;
					}
				}
				if (controlFound) {
					cmd = getBindToControlCommand(createControl, control, IPositionConstants.RIGHT, child, cmdSide,
							cmdOffset);
				}
			}
			if (!controlFound) {
				boolean snappedToPercent = false;
				List<Integer> formLayoutHPercents = layout.getPreferences().getHorizontalPercents();
				for (Integer percent : formLayoutHPercents) {
					int percentX = (int) ((double) percent.intValue() / 100 * parentWidth) + layoutMarginLeft;
					int offset = 0;
					if (FormUtils.between(percentX - hpMargin - x, 0, sens) && hpMargin > -1) {
						setHBounds(bounds, percentX - hpMargin, createControl);
						addHLine(part, percentX, baseColor);
						addHLine(part, percentX - hpMargin, offsetColor);
						locationX = percent.toString() + "%-" + hpMargin;
						offset = hpMargin;
						snappedToPercent = true;
					} else if (FormUtils.between(percentX - x, -sens, hwMargin)) {
						setHBounds(bounds, percentX, createControl);
						addHLine(part, percentX, offsetColor);
						locationX = percent.toString() + "%";
						snappedToPercent = true;
					}
					if (snappedToPercent) {
						if (createControl) {
							cmd = new CreateToPercentOffsetCommand(control, IPositionConstants.RIGHT,
									percent.intValue(), -offset);
						} else {
							cmd = new MoveToPercentOffsetCommand(control, IPositionConstants.RIGHT, percent.intValue(),
									-offset);
						}
						break;
					}
				}
				if (!snappedToPercent) {
					int rightPoint = parentWidth - hMargin + layoutMarginLeft;
					if (hMargin > -1 && x > rightPoint) {
						setHBounds(bounds, rightPoint, createControl);
						addHLine(part, rightPoint, offsetColor);
						locationX = String.valueOf(-hMargin);
						locationX = "--> " + String.valueOf(hMargin);
						if (createControl) {
							cmd = new CreateToMarginCommand(control, IPositionConstants.RIGHT, IPositionConstants.RIGHT,
									hMargin);
						} else {
							cmd = new MoveToMarginCommand(control, IPositionConstants.RIGHT, IPositionConstants.RIGHT,
									hMargin);
						}
					} else {
						setHBounds(bounds, FormUtils.snapGrid(bounds.right(), sens), createControl);
						locationX = getLocationString(rightAttachment, bounds.right() - layoutMarginLeft, parentWidth);
						if (createControl) {
							cmd = new CreateToOffsetCommand(control, IPositionConstants.RIGHT, parentWidth,
									FormUtils.snapGrid(x - layoutMarginLeft, sens));
						} else {
							cmd = new MoveToOffsetCommand(control, IPositionConstants.RIGHT, parentWidth,
									FormUtils.snapGrid(x - layoutMarginLeft, sens));
						}
					}
				}
			}
		} else {
			locationX = getLocationString(rightAttachment, bounds.right() - layoutMarginLeft, parentWidth);
			if (createControl) {
				cmd = new CreateToOffsetCommand(control, IPositionConstants.RIGHT, parentWidth, x - layoutMarginLeft);
			} else {
				cmd = new MoveToOffsetCommand(control, IPositionConstants.RIGHT, parentWidth, x - layoutMarginLeft);
			}
		}
		addHLine(part, bounds.right(), layout.getComposite().getClientArea().height, IColorConstants.red);
		if (createControl) {
			createCommand.add(cmd);
		} else {
			moveChildredCommand.add(cmd);
		}
		return locationX;
	}

	private String showTopSideFeedbacks(final GraphicalEditPart part, C control, Rectangle bounds, Point location,
			String locationY, IFormAttachmentInfo<C> topAttachment, List<C> attachables, boolean createControl) {
		int layoutMarginTop = FormUtils.getLayoutMarginTop(layout);
		int vModelMargin = layout.getPreferences().getVerticalContainerGap();
		int vMargin = vModelMargin + layoutMarginTop;
		// prepare values from preferences
		int sens = layout.getPreferences().getSnapSensitivity();
		int vpMargin = layout.getPreferences().getVerticalPercentsGap();
		int vwMargin = layout.getPreferences().getVerticalComponentGap();
		int parentHeight = layout.getContainerSize().height;
		int y = bounds.y;
		boolean controlFound = false;
		Command cmd = null;
		if (useSnap()) {
			if (vwMargin > -1) {
				C child = null;
				int cmdSide = 0;
				int cmdOffset = 0;
				for (Iterator<C> I = sortControlsByAxisRange(attachables, true, location.x).iterator(); I.hasNext()
						&& !controlFound;) {
					child = I.next();
					Rectangle childBounds = FormSelectionEditPolicyClassic.getControlModelBounds(child);
					int left = Math.min(bounds.x, childBounds.x);
					int right = Math.max(bounds.right(), childBounds.right());
					if (FormUtils.between(y - (childBounds.bottom() + vwMargin), 0, sens)) {
						controlFound = true;
						bounds.y = childBounds.bottom() + vwMargin;
						addVLine(part, child, childBounds.bottom(), controlColor);
						addVLine(part, left, right, childBounds.bottom() + vwMargin, offsetColor);
						locationY = getChildSideString(child, "bottom") + "+" + vwMargin;
						cmdSide = IPositionConstants.BOTTOM;
						cmdOffset = vwMargin;
					} else if (FormUtils.between(y - childBounds.bottom(), -sens, vwMargin)) {
						controlFound = true;
						bounds.y = childBounds.bottom();
						addVLine(part, left, right, childBounds.bottom(), offsetColor);
						addVLine(part, child, childBounds.bottom(), controlColor);
						locationY = getChildSideString(child, "bottom");
						cmdSide = IPositionConstants.BOTTOM;
						cmdOffset = 0;
					} else if (FormUtils.between(y - childBounds.y, -sens, sens)) {
						controlFound = true;
						bounds.y = childBounds.y;
						addVLine(part, left, right, childBounds.y, offsetColor);
						addVLine(part, child, childBounds.y, controlColor);
						locationY = getChildSideString(child, "y");
						cmdSide = IPositionConstants.TOP;
						cmdOffset = 0;
					}
				}
				if (controlFound) {
					cmd = getBindToControlCommand(createControl, control, IPositionConstants.TOP, child, cmdSide,
							cmdOffset);
				}
			}
			if (!controlFound) {
				boolean snappedToPercent = false;
				List<Integer> formLayoutVPercents = layout.getPreferences().getVerticalPercents();
				for (Integer percent : formLayoutVPercents) {
					int percentY = (int) ((double) percent.intValue() / 100 * parentHeight) + layoutMarginTop;
					int offset = 0;
					if (FormUtils.between(y - (percentY + vpMargin), 0, sens) && vpMargin > -1) {
						bounds.y = percentY + vpMargin;
						addVLine(part, percentY, baseColor);
						addVLine(part, percentY + vpMargin, offsetColor);
						locationY = percent.toString() + "%+" + vpMargin;
						offset = vpMargin;
						snappedToPercent = true;
					} else if (FormUtils.between(y - percentY, -sens, vwMargin)) {
						bounds.y = percentY;
						addVLine(part, percentY, offsetColor);
						locationY = percent.toString() + "%";
						snappedToPercent = true;
					}
					if (snappedToPercent) {
						if (createControl) {
							cmd = new CreateToPercentOffsetCommand(control, IPositionConstants.TOP, percent.intValue(),
									offset);
						} else {
							cmd = new MoveToPercentOffsetCommand(control, IPositionConstants.TOP, percent.intValue(),
									offset);
						}
						break;
					}
				}
				if (!snappedToPercent) {
					if (vMargin > -1 && y < vMargin) {
						bounds.y = vMargin;
						addVLine(part, vMargin, offsetColor);
						if (createControl) {
							cmd = new CreateToMarginCommand(control, IPositionConstants.TOP, IPositionConstants.TOP,
									vModelMargin);
						} else {
							cmd = new MoveToMarginCommand(control, IPositionConstants.TOP, IPositionConstants.TOP,
									vModelMargin);
						}
					} else {
						bounds.y = FormUtils.snapGrid(y, sens);
						if (createControl) {
							cmd = new CreateToOffsetCommand(control, IPositionConstants.TOP, parentHeight,
									FormUtils.snapGrid(y - layoutMarginTop, sens));
						} else {
							cmd = new MoveToOffsetCommand(control, IPositionConstants.TOP, parentHeight,
									FormUtils.snapGrid(y - layoutMarginTop, sens));
						}
					}
					locationY = getLocationString(topAttachment, bounds.y - layoutMarginTop, parentHeight);
				}
			}
		} else {
			locationY = getLocationString(topAttachment, bounds.y - layoutMarginTop, parentHeight);
			if (createControl) {
				cmd = new CreateToOffsetCommand(control, IPositionConstants.TOP, parentHeight, y - layoutMarginTop);
			} else {
				cmd = new MoveToOffsetCommand(control, IPositionConstants.TOP, parentHeight, y - layoutMarginTop);
			}
		}
		addVLine(part, bounds.y, layout.getComposite().getClientArea().width, IColorConstants.red);
		if (createControl) {
			createCommand.add(cmd);
		} else {
			moveChildredCommand.add(cmd);
		}
		return locationY;
	}

	private String showLeftSideFeedbacks(final GraphicalEditPart part, C control, final Rectangle bounds,
			final Point location, String locationX, final IFormAttachmentInfo<C> leftAttachment,
			final List<C> attachables, boolean createControl) {
		// prepare values from preferences
		int layoutMarginLeft = FormUtils.getLayoutMarginLeft(layout);
		int hModelMargin = layout.getPreferences().getHorizontalContainerGap();
		int hMargin = hModelMargin + layoutMarginLeft;
		int hwMargin = layout.getPreferences().getHorizontalComponentGap();
		int hpMargin = layout.getPreferences().getHorizontalPercentsGap();
		int sens = layout.getPreferences().getSnapSensitivity();
		// prepare feedback and parent bounds
		int parentWidth = layout.getContainerSize().width;
		int x = bounds.x;
		boolean controlFound = false;
		Command cmd = null;
		if (useSnap()) {
			if (hwMargin > -1) {
				C child = null;
				int cmdSide = 0;
				int cmdOffset = 0;
				for (Iterator<C> I = sortControlsByAxisRange(attachables, false, location.y).iterator(); I.hasNext()
						&& !controlFound;) {
					child = I.next();
					Rectangle childBounds = FormSelectionEditPolicyClassic.getControlModelBounds(child);
					int top = Math.min(bounds.y, childBounds.y);
					int bottom = Math.max(bounds.bottom(), childBounds.bottom());
					if (FormUtils.between(x - (childBounds.right() + hwMargin), 0, sens)) {
						controlFound = true;
						bounds.x = childBounds.right() + hwMargin;
						addHLine(part, child, childBounds.right(), controlColor);
						addHLine(part, top, bottom, childBounds.right() + hwMargin, offsetColor);
						locationX = getChildSideString(child, "right") + "+" + hwMargin;
						cmdSide = IPositionConstants.RIGHT;
						cmdOffset = hwMargin;
					} else if (FormUtils.between(x - childBounds.right(), -sens, hwMargin)) {
						controlFound = true;
						bounds.x = childBounds.right();
						addHLine(part, top, bottom, childBounds.right(), offsetColor);
						addHLine(part, child, childBounds.right(), controlColor);
						locationX = getChildSideString(child, "right");
						cmdSide = IPositionConstants.RIGHT;
						cmdOffset = 0;
					} else if (FormUtils.between(x - childBounds.x, -sens, sens)) {
						controlFound = true;
						bounds.x = childBounds.x;
						addHLine(part, top, bottom, childBounds.x, offsetColor);
						addHLine(part, child, childBounds.x, controlColor);
						locationX = getChildSideString(child, "x");
						cmdSide = IPositionConstants.LEFT;
						cmdOffset = 0;
					}
				}
				if (controlFound) {
					cmd = getBindToControlCommand(createControl, control, IPositionConstants.LEFT, child, cmdSide,
							cmdOffset);
				}
			}
			if (!controlFound) {
				boolean snappedToPercent = false;
				List<Integer> formLayoutHPercents = layout.getPreferences().getHorizontalPercents();
				for (Integer percent : formLayoutHPercents) {
					int percentX = (int) ((double) percent.intValue() / 100 * parentWidth) + layoutMarginLeft;
					int offset = 0;
					if (FormUtils.between(x - (percentX + hpMargin), 0, sens) && hpMargin > -1) {
						bounds.x = percentX + hpMargin;
						addHLine(part, percentX, baseColor);
						addHLine(part, percentX + hpMargin, offsetColor);
						locationX = percent.toString() + "%+" + hpMargin;
						offset = hpMargin;
						snappedToPercent = true;
					} else if (FormUtils.between(x - percentX, -sens, hwMargin)) {
						bounds.x = percentX;
						addHLine(part, percentX, offsetColor);
						locationX = percent.toString() + "%";
						snappedToPercent = true;
					}
					if (snappedToPercent) {
						if (createControl) {
							cmd = new CreateToPercentOffsetCommand(control, IPositionConstants.LEFT, percent.intValue(),
									offset);
						} else {
							cmd = new MoveToPercentOffsetCommand(control, IPositionConstants.LEFT, percent.intValue(),
									offset);
						}
						break;
					}
				}
				if (!snappedToPercent) {
					if (hMargin > -1 && x < hMargin) {
						bounds.x = hMargin;
						addHLine(part, hMargin, offsetColor);
						if (createControl) {
							cmd = new CreateToMarginCommand(control, IPositionConstants.LEFT, IPositionConstants.LEFT,
									hModelMargin);
						} else {
							cmd = new MoveToMarginCommand(control, IPositionConstants.LEFT, IPositionConstants.LEFT,
									hModelMargin);
						}
					} else {
						bounds.x = FormUtils.snapGrid(x, sens);
						if (createControl) {
							cmd = new CreateToOffsetCommand(control, IPositionConstants.LEFT, parentWidth,
									FormUtils.snapGrid(x - layoutMarginLeft, sens));
						} else {
							cmd = new MoveToOffsetCommand(control, IPositionConstants.LEFT, parentWidth,
									FormUtils.snapGrid(x - layoutMarginLeft, sens));
						}
					}
					locationX = getLocationString(leftAttachment, bounds.x - layoutMarginLeft, parentWidth);
				}
			}
		} else {
			locationX = getLocationString(leftAttachment, bounds.x - layoutMarginLeft, parentWidth);
			if (createControl) {
				cmd = new CreateToOffsetCommand(control, IPositionConstants.LEFT, parentWidth, x - layoutMarginLeft);
			} else {
				cmd = new MoveToOffsetCommand(control, IPositionConstants.LEFT, parentWidth, x - layoutMarginLeft);
			}
		}
		addHLine(part, bounds.x, layout.getComposite().getClientArea().height, IColorConstants.red);
		if (createControl) {
			createCommand.add(cmd);
		} else {
			moveChildredCommand.add(cmd);
		}
		return locationX;
	}

	private String getLocationString(IFormAttachmentInfo<C> attachment, int coord, int parentDimension) {
		String locationX;
		if (attachment == null || attachment.getNumerator() != 100) {
			locationX = String.valueOf(coord) + " <--";
		} else {
			locationX = "--> " + String.valueOf(parentDimension - coord);
		}
		return locationX;
	}

	private String getChildSideString(C child, String side) {
		String variableName = FormUtils.getVariableName(child);
		if (variableName != null) {
			return variableName;
		}
		Rectangle bounds = FormSelectionEditPolicyClassic.getControlModelBounds(child);
		if (side.equals("x")) {
			return String.valueOf(bounds.x);
		}
		if (side.equals("right")) {
			return String.valueOf(bounds.right());
		}
		if (side.equals("y")) {
			return String.valueOf(bounds.y);
		}
		if (side.equals("bottom")) {
			return String.valueOf(bounds.bottom());
		}
		return "???";
	}

	private Command getBindToControlCommand(boolean createControl, C control, int controlSide, C child, int cmdSide,
			int cmdOffset) {
		if (createControl) {
			return new CreateToControlCommand(control, controlSide, child, cmdSide, cmdOffset);
		} else {
			return new MoveToControlCommand(control, controlSide, child, cmdSide, cmdOffset);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Key modifiers
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean useSnap() {
		if (isKeyboardMoving()) {
			return false;
		}
		return !DesignerPlugin.isShiftPressed();
	}

	private boolean isFreezeVerticalAxis(Request request) {
		return request.isControlKeyPressed() && frozenYValue != 0;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback utilities
	//
	////////////////////////////////////////////////////////////////////////////
	private Polyline addHFeedbackLine(int x, int y1, int y2, Color color) {
		return addFeedbackLine(x, y1, x, y2, color);
	}

	private Polyline addVFeedbackLine(int y, int x1, int x2, Color color) {
		return addFeedbackLine(x1, y, x2, y, color);
	}

	private Polyline addFeedbackLine(int x1, int y1, int x2, int y2, Color color) {
		Polyline line = new Polyline();
		Point p1 = new Point(x1, y1);
		Point p2 = new Point(x2, y2);
		translateModelToFeedback(p1);
		translateModelToFeedback(p2);
		line.addPoint(p1);
		line.addPoint(p2);
		line.setForeground(color);
		line.setLineStyle(SWT.LINE_DOT);
		// add the line to feedbacks
		addFeedback(line);
		return line;
	}

	private void addFeedbackLine(EditPart part, Polyline line) {
		if (part == null) {
			part = getHost();
		}
		List<Figure> partFeedbacks = feedbacks.get(part);
		if (partFeedbacks == null) {
			partFeedbacks = Lists.newLinkedList();
			feedbacks.put(part, partFeedbacks);
		}
		partFeedbacks.add(line);
	}

	private void addHLine(EditPart part, int x, int parentHeight, Color color) {
		Polyline line = addHFeedbackLine(x, parentHeight - 5, parentHeight + 5, color);
		line.setLineWidth(3);
		line.setLineStyle(SWT.LINE_SOLID);
		addFeedbackLine(part, line);
	}

	private void addHLine(EditPart part, C component, int x, Color color) {
		Rectangle bounds = FormSelectionEditPolicyClassic.getControlModelBounds(component);
		Polyline line = addHFeedbackLine(x, bounds.y, bounds.bottom(), color);
		line.setLineWidth(2);
		line.setLineStyle(SWT.LINE_SOLID);
		addFeedbackLine(part, line);
	}

	private void addHLine(EditPart part, int x, Color color) {
		ICompositeInfo composite = layout.getComposite();
		int parentHeight = composite.getClientArea().height;
		Polyline line = addHFeedbackLine(x, 0, parentHeight, color);
		addFeedbackLine(part, line);
	}

	private void addHLine(EditPart part, int top, int bottom, int x, Color color) {
		Polyline line = addHFeedbackLine(x, top - EXTENSION, bottom + EXTENSION, color);
		addFeedbackLine(part, line);
	}

	private void addVLine(EditPart part, int y, int parentWidth, Color color) {
		Polyline line = addVFeedbackLine(y, parentWidth - 5, parentWidth + 5, color);
		line.setLineWidth(3);
		line.setLineStyle(SWT.LINE_SOLID);
		addFeedbackLine(part, line);
	}

	private void addVLine(EditPart part, C component, int y, Color color) {
		Rectangle bounds = FormSelectionEditPolicyClassic.getControlModelBounds(component);
		Polyline line = addVFeedbackLine(y, bounds.x, bounds.right(), color);
		line.setLineWidth(2);
		line.setLineStyle(SWT.LINE_SOLID);
		addFeedbackLine(part, line);
	}

	private void addVLine(EditPart part, int y, Color color) {
		ICompositeInfo composite = layout.getComposite();
		int parentWidth = composite.getClientArea().width;
		Polyline line = addVFeedbackLine(y, 0, parentWidth, color);
		addFeedbackLine(part, line);
	}

	private void addVLine(EditPart part, int left, int right, int y, Color color) {
		Polyline line = addVFeedbackLine(y, left - EXTENSION, right + EXTENSION, color);
		addFeedbackLine(part, line);
	}

	private void removeFeedbacks() {
		for (EditPart part : feedbacks.keySet()) {
			removeFeedbacks(part);
		}
		feedbacks.clear();
		hideLocationHints();
	}

	private void removeFeedbacks(EditPart part) {
		List<Figure> partFeedbacks = feedbacks.get(part);
		if (partFeedbacks == null) {
			return;
		}
		for (Figure figure : partFeedbacks) {
			removeFeedback(figure);
		}
		partFeedbacks.clear();
	}

	@SuppressWarnings("unchecked")
	private void showSelectionFeedbacks() {
		for (EditPart child : getHost().getChildren()) {
			if (layout.getControls().contains(child.getModel()) && child.getSelected() != EditPart.SELECTED_NONE) {
				FormSelectionEditPolicyClassic<C> editPolicy = (FormSelectionEditPolicyClassic<C>) child
						.getEditPolicy(EditPolicy.SELECTION_ROLE);
				editPolicy.showSelection();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Coordinates utilities
	//
	////////////////////////////////////////////////////////////////////////////
	protected void translateModelToFeedback(Translatable t) {
		PolicyUtils.translateModelToFeedback(this, t);
	}

	protected void translateAbsoluteToModel(Translatable t) {
		PolicyUtils.translateAbsoluteToModel(this, t);
	}

	private void setHBounds(Rectangle r, int right, boolean createControl) {
		if (createControl) {
			r.width = right - r.x;
		} else {
			r.x = right - r.width;
		}
	}

	private void setVBounds(Rectangle r, int bottom, boolean createControl) {
		if (createControl) {
			r.height = bottom - r.y;
		} else {
			r.y = bottom - r.height;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	static <C extends IControlInfo> List<C> sortControlsByAxisRange(List<C> components, final boolean isX,
			final int value) {
		return FormSelectionEditPolicyClassic.sortControlsByAxisRange(components, isX, value);
	}

	private List<C> getAttachableControls(C control, boolean horizontal, final boolean includeSelected)
			throws Exception {
		List<C> attachableControls = control != null ? FormUtils.getAttachableControls(layout, control, horizontal)
				: FormUtils.getAttachableControls(layout);
		CollectionUtils.filter(attachableControls, new Predicate() {
			@Override
			public boolean evaluate(Object input) {
				try {
					EditPart part = getHost().getViewer().getEditPartByModel(input);
					if (!includeSelected && part.getSelected() != EditPart.SELECTED_NONE) {
						return false;
					}
				} catch (Throwable e) {
					return false;
				}
				return true;
			}
		});
		return attachableControls;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location and size hints
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<EditPart, TextFeedback> xTextFeedbacks = Maps.newHashMap();
	private Map<EditPart, TextFeedback> yTextFeedbacks = Maps.newHashMap();
	private TextFeedback createControlHintFeedbackX;
	private TextFeedback createControlHintFeedbackY;

	/**
	 * @return special layer above feedback layer for to add text feedbacks
	 */
	private Layer getTextFeedbackLayer() {
		return getLayer(IEditPartViewer.FEEDBACK_LAYER_ABV_1);
	}

	private void hideLocationHints() {
		for (TextFeedback feedback : xTextFeedbacks.values()) {
			feedback.remove();
		}
		for (TextFeedback feedback : yTextFeedbacks.values()) {
			feedback.remove();
		}
		xTextFeedbacks = Maps.newHashMap(); // use fast GC :)
		yTextFeedbacks = Maps.newHashMap(); // use fast GC :)
		if (createControlHintFeedbackX != null) {
			createControlHintFeedbackX.remove();
			createControlHintFeedbackX = null;
		}
		if (createControlHintFeedbackY != null) {
			createControlHintFeedbackY.remove();
			createControlHintFeedbackY = null;
		}
	}

	public TextFeedback createTextFeedback(boolean isHorizontal) {
		TextFeedback textFeedback = new TextFeedback(getTextFeedbackLayer(), isHorizontal);
		return textFeedback;
	}

	protected TextFeedback getTextFeedback(Map<EditPart, TextFeedback> map, EditPart key, boolean isHorizontal) {
		TextFeedback textFeedback = map.get(key);
		if (textFeedback == null) {
			textFeedback = createTextFeedback(isHorizontal);
			textFeedback.add();
		}
		map.put(key, textFeedback);
		return textFeedback;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move commands
	//
	////////////////////////////////////////////////////////////////////////////
	private final class MoveToPercentOffsetCommand extends EditCommand {
		private final int offset;
		private final int percent;
		private final C child;
		private final int side;

		private MoveToPercentOffsetCommand(C child, int side, int percent, int offset) {
			super(child);
			this.child = child;
			this.side = side;
			this.percent = percent;
			this.offset = offset;
		}

		@Override
		public void executeEdit() throws Exception {
			layoutImpl.moveToPercentOffset(child, side, percent, offset);
		}
	}

	private final class MoveToOffsetCommand extends EditCommand {
		private final int offset;
		private final int parentSize;
		private final C child;
		private final int side;

		private MoveToOffsetCommand(C child, int side, int parentSize, int offset) {
			super(child);
			this.child = child;
			this.side = side;
			this.parentSize = parentSize;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.moveToOffset(child, side, parentSize, offset);
		}
	}

	private final class MoveToMarginCommand extends EditCommand {
		private final int marginValue;
		private final int direction;
		private final int side;
		private final C child;

		private MoveToMarginCommand(C child, int side, int direction, int marginValue) {
			super(child);
			this.child = child;
			this.side = side;
			this.direction = direction;
			this.marginValue = marginValue;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.moveToMargin(child, side, direction, marginValue);
		}
	}

	private final class MoveToControlCommand extends EditCommand {
		private final C control;
		private final int offset;
		private final int position;
		private final C child;
		private final int side;

		public MoveToControlCommand(C child, int side, C attachToControl, int position, int offset) {
			super(child);
			this.child = child;
			this.side = side;
			this.control = attachToControl;
			this.position = position;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.moveToControl(child, side, control, position, offset);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create commands
	//
	////////////////////////////////////////////////////////////////////////////
	private final class CreateToControlCommand extends EditCommand {
		private final C child;
		private final int side;
		private final C control;
		private final int position;
		private final int offset;

		public CreateToControlCommand(C child, int side, C control, int position, int offset) {
			super(child);
			this.child = child;
			this.side = side;
			this.control = control;
			this.position = position;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.createToControl(child, side, control, position, offset);
		}
	}

	private final class CreateToPercentOffsetCommand extends EditCommand {
		private final C child;
		private final int side;
		private final int percent;
		private final int offset;

		public CreateToPercentOffsetCommand(C child, int side, int percent, int offset) {
			super(child);
			this.child = child;
			this.side = side;
			this.percent = percent;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.createToPercentOffset(child, side, percent, offset);
		}
	}

	private final class CreateToOffsetCommand extends EditCommand {
		private final C child;
		private final int side;
		private final int parentSize;
		private final int offset;

		public CreateToOffsetCommand(C child, int side, int parentSize, int offset) {
			super(child);
			this.child = child;
			this.side = side;
			this.parentSize = parentSize;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.createToOffset(child, side, parentSize, offset);
		}
	}

	private final class CreateToMarginCommand extends EditCommand {
		private final C child;
		private final int side;
		private final int direction;
		private final int marginValue;

		private CreateToMarginCommand(C child, int side, int direction, int marginValue) {
			super(child);
			this.child = child;
			this.side = side;
			this.direction = direction;
			this.marginValue = marginValue;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.createToMargin(child, side, direction, marginValue);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHeadersProvider
	//
	////////////////////////////////////////////////////////////////////////////
	private FormHeaderLayoutEditPolicy<C> headersPolicyHorizontal;
	private FormHeaderLayoutEditPolicy<C> headersPolicyVertical;

	@Override
	public EditPart createHeaderEditPart(boolean isHorizontal, Object model) {
		return new FormHeaderEditPart<C>(layout, model, isHorizontal, getHostFigure());
	}

	@Override
	public void buildContextMenu(IMenuManager manager, boolean isHorizontal) {
		FormHeaderLayoutEditPolicy<C> headersPolicy = isHorizontal ? headersPolicyHorizontal : headersPolicyVertical;
		headersPolicy.buildContextMenu(manager);
	}

	@Override
	public LayoutEditPolicy getContainerLayoutPolicy(boolean isHorizontal) {
		FormHeaderLayoutEditPolicy<C> headersPolicy = new FormHeaderLayoutEditPolicy<C>(layout, this, isHorizontal);
		if (isHorizontal) {
			headersPolicyHorizontal = headersPolicy;
		} else {
			headersPolicyVertical = headersPolicy;
		}
		return headersPolicy;
	}

	@Override
	public List<?> getHeaders(boolean isHorizontal) {
		List<Integer> values = isHorizontal ? layout.getPreferences().getHorizontalPercents()
				: layout.getPreferences().getVerticalPercents();
		List<FormLayoutPreferences.PercentsInfo> results = Lists.newArrayList();
		for (Integer percent : values) {
			results.add(new FormLayoutPreferences.PercentsInfo(percent));
		}
		return results;
	}

	@Override
	public void handleDoubleClick(boolean isHorizontal) {
	}
}
