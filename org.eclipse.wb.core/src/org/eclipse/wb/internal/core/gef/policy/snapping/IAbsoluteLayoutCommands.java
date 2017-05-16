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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Intended for {@link PlacementsSupport} to operate with layout.
 *
 * For every method containing <code>side</code> argument the valid <code>side</code> is one of the
 * following: {@link IPositionConstants#LEFT}, {@link IPositionConstants#RIGHT},
 * {@link IPositionConstants#TOP}, {@link IPositionConstants#BOTTOM},
 * {@link IPositionConstants#BASELINE}.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public interface IAbsoluteLayoutCommands {
  /**
   * Determines whether the <code>widget</code> is attached by <code>side</code> to another widget
   * or parent container.
   */
  boolean isAttached(IAbstractComponentInfo widget, int side) throws Exception;

  /**
   * Removes the attachment by <code>side</code> for <code>widget</code>.
   */
  void detach(IAbstractComponentInfo widget, int side) throws Exception;

  /**
   * Attaches the <code>widget</code> to another <code>attachToWidget</code> in sequential position.
   *
   * @param widget
   *          the source widget to be attached.
   * @param attachToWidget
   *          the target widget for <code>widget</code> to be attached.
   * @param side
   *          the side of the source widget to be attached. Note, in sequential position the source
   *          widget's leading side would be attached to trailing side of target widget and vice
   *          versa.
   * @param distance
   *          the distance between widgets, usually this value is similar to
   *          {@link LayoutStyle#getPreferredGap()} or the same.
   */
  void attachWidgetSequientially(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachToWidget,
      int side,
      int distance) throws Exception;

  /**
   * Attaches the <code>widget</code> to another <code>attachToWidget</code> in parallel position.
   *
   * @param widget
   *          the source widget to be attached.
   * @param attachToWidget
   *          the target widget for <code>widget</code> to be attached.
   * @param side
   *          the side of the source widget to be attached. Note, in parallel position the source
   *          widget's leading side would be attached to leading side of target widget and same for
   *          trailing sides.
   * @param distance
   *          the distance between same sides widgets, ex., offset between left side of
   *          <code>widget</code> and left side of <code>attachToWidget</code>.
   */
  void attachWidgetParallelly(IAbstractComponentInfo widget,
      IAbstractComponentInfo attachToWidget,
      int side,
      int distance) throws Exception;

  /**
   * Attaches the <code>widget</code> to another <code>attachToWidget</code> in baseline position.
   *
   * @param widget
   *          the source widget to be attached.
   * @param attachToWidget
   *          the target widget for <code>widget</code> to be attached.
   */
  void attachWidgetBaseline(IAbstractComponentInfo widget, IAbstractComponentInfo attachedToWidget)
      throws Exception;

  /**
   * @param side
   *          the side of the widget which attachment to adjust.
   * @param moveDelta
   *          the value to adjust the distance. delta < 0 if widget moved/resized in leading
   *          direction (no matter it grows or shrinks while resizing), otherwise the widget
   *          moved/resized in trailing direction.
   */
  void adjustAttachmentOffset(IAbstractComponentInfo widget, int side, int moveDelta)
      throws Exception;

  /**
   * Binds the <code>widget</code> by <code>side</code> inside parent container.
   *
   * @param widget
   *          the widget to be bound.
   * @param side
   *          the side of the widget to be bound.
   * @param distance
   *          the distance to the container's boundary in direction determined by <code>side</code>.
   */
  void attachAbsolute(IAbstractComponentInfo widget, int side, int distance) throws Exception;

  /**
   * Returns sibling widget to which <code>widget</code> attached by given <code>side</code> to
   * another widget. Otherwise returns <code>null</code>. Parent container is not sibling, so also
   * <code>null</code> returned.
   *
   * @param widget
   *          the widget to check.
   * @param side
   *          the side of the widget to be checked.
   */
  IAbstractComponentInfo getAttachedToWidget(IAbstractComponentInfo widget, int side)
      throws Exception;

  /**
   * Explicitly sets the side of the <code>widget</code>. In this case the size of the widget
   * wouldn't be ruled by the layout (in those layouts which allows is).
   *
   * @param widget
   *          the widget to set size.
   * @param side
   *          the side of the widget which is currently attached.
   * @param draggingSide
   *          the side of the widget currently dragged by.
   * @param resizeDelta
   *          the value to change the size of the widget.
   *
   */
  void setExplicitSize(IAbstractComponentInfo widget, int side, int draggingSide, int resizeDelta)
      throws Exception;

  /**
   *
   * TODO: comment on IAbsoluteLayoutCommands.performAction()
   *
   * Not implemented yet and subject to change.
   *
   * @param actionId
   */
  void performAction(int actionId);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Stub
  //
  ////////////////////////////////////////////////////////////////////////////
  IAbsoluteLayoutCommands EMPTY = new AbsoluteLayoutCommandsStub();

  public class AbsoluteLayoutCommandsStub implements IAbsoluteLayoutCommands {
    public IAbstractComponentInfo getAttachedToWidget(IAbstractComponentInfo widget, int side)
        throws Exception {
      return null;
    }

    public void adjustAttachmentOffset(IAbstractComponentInfo widget, int side, int moveDelta)
        throws Exception {
    }

    public void attachAbsolute(IAbstractComponentInfo widget, int side, int distance)
        throws Exception {
    }

    public void attachWidgetParallelly(IAbstractComponentInfo widget,
        IAbstractComponentInfo attachToComponent,
        int side,
        int distance) throws Exception {
    }

    public void attachWidgetSequientially(IAbstractComponentInfo widget,
        IAbstractComponentInfo attachToComponent,
        int side,
        int distance) throws Exception {
    }

    public void attachWidgetBaseline(IAbstractComponentInfo widget,
        IAbstractComponentInfo attachedToWidget) {
    }

    public void detach(IAbstractComponentInfo widget, int side) throws Exception {
    }

    public boolean isAttached(IAbstractComponentInfo widget, int side) throws Exception {
      return false;
    }

    public void setExplicitSize(IAbstractComponentInfo widget,
        int side,
        int draggingSide,
        int resizeDelta) throws Exception {
    }

    public void lockSide(IAbstractComponentInfo widget, int side) throws Exception {
    }

    public void performAction(int actionId) {
    }
  }
}
