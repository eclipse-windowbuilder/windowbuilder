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
package org.eclipse.wb.internal.swing.model.layout.spring;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.swt.graphics.Image;

import java.util.List;

import javax.swing.SpringLayout;

/**
 * Model for {@link SpringLayout}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SpringLayoutInfo extends LayoutInfo implements IAbsoluteLayoutCommands {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SpringLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // add listeners
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isManagedObject(javaInfo)) {
          properties.add(getConstraintsProperty((ComponentInfo) javaInfo));
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_finish() throws Exception {
    super.refresh_finish();
    // update constraints text
    updateConstraintsProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout manipulation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void detach(IAbstractComponentInfo widget, int side) throws Exception {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    attachment.delete();
  }

  public void attachAbsolute(IAbstractComponentInfo widget, int side, int distance)
      throws Exception {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    attachment.setAnchorComponent(getContainer());
    attachment.setAnchorSide(side);
    attachment.setOffset(getSideOffset(side, distance));
    attachment.write();
  }

  public void adjustAttachmentOffset(IAbstractComponentInfo widget, int side, int delta)
      throws Exception {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    int oldOffset = attachment.getOffset();
    int newOffset = oldOffset + delta;
    attachment.setOffset(newOffset);
    attachment.write();
  }

  public void attachWidgetSequientially(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachToWidget,
      int side,
      int distance) throws Exception {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    attachment.setAnchorComponent((ComponentInfo) attachToWidget);
    attachment.setAnchorSide(PlacementUtils.getOppositeSide(side));
    attachment.setOffset(getSideOffset(side, distance));
    attachment.write();
  }

  public void attachWidgetParallelly(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachToWidget,
      int side,
      int distance) throws Exception {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    attachment.setAnchorComponent((ComponentInfo) attachToWidget);
    attachment.setAnchorSide(side);
    attachment.setOffset(getSideOffset(side, distance));
    attachment.write();
  }

  public void attachWidgetBaseline(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachedToWidget) throws Exception {
    SpringAttachmentInfo attachment = getAttachmentTop((ComponentInfo) widget.getUnderlyingModel());
    int offset = 0;
    int baseline = widget.getBaseline();
    int targetBaseline = attachedToWidget.getBaseline();
    if (baseline == -1 || targetBaseline == -1) {
      // move to center
      Rectangle sourceBounds = widget.getModelBounds();
      Rectangle targetBounds = attachedToWidget.getModelBounds();
      offset = targetBounds.bottom() / 2 - sourceBounds.height / 2;
    } else {
      offset = targetBaseline - baseline;
    }
    attachment.setAnchorComponent((ComponentInfo) attachedToWidget);
    attachment.setAnchorSide(IPositionConstants.TOP);
    attachment.setOffset(offset);
    attachment.write();
  }

  private static int getSideOffset(int side, int distance) {
    if (PlacementUtils.isLeadingSide(side)) {
      return distance;
    } else {
      return -distance;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setExplicitSize(IAbstractComponentInfo widget,
      int attachedSide,
      int draggingSide,
      int resizeDelta) throws Exception {
    Rectangle modelBounds = widget.getModelBounds();
    Dimension oldSize = modelBounds != null ? modelBounds.getSize() : widget.getPreferredSize();
    //
    int oldLinearSize = PlacementUtils.getSideSize(oldSize, draggingSide);
    setExplicitSize(widget, attachedSide, draggingSide, resizeDelta, oldLinearSize);
  }

  private void setExplicitSize(IAbstractComponentInfo widget,
      int attachedSide,
      int draggingSide,
      int resizeDelta,
      int oldSize) throws Exception {
    SpringAttachmentInfo attached = getAttachment(widget, attachedSide);
    SpringAttachmentInfo dragging = getAttachment(widget, draggingSide);
    if (attachedSide == draggingSide) {
      // lock "free" side
      {
        SpringAttachmentInfo lock =
            getAttachment(widget, PlacementUtils.getOppositeSide(attachedSide));
        setNewSize(attached, lock, oldSize);
      }
      // move "attached"
      /*{
      	int offsetDelta = PlacementUtils.isLeadingSide(attachedSide) ? -resizeDelta : resizeDelta;
      	adjustAttachmentOffset(widget, attachedSide, offsetDelta);
      }*/
    } else {
      int newWidth = oldSize + resizeDelta;
      setNewSize(attached, dragging, newWidth);
    }
  }

  private static void setNewSize(SpringAttachmentInfo attached,
      SpringAttachmentInfo dragging,
      int newSize) throws Exception {
    dragging.setAnchorComponent(attached.getAnchorComponent());
    dragging.setAnchorSide(attached.getAnchorSide());
    // set offset
    int offset;
    if (PlacementUtils.isLeadingSide(attached.getSide())) {
      offset = attached.getOffset() + newSize;
    } else {
      offset = attached.getOffset() - newSize;
    }
    dragging.setOffset(offset);
    dragging.write();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attachments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link SpringAttachmentInfo} for side of component.
   * 
   * @param side
   *          one of the {@link IPositionConstants#LEFT}, {@link IPositionConstants#TOP},
   *          {@link IPositionConstants#RIGHT}, {@link IPositionConstants#BOTTOM}.
   */
  public final SpringAttachmentInfo getAttachment(IAbstractComponentInfo component, int side) {
    return SpringAttachmentInfo.get(this, (ComponentInfo) component.getUnderlyingModel(), side);
  }

  public final SpringAttachmentInfo getAttachmentLeft(ComponentInfo component) {
    return getAttachment(component, IPositionConstants.LEFT);
  }

  public final SpringAttachmentInfo getAttachmentRight(ComponentInfo component) {
    return getAttachment(component, IPositionConstants.RIGHT);
  }

  public final SpringAttachmentInfo getAttachmentTop(ComponentInfo component) {
    return getAttachment(component, IPositionConstants.TOP);
  }

  public final SpringAttachmentInfo getAttachmentBottom(ComponentInfo component) {
    return getAttachment(component, IPositionConstants.BOTTOM);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget, int side)
      throws Exception {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    if (attachment != null && !attachment.isVirtual()) {
      ComponentInfo anchorComponent = attachment.getAnchorComponent();
      if (anchorComponent != null && anchorComponent != getContainer()) {
        int anchorSide = attachment.getAnchorSide();
        return new ComponentAttachmentInfo(widget, anchorComponent, anchorSide);
      }
    }
    return null;
  }

  public ComponentInfo getAttachedToWidget(IAbstractComponentInfo widget, int side) {
    SpringAttachmentInfo attachment = getAttachment(widget, side);
    ComponentInfo anchorComponent = attachment.getAnchorComponent();
    return anchorComponent != getContainer() ? anchorComponent : null;
  }

  public boolean isAttached(IAbstractComponentInfo widget, int side) throws Exception {
    return !getAttachment(widget, side).isVirtual();
  }

  public static Image getImage(String imageName) {
    return Activator.getImage("info/layout/springLayout/" + imageName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout-defined actions 
  //
  ////////////////////////////////////////////////////////////////////////////
  public void performAction(int actionId) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void command_CREATE(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    add(component, null, nextComponent);
  }

  public final void command_ADD(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    move(component, null, nextComponent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_MOVE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    initializeAttachments(component);
    move(component, null, nextComponent);
    adjustMovedComponentSides(component);
    adjustAnchoredToMovedComponent(component);
  }

  /**
   * Ensures that all {@link SpringAttachmentInfo} of {@link ComponentInfo} are initialized, mainly
   * for preparing anchors. After moving component its anchor references may become invalid
   * temporary.
   */
  private void initializeAttachments(ComponentInfo component) {
    getAttachmentLeft(component);
    getAttachmentRight(component);
    getAttachmentTop(component);
    getAttachmentBottom(component);
  }

  private void adjustMovedComponentSides(ComponentInfo component) throws Exception {
    adjustMovedComponentSide(component, IPositionConstants.TOP);
    adjustMovedComponentSide(component, IPositionConstants.LEFT);
    adjustMovedComponentSide(component, IPositionConstants.BOTTOM);
    adjustMovedComponentSide(component, IPositionConstants.RIGHT);
  }

  private void adjustMovedComponentSide(ComponentInfo component, int side) throws Exception {
    SpringAttachmentInfo attachment = getAttachment(component, side);
    if (!attachment.isVirtual()) {
      attachment.adjustAfterComponentMove();
    }
  }

  private void adjustAnchoredToMovedComponent(ComponentInfo component) throws Exception {
    for (ComponentInfo sibling : getContainer().getChildrenComponents()) {
      adjustAnchoredToMovedComponentSides(sibling, component);
    }
  }

  private void adjustAnchoredToMovedComponentSides(ComponentInfo component, ComponentInfo anchor)
      throws Exception {
    adjustAnchoredToMovedComponentSide(component, anchor, IPositionConstants.TOP);
    adjustAnchoredToMovedComponentSide(component, anchor, IPositionConstants.LEFT);
    adjustAnchoredToMovedComponentSide(component, anchor, IPositionConstants.BOTTOM);
    adjustAnchoredToMovedComponentSide(component, anchor, IPositionConstants.RIGHT);
  }

  private void adjustAnchoredToMovedComponentSide(ComponentInfo component,
      ComponentInfo anchor,
      int side) throws Exception {
    SpringAttachmentInfo attachment = getAttachment(component, side);
    if (attachment.getAnchorComponent() == anchor) {
      attachment.adjustAfterComponentMove();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setting new layout
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    List<ComponentInfo> components = getComponents();
    Insets parentAreaInsets = getContainer().getClientAreaInsets();
    for (ComponentInfo component : components) {
      Rectangle componentBounds = component.getModelBounds();
      Dimension preferredSize = component.getPreferredSize();
      int x = componentBounds.x - parentAreaInsets.left;
      int y = componentBounds.y - parentAreaInsets.top;
      int width = componentBounds.width;
      int height = componentBounds.height;
      setAttachmentOffset(component, IPositionConstants.LEFT, IPositionConstants.LEFT, x);
      setAttachmentOffset(component, IPositionConstants.TOP, IPositionConstants.TOP, y);
      if (width != preferredSize.width) {
        setAttachmentOffset(component, IPositionConstants.RIGHT, IPositionConstants.LEFT, x + width);
      }
      if (height != preferredSize.height) {
        setAttachmentOffset(component, IPositionConstants.BOTTOM, IPositionConstants.TOP, y
            + height);
      }
    }
  }

  private void setAttachmentOffset(ComponentInfo component, int side, int parentSide, int offset)
      throws Exception {
    SpringAttachmentInfo attachment = getAttachment(component, side);
    attachment.setAnchorSide(parentSide);
    attachment.setAnchorComponent(getContainer());
    attachment.setOffset(offset);
    attachment.write();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void updateConstraintsProperties() throws Exception {
    List<ComponentInfo> components = getComponents();
    for (ComponentInfo component : components) {
      if (isManagedObject(component)) {
        Property[] properties = getConstraintsProperty(component).getProperties();
        ((ComplexProperty) properties[0]).setText(getAttachmentLeft(component).toString());
        ((ComplexProperty) properties[1]).setText(getAttachmentRight(component).toString());
        ((ComplexProperty) properties[2]).setText(getAttachmentTop(component).toString());
        ((ComplexProperty) properties[3]).setText(getAttachmentBottom(component).toString());
      }
    }
  }

  private ComplexProperty getConstraintsProperty(ComponentInfo component) throws Exception {
    ComplexProperty constraintsProperty = (ComplexProperty) component.getArbitraryValue(this);
    if (constraintsProperty == null) {
      constraintsProperty = new ComplexProperty("Constraints", "<constraints>") {
        @Override
        public boolean isModified() throws Exception {
          return true;
        }
      };
      component.putArbitraryValue(this, constraintsProperty);
      constraintsProperty.setCategory(PropertyCategory.system(6));
      // set sub-properties
      Property[] constraintsProperties = createAttachmentsProperties(component);
      constraintsProperty.setProperties(constraintsProperties);
    }
    return constraintsProperty;
  }

  private Property[] createAttachmentsProperties(ComponentInfo component) throws Exception {
    return new Property[]{
        createAttachmentProperty(component, IPositionConstants.LEFT, "WEST"),
        createAttachmentProperty(component, IPositionConstants.RIGHT, "EAST"),
        createAttachmentProperty(component, IPositionConstants.TOP, "NORTH"),
        createAttachmentProperty(component, IPositionConstants.BOTTOM, "SOUTH")};
  }

  /**
   * Creates the {@link ComplexProperty} for {@link SpringAttachmentInfo} by attachment's property
   * name.
   */
  private ComplexProperty createAttachmentProperty(ComponentInfo component, int side, String title)
      throws Exception {
    final SpringAttachmentInfo attachment = getAttachment(component, side);
    ComplexProperty attachmentProperty = new ComplexProperty(title, attachment.toString()) {
      @Override
      public boolean isModified() throws Exception {
        return !attachment.isVirtual();
      }

      @Override
      public void setValue(Object value) throws Exception {
        if (value == Property.UNKNOWN_VALUE) {
          attachment.delete();
          ExecutionUtils.refresh(SpringLayoutInfo.this);
        }
      }
    };
    attachmentProperty.setProperties(attachment.getProperties());
    return attachmentProperty;
  }
}
