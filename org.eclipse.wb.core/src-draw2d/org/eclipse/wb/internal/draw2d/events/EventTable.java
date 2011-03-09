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
package org.eclipse.wb.internal.draw2d.events;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class EventTable {
  private final Map<Class<?>, List<?>> m_listenerClassToListener = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Registers the given listener for given class.
   */
  public <T extends Object> void addListener(Class<T> listenerClass, T listener) {
    List<T> listeners = getListenersImpl(listenerClass);
    if (listeners == null) {
      listeners = Lists.newArrayList();
      m_listenerClassToListener.put(listenerClass, listeners);
    }
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Unregisters the given listener.
   */
  public <T extends Object> void removeListener(Class<T> listenerClass, T listener) {
    List<T> listeners = getListenersImpl(listenerClass);
    if (listeners != null) {
      listeners.remove(listener);
    }
  }

  /**
   * @return the unmodifiable {@link List} of registered listeners for given class or
   *         <code>null</code>.
   */
  public <T extends Object> List<T> getListeners(Class<T> listenerClass) {
    List<T> listeners = getListenersImpl(listenerClass);
    return listeners == null
        ? Collections.<T>emptyList()
        : Collections.unmodifiableList(new ArrayList<T>(listeners));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of registered listeners for given class or <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  private <T extends Object> List<T> getListenersImpl(Class<T> listenerClass) {
    return (List<T>) m_listenerClassToListener.get(listenerClass);
  }
}