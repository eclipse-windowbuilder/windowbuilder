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
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.BeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableValueCodeSupport;

import java.util.List;
import java.util.Set;

/**
 * Model for <code>Java Beans</code> object properties.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class BeanPropertyBindableInfo extends PropertyBindableInfo {
  private final IObserveDecorator m_decorator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanPropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Class<?> objectType,
      String reference) {
    super(beanSupport, parent, text, objectType, reference);
    m_decorator = BeanSupport.getDecorator(objectType);
  }

  public BeanPropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      Class<?> objectType,
      String reference,
      IObservePresentation presentation) {
    super(beanSupport, parent, objectType, reference, presentation);
    m_decorator = BeanSupport.getDecorator(objectType);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObservableFactory m_observableFactory = new IObservableFactory() {
    private Type m_type;

    public Type getType() throws Exception {
      if (m_type == null) {
        // calculate type
        if (List.class.isAssignableFrom(getObjectType())) {
          m_type = Type.List;
        } else if (Set.class.isAssignableFrom(getObjectType())) {
          m_type = Type.Set;
        } else {
          m_type = Type.Any;
        }
      }
      return m_type;
    }

    public ObservableInfo createObservable(BindableInfo object,
        BindableInfo property,
        Type type,
        boolean version_1_3) throws Exception {
      Assert.isNotNull(type);
      Assert.isTrue(property == BeanPropertyBindableInfo.this);
      BeanBindableInfo bindableObject = (BeanBindableInfo) object;
      // create observable
      BeanObservableInfo observable = null;
      switch (type) {
        case OnlyValue :
          observable = new ValueBeanObservableInfo(bindableObject, BeanPropertyBindableInfo.this);
          if (version_1_3) {
            observable.setCodeSupport(new ValuePropertyCodeSupport());
          } else {
            observable.setCodeSupport(new BeanObservableValueCodeSupport());
          }
          break;
        case OnlyList :
          observable = new ListBeanObservableInfo(bindableObject, BeanPropertyBindableInfo.this);
          if (version_1_3) {
            observable.setCodeSupport(new ListPropertyCodeSupport());
          } else {
            observable.setCodeSupport(new BeanObservableListCodeSupport());
          }
          break;
        case OnlySet :
          observable = new SetBeanObservableInfo(bindableObject, BeanPropertyBindableInfo.this);
          if (version_1_3) {
            observable.setCodeSupport(new SetPropertyCodeSupport());
          } else {
            observable.setCodeSupport(new BeanObservableSetCodeSupport());
          }
          break;
      }
      //
      Assert.isNotNull(observable);
      return observable;
    }
  };

  @Override
  public final IObservableFactory getObservableFactory() throws Exception {
    return m_observableFactory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveDecorator getDecorator() {
    return m_decorator;
  }
}