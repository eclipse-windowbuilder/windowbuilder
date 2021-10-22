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
package org.eclipse.wb.internal.swing.databinding.ui.filters;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.Messages;

import org.eclipse.jface.viewers.Viewer;

/**
 * Show advanced properties filter.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ShowAdvancedPropertyFilter extends PropertyFilter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ShowAdvancedPropertyFilter() {
    super(Messages.ShowAdvancedPropertyFilter_title, Activator.getImage("show_advanced.png"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean select(Viewer viewer, IObserveInfo propertyObserve) {
    IObserveDecoration observeDecoration = (IObserveDecoration) propertyObserve;
    return observeDecoration.getDecorator() != IObserveDecorator.HIDDEN;
  }
}