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
package org.eclipse.wb.internal.core.xml.model.description;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import java.util.Collections;
import java.util.Map;

/**
 * Abstract superclass for description objects.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public abstract class AbstractDescription implements IAdaptable {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link XmlObjectInfo} toolkit object was initialized and ready to be used. For example, it is
   * possible to get default values for properties.
   */
  public static final int STATE_OBJECT_READY = 1;

  /**
   * Sends this {@link AbstractDescription} message that given {@link XmlObjectInfo} is now in given
   * state, so some action can be done. This message will be also transmitted to all children of
   * this description object.
   */
  public void visit(XmlObjectInfo object, int state) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  public <T> T getAdapter(Class<T> adapter) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tags
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, String> m_tags;

  /**
   * @return the {@link Map} of "tag name" to "tag value". Can return empty {@link Map}, but not
   *         <code>null</code>.
   */
  public final Map<String, String> getTags() {
    if (m_tags == null) {
      return Collections.emptyMap();
    }
    return m_tags;
  }

  /**
   * @return the value of given tag, may be <code>null</code>.
   */
  public final String getTag(String tag) {
    return getTags().get(tag);
  }

  /**
   * @return <code>true</code> if this {@link AbstractDescription} has given tag with value
   *         <code>"true"</code>.
   */
  public final boolean hasTrueTag(String tag) {
    return "true".equals(getTag(tag));
  }

  /**
   * Sets the value of tag.
   */
  public final void putTag(String tag, String value) {
    if (m_tags == null) {
      m_tags = Maps.newTreeMap();
    }
    m_tags.put(tag, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary values map
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<Object, Object> m_arbitraryMap;

  /**
   * Associates the given value with the given key.
   */
  public final void putArbitraryValue(Object key, Object value) {
    if (m_arbitraryMap == null) {
      m_arbitraryMap = Maps.newHashMap();
    }
    m_arbitraryMap.put(key, value);
  }

  /**
   * @return the value to which the given key is mapped, or <code>null</code>.
   */
  public final Object getArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      return m_arbitraryMap.get(key);
    }
    return null;
  }
}
