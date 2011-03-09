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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.ClassInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for build EMF classes hierarchy.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class HierarchySupport {
  private final List<TreeElement> m_roots = Lists.newArrayList();
  private final Map<String, TreeElement> m_nameToElement = Maps.newHashMap();
  private final PropertiesSupport m_propertiesSupport;
  private final String m_packagePrefix;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HierarchySupport(PropertiesSupport propertiesSupport, String packagePrefix) {
    m_propertiesSupport = propertiesSupport;
    m_packagePrefix = packagePrefix;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addClass(ClassInfo classInfo, boolean addProperties) throws Exception {
    if (classInfo.thisClass == null) {
      return;
    }
    String className = classInfo.thisClass.getName();
    TreeElement element = m_nameToElement.get(className);
    if (element == null) {
      element = new TreeElement();
      m_nameToElement.put(className, element);
      for (Class<?> superClass : classInfo.thisClass.getInterfaces()) {
        linkClass(element, superClass);
      }
    }
    element.classInfo = classInfo;
    if (addProperties) {
      element.properties.addAll(classInfo.properties);
    }
  }

  private void linkClass(TreeElement element, Class<?> superClass) throws Exception {
    String superClassName = superClass.getName();
    TreeElement superElement = m_nameToElement.get(superClassName);
    if (superElement == null) {
      superElement = new TreeElement();
      m_nameToElement.put(superClassName, superElement);
      if (superClassName.startsWith(m_packagePrefix)) {
        for (Class<?> superSuperClass : superClass.getInterfaces()) {
          linkClass(superElement, superSuperClass);
        }
      } else {
        superElement.classInfo = m_propertiesSupport.getClassInfo(superClass);
        m_roots.add(superElement);
      }
    }
    superElement.elements.add(element);
  }

  public void joinClasses() {
    for (TreeElement root : m_roots) {
      joinClass(root);
    }
    for (TreeElement root : m_roots) {
      sortProperties(root);
    }
  }

  private void joinClass(TreeElement element) {
    if (element.classInfo != null) {
      for (TreeElement childElement : element.elements) {
        for (PropertyInfo property : element.classInfo.properties) {
          if (childElement.properties.add(property) && childElement.classInfo != null) {
            childElement.classInfo.properties.add(property);
            childElement.sort = true;
          }
        }
      }
    }
    for (TreeElement childElement : element.elements) {
      joinClass(childElement);
    }
  }

  private void sortProperties(TreeElement element) {
    if (element.sort && element.classInfo != null) {
      element.sort = false;
      Collections.sort(element.classInfo.properties, new Comparator<PropertyInfo>() {
        public int compare(PropertyInfo property1, PropertyInfo property2) {
          return property1.name.compareTo(property2.name);
        }
      });
    }
    for (TreeElement childElement : element.elements) {
      sortProperties(childElement);
    }
  }

  public ClassInfo getLastClass() {
    List<TreeElement> elements = m_roots;
    while (true) {
      Assert.isTrue(!elements.isEmpty());
      TreeElement element = elements.get(0);
      if (element.elements.isEmpty()) {
        Assert.isNotNull(element.classInfo);
        return element.classInfo;
      }
      elements = element.elements;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class TreeElement {
    public ClassInfo classInfo;
    public Set<PropertyInfo> properties = Sets.newHashSet();
    public List<TreeElement> elements = Lists.newArrayList();
    public boolean sort;
  }
}