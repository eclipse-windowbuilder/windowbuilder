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

import com.google.common.collect.Maps;

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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ConverterUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.UpdateStrategyPropertiesUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.UpdateStrategyUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ValidatorUiContentProvider;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>org.eclipse.core.databinding.UpdateValueStrategy</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class UpdateValueStrategyInfo extends UpdateStrategyInfo {
  private static final String[] VALIDATORS = {
      "setAfterConvertValidator",
      "setAfterGetValidator",
      "setBeforeSetValidator"};
  private final Map<String, ValidatorInfo> m_validators = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateValueStrategyInfo() {
    setStringValue(Activator.getStore().getString(
        IPreferenceConstants.UPDATE_VALUE_STRATEGY_DEFAULT));
  }

  public UpdateValueStrategyInfo(ClassInstanceCreation creation, Expression[] arguments) {
    super(creation, arguments);
  }

  /**
   * Note: this constructor used only for tests.
   */
  public UpdateValueStrategyInfo(StrategyType strategyType,
      Object strategyValue,
      ConverterInfo converter) {
    super(strategyType, strategyValue, converter);
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
    String methodName = invocation.getName().getIdentifier();
    // parse validator
    if (ArrayUtils.contains(VALIDATORS, methodName)) {
      Assert.isLegal(arguments.length == 1);
      Assert.isNull(m_validators.get(methodName));
      ValidatorInfo validator = (ValidatorInfo) resolver.getModel(arguments[0]);
      if (validator == null) {
        AbstractParser.addError(
            editor,
            "Validator argument '" + arguments[0] + "' not found",
            new Throwable());
      } else {
        m_validators.put(methodName, validator);
        //
        IModelSupport modelSupport = resolver.getModelSupport(invocation.getExpression());
        if (modelSupport instanceof StrategyModelSupport) {
          StrategyModelSupport strategyModelSupport = (StrategyModelSupport) modelSupport;
          strategyModelSupport.addInvocation(invocation);
        }
      }
    }
    //
    return super.parseExpression(editor, signature, invocation, arguments, resolver, provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getStrategyClass() {
    return "org.eclipse.core.databinding.UpdateValueStrategy";
  }

  @Override
  protected Object getStrategyValue(String value) {
    if (value == null) {
      return Value.POLICY_UPDATE;
    }
    if (value.endsWith("POLICY_NEVER")) {
      return Value.POLICY_NEVER;
    }
    if (value.endsWith("POLICY_ON_REQUEST")) {
      return Value.POLICY_ON_REQUEST;
    }
    if (value.endsWith("POLICY_CONVERT")) {
      return Value.POLICY_CONVERT;
    }
    if (value.endsWith("POLICY_UPDATE")) {
      return Value.POLICY_UPDATE;
    }
    //
    Assert.fail("Undefine value strategy value: " + value);
    return null;
  }

  @Override
  protected String getStrategyStringValue() {
    switch ((Value) m_strategyValue) {
      case POLICY_NEVER :
        return "POLICY_NEVER";
      case POLICY_ON_REQUEST :
        return "POLICY_ON_REQUEST";
      case POLICY_CONVERT :
        return "POLICY_CONVERT";
      case POLICY_UPDATE :
        return "POLICY_UPDATE";
    }
    Assert.fail("Undefine value strategy value: " + m_strategyValue);
    return null;
  }

  @Override
  public void setStringValue(String value) {
    if (value.endsWith("POLICY_NEVER")) {
      m_strategyType = StrategyType.IntConstructor;
      m_strategyValue = Value.POLICY_NEVER;
    } else if (value.endsWith("POLICY_ON_REQUEST")) {
      m_strategyType = StrategyType.IntConstructor;
      m_strategyValue = Value.POLICY_ON_REQUEST;
    } else if (value.endsWith("POLICY_CONVERT")) {
      m_strategyType = StrategyType.IntConstructor;
      m_strategyValue = Value.POLICY_CONVERT;
    } else if (value.endsWith("POLICY_UPDATE")) {
      m_strategyType = StrategyType.Null;
      m_strategyValue = Value.POLICY_UPDATE;
    } else {
      m_strategyType = StrategyType.ExtendetClass;
      m_strategyValue = value;
    }
  }

  public ValidatorInfo getAfterConvertValidator() {
    return m_validators.get(VALIDATORS[0]);
  }

  public ValidatorInfo getAfterGetValidator() {
    return m_validators.get(VALIDATORS[1]);
  }

  public ValidatorInfo getBeforeSetValidator() {
    return m_validators.get(VALIDATORS[2]);
  }

  public ValidatorInfo getValidator(String name) {
    Assert.isTrue(ArrayUtils.contains(VALIDATORS, name));
    return m_validators.get(name);
  }

  public void setValidator(String name, ValidatorInfo validator) {
    Assert.isTrue(ArrayUtils.contains(VALIDATORS, name));
    if (validator == null) {
      // add validator
      m_validators.remove(name);
    } else {
      // replace validator
      m_validators.put(name, validator);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ChooseClassConfiguration createConfiguration(BindingUiContentProviderContext context) {
    ChooseClassConfiguration configuration = super.createConfiguration(context);
    configuration.setDialogFieldLabel("UpdateValueStrategy:");
    configuration.setDefaultValues(new String[]{
        "POLICY_UPDATE",
        "POLICY_NEVER",
        "POLICY_ON_REQUEST",
        "POLICY_CONVERT"});
    return configuration;
  }

  /**
   * Create configuration for edit strategy validator.
   */
  private ChooseClassConfiguration createValidatorConfiguration(BindingUiContentProviderContext context,
      String name,
      ValidatorInfo validator) {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel(name + ":");
    configuration.setValueScope("org.eclipse.core.databinding.validation.IValidator");
    configuration.setClearValue("N/S");
    configuration.setBaseClassName("org.eclipse.core.databinding.validation.IValidator");
    configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
    configuration.setEmptyClassErrorMessage(context.getDirection()
        + " \""
        + name
        + "\" class is empty.");
    configuration.setErrorMessagePrefix(context.getDirection() + " \"" + name + "\"");
    //
    if (validator != null
        && (validator.isAnonymous() || validator.getClassName().indexOf('(') != -1)) {
      configuration.addDefaultStart(validator.getClassName());
    }
    //
    return configuration;
  }

  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      BindingUiContentProviderContext context) throws Exception {
    // self editor
    providers.add(new UpdateStrategyUiContentProvider(createConfiguration(context), this));
    // properties editor
    UpdateStrategyPropertiesUiContentProvider propertiesUIContentProvider =
        new UpdateStrategyPropertiesUiContentProvider(context.getDirection());
    // validators
    propertiesUIContentProvider.addProvider(new ValidatorUiContentProvider(createValidatorConfiguration(
        context,
        "AfterConvertValidator",
        m_validators.get(VALIDATORS[0])), this, VALIDATORS[0]));
    propertiesUIContentProvider.addProvider(new ValidatorUiContentProvider(createValidatorConfiguration(
        context,
        "AfterGetValidator",
        m_validators.get(VALIDATORS[1])), this, VALIDATORS[1]));
    propertiesUIContentProvider.addProvider(new ValidatorUiContentProvider(createValidatorConfiguration(
        context,
        "BeforeSetValidator",
        m_validators.get(VALIDATORS[2])), this, VALIDATORS[2]));
    // converter
    propertiesUIContentProvider.addProvider(new ConverterUiContentProvider(createConverterConfiguration(context),
        this));
    //
    providers.add(propertiesUIContentProvider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean sourceCodeHasVariable() {
    return m_converter != null || !m_validators.isEmpty();
  }

  @Override
  protected void addSourceCodeForProperties(List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    super.addSourceCodeForProperties(lines, generationSupport);
    // add validators
    for (Map.Entry<String, ValidatorInfo> entry : m_validators.entrySet()) {
      lines.add(getVariableIdentifier()
          + "."
          + entry.getKey()
          + "("
          + entry.getValue().getSourceCode(lines, generationSupport)
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
    // visit to validators
    for (Map.Entry<String, ValidatorInfo> entry : m_validators.entrySet()) {
      entry.getValue().accept(visitor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Values
  //
  ////////////////////////////////////////////////////////////////////////////
  public static enum Value {
    POLICY_NEVER, POLICY_ON_REQUEST, POLICY_CONVERT, POLICY_UPDATE
  }
}