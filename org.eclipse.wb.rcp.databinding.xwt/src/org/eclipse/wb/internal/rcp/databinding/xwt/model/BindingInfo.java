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
package org.eclipse.wb.internal.rcp.databinding.xwt.model;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.Messages;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders.ModeContentProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders.TriggerContentProvider;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class BindingInfo extends AbstractBindingInfo {
  public static final String[] MODES = {"TwoWay", "OneWay", "OneTime"};
  private static final String[] modes = {"twoway", "oneway", "onetime"};
  //
  public static final String[] TRIGGERS = {"Default", "PropertyChanged", "LostFocus"};
  private static final String[] triggers = {"default", "propertychanged", "lostfocus"};
  //
  private final BindableInfo m_target;
  private final BindableInfo m_targetProperty;
  private final BindableInfo m_model;
  private final BindableInfo m_modelProperty;
  private int m_mode;
  private int m_trigger;
  private final ConverterInfo m_converter = new ConverterInfo();
  private final ValidationInfo m_validator = new ValidationInfo();
  private IDocumentEditor m_documentEditor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingInfo(BindableInfo target,
      BindableInfo targetProperty,
      BindableInfo model,
      BindableInfo modelProperty) {
    m_target = target;
    m_targetProperty = targetProperty;
    m_model = model;
    m_modelProperty = modelProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  public DocumentElement getTargetElement() {
    WidgetBindableInfo target = (WidgetBindableInfo) m_target;
    return target.getXMLObjectInfo().getElement();
  }

  public void modify(RunnableEx runnable) throws Exception {
    WidgetBindableInfo target = (WidgetBindableInfo) m_target;
    XmlObjectInfo objectInfo = target.getXMLObjectInfo();
    //
    objectInfo.startEdit();
    runnable.run();
    objectInfo.endEdit();
  }

  @Override
  public IObserveInfo getTarget() {
    return m_target;
  }

  @Override
  public IObserveInfo getTargetProperty() {
    return m_targetProperty;
  }

  public String getTargetPresentationText() throws Exception {
    return m_target.getPresentation().getTextForBinding()
        + "."
        + m_targetProperty.getPresentation().getTextForBinding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObserveInfo getModel() {
    return m_model;
  }

  @Override
  public IObserveInfo getModelProperty() {
    return m_modelProperty;
  }

  public String getModelPresentationText() throws Exception {
    return m_model.getPresentation().getTextForBinding()
        + "."
        + m_modelProperty.getPresentation().getTextForBinding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getMode() {
    return m_mode;
  }

  public void setMode(int mode) {
    m_mode = mode;
  }

  public void setMode(String mode) {
    if (mode == null) {
      m_mode = 0;
    } else {
      m_mode = ArrayUtils.indexOf(modes, mode.toLowerCase());
      if (m_mode == -1) {
        m_mode = 0;
      }
    }
  }

  public int getTriger() {
    return m_trigger;
  }

  public void setTrigger(int trigger) {
    m_trigger = trigger;
  }

  public void setTrigger(String trigger) {
    if (trigger == null) {
      m_trigger = 0;
    } else {
      m_trigger = ArrayUtils.indexOf(triggers, trigger.toLowerCase());
      if (m_trigger == -1) {
        m_trigger = 0;
      }
    }
  }

  public ConverterInfo getConverter() {
    return m_converter;
  }

  public ValidationInfo getValidator() {
    return m_validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(DataBindingContextInfo context,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    throw new UnsupportedOperationException();
  }

  public IDocumentEditor getDocumentEditor() {
    return m_documentEditor;
  }

  public void setDocumentEditor(IDocumentEditor documentEditor) {
    m_documentEditor = documentEditor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getDefinitionOffset() {
    return m_documentEditor.getDefinitionOffset();
  }

  @Override
  public String getDefinitionSource(org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider provider)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    // configure page
    listener.setTitle(Messages.BindingInfo_listenerTitle);
    listener.setMessage(Messages.BindingInfo_listenerMessage);
    // add target editors
    providers.add(new LabelUiContentProvider(Messages.BindingInfo_targetTitle,
        getTargetPresentationText()));
    // add model editors
    providers.add(new LabelUiContentProvider(Messages.BindingInfo_modelTitle,
        getModelPresentationText()));
    // separator
    providers.add(new SeparatorUiContentProvider());
    // add binding editors
    providers.add(new ModeContentProvider(this));
    providers.add(new TriggerContentProvider(this));
    m_converter.createContentProviders(providers, listener, provider);
    m_validator.createContentProviders(providers, listener, provider);
  }

  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider provider) throws Exception {
    throw new UnsupportedOperationException();
  }
}