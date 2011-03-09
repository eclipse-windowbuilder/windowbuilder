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
package org.eclipse.wb.internal.css.semantics;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Abstract value.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public abstract class AbstractValue implements IValueEventsProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractValue(AbstractSemanticsComposite composite) {
    composite.addValueEventsProvider(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IValueListener> m_listeners = Lists.newArrayList();

  public final void addListener(IValueListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  public final void removeListener(IValueListener listener) {
    m_listeners.remove(listener);
  }

  protected final void notifyListeners() {
    for (IValueListener listener : m_listeners) {
      listener.changed(this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void clear() {
    set(null);
  }

  /**
   * Assigns given string from CSS into this value.
   */
  public abstract void set(String value);

  /**
   * @return CSS string from this value.
   */
  public abstract String get();
}
