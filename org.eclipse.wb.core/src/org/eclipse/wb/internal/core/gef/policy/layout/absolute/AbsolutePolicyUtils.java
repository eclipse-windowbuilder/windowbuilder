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

import org.eclipse.wb.draw2d.IColorConstants;

import org.eclipse.swt.graphics.Color;

/**
 * Utilities related to absolute layouts.
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public final class AbsolutePolicyUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constants
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Color COLOR_FEEDBACK = IColorConstants.lightBlue;
  public static final Color COLOR_OUTLINE = IColorConstants.orange;
  public static final int DEFAULT_COMPONENT_GAP = 6;
  public static final int DEFAULT_CONTAINER_GAP = 6;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Private constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbsolutePolicyUtils() {
  }
}
