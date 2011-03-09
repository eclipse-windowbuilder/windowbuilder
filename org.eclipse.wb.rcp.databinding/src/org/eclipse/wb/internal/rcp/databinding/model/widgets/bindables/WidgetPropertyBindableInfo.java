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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleObservePresentation;

import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;

/**
 * {@link BindableInfo} model for <code>SWT</code> widget property.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class WidgetPropertyBindableInfo extends BindableInfo implements IObserveDecoration {
  private final IObservableFactory m_observableFactory;
  private final IObservePresentation m_presentation;
  private final IObserveDecorator m_decorator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetPropertyBindableInfo(String text,
      Class<?> objectType,
      String reference,
      IObserveDecorator decorator) {
    this(text, objectType, reference, SwtObservableFactory.SWT, decorator);
  }

  public WidgetPropertyBindableInfo(String text,
      Class<?> objectType,
      String reference,
      IObservableFactory observableFactory,
      IObserveDecorator decorator) {
    super(objectType, reference);
    m_observableFactory = observableFactory;
    m_presentation = new SimpleObservePresentation(text, objectType);
    m_decorator = decorator;
  }

  public WidgetPropertyBindableInfo(String text,
      Image image,
      Class<?> objectType,
      String reference,
      IObservableFactory observableFactory,
      IObserveDecorator decorator) {
    super(objectType, reference);
    m_observableFactory = observableFactory;
    m_presentation = new SimpleObservePresentation(text, image);
    m_decorator = decorator;
  }

  public WidgetPropertyBindableInfo(WidgetPropertyBindableInfo bindable) {
    super(bindable.getObjectType(), bindable.getReferenceProvider());
    m_observableFactory = bindable.m_observableFactory;
    m_presentation = bindable.m_presentation;
    m_decorator = bindable.m_decorator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BindableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<BindableInfo> getChildren() {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return null;
  }

  public List<IObserveInfo> getChildren(ChildrenContext context) {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }

  public IObserveDecorator getDecorator() {
    return m_decorator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObservableFactory getObservableFactory() throws Exception {
    return m_observableFactory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.WIDGETS;
  }
}