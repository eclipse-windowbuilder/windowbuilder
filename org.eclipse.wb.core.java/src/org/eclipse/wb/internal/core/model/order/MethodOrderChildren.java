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
package org.eclipse.wb.internal.core.model.order;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Base {@link MethodOrder} to add {@link MethodInvocation} relative to children {@link JavaInfo}'s
 * of specified types.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class MethodOrderChildren extends MethodOrder {
  private final String[] m_childrenTypeNames;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodOrderChildren(String childrenTypeNames) {
    m_childrenTypeNames =
        "*".equals(childrenTypeNames) || StringUtils.isEmpty(childrenTypeNames)
            ? null
            : StringUtils.split(childrenTypeNames);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodOrder
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReference(JavaInfo javaInfo) {
    return true;
  }

  /**
   * @return last typed (managed by this order) child {@link JavaInfo} of given parent.
   */
  protected JavaInfo getLastChild(JavaInfo javaInfo) throws Exception {
    return GenericsUtils.getLastOrNull(getTargetChildren(javaInfo));
  }

  /**
   * @return list typed (managed by this order) children {@link JavaInfo}'s of given parent.
   */
  protected List<JavaInfo> getTargetChildren(JavaInfo javaInfo) throws Exception {
    List<JavaInfo> list = Lists.newArrayList();
    for (JavaInfo child : javaInfo.getChildrenJava()) {
      if (isTargetChild(child)) {
        list.add(child);
      }
    }
    return list;
  }

  /**
   * @return <code>true</code> if given {@link JavaInfo} is managed by this order.
   */
  public boolean isTargetChild(JavaInfo child) {
    if (m_childrenTypeNames == null) {
      // all children accepted
      return true;
    }
    Class<?> componentClass = child.getDescription().getComponentClass();
    for (String childTypeName : m_childrenTypeNames) {
      if (ReflectionUtils.isSuccessorOf(componentClass, childTypeName)) {
        return true;
      }
    }
    return false;
  }
}
