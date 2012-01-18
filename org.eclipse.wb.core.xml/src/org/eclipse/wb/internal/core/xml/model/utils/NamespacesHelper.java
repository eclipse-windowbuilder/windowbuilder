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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Helper for working with namespaces.
 * 
 * @author scheglov_ke
 * @coverage XML.model.utils
 */
public class NamespacesHelper {
  protected final DocumentElement m_rootElement;
  protected final Set<String> m_names = Sets.newHashSet();
  protected final Map<String, String> m_nameForURI = Maps.newHashMap();
  protected final Map<String, String> m_uriForName = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NamespacesHelper(DocumentElement element) {
    m_rootElement = element;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares information about existing namespaces.
   */
  public void prepareNamespaces() {
    m_names.clear();
    m_nameForURI.clear();
    for (DocumentAttribute attribute : m_rootElement.getDocumentAttributes()) {
      String name = attribute.getName();
      if (name.startsWith("xmlns")) {
        name = StringUtils.removeStart(name, "xmlns");
        name = StringUtils.removeStart(name, ":");
        m_nameForURI.put(attribute.getValue(), name);
        m_uriForName.put(name, attribute.getValue());
        m_names.add(name);
      }
    }
  }

  /**
   * @return the unique name for new namespace.
   */
  public String generateUniqueName(String base) {
    String name;
    int index = 1;
    do {
      name = base + index;
      index++;
    } while (m_names.contains(name));
    return name;
  }

  /**
   * Adds new namespace declaration.
   */
  public void add(String name, String uri) {
    m_rootElement.setAttribute("xmlns:" + name, uri);
  }

  /**
   * @return the URI of given namespace
   */
  public String getURI(String name) {
    prepareNamespaces();
    return m_uriForName.get(name);
  }

  /**
   * @return the existing name given given URI, may be <code>null</code> if does not exist.
   */
  public String getName(String uri) {
    prepareNamespaces();
    return m_nameForURI.get(uri);
  }

  /**
   * @return the existing name given given URI or generate and add new name.
   */
  public String ensureName(String uri, String base) {
    prepareNamespaces();
    // try to find existing namespace declaration
    {
      String name = m_nameForURI.get(uri);
      if (name != null) {
        return name;
      }
    }
    // add new namespace
    {
      String name = getNewName(uri, base);
      add(name, uri);
      return name;
    }
  }

  /**
   * @return the new name of namespace of given URI. Subclasses can override this to provide better
   *         names for known toolkit packages.
   */
  protected String getNewName(String uri, String base) {
    return generateUniqueName(base);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Static utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param element
   *          some {@link DocumentElement} attached to root.
   * @param name
   *          the name of namespace.
   * 
   * @return the URI of given namespace name.
   */
  public static String getURI(DocumentElement element, String name) {
    NamespacesHelper helper = new NamespacesHelper(element.getRoot());
    return helper.getURI(name);
  }

  /**
   * @param element
   *          some {@link DocumentElement} attached to root.
   * @param uri
   *          the URI of namespace.
   * 
   * @return the existing name of given URI, may be <code>null</code> if does not exist.
   */
  public static String getName(DocumentElement element, String uri) {
    NamespacesHelper helper = new NamespacesHelper(element.getRoot());
    return helper.getName(uri);
  }

  /**
   * @param element
   *          some {@link DocumentElement} attached to root.
   * @param uri
   *          the URI of namespace.
   * @param base
   *          the base of name to generate.
   * 
   * @return the existing name of given URI or generate and add new name.
   */
  public static String ensureName(DocumentElement element, String uri, String base) {
    NamespacesHelper helper = new NamespacesHelper(element.getRoot());
    return helper.ensureName(uri, base);
  }
}
