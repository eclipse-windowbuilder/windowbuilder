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
package org.eclipse.wb.internal.rcp.databinding.model.context;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IASTObjectInfo2;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.BindingContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;

/**
 * Abstract model for
 * <code>DataBindingContext.bindXXX(target, model, targetStrategy, modelStrategy)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public abstract class BindingInfo extends AbstractBindingInfo implements IASTObjectInfo2 {
  protected final ObservableInfo m_target;
  protected final ObservableInfo m_model;
  protected UpdateStrategyInfo m_targetStrategy;
  protected UpdateStrategyInfo m_modelStrategy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingInfo(ObservableInfo target, ObservableInfo model) {
    m_target = target;
    m_model = model;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return target {@link ObservableInfo}.
   */
  public final ObservableInfo getTargetObservable() {
    return m_target;
  }

  /**
   * @return model {@link ObservableInfo}.
   */
  public final ObservableInfo getModelObservable() {
    return m_model;
  }

  /**
   * @return target {@link UpdateStrategyInfo}.
   */
  public final UpdateStrategyInfo getTargetStrategy() {
    return m_targetStrategy;
  }

  /**
   * @return model {@link UpdateStrategyInfo}.
   */
  public final UpdateStrategyInfo getModelStrategy() {
    return m_modelStrategy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IBindingInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveInfo getTarget() {
    return m_target.getBindableObject();
  }

  public final IObserveInfo getTargetProperty() {
    return m_target.getBindableProperty();
  }

  public final IObserveInfo getModel() {
    return m_model.getBindableObject();
  }

  public final IObserveInfo getModelProperty() {
    return m_model.getBindableProperty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    // configure page
    listener.setTitle(Messages.BindingInfo_listenerTitle);
    listener.setMessage(Messages.BindingInfo_listenerMessage);
    //
    BindingUiContentProviderContext context = new BindingUiContentProviderContext();
    // add target editors
    context.setDirection("Target");
    providers.add(new LabelUiContentProvider(Messages.BindingInfo_targetLabel,
        m_target.getPresentationText()));
    m_target.createContentProviders(providers, context, provider);
    m_targetStrategy.createContentProviders(providers, context);
    //
    providers.add(new SeparatorUiContentProvider());
    // add model editors
    context.setDirection("Model");
    providers.add(new LabelUiContentProvider(Messages.BindingInfo_modelLabel,
        m_model.getPresentationText()));
    m_model.createContentProviders(providers, context, provider);
    m_modelStrategy.createContentProviders(providers, context);
    //
    // binding self properties
    providers.add(new SeparatorUiContentProvider());
    providers.add(new BindingContentProvider(this, provider.getJavaInfoRoot()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isField() {
    try {
      return getVariableIdentifier() != null;
    } catch (Throwable e) {
      return false;
    }
  }

  public void setField() {
  }

  public void setVariableIdentifier(final JavaInfo javaInfoRoot,
      final String newVariable,
      boolean field) {
    try {
      final String oldVariable = getVariableIdentifier();
      setVariableIdentifier(newVariable);
      final TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(javaInfoRoot);
      //
      if (oldVariable == null && newVariable != null) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            BodyDeclarationTarget fieldTarget = new BodyDeclarationTarget(rootNode, null, true);
            javaInfoRoot.getEditor().addFieldDeclaration(
                "private org.eclipse.core.databinding.Binding " + newVariable + ";",
                fieldTarget);
          }
        });
      } else if (oldVariable != null && newVariable == null) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            for (FieldDeclaration field : rootNode.getFields()) {
              VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
              if (fragment.getName().getIdentifier().equals(oldVariable)) {
                javaInfoRoot.getEditor().removeBodyDeclaration(field);
                return;
              }
            }
            Assert.fail(Messages.BindingInfo_undefinedBindingField + oldVariable);
          }
        });
      } else if (oldVariable != null && newVariable != null && !oldVariable.equals(newVariable)) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            for (FieldDeclaration field : rootNode.getFields()) {
              VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
              if (fragment.getName().getIdentifier().equals(oldVariable)) {
                javaInfoRoot.getEditor().setIdentifier(fragment.getName(), newVariable);
                return;
              }
            }
            Assert.fail(Messages.BindingInfo_undefinedBindingField + oldVariable);
          }
        });
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Definition
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getDefinitionSource(DatabindingsProvider provider) throws Exception {
    DataBindingContextInfo context = provider.getRootInfo().getContextInfo();
    return context.getVariableIdentifier()
        + "."
        + getBindingMethod()
        + "("
        + m_target.getVariableIdentifier()
        + ", "
        + m_model.getVariableIdentifier();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void addSourceCode(DataBindingContextInfo context,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    // target source code
    generationSupport.addSourceCode(m_target, lines);
    // model source code
    generationSupport.addSourceCode(m_model, lines);
    // target strategy source code
    String targetStrategy = m_targetStrategy.getSourceCode(lines, generationSupport);
    // model strategy source code
    String modelStrategy = m_modelStrategy.getSourceCode(lines, generationSupport);
    // create binding source code
    String variable = getVariableIdentifier();
    if (variable == null) {
      variable = "";
    } else {
      variable += " = ";
    }
    lines.add(variable
        + context.getVariableIdentifier()
        + "."
        + getBindingMethod()
        + "("
        + m_target.getVariableIdentifier()
        + ", "
        + m_model.getVariableIdentifier()
        + ", "
        + targetStrategy
        + ", "
        + modelStrategy
        + ");");
  }

  protected abstract String getBindingMethod();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    // target
    m_target.accept(visitor);
    m_targetStrategy.accept(visitor);
    // model
    m_model.accept(visitor);
    m_modelStrategy.accept(visitor);
  }
}