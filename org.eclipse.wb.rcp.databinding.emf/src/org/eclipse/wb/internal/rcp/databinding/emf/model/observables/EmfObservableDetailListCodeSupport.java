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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailCodeSupport;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class EmfObservableDetailListCodeSupport extends BeanObservableDetailCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // BeanObservableDetailCodeSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addDetailSourceCode(List<String> lines,
      CodeGenerationSupport generationSupport,
      DetailBeanObservableInfo observable,
      ObservableInfo masterObservable) throws Exception {
    DetailEmfObservableInfo emfObservable = (DetailEmfObservableInfo) observable;
    //
    lines.add("org.eclipse.core.databinding.observable.list.IObservableList "
        + observable.getVariableIdentifier()
        + emfObservable.getPropertiesSupport().getEMFObservablesCode(
            "observeDetailList(org.eclipse.core.databinding.observable.Realm.getDefault(), ")
        + masterObservable.getVariableIdentifier()
        + ", "
        + emfObservable.getDetailPropertyReference()
        + ");");
  }
}