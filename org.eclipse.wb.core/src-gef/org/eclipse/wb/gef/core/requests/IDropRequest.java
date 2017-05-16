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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.draw2d.geometry.Point;

/**
 * A {@link Request} that requires a location in order to drop an item.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IDropRequest {
  /**
   * Returns the current mouse location.
   */
  Point getLocation();
}