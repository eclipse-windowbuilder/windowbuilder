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

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ElPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.ElPropertyUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el.ElPropertyUiConfiguration;
import org.eclipse.wb.internal.swing.databinding.ui.providers.TypeImageProvider;

import java.util.Collections;
import java.util.List;

/**
 * {@link ObserveInfo} model for {@link org.jdesktop.beansbinding.ELProperty}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class ElPropertyObserveInfo extends ObserveInfo implements IObserveDecoration {
  private static final ElPropertyUiConfiguration CONFIGURATION = new ElPropertyUiConfiguration();
  private final ObserveInfo m_parent;
  private final IObservePresentation m_presentation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElPropertyObserveInfo(ObserveInfo parent, IGenericType objectType) {
    super(objectType, StringReferenceProvider.EMPTY);
    m_parent = parent instanceof BeanPropertyObserveInfo ? parent : null;
    m_presentation =
        new SimpleObservePresentation("<EL Expression>",
            "${XXX}",
            TypeImageProvider.EL_PROPERTY_IMAGE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return null;
  }

  @Override
  public ObserveCreationType getCreationType() {
    return ObserveCreationType.AnyProperty;
  }

  @Override
  public PropertyInfo createProperty(ObserveInfo observeObject) throws Exception {
    return new ElPropertyInfo(observeObject.getObjectType(),
        ClassGenericType.OBJECT_CLASS,
        m_parent == null ? null : m_parent.createProperty(observeObject),
        "");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return m_parent;
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
    return IObserveDecorator.BOLD;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      ObserveInfo observeObject,
      PropertyInfo observeAstProperty) throws Exception {
    providers.add(new ElPropertyUiContentProvider(CONFIGURATION,
        (ElPropertyInfo) observeAstProperty));
    providers.add(new SeparatorUiContentProvider());
  }
}