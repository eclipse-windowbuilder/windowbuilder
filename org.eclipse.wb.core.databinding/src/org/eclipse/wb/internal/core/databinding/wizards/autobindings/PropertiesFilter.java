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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * {@link ViewerFilter} for hide/show the properties which can't be bound.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public final class PropertiesFilter extends ViewerFilter {
  private final DescriptorContainer m_widgetContainer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesFilter(DescriptorContainer widgetContainer) {
    m_widgetContainer = widgetContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ViewerFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    return m_widgetContainer.getDefaultDescriptor(element, false) != null;
  }
}