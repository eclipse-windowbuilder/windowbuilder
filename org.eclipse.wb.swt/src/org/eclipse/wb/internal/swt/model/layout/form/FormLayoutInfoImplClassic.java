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
package org.eclipse.wb.internal.swt.model.layout.form;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantListener;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormUtils;
import org.eclipse.wb.internal.swt.model.layout.form.actions.AnchorActionsClassic;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.TabFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 'Old-school' implementation of the FromLayout.
 * 
 * @author mitin_aa
 */
public class FormLayoutInfoImplClassic<C extends IControlInfo> extends FormLayoutInfoImpl<C> {
  private final IFormLayoutInfo<C> layout;
  private final AnchorActionsClassic<C> anchorActions;
  private final AbstractAlignmentActionsSupport<C> alignmentActions;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormLayoutInfoImplClassic(IFormLayoutInfo<C> layout_) {
    this.layout = layout_;
    this.anchorActions = new AnchorActionsClassic<C>(this);
    this.alignmentActions = new AlignmentsSupport();
    this.layout.getUnderlyingModel().addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        alignmentActions.addAlignmentActions(objects, actions);
      }
    });
    this.layout.getUnderlyingModel().addBroadcastListener(new LayoutAssistantListener() {
      @Override
      public void createAssistantPages(List<ObjectInfo> objects,
          TabFolder folder,
          List<ILayoutAssistantPage> pages) throws Exception {
        if (!objects.isEmpty()) {
          for (ObjectInfo object : objects) {
            if (object.getParent() != layout.getComposite()) {
              return;
            }
          }
          ArrayList<Object> actions = Lists.newArrayList();
          alignmentActions.addAlignmentActions(objects, actions);
          LayoutAssistantPageClassic<C> page =
              new LayoutAssistantPageClassic<C>(layout, folder, objects, actions);
          TabFactory.item(folder).text("FormLayout").control(page);
          pages.add(page);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the appropriate form attachment instance or <code>null</code> if it is virtual.
   */
  public IFormAttachmentInfo<C> getAttachment(C control, int side) throws Exception {
    if (layout.isManagedObject(control)) {
      IFormAttachmentInfo<C> attachment = getAttachment0(control, side);
      return attachment.isVirtual() ? null : attachment;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private IFormAttachmentInfo<C> getAttachment0(C child, int side) throws Exception {
    IFormDataInfo<C> formData = (IFormDataInfo<C>) layout.getLayoutData2(child);
    return formData.getAttachment(side);
  }

  public boolean isLeft(IFormAttachmentInfo<C> attachment) {
    return attachment.getSide().getEngineSide() == IPositionConstants.LEFT;
  }

  public boolean isRight(IFormAttachmentInfo<C> attachment) {
    return attachment.getSide().getEngineSide() == IPositionConstants.RIGHT;
  }

  public boolean isTop(IFormAttachmentInfo<C> attachment) {
    return attachment.getSide().getEngineSide() == IPositionConstants.TOP;
  }

  public boolean isBottom(IFormAttachmentInfo<C> attachment) {
    return attachment.getSide().getEngineSide() == IPositionConstants.BOTTOM;
  }

  public boolean isParentAttachment(IFormAttachmentInfo<C> attachment) {
    return attachment.getControl() == null;
  }

  public boolean isControlAttachment(IFormAttachmentInfo<C> attachment) {
    return !attachment.isVirtual() && attachment.getControl() != null;
  }

  public boolean shouldShowConstraintLine(IFormAttachmentInfo<C> attachment) {
    IFormAttachmentInfo<C> opposite;
    try {
      opposite = getOpposite(attachment);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
    if (opposite.isVirtual()) {
      return true;
    }
    int align = attachment.getAlignment();
    int oppositeSideAlign = opposite.getAlignment();
    if (align != oppositeSideAlign) {
      return true;
    }
    int offset = attachment.getOffset();
    int oppositeOffset = opposite.getOffset();
    return Math.abs(offset) < Math.abs(oppositeOffset);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  public void moveToPercentOffset(C child, int side, int percent, int offset) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    attachment.setControl(null);
    attachment.setNumerator(percent);
    attachment.setDenominator(100);
    attachment.setOffset(offset);
    attachment.write();
    rebindOpposite(attachment);
  }

  public void moveToOffset(C child, int side, int parentSize, int offset) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    moveToOffset(attachment, parentSize, offset);
  }

  private void moveToOffset(IFormAttachmentInfo<C> attachment, int parentSize, int offset)
      throws Exception {
    bindToParent(attachment, parentSize, offset);
    rebindOpposite(attachment);
  }

  public void moveToMargin(C child, int side, int direction, int marginValue) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    attachment.setControl(null);
    attachment.setDenominator(100);
    if (!PlacementUtils.isTrailingSide(direction)) {
      attachment.setNumerator(0);
      attachment.setOffset(marginValue);
    } else {
      attachment.setNumerator(100);
      attachment.setOffset(-marginValue);
    }
    attachment.write();
    rebindOpposite(attachment);
  }

  public void moveToControl(C child, int side, C target, int alignment, int offset)
      throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    attachment.setControl(target);
    attachment.setOffset(offset);
    attachment.setAlignment(FormLayoutUtils.convertGefSide(alignment));
    attachment.write();
    //
    int thisSide = attachment.getSide().getEngineSide();
    IFormAttachmentInfo<C> opposite = getOpposite(attachment);
    if (!opposite.isVirtual()) {
      opposite.setControl(target);
      opposite.setAlignment(FormLayoutUtils.convertGefSide(alignment));
      org.eclipse.wb.draw2d.geometry.Rectangle controlBounds = child.getModelBounds();
      int oppositeOffset = attachment.getOffset();
      if (controlBounds != null) {
        int k = PlacementUtils.isTrailingSide(thisSide) ? -1 : 1;
        if (PlacementUtils.isHorizontalSide(opposite.getSide().getEngineSide())) {
          oppositeOffset += k * controlBounds.width;
        } else {
          oppositeOffset += k * controlBounds.height;
        }
      }
      opposite.setOffset(oppositeOffset);
      opposite.write();
    }
  }

  /**
   * Binds an attachment to parent.
   */
  private void bindToParent(IFormAttachmentInfo<C> attachment, int parentSize, int offset)
      throws Exception {
    int numerator = 0;
    int denominator = 100;
    int oldNumerator = attachment.getNumerator();
    if (oldNumerator == 100) {
      numerator = 100;
      offset = parentSize - offset;
      offset = -offset;
    }
    attachment.setControl(null);
    attachment.setNumerator(numerator);
    attachment.setDenominator(denominator);
    attachment.setOffset(offset);
    attachment.write();
  }

  /**
   * If the opposite side has an attachment, it would be rebound to parent
   */
  private void rebindOpposite(IFormAttachmentInfo<C> attachment) throws Exception {
    int thisSide = attachment.getSide().getEngineSide();
    IFormAttachmentInfo<C> opposite = getOpposite(attachment);
    if (!opposite.isVirtual()) {
      opposite.setControl(null);
      org.eclipse.wb.draw2d.geometry.Rectangle controlBounds =
          getThisControl(attachment).getModelBounds();
      opposite.setNumerator(attachment.getNumerator());
      opposite.setDenominator(attachment.getDenominator());
      if (controlBounds != null) {
        int sign = PlacementUtils.isTrailingSide(thisSide) ? -1 : 1;
        if (PlacementUtils.isHorizontalSide(opposite.getSide().getEngineSide())) {
          opposite.setOffset(attachment.getOffset() + sign * controlBounds.width);
        } else {
          opposite.setOffset(attachment.getOffset() + sign * controlBounds.height);
        }
      }
      opposite.write();
    }
  }

  @SuppressWarnings("unchecked")
  private C getThisControl(IFormAttachmentInfo<C> attachment) {
    return (C) attachment.getParent().getParent();
  }

  @SuppressWarnings("unchecked")
  private IFormAttachmentInfo<C> getOpposite(IFormAttachmentInfo<C> attachment) throws Exception {
    IFormDataInfo<C> formData = (IFormDataInfo<C>) attachment.getParent();
    int oppositeSide = attachment.getSide().getOppositeSide().getEngineSide();
    return formData.getAttachment(oppositeSide);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible controls to attach to
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<C> getAlignControlInfos(C sourceControl, int sourceSide) throws Exception {
    List<C> list = Lists.newArrayList();
    for (C info : layout.getControls()) {
      if (isRelative(info, sourceControl, sourceSide)) {
        continue;
      }
      list.add(info);
    }
    return list;
  }

  /**
   * Return <code>true</code>, if <code>control</code> is located relative to
   * <code>refControl</code>.
   */
  private boolean isRelative(C checkingControl, C refControl, int side) throws Exception {
    Set<C> referenced = Sets.newHashSet();
    collectReferencedControls(checkingControl, side, referenced);
    return referenced.contains(refControl);
  }

  /**
   * Collect all children relative to which <code>control</code> is located.
   */
  private void collectReferencedControls(C control, int side, Set<C> referenced) throws Exception {
    if (referenced.contains(control)) {
      return;
    }
    referenced.add(control);
    if (PlacementUtils.isHorizontalSide(side)) {
      collectReferencedControls0(control, IPositionConstants.LEFT, referenced);
      collectReferencedControls0(control, IPositionConstants.RIGHT, referenced);
    } else {
      collectReferencedControls0(control, IPositionConstants.TOP, referenced);
      collectReferencedControls0(control, IPositionConstants.BOTTOM, referenced);
    }
  }

  private void collectReferencedControls0(C control, int side, Set<C> referenced) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(control, side);
    if (isControlAttachment(attachment)) {
      collectReferencedControls(attachment.getControl(), side, referenced);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createToOffset(C child, int side, int parentSize, int offset) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    if (PlacementUtils.isTrailingSide(side)) {
      resizeToOffset(attachment, parentSize, offset);
    } else {
      moveToOffset(attachment, parentSize, offset);
    }
  }

  public void createToControl(C child, int side, C targetControl, int targetSide, int offset)
      throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    if (PlacementUtils.isTrailingSide(side)) {
      resizeToControl(attachment, targetControl, targetSide, offset);
    } else {
      moveToControl(child, side, targetControl, targetSide, offset);
    }
  }

  public void createToPercentOffset(C child, int side, int percent, int offset) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    if (PlacementUtils.isTrailingSide(side)) {
      resizeToPercentOffset(attachment, percent, offset);
    } else {
      moveToPercentOffset(child, side, percent, offset);
    }
  }

  public void createToMargin(C child, int side, int direction, int marginValue) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(child, side);
    if (PlacementUtils.isTrailingSide(side)) {
      resizeToMargin(attachment, direction, marginValue);
    } else {
      moveToMargin(child, side, direction, marginValue);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  public void resizeToOffset(IFormAttachmentInfo<C> attachment, int parentDimension, int offset)
      throws Exception {
    bindOppositeToParent(attachment);
    bindToParent(attachment, parentDimension, offset);
  }

  private void bindOppositeToParent(IFormAttachmentInfo<C> attachment) throws Exception {
    IFormAttachmentInfo<C> opposite = getOpposite(attachment);
    if (opposite.isVirtual()) {
      Rectangle parentBounds = getThisControl(attachment).getModelBounds();
      opposite.setDenominator(100);
      if (isLeft(attachment)) {
        opposite.setOffset(parentBounds.x
            + parentBounds.width
            - FormUtils.getLayoutMarginLeft(layout));
      } else if (isTop(attachment)) {
        opposite.setOffset(parentBounds.y
            + parentBounds.height
            - FormUtils.getLayoutMarginTop(layout));
      } else if (isRight(attachment)) {
        opposite.setOffset(parentBounds.x - FormUtils.getLayoutMarginLeft(layout));
      } else if (isBottom(attachment)) {
        opposite.setOffset(parentBounds.y - FormUtils.getLayoutMarginTop(layout));
      }
      opposite.write();
    }
  }

  public void resizeToPercentOffset(IFormAttachmentInfo<C> attachment, int percent, int offset)
      throws Exception {
    bindOppositeToParent(attachment);
    attachment.setDenominator(100);
    attachment.setControl(null);
    attachment.setNumerator(percent);
    attachment.setOffset(offset);
    attachment.write();
  }

  public void resizeToMargin(IFormAttachmentInfo<C> attachment, int direction, int marginValue)
      throws Exception {
    bindOppositeToParent(attachment);
    attachment.setDenominator(100);
    attachment.setControl(null);
    if (direction == IPositionConstants.LEFT || direction == IPositionConstants.TOP) {
      attachment.setNumerator(0);
      attachment.setOffset(marginValue);
    } else {
      attachment.setNumerator(100);
      attachment.setDenominator(100);
      attachment.setOffset(-marginValue);
    }
    attachment.write();
  }

  public void resizeToControl(IFormAttachmentInfo<C> attachment,
      C targetControl,
      int targetSide,
      int offset) throws Exception {
    bindOppositeToParent(attachment);
    attachment.setControl(targetControl);
    attachment.setOffset(offset);
    attachment.setAlignment(FormLayoutUtils.convertGefSide(targetSide));
    attachment.write();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchor changing actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public AnchorActionsClassic<C> getAnchorActions() {
    return anchorActions;
  }

  public void anchor_delete(C control, int controlSide) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(control, controlSide);
    attachment.delete();
  }

  public void anchor_bindToParent(C control, int controlSide, int parentSide) throws Exception {
    layout.anchorToParent(control, controlSide, parentSide);
  }

  public void anchor_bindToParentAsPercent(C control, int controlSide) throws Exception {
    layout.anchorToParentAsPercent(control, controlSide);
  }

  public void anchor_bindToControl(C control, int controlSide, int targetSide) throws Exception {
    IFormAttachmentInfo<C> attachment = getAttachment0(control, controlSide);
    WidgetSelectDialog<C> dialog =
        new WidgetSelectDialog<C>(DesignerPlugin.getShell(), getAlignControlInfos(
            control,
            controlSide), "Choose control", "Control list:", "Control");
    if (dialog.open() != Window.OK) {
      return;
    }
    C targetControl = dialog.getSelectedWidget();
    Transposer t = new Transposer(!PlacementUtils.isHorizontalSide(controlSide));
    org.eclipse.wb.draw2d.geometry.Rectangle controlBounds = t.t(control.getModelBounds());
    org.eclipse.wb.draw2d.geometry.Rectangle targetBounds =
        t.t(targetControl.getModelBounds());
    int offset = 0;
    if (!PlacementUtils.isTrailingSide(controlSide)) {
      if (PlacementUtils.isTrailingSide(targetSide)) {
        offset = -(targetBounds.right() - controlBounds.x);
      } else {
        offset = targetBounds.x;
      }
    } else {
      if (PlacementUtils.isTrailingSide(targetSide)) {
        offset = -(targetBounds.right() - controlBounds.right());
      } else {
        offset = controlBounds.right();
      }
    }
    attachment.setOffset(offset);
    attachment.setControl(targetControl);
    attachment.setAlignment(FormLayoutUtils.convertGefSide(targetSide));
    attachment.write();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Settings
  //
  ////////////////////////////////////////////////////////////////////////////
  private IPreferenceStore getPreferenceStore() {
    return GlobalState.getToolkit().getPreferences();
  }

  private boolean shouldKeepAttachmentsStyle() {
    return getPreferenceStore().getBoolean(IPreferenceConstants.PREF_KEEP_ATTACHMENTS_STYLE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class AlignmentsSupport extends AbstractAlignmentActionsSupport<C> {
    @Override
    protected boolean isComponentInfo(ObjectInfo object) {
      return layout.isManagedObject(object);
    }

    @Override
    protected IAbstractComponentInfo getLayoutContainer() {
      return layout.getComposite();
    }

    @Override
    protected void commandAlignLeft() throws Exception {
      C target = m_components.get(0);
      int x = target.getModelBounds().x - FormUtils.getLayoutMarginLeft(layout);
      int clientWidth = layout.getContainerSize().width;
      for (C control : m_components) {
        if (target != control) {
          int width = control.getModelBounds().width;
          copyAttachmentPosition(clientWidth, getAttachment0(control, IPositionConstants.LEFT), x
              + width, getAttachment0(target, IPositionConstants.LEFT), x);
        }
      }
    }

    @Override
    protected void commandAlignRight() throws Exception {
      C target = m_components.get(0);
      int r = target.getModelBounds().right() - FormUtils.getLayoutMarginLeft(layout);
      int clientWidth = layout.getContainerSize().width;
      for (C control : m_components) {
        if (target != control) {
          int width = control.getModelBounds().width;
          copyAttachmentPosition(clientWidth, getAttachment0(control, IPositionConstants.RIGHT), r
              - width, getAttachment0(target, IPositionConstants.RIGHT), r);
        }
      }
    }

    @Override
    protected void commandAlignTop() throws Exception {
      C target = m_components.get(0);
      int y = target.getModelBounds().y - FormUtils.getLayoutMarginTop(layout);
      int clientHeight = layout.getContainerSize().height;
      for (C control : m_components) {
        if (target != control) {
          int height = control.getModelBounds().height;
          copyAttachmentPosition(clientHeight, getAttachment0(control, IPositionConstants.TOP), y
              + height, getAttachment0(target, IPositionConstants.TOP), y);
        }
      }
    }

    @Override
    protected void commandAlignBottom() throws Exception {
      C target = m_components.get(0);
      int b = target.getModelBounds().bottom() - FormUtils.getLayoutMarginTop(layout);
      int clientHeight = layout.getContainerSize().height;
      for (C control : m_components) {
        if (target != control) {
          int height = control.getModelBounds().height;
          copyAttachmentPosition(
              clientHeight,
              getAttachment0(control, IPositionConstants.BOTTOM),
              b - height,
              getAttachment0(target, IPositionConstants.BOTTOM),
              b);
        }
      }
    }

    @Override
    protected void commandAlignCenterVertically() throws Exception {
      C target = m_components.get(0);
      Rectangle bounds = target.getModelBounds();
      int y = bounds.y - FormUtils.getLayoutMarginTop(layout);
      int c = y + bounds.height / 2;
      int clientHeight = layout.getContainerSize().height;
      for (C control : m_components) {
        if (target != control) {
          int height = control.getModelBounds().height;
          setAttachmentPosition(getAttachment0(control, IPositionConstants.TOP), clientHeight, c
              - height
              / 2);
          setAttachmentPosition(getAttachment0(control, IPositionConstants.BOTTOM), clientHeight, c
              + height
              / 2);
        }
      }
    }

    @Override
    protected void commandAlignCenterHorizontally() throws Exception {
      C target = m_components.get(0);
      Rectangle bounds = target.getModelBounds();
      int x = bounds.x - FormUtils.getLayoutMarginLeft(layout);
      int c = x + bounds.width / 2;
      int clientWidth = layout.getContainerSize().width;
      for (C control : m_components) {
        if (target != control) {
          int width = control.getModelBounds().width;
          setAttachmentPosition(getAttachment0(control, IPositionConstants.LEFT), clientWidth, c
              - width
              / 2);
          setAttachmentPosition(getAttachment0(control, IPositionConstants.RIGHT), clientWidth, c
              + width
              / 2);
        }
      }
    }

    @Override
    protected void commandCenterVertically() throws Exception {
      int clientHeight = layout.getContainerSize().height;
      for (C control : m_components) {
        int height = control.getModelBounds().height;
        int leftPosition = clientHeight / 2 - height / 2;
        int rightPosition = clientHeight / 2 + height / 2;
        setAttachmentPosition(
            getAttachment0(control, IPositionConstants.TOP),
            clientHeight,
            leftPosition);
        setAttachmentPosition(
            getAttachment0(control, IPositionConstants.BOTTOM),
            clientHeight,
            rightPosition);
      }
    }

    @Override
    protected void commandCenterHorizontally() throws Exception {
      int clientWidth = layout.getContainerSize().width;
      for (C control : m_components) {
        int width = control.getModelBounds().width;
        int leftPosition = clientWidth / 2 - width / 2;
        int rightPosition = clientWidth / 2 + width / 2;
        setAttachmentPosition(
            getAttachment0(control, IPositionConstants.LEFT),
            clientWidth,
            leftPosition);
        setAttachmentPosition(
            getAttachment0(control, IPositionConstants.RIGHT),
            clientWidth,
            rightPosition);
      }
    }

    @Override
    protected void commandReplicateWidth() throws Exception {
      C target = m_components.get(0);
      int width = target.getModelBounds().width;
      int clientWidth = layout.getContainerSize().width;
      for (C control : m_components) {
        if (target != control) {
          int x = control.getModelBounds().x - FormUtils.getLayoutMarginLeft(layout);
          setAttachmentPosition(getAttachment0(control, IPositionConstants.LEFT), clientWidth, x);
          setAttachmentPosition(getAttachment0(control, IPositionConstants.RIGHT), clientWidth, x
              + width);
        }
      }
    }

    @Override
    protected void commandReplicateHeight() throws Exception {
      C target = m_components.get(0);
      int height = target.getModelBounds().height;
      int clientHeight = layout.getContainerSize().height;
      for (C control : m_components) {
        if (target != control) {
          int y = control.getModelBounds().y - FormUtils.getLayoutMarginTop(layout);
          setAttachmentPosition(getAttachment0(control, IPositionConstants.TOP), clientHeight, y);
          setAttachmentPosition(getAttachment0(control, IPositionConstants.BOTTOM), clientHeight, y
              + height);
        }
      }
    }

    @Override
    protected void commandDistributeSpaceVertically() throws Exception {
      distributeSpace(false);
    }

    @Override
    protected void commandDistributeSpaceHorizontally() throws Exception {
      distributeSpace(true);
    }

    private void distributeSpace(boolean isHorizontal) throws Exception {
      int leadingSide = isHorizontal ? IPositionConstants.LEFT : IPositionConstants.TOP;
      int trailingSide = isHorizontal ? IPositionConstants.RIGHT : IPositionConstants.BOTTOM;
      final Transposer t = new Transposer(!isHorizontal);
      int margin =
          isHorizontal
              ? FormUtils.getLayoutMarginLeft(layout)
              : FormUtils.getLayoutMarginTop(layout);
      List<C> controlList = Lists.newArrayList(m_components);
      int clientSize = t.t(layout.getContainerSize()).width;
      boolean alternative = DesignerPlugin.isCtrlPressed() && m_components.size() > 2;
      // calculate sum size of the controls
      int controlsSize = 0;
      for (IAbstractComponentInfo control : m_components) {
        controlsSize += t.t(control.getModelBounds()).width;
      }
      // sort controls by their leading positions
      Collections.sort(controlList, new Comparator<IAbstractComponentInfo>() {
        public int compare(IAbstractComponentInfo o1, IAbstractComponentInfo o2) {
          return t.t(o1.getModelBounds()).x - t.t(o2.getModelBounds()).x;
        }
      });
      // distribute controls between leading-most and trailing-most controls (if Ctrl pressed),
      // or in parents client area
      int space;
      int x;
      if (alternative) {
        // calculate space and start location (x)
        C leadingControl = controlList.get(0);
        C trailingControl = controlList.get(controlList.size() - 1);
        Rectangle leadingBounds = t.t(leadingControl.getModelBounds());
        Rectangle trailingBounds = t.t(trailingControl.getModelBounds());
        int totalSize = trailingBounds.right() - leadingBounds.x;
        space = (totalSize - controlsSize) / (controlList.size() - 1);
        x = leadingBounds.x + margin;
      } else {
        // calculate space and start location (x)
        space = (clientSize - controlsSize) / (controlList.size() + 1);
        x = space;
      }
      // change positions for controls from leading to trailing
      for (int i = 0; i < controlList.size(); i++) {
        C control = controlList.get(i);
        int width = t.t(control.getModelBounds()).width;
        if (!(alternative && (i == 0 || i == controlList.size() - 1))) {
          setAttachmentPosition(getAttachment0(control, leadingSide), clientSize, x);
          setAttachmentPosition(getAttachment0(control, trailingSide), clientSize, x + width);
        }
        x += width;
        x += space;
      }
    }

    private void copyAttachmentPosition(int parentSize,
        IFormAttachmentInfo<C> target,
        int oppositeTargetValue,
        IFormAttachmentInfo<C> source,
        int sourceValue) throws Exception {
      IFormAttachmentInfo<C> targetOpposite = getOpposite(target);
      if (shouldKeepAttachmentsStyle() || source.isVirtual()) {
        setAttachmentPosition(target, parentSize, sourceValue);
        setAttachmentPosition(targetOpposite, parentSize, oppositeTargetValue);
        return;
      } else {
        boolean virtualTarget = target.isVirtual();
        copy(source, target);
        if (virtualTarget) {
          targetOpposite.delete();
        } else {
          if (!targetOpposite.isVirtual()) {
            setAttachmentPosition(targetOpposite, parentSize, oppositeTargetValue);
          }
        }
      }
      target.write();
    }

    private void copy(IFormAttachmentInfo<C> source, IFormAttachmentInfo<C> target) {
      if (source.isVirtual()) {
        // do nothing
      } else if (isControlAttachment(source)) {
        target.setControl(source.getControl());
        target.setAlignment(source.getAlignment());
        target.setOffset(source.getOffset());
      } else {
        target.setNumerator(source.getNumerator());
        target.setDenominator(source.getDenominator());
        target.setOffset(source.getOffset());
      }
    }

    private void setAttachmentPosition(IFormAttachmentInfo<C> attachment, int parentSize, int value)
        throws Exception {
      if (attachment.isParentLeading()) {
        attachment.setOffset(value);
      } else if (attachment.isParentTrailing()) {
        attachment.setOffset(value - parentSize);
      } else if (attachment.isPercentaged()) {
        int percent = (int) ((double) value * 100 / parentSize);
        attachment.setNumerator(percent);
      } else {
        attachment.setControl(null);
        attachment.setNumerator(0);
        attachment.setOffset(value);
      }
      attachment.write();
    }
  }
}
