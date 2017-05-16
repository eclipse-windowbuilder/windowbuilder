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
package org.eclipse.wb.internal.core.model.description;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description for creating (adding) new component.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class CreationDescription extends AbstractDescription {
  private final ComponentDescription m_componentDescription;
  private final Class<?> m_componentClass;
  private final String m_componentClassName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationDescription(ComponentDescription componentDescription,
      String creationId,
      String name) {
    m_componentDescription = componentDescription;
    m_componentClass = componentDescription.getComponentClass();
    m_componentClassName = ReflectionUtils.getCanonicalName(m_componentClass);
    m_id = creationId;
    m_name = name != null ? name : CodeUtils.getShortClass(m_componentClassName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // id
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_id;

  /**
   * @return identifier of this creation.
   */
  public String getId() {
    return m_id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // icon
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_icon;

  /**
   * @return the icon of this creation for displaying for user.
   */
  public Image getIcon() {
    return m_icon != null ? m_icon : m_componentDescription.getIcon();
  }

  /**
   * Sets the icon of this creation for displaying for user.
   */
  public void setIcon(Image icon) {
    m_icon = icon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // name
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_name;

  /**
   * @return the name of this creation for displaying for user.
   */
  public String getName() {
    return m_name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // description
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_description;

  /**
   * @return the description of this creation for displaying for user.
   */
  public String getDescription() {
    return m_description != null ? m_description : m_componentDescription.getDescription();
  }

  /**
   * Sets the description of this creation for displaying for user.
   */
  public void setDescription(String description) {
    m_description = description != null ? StringUtilities.normalizeWhitespaces(description) : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_source;

  /**
   * @return the source for creating new component instance.
   */
  public String getSource() {
    return m_source;
  }

  /**
   * Sets that source for creating new component instance.
   */
  public void setSource(String source) {
    m_source = evaluate(source);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Invocations
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<CreationInvocationDescription> m_invocations = Lists.newArrayList();

  /**
   * @return the {@link List} of {@link CreationInvocationDescription}.
   */
  public List<CreationInvocationDescription> getInvocations() {
    return m_invocations;
  }

  /**
   * Adds the {@link CreationInvocationDescription}.
   */
  public void addInvocation(CreationInvocationDescription invocation) {
    m_invocations.add(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, String> m_parameters = Maps.newTreeMap();

  /**
   * @return the {@link CreationDescription} specific parameters.
   */
  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(m_parameters);
  }

  /**
   * Adds new parameter.
   */
  public void addParameter(String name, String value) {
    m_parameters.put(name, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generics
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class TypeParameterDescription {
    private final String m_typeName;
    private final String m_title;

    public TypeParameterDescription(String typeName, String title) {
      m_typeName = typeName;
      m_title = title;
    }

    public String getTypeName() {
      return m_typeName;
    }

    public String getTitle() {
      return m_title;
    }
  }

  private Map<String, TypeParameterDescription> m_typeArguments;

  /**
   * @return the {@link Map} of generic parameters.
   */
  public Map<String, TypeParameterDescription> getTypeParameters() {
    return m_typeArguments != null
        ? m_typeArguments
        : ImmutableMap.<String, TypeParameterDescription>of();
  }

  /**
   * Adds new generic parameter info.
   */
  public void setTypeParameter(String name, String typeName, String title) {
    if (m_typeArguments == null) {
      m_typeArguments = Maps.newLinkedHashMap();
    }
    m_typeArguments.put(name, new TypeParameterDescription(typeName, title));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Do any replaces in given template (with <code>%pattern%</code>).
   */
  private String evaluate(String s) {
    return StringUtils.replace(s, "%component.class%", m_componentClassName);
  }
}
