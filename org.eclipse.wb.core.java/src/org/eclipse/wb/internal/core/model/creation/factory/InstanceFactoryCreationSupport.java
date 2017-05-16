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
package org.eclipse.wb.internal.core.model.creation.factory;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.util.live.AbstractLiveManager;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;

import org.apache.commons.lang.NotImplementedException;

import java.util.List;

/**
 * Implementation of {@link CreationSupport} for creating objects using instance methods.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class InstanceFactoryCreationSupport extends AbstractExplicitFactoryCreationSupport {
  private final InstanceFactoryInfo m_factory;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InstanceFactoryCreationSupport(InstanceFactoryInfo factory,
      FactoryMethodDescription description) {
    super(description);
    m_factory = factory;
  }

  public InstanceFactoryCreationSupport(InstanceFactoryInfo factory,
      FactoryMethodDescription description,
      MethodInvocation invocation) {
    super(description, invocation);
    m_factory = factory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "instance factory: "
        + "{"
        + m_factory.getVariableSupport().toString()
        + "}"
        + " "
        + m_description.getSignature();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link InstanceFactoryInfo} used to create this component.
   */
  public InstanceFactoryInfo getFactory() {
    return m_factory;
  }

  public CreationSupport getLiveComponentCreation() {
    FactoryMethodDescription factoryMethodDescription = getDescription();
    return new LiveCreationSupport(factoryMethodDescription);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String add_getSource_invocationExpression(NodeTarget target) throws Exception {
    return TemplateUtils.getExpression(m_factory) + ".";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IClipboardCreationSupport getClipboard() throws Exception {
    final String factoryClassName = m_description.getDeclaringClass().getName();
    final String methodSignature = m_description.getSignature();
    final String argumentsSource = getClipboardArguments();
    return new IClipboardCreationSupport() {
      private static final long serialVersionUID = 0L;

      @Override
      public CreationSupport create(JavaInfo rootObject) throws Exception {
        AstEditor editor = rootObject.getEditor();
        Class<?> factoryClass =
            EditorState.get(editor).getEditorLoader().loadClass(factoryClassName);
        //
        InstanceFactoryInfo factoryInfo = getFactory(rootObject, factoryClass);
        FactoryMethodDescription description =
            FactoryDescriptionHelper.getDescription(editor, factoryClass, methodSignature, false);
        //
        InstanceFactoryCreationSupport creationSupport =
            new InstanceFactoryCreationSupport(factoryInfo, description);
        creationSupport.m_addArguments = argumentsSource;
        return creationSupport;
      }

      /**
       * @return the new or existing {@link InstanceFactoryInfo} from hierarchy.
       */
      private InstanceFactoryInfo getFactory(JavaInfo rootObject, Class<?> factoryClass)
          throws Exception {
        List<InstanceFactoryInfo> factories =
            InstanceFactoryInfo.getFactories(rootObject, factoryClass);
        // single factory
        if (factories.size() == 1) {
          return factories.get(0);
        }
        // no factories
        /*if (factories.size() == 0)*/{
          Assert.isTrue(factories.isEmpty(), "Only single and no instance factories expected for "
              + factoryClassName
              + "."
              + methodSignature
              + ", but found "
              + factories);
          return InstanceFactoryInfo.add(rootObject, factoryClass);
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LiveCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link CreationSupport} for creating {@link JavaInfo} for
   * {@link AbstractLiveManager}.
   * <p>
   * We use here special trick - instead of real source code for {@link InstanceFactoryInfo} access
   * we use casted {@link NullLiteral}, and then evaluate it as
   * <em>already existing<em> object of factory! This object
   * exists because "live" works when we already show some GUI for user, so there are objects for all {@link JavaInfo}'s,
   * including {@link InstanceFactoryInfo}.
   */
  private final class LiveCreationSupport extends AbstractExplicitFactoryCreationSupport {
    private final String m_factoryTypeName;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private LiveCreationSupport(FactoryMethodDescription description) {
      super(description);
      m_factoryTypeName = m_factory.getDescription().getComponentClass().getName();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Evaluation
    //
    ////////////////////////////////////////////////////////////////////////////
    private boolean m_canBeEvaluated = false;

    @Override
    public boolean canBeEvaluated() {
      // first time return "false", so force to call our "create()" method
      if (!m_canBeEvaluated) {
        m_canBeEvaluated = true;
        return false;
      }
      // next times return "true" to evaluate using ASTEvaluationEngine,
      // with our IExpressionEvaluator that evaluates "(our.factory.Type) null"
      return true;
    }

    @Override
    public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
        throws Exception {
      // evaluates "(our.factory.Type) null" into factory object
      IExpressionEvaluator evaluator = new IExpressionEvaluator() {
        public Object evaluate(EvaluationContext _context,
            Expression expression,
            ITypeBinding typeBinding,
            String typeQualifiedName) throws Exception {
          if (expression instanceof CastExpression) {
            CastExpression castExpression = (CastExpression) expression;
            String typeName = AstNodeUtils.getFullyQualifiedName(castExpression.getType(), false);
            if (typeName.equals(m_factoryTypeName)
                && castExpression.getExpression() instanceof NullLiteral) {
              return m_factory.getObject();
            }
          }
          return AstEvaluationEngine.UNKNOWN;
        }
      };
      // JavaInfo object is evaluation of invocation
      context.addEvaluator(evaluator);
      try {
        return AstEvaluationEngine.evaluate(context, m_invocation);
      } finally {
        context.removeEvaluator(evaluator);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Adding
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String add_getSource_invocationExpression(NodeTarget target) throws Exception {
      return "((" + m_factoryTypeName + ") null).";
    }

    public CreationSupport getLiveComponentCreation() {
      throw new NotImplementedException();
    }
  }
}
