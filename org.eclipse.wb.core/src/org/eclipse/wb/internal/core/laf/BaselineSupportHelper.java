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
package org.eclipse.wb.internal.core.laf;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import java.util.List;

/**
 * Helper class for baseline support. See {@link IBaselineSupport}.
 *
 * @author mitin_aa
 * @coverage core.laf
 */
public final class BaselineSupportHelper {
  private BaselineSupportHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the instances of {@link IBaselineSupport}.
   */
  private static List<IBaselineSupport> getBaselineSupports() throws Exception {
    return ExternalFactoriesHelper.getElementsInstances(
        IBaselineSupport.class,
        "org.eclipse.wb.core.baselineSupport",
        "support");
  }

  /**
   * Iterates through contributed {@link IBaselineSupport} instances and returns the baseline offset
   * from top of component.
   *
   * @param component
   *          the visual element of some GUI toolkit.
   * @return the baseline offset from top of component.
   */
  public static int getBaseline(Object component) {
    try {
      for (IBaselineSupport support : getBaselineSupports()) {
        int baseline = support.getBaseline(component);
        if (baseline != IBaselineSupport.NO_BASELINE) {
          return baseline;
        }
      }
    } catch (Throwable e) {
      // ignore exceptions
    }
    return IBaselineSupport.NO_BASELINE;
  }
}
