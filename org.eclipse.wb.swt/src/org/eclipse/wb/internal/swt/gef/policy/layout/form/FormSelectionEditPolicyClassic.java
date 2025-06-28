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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.LineEndFigure;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.layout.form.IFormAttachmentInfo;
import org.eclipse.wb.internal.swt.model.layout.form.IFormDataInfo;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Selection Policy for 'Classic' version of the FormLayout support.
 *
 * @author mitin_aa
 */
public final class FormSelectionEditPolicyClassic<C extends IControlInfo>
extends
SelectionEditPolicy {
	private static final int EXTENSION = 8;
	private final Color baseColor = ColorConstants.blue;
	private final Color offsetColor = ColorConstants.lightBlue;
	private final IFormLayoutInfo<C> layoutInfo;
	private final FormLayoutInfoImplClassic<C> layoutImpl;
	private final AnchorFiguresClassic<C> anchorFigures;
	private final ObjectInfo layoutModel;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormSelectionEditPolicyClassic(IFormLayoutInfo<C> info) {
		this.layoutInfo = info;
		this.layoutModel = info.getUnderlyingModel();
		this.layoutImpl = (FormLayoutInfoImplClassic<C>) layoutInfo.getImpl();
		this.anchorFigures = new AnchorFiguresClassic<>(this, layoutImpl);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Activate/deactivate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void activate() {
		super.activate();
		installQuadrantHandler();
	}

	@Override
	public void deactivate() {
		super.deactivate();
		uninstallQuadrantHandler();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize handles
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<String, Figure> resizeFeedbacks = new HashMap<>();

	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		MoveHandle moveHandle = new MoveHandle(getHost());
		moveHandle.setBorder(new LineBorder(ColorConstants.lightBlue));
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

	private Handle createResizeHandle(int direction) {
		GraphicalEditPart owner = getHost();
		ResizeHandle handle = new ResizeHandle(owner, direction) {
			@Override
			protected Color getBorderColor() {
				return isPrimary() ? ColorConstants.white : ColorConstants.lightBlue;
			}

			@Override
			protected Color getFillColor() {
				return isPrimary() ? ColorConstants.lightBlue : ColorConstants.white;
			}
		};
		handle.setDragTracker(new ResizeTracker(direction,
				AbsoluteBasedSelectionEditPolicy.REQ_RESIZE));
		return handle;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source feedback and commands
	//
	////////////////////////////////////////////////////////////////////////////
	private Figure sourceFeedbackFigure;

	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request)
				|| request.getType() == AbsoluteBasedSelectionEditPolicy.REQ_RESIZE;
	}

	@Override
	public void showSourceFeedback(final Request request) {
		if (AbsoluteBasedSelectionEditPolicy.REQ_RESIZE.equals(request.getType())) {
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					showResizeFeedback((ChangeBoundsRequest) request);
				}
			});
		}
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		super.eraseSourceFeedback(request);
		if (sourceFeedbackFigure != null) {
			removeFeedback(sourceFeedbackFigure);
			sourceFeedbackFigure = null;
		}
		removeResizeFeedbacks();
		hideSizeHints();
	}

	private void showResizeFeedback(ChangeBoundsRequest request) throws Exception {
		FormLayoutEditPolicyClassic<C> layout = getLayoutEditPolicy();
		if (sourceFeedbackFigure == null) {
			sourceFeedbackFigure = new Figure();
			sourceFeedbackFigure.setBorder(new LineBorder(AbsolutePolicyUtils.COLOR_OUTLINE));
			addFeedback(sourceFeedbackFigure);
		}
		int layoutMarginLeft = FormUtils.getLayoutMarginLeft(layoutInfo);
		int layoutMarginTop = FormUtils.getLayoutMarginTop(layoutInfo);
		// prepare values from preferences
		int hMargin = layoutInfo.getPreferences().getHorizontalContainerGap();
		int phMargin = layoutInfo.getPreferences().getHorizontalPercentsGap();
		int hwMargin = layoutInfo.getPreferences().getHorizontalComponentGap();
		int sens = layoutInfo.getPreferences().getSnapSensitivity();
		int vMargin = layoutInfo.getPreferences().getVerticalContainerGap();
		int pvMargin = layoutInfo.getPreferences().getVerticalPercentsGap();
		int vwMargin = layoutInfo.getPreferences().getVerticalComponentGap();
		// hide any old resize feedbacks (it is more easy to add each time new feedback than track old ones and change their positions)
		removeResizeFeedbacks();
		@SuppressWarnings("unchecked")
		C control = (C) getHost().getModel();
		Rectangle bounds = getControlModelBounds(control);
		bounds.performTranslate(request.getMoveDelta());
		bounds.resize(request.getSizeDelta());
		Point requestLocation = request.getLocation();
		Point location =
				requestLocation == null
				? getControlModelBounds(control).getLocation()
						: requestLocation.getCopy();
		// find attachable controls
		List<C> hAttachables = FormUtils.getAttachableControls(layoutInfo, control, true);
		List<C> vAttachables = FormUtils.getAttachableControls(layoutInfo, control, false);
		int direction = request.getResizeDirection();
		// get parent info
		int parentWidth = layoutInfo.getContainerSize().width;
		int parentHeight = layoutInfo.getContainerSize().height;
		Rectangle clientArea = layoutInfo.getComposite().getClientArea();
		int parentClientAreaWidth = clientArea.width;
		int parentClientAreaHeight = clientArea.height;
		// size hints feedback
		String xText = "", yText = "";
		// West
		if (hasDirection(direction, PositionConstants.WEST)) {
			removeSelectionFeedbacks("W");
			int x = bounds.x;
			boolean componentFound = false;
			if (useSnap()) {
				if (hwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(hAttachables, false, location.y).iterator(); I.hasNext()
							&& !componentFound;) {
						C child = I.next();
						Rectangle childBounds = getControlModelBounds(child);
						if (FormUtils.between(x - (childBounds.right() + hwMargin), 0, sens)) {
							componentFound = true;
							setX(bounds, childBounds.right() + hwMargin);
							addVerticalResizeLine("12", child, childBounds.right(), ColorConstants.green);
							addVerticalResizeLine("2", childBounds.right() + hwMargin, offsetColor);
							xText =
									(FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.right())
											: FormUtils.getVariableName(child)) + "+" + hwMargin;
						} else if (FormUtils.between(x - childBounds.right(), -sens, hwMargin)) {
							componentFound = true;
							setX(bounds, childBounds.right());
							addVerticalResizeLine("11", childBounds.right(), offsetColor);
							addVerticalResizeLine("12", child, childBounds.right(), ColorConstants.green);
							xText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.right())
											: FormUtils.getVariableName(child);
						} else if (FormUtils.between(x - childBounds.x, -sens, sens)) {
							componentFound = true;
							setX(bounds, childBounds.x);
							addVerticalResizeLine("11", childBounds.x, offsetColor);
							addVerticalResizeLine("12", child, childBounds.x, ColorConstants.green);
							xText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.right())
											: FormUtils.getVariableName(child);
						}
					}
				}
				if (!componentFound) {
					boolean snappedToPercent = false;
					List<Integer> formLayoutHPercents = layoutInfo.getPreferences().getHorizontalPercents();
					for (Integer percent : formLayoutHPercents) {
						int newX = (int) ((double) percent.intValue() / 100 * parentWidth) + layoutMarginLeft;
						if (FormUtils.between(x - (newX + hwMargin), 0, sens) && phMargin > -1) {
							setX(bounds, newX + phMargin);
							addVerticalResizeLine("3", newX, baseColor);
							addVerticalResizeLine("4", newX + phMargin, offsetColor);
							xText = percent.toString() + "%+" + phMargin;
							snappedToPercent = true;
						} else if (FormUtils.between(x - newX, -sens, hwMargin)) {
							setX(bounds, newX);
							addVerticalResizeLine("3", newX, offsetColor);
							xText = percent.toString() + "%";
							snappedToPercent = true;
						}
						if (snappedToPercent) {
							break;
						}
					}
					if (!snappedToPercent) {
						int leftPoint = hMargin + layoutMarginLeft;
						if (hMargin > -1 && x < leftPoint) {
							setX(bounds, leftPoint);
							addVerticalResizeLine("2", leftPoint, offsetColor);
						} else {
							setX(bounds, FormUtils.snapGrid(x, sens));
						}
						xText = String.valueOf(bounds.width);
					}
				}
			} else {
				xText = String.valueOf(bounds.width);
			}
			addVerticalResizeLine("5", bounds.x, parentClientAreaHeight, ColorConstants.red);
		}
		// East
		if (hasDirection(direction, PositionConstants.EAST)) {
			removeSelectionFeedbacks("E");
			int x = bounds.right();
			boolean componentFound = false;
			if (useSnap()) {
				if (hwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(hAttachables, false, location.y).iterator(); I.hasNext()
							&& !componentFound;) {
						C child = I.next();
						Rectangle childBounds = getControlModelBounds(child);
						if (FormUtils.between(childBounds.x - hwMargin - x, 0, sens)) {
							componentFound = true;
							bounds.width = childBounds.x - hwMargin - bounds.x;
							addVerticalResizeLine("12", child, childBounds.x, ColorConstants.green);
							addVerticalResizeLine("2", childBounds.x - hwMargin, offsetColor);
							xText =
									(FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.x)
											: FormUtils.getVariableName(child)) + "-" + hwMargin;
						} else if (FormUtils.between(childBounds.x - x, -sens, hwMargin)) {
							componentFound = true;
							bounds.width = childBounds.x - bounds.x;
							addVerticalResizeLine("11", childBounds.x, offsetColor);
							addVerticalResizeLine("12", child, childBounds.x, ColorConstants.green);
							xText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.x)
											: FormUtils.getVariableName(child);
						} else if (FormUtils.between(childBounds.right() - x, -sens, sens)) {
							componentFound = true;
							bounds.width = childBounds.right() - bounds.x;
							addVerticalResizeLine("11", childBounds.right(), offsetColor);
							addVerticalResizeLine("12", child, childBounds.right(), ColorConstants.green);
							xText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.x)
											: FormUtils.getVariableName(child);
						}
					}
				}
				if (!componentFound) {
					boolean snappedToPercent = false;
					List<Integer> formLayoutHPercents = layoutInfo.getPreferences().getHorizontalPercents();
					for (Integer percent : formLayoutHPercents) {
						int newX = (int) ((double) percent.intValue() / 100 * parentWidth) + layoutMarginLeft;
						if (FormUtils.between(newX - hwMargin - x, 0, sens) && phMargin > -1) {
							bounds.width = newX - bounds.x - phMargin;
							addVerticalResizeLine("3", newX, baseColor);
							addVerticalResizeLine("4", newX - phMargin, offsetColor);
							xText = percent.toString() + "%-" + phMargin;
							snappedToPercent = true;
						} else if (FormUtils.between(newX - x, -sens, hwMargin)) {
							bounds.width = newX - bounds.x;
							addVerticalResizeLine("3", newX, offsetColor);
							xText = percent.toString() + "%";
							snappedToPercent = true;
						}
						if (snappedToPercent) {
							break;
						}
					}
					if (!snappedToPercent) {
						int rightPoint = parentWidth - hMargin + layoutMarginLeft;
						if (hMargin > -1 && x > rightPoint) {
							bounds.width = rightPoint - bounds.x;
							addVerticalResizeLine("2", rightPoint, offsetColor);
						} else {
							bounds.width = FormUtils.snapGrid(x, sens) - bounds.x;
						}
						xText = String.valueOf(bounds.width);
					}
				}
			} else {
				xText = String.valueOf(bounds.width);
			}
			addVerticalResizeLine("5", bounds.right(), parentClientAreaHeight, ColorConstants.red);
		}
		// North
		if (hasDirection(direction, PositionConstants.NORTH)) {
			removeSelectionFeedbacks("N");
			int y = bounds.y;
			boolean componentFound = false;
			if (useSnap()) {
				if (vwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(vAttachables, true, location.x).iterator(); I.hasNext()
							&& !componentFound;) {
						C child = I.next();
						Rectangle childBounds = getControlModelBounds(child);
						if (FormUtils.between(y - (childBounds.bottom() + vwMargin), 0, sens)) {
							componentFound = true;
							setY(bounds, childBounds.bottom() + vwMargin);
							addHorizontalResizeLine("12", child, childBounds.bottom(), ColorConstants.green);
							addHorizontalResizeLine("2", childBounds.bottom() + vwMargin, offsetColor);
							yText =
									(FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.bottom())
											: FormUtils.getVariableName(child)) + "+" + vwMargin;
						} else if (FormUtils.between(y - childBounds.bottom(), -sens, vwMargin)) {
							componentFound = true;
							setY(bounds, childBounds.bottom());
							addHorizontalResizeLine("11", childBounds.bottom(), offsetColor);
							addHorizontalResizeLine("12", child, childBounds.bottom(), ColorConstants.green);
							yText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.bottom())
											: FormUtils.getVariableName(child);
						} else if (FormUtils.between(y - childBounds.y, -sens, sens)) {
							componentFound = true;
							setY(bounds, childBounds.y);
							addHorizontalResizeLine("11", childBounds.y, offsetColor);
							addHorizontalResizeLine("12", child, childBounds.y, ColorConstants.green);
							yText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.bottom())
											: FormUtils.getVariableName(child);
						}
					}
				}
				if (!componentFound) {
					boolean snappedToPercent = false;
					List<Integer> formLayoutVPercents = layoutInfo.getPreferences().getVerticalPercents();
					for (Integer percent : formLayoutVPercents) {
						int newY = (int) ((double) percent.intValue() / 100 * parentHeight) + layoutMarginTop;
						if (FormUtils.between(y - (newY + vwMargin), 0, sens) && pvMargin > -1) {
							setY(bounds, newY + pvMargin);
							addHorizontalResizeLine("3", newY, baseColor);
							addHorizontalResizeLine("4", newY + pvMargin, offsetColor);
							yText = percent.toString() + "%+" + pvMargin;
							snappedToPercent = true;
						} else if (FormUtils.between(y - newY, -sens, vwMargin)) {
							setY(bounds, newY);
							addHorizontalResizeLine("3", newY, offsetColor);
							yText = percent.toString() + "%";
							snappedToPercent = true;
						}
						if (snappedToPercent) {
							break;
						}
					}
					if (!snappedToPercent) {
						int topPoint = vMargin + layoutMarginTop;
						if (vMargin > -1 && y < topPoint) {
							setY(bounds, topPoint);
							addHorizontalResizeLine("2", topPoint, offsetColor);
						} else {
							setY(bounds, FormUtils.snapGrid(y, sens));
						}
						yText = String.valueOf(bounds.height);
					}
				}
			} else {
				yText = String.valueOf(bounds.height);
			}
			addHorizontalResizeLine("5", bounds.y, parentClientAreaWidth, ColorConstants.red);
		}
		// South
		if (hasDirection(direction, PositionConstants.SOUTH)) {
			removeSelectionFeedbacks("S");
			int y = bounds.bottom();
			boolean componentFound = false;
			if (useSnap()) {
				if (vwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(vAttachables, true, location.x).iterator(); I.hasNext()
							&& !componentFound;) {
						C child = I.next();
						Rectangle childBounds = getControlModelBounds(child);
						if (FormUtils.between(childBounds.y - vwMargin - y, 0, sens)) {
							componentFound = true;
							bounds.height = childBounds.y - vwMargin - bounds.y;
							addHorizontalResizeLine("12", child, childBounds.y, ColorConstants.green);
							addHorizontalResizeLine("2", childBounds.y - vwMargin, offsetColor);
							yText =
									(FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.y)
											: FormUtils.getVariableName(child)) + "-" + vwMargin;
						} else if (FormUtils.between(childBounds.y - y, -sens, vwMargin)) {
							componentFound = true;
							bounds.height = childBounds.y - bounds.y;
							addHorizontalResizeLine("11", childBounds.y, offsetColor);
							addHorizontalResizeLine("12", child, childBounds.y, ColorConstants.green);
							yText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.y)
											: FormUtils.getVariableName(child);
						} else if (FormUtils.between(childBounds.bottom() - y, -sens, sens)) {
							componentFound = true;
							bounds.height = childBounds.bottom() - bounds.y;
							addHorizontalResizeLine("11", childBounds.bottom(), offsetColor);
							addHorizontalResizeLine("12", child, childBounds.bottom(), ColorConstants.green);
							yText =
									FormUtils.getVariableName(child) == null
									? String.valueOf(childBounds.y)
											: FormUtils.getVariableName(child);
						}
					}
				}
				if (!componentFound) {
					boolean snappedToPercent = false;
					List<Integer> formLayoutVPercents = layoutInfo.getPreferences().getVerticalPercents();
					for (Integer percent : formLayoutVPercents) {
						int newY = (int) ((double) percent.intValue() / 100 * parentHeight) + layoutMarginTop;
						if (FormUtils.between(newY - vwMargin - y, 0, sens) && pvMargin > -1) {
							bounds.height = newY - pvMargin - bounds.y;
							addHorizontalResizeLine("3", newY, baseColor);
							addHorizontalResizeLine("4", newY - pvMargin, offsetColor);
							yText = percent.toString() + "%-" + pvMargin;
							snappedToPercent = true;
						} else if (FormUtils.between(newY - y, -sens, vwMargin)) {
							bounds.height = newY - bounds.y;
							addHorizontalResizeLine("3", newY, offsetColor);
							yText = percent.toString() + "%";
							snappedToPercent = true;
						}
						if (snappedToPercent) {
							break;
						}
					}
					if (!snappedToPercent) {
						int bottomPoint = parentHeight - vMargin + layoutMarginTop;
						if (vMargin > -1 && y > bottomPoint) {
							bounds.height = bottomPoint - bounds.y;
							addHorizontalResizeLine("2", bottomPoint, offsetColor);
						} else {
							bounds.height = FormUtils.snapGrid(y, sens) - bounds.y;
						}
						yText = String.valueOf(bounds.height);
					}
				}
			} else {
				yText = String.valueOf(bounds.height);
			}
			addHorizontalResizeLine("5", bounds.bottom(), parentClientAreaWidth, ColorConstants.red);
		}
		{
			// move the source feedback figure
			Rectangle feedbackBounds = bounds.getCopy();
			translateModelToFeedback(feedbackBounds);
			sourceFeedbackFigure.setBounds(feedbackBounds);
			if (xText.length() != 0) {
				if (xTextFeedback == null) {
					xTextFeedback = layout.createTextFeedback(true);
					xTextFeedback.add();
				}
				xTextFeedback.setText(xText);
				Dimension textSize = xTextFeedback.getSize();
				Rectangle textBounds = bounds.getCopy();
				textBounds.y = parentClientAreaHeight + 1;
				int x =
						hasDirection(direction, PositionConstants.EAST) ? textBounds.right() : textBounds.x;
				x -= textSize.width / 2;
				if (x < 0) {
					x = 0;
				} else if (x + textSize.width > parentClientAreaWidth) {
					x = parentClientAreaWidth - textSize.width;
				}
				textBounds.x = x;
				translateModelToFeedback(textBounds);
				xTextFeedback.setLocation(textBounds.getLocation());
			}
			if (yText.length() != 0) {
				if (yTextFeedback == null) {
					yTextFeedback = layout.createTextFeedback(false);
					yTextFeedback.add();
				}
				yTextFeedback.setText(yText);
				Dimension textSize = yTextFeedback.getSize();
				Rectangle textBounds = bounds.getCopy();
				textBounds.x = parentClientAreaWidth + 1;
				int y =
						hasDirection(direction, PositionConstants.SOUTH) ? textBounds.bottom() : textBounds.y;
				y -= textSize.height / 2;
				if (y < 0) {
					y = 0;
				} else if (y + textSize.height > parentClientAreaHeight) {
					y = parentClientAreaHeight - textSize.height;
				}
				textBounds.y = y;
				translateModelToFeedback(textBounds);
				yTextFeedback.setLocation(textBounds.getLocation());
			}
		}
	}

	@Override
	public Command getCommand(final Request request) {
		if (AbsoluteBasedSelectionEditPolicy.REQ_RESIZE.equals(request.getType())) {
			return ExecutionUtils.runObjectLog(() -> getResizeCommand((ChangeBoundsRequest) request), null);
		}
		return null;
	}

	protected Command getResizeCommand(ChangeBoundsRequest request) throws Exception {
		int layoutMarginLeft = FormUtils.getLayoutMarginLeft(layoutInfo);
		int layoutMarginTop = FormUtils.getLayoutMarginTop(layoutInfo);
		// prepare values from preferences
		int hMargin = layoutInfo.getPreferences().getHorizontalContainerGap();
		int phMargin = layoutInfo.getPreferences().getHorizontalPercentsGap();
		int hwMargin = layoutInfo.getPreferences().getHorizontalComponentGap();
		int sens = layoutInfo.getPreferences().getSnapSensitivity();
		int vMargin = layoutInfo.getPreferences().getVerticalContainerGap();
		int pvMargin = layoutInfo.getPreferences().getVerticalPercentsGap();
		int vwMargin = layoutInfo.getPreferences().getVerticalComponentGap();
		@SuppressWarnings("unchecked")
		C control = (C) getHost().getModel();
		CompoundEditCommand compoundCommand = new CompoundEditCommand(layoutModel);
		Rectangle bounds = getControlModelBounds(control);
		bounds.performTranslate(request.getMoveDelta());
		bounds.resize(request.getSizeDelta());
		Point requestLocation = request.getLocation();
		Point location =
				requestLocation == null
				? getControlModelBounds(control).getLocation()
						: requestLocation.getCopy();
		//
		List<C> hAttachables = FormUtils.getAttachableControls(layoutInfo, control, true);
		List<C> vAttachables = FormUtils.getAttachableControls(layoutInfo, control, false);
		final int direction = request.getResizeDirection();
		//
		int parentWidth = layoutInfo.getContainerSize().width;
		int parentHeight = layoutInfo.getContainerSize().height;
		IFormDataInfo<C> formDataInfo = (IFormDataInfo<C>) layoutInfo.getLayoutData2(control);
		// West
		if (hasDirection(direction, PositionConstants.WEST)) {
			int x = bounds.x;
			Command cmd = null;
			IFormAttachmentInfo<C> attachment = formDataInfo.getAttachment(PositionConstants.LEFT);
			if (useSnap()) {
				if (hwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(hAttachables, false, location.y).iterator(); I.hasNext()
							&& cmd == null;) {
						C child = I.next();
						Rectangle componentBounds = getControlModelBounds(child);
						if (FormUtils.between(x - (componentBounds.right() + hwMargin), 0, sens)) {
							cmd =
									new ResizeToControlCommand(attachment, child, PositionConstants.RIGHT, hwMargin);
						} else if (FormUtils.between(x - componentBounds.right(), -sens, hwMargin)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.RIGHT, 0);
						} else if (FormUtils.between(x - componentBounds.x, -sens, sens)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.LEFT, 0);
						}
					}
				}
				if (cmd == null) {
					List<Integer> formLayoutHPercents = layoutInfo.getPreferences().getHorizontalPercents();
					for (Integer percent : formLayoutHPercents) {
						int newX = (int) ((double) percent.intValue() / 100 * parentWidth) + layoutMarginLeft;
						if (FormUtils.between(x - (newX + hwMargin), 0, sens) && phMargin > -1) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), phMargin);
						} else if (FormUtils.between(x - newX, -sens, hwMargin)) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), 0);
						}
						if (cmd != null) {
							break;
						}
					}
					if (cmd == null) {
						if (hMargin > -1 && x < hMargin + layoutMarginLeft) {
							cmd = new ResizeToMarginCommand(attachment, PositionConstants.LEFT, hMargin);
						} else {
							cmd =
									new ResizeToOffsetCommand(attachment, parentWidth, FormUtils.snapGrid(x
											- layoutMarginLeft, sens));
						}
					}
				}
			} else {
				cmd = new ResizeToOffsetCommand(attachment, parentWidth, x - layoutMarginLeft);
			}
			compoundCommand.add(cmd);
		}
		// East
		if (hasDirection(direction, PositionConstants.EAST)) {
			int x = bounds.right();
			Command cmd = null;
			IFormAttachmentInfo<C> attachment = formDataInfo.getAttachment(PositionConstants.RIGHT);
			if (useSnap()) {
				if (hwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(hAttachables, false, location.y).iterator(); I.hasNext()
							&& cmd == null;) {
						C child = I.next();
						Rectangle componentBounds = getControlModelBounds(child);
						if (FormUtils.between(componentBounds.x - hwMargin - x, 0, sens)) {
							cmd =
									new ResizeToControlCommand(attachment, child, PositionConstants.LEFT, -hwMargin);
						} else if (FormUtils.between(componentBounds.x - x, -sens, hwMargin)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.LEFT, 0);
						} else if (FormUtils.between(componentBounds.right() - x, -sens, sens)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.RIGHT, 0);
						}
					}
				}
				if (cmd == null) {
					List<Integer> formLayoutHPercents = layoutInfo.getPreferences().getHorizontalPercents();
					for (Integer percent : formLayoutHPercents) {
						int newX = (int) ((double) percent.intValue() / 100 * parentWidth) + layoutMarginLeft;
						if (FormUtils.between(newX - hwMargin - x, 0, sens) && phMargin > -1) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), -phMargin);
						} else if (FormUtils.between(newX - x, -sens, hwMargin)) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), 0);
						}
						if (cmd != null) {
							break;
						}
					}
					if (cmd == null) {
						if (hMargin > -1 && x > parentWidth - hMargin + layoutMarginLeft) {
							cmd = new ResizeToMarginCommand(attachment, PositionConstants.RIGHT, hMargin);
						} else {
							cmd =
									new ResizeToOffsetCommand(attachment, parentWidth, FormUtils.snapGrid(x
											- layoutMarginLeft, sens));
						}
					}
				}
			} else {
				cmd = new ResizeToOffsetCommand(attachment, parentWidth, x - layoutMarginLeft);
			}
			compoundCommand.add(cmd);
		}
		// North
		if (hasDirection(direction, PositionConstants.NORTH)) {
			int y = bounds.y;
			Command cmd = null;
			IFormAttachmentInfo<C> attachment = formDataInfo.getAttachment(PositionConstants.TOP);
			if (useSnap()) {
				if (vwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(vAttachables, true, location.x).iterator(); I.hasNext()
							&& cmd == null;) {
						C child = I.next();
						Rectangle componentBounds = getControlModelBounds(child);
						if (FormUtils.between(y - (componentBounds.bottom() + vwMargin), 0, sens)) {
							cmd =
									new ResizeToControlCommand(attachment, child, PositionConstants.BOTTOM, vwMargin);
						} else if (FormUtils.between(y - componentBounds.bottom(), -sens, vwMargin)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.BOTTOM, 0);
						} else if (FormUtils.between(y - componentBounds.y, -sens, sens)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.TOP, 0);
						}
					}
				}
				if (cmd == null) {
					List<Integer> formLayoutVPercents = layoutInfo.getPreferences().getVerticalPercents();
					for (Integer percent : formLayoutVPercents) {
						int newY = (int) ((double) percent.intValue() / 100 * parentHeight) + layoutMarginTop;
						if (FormUtils.between(y - (newY + vwMargin), 0, sens) && pvMargin > -1) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), pvMargin);
						} else if (FormUtils.between(y - newY, -sens, vwMargin)) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), 0);
						}
						if (cmd != null) {
							break;
						}
					}
					if (cmd == null) {
						if (vMargin > -1 && y < vMargin + layoutMarginTop) {
							cmd = new ResizeToMarginCommand(attachment, PositionConstants.TOP, vMargin);
						} else {
							cmd =
									new ResizeToOffsetCommand(attachment, parentHeight, FormUtils.snapGrid(y
											- layoutMarginTop, sens));
						}
					}
				}
			} else {
				cmd = new ResizeToOffsetCommand(attachment, parentHeight, y - layoutMarginTop);
			}
			compoundCommand.add(cmd);
		}
		// South
		if (hasDirection(direction, PositionConstants.SOUTH)) {
			int y = bounds.bottom();
			Command cmd = null;
			IFormAttachmentInfo<C> attachment = formDataInfo.getAttachment(PositionConstants.BOTTOM);
			if (useSnap()) {
				if (vwMargin > -1) {
					for (Iterator<C> I = sortControlsByAxisRange(vAttachables, true, location.x).iterator(); I.hasNext()
							&& cmd == null;) {
						C child = I.next();
						Rectangle componentBounds = getControlModelBounds(child);
						if (FormUtils.between(componentBounds.y - vwMargin - y, 0, sens)) {
							cmd =
									new ResizeToControlCommand(attachment, child, PositionConstants.TOP, -vwMargin);
						} else if (FormUtils.between(componentBounds.y - y, -sens, vwMargin)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.TOP, 0);
						} else if (FormUtils.between(componentBounds.bottom() - y, -sens, sens)) {
							cmd = new ResizeToControlCommand(attachment, child, PositionConstants.BOTTOM, 0);
						}
					}
				}
				if (cmd == null) {
					List<Integer> formLayoutVPercents = layoutInfo.getPreferences().getVerticalPercents();
					for (Integer percent : formLayoutVPercents) {
						int newY = (int) ((double) percent.intValue() / 100 * parentHeight) + layoutMarginTop;
						if (FormUtils.between(newY - vwMargin - y, 0, sens)) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), -pvMargin);
						} else if (FormUtils.between(newY - y, -sens, vwMargin)) {
							cmd = new ResizeToPercentOffsetCommand(attachment, percent.intValue(), 0);
						}
						if (cmd != null) {
							break;
						}
					}
					if (cmd == null) {
						if (vMargin > -1 && y > parentHeight - vMargin + layoutMarginTop) {
							cmd = new ResizeToMarginCommand(attachment, PositionConstants.BOTTOM, vMargin);
						} else {
							cmd =
									new ResizeToOffsetCommand(attachment, parentHeight, FormUtils.snapGrid(y
											- layoutMarginTop, sens));
						}
					}
				}
			} else {
				cmd = new ResizeToOffsetCommand(attachment, parentHeight, y - layoutMarginTop);
			}
			compoundCommand.add(cmd);
		}
		return compoundCommand;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<String, Figure> selectionFeedbacks;

	@Override
	protected void showSelection() {
		// check that part is still selected: Case 9967 - we restore selection in FormLayoutEditPolicy.eraseMoveFeedback()
		if (getHost().getSelected() == EditPart.SELECTED_NONE) {
			return;
		}
		// show default selection (probably handles)
		super.showSelection();
		//
		if (!(getHost().getModel() instanceof IControlInfo)) {
			return;
		}
		//
		if (selectionFeedbacks == null) {
			try {
				selectionFeedbacks = new HashMap<>();
				addSideLinesFeedbacks();
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}
		anchorFigures.hide();
		anchorFigures.show();
	}

	@Override
	public void hideSelection() {
		super.hideSelection();
		hideSelectionFeedbacks();
		anchorFigures.hide();
	}

	private void hideSelectionFeedbacks() {
		if (selectionFeedbacks != null) {
			for (Figure feedback : selectionFeedbacks.values()) {
				FigureUtils.removeFigure(feedback);
			}
			selectionFeedbacks = null;
		}
	}

	private boolean isVSnapPercent(int percent) {
		List<Integer> formLayoutVPercents = layoutInfo.getPreferences().getVerticalPercents();
		return formLayoutVPercents.indexOf(percent) != -1;
	}

	private boolean isHSnapPercent(int percent) {
		List<Integer> formLayoutHPercents = layoutInfo.getPreferences().getHorizontalPercents();
		return formLayoutHPercents.indexOf(percent) != -1;
	}

	/**
	 * Used for add vertical feedback lines for horizontal form attachments
	 */
	private void addVerticalSideLinesFeedbacks(int parentWidth,
			int parentHeight,
			Rectangle bounds,
			IFormAttachmentInfo<C> attachment) {
		if (attachment == null) {
			return;
		}
		boolean isLeft = layoutImpl.isLeft(attachment);
		int sideMargin = layoutInfo.getPreferences().getHorizontalContainerGap();
		if (layoutImpl.isParentAttachment(attachment)) {
			int percent = attachment.getNumerator();
			int offset = attachment.getOffset();
			if (isLeft && percent == 0) {
				if (offset == sideMargin) {
					addVerticalFeedbackLine("W1", bounds.x, 0, parentHeight, offsetColor);
				} else {
					int thisY = bounds.getCenter().y;
					addHorizontalFeedbackLine("W1", thisY, 0, bounds.x, offsetColor);
					addLineEndFigure("W2", 0, thisY, PositionConstants.LEFT, offsetColor);
				}
			} else if (!isLeft && percent == 100) {
				if (offset == -sideMargin) {
					addVerticalFeedbackLine("E1", bounds.right(), 0, parentHeight, offsetColor);
				} else {
					int thisY = bounds.getCenter().y;
					addHorizontalFeedbackLine("E1", thisY, bounds.right(), parentWidth, offsetColor);
					addLineEndFigure("E2", parentWidth, thisY, PositionConstants.RIGHT, offsetColor);
				}
			} else if (isHSnapPercent(percent)) {
				int percentX;
				if (isLeft) {
					percentX = bounds.x - offset;
					if (offset == layoutInfo.getPreferences().getHorizontalPercentsGap()) {
						addVerticalFeedbackLine("W2", percentX + offset, 0, parentHeight, offsetColor);
					}
				} else {
					percentX = bounds.right() - offset;
					if (offset == -layoutInfo.getPreferences().getHorizontalPercentsGap()) {
						addVerticalFeedbackLine("E2", percentX + offset, 0, parentHeight, offsetColor);
					}
				}
				addVerticalFeedbackLine(isLeft ? "W3" : "E3", percentX, 0, parentHeight, baseColor);
			}
		} else if (layoutImpl.isControlAttachment(attachment)
				&& layoutImpl.shouldShowConstraintLine(attachment)) {
			addSideLinesForControlAttachment(bounds, attachment, false);
		}
	}

	/**
	 * Used for add horizontal feedback lines for vertical form attachments
	 */
	private void addHorizontalSideLinesFeedbacks(int parentWidth,
			int parentHeight,
			Rectangle bounds,
			IFormAttachmentInfo<C> attachment) {
		if (attachment == null) {
			return;
		}
		boolean isTop = layoutImpl.isTop(attachment);
		int sideMargin = layoutInfo.getPreferences().getVerticalContainerGap();
		if (layoutImpl.isParentAttachment(attachment)) {
			int percent = attachment.getNumerator();
			int offset = attachment.getOffset();
			if (isTop && percent == 0) {
				if (offset == sideMargin) {
					addHorizontalFeedbackLine("N1", bounds.y, 0, parentWidth, offsetColor);
				} else {
					int thisX = bounds.getCenter().x;
					addVerticalFeedbackLine("N1", thisX, 0, bounds.y, offsetColor);
					addLineEndFigure("N2", thisX, 0, PositionConstants.TOP, offsetColor);
				}
			} else if (!isTop && percent == 100) {
				if (offset == -sideMargin) {
					addHorizontalFeedbackLine("S1", bounds.bottom(), 0, parentWidth, offsetColor);
				} else {
					int thisX = bounds.getCenter().x;
					addVerticalFeedbackLine("S1", thisX, bounds.bottom(), parentHeight, offsetColor);
					addLineEndFigure("S2", thisX, parentHeight, PositionConstants.BOTTOM, offsetColor);
				}
			} else if (isVSnapPercent(percent)) {
				int percentY;
				if (isTop) {
					percentY = bounds.y - offset;
					if (offset == layoutInfo.getPreferences().getVerticalPercentsGap()) {
						addHorizontalFeedbackLine("N2", percentY + offset, 0, parentWidth, offsetColor);
					}
				} else {
					percentY = bounds.bottom() - offset;
					if (offset == -layoutInfo.getPreferences().getVerticalPercentsGap()) {
						addHorizontalFeedbackLine("S2", percentY + offset, 0, parentWidth, offsetColor);
					}
				}
				addHorizontalFeedbackLine(isTop ? "N3" : "S3", percentY, 0, parentWidth, baseColor);
			}
		} else if (layoutImpl.isControlAttachment(attachment)
				&& layoutImpl.shouldShowConstraintLine(attachment)) {
			addSideLinesForControlAttachment(bounds, attachment, true);
		}
	}

	private void addSideLinesFeedbacks() throws Exception {
		@SuppressWarnings("unchecked")
		C control = (C) getHost().getModel();
		ICompositeInfo composite = layoutInfo.getComposite();
		int parentWidth = composite.getClientArea().width;
		int parentHeight = composite.getClientArea().height;
		Rectangle bounds = getControlModelBounds(control);
		addVerticalSideLinesFeedbacks(
				parentWidth,
				parentHeight,
				bounds,
				layoutImpl.getAttachment(control, PositionConstants.LEFT));
		addVerticalSideLinesFeedbacks(
				parentWidth,
				parentHeight,
				bounds,
				layoutImpl.getAttachment(control, PositionConstants.RIGHT));
		addHorizontalSideLinesFeedbacks(
				parentWidth,
				parentHeight,
				bounds,
				layoutImpl.getAttachment(control, PositionConstants.TOP));
		addHorizontalSideLinesFeedbacks(
				parentWidth,
				parentHeight,
				bounds,
				layoutImpl.getAttachment(control, PositionConstants.BOTTOM));
	}

	/**
	 * @param b
	 *          Control bounds in parent coordinates
	 * @param attachment
	 * @param vertical
	 */
	private void addSideLinesForControlAttachment(Rectangle b,
			IFormAttachmentInfo<C> attachment,
			boolean vertical) {
		C alignControl = attachment.getControl();
		IEditPartViewer viewer = getHost().getViewer();
		GraphicalEditPart bindPart = (GraphicalEditPart) viewer.getEditPartRegistry().get(alignControl);
		if (bindPart == null) {
			return;
		}
		Rectangle bounds = b.getCopy();
		Rectangle bindBounds = getControlModelBounds(alignControl);
		// transpose bounds for vertical
		if (vertical) {
			bindBounds.transpose();
			bounds.transpose();
		}
		int x;
		int controlAlign = attachment.getAlignment();
		if (controlAlign == SWT.LEFT || controlAlign == SWT.TOP) {
			x = bindBounds.x;
		} else {
			x = bindBounds.right();
		}
		//
		int y1 = Math.min(bindBounds.y, bounds.y);
		int y2 = Math.max(bindBounds.bottom(), bounds.bottom());
		int offset = attachment.getOffset();
		y1 -= EXTENSION;
		y2 += EXTENSION;
		if (vertical) {
			addHorizontalFeedbackLine(
					layoutImpl.isTop(attachment) ? "N6" : "S6",
							x + offset,
							y1,
							y2,
							offsetColor);
		} else {
			addVerticalFeedbackLine(
					layoutImpl.isLeft(attachment) ? "W6" : "E6",
							x + offset,
							y1,
							y2,
							offsetColor);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities for selection feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private void addVerticalFeedbackLine(String name, int x, int y1, int y2, Color color) {
		selectionFeedbacks.put(name, addFeedbackLine(x, y1, x, y2, color));
	}

	private void addHorizontalFeedbackLine(String name, int y, int x1, int x2, Color color) {
		selectionFeedbacks.put(name, addFeedbackLine(x1, y, x2, y, color));
	}

	private void addLineEndFigure(String name, int x, int y, int alignment, Color color) {
		selectionFeedbacks.put(name, addLineEndFigure(x, y, alignment, color));
	}

	private void removeSelectionFeedbacks(String prefix) {
		for (Iterator<String> I = selectionFeedbacks.keySet().iterator(); I.hasNext();) {
			String key = I.next();
			if (!key.startsWith(prefix)) {
				continue;
			}
			Figure feedback = selectionFeedbacks.get(key);
			feedback.getParent().remove(feedback);
			I.remove();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities for feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	private Polyline addFeedbackLine(int x1, int y1, int x2, int y2, Color color) {
		Polyline line = new Polyline();
		Point p1 = new Point(x1, y1);
		translateModelToFeedback(p1);
		Point p2 = new Point(x2, y2);
		translateModelToFeedback(p2);
		line.addPoint(p1);
		line.addPoint(p2);
		line.setForegroundColor(color);
		line.setLineStyle(SWT.LINE_DOT);
		// add feedback
		addFeedback(line);
		return line;
	}

	private LineEndFigure addLineEndFigure(int x, int y, int alignment, Color color) {
		Point point = new Point(x, y);
		translateModelToFeedback(point);
		LineEndFigure lineEndFigure = new LineEndFigure(alignment, color);
		Dimension size = lineEndFigure.getSize();
		lineEndFigure.setLocation(new Point(point.x - size.width / 2, point.y - size.height / 2));
		addFeedback(lineEndFigure);
		return lineEndFigure;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Coordinates utilities
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns model bounds of the control in parent client area coordinates
	 *
	 * @param control
	 * @return
	 */
	public static <C extends IControlInfo> Rectangle getControlModelBounds(C control) {
		return control.getModelBounds().getCopy();
	}

	private void translateModelToFeedback(Translatable t) {
		PolicyUtils.translateModelToFeedback(this, t);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Quadrant feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private MouseMotionListener mouseMotionListener;
	private FigureListener figureListener;
	private Figure hoverFigure;
	private int mouseQuadrant;

	private void installQuadrantHandler() {
		mouseQuadrant = -1;
		final IFigure figure = getHostFigure();
		// add mouse listener
		mouseMotionListener = new MouseMotionListener.Stub() {
			@Override
			public void mouseExited(MouseEvent event) {
				mouseQuadrant = -1;
				figure.repaint();
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				int oldQuadrant = mouseQuadrant;
				Rectangle r = figure.getBounds().getCopy();
				if (event.x < r.width / 2 && event.y < r.height / 2) {
					mouseQuadrant = 0;
				} else if (event.y < r.height / 2) {
					mouseQuadrant = 1;
				} else if (event.x < r.width / 2) {
					mouseQuadrant = 2;
				} else {
					mouseQuadrant = 3;
				}
				if (mouseQuadrant != oldQuadrant) {
					figure.repaint();
				}
			}
		};
		figureListener = new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				Rectangle bounds = figure.getBounds();
				hoverFigure.setBounds(new Rectangle(0, 0, bounds.width, bounds.height));
			}
		};
		hoverFigure = new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				if (mouseQuadrant == -1) {
					return;
				}
				graphics.setBackgroundColor(ColorConstants.blue);
				Rectangle bounds = figure.getBounds();
				PointList points = new PointList();
				switch (mouseQuadrant) {
				case 0 :
					points.addPoint(1, 1);
					points.addPoint(7, 1);
					points.addPoint(1, 7);
					break;
				case 1 :
					points.addPoint(bounds.width - 1, 1);
					points.addPoint(bounds.width - 7, 1);
					points.addPoint(bounds.width - 1, 7);
					break;
				case 2 :
					points.addPoint(1, bounds.height - 1);
					points.addPoint(7, bounds.height - 1);
					points.addPoint(1, bounds.height - 7);
					break;
				case 3 :
					points.addPoint(bounds.width - 1, bounds.height - 1);
					points.addPoint(bounds.width - 7, bounds.height - 1);
					points.addPoint(bounds.width - 1, bounds.height - 7);
					break;
				default :
					break;
				}
				graphics.fillPolygon(points);
			}
		};
		hoverFigure.setOpaque(false);
		Rectangle figureBounds = figure.getBounds();
		hoverFigure.setBounds(new Rectangle(0, 0, figureBounds.width, figureBounds.height));
		hoverFigure.addMouseMotionListener(mouseMotionListener);
		figure.add(hoverFigure, 0);
		figure.addFigureListener(figureListener);
	}

	private void uninstallQuadrantHandler() {
		getHostFigure().removeFigureListener(figureListener);
		FigureUtils.removeFigure(hoverFigure);
	}

	private void removeResizeFeedbacks() {
		removeResizeFeedback("H");
		removeResizeFeedback("V");
	}

	private void removeResizeFeedback(String name) {
		removeSingleResizeFeedback(name + "11");
		removeSingleResizeFeedback(name + "12");
		removeSingleResizeFeedback(name + "2");
		removeSingleResizeFeedback(name + "3");
		removeSingleResizeFeedback(name + "4");
		removeSingleResizeFeedback(name + "5");
	}

	private void removeSingleResizeFeedback(String name) {
		Figure feedback = resizeFeedbacks.get(name);
		if (feedback != null) {
			removeFeedback(feedback);
			resizeFeedbacks.remove(name);
		}
	}

	private void addVerticalResizeLine(String suffix, C component, int x, Color color)
			throws Exception {
		Rectangle bounds = getControlModelBounds(component);
		Polyline line = addFeedbackLine(x, bounds.y, x, bounds.bottom(), color);
		line.setLineWidth(2);
		line.setLineStyle(SWT.LINE_SOLID);
		resizeFeedbacks.put("H" + suffix, line);
	}

	private void addVerticalResizeLine(String suffix, int x, int parentHeight, Color color) {
		Polyline line = addFeedbackLine(x, parentHeight - 5, x, parentHeight + 5, color);
		line.setLineWidth(3);
		line.setLineStyle(SWT.LINE_SOLID);
		resizeFeedbacks.put("H" + suffix, line);
	}

	private void addVerticalResizeLine(String suffix, int x, Color color) {
		ICompositeInfo composite = layoutInfo.getComposite();
		int parentHeight = composite.getClientArea().height;
		Polyline line = addFeedbackLine(x, 0, x, parentHeight, color);
		resizeFeedbacks.put("H" + suffix, line);
	}

	private void addHorizontalResizeLine(String suffix, C component, int y, Color color)
			throws Exception {
		Rectangle bounds = getControlModelBounds(component);
		Polyline line = addFeedbackLine(bounds.x, y, bounds.right(), y, color);
		line.setLineWidth(2);
		line.setLineStyle(SWT.LINE_SOLID);
		resizeFeedbacks.put("V" + suffix, line);
	}

	private void addHorizontalResizeLine(String suffix, int y, int parentWidth, Color color) {
		Polyline line = addFeedbackLine(parentWidth - 5, y, parentWidth + 5, y, color);
		line.setLineWidth(3);
		line.setLineStyle(SWT.LINE_SOLID);
		resizeFeedbacks.put("V" + suffix, line);
	}

	private void addHorizontalResizeLine(String suffix, int y, Color color) {
		ICompositeInfo composite = layoutInfo.getComposite();
		int parentWidth = composite.getClientArea().width;
		Polyline line = addFeedbackLine(0, y, parentWidth, y, color);
		resizeFeedbacks.put("V" + suffix, line);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	private FormLayoutEditPolicyClassic<C> getLayoutEditPolicy() {
		FormLayoutEditPolicyClassic<C> editPolicy =
				(FormLayoutEditPolicyClassic<C>) getHost().getParent().getEditPolicy(EditPolicy.LAYOUT_ROLE);
		return editPolicy;
	}

	private static boolean hasDirection(int direction, int mask) {
		return (direction & mask) == mask;
	}

	private boolean useSnap() {
		return getLayoutEditPolicy().useSnap();
	}

	/**
	 * Sort controls by nearest distance from specified coordinate.
	 */
	static <C extends IControlInfo> List<C> sortControlsByAxisRange(List<C> components,
			final boolean isX,
			final int value) {
		List<C> newControls = new LinkedList<>();
		newControls.addAll(components);
		// proceed with sorting
		Collections.sort(newControls, new Comparator<C>() {
			@Override
			public int compare(C o1, C o2) {
				C component1 = o1;
				C component2 = o2;
				Rectangle bounds1 = getControlModelBounds(component1);
				Rectangle bounds2 = getControlModelBounds(component2);
				if (isX) {
					int min1 = min(value, bounds1.x, bounds1.right());
					int min2 = min(value, bounds2.x, bounds2.right());
					return min1 - min2;
				}
				int min1 = min(value, bounds1.y, bounds1.bottom());
				int min2 = min(value, bounds2.y, bounds2.bottom());
				return min1 - min2;
			}

			private int min(int x, int a, int b) {
				return Math.min(Math.abs(x - a), Math.abs(x - b));
			}
		});
		// return sorted
		return newControls;
	}

	private static void setX(Rectangle r, int x) {
		r.width += r.x - x;
		r.x = x;
	}

	private static void setY(Rectangle r, int y) {
		r.height += r.y - y;
		r.y = y;
	}

	public int getMouseQuadrant() {
		return mouseQuadrant;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location and size hints
	//
	////////////////////////////////////////////////////////////////////////////
	TextFeedback xTextFeedback;
	TextFeedback yTextFeedback;

	private void hideSizeHints() {
		if (xTextFeedback != null) {
			xTextFeedback.remove();
			xTextFeedback = null;
		}
		if (yTextFeedback != null) {
			yTextFeedback.remove();
			yTextFeedback = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Inner classes
	//
	////////////////////////////////////////////////////////////////////////////
	private final class ResizeToPercentOffsetCommand extends EditCommand {
		private final int offset;
		private final IFormAttachmentInfo<C> attachment;
		private final int percent;

		private ResizeToPercentOffsetCommand(IFormAttachmentInfo<C> attachment, int percent, int offset) {
			super(attachment);
			this.attachment = attachment;
			this.percent = percent;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.resizeToPercentOffset(attachment, percent, offset);
		}
	}
	private final class ResizeToOffsetCommand extends EditCommand {
		private final int offset;
		private final IFormAttachmentInfo<C> attachment;
		private final int parentDimension;

		private ResizeToOffsetCommand(IFormAttachmentInfo<C> attachment, int parentDimension, int offset) {
			super(attachment);
			this.attachment = attachment;
			this.parentDimension = parentDimension;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.resizeToOffset(attachment, parentDimension, offset);
		}
	}
	private final class ResizeToMarginCommand extends EditCommand {
		private final IFormAttachmentInfo<C> attachment;
		private final int marginValue;
		private final int direction;

		private ResizeToMarginCommand(IFormAttachmentInfo<C> attachment, int direction, int marginValue) {
			super(attachment);
			this.attachment = attachment;
			this.direction = direction;
			this.marginValue = marginValue;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.resizeToMargin(attachment, direction, marginValue);
		}
	}
	private final class ResizeToControlCommand extends EditCommand {
		private final IFormAttachmentInfo<C> attachment;
		private final C control;
		private final int offset;
		private final int position;

		public ResizeToControlCommand(IFormAttachmentInfo<C> attachment,
				C control,
				int position,
				int offset) {
			super(attachment);
			this.attachment = attachment;
			this.control = control;
			this.position = position;
			this.offset = offset;
		}

		@Override
		protected void executeEdit() throws Exception {
			layoutImpl.resizeToControl(attachment, control, position, offset);
		}
	}
}
