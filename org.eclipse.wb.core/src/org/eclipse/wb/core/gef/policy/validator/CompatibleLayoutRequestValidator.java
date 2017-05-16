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
package org.eclipse.wb.core.gef.policy.validator;

import com.google.common.collect.Maps;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * {@link ILayoutRequestValidator} that checks also that given parent/child objects are compatible.
 * See for details {@link LayoutRequestValidatorUtils}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class CompatibleLayoutRequestValidator extends AbstractLayoutRequestValidator {
  private static final ILayoutRequestValidator INSTANCE0 = new CompatibleLayoutRequestValidator();
  public static final ILayoutRequestValidator INSTANCE =
      new CachingLayoutRequestValidator(INSTANCE0);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private CompatibleLayoutRequestValidator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean validate(EditPart host, Object child) {
    return areCompatible(host, child);
  }

  @Override
  protected boolean validateDescription(EditPart host, IComponentDescription childDescription) {
    return areCompatible(host, childDescription);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given parent and child objects are compatible.
   */
  private static boolean areCompatible(final EditPart host, final Object child) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        Object parent = host.getModel();
        return parentAgreeToAcceptChild(parent, child)
            && childAgreeToBeDroppedOnParent(parent, child);
      }
    },
        false);
  }

  private static boolean parentAgreeToAcceptChild(Object parent, Object child) throws Exception {
    // ask parent script, if it accepts child
    {
      String script = getParameter(parent, "GEF.requestValidator.parent");
      if (script != null) {
        return executeScriptBoolean(script, parent, child);
      }
    }
    // OK
    return true;
  }

  private static boolean childAgreeToBeDroppedOnParent(Object parent, Object child)
      throws Exception {
    // ask child script, if it can be dropped
    {
      String script = getParameter(child, "GEF.requestValidator.child");
      if (script != null) {
        if (!executeScriptBoolean(script, parent, child)) {
          return false;
        }
      }
    }
    // ask "child" if it can be placed on "parent"
    if (!GlobalState.getValidatorHelper().canUseParentForChild(parent, child)) {
      return false;
    }
    // OK
    return true;
  }

  /**
   * @param object
   *          the component or its description.
   *
   * @return the value of parameter with given name.
   */
  private static String getParameter(Object object, String name) {
    return GlobalState.getParametersProvider().getParameter(object, name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Scripts
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String DEF_functions = StringUtils.join(new String[]{
      "def isComponentType(model, c) {",
      "  if (ReflectionUtils.isSuccessorOf(model, 'org.eclipse.wb.core.model.ObjectInfo')) {",
      "    return ReflectionUtils.isSuccessorOf(model.description.componentClass, c);",
      "  } else {",
      "    return ReflectionUtils.isSuccessorOf(model.componentClass, c);",
      "  }",
      "};",}, "\n");

  private static boolean executeScriptBoolean(String script, Object parent, Object child)
      throws Exception {
    Map<String, Object> variables = Maps.newTreeMap();
    variables.put("parent", parent);
    variables.put("child", child);
    return (Boolean) ScriptUtils.evaluate(DEF_functions + script, variables);
  }
}
