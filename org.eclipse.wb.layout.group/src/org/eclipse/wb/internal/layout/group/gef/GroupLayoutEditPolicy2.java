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
package org.eclipse.wb.internal.layout.group.gef;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.OutlineImageFigure;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutUtils;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;

import java.util.ArrayList;
import java.util.List;

/**
 * Policy implementing manipulations with GroupLayout.
 * 
 * @author mitin_aa
 */
public abstract class GroupLayoutEditPolicy2 extends LayoutEditPolicy implements IFeedbacksHelper {
  private final IGroupLayoutInfo m_layout;
  private Figure m_dragFeedback;
  private final FeedbacksDrawer m_feedbacksDrawer;
  private java.awt.Rectangle[] m_movingBounds;
  private boolean m_dragInProgress;
  private String[] m_movingIds;
  private LayoutComponent[] m_pastedLayoutComponents;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutEditPolicy2(final IGroupLayoutInfo layout) {
    m_layout = layout;
    new BroadcastListenerHelper(layout.getAdapter(JavaInfo.class), this, new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        showSelectionFeedbacks();
      }
    });
    m_feedbacksDrawer = new FeedbacksDrawer(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decorate Child
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    if (m_layout.isRelatedComponent((JavaInfo) child.getModel())) {
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new GroupSelectionEditPolicy2(m_layout));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void eraseSelectionFeedbacks() {
    for (EditPart child : getHost().getChildren()) {
      if (m_layout.isRelatedComponent((JavaInfo) child.getModel())) {
        GroupSelectionEditPolicy2 editPolicy =
            (GroupSelectionEditPolicy2) child.getEditPolicy(EditPolicy.SELECTION_ROLE);
        editPolicy.hideSelection();
      }
    }
  }

  protected void showSelectionFeedbacks() {
    for (EditPart child : getHost().getChildren()) {
      if (m_layout.isRelatedComponent((JavaInfo) child.getModel())
          && child.getSelected() != EditPart.SELECTED_NONE) {
        GroupSelectionEditPolicy2 editPolicy =
            (GroupSelectionEditPolicy2) child.getEditPolicy(EditPolicy.SELECTION_ROLE);
        editPolicy.showSelection();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showLayoutTargetFeedback(Request request) {
    eraseSelectionFeedbacks();
    if (request instanceof ChangeBoundsRequest) {
      if (Request.REQ_ADD.equals(request.getType())) {
        showAddFeedback((ChangeBoundsRequest) request);
      } else {
        showMoveFeedback((ChangeBoundsRequest) request);
      }
    } else if (request instanceof CreateRequest) {
      showCreationFeedback((CreateRequest) request);
    } else if (request instanceof PasteRequest) {
      showPasteFeedback((PasteRequest) request);
    }
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    m_feedbacksDrawer.removeFeedbacks();
    if (m_dragFeedback != null) {
      removeFeedback(m_dragFeedback);
      m_dragFeedback = null;
    }
    m_dragInProgress = false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  private java.awt.Point m_startLocation;

  private void showCreationFeedback(CreateRequest request) {
    // remove create feedback
    if (m_dragFeedback != null) {
      removeFeedback(m_dragFeedback);
      m_dragFeedback = null;
    }
    m_feedbacksDrawer.removeFeedbacks();
    // prepare
    Point location = request.getLocation().getCopy();
    translateAbsoluteToModel(location);
    AbstractComponentInfo newWidget = (AbstractComponentInfo) request.getNewObject();
    Image image = newWidget.getImage();
    // set size
    Dimension size = request.getSize();
    java.awt.Point topLeftPoint;
    Dimension preferredSize;
    if (size != null) {
      // size-on-drop info update
      topLeftPoint = new java.awt.Point(m_startLocation.x, m_startLocation.y);
      preferredSize =
          new Dimension(size.width + location.x - m_startLocation.x, size.height
              + location.y
              - m_startLocation.y);
    } else {
      preferredSize = newWidget.getPreferredSize();
      int y = location.y - preferredSize.height / 2;
      topLeftPoint = new java.awt.Point(location.x - preferredSize.width / 2, y);
    }
    m_dragFeedback = new OutlineImageFigure(image);
    addFeedback(m_dragFeedback);
    // process placement
    // calling m_layoutDesigner.startAdding is cheap, so we don't need no track creation start
    final LayoutComponent[] layoutComponents =
        new LayoutComponent[]{m_layout.createLayoutComponent(newWidget)};
    final java.awt.Rectangle[] movingBounds =
        new java.awt.Rectangle[]{new java.awt.Rectangle(0,
            0,
            preferredSize.width,
            preferredSize.height)};
    String id = getContainerId();
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    layoutDesigner.startAdding(layoutComponents, movingBounds, new java.awt.Point(0, 0), id);
    final java.awt.Rectangle[] movedBounds =
        new java.awt.Rectangle[]{new java.awt.Rectangle(0,
            0,
            preferredSize.width,
            preferredSize.height)};
    layoutDesigner.move(topLeftPoint, id, !DesignerPlugin.isShiftPressed(), false, movedBounds);
    layoutDesigner.paintMoveFeedback(m_feedbacksDrawer);
    Rectangle widgetBounds = GroupLayoutUtils.get(movedBounds[0]);
    // store drag start location
    if (size == null) {
      m_startLocation = movedBounds[0].getLocation();
    }
    //
    translateModelToFeedback(widgetBounds);
    // update create feedback
    m_dragFeedback.setBounds(widgetBounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add (re-parent)
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showAddFeedback(ChangeBoundsRequest request) {
    if (m_dragFeedback != null) {
      removeFeedback(m_dragFeedback);
      m_dragFeedback = null;
    }
    m_feedbacksDrawer.removeFeedbacks();
    // prepare
    List<EditPart> editParts = request.getEditParts();
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    LayoutComponent[] layoutComponents = new LayoutComponent[editParts.size()];
    String id = getContainerId();
    java.awt.Rectangle[] movingBounds = new java.awt.Rectangle[editParts.size()];
    GraphicalEditPart firstEditPart = (GraphicalEditPart) editParts.get(0);
    AbstractComponentInfo firstEditPartModel = (AbstractComponentInfo) firstEditPart.getModel();
    // calc offsets
    int offsetX = Integer.MAX_VALUE;
    int offsetY = Integer.MAX_VALUE;
    int j = 0;
    for (EditPart editPart : editParts) {
      AbstractComponentInfo model = (AbstractComponentInfo) editPart.getModel();
      Rectangle bounds = model.getModelBounds();
      offsetX = Math.min(offsetX, bounds.x);
      offsetY = Math.min(offsetY, bounds.y);
    }
    for (EditPart editPart : editParts) {
      AbstractComponentInfo model = (AbstractComponentInfo) editPart.getModel();
      java.awt.Rectangle bounds = GroupLayoutUtils.get(model.getModelBounds());
      movingBounds[j] =
          new java.awt.Rectangle(bounds.x - offsetX,
              bounds.y - offsetY,
              bounds.width,
              bounds.height);
      layoutComponents[j++] = m_layout.createLayoutComponent(model);
    }
    // the position of the mouse inside the dragging rectangle 
    Point hotSpot = request.getLocation().getCopy();
    PolicyUtils.translateAbsoluteToModel((GraphicalEditPart) firstEditPart.getParent(), hotSpot);
    hotSpot.translate(request.getMoveDelta().getNegated());
    hotSpot.translate(-offsetX, -offsetY);
    // current mouse position in model coordinates
    Point topLeft = request.getLocation().getCopy();
    translateAbsoluteToModel(topLeft);
    {
      // start re-parenting
      layoutDesigner.startAdding(layoutComponents, movingBounds, new java.awt.Point(hotSpot.x,
          hotSpot.y), id);
      if (editParts.size() > 1) {
        Rectangle firstPartBounds = new Rectangle(firstEditPart.getFigure().getBounds());
        m_dragFeedback = new OutlineImageFigure(null);
        for (EditPart editPart : editParts) {
          AbstractComponentInfo model = (AbstractComponentInfo) editPart.getModel();
          Rectangle bounds = ((GraphicalEditPart) editPart).getFigure().getBounds();
          m_dragFeedback.add(new OutlineImageFigure(model.getImage(),
              AbsolutePolicyUtils.COLOR_OUTLINE), bounds);
        }
        // set bounds of nested figures
        List<Figure> moveFeedbackFigures = m_dragFeedback.getChildren();
        for (j = 0; j < moveFeedbackFigures.size(); ++j) {
          Figure figure = moveFeedbackFigures.get(j);
          figure.getBounds().translate(-firstPartBounds.x, -firstPartBounds.y);
        }
      } else {
        m_dragFeedback =
            new OutlineImageFigure(firstEditPartModel.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
      }
      addFeedback(m_dragFeedback);
    }
    // prepare and move
    java.awt.Rectangle[] movedBounds = new java.awt.Rectangle[editParts.size()];
    for (int i = 0; i < movedBounds.length; i++) {
      movedBounds[i] = new java.awt.Rectangle();
      movedBounds[i].width = movingBounds[i].width;
      movedBounds[i].height = movingBounds[i].height;
    }
    layoutDesigner.move(
        new java.awt.Point(topLeft.x, topLeft.y),
        id,
        !DesignerPlugin.isShiftPressed(),
        false,
        movedBounds);
    layoutDesigner.paintMoveFeedback(m_feedbacksDrawer);
    // done
    Rectangle newBounds = GroupLayoutUtils.getRectangleUnion(movedBounds);
    translateModelToFeedback(newBounds);
    m_dragFeedback.setBounds(newBounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showMoveFeedback(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    if (editParts.size() == 0) {
      return;
    }
    // prepare
    Point location = request.getLocation().getCopy();
    m_feedbacksDrawer.removeFeedbacks();
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    // start dragging
    if (!m_dragInProgress) {
      m_movingBounds = new java.awt.Rectangle[editParts.size()];
      m_movingIds = new String[editParts.size()];
      int i = 0;
      for (EditPart part : editParts) {
        AbstractComponentInfo model = (AbstractComponentInfo) part.getModel();
        m_movingIds[i] = ObjectInfoUtils.getId(model);
        m_movingBounds[i] = GroupLayoutUtils.getBoundsInLayout(m_layout, model);
        Point moveDelta = request.getMoveDelta();
        m_movingBounds[i].x += moveDelta.x;
        m_movingBounds[i].y += moveDelta.y;
        i++;
      }
      layoutDesigner.startMoving(m_movingIds, m_movingBounds, new java.awt.Point(location.x,
          location.y));
      // calculate model bounds and create move feedback
      if (editParts.size() > 1) {
        GraphicalEditPart firstEditPart = (GraphicalEditPart) editParts.get(0);
        Rectangle firstPartBounds = new Rectangle(firstEditPart.getFigure().getBounds());
        m_dragFeedback = new OutlineImageFigure(null);
        //
        for (EditPart editPart : editParts) {
          AbstractComponentInfo model = (AbstractComponentInfo) editPart.getModel();
          Rectangle bounds = ((GraphicalEditPart) editPart).getFigure().getBounds();
          m_dragFeedback.add(new OutlineImageFigure(model.getImage(),
              AbsolutePolicyUtils.COLOR_OUTLINE), bounds);
        }
        //
        List<Figure> moveFeedbackFigures = m_dragFeedback.getChildren();
        for (int j = 0; j < moveFeedbackFigures.size(); ++j) {
          Figure figure = moveFeedbackFigures.get(j);
          figure.getBounds().translate(-firstPartBounds.x, -firstPartBounds.y);
        }
      } else {
        EditPart editPart = editParts.get(0);
        AbstractComponentInfo model = (AbstractComponentInfo) editPart.getModel();
        m_dragFeedback =
            new OutlineImageFigure(model.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
      }
      addFeedback(m_dragFeedback);
      m_dragInProgress = true;
      return;
    }
    // continue dragging
    String id = getContainerId();
    java.awt.Rectangle[] movedBounds = new java.awt.Rectangle[editParts.size()];
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
    // setup feedback figure
    Rectangle newBounds = GroupLayoutUtils.getRectangleUnion(movedBounds);
    translateModelToFeedback(newBounds);
    m_dragFeedback.setBounds(newBounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showPasteFeedback(PasteRequest request) {
    if (m_dragFeedback != null) {
      removeFeedback(m_dragFeedback);
      m_dragFeedback = null;
    }
    m_feedbacksDrawer.removeFeedbacks();
    @SuppressWarnings("unchecked")
    List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
    List<AbstractComponentInfo> pastedModels =
        new ArrayList<AbstractComponentInfo>(mementos.size());
    // prepare
    request.setObjects(pastedModels);
    Point location = request.getLocation().getCopy();
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    translateAbsoluteToModel(location);
    try {
      // calculate model bounds and create drag feedback
      m_pastedLayoutComponents = new LayoutComponent[mementos.size()];
      java.awt.Rectangle[] movingBounds = new java.awt.Rectangle[mementos.size()];
      java.awt.Rectangle[] movedBounds = new java.awt.Rectangle[mementos.size()];
      if (mementos.size() > 1) {
        // multiple components
        m_dragFeedback = new OutlineImageFigure(null);
        // create widgets from memento and calculate their relative placement
        int offsetX = Integer.MAX_VALUE;
        int offsetY = Integer.MAX_VALUE;
        for (JavaInfoMemento memento : mementos) {
          AbstractComponentInfo model = (AbstractComponentInfo) memento.create(getJavaInfo());
          Rectangle bounds = model.getBounds();
          offsetX = Math.min(offsetX, bounds.x);
          offsetY = Math.min(offsetY, bounds.y);
          pastedModels.add(model);
        }
        for (int i = 0; i < pastedModels.size(); ++i) {
          AbstractComponentInfo model = pastedModels.get(i);
          java.awt.Rectangle bounds = GroupLayoutUtils.getBoundsInLayout(m_layout, model);
          movingBounds[i] =
              new java.awt.Rectangle(bounds.x - offsetX,
                  bounds.y - offsetY,
                  bounds.width,
                  bounds.height);
          m_dragFeedback.add(new OutlineImageFigure(model.getImage(),
              AbsolutePolicyUtils.COLOR_OUTLINE), GroupLayoutUtils.get(movingBounds[i]));
          // prepare bounds for dragging
          movedBounds[i] = new java.awt.Rectangle();
          movedBounds[i].width = movingBounds[i].width;
          movedBounds[i].height = movingBounds[i].height;
          m_pastedLayoutComponents[i] = m_layout.createLayoutComponent(model);
        }
      } else {
        // single component
        AbstractComponentInfo model = (AbstractComponentInfo) mementos.get(0).create(getJavaInfo());
        pastedModels.add(model);
        java.awt.Rectangle modelBounds = GroupLayoutUtils.getBoundsInLayout(m_layout, model);
        movingBounds[0] = new java.awt.Rectangle(0, 0, modelBounds.width, modelBounds.height);
        movedBounds[0] = new java.awt.Rectangle(0, 0, modelBounds.width, modelBounds.height);
        m_pastedLayoutComponents[0] = m_layout.createLayoutComponent(model);
        // drag feedback
        m_dragFeedback =
            new OutlineImageFigure(model.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
      }
      addFeedback(m_dragFeedback);
      // do drag
      String id = getContainerId();
      Rectangle movingBoundsUnion = GroupLayoutUtils.getRectangleUnion(movingBounds);
      java.awt.Point hotSpot =
          new java.awt.Point(movingBoundsUnion.width / 2, movingBoundsUnion.height / 2);
      layoutDesigner.startAdding(m_pastedLayoutComponents, movingBounds, hotSpot, id);
      layoutDesigner.move(
          new java.awt.Point(location.x, location.y),
          id,
          !DesignerPlugin.isShiftPressed(),
          false,
          movedBounds);
      layoutDesigner.paintMoveFeedback(m_feedbacksDrawer);
      // apply bounds to feedback
      Rectangle newBounds = GroupLayoutUtils.getRectangleUnion(movedBounds);
      translateModelToFeedback(newBounds);
      m_dragFeedback.setBounds(newBounds);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    return new EditCommand(getJavaInfo()) {
      @Override
      protected void executeEdit() throws Exception {
        m_layout.command_commit();
      }
    };
  }

  @Override
  protected Command getPasteCommand(final PasteRequest request) {
    return new EditCommand(getJavaInfo()) {
      @Override
      protected void executeEdit() throws Exception {
        @SuppressWarnings("unchecked")
        List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
        m_layout.command_paste(mementos);
      }
    };
  }

  @Override
  protected Command getCreateCommand(final CreateRequest request) {
    return new EditCommand(getJavaInfo()) {
      @Override
      protected void executeEdit() throws Exception {
        m_layout.command_create((AbstractComponentInfo) request.getNewObject());
      }
    };
  }

  @Override
  protected Command getAddCommand(final ChangeBoundsRequest request) {
    return new EditCommand(getJavaInfo()) {
      @Override
      protected void executeEdit() throws Exception {
        List<EditPart> editParts = request.getEditParts();
        @SuppressWarnings("unchecked")
        List<AbstractComponentInfo> models =
            (List<AbstractComponentInfo>) CollectionUtils.collect(editParts, new Transformer() {
              public Object transform(Object input) {
                return ((EditPart) input).getModel();
              }
            });
        m_layout.command_add(models);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  public void translateAbsoluteToModel(Translatable t) {
    PolicyUtils.translateAbsoluteToModel(this, t);
    t.translate(getClientAreaOffset().getNegated());
  }

  public void translateModelToFeedback(Translatable t) {
    PolicyUtils.translateModelToFeedback(this, t);
    t.translate(getClientAreaOffset());
  }

  protected Point getClientAreaOffset() {
    Insets insets = m_layout.getContainerInsets();
    return new Point(insets.left, insets.top);
  }

  public void addFeedback2(Figure figure) {
    addFeedback(figure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfo getJavaInfo() {
    return m_layout.getAdapter(JavaInfo.class);
  }

  private String getContainerId() {
    return ObjectInfoUtils.getId(m_layout.getLayoutContainer());
  }
}