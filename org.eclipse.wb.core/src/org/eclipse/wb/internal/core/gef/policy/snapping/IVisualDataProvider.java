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
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;

/**
 * Used for providing visual data for support of widget snapping while designing absolute-based
 * layouts.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public interface IVisualDataProvider {
  /**
   * @param component
   *          A component model for which baseline value is requested. May not be null. Should
   *          return {@link #NO_BASELINE} in case if the component has no baseline.
   * @return Baseline value.
   */
  int getBaseline(IAbstractComponentInfo component);

  /**
   * @param component
   *          A component model for which gap to border of parent container is returned. May not be
   *          null.
   * @param direction
   *          Determines a side of parent container for which gap value is requested. Valid values
   *          are {@link IPositionConstants#LEFT}, {@link IPositionConstants#RIGHT},
   *          {@link IPositionConstants#TOP}, {@link IPositionConstants#BOTTOM}.
   * @return Gap value for container.
   */
  int getContainerGapValue(IAbstractComponentInfo component, int direction);

  /**
   * Returns gap value between two components.
   *
   * @param component1
   *          First component model, may not be null.
   * @param component2
   *          Second component model, may not be null.
   * @param direction
   *          Determines a side of component1 in which gap to component2 is requested.
   * @return Gap value between component1 and component2.
   */
  int getComponentGapValue(IAbstractComponentInfo component1,
      IAbstractComponentInfo component2,
      int direction);

  /**
   * @return Current size of parent container.
   */
  Dimension getContainerSize();

  /**
   * @return the top-left offset at which container's client area starts.
   */
  Point getClientAreaOffset();

  /**
   * @param component
   *          A component which preferred size is requested. May not be null.
   * @return Component's preferred size or null if component preferred size cannot be retrieved.
   */
  Dimension getComponentPreferredSize(IAbstractComponentInfo component);

  /**
   * @return a X grid step value for this layout
   */
  int getGridStepX();

  /**
   * @return a Y grid step value for this layout
   */
  int getGridStepY();

  /**
   * @return <code>true</code> if "free mode" snapping enabled for the layout
   */
  boolean useFreeSnapping();

  /**
   * @return <code>true</code> if grid snapping enabled for the layout
   */
  boolean useGridSnapping();

  /**
   * @return <code>true</code> when snapping should be disabled
   */
  boolean isSuppressingSnapping();
}