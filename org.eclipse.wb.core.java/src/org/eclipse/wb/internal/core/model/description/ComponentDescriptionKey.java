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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang.ObjectUtils;

/**
 * The key for caching {@link ComponentDescription}.
 * <p>
 * We use such advanced key instead of just {@link Class} because sometimes we need "tweaked"
 * descriptions, for example for exposed children. We may expose {@link java.awt.Container}, that in
 * general has layout, but as developers we may know, that <em>this</em> {@link java.awt.Container}
 * should not have layout.
 * <p>
 * It is supposed that <code>host</code> is key of host {@link JavaInfo} description, and
 * <code>suffix</code> is method/field use for exposing.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentDescriptionKey {
  private final Class<?> m_componentClass;
  private final ComponentDescriptionKey m_host;
  private final String m_suffix;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentDescriptionKey(Class<?> componentClass) {
    this(componentClass, null, null);
  }

  public ComponentDescriptionKey(Class<?> componentClass,
      ComponentDescriptionKey host,
      String suffix) {
    Assert.isNotNull(componentClass);
    Assert.isLegal(
        !(host != null ^ suffix != null),
        "Host and suffix should both be null or not null.");
    m_componentClass = componentClass;
    m_host = host;
    m_suffix = suffix;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int hashCode() {
    return m_componentClass.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ComponentDescriptionKey) {
      ComponentDescriptionKey key = (ComponentDescriptionKey) obj;
      return m_componentClass == key.m_componentClass
          && ObjectUtils.equals(m_host, key.m_host)
          && ObjectUtils.equals(m_suffix, key.m_suffix);
    }
    return false;
  }

  @Override
  public String toString() {
    String s = "CDKey(";
    s += m_componentClass.getName();
    // some specialization of "host"
    if (m_host != null) {
      s += "," + m_host.toString() + "," + m_suffix;
    }
    // finalize toString()
    s += ")";
    return s;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the underlying component {@link Class}.
   */
  public Class<?> getComponentClass() {
    return m_componentClass;
  }

  /**
   * @return <code>true</code> if this {@link ComponentDescriptionKey} is pure component
   *         {@link Class}, not hosted by other {@link ComponentDescriptionKey}.
   */
  public boolean isPureComponent() {
    return m_host == null;
  }

  /**
   * @return the name of resource with {@link ComponentDescription}, excluding
   *         <code>.wbp-component.xml</code> extension.
   */
  public String getName() {
    if (m_host != null) {
      return m_host.getName() + "." + m_suffix;
    }
    return m_componentClass.getName().replace('.', '/');
  }
}
