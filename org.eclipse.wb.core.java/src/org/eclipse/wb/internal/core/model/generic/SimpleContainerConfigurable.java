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
package org.eclipse.wb.internal.core.model.generic;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Configurable {@link SimpleContainer} for {@link JavaInfo} children.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public final class SimpleContainerConfigurable implements SimpleContainer {
  private final JavaInfo m_container;
  private final SimpleContainerConfiguration m_configuration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleContainerConfigurable(JavaInfo container, SimpleContainerConfiguration configuration) {
    m_container = container;
    m_configuration = configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isEmpty() {
    return getChild() == null;
  }

  public Object getChild() {
    for (ObjectInfo child : getContainerChildren()) {
      if (validateComponent(child)) {
        return child;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateComponent(Object component) {
    return m_configuration.getComponentValidator().validate(m_container, component);
  }

  @SuppressWarnings("unchecked")
  private List<ObjectInfo> getContainerChildren() {
    {
      String signature = "getSimpleContainerChildren()";
      Method method = ReflectionUtils.getMethodBySignature(m_container.getClass(), signature);
      if (method != null) {
        return (List<ObjectInfo>) ReflectionUtils.invokeMethodEx(m_container, signature);
      }
    }
    return m_container.getChildren();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(Object newObject) throws Exception {
    if (!tryDuckTyping("command_CREATE", newObject)) {
      command_CREATE_default(newObject);
    }
    tryDuckTyping("command_CREATE_after", newObject);
    tryDuckTyping("command_TARGET_after", newObject);
  }

  public void command_ADD(Object moveObject) throws Exception {
    if (!tryDuckTyping("command_ADD", moveObject)) {
      command_ADD_default(moveObject);
    }
    tryDuckTyping("command_ADD_after", moveObject);
    tryDuckTyping("command_TARGET_after", moveObject);
  }

  private void command_CREATE_default(Object newObject) throws Exception {
    JavaInfo component = (JavaInfo) newObject;
    AssociationObject associationObject = createAssociationObject();
    JavaInfoUtils.add(component, associationObject, m_container, null);
  }

  private void command_ADD_default(Object moveObject) throws Exception {
    JavaInfo component = (JavaInfo) moveObject;
    AssociationObject associationObject = createAssociationObject();
    JavaInfoUtils.move(component, associationObject, m_container, null);
  }

  private AssociationObject createAssociationObject() {
    return m_configuration.getAssociationObjectFactory().create();
  }

  private boolean tryDuckTyping(String methodName, Object object) throws Exception {
    Method method = getCommandMethod(methodName, object);
    if (method != null) {
      method.invoke(m_container, object);
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Method} with given name that can be invoked with given parameter object.
   */
  private Method getCommandMethod(String methodName, Object object) {
    for (Method method : m_container.getClass().getMethods()) {
      if (method.getName().equals(methodName)) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
          if (ReflectionUtils.isAssignableFrom(parameterTypes[0], object)) {
            return method;
          }
        }
      }
    }
    return null;
  }
}
