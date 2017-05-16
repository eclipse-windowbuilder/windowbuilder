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
package org.eclipse.wb.internal.core.nls;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;

import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for externalized {@link String}'s.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class NlsStringEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if ("java.lang.String".equals(typeQualifiedName)) {
      AstEditor editor = (AstEditor) context.getArbitraryValue(JavaInfoEvaluationHelper.KEY_EDITOR);
      if (editor != null) {
        JavaInfo root = (JavaInfo) editor.getGlobalValue(JavaInfoParser.KEY_ROOT);
        if (root != null) {
          String value = NlsSupport.get(root).getValue(expression);
          return value != null ? value : AstEvaluationEngine.UNKNOWN;
        } else if (expression instanceof MethodInvocation || expression instanceof QualifiedName) {
          @SuppressWarnings("unchecked")
          List<JavaInfo> components =
              (List<JavaInfo>) editor.getGlobalValue(JavaInfoParser.KEY_COMPONENTS);
          Assert.isTrue(!components.isEmpty(), "Components should have at least one JavaInfo.");
          String value = NlsSupport.getValue(components.get(0), expression);
          return value != null ? value : AstEvaluationEngine.UNKNOWN;
        }
      }
    }
    // no ASTEditor or JavaInfo, can not access NLSSupport
    return AstEvaluationEngine.UNKNOWN;
  }
}
