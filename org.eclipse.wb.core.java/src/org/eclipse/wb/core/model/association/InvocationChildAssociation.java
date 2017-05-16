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
package org.eclipse.wb.core.model.association;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.InvocationChildAssociationAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.awt.Container;
import java.util.List;

/**
 * Implementation of {@link Association} for {@link MethodInvocation} as separate
 * {@link ExpressionStatement}, when <em>child</em> passed as argument. Often used in Swing:
 * {@link Container#add(java.awt.Component, Object)}.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class InvocationChildAssociation extends InvocationAssociation {
  private String m_source;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public InvocationChildAssociation(String source) {
    Assert.isTrue(
        source == null || source.startsWith("%parent%."),
        "Source should start with %%parent%%., but '%s' found.",
        source);
    m_source = source;
  }

  public InvocationChildAssociation(MethodInvocation invocation) {
    super(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MethodDescription} (from parent {@link JavaInfo}) used for this association.
   */
  public MethodDescription getDescription() {
    String signature = AstNodeUtils.getMethodSignature(m_invocation);
    return m_javaInfo.getParentJava().getDescription().getMethod(signature);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo javaInfo, StatementTarget target, String[] leadingComments)
      throws Exception {
    // initialize MethodInvocation instance
    if (m_source != null) {
      // add new statement
      String source = AssociationUtils.replaceTemplates(javaInfo, m_source, target);
      List<String> lines = GenericsUtils.asList(leadingComments, source + ";");
      ExpressionStatement statement =
          (ExpressionStatement) javaInfo.getEditor().addStatement(lines, target);
      m_invocation = (MethodInvocation) statement.getExpression();
    } else {
      // we expect that JavaInfo has EmptyVariableSupport with argument in MethodInvocation
      EmptyVariableSupport emptyVariableSupport =
          (EmptyVariableSupport) javaInfo.getVariableSupport();
      m_invocation = (MethodInvocation) emptyVariableSupport.getInitializer().getParent();
    }
    // add related nodes
    AbstractInsideStatementGenerator.addRelatedNodes(javaInfo, m_invocation);
    // set association
    setInModelNoCompound(javaInfo);
  }

  @Override
  public void move(StatementTarget target) throws Exception {
    // may be move constraints (will move invocation too)
    {
      MethodDescription description = getDescription();
      List<ParameterDescription> parameters = description.getParameters();
      if (parameters.size() == 2
          && parameters.get(0).isChild()
          && parameters.get(0).isParent2()
          && parameters.get(1).isChild2()) {
        Expression constraintsExpression = DomGenerics.arguments(m_invocation).get(1);
        JavaInfo constraints = m_javaInfo.getChildRepresentedBy(constraintsExpression);
        if (constraints != null) {
          constraints.getVariableSupport().ensureInstanceReadyAt(target);
          return;
        }
      }
    }
    // no constraints, move single statement
    {
      Statement statement = getStatement();
      m_editor.moveStatement(statement, target);
    }
  }

  @Override
  public boolean remove() throws Exception {
    // ensure that "parent" is not created in Statement
    {
      ObjectInfo parent = m_javaInfo.getParent();
      if (parent instanceof JavaInfo && !parent.isDeleting()) {
        JavaInfoUtils.materializeVariable((JavaInfo) parent);
      }
    }
    // remove Statement
    m_editor.removeEnclosingStatement(m_invocation);
    // continue
    return super.remove();
  }

  @Override
  public Association getCopy() {
    return new InvocationChildAssociation(m_invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_properties_oldSignature;
  private ComplexProperty m_properties_oldProperty;

  @Override
  public void addProperties(List<Property> properties) throws Exception {
    Property associationProperty = getAssociationProperty();
    if (associationProperty != null) {
      properties.add(associationProperty);
    }
  }

  /**
   * @return the new or existing "Association" {@link Property}, may be <code>null</code> if no
   *         sub-properties.
   */
  private Property getAssociationProperty() throws Exception {
    String signature = AstNodeUtils.getMethodSignature(m_invocation);
    if (m_properties_oldSignature != null && m_properties_oldSignature.equals(signature)) {
      // do nothing, use cached
    } else {
      List<Property> associationProperties = createAssociationSubProperties(signature);
      if (!associationProperties.isEmpty()) {
        m_properties_oldProperty = new ComplexProperty("Association", "(Association properties)");
        m_properties_oldProperty.setCategory(PropertyCategory.system(4));
        m_properties_oldProperty.setModified(true);
        m_properties_oldProperty.setProperties(associationProperties);
      } else {
        m_properties_oldProperty = null;
      }
    }
    // return cached
    m_properties_oldSignature = signature;
    return m_properties_oldProperty;
  }

  /**
   * @return the new {@link Property}'s to edit arguments in association {@link MethodInvocation}.
   */
  private List<Property> createAssociationSubProperties(String signature) throws Exception {
    // prepare description for association method
    MethodDescription description;
    {
      ComponentDescription parentDescription = m_javaInfo.getParentJava().getDescription();
      description = parentDescription.getMethod(signature);
      ComponentDescriptionHelper.ensureInitialized(m_javaProject, description);
    }
    // add properties
    List<Property> associationProperties = Lists.newArrayList();
    for (ParameterDescription parameter : description.getParameters()) {
      if (!parameter.isChild()) {
        Property property = createAssociationSubProperty(parameter);
        if (property != null) {
          associationProperties.add(property);
        }
      }
    }
    return associationProperties;
  }

  /**
   * @return the new {@link Property} to edit expression of given {@link ParameterDescription} in
   *         association {@link MethodInvocation}.
   */
  private Property createAssociationSubProperty(ParameterDescription parameter) throws Exception {
    int index = parameter.getIndex();
    String name = parameter.getName();
    String defaultSource = parameter.getDefaultSource();
    // prepare Property elements
    ExpressionAccessor accessor = new InvocationChildAssociationAccessor(index, defaultSource);
    ExpressionConverter converter = parameter.getConverter();
    PropertyEditor editor = parameter.getEditor();
    if (editor == null) {
      return null;
    }
    // create generic Property
    return new GenericPropertyImpl(m_javaInfo,
        name,
        new ExpressionAccessor[]{accessor},
        defaultSource,
        converter,
        editor);
  }
}
