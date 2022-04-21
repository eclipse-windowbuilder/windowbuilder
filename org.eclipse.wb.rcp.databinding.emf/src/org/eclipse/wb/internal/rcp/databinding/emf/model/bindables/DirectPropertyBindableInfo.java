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

import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableDetailValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

/**
 * XXX
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class DirectPropertyBindableInfo extends EPropertyBindableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirectPropertyBindableInfo(Class<?> objectType) {
    super(objectType, "", new SimpleObservePresentation(DirectObservableInfo.DETAIL_PROPERTY_NAME,
        TypeImageProvider.DIRECT_IMAGE));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObservableFactory m_observableFactory = new IObservableFactory() {
    @Override
    public Type getType() throws Exception {
      return Type.Detail;
    }

    @Override
    public ObservableInfo createObservable(BindableInfo object,
        BindableInfo property,
        Type type,
        boolean version_1_3) throws Exception {
      EObjectBindableInfo eObject = (EObjectBindableInfo) object;
      PropertiesSupport propertiesSupport = eObject.getPropertiesSupport();
      // create master
      DirectObservableInfo masterObservable = new DirectObservableInfo(object, property);
      // create detail
      ObservableInfo observable = null;
      switch (type) {
        case OnlyValue :
          observable = new DetailValueEmfObservableInfo(masterObservable, propertiesSupport);
          observable.setCodeSupport(new EmfObservableDetailValueCodeSupport());
          break;
        case OnlyList :
          observable = new DetailListEmfObservableInfo(masterObservable, propertiesSupport);
          observable.setCodeSupport(new EmfObservableDetailListCodeSupport());
          break;
      }
      Assert.isNotNull(observable);
      return observable;
    }
  };

  @Override
  public IObservableFactory getObservableFactory() throws Exception {
    return m_observableFactory;
  }
}