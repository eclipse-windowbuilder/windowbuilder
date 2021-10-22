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
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.JListDetailContainerUiContentProvider;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

/**
 * Model for {@link org.jdesktop.swingbinding.JListBinding}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class JListBindingInfo extends AutoBindingInfo {
  private static final String DETAIL_BINDING_1 =
      "org.jdesktop.swingbinding.JListBinding.setDetailBinding(org.jdesktop.beansbinding.Property)";
  private static final String DETAIL_BINDING_2 =
      "org.jdesktop.swingbinding.JListBinding.setDetailBinding(org.jdesktop.beansbinding.Property,java.lang.String)";
  //
  private static final IGenericType JLIST_CLASS = new ClassGenericType(javax.swing.JList.class,
      null,
      null);
  //
  private DetailBindingInfo m_detailBinding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JListBindingInfo(UpdateStrategyInfo strategyInfo,
      ObserveInfo target,
      ObserveInfo targetProperty,
      PropertyInfo targetAstProperty,
      ObserveInfo model,
      ObserveInfo modelProperty,
      PropertyInfo modelAstProperty) {
    super(strategyInfo,
        target,
        targetProperty,
        targetAstProperty,
        model,
        modelProperty,
        modelAstProperty);
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
    // JListBinding.setDetailBinding(Property, [String])
    if (DETAIL_BINDING_1.equals(signature) || DETAIL_BINDING_2.equals(signature)) {
      Assert.isNull(m_detailBinding);
      m_detailBinding = createDetailBinding();
      m_detailBinding.setDetailProperty((PropertyInfo) resolver.getModel(arguments[0]));
      //
      if (signature.endsWith(",java.lang.String)")) {
        String name = CoreUtils.evaluate(String.class, editor, arguments[arguments.length - 1]);
        m_detailBinding.setName(StringEscapeUtils.unescapeJava(name));
      }
      //
      return m_detailBinding;
    }
    return super.parseExpression(editor, signature, invocation, arguments, resolver, provider);
  }

  @Override
  public void create(List<BindingInfo> bindings) throws Exception {
    super.create(bindings);
    if (m_detailBinding == null) {
      m_detailBinding = createDefaultDetailBinding();
    }
    bindings.add(bindings.indexOf(this) + 1, m_detailBinding);
    preCreate();
  }

  @Override
  public boolean delete(List<BindingInfo> bindings) throws Exception {
    m_detailBinding.postDelete();
    bindings.remove(m_detailBinding);
    return true;
  }

  @Override
  public void move(List<BindingInfo> bindings) {
    bindings.remove(m_detailBinding);
    bindings.add(bindings.indexOf(this) + 1, m_detailBinding);
  }

  public DetailBindingInfo createDefaultDetailBinding() {
    DetailBindingInfo binding = createDetailBinding();
    binding.setDetailProperty(new ObjectPropertyInfo(getInputElementType()));
    return binding;
  }

  private DetailBindingInfo createDetailBinding() {
    if (isJListBinding(m_target, m_targetProperty)) {
      return new DetailBindingInfo(m_target,
          m_targetProperty,
          m_targetAstProperty,
          m_model,
          m_modelProperty,
          m_modelAstProperty,
          this);
    }
    return new DetailBindingInfo(m_model,
        m_modelProperty,
        m_modelAstProperty,
        m_target,
        m_targetProperty,
        m_targetAstProperty,
        this);
  }

  @Override
  public void preCreate() throws Exception {
    BeanPropertyObserveInfo selectedElement = getSelectedElementProperty();
    selectedElement.setHostedType(getInputElementType());
  }

  @Override
  public void postDelete() throws Exception {
    super.postDelete();
    BeanPropertyObserveInfo selectedElement = getSelectedElementProperty();
    selectedElement.setHostedType(ClassGenericType.OBJECT_CLASS);
  }

  private BeanPropertyObserveInfo getSelectedElementProperty() throws Exception {
    return BeanSupport.getProperty(
        this,
        isJListBinding(m_target, m_targetProperty),
        "selectedElement");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IGenericType getInputElementType() {
    if (isJListBinding(m_target, m_targetProperty)) {
      return m_modelProperty.getObjectType().getSubType(0);
    }
    return m_targetProperty.getObjectType().getSubType(0);
  }

  public String getTypeSourceCode(CodeGenerationSupport generationSupport) {
    // check generic
    if (!generationSupport.useGenerics()) {
      return "org.jdesktop.swingbinding.JListBinding";
    }
    // calculate types
    IGenericType type0;
    IGenericType type1;
    if (isJListBinding(m_target, m_targetProperty)) {
      if (m_modelAstProperty instanceof ObjectPropertyInfo) {
        type0 = m_model.getObjectType().getSubType(0);
        type1 = m_model.getObjectType();
      } else {
        type0 = m_modelProperty.getObjectType().getSubType(0);
        type1 = m_model.getObjectType();
      }
    } else {
      if (m_targetAstProperty instanceof ObjectPropertyInfo) {
        type0 = m_target.getObjectType().getSubType(0);
        type1 = m_target.getObjectType();
      } else {
        type0 = m_targetProperty.getObjectType().getSubType(0);
        type1 = m_target.getObjectType();
      }
    }
    // source code
    return "org.jdesktop.swingbinding.JListBinding"
        + GenericUtils.getTypesSource(type0, type1, JLIST_CLASS);
  }

  public DetailBindingInfo getDetailBinding() {
    if (m_detailBinding == null) {
      m_detailBinding = createDefaultDetailBinding();
    }
    return m_detailBinding;
  }

  public void setDetailBinding(DetailBindingInfo detailBinding, List<BindingInfo> bindings)
      throws Exception {
    int index = bindings.indexOf(this);
    if (index == -1) {
      m_detailBinding = detailBinding;
    } else {
      if (m_detailBinding != null) {
        bindings.remove(m_detailBinding);
        m_detailBinding.postDelete();
      }
      m_detailBinding = detailBinding;
      bindings.add(index + 1, m_detailBinding);
    }
    m_detailBinding.create(bindings);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<BindingInfo> bindings,
      List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    super.createContentProviders(bindings, providers, listener, provider);
    providers.add(new JListDetailContainerUiContentProvider(createTabConfiguration(),
        this,
        bindings,
        provider));
  }

  private TabContainerConfiguration createTabConfiguration() {
    TabContainerConfiguration configuration = new TabContainerConfiguration();
    configuration.setUseRemoveButton(true);
    return configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //  Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    if (isJListBinding(m_target, m_targetProperty)) {
      addSourceCode(
          m_target,
          m_model,
          m_modelProperty,
          m_modelAstProperty,
          lines,
          generationSupport);
    } else {
      addSourceCode(
          m_model,
          m_target,
          m_targetProperty,
          m_targetAstProperty,
          lines,
          generationSupport);
    }
  }

  private void addSourceCode(ObserveInfo component,
      ObserveInfo model,
      ObserveInfo modelProperty,
      PropertyInfo modelAstProperty,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    // handle variable
    if (getVariableIdentifier() == null) {
      setVariableIdentifier(generationSupport.generateLocalName("JListBinding"));
    }
    // begin
    StringBuffer line = new StringBuffer();
    boolean localVariable = !isField();
    if (localVariable) {
      line.append("org.jdesktop.swingbinding.JListBinding");
    }
    if (modelAstProperty instanceof ObjectPropertyInfo) {
      if (localVariable) {
        if (generationSupport.useGenerics()) {
          line.append(GenericUtils.getTypesSource(
              model.getObjectType().getSubType(0),
              model.getObjectType(),
              JLIST_CLASS));
        }
        line.append(" ");
      }
      line.append(getVariableIdentifier());
      line.append(" = org.jdesktop.swingbinding.SwingBindings.createJListBinding(");
      line.append(m_strategyInfo.getStrategySourceCode());
      line.append(", ");
      line.append(model.getReference());
      line.append(", ");
      line.append(component.getReference());
    } else {
      generationSupport.addSourceCode(modelAstProperty, lines);
      if (localVariable) {
        if (generationSupport.useGenerics()) {
          line.append(GenericUtils.getTypesSource(
              modelProperty.getObjectType().getSubType(0),
              model.getObjectType(),
              JLIST_CLASS));
        }
        line.append(" ");
      }
      line.append(getVariableIdentifier());
      line.append(" = org.jdesktop.swingbinding.SwingBindings.createJListBinding(");
      line.append(m_strategyInfo.getStrategySourceCode());
      line.append(", ");
      line.append(model.getReference());
      line.append(", ");
      line.append(modelAstProperty.getVariableIdentifier());
      line.append(", ");
      line.append(component.getReference());
    }
    // end
    line.append(getCreateMethodHeaderEnd());
    line.append(";");
    lines.add(line.toString());
    // detail
    if (!m_detailBinding.isVirtual()) {
      lines.add("//");
      m_detailBinding.addDetailSourceCode(lines, generationSupport);
      lines.add("//");
    }
    // converter & validator
    addFinishSourceCode(lines, generationSupport, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
    String type = "org.jdesktop.swingbinding.JListBinding";
    if (CoreUtils.useGenerics(javaInfoRoot.getEditor().getJavaProject())) {
      if (isJListBinding(m_target, m_targetProperty)) {
        type += getTypeSource(m_model, m_modelProperty, m_modelAstProperty, JLIST_CLASS);
      } else {
        type += getTypeSource(m_target, m_targetProperty, m_targetAstProperty, JLIST_CLASS);
      }
    }
    setVariableIdentifier(javaInfoRoot, type, variable, field);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isJListBinding(ObserveInfo observe, ObserveInfo propertyObserve) {
    return observe.getCreationType() == ObserveCreationType.JListBinding
        && propertyObserve.getCreationType() == ObserveCreationType.SelfProperty;
  }
}