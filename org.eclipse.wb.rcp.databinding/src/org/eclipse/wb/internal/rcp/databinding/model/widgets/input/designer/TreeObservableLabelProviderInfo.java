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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.LabelProviderInfo;

import org.eclipse.core.databinding.observable.set.IObservableSet;

import java.util.List;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class TreeObservableLabelProviderInfo extends LabelProviderInfo {
  private static final String PROVIDER_CLASS =
      "org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider";
  private final KnownElementsObservableInfo m_allElementsObservable;
  private Class<?> m_elementType;
  private String m_textProperty;
  private String m_imageProperty;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeObservableLabelProviderInfo(String className,
      KnownElementsObservableInfo allElementsObservable) {
    super(className);
    m_allElementsObservable = allElementsObservable;
  }

  public TreeObservableLabelProviderInfo(KnownElementsObservableInfo allElementsObservable) {
    this(PROVIDER_CLASS, allElementsObservable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setElementType(Class<?> elementType) {
    m_elementType = elementType;
  }

  public Class<?> getElementType() {
    return m_elementType;
  }

  public KnownElementsObservableInfo getAllElementsObservable() {
    return m_allElementsObservable;
  }

  public String getTextProperty() {
    return m_textProperty;
  }

  public void setTextProperty(String textProperty) throws Exception {
    m_textProperty = textProperty;
  }

  public String getImageProperty() {
    return m_imageProperty;
  }

  public void setImageProperty(String imageProperty) throws Exception {
    m_imageProperty = imageProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configure(ChooseClassConfiguration configuration, boolean useClear) {
    configuration.setValueScope(PROVIDER_CLASS);
    if (useClear) {
      configuration.setClearValue(PROVIDER_CLASS);
    }
    configuration.setBaseClassName(PROVIDER_CLASS);
    configuration.setConstructorParameters(new Class[]{
        IObservableSet.class,
        Class.class,
        String.class,
        String.class});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return super.getPresentationText()
        + "["
        + CoreUtils.joinStrings(", ", m_textProperty, m_imageProperty)
        + "]";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    return "new "
        + m_className
        + "("
        + m_allElementsObservable.getSourceCode()
        + ", "
        + CoreUtils.getClassName(m_elementType)
        + ".class, "
        + CoreUtils.getDefaultString(m_textProperty, "\"", "null")
        + ", "
        + CoreUtils.getDefaultString(m_imageProperty, "\"", "null")
        + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    m_allElementsObservable.accept(visitor);
  }
}