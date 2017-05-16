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
package org.eclipse.wb.internal.core.utils.state;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

/**
 * Global context for active Java or XML based editor.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class GlobalState {
  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean m_parsing;
  /**
   * @return <code>true</code> if parsing is in progress.
   */
  public static boolean isParsing() {
    return m_parsing;
  }
  /**
   * Specifies if parsing is in progress.
   */
  public static void setParsing(boolean parsing) {
    m_parsing = parsing;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Toolkit
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ToolkitDescription m_toolkit;
  /**
   * @return the {@link ToolkitDescription} of active editor.
   */
  public static ToolkitDescription getToolkit() {
    return m_toolkit;
  }
  /**
   * Sets the {@link ToolkitDescription} of active editor.
   */
  public static void setToolkit(ToolkitDescription toolkit) {
    m_toolkit = toolkit;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ObjectInfo m_activeObject;
  /**
   * @return some {@link ObjectInfo} of active editor.
   */
  public static ObjectInfo getActiveObject() {
    return m_activeObject;
  }
  /**
   * Sets some {@link ObjectInfo} of active editor.
   */
  public static void setActiveObject(ObjectInfo activeObject) {
    m_activeObject = activeObject;
  }
  /**
   * @return <code>true</code> if given object is component model.
   */
  public static boolean isComponent(Object object) {
    return m_validatorHelper.isComponent(object);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ClassLoader m_classLoader;
  /**
   * @return the {@link ClassLoader} of active editor.
   */
  public static ClassLoader getClassLoader() {
    return m_classLoader;
  }
  /**
   * Sets the {@link ClassLoader} of active editor.
   */
  public static void setClassLoader(ClassLoader classLoader) {
    m_classLoader = classLoader;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IParametersProvider m_parametersProvider;
  public static IParametersProvider getParametersProvider() {
    return m_parametersProvider;
  }
  public static void setParametersProvider(IParametersProvider parametersProvider) {
    m_parametersProvider = parametersProvider;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Description
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IDescriptionHelper m_descriptionHelper;
  public static IDescriptionHelper getDescriptionHelper() {
    return m_descriptionHelper;
  }
  public static void setDescriptionHelper(IDescriptionHelper descriptionHelper) {
    m_descriptionHelper = descriptionHelper;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validators
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ILayoutRequestValidatorHelper m_validatorHelper;
  public static ILayoutRequestValidatorHelper getValidatorHelper() {
    return m_validatorHelper;
  }
  public static void setValidatorHelper(ILayoutRequestValidatorHelper validatorHelper) {
    m_validatorHelper = validatorHelper;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IPasteRequestProcessor m_pasteRequestProcessor;
  public static IPasteRequestProcessor getPasteRequestProcessor() {
    return m_pasteRequestProcessor;
  }
  public static void setPasteRequestProcessor(IPasteRequestProcessor pasteRequestProcessor) {
    m_pasteRequestProcessor = pasteRequestProcessor;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Order
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IOrderProcessor m_orderProcessor;
  public static IOrderProcessor getOrderProcessor() {
    return m_orderProcessor;
  }
  public static void setOrderProcessor(IOrderProcessor orderProcessor) {
    m_orderProcessor = orderProcessor;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Other
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IOtherHelper m_otherHelper;
  public static IOtherHelper getOtherHelper() {
    return m_otherHelper;
  }
  public static void setOtherHelper(IOtherHelper otherHelper) {
    m_otherHelper = otherHelper;
  }
}
