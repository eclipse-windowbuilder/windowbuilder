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
package org.eclipse.wb.internal.rcp.databinding.model.context.strategies;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.IModelSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Abstract model for strategies.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public abstract class UpdateStrategyInfo extends AstObjectInfo {
  protected StrategyType m_strategyType;
  protected Object m_strategyValue;
  protected ConverterInfo m_converter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  protected UpdateStrategyInfo() {
    m_strategyType = StrategyType.Null;
    m_strategyValue = getStrategyValue(null);
  }

  protected UpdateStrategyInfo(ClassInstanceCreation creation, Expression[] arguments) {
    String className = AstNodeUtils.getFullyQualifiedName(creation, false);
    // parse value
    if (arguments.length == 0) {
      // default constructor
      if (getStrategyClass().equals(className)) {
        // original strategy
        m_strategyType = StrategyType.DefaultConstructor;
        m_strategyValue = getStrategyValue(null);
      } else {
        // inherit strategy
        m_strategyType = StrategyType.ExtendetClass;
        m_strategyValue = className;
      }
    } else {
      // original strategy with one parameter
      if (getStrategyClass().equals(className) && arguments.length == 1) {
        m_strategyType = StrategyType.IntConstructor;
        Expression expression = AstNodeUtils.getActualVariableExpression(arguments[0]);
        m_strategyValue = getStrategyValue(expression.toString());
      } else {
        String source = EditorState.getActiveJavaInfo().getEditor().getSource(creation);
        int index = source.indexOf('(');
        // inherit strategy
        m_strategyType = StrategyType.ExtendetClass;
        m_strategyValue = className + source.substring(index);
      }
    }
  }

  /**
   * Note: this constructor used only for tests.
   */
  protected UpdateStrategyInfo(StrategyType strategyType,
      Object strategyValue,
      ConverterInfo converter) {
    m_strategyType = strategyType;
    m_strategyValue = strategyValue;
    m_converter = converter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    // parse converter
    if ("setConverter".equals(invocation.getName().getIdentifier())) {
      Assert.isLegal(arguments.length == 1);
      Assert.isNull(m_converter);
      m_converter = (ConverterInfo) resolver.getModel(arguments[0]);
      if (m_converter == null) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.UpdateStrategyInfo_converterArgumentNotFound,
            arguments[0]), new Throwable());
      } else {
        IModelSupport modelSupport = resolver.getModelSupport(invocation.getExpression());
        if (modelSupport instanceof StrategyModelSupport) {
          StrategyModelSupport strategyModelSupport = (StrategyModelSupport) modelSupport;
          strategyModelSupport.addInvocation(invocation);
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link StrategyType} for this strategy.
   */
  public final StrategyType getStrategyType() {
    return m_strategyType;
  }

  /**
   * @return the current value for this strategy, may be <code>String</code> or <code>enum</code>.
   */
  public final Object getStrategyValue() {
    return m_strategyValue;
  }

  /**
   * @return string presentation for current value.
   */
  public final String getStringValue() {
    if (m_strategyType == StrategyType.ExtendetClass) {
      return (String) m_strategyValue;
    }
    return getStrategyStringValue();
  }

  protected abstract String getStrategyClass();

  /**
   * @return strategy value (<code>enum</code> and etc.) for given string value.
   */
  protected abstract Object getStrategyValue(String value);

  /**
   * @return string presentation for current value.
   */
  protected abstract String getStrategyStringValue();

  /**
   * Sets strategy value.
   */
  public abstract void setStringValue(String value);

  /**
   * @return {@link ConverterInfo} for this strategy, may be <code>null</code>.
   */
  public final ConverterInfo getConverter() {
    return m_converter;
  }

  /**
   * Sets or clear {@link ConverterInfo} for this strategy.
   */
  public void setConverter(ConverterInfo converter) {
    m_converter = converter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getPresentationText() throws Exception {
    return getStringValue();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create configuration for edit this strategy.
   */
  protected ChooseClassConfiguration createConfiguration(BindingUiContentProviderContext context) {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setValueScope(getStrategyClass());
    configuration.setRetargetClassName(getStrategyClass(), "POLICY_UPDATE");
    configuration.setBaseClassName(getStrategyClass());
    configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
    configuration.setEmptyClassErrorMessage(MessageFormat.format(
        Messages.UpdateStrategyInfo_errorMessage,
        context.getDirection()));
    configuration.setErrorMessagePrefix(MessageFormat.format(
        Messages.UpdateStrategyInfo_errorMessagePrefix,
        context.getDirection()));
    //
    if (m_strategyType == StrategyType.ExtendetClass
        && m_strategyValue.toString().indexOf('(') != -1) {
      configuration.addDefaultStart(m_strategyValue.toString());
    }
    //
    return configuration;
  }

  /**
   * Create configuration for edit strategy convert.
   */
  protected final ChooseClassConfiguration createConverterConfiguration(BindingUiContentProviderContext context) {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel(Messages.UpdateStrategyInfo_chooseLabel);
    configuration.setValueScope("org.eclipse.core.databinding.conversion.IConverter");
    configuration.setClearValue("N/S");
    configuration.setBaseClassName("org.eclipse.core.databinding.conversion.IConverter");
    configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
    configuration.setEmptyClassErrorMessage(MessageFormat.format(
        Messages.UpdateStrategyInfo_chooseErrorMessage,
        context.getDirection()));
    configuration.setErrorMessagePrefix(MessageFormat.format(
        Messages.UpdateStrategyInfo_chooseErrorMessagePrefix,
        context.getDirection()));
    //
    if (m_converter != null
        && (m_converter.isAnonymous() || m_converter.getClassName().indexOf('(') != -1)) {
      configuration.addDefaultStart(m_converter.getClassName());
    }
    //
    return configuration;
  }

  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public abstract void createContentProviders(List<IUiContentProvider> providers,
      BindingUiContentProviderContext context) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Generate source code association with this object.
   */
  public final String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    // handle variable
    boolean hasVariable = sourceCodeHasVariable();
    //
    if (getVariableIdentifier() == null) {
      if (hasVariable) {
        setVariableIdentifier(generationSupport.generateLocalName("strategy"));
      }
    } else {
      hasVariable = true;
    }
    //
    String strategyClassName = getStrategyClass();
    // generate code
    switch (m_strategyType) {
      case Null :
        if (!hasVariable) {
          setVariableIdentifier(null);
          return "null";
        }
      case DefaultConstructor :
        if (hasVariable) {
          lines.add(strategyClassName
              + " "
              + getVariableIdentifier()
              + " = "
              + "new "
              + strategyClassName
              + "();");
        } else {
          return "new " + strategyClassName + "()";
        }
        break;
      case IntConstructor :
        if (hasVariable) {
          lines.add(strategyClassName
              + " "
              + getVariableIdentifier()
              + " = new "
              + strategyClassName
              + "("
              + strategyClassName
              + "."
              + getStrategyStringValue()
              + ");");
        } else {
          return "new "
              + strategyClassName
              + "("
              + strategyClassName
              + "."
              + getStrategyStringValue()
              + ")";
        }
        break;
      case ExtendetClass :
        String defaultCostructor = m_strategyValue.toString().indexOf('(') == -1 ? "()" : "";
        if (hasVariable) {
          lines.add(strategyClassName
              + " "
              + getVariableIdentifier()
              + " = new "
              + m_strategyValue
              + defaultCostructor
              + ";");
        } else {
          return "new " + m_strategyValue + defaultCostructor;
        }
        break;
    }
    //
    addSourceCodeForProperties(lines, generationSupport);
    //
    Assert.isTrue(hasVariable);
    return getVariableIdentifier();
  }

  /**
   * @return <code>true</code> if strategy creation need assignment to variable.
   */
  protected boolean sourceCodeHasVariable() {
    return m_converter != null;
  }

  /**
   * Add additional source code (converter, validators, etc.).
   */
  protected void addSourceCodeForProperties(List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    if (m_converter != null) {
      lines.add(getVariableIdentifier()
          + ".setConverter("
          + m_converter.getSourceCode(lines, generationSupport)
          + ");");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    // visit to converter
    if (m_converter != null) {
      m_converter.accept(visitor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  public static enum StrategyType {
    Null, DefaultConstructor, IntConstructor, ExtendetClass
  }
}