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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentGroup;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentInGroup;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import java.util.List;

/**
 * Helper for support correct creation flow for absolute layouts. In order according left-to-right
 * and top-to-bottom direction.
 *
 * @author sablin_aa
 * @coverage core.model.util
 */
public final class AbsoluteLayoutCreationFlowSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbsoluteLayoutCreationFlowSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Correct creation position according grid order.
   *
   * @param container
   *          the {@link IAbstractComponentInfo} container has absolute layout.
   * @param cildrenComponents
   *          the list of {@link IAbstractComponentInfo} all children components in container.
   * @param component
   *          the {@link ComponentInfo} which modifications applies to.
   * @param location
   *          the {@link Point} of new location of component.
   * @param size
   *          the {@link Dimension} of new size of component. May be null.
   */
  public static void apply(IAbstractComponentInfo container,
      List<? extends IAbstractComponentInfo> childrenComponents,
      IAbstractComponentInfo component,
      Point newLocation,
      Dimension newSize) throws Exception {
    Assert.isTrue(childrenComponents.contains(component));
    Assert.isNotNull(newLocation);
    // EnvironmentUtils.isTestingTime()
    {
      // force set bounds (for drops on tree & testing time)
      checkBounds(component, newLocation, newSize);
    }
    // prepare columns and rows and distribute controls in them
    List<ComponentGroup> columns = GridConvertionHelper.buildGroups(childrenComponents, true);
    List<ComponentGroup> rows = GridConvertionHelper.buildGroups(childrenComponents, false);
    // sort components in columns and rows
    GridConvertionHelper.sortGroupsByTranspose(columns, rows);
    GridConvertionHelper.sortGroupsByTranspose(rows, columns);
    // ensure that columns and rows are sorted by start coordinates
    GridConvertionHelper.sortGroups(columns);
    GridConvertionHelper.sortGroups(rows);
    // calculate begin/end for each column/row
    GridConvertionHelper.updateBoundsGaps(columns, false);
    GridConvertionHelper.updateBoundsGaps(rows, false);
    // check grid position
    IAbstractComponentInfo nextControl = getReferenceComponent(rows, component);
    if (nextControl != GenericsUtils.getNextOrNull(childrenComponents, component)) {
      GlobalState.getOrderProcessor().move(component, nextControl);
    }
  }

  /**
   * Check for new bounds is applied for component.
   */
  public static void checkBounds(IAbstractComponentInfo component, Point location, Dimension size) {
    if (size == null) {
      Rectangle bounds = component.getModelBounds();
      size = bounds != null ? bounds.getSize() : new Dimension(1, 1);
    } else {
      size = size.getCopy();
    }
    // force set bounds
    component.setModelBounds(new Rectangle(location.getCopy(), size));
  }

  /**
   * Locate reference component.
   */
  private static IAbstractComponentInfo getReferenceComponent(List<ComponentGroup> rows,
      IAbstractComponentInfo component) {
    boolean componentArrived = false;
    for (ComponentGroup row : rows) {
      for (ComponentInGroup componentInGroup : row.getComponents()) {
        if (componentArrived) {
          return componentInGroup.getComponent();
        }
        if (componentInGroup.getComponent() == component) {
          componentArrived = true;
        }
      }
    }
    return null;
  }
}
