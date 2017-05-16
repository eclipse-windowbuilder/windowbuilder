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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Helper class for inspecting a JavaBean.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class IntrospectionHelper {
  private final Class<?> m_clazz;
  private List<BeanInfo> m_beanInfos;
  private BeanDescriptor m_beanDescriptor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IntrospectionHelper(Class<?> clazz) {
    m_clazz = clazz;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the consolidated {@link BeanDescriptor}.
   */
  public BeanDescriptor getBeanDescriptor() throws IntrospectionException {
    if (m_beanDescriptor == null) {
      // temporary data structure for bean descriptor
      class BeanDescData {
        String name;
        String displayName;
        String shortDescription;
        Class<?> customizerClass;
        Map<String, Object> values = Maps.newTreeMap();
      }
      BeanDescData beanDescData = new BeanDescData();
      // collect the bean descriptor data from all classes in supertype hierarchy
      for (BeanInfo info : getBeanInfos()) {
        BeanDescriptor desc = info.getBeanDescriptor();
        if (desc != null) {
          // set the name
          if (beanDescData.name == null) {
            beanDescData.name = desc.getName();
          }
          // set the display name
          if (beanDescData.displayName == null) {
            beanDescData.displayName = desc.getDisplayName();
          }
          // set the short description
          if (beanDescData.shortDescription == null) {
            beanDescData.shortDescription = desc.getShortDescription();
          }
          // set the customizer class
          if (beanDescData.customizerClass == null) {
            beanDescData.customizerClass = desc.getCustomizerClass();
          }
          // set the attribute values
          Enumeration<String> attrNames = desc.attributeNames();
          while (attrNames.hasMoreElements()) {
            String name = attrNames.nextElement();
            Object value = desc.getValue(name);
            if (!beanDescData.values.containsKey(name)) {
              beanDescData.values.put(name, value);
            }
          }
        }
      }
      // create a new bean descriptor with the collected data
      m_beanDescriptor = new BeanDescriptor(m_clazz, beanDescData.customizerClass);
      m_beanDescriptor.setName(beanDescData.name);
      m_beanDescriptor.setDisplayName(beanDescData.displayName);
      m_beanDescriptor.setShortDescription(beanDescData.shortDescription);
      for (Map.Entry<String, Object> entry : beanDescData.values.entrySet()) {
        String name = entry.getKey();
        Object value = entry.getValue();
        m_beanDescriptor.setValue(name, value);
      }
    }
    return m_beanDescriptor;
  }

  /**
   * @return the complete {@link List} of {@link BeanInfo} objects for the JavaBean and it's
   *         super-classes.
   */
  private List<BeanInfo> getBeanInfos() throws IntrospectionException {
    if (m_beanInfos == null) {
      m_beanInfos = Lists.newArrayList();
      for (Class<?> cls = m_clazz; cls != null; cls = cls.getSuperclass()) {
        BeanInfo info = Introspector.getBeanInfo(cls);
        m_beanInfos.add(info);
      }
    }
    return m_beanInfos;
  }
}
