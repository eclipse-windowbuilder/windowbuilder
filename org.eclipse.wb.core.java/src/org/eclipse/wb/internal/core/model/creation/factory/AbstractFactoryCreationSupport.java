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
package org.eclipse.wb.internal.core.model.creation.factory;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupportUtils;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FactoryAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Implementation of {@link CreationSupport} for creating objects using some
 * {@link MethodInvocation}.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public abstract class AbstractFactoryCreationSupport extends CreationSupport {
  protected CreationSupportUtils m_utils;
  protected MethodDescription m_description;
  protected MethodInvocation m_invocation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractFactoryCreationSupport() {
  }

  public AbstractFactoryCreationSupport(MethodDescription description) {
    Assert.isNotNull(description);
    m_description = description;
  }

  public AbstractFactoryCreationSupport(MethodDescription description, MethodInvocation invocation) {
    this(description);
    m_invocation = invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_utils = new CreationSupportUtils(m_javaInfo);
  }

  @Override
  public final boolean isJavaInfo(ASTNode node) {
    return node == m_invocation;
  }

  @Override
  public final ASTNode getNode() {
    return m_invocation;
  }

  /**
   * @return the {@link MethodInvocation} for creating this {@link JavaInfo}.
   */
  public final MethodInvocation getInvocation() {
    return m_invocation;
  }

  /**
   * @return the {@link MethodDescription} for this factory method.
   */
  public MethodDescription getDescription() {
    return m_description;
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
      m_complexProperty = new ComplexProperty("Factory", "(Factory properties)");
      m_complexProperty.setCategory(PropertyCategory.system(3));
      m_complexProperty.setModified(true);
      // initialize description
      ComponentDescriptionHelper.ensureInitialized(
          m_javaInfo.getEditor().getJavaProject(),
          m_description);
      // prepare list of sub-properties
      List<Property> subPropertiesList = Lists.newArrayList();
      for (ParameterDescription parameter : m_description.getParameters()) {
        Property property = getGenericProperty(parameter);
        if (property != null) {
          subPropertiesList.add(property);
        }
      }
      // set sub-properties
      if (!subPropertiesList.isEmpty()) {
        Property[] subProperties =
            subPropertiesList.toArray(new Property[subPropertiesList.size()]);
        m_complexProperty.setProperties(subProperties);
      }
    }
    // add complex property if there are sub-properties
    if (m_complexProperty.getProperties().length != 0) {
      properties.add(m_complexProperty);
    }
  }

  @Override
  public final void addAccessors(GenericPropertyDescription propertyDescription,
      List<ExpressionAccessor> accessors) throws Exception {
    for (ParameterDescription parameter : m_description.getParameters()) {
      if (propertyDescription.getId().equals(parameter.getProperty())) {
        int index = parameter.getIndex();
        String defaultSource = parameter.getDefaultSource();
        ExpressionAccessor accessor = new FactoryAccessor(index, defaultSource);
        accessors.add(accessor);
      }
    }
  }

  /**
   * @return the {@link GenericPropertyImpl} for given {@link ParameterDescription} of factory.
   */
  protected final Property getGenericProperty(ParameterDescription parameter) throws Exception {
    // try to find JavaInfo passed as parameter
    /*{
    	Expression argument = DomGenerics.arguments(m_invocation).get(parameter.getIndex());
    	JavaInfo javaInfo = m_javaInfo.getRootJava().getChildRepresentedBy(argument);
    	if (javaInfo != null) {
    		ComplexProperty complexProperty = new ComplexProperty(parameter.getName(), "(properties)");
    		complexProperty.setProperties(javaInfo.getProperties());
    		return complexProperty;
    	}
    }*/
    // use GenericProperty
    return m_utils.createProperty(parameter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean canDelete() {
    return true;
  }

  @Override
  public final void delete() throws Exception {
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, true);
  }
}
