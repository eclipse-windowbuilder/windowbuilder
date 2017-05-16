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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets {@link Class} property with given name.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class SetClassPropertyRule extends Rule {
  private final ClassLoader m_classLoader;
  private final String m_attributeName;
  private final String m_propertyName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetClassPropertyRule(ClassLoader classLoader, String propertyName) {
    this(classLoader, propertyName, propertyName);
  }

  public SetClassPropertyRule(ClassLoader classLoader, String attributeName, String propertyName) {
    m_classLoader = classLoader;
    m_attributeName = attributeName;
    m_propertyName = propertyName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    // prepare class
    Class<?> clazz;
    {
      String className = attributes.getValue(m_attributeName);
      Assert.isNotNull(className);
      clazz = ReflectionUtils.getClassByName(m_classLoader, className);
    }
    // set property
    BeanUtilsBean.getInstance().getPropertyUtils().setProperty(
        digester.peek(),
        m_propertyName,
        clazz);
  }
}