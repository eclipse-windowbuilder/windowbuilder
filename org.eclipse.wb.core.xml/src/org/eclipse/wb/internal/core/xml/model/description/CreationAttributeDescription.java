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

import org.eclipse.wb.internal.core.utils.check.Assert;

/**
 * {@link CreationDescription} consists of tag name and attributes to set on creation.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class CreationAttributeDescription extends AbstractDescription {
  private final String m_namespace;
  private final String m_name;
  private final String m_value;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationAttributeDescription(String namespace, String name, String value) {
    m_namespace = namespace;
    m_name = name;
    m_value = value;
    Assert.isNotNull(name, "No x-attribute name.");
    Assert.isNotNull(value, "No x-attribute value.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the optional namespace, may be <code>null</code>.
   */
  public String getNamespace() {
    return m_namespace;
  }

  /**
   * @return the simple name of attribute, not <code>null</code>.
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return the name of attribute, not <code>null</code>.
   */
  public String getValue() {
    return m_value;
  }
}
