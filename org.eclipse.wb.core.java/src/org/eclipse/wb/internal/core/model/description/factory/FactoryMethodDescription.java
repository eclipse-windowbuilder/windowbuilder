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
package org.eclipse.wb.internal.core.model.description.factory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.StringUtilities;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.graphics.Image;

import java.util.List;
import java.util.Map;

/**
 * Description of single method that should be considered as factory.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class FactoryMethodDescription extends MethodDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryMethodDescription(Class<?> declaringClass) {
    super(declaringClass);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_factory;

  /**
   * @return <code>true</code> if this method should be handled as factory.
   */
  public boolean isFactory() {
    return m_factory;
  }

  /**
   * Sets the factory flag.
   */
  public void setFactory(boolean factory) {
    m_factory = factory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMethod
  //
  ////////////////////////////////////////////////////////////////////////////
  private IMethod m_modelMethod;

  /**
   * @return the {@link IMethod}, from JDT model.
   */
  public IMethod getModelMethod() {
    return m_modelMethod;
  }

  /**
   * Set the {@link IMethod}, from JDT model.
   */
  public void setModelMethod(IMethod modelMethod) {
    m_modelMethod = modelMethod;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Icon
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_icon;

  /**
   * @return the icon for this factory method.
   */
  public Image getIcon() {
    return m_icon;
  }

  /**
   * Sets the icon for this factory method.
   */
  public void setIcon(Image icon) {
    m_icon = icon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_presentationName;

  /**
   * @return the name of this factory, to display for user. Usually <code>null</code>, but if may be
   *         useful if the user is not using a wbp-palette.xml file.
   */
  public String getPresentationName() {
    return m_presentationName;
  }

  /**
   * Sets the name of this factory, to display for user.
   */
  public void setPresentationName(String presentationName) {
    m_presentationName = presentationName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_description;

  /**
   * @return the description text for this factory method.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Sets the description text for this factory method.
   */
  public void setDescription(String description) {
    m_description = StringUtilities.normalizeWhitespaces(description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, String> m_parameters = Maps.newTreeMap();

  /**
   * Adds new parameter.
   * <p>
   * These parameters are used to tweak type-level parameters from {@link ComponentDescription} for
   * separate factory methods. For example factory returns {@link javax.swing.JComponent}, and we
   * don't know, may be it has {@link java.awt.LayoutManager}, may be not. So, user should specify
   * this on factory method level.
   */
  public void addParameter(String name, String value) {
    m_parameters.put(name, value);
  }

  /**
   * @return the value of parameter with given name.
   */
  public String getParameter(String name) {
    return m_parameters.get(name);
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
}
