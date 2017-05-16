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
package org.eclipse.wb.core.gef.policy;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.border.CompoundBorder;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.ObjectUtils;

import java.lang.reflect.Method;

/**
 * Helper for {@link EditPolicy}'s.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class PolicyUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Border target feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BORDER_FEEDBACK_KEY = "PolicyUtils.borderFeedback";

  /**
   * Shows border around given {@link EditPolicy} host figure.
   */
  public static void showBorderTargetFeedback(final GraphicalEditPolicy policy) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        Layer layer = (Layer) ReflectionUtils.invokeMethod2(policy, "getFeedbackLayer");
        showBorderTargetFeedback(layer, policy.getHost());
      }
    });
  }

  /**
   * Shows border around given {@link GraphicalEditPart} figure.
   */
  public static void showBorderTargetFeedback(GraphicalEditPart part) {
    Layer feedbackLayer = part.getViewer().getLayer(IEditPartViewer.FEEDBACK_LAYER);
    showBorderTargetFeedback(feedbackLayer, part);
  }

  /**
   * Shows border around given {@link GraphicalEditPart} figure.
   */
  private static void showBorderTargetFeedback(Layer layer, GraphicalEditPart part) {
    // we can show only one "border target", so remove any existing
    eraseBorderTargetFeedback(part);
    // prepare border
    Border targetBorder = createTargetBorder();
    // prepare bounds
    Rectangle bounds;
    {
      Figure hostFigure = part.getFigure();
      bounds = hostFigure.getBounds().getCopy();
      bounds.expand(3, 3);
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
    }
    // add feedback
    Figure borderFeedback = new Figure();
    borderFeedback.setBorder(targetBorder);
    borderFeedback.setBounds(bounds);
    layer.add(borderFeedback);
    part.getViewer().getControl().setData(BORDER_FEEDBACK_KEY, borderFeedback);
  }

  /**
   * Erases border feedback.
   */
  public static void eraseBorderTargetFeedback(GraphicalEditPolicy policy) {
    GraphicalEditPart part = policy.getHost();
    eraseBorderTargetFeedback(part);
  }

  /**
   * Erases border feedback.
   */
  public static void eraseBorderTargetFeedback(GraphicalEditPart part) {
    Figure borderFeedback = (Figure) part.getViewer().getControl().getData(BORDER_FEEDBACK_KEY);
    if (borderFeedback != null) {
      FigureUtils.removeFigure(borderFeedback);
      borderFeedback = null;
    }
  }

  /**
   * @return the {@link Border} for target feedback.
   */
  public static Border createTargetBorder() {
    LineBorder darkBorder = new LineBorder(IColorConstants.darkGreen, 1);
    LineBorder darkBorder2 = new LineBorder(IColorConstants.darkGreen, 1);
    LineBorder lightBorder = new LineBorder(IColorConstants.lightGreen, 1);
    return new CompoundBorder(new CompoundBorder(darkBorder, lightBorder), darkBorder2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utility methods for policy methods accessing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invokes {@link EditPolicy#getLayer(java.lang.String)}.
   */
  private static Layer getLayer(GraphicalEditPolicy policy, String name) throws Exception {
    Method method = findPolicyMethod(policy, "getLayer(java.lang.String)");
    return (Layer) method.invoke(policy, new Object[]{name});
  }

  /**
   * @return an possible unaccessible (protected) method from given {@link EditPolicy} object.
   */
  private static Method findPolicyMethod(GraphicalEditPolicy policy, String signature)
      throws Exception {
    return ReflectionUtils.getMethodBySignature(policy.getClass(), signature);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return absolute bounds of given {@link EditPart}'s {@link Figure}.
   */
  public static Rectangle getAbsoluteBounds(GraphicalEditPart editPart) {
    Figure figure = editPart.getFigure();
    Rectangle bounds = figure.getBounds().getCopy();
    FigureUtils.translateFigureToAbsolute(figure, bounds);
    return bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Geometry utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void translateAbsoluteToFeedback(GraphicalEditPolicy policy, Translatable t) {
    try {
      Figure layer = getLayer(policy, IEditPartViewer.FEEDBACK_LAYER);
      FigureUtils.translateAbsoluteToFigure(layer, t);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  public static void translateAbsoluteToModel(SelectionEditPolicy policy, Translatable t) {
    try {
      GraphicalEditPart containerEditPart = (GraphicalEditPart) policy.getHost().getParent();
      translateAbsoluteToModel(containerEditPart, t);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  public static void translateAbsoluteToModel(LayoutEditPolicy policy, Translatable t) {
    try {
      GraphicalEditPart containerEditPart = policy.getHost();
      translateAbsoluteToModel(containerEditPart, t);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Converts 'absolute' coordinates into coordinates by <code>toContainer</code> {@link EditPart}.
   * Useful during reparenting.
   */
  public static void translateAbsoluteToModel(GraphicalEditPart toContainer, Translatable t) {
    try {
      // translate to figure
      {
        Figure hostFigure = toContainer.getFigure();
        FigureUtils.translateAbsoluteToFigure2(hostFigure, t);
      }
      // translate: container figure -> client area
      {
        IAbstractComponentInfo container = (IAbstractComponentInfo) toContainer.getModel();
        absoluteToModel_rightToLeft(t, container);
        t.translate(container.getClientAreaInsets().getNegated());
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Translates given {@link Translatable} from model coordinates into feedback layer coordinates.
   */
  public static void translateModelToFeedback(GraphicalEditPolicy policy, Translatable t) {
    if (policy instanceof LayoutEditPolicy) {
      translateAbsoluteToModel((LayoutEditPolicy) policy, t);
    } else if (policy instanceof SelectionEditPolicy) {
      translateAbsoluteToModel((SelectionEditPolicy) policy, t);
    } else {
      throw new IllegalArgumentException(ObjectUtils.toString(policy));
    }
  }

  public static void translateModelToFeedback(LayoutEditPolicy policy, Translatable t) {
    try {
      GraphicalEditPart containerEditPart = policy.getHost();
      // translate: client area -> container figure
      {
        IAbstractComponentInfo container = (IAbstractComponentInfo) containerEditPart.getModel();
        t.translate(container.getClientAreaInsets());
        modelToFeedback_rightToLeft(t, container);
      }
      // translate to layer
      {
        Figure hostFigure = containerEditPart.getFigure();
        Figure layer = getLayer(policy, IEditPartViewer.FEEDBACK_LAYER);
        FigureUtils.translateFigureToFigure2(hostFigure, layer, t);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  public static void translateModelToFeedback(SelectionEditPolicy policy, Translatable t) {
    try {
      GraphicalEditPart containerEditPart = (GraphicalEditPart) policy.getHost().getParent();
      // translate: client area -> container figure
      {
        IAbstractComponentInfo container = (IAbstractComponentInfo) containerEditPart.getModel();
        t.translate(container.getClientAreaInsets());
        modelToFeedback_rightToLeft(t, container);
      }
      // translate to layer
      {
        Figure hostFigure = containerEditPart.getFigure();
        Figure layer = getLayer(policy, IEditPartViewer.FEEDBACK_LAYER);
        FigureUtils.translateFigureToFigure2(hostFigure, layer, t);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Right-to-left support
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void modelToFeedback_rightToLeft(Translatable t, IAbstractComponentInfo container) {
    if (container.isRTL()) {
      int containerWidth = container.getBounds().width;
      if (t instanceof Point) {
        Point point = (Point) t;
        point.x = containerWidth - point.x;
      }
      if (t instanceof Rectangle) {
        Rectangle rectangle = (Rectangle) t;
        rectangle.x = containerWidth - rectangle.x - rectangle.width;
      }
    }
  }

  private static void absoluteToModel_rightToLeft(Translatable t, IAbstractComponentInfo container) {
    if (container.isRTL()) {
      int containerWidth = container.getBounds().width;
      if (t instanceof Point) {
        Point point = (Point) t;
        point.x = containerWidth - point.x;
      }
      if (t instanceof Rectangle) {
        Rectangle rectangle = (Rectangle) t;
        rectangle.x = containerWidth - rectangle.x - rectangle.width;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Schedules selection of {@link EditPart} with given model.
   *
   * @param host
   *          the {@link EditPolicy} to get {@link IEditPartViewer} to select in.
   */
  public static void scheduleSelection(EditPolicy policy, Object model) {
    scheduleSelection(policy.getHost(), model);
  }

  /**
   * Schedules selection of {@link EditPart} with given model.
   *
   * @param host
   *          the {@link EditPart} to get {@link IEditPartViewer} to select in.
   */
  public static void scheduleSelection(EditPart host, Object model) {
    scheduleSelection(host.getViewer(), model);
  }

  /**
   * Schedules selection of {@link EditPart} with given model.
   */
  public static void scheduleSelection(final IEditPartViewer viewer, final Object model) {
    ExecutionUtils.runLogLater(new RunnableEx() {
      public void run() throws Exception {
        EditPart editPart = viewer.getEditPartByModel(model);
        if (editPart != null) {
          viewer.select(editPart);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Direction analysis
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if <code>direction</code> has <code>requiredDirection</code>.
   */
  public static boolean hasDirection(int direction, int requiredDirection) {
    return (direction & requiredDirection) != 0;
  }
}
