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
package org.eclipse.wb.internal.swing.databinding.model.beans;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;

/**
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class VirtualObserveInfo extends BeanObserveInfo {
  private final IObservePresentation m_presentation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualObserveInfo() {
    super(null, null, ClassGenericType.LIST_CLASS, StringReferenceProvider.EMPTY);
    setBindingDecoration(SwtResourceManager.TOP_LEFT);
    m_presentation =
        new SimpleObservePresentation("[Virtual]", "[Virtual]", Activator.getImage("virtual.png"));
    setProperties(ImmutableList.<ObserveInfo>of(new ObjectPropertyObserveInfo(getObjectType())));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ObserveCreationType getCreationType() {
    return ObserveCreationType.VirtualBinding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }
}