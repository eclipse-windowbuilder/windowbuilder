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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.Messages;

import org.eclipse.jdt.ui.IJavaElementSearchConstants;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Configuration for {@link ChooseClassUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ChooseClassConfiguration {
  private String m_dialogFieldLabel;
  private boolean m_dialogFieldEnabled = true;
  private boolean m_useClearButton;
  private String m_valuesScope;
  private String m_clearValue;
  private String[] m_defaultValues;
  private String m_targetClassName;
  private String m_retargetClassName;
  private String[] m_baseClassNames;
  private boolean m_chooseInterfaces;
  private final List<String> m_defaultStarts = Lists.newArrayList();
  private Class<?>[][] m_constructorParameters;
  private String m_emptyClassErrorMessage = Messages.ChooseClassConfiguration_validateEmptyClass;
  private String m_errorMessagePrefix = Messages.ChooseClassConfiguration_validateMessagePrefix;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassConfiguration() {
  }

  public ChooseClassConfiguration(ChooseClassConfiguration configuration) {
    m_dialogFieldLabel = configuration.m_dialogFieldLabel;
    m_dialogFieldEnabled = configuration.m_dialogFieldEnabled;
    m_useClearButton = configuration.m_useClearButton;
    m_valuesScope = configuration.m_valuesScope;
    m_clearValue = configuration.m_clearValue;
    m_defaultValues = configuration.m_defaultValues;
    m_targetClassName = configuration.m_targetClassName;
    m_retargetClassName = configuration.m_retargetClassName;
    m_baseClassNames = configuration.m_baseClassNames;
    m_chooseInterfaces = configuration.m_chooseInterfaces;
    m_defaultStarts.addAll(configuration.m_defaultStarts);
    m_constructorParameters = configuration.m_constructorParameters;
    m_emptyClassErrorMessage = configuration.m_emptyClassErrorMessage;
    m_errorMessagePrefix = configuration.m_errorMessagePrefix;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DialogField settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the label for dialog field.
   */
  public final String getDialogFieldLabel() {
    return m_dialogFieldLabel;
  }

  /**
   * Sets the label for dialog field.
   */
  public final void setDialogFieldLabel(String dialogFieldLabel) {
    m_dialogFieldLabel = dialogFieldLabel;
  }

  /**
   * @return the enable state of the dialog field.
   */
  public boolean isDialogFieldEnabled() {
    return m_dialogFieldEnabled;
  }

  /**
   * Sets the enable state of the dialog field.
   */
  public void setDialogFieldEnabled(boolean dialogFieldEnabled) {
    m_dialogFieldEnabled = dialogFieldEnabled;
  }

  /**
   * @return the state of use "clear" button.
   */
  public final boolean isUseClearButton() {
    return m_useClearButton;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value scope name, maybe not set.
   */
  public final String getValuesScope() {
    return m_valuesScope;
  }

  /**
   * Sets the value scope name.
   */
  public final void setValueScope(String valuesScope) {
    m_valuesScope = valuesScope;
  }

  /**
   * @return the clear value.
   */
  public final String getClearValue() {
    return m_clearValue;
  }

  /**
   * Sets clear value. Clear value sets to provider if press "clear" button.
   */
  public final void setClearValue(String clearValue) {
    m_useClearButton = !StringUtils.isEmpty(clearValue);
    m_clearValue = clearValue;
  }

  /**
   * @return the default values.
   */
  public final String[] getDefaultValues() {
    return m_defaultValues;
  }

  /**
   * Sets the default values. Default values adds at the beginning of combo items and if selected
   * not checked load class.
   */
  public final void setDefaultValues(String[] defaultValues) {
    m_defaultValues = defaultValues;
  }

  /**
   * check apply redirect rule.
   */
  public final String getRetargetClassName(String className) {
    if (className.equals(m_targetClassName)) {
      return m_retargetClassName;
    }
    return className;
  }

  /**
   * Sets redirect rule: replace <code>targetClassName</code> value to
   * <code>retargetClassName</code> value.
   */
  public final void setRetargetClassName(String targetClassName, String retargetClassName) {
    m_targetClassName = targetClassName;
    m_retargetClassName = retargetClassName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the base class name.
   */
  public final String[] getBaseClassNames() {
    return m_baseClassNames;
  }

  /**
   * Sets base class name for choose type dialog.
   */
  public final void setBaseClassName(String baseClassName) {
    setBaseClassNames(baseClassName);
  }

  /**
   * Sets base class name for choose type dialog.
   */
  public final void setBaseClassNames(String... baseClassNames) {
    m_baseClassNames = baseClassNames;
  }

  /**
   * @return the styles for choose type dialog.
   */
  public final int getOpenTypeStyle() {
    return m_chooseInterfaces
        ? IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES
        : IJavaElementSearchConstants.CONSIDER_CLASSES;
  }

  /**
   * @return on/off state for choose interfaces. Default value is <code>false</code>.
   */
  public boolean isChooseInterfaces() {
    return m_chooseInterfaces;
  }

  /**
   * Sets on/off state for choose interfaces.
   */
  public final void setChooseInterfaces(boolean chooseInterfaces) {
    m_chooseInterfaces = chooseInterfaces;
  }

  /**
   * @return the parameters for required class constructor.
   */
  public final Class<?>[][] getConstructorsParameters() {
    return m_constructorParameters;
  }

  /**
   * Sets the parameters for required class constructor.
   */
  public final void setConstructorParameters(Class<?>[] constructorParameters) {
    setConstructorsParameters(constructorParameters);
  }

  /**
   * Sets the parameters for required class constructors.
   */
  public final void setConstructorsParameters(Class<?>[]... constructorParameters) {
    m_constructorParameters = constructorParameters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default value settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check if given string starts with one of more prefix.
   */
  public final boolean isDefaultString(String testString) {
    for (String defaultString : m_defaultStarts) {
      if (testString.startsWith(defaultString)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add default prefix. If class name starts with given string then class name is default.
   */
  public final void addDefaultStart(String defaultStart) {
    m_defaultStarts.add(defaultStart);
  }

  public final void clearDefaultStrings() {
    m_defaultStarts.clear();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Error settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the error message if chosen empty class name.
   */
  public final String getEmptyClassErrorMessage() {
    return m_emptyClassErrorMessage;
  }

  /**
   * Sets the error message if chosen empty class name.
   */
  public final void setEmptyClassErrorMessage(String errorMessage) {
    m_emptyClassErrorMessage = errorMessage;
  }

  /**
   * @return the error message prefix for error states.
   */
  public final String getErrorMessagePrefix() {
    return m_errorMessagePrefix;
  }

  /**
   * Sets the error message prefix for error states (%AAA% not exist, %AAA% is abstract, etc.).
   */
  public final void setErrorMessagePrefix(String errorMessagePrefix) {
    m_errorMessagePrefix = errorMessagePrefix;
  }
}