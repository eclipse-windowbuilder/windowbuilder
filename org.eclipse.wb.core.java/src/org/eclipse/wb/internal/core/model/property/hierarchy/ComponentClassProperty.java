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
package org.eclipse.wb.internal.core.model.property.hierarchy;

import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Property that shows class name for component and hierarchy as value hint.
 *
 * @author lobas_av
 * @coverage core.model.property
 */
public final class ComponentClassProperty extends Property {
  private static final String TITLE_TOOLTIP = ModelMessages.ComponentClassProperty_tooltip;
  private final String m_componentClassName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentClassProperty(IJavaProject javaProject, Class<?> componentClass) {
    super(new ComponentClassPropertyEditor(javaProject, componentClass));
    setCategory(PropertyCategory.system(7));
    m_componentClassName = componentClass.getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return "Class";
  }

  @Override
  public Object getValue() throws Exception {
    return m_componentClassName;
  }

  @Override
  public boolean isModified() throws Exception {
    return false;
  }

  @Override
  public void setValue(Object value) throws Exception {
  }

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    // tooltip
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(new PropertyTooltipTextProvider() {
        @Override
        protected String getText(Property property) throws Exception {
          return TITLE_TOOLTIP;
        }
      });
    }
    // other
    return super.getAdapter(adapter);
  }
}