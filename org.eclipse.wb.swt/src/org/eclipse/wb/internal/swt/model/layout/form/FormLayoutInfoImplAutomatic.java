/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation using automatic component placement.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class FormLayoutInfoImplAutomatic<C extends IControlInfo>
extends
FormLayoutInfoImpl<C> implements IAbsoluteLayoutCommands {
	private final IFormLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutInfoImplAutomatic(IFormLayoutInfo<C> formLayoutInfo) {
		m_layout = formLayoutInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move operations
	//
	////////////////////////////////////////////////////////////////////////////
	public void command_moveToContainer(List<? extends IAbstractComponentInfo> sourceSet,
			IAbstractComponentInfo nearestBeingSnapped,
			int side,
			int distance) throws Exception {
	}

	public void command_moveAsAttachedToComponent(List<? extends IAbstractComponentInfo> sourceSet,
			IAbstractComponentInfo nearestBeingSnapped,
			int sourceSide,
			IAbstractComponentInfo target,
			int targetSide,
			int gap) {
		String a = FormLayoutUtils.getAlignmentSource(FormLayoutUtils.convertGefSide(sourceSide));
		String b = FormLayoutUtils.getAlignmentSource(FormLayoutUtils.convertGefSide(targetSide));
	}

	public void command_moveToPercent(List<? extends IAbstractComponentInfo> workingSet,
			IAbstractComponentInfo nearestBeingSnapped,
			int sourceSide,
			int percent,
			int gap) {
		String a = FormLayoutUtils.getAlignmentSource(FormLayoutUtils.convertGefSide(sourceSide));
	}

	/**
	 * @param bounds
	 *          the new (union) bounds of the widgets.
	 * @param sourceSet
	 *          the set of widgets which the user is dragging.
	 * @param singleSource
	 *          the widget which is nearest to the edge by dragging direction.
	 * @param moveDirection
	 *          LEADING or TRAILING dragging direction.
	 * @param isHorizontal
	 */
	public void command_moveFreely(Rectangle bounds,
			List<? extends IAbstractComponentInfo> sourceSet,
			IAbstractComponentInfo singleSource,
			int moveDirection,
			boolean isHorizontal) throws Exception {
		NeighborInfo leadingNeighbor =
				findNeighbor(bounds, sourceSet, PlacementInfo.LEADING, isHorizontal);
		NeighborInfo trailingNeighbor =
				findNeighbor(bounds, sourceSet, PlacementInfo.TRAILING, isHorizontal);
		int newAlignmentHint =
				leadingNeighbor.distance < trailingNeighbor.distance
				? PlacementInfo.LEADING
						: PlacementInfo.TRAILING;
		// single
		AlignmentInfo currentAlignment = getAlignment(singleSource, isHorizontal);
		// non-resizable
		if (!currentAlignment.resizable) {
			// TODO: pay attention to moving direction
			// TODO: pay attention on existing alignment
			attachNonResizableFreely(
					singleSource,
					bounds,
					leadingNeighbor,
					trailingNeighbor,
					newAlignmentHint,
					isHorizontal);
		} else {
			attachResizableFreely(singleSource, bounds, leadingNeighbor, trailingNeighbor, isHorizontal);
		}
		keepWidgetsPositions(sourceSet, isHorizontal);
		optimizeLayout();
	}

	private void optimizeLayout() {
		// TODO:
	}

	private void keepWidgetsPositions(List<? extends IAbstractComponentInfo> sourceSet,
			boolean isHorizontal) throws Exception {
		List<AttachmentDef> affected = findAttachedToSource(sourceSet, isHorizontal);
		for (AttachmentDef attachmentDef : affected) {
		}
	}

	private List<AttachmentDef> findAttachedToSource(List<? extends IAbstractComponentInfo> sourceSet,
			boolean isHorizontal) throws Exception {
		List<AttachmentDef> affected = new ArrayList<>();
		int leadingSide = PlacementUtils.getSide(isHorizontal, true);
		int trailingSide = PlacementUtils.getSide(isHorizontal, false);
		List<IAbstractComponentInfo> remainingComponents = getRemainingComponents(sourceSet);
		for (IAbstractComponentInfo source : remainingComponents) {
			for (IAbstractComponentInfo target : sourceSet) {
				checkAttachedToComponent(source, target, leadingSide, affected);
				checkAttachedToComponent(source, target, trailingSide, affected);
			}
		}
		return affected;
	}

	private void checkAttachedToComponent(IAbstractComponentInfo source,
			IAbstractComponentInfo possibleTarget,
			int checkingSide,
			List<AttachmentDef> resultList) throws Exception {
		if (isAttached(source, checkingSide)) {
			IFormAttachmentInfo<C> attachment = getAttachment(source, checkingSide);
			if (attachment.getControl() == possibleTarget) {
				AttachmentDef def = new AttachmentDef();
				def.source = source;
				def.target = possibleTarget;
				def.sourceSide = checkingSide;
				def.targetSide = FormLayoutUtils.convertSwtAlignment(attachment.getAlignment());
				resultList.add(def);
			}
		}
	}

	private void attachResizableFreely(IAbstractComponentInfo source,
			Rectangle bounds,
			NeighborInfo leadingNeighbor,
			NeighborInfo trailingNeighbor,
			boolean isHorizontal) throws Exception {
		int leadingSide = PlacementUtils.getSide(isHorizontal, true);
		int trailingSide = PlacementUtils.getSide(isHorizontal, false);
		if (leadingNeighbor.neighbor == null) {
			attachResizableToContainer(source, bounds, leadingSide);
		} else {
			IAbstractComponentInfo target = leadingNeighbor.neighbor;
			int offset = leadingNeighbor.distance;
			attachSideToComponent(source, target, offset, leadingSide, trailingSide);
		}
		if (trailingNeighbor.neighbor == null) {
			attachResizableToContainer(source, bounds, PlacementUtils.getSide(isHorizontal, false));
		} else {
			IAbstractComponentInfo target = trailingNeighbor.neighbor;
			int offset = -trailingNeighbor.distance;
			attachSideToComponent(source, target, offset, trailingSide, leadingSide);
		}
	}

	private void attachResizableToContainer(IAbstractComponentInfo source,
			Rectangle bounds,
			int checkingSide) throws Exception {
		boolean isHorizontal = PlacementUtils.isHorizontalSide(checkingSide);
		int targetSide = getTargetContainerSide(source, checkingSide);
		boolean isLeadingSide = PlacementUtils.isLeadingSide(checkingSide);
		int containerSize = PlacementUtils.getSideSize(m_layout.getContainerSize(), targetSide);
		int offset;
		if (checkingSide == targetSide) {
			if (isLeadingSide) {
				offset = isHorizontal ? bounds.x : bounds.y;
			} else {
				offset = -(containerSize - (isHorizontal ? bounds.right() : bounds.bottom()));
			}
		} else {
			if (isLeadingSide) {
				offset = -(containerSize - (isHorizontal ? bounds.x : bounds.y));
			} else {
				offset = isHorizontal ? bounds.right() : bounds.bottom();
			}
		}
		attachSideToContainer(source, offset, checkingSide, targetSide);
	}

	private int getTargetContainerSide(IAbstractComponentInfo source, int side) throws Exception {
		Assert.isLegal(isAttached(source, side));
		IFormAttachmentInfo<C> attachment = getAttachment(source, side);
		Assert.isLegal(attachment.getControl() == null);
		int numerator = attachment.getNumerator();
		boolean isTrailing = numerator == 100;
		boolean isHorizontal = PlacementUtils.isHorizontalSide(side);
		return PlacementUtils.getSide(isHorizontal, !isTrailing);
	}

	private void attachNonResizableFreely(IAbstractComponentInfo source,
			Rectangle bounds,
			NeighborInfo leadingNeighbor,
			NeighborInfo trailingNeighbor,
			int alignment,
			boolean isHorizontal) throws Exception {
		int leadingSide = PlacementUtils.getSide(isHorizontal, true);
		int trailingSide = PlacementUtils.getSide(isHorizontal, false);
		boolean isLeadingAlignment = alignment == PlacementInfo.LEADING;
		int targetContainerSide = PlacementUtils.getSide(isHorizontal, isLeadingAlignment);
		boolean leadingAttached = isAttached(source, leadingSide);
		boolean trailingAttached = isAttached(source, trailingSide);
		if (leadingAttached ^ trailingAttached || !leadingAttached && !trailingAttached) {
			// attached by single side or none
			if (isLeadingAlignment) {
				if (leadingNeighbor.neighbor == null) {
					attachSideToContainer(source, leadingNeighbor.distance, leadingSide, targetContainerSide);
				} else {
					attachSideToComponent(
							source,
							leadingNeighbor.neighbor,
							leadingNeighbor.distance,
							leadingSide,
							PlacementUtils.getOppositeSide(leadingSide));
				}
				deleteAttachment(source, trailingSide);
			} else {
				if (trailingNeighbor.neighbor == null) {
					attachSideToContainer(
							source,
							-trailingNeighbor.distance,
							trailingSide,
							targetContainerSide);
				} else {
					attachSideToComponent(
							source,
							trailingNeighbor.neighbor,
							-trailingNeighbor.distance,
							trailingSide,
							PlacementUtils.getOppositeSide(trailingSide));
				}
				deleteAttachment(source, leadingSide);
			}
		} else {
			// both sides
			int offset = isLeadingAlignment ? leadingNeighbor.distance : -trailingNeighbor.distance;
			int width = PlacementUtils.getSideSize(source.getModelBounds().getSize(), leadingSide);
			NeighborInfo targetNeighbor = isLeadingAlignment ? leadingNeighbor : trailingNeighbor;
			if (targetNeighbor.neighbor == null) {
				{
					int widthDelta = isLeadingAlignment ? 0 : -width;
					attachSideToContainer(source, offset + widthDelta, leadingSide, targetContainerSide);
				}
				{
					int widthDelta = isLeadingAlignment ? width : 0;
					attachSideToContainer(source, offset + widthDelta, trailingSide, targetContainerSide);
				}
			} else {
				{
					int widthDelta = isLeadingAlignment ? 0 : -width;
					attachSideToComponent(
							source,
							targetNeighbor.neighbor,
							offset + widthDelta,
							leadingSide,
							isLeadingAlignment ? PlacementUtils.getOppositeSide(leadingSide) : leadingSide);
				}
				{
					int widthDelta = isLeadingAlignment ? width : 0;
					attachSideToComponent(
							source,
							targetNeighbor.neighbor,
							offset + widthDelta,
							trailingSide,
							isLeadingAlignment ? trailingSide : PlacementUtils.getOppositeSide(trailingSide));
				}
			}
		}
	}

	private NeighborInfo findNeighbor(Rectangle bounds,
			List<? extends IAbstractComponentInfo> sourceSet,
			int direction,
			boolean isHorizontal) {
		NeighborInfo neighbor = new NeighborInfo(direction);
		Transposer t = new Transposer(!isHorizontal);
		Rectangle widgetsBounds = t.t(bounds.getCopy());
		Interval widgetsWidth = new Interval(widgetsBounds.x, widgetsBounds.width);
		Interval widgetsHeight = new Interval(widgetsBounds.y, widgetsBounds.height);
		List<IAbstractComponentInfo> remainingWidgets = getRemainingComponents(sourceSet);
		for (IAbstractComponentInfo widget : remainingWidgets) {
			// test where is the neighbor component located: leading or trailing
			Rectangle possibleNeighborBounds = t.t(getTranslatedBounds(widget));
			// neighbor should intersect in opposite dimension.
			Interval possibleNeighborHeight =
					new Interval(possibleNeighborBounds.y, possibleNeighborBounds.height);
			if (widgetsHeight.intersects(possibleNeighborHeight)) {
				// this is possible neighbor, get the most nearest depending on side: leading or trailing
				Interval possibleNeighborWidth =
						new Interval(possibleNeighborBounds.x, possibleNeighborBounds.width);
				if (!possibleNeighborWidth.intersects(widgetsWidth)) {
					// no overlapping, check the distances
					if (direction == PlacementInfo.LEADING && possibleNeighborWidth.isLeadingOf(widgetsWidth)) {
						// leading
						int distance = widgetsWidth.distance(possibleNeighborWidth.end());
						if (neighbor.distance > distance) {
							neighbor.distance = distance;
							neighbor.neighbor = widget;
							continue;
						}
					} else if (direction == PlacementInfo.TRAILING
							&& possibleNeighborWidth.isTrailingOf(widgetsWidth)) {
						// trailing
						int distance = widgetsWidth.distance(possibleNeighborWidth.begin());
						if (neighbor.distance > distance) {
							neighbor.distance = distance;
							neighbor.neighbor = widget;
							continue;
						}
					}
				}
			}
		}
		if (neighbor.neighbor == null) {
			// if no one wants to be our neighbor ;-) so get the distance to the container's boundary
			Dimension containerSize = t.t(m_layout.getContainerSize());
			neighbor.distance =
					direction == PlacementInfo.LEADING ? widgetsWidth.begin() : containerSize.width
							- widgetsWidth.end();
		}
		return neighbor;
	}

	public AlignmentInfo getAlignment(IAbstractComponentInfo widget, boolean isHorizontal)
			throws Exception {
		int leadingSide = PlacementUtils.getSide(isHorizontal, true);
		int trailingSide = PlacementUtils.getSide(isHorizontal, false);
		boolean attachedLeading = isAttached(widget, leadingSide);
		boolean attachedTrailing = isAttached(widget, trailingSide);
		if (!attachedLeading && !attachedTrailing) {
			// no constraints
			return new AlignmentInfo(PlacementInfo.LEADING);
		} else if (attachedLeading && attachedTrailing) {
			int lEffectiveAlignment = getEffectiveAlignmentForSide(widget, leadingSide);
			int tEffectiveAlignment = getEffectiveAlignmentForSide(widget, trailingSide);
			if (lEffectiveAlignment != tEffectiveAlignment) {
				return new AlignmentInfo(PlacementInfo.LEADING, true);
			} else {
				// can be resizable if both edges attached to different percents
				IFormAttachmentInfo<C> lAttachment = getAttachment(widget, leadingSide);
				IFormAttachmentInfo<C> tAttachment = getAttachment(widget, trailingSide);
				if (lAttachment.getNumerator() != tAttachment.getNumerator()) {
					return new AlignmentInfo(PlacementInfo.LEADING, true);
				}
				// indirectly non-resizable
				return new AlignmentInfo(lEffectiveAlignment);
			}
		} else {
			// attached single side only
			return new AlignmentInfo(getEffectiveAlignmentForSide(widget, attachedLeading
					? leadingSide
							: trailingSide));
		}
	}

	private int getEffectiveAlignmentForSide(IAbstractComponentInfo widget, int side)
			throws Exception {
		// go by given side until parent or not attached.
		IAbstractComponentInfo testedWidget = widget;
		int testedSide = side;
		while (true) {
			if (isAttached(testedWidget, testedSide)) {
				IFormAttachmentInfo<C> attachment = getAttachment(testedWidget, testedSide);
				C anchorComponent = attachment.getControl();
				if (anchorComponent != null) {
					// another component
					testedWidget = anchorComponent;
					testedSide = attachment.getSide().getEngineSide();
					continue;
				} else {
					// container
					return attachment.getNumerator() > 0 ? PlacementInfo.TRAILING : PlacementInfo.LEADING;
				}
			} else {
				return PlacementUtils.getSidePosition(PlacementUtils.getOppositeSide(testedSide));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc/Helpers
	//
	////////////////////////////////////////////////////////////////////////////
	private List<IAbstractComponentInfo> getRemainingComponents(List<? extends IAbstractComponentInfo> sourceSet) {
		List<IAbstractComponentInfo> components = new ArrayList<>();
		List<C> allControls = m_layout.getControls();
		for (C control : allControls) {
			if (!sourceSet.contains(control)) {
				components.add(control);
			}
		}
		return components;
	}

	private Rectangle getUnionRectangle(List<? extends IAbstractComponentInfo> sourceSet) {
		Rectangle unionRectangle = new Rectangle();
		for (IAbstractComponentInfo component : sourceSet) {
			unionRectangle.union(component.getModelBounds());
		}
		return unionRectangle;
	}

	private Rectangle getTranslatedBounds(IAbstractComponentInfo widget) {
		return PlacementUtils.getTranslatedBounds(
				m_layout.getComposite().getClientArea().getLocation(),
				widget);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Low-level" attachment manipulations
	//
	////////////////////////////////////////////////////////////////////////////
	private void attachSideToContainer(IAbstractComponentInfo source,
			int offset,
			int sourceSide,
			int containerSide) throws Exception {
		int numerator = PlacementUtils.isLeadingSide(containerSide) ? 0 : 100;
		IFormAttachmentInfo<C> attachment = getAttachment(source, sourceSide);
		attachment.setControl(null);
		attachment.setNumerator(numerator);
		attachment.setDenominator(100);
		attachment.setOffset(offset);
		attachment.write();
	}

	private void attachSideToComponent(IAbstractComponentInfo source,
			IAbstractComponentInfo target,
			int offset,
			int sourceSide,
			int targetSide) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(source, sourceSide);
		attachment.setControl((C) target);
		attachment.setAlignment(FormLayoutUtils.convertGefSide(targetSide));
		attachment.setNumerator(0);
		attachment.setDenominator(100);
		attachment.setOffset(offset);
		attachment.write();
	}

	private void deleteAttachment(IAbstractComponentInfo source, int sourceSide) throws Exception {
		if (isAttached(source, sourceSide)) {
			IFormAttachmentInfo<C> attachment = getAttachment(source, sourceSide);
			attachment.delete();
		}
	}

	/**
	 * @return the {@link IFormAttachment} instance for given <code>side</code> of widget.
	 */
	private final IFormAttachmentInfo<C> getAttachment(IAbstractComponentInfo widget, int side)
			throws Exception {
		IFormDataInfo<C> layoutData = (IFormDataInfo<C>) m_layout.getLayoutData2((C) widget);
		return layoutData.getAttachment(side);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//	Helper classes
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class NeighborInfo {
		final int direction;
		int distance = PlacementInfo.UNDEFINED_DISTANCE;
		IAbstractComponentInfo neighbor;

		NeighborInfo(int direction) {
			this.direction = direction;
		}
	}
	public static final class AlignmentInfo {
		public final int alignment;
		public final boolean resizable;

		public AlignmentInfo(int alignment) {
			this(alignment, false);
		}

		public AlignmentInfo(int alignment, boolean resizable) {
			this.alignment = alignment;
			this.resizable = resizable;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Obsolete code, is subject to remove
	//
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	//
	// Layout manipulation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void detach(IAbstractComponentInfo widget, int side) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
		// do nothing if it is not exist
		if (attachment.isVirtual()) {
			return;
		}
		// proceed with detach
		attachment.delete();
	}

	// XXX
	@Override
	public void attachAbsolute(IAbstractComponentInfo widget, int side, int distance)
			throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
		attachment.setControl(null);
		if (PlacementUtils.isTrailingSide(side)) {
			attachment.setOffset(-distance);
			attachment.setNumerator(100);
		} else {
			attachment.setOffset(distance);
			attachment.setNumerator(0);
		}
		attachment.write();
	}

	@Override
	public void adjustAttachmentOffset(IAbstractComponentInfo widget, int side, int delta)
			throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
		int oldOffset = attachment.getOffset();
		int newOffset = oldOffset + delta;
		attachment.setOffset(newOffset);
		attachment.write();
	}

	public void attachContainer(IAbstractComponentInfo widget, int side, int distance)
			throws Exception {
		attachAbsolute(widget, side, distance);
	}

	@Override
	public void attachWidgetSequientially(IAbstractComponentInfo widget,
			IAbstractComponentInfo attachToWidget,
			int side,
			int distance) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
		attachment.setControl((C) attachToWidget);
		attachment.setOffset(PlacementUtils.isTrailingSide(side) ? -distance : distance);
		{
			int oppositeSide = PlacementUtils.getOppositeSide(side);
			int oppositeSideGef = FormLayoutUtils.convertGefSide(oppositeSide);
			attachment.setAlignment(oppositeSideGef);
		}
		attachment.write();
	}

	@Override
	public void attachWidgetParallelly(IAbstractComponentInfo widget,
			IAbstractComponentInfo attachToWidget,
			int side,
			int distance) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
		attachment.setControl((C) attachToWidget);
		attachment.setOffset(PlacementUtils.isTrailingSide(side) ? -distance : distance);
		attachment.setAlignment(FormLayoutUtils.convertGefSide(side));
		attachment.write();
	}

	@Override
	public void attachWidgetBaseline(IAbstractComponentInfo widget,
			IAbstractComponentInfo attachToWidget) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(widget, PlacementUtils.getSide(false, true));
		attachment.setControl((C) attachToWidget);
		int offset = 0;
		int baseline = widget.getBaseline();
		int targetBaseline = attachToWidget.getBaseline();
		if (baseline == -1 || targetBaseline == -1) {
			// move to center
			Rectangle sourceBounds = widget.getModelBounds();
			Rectangle targetBounds = attachToWidget.getModelBounds();
			offset = targetBounds.bottom() / 2 - sourceBounds.height / 2;
		} else {
			offset = targetBaseline - baseline;
		}
		attachment.setOffset(offset);
		attachment.setAlignment(SWT.TOP);
		attachment.write();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	/*public void setExplicitSize(IAbstractComponentInfo widget, int side, int draggingSide, int resizeDelta)
  		throws Exception {
  	// set the size explicitly in layout data, if the resulting size is preferred size, remove the size set in layout data
  	Rectangle modelBounds = widget.getModelBounds();
  	Dimension oldSize = modelBounds != null ? modelBounds.getSize() : widget.getPreferredSize();
  	FormDataInfo layoutData = (FormDataInfo) getLayoutData((ControlInfo) widget);
  	Dimension preferredSize = widget.getPreferredSize();
  	if (PlacementUtils.isHorizontalSide(side)) {
  		int newWidth = oldSize.width + resizeDelta;
  		layoutData.setWidth(preferredSize.width != newWidth ? newWidth : SWT.DEFAULT);
  	} else {
  		int newHeight = oldSize.height + resizeDelta;
  		layoutData.setHeight(preferredSize.height != newHeight ? newHeight : SWT.DEFAULT);
  	}
  }*/
	@Override
	public void setExplicitSize(IAbstractComponentInfo widget,
			int attachedSide,
			int draggingSide,
			int resizeDelta) throws Exception {
		Rectangle modelBounds = widget.getModelBounds();
		Dimension oldSize = modelBounds != null ? modelBounds.getSize() : widget.getPreferredSize();
		//
		int oldLinearSize = PlacementUtils.getSideSize(oldSize, draggingSide);
		setExplicitSize(widget, attachedSide, draggingSide, resizeDelta, oldLinearSize);
		IFormDataInfo<C> layoutData = (IFormDataInfo<C>) m_layout.getLayoutData2((C) widget);
		if (PlacementUtils.isHorizontalSide(attachedSide)) {
			layoutData.setWidth(SWT.DEFAULT);
		} else {
			layoutData.setHeight(SWT.DEFAULT);
		}
	}

	private void setExplicitSize(IAbstractComponentInfo widget,
			int attachedSide,
			int draggingSide,
			int resizeDelta,
			int oldSize) throws Exception {
		IFormAttachmentInfo<C> attached = getAttachment(widget, attachedSide);
		IFormAttachmentInfo<C> dragging = getAttachment(widget, draggingSide);
		if (attachedSide == draggingSide) {
			// lock "free" side
			{
				IFormAttachmentInfo<C> lock =
						getAttachment(widget, PlacementUtils.getOppositeSide(attachedSide));
				setNewSize(attached, attachedSide, lock, oldSize);
			}
		} else {
			int newWidth = oldSize + resizeDelta;
			setNewSize(attached, attachedSide, dragging, newWidth);
		}
	}

	// XXX
	private static <C extends IControlInfo> void setNewSize(IFormAttachmentInfo<C> attached,
			int attachedSide,
			IFormAttachmentInfo<C> dragging,
			int newSize) throws Exception {
		C attachedControl = attached.getControl();
		dragging.setControl(attachedControl);
		if (attachedControl != null) {
			dragging.setAlignment(attached.getAlignment());
		} else {
			dragging.setDenominator(attached.getDenominator());
			dragging.setNumerator(attached.getNumerator());
		}
		// set offset
		int offset;
		if (PlacementUtils.isLeadingSide(attachedSide)) {
			offset = attached.getOffset() + newSize;
		} else {
			offset = attached.getOffset() - newSize;
		}
		dragging.setOffset(offset);
		// write
		dragging.write();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget, int side)
			throws Exception {
		if (m_layout.isManagedObject(widget) && isAttached(widget, side)) {
			IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
			C control = attachment.getControl();
			if (control != null) {
				int alignment = attachment.getAlignment();
				if (alignment == SWT.DEFAULT) {
					alignment = PlacementUtils.getOppositeSide(side);
				} else {
					alignment = FormLayoutUtils.convertSwtAlignment(alignment);
				}
				return new ComponentAttachmentInfo(widget, control, alignment);
			}
		}
		return null;
	}

	public static ImageDescriptor getImageDescriptor(String imageName) {
		return Activator.getImageDescriptor("info/layout/FormLayout/" + imageName);
	}

	@Override
	public IAbstractComponentInfo getAttachedToWidget(IAbstractComponentInfo widget, int side)
			throws Exception {
		if (!isAttached(widget, side)) {
			return null;
		}
		IFormAttachmentInfo<C> attachment = getAttachment(widget, side);
		return attachment.getControl();
	}

	@Override
	public boolean isAttached(IAbstractComponentInfo widget, int side) throws Exception {
		return m_layout.isManagedObject(widget) && !getAttachment(widget, side).isVirtual();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout-defined actions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performAction(int actionId) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move in tree
	//
	////////////////////////////////////////////////////////////////////////////
	public void command_CREATE2(C component, C nextComponent) throws Exception {
		m_layout.commandCreate(component, nextComponent);
	}

	public void command_MOVE2(C component, C nextComponent) throws Exception {
		m_layout.commandMove(component, nextComponent);
		adjustMovedComponentSides(component);
		adjustAnchoredToMovedComponent(component);
	}

	private void adjustMovedComponentSides(C component) throws Exception {
		adjustMovedComponentSide(component, PositionConstants.TOP);
		adjustMovedComponentSide(component, PositionConstants.LEFT);
		adjustMovedComponentSide(component, PositionConstants.BOTTOM);
		adjustMovedComponentSide(component, PositionConstants.RIGHT);
	}

	private void adjustMovedComponentSide(C component, int side) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(component, side);
		if (!attachment.isVirtual()) {
			attachment.write();
		}
	}

	private void adjustAnchoredToMovedComponent(C component) throws Exception {
		for (C sibling : m_layout.getControls()) {
			adjustAnchoredToMovedComponentSides(sibling, component);
		}
	}

	private void adjustAnchoredToMovedComponentSides(C component, C target) throws Exception {
		adjustAnchoredToMovedComponentSide(component, target, PositionConstants.TOP);
		adjustAnchoredToMovedComponentSide(component, target, PositionConstants.LEFT);
		adjustAnchoredToMovedComponentSide(component, target, PositionConstants.BOTTOM);
		adjustAnchoredToMovedComponentSide(component, target, PositionConstants.RIGHT);
	}

	private void adjustAnchoredToMovedComponentSide(C component, C anchor, int side) throws Exception {
		IFormAttachmentInfo<C> attachment = getAttachment(component, side);
		if (attachment.getControl() == anchor) {
			attachment.write();
		}
	}
}
