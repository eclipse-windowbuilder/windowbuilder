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
package org.eclipse.wb.internal.core.xml.model.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description for creating new component.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class CreationDescription extends AbstractDescription {
  private final ComponentDescription m_componentDescription;
  private final Class<?> m_componentClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationDescription(ComponentDescription componentDescription, String id, String name) {
    m_componentDescription = componentDescription;
    m_componentClass = componentDescription.getComponentClass();
    m_id = id;
    m_name = name != null ? name : CodeUtils.getShortClass(m_componentClass.getName());
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
  // Attributes
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<CreationAttributeDescription> m_attributes = Lists.newArrayList();

  /**
   * @return attributes to set on creation.
   */
  public List<CreationAttributeDescription> getAttributes() {
    return m_attributes;
  }

  /**
   * Adds new {@link CreationAttributeDescription}.
   */
  public void addAttribute(CreationAttributeDescription attribute) {
    m_attributes.add(attribute);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Content
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_content;

  /**
   * @return the text to use as content of element on creation.
   */
  public String getContent() {
    return m_content;
  }

  /**
   * Sets the text to use as content of element on creation.
   */
  public void setContent(String content) {
    m_content = content;
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
}
