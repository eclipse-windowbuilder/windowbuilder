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
package org.eclipse.wb.internal.core.databinding.parser;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public abstract class AbstractParser implements IModelResolver {
  protected final AstEditor m_editor;
  protected final IDatabindingsProvider m_provider;
  protected final List<ISubParser> m_subParsers = Lists.newArrayList();
  protected final List<IModelSupport> m_modelSupports = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractParser(AstEditor editor, IDatabindingsProvider provider) {
    m_editor = editor;
    m_provider = provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void parseMethod(MethodDeclaration method) {
    method.accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation creation) {
        try {
          // prepare signature
          String signature = CoreUtils.getCreationSignature(creation);
          // prepare arguments
          Expression[] arguments = CoreUtils.getExpressionArray(DomGenerics.arguments(creation));
          if (failExpressions(arguments)) {
            return;
          }
          // ask each parser, maybe this creation is ASTObjectInfo creation
          for (ISubParser subParser : m_subParsers) {
            AstObjectInfo model =
                subParser.parseExpression(
                    m_editor,
                    signature,
                    creation,
                    arguments,
                    AbstractParser.this,
                    m_provider);
            //
            if (model != null) {
              addModel(model, creation);
              return;
            }
          }
        } catch (Throwable e) {
          ReflectionUtils.propagate(e);
        }
      }

      @Override
      public void endVisit(MethodInvocation invocation) {
        try {
          // prepare signature
          String signature = CoreUtils.getMethodSignature(invocation);
          if (signature == null) {
            return;
          }
          // prepare arguments
          Expression[] arguments = CoreUtils.getExpressionArray(DomGenerics.arguments(invocation));
          if (failExpressions(arguments)) {
            return;
          }
          // prepare invocation expression
          Expression expression = invocation.getExpression();
          if (expression != null) {
            AstObjectInfo model = getModel(expression);
            // ask expression model, maybe this invocation is ASTObjectInfo creation
            if (model != null) {
              model =
                  model.parseExpression(
                      m_editor,
                      signature,
                      invocation,
                      arguments,
                      AbstractParser.this,
                      m_provider);
              //
              if (model != null) {
                addModel(model, invocation);
                return;
              }
            }
          }
          // ask each parser, maybe this invocation is ASTObjectInfo creation
          for (ISubParser subParser : m_subParsers) {
            AstObjectInfo model =
                subParser.parseExpression(
                    m_editor,
                    signature,
                    invocation,
                    arguments,
                    AbstractParser.this);
            //
            if (model != null) {
              addModel(model, invocation);
              return;
            }
          }
        } catch (Throwable e) {
          ReflectionUtils.propagate(e);
        }
      }
    });
  }

  private boolean failExpressions(Expression[] arguments) throws Exception {
    for (Expression argument : arguments) {
      if (AstNodeUtils.getTypeBinding(argument) == null) {
        addError(m_editor, "Expression with errors: '" + argument + "'", new Throwable());
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IModelResolver
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addModel(AstObjectInfo model, Expression creation) throws Exception {
    addModelSupport(new AstModelSupport(model, creation));
  }

  public AstObjectInfo getModel(Expression expression) throws Exception {
    IModelSupport modelSupport = getModelSupport(expression);
    return modelSupport == null ? null : modelSupport.getModel();
  }

  public AstObjectInfo getModel(Expression expression, IModelResolverFilter filter)
      throws Exception {
    for (IModelSupport modelSupport : m_modelSupports) {
      if (modelSupport.isRepresentedBy(expression) && filter.accept(modelSupport)) {
        return modelSupport.getModel();
      }
    }
    return null;
  }

  public void addModelSupport(IModelSupport modelSupport) {
    m_modelSupports.add(modelSupport);
  }

  public IModelSupport getModelSupport(Expression expression) throws Exception {
    Expression actualExpression = AstNodeUtils.getActualVariableExpression(expression);
    IModelSupport modelSupport = getModelSupport0(actualExpression);
    if (modelSupport == null && actualExpression != expression) {
      modelSupport = getModelSupport0(expression);
    }
    return modelSupport;
  }

  private IModelSupport getModelSupport0(Expression expression) throws Exception {
    for (IModelSupport modelSupport : m_modelSupports) {
      if (modelSupport.isRepresentedBy(expression)) {
        return modelSupport;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Errors
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void addError(AstEditor editor, String message, Throwable exception) {
    EditorState state = EditorState.get(editor);
    state.addWarning(new EditorWarning(message, exception));
  }
}