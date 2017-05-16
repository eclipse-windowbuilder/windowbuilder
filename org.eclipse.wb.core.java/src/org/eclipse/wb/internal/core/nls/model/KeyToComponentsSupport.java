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
package org.eclipse.wb.internal.core.nls.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class that encapsulates information about keys and components that have properties with
 * these keys.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class KeyToComponentsSupport {
  private final Map<String, Set<JavaInfo>> m_keyToComponents = Maps.newTreeMap();

  /*private final IJavaInfoListener m_javaInfoListener = new IJavaInfoListener() {
   public void deleted(JavaInfo component) {
   // remove deleted component from key -> components map
   for (Iterator I = m_keyToComponents.values().iterator(); I.hasNext();) {
   List components = (List) I.next();
   components.remove(component);
   }
   }
   };*/
  //private final boolean m_useListener;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public KeyToComponentsSupport(boolean useListener) {
    //m_useListener = useListener;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the copy of this {@link KeyToComponentsSupport}.
   */
  public KeyToComponentsSupport getCopy(boolean useListener) {
    KeyToComponentsSupport copy = new KeyToComponentsSupport(useListener);
    for (Map.Entry<String, Set<JavaInfo>> entry : m_keyToComponents.entrySet()) {
      String key = entry.getKey();
      Set<JavaInfo> components = entry.getValue();
      //
      Set<JavaInfo> componentsCopy = new HashSet<JavaInfo>(components);
      copy.m_keyToComponents.put(key, componentsCopy);
    }
    return copy;
  }

  /**
   * @return the {@link Set} of components for that have externalized properties with given key.
   */
  public Set<JavaInfo> getComponentsByKey(String key) {
    Set<JavaInfo> components = m_keyToComponents.get(key);
    if (components != null) {
      return components;
    }
    return Collections.emptySet();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update source on key operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds correspondence between given component and key.
   */
  public void add(JavaInfo component, String key) {
    // prepare components
    Set<JavaInfo> components = m_keyToComponents.get(key);
    if (components == null) {
      components = Sets.newHashSet();
      m_keyToComponents.put(key, components);
    }
    // add component
    components.add(component);
    /*if (m_useListener) {
     component.addListener(m_javaInfoListener);
     }*/
  }

  /**
   * Moves components from one key to another.
   */
  public void rename(String oldKey, String newKey) {
    Set<JavaInfo> sourceComponents = m_keyToComponents.remove(oldKey);
    if (sourceComponents != null) {
      Set<JavaInfo> targetComponents = m_keyToComponents.get(newKey);
      if (targetComponents == null) {
        m_keyToComponents.put(newKey, sourceComponents);
      } else {
        targetComponents.addAll(sourceComponents);
      }
    }
  }

  /**
   * Removes key.
   */
  public void remove(String key) {
    m_keyToComponents.remove(key);
  }

  /**
   * Removes component for key.
   */
  public void remove(JavaInfo component, String key) {
    Set<JavaInfo> components = m_keyToComponents.get(key);
    components.remove(component);
  }
}
