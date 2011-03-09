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
package org.eclipse.wb.internal.css.model.root;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Model object for CSS.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public final class Model {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IModelChangedListener> m_listeners = Lists.newArrayList();

  /**
   * Adds {@link IModelChangedListener} to the list of listeners that will be notified on model
   * changes.
   */
  public void addModelChangedListener(IModelChangedListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  /**
   * Removes {@link IModelChangedListener} from the list of registered change listeners.
   */
  public void removeModelChangedListener(IModelChangedListener listener) {
    m_listeners.remove(listener);
  }

  /**
   * Delivers {@link ModelChangedEvent} to all the registered listeners.
   */
  public void fireModelChanged(ModelChangedEvent event) {
    List<IModelChangedListener> listeners = ImmutableList.copyOf(m_listeners);
    for (IModelChangedListener listener : listeners) {
      listener.modelChanged(event);
    }
  }
}
