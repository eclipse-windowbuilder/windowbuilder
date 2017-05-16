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
package org.eclipse.wb.internal.core.utils.xml;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Model for XML.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public final class Model {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Encoding
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_charset;

  /**
   * Sets the XML document charset.
   */
  public void setCharset(String charset) {
    m_charset = charset;
  }

  /**
   * @return the XML document charset, may be <code>null</code> if unknown.
   */
  public String getCharset() {
    return m_charset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IModelChangedListener> m_listeners = Lists.newArrayList();

  /**
   * Adds the listener to the list of listeners that will be notified on model changes.
   */
  public void addModelChangedListener(IModelChangedListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  /**
   * Takes the listener off the list of registered change listeners.
   */
  public void removeModelChangedListener(IModelChangedListener listener) {
    m_listeners.remove(listener);
  }

  /**
   * Delivers change event to all the registered listeners.
   */
  public void fireModelChanged(ModelChangedEvent event) {
    List<IModelChangedListener> listeners = Lists.newArrayList(m_listeners);
    for (IModelChangedListener listener : listeners) {
      listener.modelChanged(event);
    }
  }
}
