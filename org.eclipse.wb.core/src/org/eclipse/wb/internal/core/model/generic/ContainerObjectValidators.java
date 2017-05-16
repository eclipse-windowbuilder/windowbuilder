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

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IDescriptionHelper;
import org.eclipse.wb.internal.core.utils.state.ILayoutRequestValidatorHelper;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Factory for {@link ContainerObjectValidator} objects.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public final class ContainerObjectValidators {
  public static ContainerObjectValidator alwaysTrue() {
    return new ContainerObjectValidator() {
      public boolean validate(Object container, Object object) {
        return true;
      }

      @Override
      public String toString() {
        return "alwaysTrue";
      }
    };
  }

  public static ContainerObjectValidator forList(final String[] types) {
    return new ContainerObjectValidator() {
      public boolean validate(Object container, Object object) {
        ILayoutRequestValidatorHelper validatorHelper = GlobalState.getValidatorHelper();
        if (validatorHelper.isComponent(object)) {
          IDescriptionHelper descriptionHelper = GlobalState.getDescriptionHelper();
          IComponentDescription description = descriptionHelper.getDescription(object);
          Class<?> componentClass = description.getComponentClass();
          for (String type : types) {
            if (ReflectionUtils.isSuccessorOf(componentClass, type)) {
              return true;
            }
          }
        }
        return false;
      }

      @Override
      public String toString() {
        return StringUtils.join(types, " ");
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // for*Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String DEF_functions = StringUtils.join(new String[]{
      "def isModelType(model, c) {",
      "  if (c is String) {",
      "    return ReflectionUtils.isSuccessorOf((Class) model.description.componentClass, c);",
      "  } else {",
      "    return c.isAssignableFrom(model.description.componentClass);",
      "  }",
      "};",
      "def isSuccessorOf(o, c) {",
      "  if (c is String) {",
      "    return ReflectionUtils.isSuccessorOf(o, c);",
      "  } else {",
      "    return o != null && c.isAssignableFrom(o.getClass());",
      "  }",
      "};",
      "def isComponentType(c) {",
      "  return isModelType(component, c);",
      "};",
      "def isReferenceType(c) {",
      "  return isModelType(reference, c);",
      "};",
      "def isContainerType(c) {",
      "  return isModelType(container, c);",
      "};",
      "def isContainerThis() {",
      "  return isSuccessorOf(container.creationSupport, "
          + "'org.eclipse.wb.internal.core.model.creation.ThisCreationSupport');",
      "};",}, "\n");

  public static ContainerObjectValidator forComponentExpression(final String expression) {
    return new ContainerObjectValidator() {
      public boolean validate(Object container, Object component) {
        ILayoutRequestValidatorHelper validatorHelper = GlobalState.getValidatorHelper();
        if (validatorHelper.isComponent(container) && validatorHelper.isComponent(component)) {
          Map<String, Object> variables = Maps.newTreeMap();
          variables.put("container", container);
          variables.put("component", component);
          return evaluate(expression, variables);
        }
        return false;
      }

      @Override
      public String toString() {
        return expression;
      }
    };
  }

  public static ContainerObjectValidator forReferenceExpression(final String expression) {
    return new ContainerObjectValidator() {
      public boolean validate(Object container, Object reference) {
        ILayoutRequestValidatorHelper validatorHelper = GlobalState.getValidatorHelper();
        if (validatorHelper.isComponent(container) && validatorHelper.isComponent(reference)) {
          Map<String, Object> variables = Maps.newTreeMap();
          variables.put("container", container);
          variables.put("reference", reference);
          return evaluate(expression, variables);
        }
        return false;
      }

      @Override
      public String toString() {
        return expression;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Container
  //
  ////////////////////////////////////////////////////////////////////////////
  public static boolean validateContainer(Object container, String expression) {
    return validateContainer(expression, container);
  }

  private static boolean validateContainer(String expression, Object container) {
    Map<String, Object> variables = Maps.newTreeMap();
    variables.put("container", container);
    return evaluate(expression, variables);
  }

  public static Predicate<Object> forContainerExpression(final String expression) {
    return new Predicate<Object>() {
      public boolean apply(Object container) {
        ILayoutRequestValidatorHelper validatorHelper = GlobalState.getValidatorHelper();
        if (validatorHelper.isComponent(container)) {
          return validateContainer(expression, container);
        }
        return false;
      }

      @Override
      public String toString() {
        return expression;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean evaluate(String expression, Map<String, Object> variables) {
    String e = DEF_functions + expression;
    return (Boolean) ScriptUtils.evaluate(e, variables);
  }
}
