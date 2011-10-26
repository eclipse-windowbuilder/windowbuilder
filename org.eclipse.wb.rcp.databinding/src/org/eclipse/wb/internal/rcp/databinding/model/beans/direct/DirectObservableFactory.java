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
package org.eclipse.wb.internal.rcp.databinding.model.beans.direct;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailValueCodeSupport;

/**
 * {@link IObservableFactory} for direct observable objects.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class DirectObservableFactory implements IObservableFactory {
  private final Type m_type;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DirectObservableFactory(Type type) {
    m_type = type;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservableFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public final Type getType() throws Exception {
    return m_type;
  }

  public final ObservableInfo createObservable(BindableInfo object,
      BindableInfo property,
      Type type,
      boolean version_1_3) throws Exception {
    BeanBindableInfo bindableObject = (BeanBindableInfo) object;
    DirectPropertyBindableInfo directProperty = (DirectPropertyBindableInfo) property;
    return createObservable(bindableObject, directProperty, type, version_1_3);
  }

  /**
   * Create {@link ObservableInfo} for given <code>bindableObject</code> and <code>property</code>
   * with given {@link Type}.
   */
  protected abstract ObservableInfo createObservable(BeanBindableInfo bindableObject,
      DirectPropertyBindableInfo property,
      Type type,
      boolean version_1_3) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Factory for create {@link DirectPropertyObservableInfo} with given {@link Type}.
   */
  public static IObservableFactory forProperty(Type type) {
    return new DirectObservableFactory(type) {
      @Override
      protected ObservableInfo createObservable(BeanBindableInfo bindableObject,
          DirectPropertyBindableInfo property,
          Type type,
          boolean version_1_3) throws Exception {
        return new DirectPropertyObservableInfo(bindableObject, property);
      }
    };
  }

  /**
   * Factory for create {@link DirectObservableInfo} with given {@link Type}.
   */
  public static IObservableFactory forBean(Type type) {
    return new DirectObservableFactory(type) {
      @Override
      protected ObservableInfo createObservable(BeanBindableInfo bindableObject,
          DirectPropertyBindableInfo property,
          Type type,
          boolean version_1_3) throws Exception {
        return new DirectObservableInfo(bindableObject, property);
      }
    };
  }

  /**
   * Factory with type {@link Type#Detail} for create master-detail observable's.
   */
  public static IObservableFactory forDetailBean() {
    return new DirectObservableFactory(Type.Detail) {
      @Override
      protected ObservableInfo createObservable(BeanBindableInfo bindableObject,
          DirectPropertyBindableInfo property,
          Type type,
          boolean version_1_3) throws Exception {
        // create master
        DirectObservableInfo masterObservable = new DirectObservableInfo(bindableObject, property);
        // create detail
        DetailBeanObservableInfo observable = null;
        switch (type) {
          case OnlyValue :
            observable = new DetailValueBeanObservableInfo(masterObservable, null, null, null);
            if (version_1_3) {
              observable.setCodeSupport(new ValuePropertyDetailCodeSupport());
            } else {
              observable.setCodeSupport(new BeanObservableDetailValueCodeSupport());
            }
            break;
          case OnlyList :
            observable = new DetailListBeanObservableInfo(masterObservable, null, null, null);
            if (version_1_3) {
              observable.setCodeSupport(new ListPropertyDetailCodeSupport());
            } else {
              observable.setCodeSupport(new BeanObservableDetailListCodeSupport());
            }
            break;
          case OnlySet :
            observable = new DetailSetBeanObservableInfo(masterObservable, null, null, null);
            if (version_1_3) {
              observable.setCodeSupport(new SetPropertyDetailCodeSupport());
            } else {
              observable.setCodeSupport(new BeanObservableDetailSetCodeSupport());
            }
            break;
        }
        Assert.isNotNull(observable);
        observable.setPojoBindable(masterObservable.isPojoBindable());
        return observable;
      }
    };
  }
}