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
package org.eclipse.wb.internal.core.model.creation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription.TypeParameterDescription;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implementation of {@link CreationSupport} for creating objects using constructors.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class ConstructorCreationSupport extends CreationSupport
    implements
      ILiveCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Existing state
  //
  ////////////////////////////////////////////////////////////////////////////
  private ClassInstanceCreation m_creation;
  private IMethodBinding m_binding;
  private ConstructorDescription m_description;
  private CreationSupportUtils m_utils;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_addInvocations;
  private String m_creationId;
  private String m_creationSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructor for existing {@link ClassInstanceCreation}, used during parsing.
   */
  public ConstructorCreationSupport(ClassInstanceCreation creation) {
    setCreation(creation);
  }

  /**
   * Constructor for adding new {@link JavaInfo} using default creation.
   */
  public ConstructorCreationSupport() {
    this(null, true);
  }

  /**
   * Constructor for adding new {@link JavaInfo} using creation wit specified id.
   *
   * @param creationId
   *          the id of {@link CreationDescription} to use during add.
   * @param addInvocations
   *          is <code>true</code> if invocations from {@link CreationDescription} during
   *          {@link #add_setSourceExpression(Expression)}.
   *
   */
  public ConstructorCreationSupport(String creationId, boolean addInvocations) {
    m_creationId = creationId;
    m_addInvocations = addInvocations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ConstructorCreationSupport} that uses specified source. No invocations.
   */
  public static ConstructorCreationSupport forSource(String source) {
    ConstructorCreationSupport creation = new ConstructorCreationSupport();
    creation.m_creationSource = source;
    creation.m_addInvocations = false;
    return creation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link ClassInstanceCreation}.
   */
  private void setCreation(ClassInstanceCreation creation) {
    m_creation = creation;
    m_binding = AstNodeUtils.getCreationBinding(m_creation);
    initializeConstructorDescription();
  }

  /**
   * Initializes {@link #m_description}. We do this in separate method and not in
   * {@link #setCreation(ClassInstanceCreation)} because when we call
   * {@link #setCreation(ClassInstanceCreation)} first time from constructor, {@link JavaInfo} is
   * not known yet, so we don't know {@link ComponentDescription} and can not determine
   * {@link ConstructorDescription} .
   */
  private void initializeConstructorDescription() {
    if (m_javaInfo != null && m_creation != null) {
      ComponentDescription componentDescription = m_javaInfo.getDescription();
      m_description = componentDescription.getConstructor(m_binding);
      if (m_description == null) {
        String source = m_javaInfo.getEditor().getSource(m_creation);
        throw new DesignerException(ICoreExceptionConstants.GEN_NO_CONSTRUCTOR_BINDING, source);
      }
      ComponentDescriptionHelper.ensureInitialized(
          m_javaInfo.getEditor().getJavaProject(),
          m_description);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "new: "
        + getComponentClass().getName()
        + (m_creationId != null ? " " + m_creationId : "");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    initializeConstructorDescription();
    // apply parameters from CreationDescription
    {
      CreationDescription creationDescription =
          m_javaInfo.getDescription().getCreation(m_creationId);
      // parameters
      for (Map.Entry<String, String> entry : creationDescription.getParameters().entrySet()) {
        JavaInfoUtils.setParameter(javaInfo, entry.getKey(), entry.getValue());
      }
      // fill type parameters with defaults
      Map<String, TypeParameterDescription> typeParameters =
          creationDescription.getTypeParameters();
      for (Entry<String, TypeParameterDescription> parameter : typeParameters.entrySet()) {
        m_javaInfo.putTemplateArgument(parameter.getKey(), parameter.getValue().getTypeName());
      }
    }
    // prepare Utils
    m_utils = new CreationSupportUtils(m_javaInfo);
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return node == m_creation;
  }

  @Override
  public ASTNode getNode() {
    return m_creation;
  }

  public CreationSupport getLiveComponentCreation() {
    return new ConstructorCreationSupport(m_creationId, m_addInvocations);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ClassInstanceCreation} of this {@link ConstructorCreationSupport}.
   */
  public ClassInstanceCreation getCreation() {
    return m_creation;
  }

  /**
   * @return the {@link IMethodBinding} of this {@link ClassInstanceCreation}.
   */
  public IMethodBinding getBinding() {
    return m_binding;
  }

  /**
   * @return the {@link ConstructorDescription} of this {@link ClassInstanceCreation}.
   */
  public ConstructorDescription getDescription() {
    return m_description;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Set<String> m_validParentSources = Sets.newTreeSet();
  private final Set<String> m_invalidParentSources = Sets.newTreeSet();

  @Override
  public boolean canUseParent(JavaInfo parent) throws Exception {
    Class<?> parentType = parent.getDescription().getComponentClass();
    // if this JavaInfo is not created yet, try to compile creation source with this given "parent"
    if (m_description == null) {
      return canUseParent_noDescription(parent, parentType);
    }
    // check that if constructor has "parent" parameter, it is compatible with given "parent"
    for (ParameterDescription parameter : m_description.getParameters()) {
      if (parameter.isParent()) {
        Class<?> requiredType = parameter.getType();
        if (!requiredType.isAssignableFrom(parentType)) {
          return false;
        }
      }
    }
    // continue
    return true;
  }

  private boolean canUseParent_noDescription(JavaInfo parent, Class<?> parentType) throws Exception {
    String source = add_getSource(null);
    // we don't care if there are no "parent" to check
    if (!source.contains("%parent%")) {
      return true;
    }
    source = StringUtils.replace(source, "%parent%", "(" + parentType.getName() + ") null");
    // check cached result
    if (m_validParentSources.contains(source)) {
      return true;
    }
    if (m_invalidParentSources.contains(source)) {
      return false;
    }
    // check that arguments correspond to parameter types
    {
      int position = parent.getCreationSupport().getNode().getStartPosition();
      ClassInstanceCreation creation =
          (ClassInstanceCreation) m_javaInfo.getEditor().getParser().parseExpression(
              position,
              source);
      IMethodBinding creationBinding = AstNodeUtils.getCreationBinding(creation);
      ITypeBinding[] parameterTypes = creationBinding.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        ITypeBinding parameterType = parameterTypes[i];
        Expression argument = DomGenerics.arguments(creation).get(i);
        if (!AstNodeUtils.isSuccessorOf(argument, parameterType)) {
          m_invalidParentSources.add(source);
          return false;
        }
      }
    }
    // OK
    m_validParentSources.add(source);
    return true;
  }

  @Override
  public boolean canReorder() {
    return true;
  }

  @Override
  public boolean canReparent() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_complexProperty;

  @Override
  public void addProperties(List<Property> properties) throws Exception {
    if (m_complexProperty == null) {
      m_complexProperty = new ComplexProperty("Constructor", "(Constructor properties)");
      m_complexProperty.setCategory(PropertyCategory.system(3));
      m_complexProperty.setModified(true);
      // prepare list of sub-properties
      List<Property> subPropertiesList = Lists.newArrayList();
      for (ParameterDescription parameter : m_description.getParameters()) {
        Property property = m_utils.createProperty(parameter);
        if (property != null) {
          subPropertiesList.add(property);
        }
      }
      // set sub-properties
      if (!subPropertiesList.isEmpty()) {
        m_complexProperty.setProperties(subPropertiesList);
      }
    }
    // add complex property if there are sub-properties
    if (m_complexProperty.getProperties().length != 0) {
      properties.add(m_complexProperty);
    }
  }

  @Override
  public void addAccessors(GenericPropertyDescription propertyDescription,
      List<ExpressionAccessor> accessors) throws Exception {
    // add accessors for parameters bound to given property
    List<ParameterDescription> parameters = m_description.getParameters();
    for (ParameterDescription parameter : parameters) {
      if (propertyDescription.getId().equals(parameter.getProperty())) {
        accessors.add(new ConstructorAccessor(parameter.getIndex(), parameter.getDefaultSource()));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    if (m_creationSource != null) {
      return m_creationSource;
    } else {
      CreationDescription creationDescription =
          m_javaInfo.getDescription().getCreation(m_creationId);
      return creationDescription.getSource();
    }
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    ClassInstanceCreation creation = (ClassInstanceCreation) expression;
    setCreation(creation);
    m_javaInfo.bindToExpression(creation);
    // add invocations
    if (m_addInvocations) {
      CreationDescription creationDescription =
          m_javaInfo.getDescription().getCreation(m_creationId);
      for (CreationInvocationDescription invocation : creationDescription.getInvocations()) {
        m_javaInfo.addMethodInvocation(invocation.getSignature(), invocation.getArguments());
      }
    }
  }

  @Override
  public Association getAssociation() throws Exception {
    if (add_getSource(null).indexOf("%parent%") != -1) {
      return new ConstructorParentAssociation();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    boolean removeFromParent = !m_javaInfo.isRoot();
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, removeFromParent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD_INNER_CLASS")
  public IClipboardCreationSupport getClipboard() throws Exception {
    final String source = getClipboardSource();
    return new IClipboardCreationSupport() {
      private static final long serialVersionUID = 0L;

      @Override
      public CreationSupport create(JavaInfo rootObject) throws Exception {
        return forSource(source);
      }
    };
  }

  /**
   * @return the source for copy/paste.
   */
  private String getClipboardSource() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    String typeArgumentsSource = editor.getTypeArgumentsSource(m_creation);
    String argumentsSource = m_utils.getClipboardArguments(m_description.getParameters());
    String methodStubs = getClipboardSourceMethodStubs(editor);
    return "new "
        + m_javaInfo.getDescription().getComponentClass().getName()
        + typeArgumentsSource
        + "("
        + argumentsSource
        + ")"
        + methodStubs;
  }

  /**
   * @return the {@link AnonymousClassDeclaration} method stubs, may be empty string.
   */
  private String getClipboardSourceMethodStubs(AstEditor editor) throws Exception {
    AnonymousClassDeclaration anonymousDeclaration = m_creation.getAnonymousClassDeclaration();
    if (anonymousDeclaration != null) {
      StringBuilder sb = new StringBuilder();
      sb.append(" {\n");
      List<MethodDeclaration> methodDeclarations =
          DomGenerics.methodDeclarations(anonymousDeclaration);
      for (MethodDeclaration methodDeclaration : methodDeclarations) {
        String stubSource = editor.getMethodStubSource(methodDeclaration);
        sb.append(stubSource);
        sb.append("\n");
      }
      sb.append("}");
      return sb.toString();
    } else {
      return "";
    }
  }
}
