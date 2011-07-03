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
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
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
  private final List<HierarchyElement> m_roots = Lists.newArrayList();
  private final Map<String, HierarchyElement> m_nameToElement = Maps.newHashMap();
  private final PropertiesSupport m_propertiesSupport;
  private final boolean m_addProperties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HierarchySupport(PropertiesSupport propertiesSupport, boolean addProperties) {
    m_propertiesSupport = propertiesSupport;
    m_addProperties = addProperties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addClass(ClassInfo classInfo) throws Exception {
    loadClass(classInfo);
  }

  private HierarchyElement loadClass(ClassInfo classInfo) throws Exception {
    if (classInfo.thisClass == null) {
      return null;
    }
    String className = classInfo.thisClass.getName();
    HierarchyElement element = m_nameToElement.get(className);
    if (element == null) {
      element = loadClassHierarchy(classInfo.thisClass);
    }
    element.classInfo = classInfo;
    if (m_addProperties) {
      element.properties.addAll(classInfo.properties);
    }
    return element;
  }

  private HierarchyElement loadClassHierarchy(Class<?> clazz) throws Exception {
    HierarchyElement element = new HierarchyElement();
    String className = clazz.getName();
    String packageName = CodeUtils.getPackage(className);
    m_nameToElement.put(className, element);
    for (Class<?> superClass : clazz.getInterfaces()) {
      String superClassName = superClass.getName();
      HierarchyElement superElement = m_nameToElement.get(superClassName);
      if (superElement == null) {
        String superPackageName = CodeUtils.getPackage(superClassName);
        if (superPackageName.equals(packageName)) {
          superElement = loadClassHierarchy(superClass);
        } else {
          ClassInfo superClassInfo = m_propertiesSupport.getClassInfo(superClass);
          if (superClassInfo != null) {
            superElement = loadClass(superClassInfo);
          } else {
            superElement = loadClassHierarchy(superClass);
          }
          m_roots.add(superElement);
        }
      }
      superElement.elements.add(element);
    }
    return element;
  }

  public void joinClasses() {
    for (HierarchyElement root : m_roots) {
      joinClass(root);
    }
    for (HierarchyElement root : m_roots) {
      sortProperties(root);
    }
  }

  private void joinClass(HierarchyElement element) {
    if (element.classInfo != null) {
      for (HierarchyElement childElement : element.elements) {
        for (PropertyInfo property : element.classInfo.properties) {
          if (childElement.properties.add(property) && childElement.classInfo != null) {
            childElement.classInfo.properties.add(property);
            childElement.sort = true;
          }
        }
      }
    }
    for (HierarchyElement childElement : element.elements) {
      joinClass(childElement);
    }
  }

  private void sortProperties(HierarchyElement element) {
    if (element.sort && element.classInfo != null) {
      element.sort = false;
      Collections.sort(element.classInfo.properties, new Comparator<PropertyInfo>() {
        public int compare(PropertyInfo property1, PropertyInfo property2) {
          return property1.name.compareTo(property2.name);
        }
      });
    }
    for (HierarchyElement childElement : element.elements) {
      sortProperties(childElement);
    }
  }

  public ClassInfo getLastClass() {
    List<HierarchyElement> elements = m_roots;
    while (true) {
      Assert.isTrue(!elements.isEmpty());
      HierarchyElement element = elements.get(0);
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
  private static class HierarchyElement {
    ClassInfo classInfo;
    Set<PropertyInfo> properties = Sets.newHashSet();
    List<HierarchyElement> elements = Lists.newArrayList();
    boolean sort;
  }
}