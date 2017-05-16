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

/**
 * A {@link Request} to create a new object.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class CreateRequest extends AbstractCreateRequest {
  private final ICreationFactory m_factory;
  private Object m_newObject;
  private Object m_selectObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a {@link CreateRequest} with the specified <i>type</i> and <i>factory</i>.
   */
  public CreateRequest(ICreationFactory factory) {
    super(Request.REQ_CREATE);
    m_factory = factory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Gets the new object from the factory and returns that object.
   */
  public Object getNewObject() {
    if (m_newObject == null) {
      m_newObject = m_factory.getNewObject();
      m_selectObject = m_newObject;
    }
    return m_newObject;
  }

  /**
   * @return the object that should be selected after finishing create operation. By default return
   *         same as {@link #getNewObject()}.
   */
  public Object getSelectObject() {
    return m_selectObject;
  }

  /**
   * Sets the object that should be selected after finishing create operation.
   */
  public void setSelectObject(Object object) {
    m_selectObject = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("CreateRequest(type=");
    buffer.append(getType());
    buffer.append(", stateMask=");
    buffer.append(getStateMask());
    buffer.append(", location=");
    buffer.append(getLocation());
    buffer.append(", size=");
    buffer.append(getSize());
    buffer.append(", factory=");
    buffer.append(m_factory);
    if (m_factory != null) {
      buffer.append("[object=");
      buffer.append(safeToString(m_factory.getNewObject()));
      buffer.append("]");
    }
    buffer.append(")");
    return buffer.toString();
  }
}