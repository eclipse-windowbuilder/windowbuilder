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
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;

import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;

/**
 * Model for viewer properties (see to
 * {@link org.eclipse.jface.databinding.viewers.ViewersObservables}) when
 * {@link org.eclipse.jface.viewers.ISelectionProvider} object isn't widget.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class ViewerObservablePropertyBindableInfo extends PropertyBindableInfo {
  private final IObservableFactory m_factory;
  private final IObserveDecorator m_decorator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerObservablePropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Image image,
      Class<?> objectType,
      String reference,
      IObservableFactory factory,
      IObserveDecorator decorator) {
    super(beanSupport, parent, text, image, objectType, reference);
    m_factory = factory;
    m_decorator = decorator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<IObserveInfo> getChildren(ChildrenContext context) {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObservableFactory getObservableFactory() throws Exception {
    return m_factory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveDecoration
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveDecorator getDecorator() {
    return m_decorator;
  }
}