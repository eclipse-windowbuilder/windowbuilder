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

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public class PasteRequest extends AbstractCreateRequest {
  private final Object m_memento;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PasteRequest(Object memento) {
    super(Request.REQ_PASTE);
    m_memento = memento;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns object with paste info.
   */
  public Object getMemento() {
    return m_memento;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Created objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<?> m_objects = Collections.emptyList();

  /**
   * @return the {@link List} of pasted objects.
   */
  public List<?> getObjects() {
    return m_objects;
  }

  /**
   * Sets the {@link List} of pasted objects, these objects will be selected after paste.<br>
   * It is expected that handler for {@link PasteRequest} will invoke this method.
   */
  public void setObjects(List<?> objects) {
    m_objects = objects;
  }

  /**
   * Shortcut for {@link #setObjects(List)} for single object.
   */
  public void setObject(Object object) {
    List<Object> objects = Lists.newArrayList();
    objects.add(object);
    setObjects(objects);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("PasteRequest(type=");
    buffer.append(getType());
    buffer.append(", stateMask=");
    buffer.append(getStateMask());
    buffer.append(", location=");
    buffer.append(getLocation());
    buffer.append(", size=");
    buffer.append(getSize());
    buffer.append(", memento=");
    buffer.append(m_memento);
    buffer.append(")");
    return buffer.toString();
  }
}