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
package org.eclipse.wb.internal.rcp.databinding.ui.filter;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * Filter for {@link BindableInfo} properties over {@link Class} type.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TypesPropertyFilter
    extends
      org.eclipse.wb.internal.core.databinding.ui.filter.TypesPropertyFilter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TypesPropertyFilter(String name, Image image, Class<?>... types) {
    super(name, image, types);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean select(Viewer viewer, IObserveInfo propertyObserve) {
    BindableInfo bindable = (BindableInfo) propertyObserve;
    return select(bindable.getObjectType());
  }
}