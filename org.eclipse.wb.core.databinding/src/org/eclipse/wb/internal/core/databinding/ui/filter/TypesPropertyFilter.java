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
package org.eclipse.wb.internal.core.databinding.ui.filter;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ArrayUtils;

/**
 * Filter for {@link IObserveInfo} properties over {@link Class} type.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class TypesPropertyFilter extends PropertyFilter {
  private final Class<?>[] m_types;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TypesPropertyFilter(String name, Image image, Class<?>... types) {
    super(name, image);
    m_types = types;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final boolean select(Class<?> type) {
    return ArrayUtils.contains(m_types, type);
  }
}