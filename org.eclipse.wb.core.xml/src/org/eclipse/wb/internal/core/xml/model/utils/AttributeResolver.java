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
package org.eclipse.wb.internal.core.xml.model.utils;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.apache.commons.lang.StringUtils;

/**
 * Helper for attribute name resolving. Searches for namespace in {@link ComponentDescription} and
 * adds it to attribute name, if applicable.
 * 
 * @author mitin_aa
 * @coverage XML.model.utils
 */
public class AttributeResolver extends NamespacesHelper {
  private final XmlObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AttributeResolver(XmlObjectInfo object) {
    super(object.getElement().getRoot());
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Searches for namespace in {@link ComponentDescription} and adds it to attribute name, if
   * applicable.
   */
  public String getResolvedAttribute(String attribute) {
    return getNamespace() + attribute;
  }

  private String getNamespace() {
    ComponentDescription description = m_object.getDescription();
    String namespace = description.getPropertyAttributeXmlns();
    if (StringUtils.isEmpty(namespace)) {
      return "";
    }
    return getName(namespace) + ":";
  }

  public String getLocalAttribute(String attribute) {
    if (StringUtils.isEmpty(attribute)) {
      return attribute;
    }
    String namespace = getNamespace();
    if (StringUtils.isEmpty(namespace)) {
      return attribute;
    }
    if (attribute.startsWith(namespace)) {
      return attribute.substring(namespace.length());
    }
    return attribute;
  }

  /**
   * Searches for namespace and adds it to attribute name, if applicable.
   */
  public static String getResolved(XmlObjectInfo object, String attrName) {
    return new AttributeResolver(object).getResolvedAttribute(attrName);
  }
}
