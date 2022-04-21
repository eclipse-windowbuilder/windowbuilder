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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.rcp.databinding.emf.model.EmfObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectFieldModelSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableInfo;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Model for EMF objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class EObjectBindableInfo extends BindableInfo {
  private final VariableDeclarationFragment m_fragment;
  private final PropertiesSupport m_propertiesSupport;
  private final IObservePresentation m_presentation;
  private final List<EPropertyBindableInfo> m_properties = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EObjectBindableInfo(Class<?> objectType,
      VariableDeclarationFragment fragment,
      PropertiesSupport propertiesSupport,
      IModelResolver resolver) throws Exception {
    super(objectType, new FragmentReferenceProvider(fragment));
    setBindingDecoration(SwtResourceManager.TOP_RIGHT);
    m_fragment = fragment;
    m_propertiesSupport = propertiesSupport;
    m_presentation = new EObjectObservePresentation(this);
    // add properties
    for (PropertyInfo propertyInfo : propertiesSupport.getProperties(objectType)) {
      m_properties.add(new EPropertyBindableInfo(propertiesSupport,
          null,
          propertyInfo.type,
          propertyInfo.name,
          propertyInfo.reference));
    }
    // check observable object
    if (CoreUtils.isAssignableFrom(m_propertiesSupport.getIObservableValue(), objectType)) {
      DirectPropertyBindableInfo property = new DirectPropertyBindableInfo(objectType);
      m_properties.add(property);
      //
      if (resolver != null) {
        DirectObservableInfo directObservable = new DirectObservableInfo(this, property);
        resolver.addModelSupport(new DirectFieldModelSupport(directObservable));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public VariableDeclarationFragment getFragment() {
    return m_fragment;
  }

  public PropertiesSupport getPropertiesSupport() {
    return m_propertiesSupport;
  }

  @Override
  public EPropertyBindableInfo resolvePropertyReference(String reference) throws Exception {
    if (reference.startsWith("org.eclipse.emf.databinding.FeaturePath.fromList(")) {
      reference = StringUtils.substringBetween(reference, "(", ")");
      String[] references = StringUtils.split(reference, ", ");
      //
      for (EPropertyBindableInfo property : m_properties) {
        if (references[0].equals(property.getReference())) {
          return property.resolvePropertyReference(references, 1);
        }
      }
    } else {
      for (EPropertyBindableInfo property : m_properties) {
        if (reference.equals(property.getReference())) {
          return property;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObserveInfo getParent() {
    return null;
  }

  @Override
  protected List<BindableInfo> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public List<IObserveInfo> getChildren(ChildrenContext context) {
    if (context == ChildrenContext.ChildrenForPropertiesTable) {
      return CoreUtils.cast(m_properties);
    }
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObservePresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ObserveType getType() {
    return EmfObserveTypeContainer.TYPE;
  }
}