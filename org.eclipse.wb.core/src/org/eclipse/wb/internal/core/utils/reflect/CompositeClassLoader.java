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
package org.eclipse.wb.internal.core.utils.reflect;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link ClassLoader} that is composed of other {@link ClassLoader}s'. Each {@link ClassLoader}
 * will be used to try to load the particular class, until one of them succeeds. <b>Note:</b> The
 * loaders will always be called in the REVERSE order they were added in.
 * 
 * @author scheglov_ke
 * @coverage core.util
 */
public class CompositeClassLoader extends ClassLoader {
  private final List<ClassLoader> m_classLoaders = new ArrayList();
  private final List/*<List<String>>*/m_classNamespaces = new ArrayList();
  private final List/*<List<String>>*/m_resourceNamespaces = new ArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of underlying {@link ClassLoader}'s.
   */
  public List getClassLoaders() {
    return m_classLoaders;
  }

  /**
   * Adds new {@link ClassLoader} into composition.
   * 
   * @param namespaces
   *          the {@link List} of {@link String} prefixes for package name (with '.' dots). May be
   *          <code>null</code>, in this case this {@link ClassLoader} will be always used for
   *          attempt to load {@link Class}.
   */
  public void add(ClassLoader classLoader, List namespaces) {
    add(classLoader, namespaces, namespaces);
  }

  /**
   * Adds new {@link ClassLoader} into composition.
   * 
   * @param resourcePrefixes
   *          the {@link List} of {@link String} prefixes for package name (with '.' dots). May be
   *          <code>null</code>, in this case this {@link ClassLoader} will be always used for
   *          attempt to load {@link Class}.
   */
  public void add(ClassLoader classLoader, List classPrefixes, List resourcePrefixes) {
    if (classLoader != null) {
      m_classLoaders.add(0, classLoader);
      m_classNamespaces.add(0, classPrefixes);
      // resource namespace: convert to use '/' instead of '.' to do this only one time
      if (resourcePrefixes != null) {
        List resourceNamespaces = new ArrayList();
        for (Iterator iterator = resourcePrefixes.iterator(); iterator.hasNext();) {
          String namespace = (String) iterator.next();
          resourceNamespaces.add(namespace.replace('.', '/'));
        }
        m_resourceNamespaces.add(0, resourceNamespaces);
      } else {
        m_resourceNamespaces.add(0, null);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Package getPackage(String name) {
    try {
      Method method_getPackage =
          ClassLoader.class.getDeclaredMethod("getPackage", new Class[]{String.class});
      method_getPackage.setAccessible(true);
      for (int i = 0; i < m_classLoaders.size(); i++) {
        ClassLoader classLoader = m_classLoaders.get(i);
        Package pkg = (Package) method_getPackage.invoke(classLoader, new Object[]{name});
        if (pkg != null) {
          return pkg;
        }
      }
    } catch (Throwable e) {
    }
    return super.getPackage(name);
  }

  @Override
  public URL getResource(String name) {
    for (int i = 0; i < m_classLoaders.size(); i++) {
      ClassLoader classLoader = m_classLoaders.get(i);
      // check namespace
      {
        List namespaces = (List) m_resourceNamespaces.get(i);
        if (!hasNamespace(name, namespaces)) {
          continue;
        }
      }
      // try to find
      URL resource = classLoader.getResource(name);
      if (resource != null) {
        return resource;
      }
    }
    // not found
    return null;
  }

  @Override
  protected Enumeration findResources(String name) throws IOException {
    Set allResources = new HashSet();
    for (Iterator iterator = m_classLoaders.iterator(); iterator.hasNext();) {
      ClassLoader classLoader = (ClassLoader) iterator.next();
      Enumeration resources = classLoader.getResources(name);
      CollectionUtils.addAll(allResources, resources);
    }
    return new IteratorEnumeration(allResources.iterator());
  }

  @Override
  protected Class findClass(String name) throws ClassNotFoundException {
    for (int i = 0; i < m_classLoaders.size(); i++) {
      ClassLoader classLoader = m_classLoaders.get(i);
      // check namespace
      {
        List namespaces = (List) m_classNamespaces.get(i);
        if (!hasNamespace(name, namespaces)) {
          continue;
        }
      }
      // try to load
      try {
        return classLoader.loadClass(name);
      } catch (ClassNotFoundException notFound) {
        // OK... try another one
      }
    }
    // not found
    throw new ClassNotFoundException(name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given name is in one of the given namespace's. Always returns
   *         <code>true</code> if given namespace's is <code>null</code>.
   */
  private static boolean hasNamespace(String name, List/*<String>*/namespaces) {
    if (namespaces != null) {
      for (Iterator iterator = namespaces.iterator(); iterator.hasNext();) {
        String namespace = (String) iterator.next();
        if (name.startsWith(namespace)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }
}