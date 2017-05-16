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
package org.eclipse.wb.internal.core.gef.policy.layout.generic;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IParametersProvider;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;

/**
 * Helper for installing {@link SelectionEditPolicy} using "duck typing".
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class SelectionEditPolicyInstaller {
  private final Object container;
  private final String containerClassName;
  private final EditPart childPart;
  private final Object child;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionEditPolicyInstaller(Object _container, EditPart _childPart) {
    container = _container;
    containerClassName = container.getClass().getName();
    childPart = _childPart;
    child = childPart.getModel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Try to find {@link SelectionEditPolicy} and use to decorate child {@link EditPart}.
   */
  public void decorate() {
    IParametersProvider parametersProvider = GlobalState.getParametersProvider();
    // use main package
    {
      String packageName = StringUtils.substringBeforeLast(containerClassName, ".model.");
      if (decorate(packageName)) {
        return;
      }
    }
    // use "related" packages
    for (int i = 1; i < 10; i++) {
      String parameterName = "GEF.relatedToolkitPackages." + i;
      String packageName = parametersProvider.getParameter(container, parameterName);
      if (packageName != null) {
        if (decorate(packageName)) {
          return;
        }
      }
    }
    // use default policy
    {
      String policyClassName =
          parametersProvider.getParameter(container, "GEF.defaultSelectionPolicy");
      if (!StringUtils.isBlank(policyClassName)) {
        policyClassName = policyClassName.trim();
        try {
          ClassLoader classLoader = container.getClass().getClassLoader();
          Class<?> policyClass = classLoader.loadClass(policyClassName);
          SelectionEditPolicy policy = createPolicy(policyClass);
          if (policy != null) {
            childPart.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
            return;
          }
        } catch (Throwable e) {
        }
      }
    }
  }

  /**
   * @return <code>true</code> if policy {@link Class} was found and loaded.
   */
  private boolean decorate(String packageName) {
    // prepare policy Class
    Class<?> policyClass = loadClass(packageName);
    if (policyClass == null) {
      return false;
    }
    // install policy
    SelectionEditPolicy policy = createPolicy(policyClass);
    if (policy != null) {
      childPart.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
    // policy was loaded
    return true;
  }

  /**
   * Loads policy {@link Class} from given package and its parents.
   */
  private Class<?> loadClass(String basePackageName) {
    // name, based on container Class name
    String containerName;
    {
      containerName = StringUtils.substringAfterLast(containerClassName, ".model.");
      containerName = StringUtils.removeEnd(containerName, "Info");
    }
    // containerName + childName
    {
      String childClassName = child.getClass().getName();
      String childName = StringUtils.substringAfterLast(childClassName, ".");
      childName = StringUtils.removeEnd(childName, "Info");
      //
      Class<?> clazz = loadClass2(basePackageName, containerName + childName);
      if (clazz != null) {
        return clazz;
      }
    }
    // only containerName
    {
      Class<?> clazz = loadClass2(basePackageName, containerName);
      if (clazz != null) {
        return clazz;
      }
    }
    // no class
    return null;
  }

  /**
   * Loads policy {@link Class} with given name, from given package and its parents.
   */
  private Class<?> loadClass2(String basePackageName, String name) {
    String baseClassName = basePackageName + ".gef.policy." + name + "SelectionEditPolicy";
    String packageName = StringUtils.substringBeforeLast(baseClassName, ".");
    String className = StringUtils.substringAfterLast(baseClassName, ".");
    // check package and its parents
    ClassLoader classLoader = container.getClass().getClassLoader();
    while (packageName.contains(".")) {
      try {
        String policyClassName = packageName + "." + className;
        return classLoader.loadClass(policyClassName);
      } catch (Throwable e) {
      }
      packageName = StringUtils.substringBeforeLast(packageName, ".");
    }
    // no class
    return null;
  }

  /**
   * Attempts to create {@link SelectionEditPolicy} instance using different constructor variants,
   * may return <code>null</code>.
   */
  private SelectionEditPolicy createPolicy(Class<?> selectionPolicyClass) {
    Constructor<?> constructor = selectionPolicyClass.getConstructors()[0];
    // container model + child model
    try {
      return (SelectionEditPolicy) constructor.newInstance(container, child);
    } catch (Throwable e) {
    }
    // only child model
    try {
      return (SelectionEditPolicy) constructor.newInstance(child);
    } catch (Throwable e) {
    }
    // only container model
    try {
      return (SelectionEditPolicy) constructor.newInstance(container);
    } catch (Throwable e) {
    }
    // no arguments
    try {
      return (SelectionEditPolicy) constructor.newInstance();
    } catch (Throwable e) {
    }
    // fail
    return null;
  }
}
