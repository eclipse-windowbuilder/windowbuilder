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

import org.eclipse.wb.draw2d.geometry.Dimension;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class AbstractCreateRequest extends LocationRequest implements IDropRequest {
  private Dimension m_size;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractCreateRequest() {
  }

  public AbstractCreateRequest(Object type) {
    super(type);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the size of the object to be created.
   */
  public Dimension getSize() {
    return m_size;
  }

  /**
   * Sets the size of the new object.
   */
  public void setSize(Dimension size) {
    m_size = size;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copies state from given {@link Request} into this one.
   */
  public void copyStateFrom(Request _source) {
    if (_source instanceof AbstractCreateRequest) {
      AbstractCreateRequest source = (AbstractCreateRequest) _source;
      setLocation(source.getLocation());
      setSize(source.getSize());
      setStateMask(source.getStateMask());
    }
  }
}