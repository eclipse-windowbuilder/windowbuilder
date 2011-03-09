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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IASTObjectInfo2;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.BindingContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.TypeObjectInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.BindingNameUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ConverterUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ValidatorUiContentProvider;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for {@link org.jdesktop.beansbinding.Binding}.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public abstract class BindingInfo extends AstObjectInfo implements IBindingInfo, IASTObjectInfo2 {
  private static final String SET_CONVERTER =
      "org.jdesktop.beansbinding.Binding.setConverter(org.jdesktop.beansbinding.Converter)";
  private static final String SET_VALIDATOR =
      "org.jdesktop.beansbinding.Binding.setValidator(org.jdesktop.beansbinding.Validator)";
  //
  private static ChooseClassConfiguration m_converterConfiguration;
  private static ChooseClassConfiguration m_validatorConfiguration;
  //
  protected final ObserveInfo m_target;
  protected final ObserveInfo m_targetProperty;
  protected final PropertyInfo m_targetAstProperty;
  protected final ObserveInfo m_model;
  protected final ObserveInfo m_modelProperty;
  protected final PropertyInfo m_modelAstProperty;
  protected ConverterInfo m_converter;
  protected ValidatorInfo m_validator;
  private String m_name;
  private boolean m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingInfo(ObserveInfo target,
      ObserveInfo targetProperty,
      PropertyInfo targetAstProperty,
      ObserveInfo model,
      ObserveInfo modelProperty,
      PropertyInfo modelAstProperty) {
    m_target = target;
    m_targetProperty = targetProperty;
    m_targetAstProperty = targetAstProperty;
    m_model = model;
    m_modelProperty = modelProperty;
    m_modelAstProperty = modelAstProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveInfo getTarget() {
    return m_target;
  }

  public final IObserveInfo getTargetProperty() {
    return m_targetProperty;
  }

  public IGenericType getTargetPropertyType() {
    return m_targetProperty.getObjectType();
  }

  public final PropertyInfo getTargetAstProperty() {
    return m_targetAstProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveInfo getModel() {
    return m_model;
  }

  public final IObserveInfo getModelProperty() {
    return m_modelProperty;
  }

  public IGenericType getModelPropertyType() {
    return m_modelProperty.getObjectType();
  }

  public final PropertyInfo getModelAstProperty() {
    return m_modelAstProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Converter
  //
  ////////////////////////////////////////////////////////////////////////////
  public final ConverterInfo getConverter() {
    return m_converter;
  }

  public final void setConverter(ConverterInfo converter) {
    m_converter = converter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  public final ValidatorInfo getValidator() {
    return m_validator;
  }

  public final void setValidator(ValidatorInfo validator) {
    m_validator = validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   * 
   * @param provider
   *          TODO
   */
  public void createContentProviders(List<BindingInfo> bindings,
      List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    // configure page
    listener.setTitle("Properties");
    listener.setMessage("Choose properties for the target and the model.");
    // add target editors
    providers.add(new LabelUiContentProvider("Target:", getTargetPresentationText(false)));
    m_targetProperty.createContentProviders(providers, m_target, m_targetAstProperty);
    // add model editors
    providers.add(new LabelUiContentProvider("Model:", getModelPresentationText(false)));
    m_modelProperty.createContentProviders(providers, m_model, m_modelAstProperty);
    // binding self properties
    providers.add(new ConverterUiContentProvider(createConverterConfiguration(), this));
    providers.add(new ValidatorUiContentProvider(createValidatorConfiguration(), this));
    providers.add(new BindingNameUiContentProvider(this));
    providers.add(new BindingContentProvider(this, provider.getJavaInfoRoot()));
  }

  protected final ChooseClassConfiguration createConverterConfiguration() {
    if (m_converterConfiguration == null) {
      m_converterConfiguration = new ChooseClassConfiguration();
      m_converterConfiguration.setDialogFieldLabel("Converter:");
      m_converterConfiguration.setValueScope("org.jdesktop.beansbinding.Converter");
      m_converterConfiguration.setClearValue("N/S");
      m_converterConfiguration.setBaseClassName("org.jdesktop.beansbinding.Converter");
      m_converterConfiguration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
      m_converterConfiguration.setEmptyClassErrorMessage("Converter class is empty.");
      m_converterConfiguration.setErrorMessagePrefix("Converter");
    }
    m_converterConfiguration.clearDefaultStrings();
    if (m_converter != null && !StringUtils.isEmpty(m_converter.getParameters())) {
      m_converterConfiguration.addDefaultStart(m_converter.getFullClassName());
    }
    return m_converterConfiguration;
  }

  protected final ChooseClassConfiguration createValidatorConfiguration() {
    if (m_validatorConfiguration == null) {
      m_validatorConfiguration = new ChooseClassConfiguration();
      m_validatorConfiguration.setDialogFieldLabel("Validator:");
      m_validatorConfiguration.setValueScope("org.jdesktop.beansbinding.Validator");
      m_validatorConfiguration.setClearValue("N/S");
      m_validatorConfiguration.setBaseClassName("org.jdesktop.beansbinding.Validator");
      m_validatorConfiguration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
      m_validatorConfiguration.setEmptyClassErrorMessage("Validator class is empty.");
      m_validatorConfiguration.setErrorMessagePrefix("Validator");
    }
    m_validatorConfiguration.clearDefaultStrings();
    if (m_validator != null && !StringUtils.isEmpty(m_validator.getParameters())) {
      m_validatorConfiguration.addDefaultStart(m_validator.getFullClassName());
    }
    return m_validatorConfiguration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  public final boolean isField() {
    return m_field;
  }

  public final void setField() {
    m_field = true;
  }

  protected final void setVariableIdentifier(final JavaInfo javaInfoRoot,
      final String type,
      final String newVariable,
      boolean newFieldState) {
    try {
      boolean oldFieldState = m_field;
      m_field = newFieldState;
      //
      final String oldVariable = getVariableIdentifier();
      setVariableIdentifier(newVariable);
      final TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(javaInfoRoot);
      //
      if (!oldFieldState && newFieldState) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            BodyDeclarationTarget fieldTarget = new BodyDeclarationTarget(rootNode, null, true);
            javaInfoRoot.getEditor().addFieldDeclaration(
                "private " + type + " " + newVariable + ";",
                fieldTarget);
          }
        });
      } else if (oldFieldState && !newFieldState) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            for (FieldDeclaration field : rootNode.getFields()) {
              VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
              if (fragment.getName().getIdentifier().equals(oldVariable)) {
                javaInfoRoot.getEditor().removeBodyDeclaration(field);
                return;
              }
            }
            Assert.fail("Undefine binding field: " + oldVariable);
          }
        });
      } else if (oldFieldState && newFieldState) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            for (FieldDeclaration field : rootNode.getFields()) {
              VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
              if (fragment.getName().getIdentifier().equals(oldVariable)) {
                javaInfoRoot.getEditor().setIdentifier(fragment.getName(), newVariable);
                return;
              }
            }
            Assert.fail("Undefine binding field: " + oldVariable);
          }
        });
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //  Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @see to org.jdesktop.beansbinding.Binding.isManaged()
   */
  public boolean isManaged() {
    return false;
  }

  protected final void addFinishSourceCode(List<String> lines,
      CodeGenerationSupport generationSupport,
      boolean addBind) throws Exception {
    // converter
    if (m_converter != null) {
      lines.add(getVariableIdentifier()
          + ".setConverter("
          + m_converter.getSourceCode(lines, generationSupport)
          + ");");
    }
    // validator
    if (m_validator != null) {
      lines.add(getVariableIdentifier()
          + ".setValidator("
          + m_validator.getSourceCode(lines, generationSupport)
          + ");");
    }
    // bind
    if (addBind) {
      lines.add(getVariableIdentifier() + ".bind();");
    }
  }

  protected final String getCreateMethodHeaderEnd() {
    if (StringUtils.isEmpty(m_name)) {
      return ")";
    }
    return ", \"" + StringUtilities.escapeJava(m_name) + "\")";
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
    if (SET_CONVERTER.equals(signature)) {
      // Binding.setConverter(Converter)
      Assert.isNull(m_converter);
      TypeObjectInfo converter = (TypeObjectInfo) resolver.getModel(arguments[0]);
      if (converter == null) {
        AbstractParser.addError(
            editor,
            "Converter argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      m_converter = new ConverterInfo(converter.getObjectType(), this);
      m_converter.setParameters(converter.getParameters());
      m_converter.setVariableIdentifier(converter.getVariableIdentifier());
    } else if (SET_VALIDATOR.equals(signature)) {
      // Binding.setValidator(Validator)
      Assert.isNull(m_validator);
      TypeObjectInfo validator = (TypeObjectInfo) resolver.getModel(arguments[0]);
      if (validator == null) {
        AbstractParser.addError(
            editor,
            "Validator argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      m_validator = new ValidatorInfo(validator.getObjectType(), this);
      m_validator.setParameters(validator.getParameters());
      m_validator.setVariableIdentifier(validator.getVariableIdentifier());
    }
    return null;
  }

  public void preCreate() throws Exception {
  }

  /**
   * This method is invoked as last step of parsing.
   */
  public void create(List<BindingInfo> bindings) throws Exception {
    m_target.createBinding(this);
    m_targetProperty.createBinding(this);
    m_model.createBinding(this);
    m_modelProperty.createBinding(this);
  }

  public void edit(List<BindingInfo> bindings) throws Exception {
  }

  public boolean delete(List<BindingInfo> bindings) throws Exception {
    return true;
  }

  public void postDelete() throws Exception {
    m_target.deleteBinding(this);
    m_targetProperty.deleteBinding(this);
    m_model.deleteBinding(this);
    m_modelProperty.deleteBinding(this);
  }

  public void move(List<BindingInfo> bindings) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getTargetPresentationText(boolean full) throws Exception {
    return m_targetAstProperty.getPresentationText(m_target, m_targetProperty, full);
  }

  public String getModelPresentationText(boolean full) throws Exception {
    return m_modelAstProperty.getPresentationText(m_model, m_modelProperty, full);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    m_targetAstProperty.accept(visitor);
    m_modelAstProperty.accept(visitor);
  }
}