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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;

import java.util.List;

/**
 * Model for observable object {@code BeansObservables.observeDetailSet(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class BeanObservableDetailSetCodeSupport extends BeanObservableDetailCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addDetailSourceCode(List<String> lines,
      CodeGenerationSupport generationSupport,
      DetailBeanObservableInfo observable,
      ObservableInfo masterObservable) throws Exception {
    boolean dontUseDeprecatedMethods =
        Activator.getStore().getBoolean(IPreferenceConstants.DONT_USE_DEPRECATED_METHODS);
    String realmCode =
        dontUseDeprecatedMethods
            ? ""
            : "org.eclipse.core.databinding.observable.Realm.getDefault(), ";
    String observeMethod =
        observable.isPojoBindable0()
            ? " = " + DataBindingsCodeUtils.getPojoObservablesClass() + ".observeDetailSet("
            : " = org.eclipse.core.databinding.beans.BeansObservables.observeDetailSet(";
    lines.add("org.eclipse.core.databinding.observable.set.IObservableSet "
        + observable.getVariableIdentifier()
        + observeMethod
        + realmCode
        + masterObservable.getVariableIdentifier()
        + ", "
        + observable.getDetailPropertyReference()
        + ", "
        + CoreUtils.getClassName(observable.getDetailPropertyType())
        + ".class);");
  }
}