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
 * A {@link Request} that needs to keep track of a location.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class LocationRequest extends Request {
  private Point m_location;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs an empty {@link LocationRequest}.
   */
  public LocationRequest() {
  }

  /**
   * Constructs a {@link LocationRequest} with the specified <i>type</i>.
   */
  public LocationRequest(Object type) {
    super(type);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the current location.
   */
  public Point getLocation() {
    return m_location;
  }

  /**
   * Sets the current location.
   */
  public void setLocation(Point location) {
    m_location = location;
  }
}