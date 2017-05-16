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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.OutlineImageFigure;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.GroupRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.gef.policy.snapping.IFeedbackProxy;
import org.eclipse.wb.internal.core.gef.policy.snapping.IVisualDataProvider;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;
import org.eclipse.wb.internal.gef.core.CompoundCommand;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generic {@link LayoutEditPolicy} for absolute based layouts.
 *
 * @author mitin_aa
 * @author lobas_av
 * @coverage core.gef.policy
 */
public abstract class AbsoluteBasedLayoutEditPolicy<C extends IAbstractComponentInfo>
    extends
      KeyboardMovingLayoutEditPolicy
    implements
      IVisualDataProvider,
      IFeedbackProxy,
      IPreferenceConstants {
  protected PlacementsSupport placementsSupport;
  private int m_frozenYValue;
  private final IObjectInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteBasedLayoutEditPolicy(ObjectInfo layout) {
    m_layout = layout;
    new BroadcastListenerHelper(layout, this, new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent instanceof IAbstractComponentInfo) {
          // don't do any processing if parent is deleting.
          if (!parent.isDeleting()) {
            if (isOurChild(parent, child) && child instanceof IAbstractComponentInfo) {
              onDelete(child);
            }
          }
        }
      }

      private boolean isOurChild(ObjectInfo parent, ObjectInfo child) {
        return getHostModel() == parent && parent != null && parent.getChildren().contains(child);
      }
    });
    new BroadcastListenerHelper(layout, this, new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        showSelectionFeedbacks();
      }

      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        AbstractAlignmentActionsSupport<C> alignmentSupport = getAlignmentActionsSupport();
        if (alignmentSupport != null) {
          alignmentSupport.addAlignmentActions(objects, actions);
        }
      }
    });
  }

  protected final void createPlacementsSupport(IAbsoluteLayoutCommands layoutCommands) {
    placementsSupport = new PlacementsSupport(this, this, layoutCommands, getAllComponents());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout editing preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return preference store for this layout editing.
   */
  protected final IPreferenceStore getPreferenceStore() {
    return getToolkit().getPreferences();
  }

  /**
   * @return the {@link ToolkitDescription} of the toolkit this policy applies to.
   */
  protected abstract ToolkitDescription getToolkit();

  /**
   * @return a X grid step value for this layout.
   */
  public int getGridStepX() {
    return getPreferenceStore().getInt(P_GRID_STEP_X);
  }

  /**
   * @return a Y grid step value for this layout.
   */
  public int getGridStepY() {
    return getPreferenceStore().getInt(P_GRID_STEP_Y);
  }

  /**
   * @return <code>true</code> if "free mode" snapping enabled for the layout.
   */
  public boolean useFreeSnapping() {
    return getPreferenceStore().getBoolean(P_USE_FREE_MODE);
  }

  /**
   * @return <code>true</code> if grid snapping enabled for the layout.
   */
  public boolean useGridSnapping() {
    return getPreferenceStore().getBoolean(P_USE_GRID);
  }

  /**
   * @return <code>true</code> if move/resize hint feedback enabled for the layout.
   */
  public boolean isShowTextFeedback() {
    return getPreferenceStore().getBoolean(P_DISPLAY_LOCATION_SIZE_HINTS);
  }

  /**
   * @return <code>true</code> if grid feedback enabled for the layout.
   */
  public boolean isShowGridFeedback() {
    return getPreferenceStore().getBoolean(P_DISPLAY_GRID);
  }

  /**
   * @return <code>true</code> when snapping should be disabled.
   */
  public boolean isSuppressingSnapping() {
    return DesignerPlugin.isShiftPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link List<IAbstractComponentInfo>} of all widget info's.
   */
  public abstract List<C> getAllComponents();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getResizeRequestType() {
    return AbsoluteBasedSelectionEditPolicy.REQ_RESIZE;
  }

  @Override
  public boolean understandsRequest(Request request) {
    if (Request.REQ_ORPHAN.equals(request.getType())
        || getResizeRequestType().equals(request.getType())) {
      return true;
    }
    return super.understandsRequest(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_dotsFeedback;
  private Figure m_moveFeedback;
  private Figure m_createFeedback;
  private TextFeedback m_locationFeedback;
  private TextFeedback m_sizeFeedback;

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
  public final String getSizeHintString(EditPart editPart, int width, int height) {
    return Integer.toString(width) + " x " + Integer.toString(height);
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
  public String getLocationHintString(EditPart editPart, int x, int y) {
    return Integer.toString(x) + " x " + Integer.toString(y);
  }

  //
  @Override
  protected void showLayoutTargetFeedback(Request request) {
    // prepare snapping
    if (request instanceof ChangeBoundsRequest) {
      eraseSelectionFeedbacks();
      placementsSupport.cleanup();
      showMoveFeedback((ChangeBoundsRequest) request);
    } else if (request instanceof CreateRequest) {
      placementsSupport.cleanup();
      showCreationFeedback((CreateRequest) request);
    } else if (request instanceof PasteRequest) {
      placementsSupport.cleanup();
      showPasteFeedback((PasteRequest) request);
    }
  }

  /**
   * Called before starting to move components.
   */
  protected void eraseSelectionFeedbacks() {
  }

  /**
   * Restores the selection after any operation.
   */
  protected void showSelectionFeedbacks() {
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    if (m_dotsFeedback != null) {
      removeFeedback(m_dotsFeedback);
      m_dotsFeedback = null;
    }
    placementsSupport.clearFeedbacks();
    if (m_moveFeedback != null) {
      removeFeedback(m_moveFeedback);
      m_moveFeedback = null;
    }
    if (m_createFeedback != null) {
      removeFeedback(m_createFeedback);
      m_createFeedback = null;
    }
    if (m_locationFeedback != null) {
      m_locationFeedback.remove();
      m_locationFeedback = null;
    }
    if (m_sizeFeedback != null) {
      m_sizeFeedback.remove();
      m_sizeFeedback = null;
    }
    m_frozenYValue = 0;
  }

  private boolean isFreezeVerticalAxis(Request request) {
    return request.isControlKeyPressed() && m_frozenYValue != 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showMoveFeedback(ChangeBoundsRequest request) {
    // first of all remove an old feedback
    if (m_moveFeedback != null) {
      removeFeedback(m_moveFeedback);
      m_moveFeedback = null;
    }
    // is this edit parts moved by using a keyboard
    boolean isKeyboardMoving = isKeyboardMoving();
    // prepare dots feedback if enabled
    Figure hostFigure = getHostFigure();
    if (m_dotsFeedback == null) {
      if ((useGridSnapping() || isKeyboardMoving()) && isShowGridFeedback()) {
        m_dotsFeedback = new DotsFeedback<C>(this, hostFigure);
        addFeedback(m_dotsFeedback);
      }
    }
    // some preparations
    List<EditPart> editParts = request.getEditParts();
    List<IAbstractComponentInfo> modelList = Lists.newArrayList();
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
        figure.getBounds().translate(-widgetBounds.x, -widgetBounds.y);
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
    placementsSupport.drag(
        moveLocation,
        ImmutableList.copyOf(modelList),
        widgetBounds,
        ImmutableList.copyOf(relativeBounds));
    widgetBounds = placementsSupport.getBounds();
    // Store new "model" location to be shown in TextFeedback if enabled
    int newX = widgetBounds.x;
    int newY = widgetBounds.y;
    // update move feedback: translate bounds back into feedback coordinates, apply bounds for feedback figure
    translateModelToFeedback(widgetBounds);
    m_moveFeedback.setBounds(widgetBounds);
    // create text feedback
    if (m_locationFeedback == null && isShowTextFeedback() && !isKeyboardMoving) {
      m_locationFeedback = new TextFeedback(getTextFeedbackLayer());
      m_locationFeedback.add();
    }
    // update text feedback
    if (m_locationFeedback != null) {
      EditPart firstEditPart = editParts.get(0);
      m_locationFeedback.setText(getLocationHintString(firstEditPart, newX, newY));
      Point locationFeedbackLocation =
          getLocationHintLocation(firstEditPart, widgetBounds, m_locationFeedback.getSize());
      m_locationFeedback.setLocation(locationFeedbackLocation);
    }
  }

  protected Point getLocationHintLocation(EditPart editPart,
      Rectangle widgetBounds,
      Dimension hintSize) {
    return widgetBounds.getLocation().getTranslated(-30, -25);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_resizeFeedback;
  private TextFeedback m_textFeedback;

  public void showResizeFeedback(ChangeBoundsRequest request) {
    if (m_resizeFeedback != null) {
      removeFeedback(m_resizeFeedback);
      m_resizeFeedback = null;
    }
    getPlacementsSupport().cleanup();
    // prepare
    List<EditPart> editParts = request.getEditParts();
    List<IAbstractComponentInfo> modelList = Lists.newArrayList();
    Rectangle[] relativeBounds = new Rectangle[editParts.size()];
    Rectangle widgetBounds;
    // calculate model bounds and create move feedback
    if (editParts.size() > 1) {
      GraphicalEditPart firstEditPart = (GraphicalEditPart) editParts.get(0);
      widgetBounds = new Rectangle(firstEditPart.getFigure().getBounds());
      m_resizeFeedback = new OutlineImageFigure(null);
      //
      for (EditPart editPart : editParts) {
        IAbstractComponentInfo model = (IAbstractComponentInfo) editPart.getModel();
        Rectangle bounds = ((GraphicalEditPart) editPart).getFigure().getBounds();
        //
        widgetBounds.union(bounds);
        modelList.add(model);
        //
        m_resizeFeedback.add(new OutlineImageFigure(model.getImage(),
            AbsolutePolicyUtils.COLOR_OUTLINE), bounds);
      }
      //
      List<Figure> moveFeedbackFigures = m_resizeFeedback.getChildren();
      for (int i = 0; i < moveFeedbackFigures.size(); ++i) {
        Figure figure = moveFeedbackFigures.get(i);
        figure.getBounds().translate(-widgetBounds.x, -widgetBounds.y);
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
      m_resizeFeedback =
          new OutlineImageFigure(model.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
    }
    boolean isKeyboardMoving = isKeyboardMoving();
    //
    widgetBounds = request.getTransformedRectangle(widgetBounds.getCopy());
    // translate feedback coordinates into model coordinates (ex., SWT shell control
    // has caption and border area, so (0,0) point offset is border left and caption
    // size, i.e. insets.
    FigureUtils.translateFigureToAbsolute2(getHostFigure(), widgetBounds);
    PolicyUtils.translateAbsoluteToModel(this, widgetBounds);
    // do drag
    Dimension sizeDelta = request.getSizeDelta();
    Point moveLocation =
        isKeyboardMoving ? new Point(sizeDelta.width, sizeDelta.height) : request.getLocation();
    // do drag
    getPlacementsSupport().drag(
        moveLocation,
        ImmutableList.copyOf(modelList),
        widgetBounds,
        ImmutableList.copyOf(relativeBounds),
        request.getResizeDirection());
    //
    widgetBounds = getPlacementsSupport().getBounds();
    PolicyUtils.translateModelToFeedback(this, widgetBounds);
    // create dots feedback
    if (m_dotsFeedback == null) {
      if (useGridSnapping() || isKeyboardMoving) {
        m_dotsFeedback = new DotsFeedback<C>(this, getHost().getFigure());
        addFeedback(m_dotsFeedback);
      }
    }
    // create selection feedback
    addFeedback(m_resizeFeedback);
    // update selection feedback
    m_resizeFeedback.setBounds(widgetBounds);
    // create text feedback
    if (m_textFeedback == null && isShowTextFeedback() && !isKeyboardMoving) {
      m_textFeedback = new TextFeedback(getFeedbackLayer());
      m_textFeedback.add();
    }
    // update text feedback
    if (m_textFeedback != null) {
      m_textFeedback.setText(getSizeHintString(getHost(), widgetBounds.width, widgetBounds.height));
      m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
    }
  }

  public void eraseResizeFeedback(ChangeBoundsRequest request) {
    eraseLayoutTargetFeedback(request);
    if (m_resizeFeedback != null) {
      removeFeedback(m_resizeFeedback);
      m_resizeFeedback = null;
    }
    if (m_textFeedback != null) {
      m_textFeedback.remove();
      m_textFeedback = null;
    }
  }

  final Command getResizeCommandImpl(ChangeBoundsRequest request) {
    CompoundEditCommand command = new CompoundEditCommand(m_layout.getUnderlyingModel());
    command.add(new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        getPlacementsSupport().commit();
      }
    });
    Command resizeCommand = getResizeCommand(request);
    if (resizeCommand != null) {
      command.add(resizeCommand);
    }
    return command;
  }

  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  protected boolean m_resizeOnCreate;
  private Point m_startLocation;

  private void showCreationFeedback(CreateRequest request) {
    // remove create feedback
    if (m_createFeedback != null) {
      removeFeedback(m_createFeedback);
      m_createFeedback = null;
    }
    Point location = request.getLocation().getCopy();
    translateAbsoluteToModel(location);
    IAbstractComponentInfo newWidget = (IAbstractComponentInfo) request.getNewObject();
    Image image = newWidget.getImage();
    // set size
    Dimension size = request.getSize();
    Point topLeftPoint;
    Dimension preferredSize;
    if (size != null) {
      m_resizeOnCreate = true;
      // size-on-drop info update
      topLeftPoint = new Point(m_startLocation.x, m_startLocation.y);
      preferredSize =
          new Dimension(size.width + location.x - m_startLocation.x, size.height
              + location.y
              - m_startLocation.y);
      // prevent axis fixing
      m_frozenYValue = 0;
    } else {
      m_resizeOnCreate = false;
      preferredSize = newWidget.getPreferredSize();
      if (preferredSize != null) {
        int y;
        // freeze vertical axis if needed
        boolean freezeVerticalAxis = isFreezeVerticalAxis(request);
        if (freezeVerticalAxis) {
          y = m_frozenYValue;
        } else {
          y = m_frozenYValue = location.y - preferredSize.height / 2;
        }
        topLeftPoint = new Point(location.x - preferredSize.width / 2, y);
      } else {
        preferredSize = new Dimension(0, 0);
        topLeftPoint = location.getCopy();
      }
    }
    Rectangle widgetBounds =
        new Rectangle(topLeftPoint.x, topLeftPoint.y, preferredSize.width, preferredSize.height);
    m_createFeedback = new OutlineImageFigure(image);
    addFeedback(m_createFeedback);
    // process placement
    placementsSupport.drag(location, newWidget, widgetBounds, size == null
        ? 0
        : IPositionConstants.SOUTH_EAST);
    widgetBounds = placementsSupport.getBounds();
    Rectangle widgetModelBounds = widgetBounds.getCopy();
    // store drag start location
    if (size == null) {
      m_startLocation = widgetBounds.getLocation();
    }
    //
    Figure hostFigure = getHostFigure();
    // create dots feedback
    if (m_dotsFeedback == null) {
      if (useGridSnapping() || isKeyboardMoving()) {
        m_dotsFeedback = new DotsFeedback<C>(this, hostFigure);
        addFeedback(m_dotsFeedback);
      }
    }
    translateModelToFeedback(widgetBounds);
    // update create feedback
    m_createFeedback.setBounds(widgetBounds);
    // create text feedbacks
    if (isShowTextFeedback() && !isKeyboardMoving()) {
      if (m_locationFeedback == null) {
        m_locationFeedback = new TextFeedback(getTextFeedbackLayer());
        m_locationFeedback.add();
      }
      if (size != null && m_sizeFeedback == null) {
        m_sizeFeedback = new TextFeedback(getTextFeedbackLayer());
        m_sizeFeedback.add();
      }
      // update text feedbacks
      m_locationFeedback.setText(getLocationHintString(
          getHost(),
          widgetModelBounds.x,
          widgetModelBounds.y));
      m_locationFeedback.setLocation(widgetBounds.getLocation().getTranslated(-30, -25));
      if (size != null) {
        m_sizeFeedback.setText(getSizeHintString(
            getHost(),
            widgetModelBounds.width,
            widgetModelBounds.height));
        m_sizeFeedback.setLocation(widgetBounds.getBottomRight().getTranslated(30, 25));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<IAbstractComponentInfo, PastedComponentInfo> m_pastedComponents;
  private Point m_pasteLocation;

  private void showPasteFeedback(final PasteRequest request) {
    List<IObjectInfo> pastingComponents =
        GlobalState.getPasteRequestProcessor().getPastingComponents(request);
    m_pastedComponents = Maps.newHashMap();
    List<IAbstractComponentInfo> pastedModels =
        new ArrayList<IAbstractComponentInfo>(pastingComponents.size());
    try {
      // remove create feedback
      if (m_createFeedback != null) {
        removeFeedback(m_createFeedback);
        m_createFeedback = null;
      }
      //
      Point location = request.getLocation().getCopy();
      translateAbsoluteToModel(location);
      //
      Rectangle widgetBounds;
      request.setObjects(pastedModels);
      // calculate model bounds and create move feedback
      Rectangle[] relativeBounds = new Rectangle[pastingComponents.size()];
      if (pastingComponents.size() > 1) {
        widgetBounds = new Rectangle();
        m_createFeedback = new OutlineImageFigure(null);
        // create widgets from memento and calculate their relative placement
        int offsetX = Integer.MAX_VALUE;
        int offsetY = Integer.MAX_VALUE;
        for (IObjectInfo pastedObject : pastingComponents) {
          IAbstractComponentInfo model = (IAbstractComponentInfo) pastedObject;
          Rectangle bounds = model.getBounds();
          offsetX = Math.min(offsetX, bounds.x);
          offsetY = Math.min(offsetY, bounds.y);
          m_pastedComponents.put(model, new PastedComponentInfo(model));
          pastedModels.add(model);
        }
        // make union rectangle with relative bounds and prepare union rectangle feedback
        int i = 0;
        for (Entry<IAbstractComponentInfo, PastedComponentInfo> entry : m_pastedComponents.entrySet()) {
          PastedComponentInfo pastedComponent = entry.getValue();
          Rectangle bounds = pastedComponent.getComponent().getBounds();
          relativeBounds[i] =
              new Rectangle(bounds.x - offsetX, bounds.y - offsetY, bounds.width, bounds.height);
          widgetBounds.union(relativeBounds[i]);
          m_createFeedback.add(new OutlineImageFigure(pastedComponent.getComponent().getImage(),
              AbsolutePolicyUtils.COLOR_OUTLINE), relativeBounds[i]);
          pastedComponent.setBounds(relativeBounds[i]);
          i++;
        }
      } else {
        IAbstractComponentInfo model = (IAbstractComponentInfo) pastingComponents.get(0);
        //
        relativeBounds[0] = new Rectangle();
        widgetBounds = new Rectangle(new Point(0, 0), model.getBounds().getSize());
        m_pastedComponents.put(model, new PastedComponentInfo(model, widgetBounds.getCopy()));
        pastedModels.add(model);
        //
        m_createFeedback =
            new OutlineImageFigure(model.getImage(), AbsolutePolicyUtils.COLOR_OUTLINE);
      }
      addFeedback(m_createFeedback);
      widgetBounds.x = location.x - widgetBounds.width / 2;
      if (isFreezeVerticalAxis(request)) {
        widgetBounds.y = m_frozenYValue;
      } else {
        widgetBounds.y = m_frozenYValue = location.y - widgetBounds.height / 2;
      }
      // create dots feedback
      if (m_dotsFeedback == null) {
        if (useGridSnapping() || isKeyboardMoving()) {
          m_dotsFeedback = new DotsFeedback<C>(this, getHostFigure());
          addFeedback(m_dotsFeedback);
        }
      }
      placementsSupport.drag(
          request.getLocation(),
          ImmutableList.copyOf(pastedModels),
          widgetBounds,
          ImmutableList.copyOf(relativeBounds));
      widgetBounds = placementsSupport.getBounds();
      m_pasteLocation = widgetBounds.getLocation();
      translateModelToFeedback(widgetBounds);
      // update paste feedback
      m_createFeedback.setBounds(widgetBounds);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  @Override
  protected final Command getPasteCommand(PasteRequest request) {
    Command pasteCommand =
        GlobalState.getPasteRequestProcessor().getPasteCommand(
            request,
            new IPasteComponentProcessor() {
              public void process(Object component) throws Exception {
                doPasteComponent(m_pasteLocation, m_pastedComponents.get(component));
              }
            });
    EditCommand cleanupCommand = new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        m_pastedComponents = null;
        m_pasteLocation = null;
      }
    };
    CompoundCommand resultCommand = new CompoundEditCommand(m_layout.getUnderlyingModel());
    resultCommand.add(pasteCommand);
    resultCommand.add(cleanupCommand);
    return resultCommand;
  }

  /**
   * Class for keeping pasting information.
   */
  protected static final class PastedComponentInfo {
    private final IAbstractComponentInfo m_component;
    private Rectangle m_bounds;

    public PastedComponentInfo(IAbstractComponentInfo model, Rectangle bounds) {
      m_component = model;
      m_bounds = bounds;
    }

    public PastedComponentInfo(IAbstractComponentInfo model) {
      this(model, null);
    }

    public final Rectangle getBounds() {
      return m_bounds;
    }

    public final void setBounds(Rectangle bounds) {
      m_bounds = bounds;
    }

    public final IAbstractComponentInfo getComponent() {
      return m_component;
    }
  }

  /**
   * This method does some toolkit-specific as well as applying memento to pasted widget. Executed
   * from command's {@link EditCommand#executeEdit()} method.
   *
   * @param location
   *          the pasting location.
   * @param widget
   *          the widget being pasted.
   */
  protected abstract void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedComponent)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete/Reparent
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void onDelete(ObjectInfo child) throws Exception {
    placementsSupport.delete(ImmutableList.of((IAbstractComponentInfo) child));
  }

  @Override
  protected Command getOrphanCommand(GroupRequest request) {
    List<EditPart> editParts = request.getEditParts();
    final List<IAbstractComponentInfo> widgets = Lists.newArrayList();
    for (EditPart editPart : editParts) {
      widgets.add((IAbstractComponentInfo) editPart.getModel());
    }
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.delete(widgets);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getComponentGapValue(IAbstractComponentInfo component1,
      IAbstractComponentInfo component2,
      int direction) {
    switch (direction) {
      case IPositionConstants.LEFT :
        return getPreferenceStore().getInt(P_COMPONENT_GAP_LEFT);
      case IPositionConstants.RIGHT :
        return getPreferenceStore().getInt(P_COMPONENT_GAP_RIGHT);
      case IPositionConstants.TOP :
        return getPreferenceStore().getInt(P_COMPONENT_GAP_TOP);
      case IPositionConstants.BOTTOM :
        return getPreferenceStore().getInt(P_COMPONENT_GAP_BOTTOM);
    }
    return 6;
  }

  public int getContainerGapValue(IAbstractComponentInfo component, int direction) {
    switch (direction) {
      case IPositionConstants.LEFT :
        return getPreferenceStore().getInt(P_CONTAINER_GAP_LEFT);
      case IPositionConstants.RIGHT :
        return getPreferenceStore().getInt(P_CONTAINER_GAP_RIGHT);
      case IPositionConstants.TOP :
        return getPreferenceStore().getInt(P_CONTAINER_GAP_TOP);
      case IPositionConstants.BOTTOM :
        return getPreferenceStore().getInt(P_CONTAINER_GAP_BOTTOM);
    }
    return 10;
  }

  public Point getClientAreaOffset() {
    return new Point();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IFeedbackProxy
  //
  ////////////////////////////////////////////////////////////////////////////
  public Figure addHorizontalFeedbackLine(int y, int x, int width) {
    Polyline line = createLineFeedback(x, y, x + width, y);
    line.setForeground(AbsolutePolicyUtils.COLOR_FEEDBACK);
    return line;
  }

  public Figure addHorizontalMiddleLineFeedback(int y, int x, int width) {
    Polyline line = createLineFeedback(x, y, x + width, y);
    line.setForeground(AbsolutePolicyUtils.COLOR_FEEDBACK);
    line.setLineStyle(SWT.LINE_DASH);
    return line;
  }

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

  public Figure addVerticalFeedbackLine(int x, int y, int height) {
    Polyline line = createLineFeedback(x, y, x, y + height);
    line.setForeground(AbsolutePolicyUtils.COLOR_FEEDBACK);
    return line;
  }

  public Figure addVerticalMiddleLineFeedback(int x, int y, int height) {
    Polyline line = createLineFeedback(x, y, x, y + height);
    line.setForeground(AbsolutePolicyUtils.COLOR_FEEDBACK);
    line.setLineStyle(SWT.LINE_DASH);
    return line;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection actions
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract AbstractAlignmentActionsSupport<C> getAlignmentActionsSupport();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void translateModelToFeedback(Translatable t) {
    PolicyUtils.translateModelToFeedback(this, t);
  }

  protected void translateAbsoluteToModel(Translatable t) {
    PolicyUtils.translateAbsoluteToModel(this, t);
  }

  protected final PlacementsSupport getPlacementsSupport() {
    return placementsSupport;
  }
}