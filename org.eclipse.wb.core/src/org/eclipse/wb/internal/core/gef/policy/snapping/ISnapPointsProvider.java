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

import java.util.List;

/**
 * Provider of the {@link SnapPoint}'s.
 *
 * @author mitin_aa
 */
public interface ISnapPointsProvider {
  /**
   * Return list of the snap points for <code>target</code> component. Called for every component
   * provided in <code>allComponents</code> argument in the SnapPoints constructor.
   *
   * @param target
   *          the component for which returned snap points are related.
   * @param isHorizontal
   *          by vertical or horizontal dimension.
   */
  List<SnapPoint> forComponent(IAbstractComponentInfo target, boolean isHorizontal);

  /**
   * Return list of the snap points for container of the components.
   *
   * @param isHorizontal
   *          by vertical or horizontal dimension.
   */
  List<SnapPoint> forContainer(boolean isHorizontal);
}
