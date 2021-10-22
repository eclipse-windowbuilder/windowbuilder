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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.UpdateStrategyUiContentProvider;

import java.util.List;

/**
 * Model for {@link org.jdesktop.beansbinding.AutoBinding}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public class AutoBindingInfo extends BindingInfo {
  protected final UpdateStrategyInfo m_strategyInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutoBindingInfo(UpdateStrategyInfo strategyInfo,
      ObserveInfo target,
      ObserveInfo targetProperty,
      PropertyInfo targetAstProperty,
      ObserveInfo model,
      ObserveInfo modelProperty,
      PropertyInfo modelAstProperty) {
    super(target, targetProperty, targetAstProperty, model, modelProperty, modelAstProperty);
    m_strategyInfo = strategyInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final UpdateStrategyInfo getStrategyInfo() {
    return m_strategyInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  @Override
  public void createContentProviders(List<BindingInfo> bindings,
      List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    super.createContentProviders(bindings, providers, listener, provider);
    providers.add(0, new UpdateStrategyUiContentProvider(m_strategyInfo));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //  Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    // handle variable
    if (getVariableIdentifier() == null) {
      setVariableIdentifier(generationSupport.generateLocalName("AutoBinding"));
    }
    // begin
    StringBuffer line = new StringBuffer();
    boolean localVariable = !isField();
    if (localVariable) {
      line.append("org.jdesktop.beansbinding.AutoBinding");
    }
    if (m_modelAstProperty instanceof ObjectPropertyInfo) {
      generationSupport.addSourceCode(m_targetAstProperty, lines);
      if (localVariable) {
        appendGenericTypes(line, generationSupport);
        line.append(" ");
      }
      line.append(getVariableIdentifier());
      line.append(" = org.jdesktop.beansbinding.Bindings.createAutoBinding(");
      line.append(m_strategyInfo.getStrategySourceCode());
      line.append(", ");
      line.append(m_model.getReference());
      line.append(", ");
      line.append(m_target.getReference());
      line.append(", ");
      line.append(m_targetAstProperty.getVariableIdentifier());
    } else {
      generationSupport.addSourceCode(m_modelAstProperty, lines);
      generationSupport.addSourceCode(m_targetAstProperty, lines);
      if (localVariable) {
        appendGenericTypes(line, generationSupport);
        line.append(" ");
      }
      line.append(getVariableIdentifier());
      line.append(" = org.jdesktop.beansbinding.Bindings.createAutoBinding(");
      line.append(m_strategyInfo.getStrategySourceCode());
      line.append(", ");
      line.append(m_model.getReference());
      line.append(", ");
      line.append(m_modelAstProperty.getVariableIdentifier());
      line.append(", ");
      line.append(m_target.getReference());
      line.append(", ");
      line.append(m_targetAstProperty.getVariableIdentifier());
    }
    // end
    line.append(getCreateMethodHeaderEnd());
    line.append(";");
    lines.add(line.toString());
    // converter & validator
    addFinishSourceCode(lines, generationSupport, true);
  }

  private void appendGenericTypes(StringBuffer line, CodeGenerationSupport generationSupport) {
    if (generationSupport.useGenerics()) {
      line.append(GenericUtils.getTypesSource(
          m_modelAstProperty.getSourceObjectType(),
          m_modelAstProperty.getValueType(),
          m_targetAstProperty.getSourceObjectType(),
          m_targetAstProperty.getValueType()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
    String type = "org.jdesktop.beansbinding.AutoBinding";
    if (CoreUtils.useGenerics(javaInfoRoot.getEditor().getJavaProject())) {
      type +=
          GenericUtils.getTypesSource(
              m_modelAstProperty.getSourceObjectType(),
              m_modelAstProperty.getValueType(),
              m_targetAstProperty.getSourceObjectType(),
              m_targetAstProperty.getValueType());
    }
    setVariableIdentifier(javaInfoRoot, type, variable, field);
  }

  protected static String getTypeSource(ObserveInfo model,
      ObserveInfo modelProperty,
      PropertyInfo modelAstProperty,
      IGenericType genericType) {
    if (modelAstProperty instanceof ObjectPropertyInfo) {
      return GenericUtils.getTypesSource(
          model.getObjectType().getSubType(0),
          model.getObjectType(),
          genericType);
    }
    return GenericUtils.getTypesSource(
        modelProperty.getObjectType().getSubType(0),
        model.getObjectType(),
        genericType);
  }
}