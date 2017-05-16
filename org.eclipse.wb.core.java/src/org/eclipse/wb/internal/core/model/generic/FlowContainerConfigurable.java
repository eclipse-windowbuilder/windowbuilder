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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Configurable {@link FlowContainer} for {@link JavaInfo} children.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public final class FlowContainerConfigurable implements FlowContainer {
  private final JavaInfo m_container;
  private final FlowContainerConfiguration m_configuration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlowContainerConfigurable(JavaInfo container, FlowContainerConfiguration configuration) {
    m_container = container;
    m_configuration = configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isHorizontal() {
    return m_configuration.getHorizontalPredicate().apply(m_container);
  }

  public boolean isRtl() {
    return m_configuration.getRtlPredicate().apply(m_container);
  }

  public String getGroupName() {
    return m_configuration.getGroupName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateComponent(Object component) {
    return m_configuration.getComponentValidator().validate(m_container, component);
  }

  public boolean validateReference(Object reference) {
    return m_configuration.getReferenceValidator().validate(m_container, reference);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(Object newObject, Object referenceObject) throws Exception {
    if (!tryDuckTyping("command_CREATE", newObject, referenceObject)) {
      command_CREATE_default(newObject, referenceObject);
    }
    tryDuckTyping("command_CREATE_after", newObject, referenceObject);
    tryDuckTyping("command_TARGET_after", newObject, referenceObject);
  }

  public void command_MOVE(Object moveObject, Object referenceObject) throws Exception {
    if (!tryDuckTyping("command_MOVE", moveObject, referenceObject)) {
      command_MOVE_default(moveObject, referenceObject);
    }
    tryDuckTyping("command_MOVE_after", moveObject, referenceObject);
    tryDuckTyping("command_TARGET_after", moveObject, referenceObject);
  }

  private void command_CREATE_default(Object newObject, Object referenceObject) throws Exception {
    JavaInfo component = (JavaInfo) newObject;
    JavaInfo nextComponent = (JavaInfo) referenceObject;
    AssociationObject associationObject = createAssociationObject();
    JavaInfoUtils.add(component, associationObject, m_container, nextComponent);
  }

  private void command_MOVE_default(Object moveObject, Object referenceObject) throws Exception {
    JavaInfo component = (JavaInfo) moveObject;
    JavaInfo nextComponent = (JavaInfo) referenceObject;
    AssociationObject associationObject = createAssociationObject();
    JavaInfoUtils.move(component, associationObject, m_container, nextComponent);
  }

  private AssociationObject createAssociationObject() {
    return m_configuration.getAssociationObjectFactory().create();
  }

  private boolean tryDuckTyping(String methodName, Object object, Object referenceObject)
      throws Exception {
    Method method = getCommandMethod(methodName, object, referenceObject);
    if (method != null) {
      method.invoke(m_container, object, referenceObject);
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
   * @return the {@link Method} with given name that can be invoked with object/referenceObject.
   */
  private Method getCommandMethod(String methodName, Object object, Object referenceObject) {
    List<Method> methods = Lists.newArrayList();
    for (Method method : m_container.getClass().getMethods()) {
      if (method.getName().equals(methodName)) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 2) {
          if (ReflectionUtils.isAssignableFrom(parameterTypes[0], object)
              && ReflectionUtils.isAssignableFrom(parameterTypes[1], referenceObject)) {
            methods.add(method);
          }
        }
      }
    }
    return ReflectionUtils.getMostSpecific(methods);
  }
}
