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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.CompoundReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.JavaInfoReferenceProvider;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

/**
 * Model for field based <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class FieldBeanBindableInfo extends BeanBindableInfo {
  private JavaInfo m_hostJavaInfo;
  private VariableDeclarationFragment m_fragment;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldBeanBindableInfo(BeanSupport beanSupport,
      VariableDeclarationFragment fragment,
      Class<?> objectType,
      JavaInfo javaInfo) throws Exception {
    this(beanSupport, fragment, objectType, new FragmentReferenceProvider(fragment), javaInfo);
  }

  public FieldBeanBindableInfo(BeanSupport beanSupport,
      VariableDeclarationFragment fragment,
      Class<?> objectType,
      IReferenceProvider referenceProvider,
      JavaInfo javaInfo) throws Exception {
    super(beanSupport, null, objectType, referenceProvider, javaInfo);
    setBindingDecoration(SwtResourceManager.TOP_RIGHT);
    m_hostJavaInfo = fragment == null ? javaInfo : null;
    m_fragment = fragment;
    m_children = new ArrayList<>();
    // add "getter" properties contains sub properties to children
    for (PropertyBindableInfo property : getProperties()) {
      if (property instanceof BeanPropertyDescriptorBindableInfo) {
        BeanPropertyDescriptorBindableInfo descriptorProperty =
            (BeanPropertyDescriptorBindableInfo) property;
        PropertyDescriptor descriptor = descriptorProperty.getDescriptor();
        //
        if (BeanSupport.isGetter(descriptor)) {
          String propertyName = descriptor.getReadMethod().getName() + "()";
          MethodBeanBindableInfo newChildren = new MethodBeanBindableInfo(beanSupport,
              this,
              descriptor.getPropertyType(),
              new CompoundReferenceProvider(getReferenceProvider(), "." + propertyName),
              new StringReferenceProvider(getReference() + "." + propertyName));
          //
          if (newChildren.getProperties().size() > 1) {
            m_children.add(newChildren);
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Access to field {@link VariableDeclarationFragment}.
   */
  public VariableDeclarationFragment getFragment() {
    return m_fragment;
  }

  public JavaInfo getHostJavaInfo() {
    return m_hostJavaInfo;
  }

  public void update(BeansObserveTypeContainer container) throws Exception {
    BeanBindablePresentation presentation = (BeanBindablePresentation) getPresentation();
    //
    JavaInfo oldJavaInfo = m_hostJavaInfo;
    if (m_hostJavaInfo == null) {
      oldJavaInfo = presentation.getJavaInfo();
    }
    //
    if (oldJavaInfo != null && oldJavaInfo.isDeleted()) {
      JavaInfo newJavaInfo =
          container.getJavaInfoRepresentedBy(m_fragment.getName().getIdentifier());
      if (newJavaInfo == null) {
        return;
      }
      Class<?> componentClass = newJavaInfo.getDescription().getComponentClass();
      //
      setObjectType(componentClass);
      //
      presentation.setJavaInfo(newJavaInfo);
      presentation.setObjectType(componentClass);
      presentation.setBeanImage(getBeanSupport().getBeanImage(componentClass, newJavaInfo));
      //
      if (m_hostJavaInfo != null) {
        m_hostJavaInfo = newJavaInfo;
        FragmentReferenceProvider fragmentProvider =
            (FragmentReferenceProvider) getReferenceProvider();
        JavaInfoReferenceProvider javaInfoProvider =
            (JavaInfoReferenceProvider) fragmentProvider.getProvider();
        javaInfoProvider.setJavaInfo(newJavaInfo);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createBinding(AbstractBindingInfo binding) throws Exception {
    super.createBinding(binding);
    // ensure convert local variable to field
    if (m_fragment == null) {
      // disable synchronize
      DatabindingsProvider provider = getBeanSupport().getProvider();
      provider.setSynchronizeObserves(false);
      //
      try {
        // do convert
        m_hostJavaInfo.getVariableSupport().convertLocalToField();
        // prepare fragment
        TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_hostJavaInfo);
        String fieldName = m_hostJavaInfo.getVariableSupport().getName();
        m_fragment = AstNodeUtils.getFieldFragmentByName(typeDeclaration, fieldName);
        // configure reference
        FragmentReferenceProvider referenceProvider =
            (FragmentReferenceProvider) getReferenceProvider();
        referenceProvider.setFragment(m_fragment);
      } finally {
        // do synchronize
        provider.setSynchronizeObserves(true);
        provider.synchronizeObserves();
      }
    }
  }
}