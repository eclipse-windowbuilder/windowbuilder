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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * This filter accept any property.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class AllPropertiesFilter extends PropertyFilter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AllPropertiesFilter(String name, Image image) {
    super(name, image);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean select(Viewer viewer, IObserveInfo propertyObserve) {
    return true;
  }
}